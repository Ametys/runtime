/*
 *  Copyright 2009 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ametys.runtime.plugins.core.dispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.util.location.LocatedException;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.IgnoreRootHandler;

/**
 * This generator read the request incoming from the client org.ametys.servercomm.ServerComm component,
 * then dispatch it to given url
 * and aggregate the result 
 */
public class DispatchGenerator extends ServiceableGenerator
{
    private static JsonFactory _jsonFactory = new JsonFactory();
    private static ObjectMapper _objectMapper = new ObjectMapper();

    private SourceResolver _resolver;
    private SAXParser _saxParser;
    private DispatchPostProcessExtensionPoint _dispatchPostProcessExtensionPoint;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        
        _resolver = (SourceResolver) smanager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
        _dispatchPostProcessExtensionPoint = (DispatchPostProcessExtensionPoint) manager.lookup(DispatchPostProcessExtensionPoint.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String parametersAsJSONString = _getRequestBody();
        Map<String, Object> parametersAsMap = _getMapFromJsonString(parametersAsJSONString);
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "responses");
        
        _dispatching(parametersAsMap);
        
        XMLUtils.endElement(contentHandler, "responses");
        contentHandler.endDocument();
    }

    private String _getRequestBody()
    {
        return ObjectModelHelper.getRequest(objectModel).getParameter("content");
    }

    @SuppressWarnings("unchecked")
    private void _dispatching(Map<String, Object> parametersAsMap) throws SAXException
    {
        Map<String, Object> attributes = _saveRequestAttributes();
        
        for (String parameterKey : parametersAsMap.keySet())
        {
            Map<String, Object> parameterObject = (Map<String, Object>) parametersAsMap.get(parameterKey);
            
            String plugin = (String) parameterObject.get("plugin");
            String relativeUrl = (String) parameterObject.get("url");
            String responseType = (String) parameterObject.get("responseType");
            
            Map<String, Object> requestParameters = (Map<String, Object>) parameterObject.get("parameters");
            
            Source response = null;
            InputStream is = null;

            try
            {
                String url = _createUrl(plugin, relativeUrl, requestParameters);
                
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Dispatching url '" + url + "'");
                }

                response = _resolver.resolveURI(url, null, requestParameters);

                ResponseHandler responseHandler = new ResponseHandler(contentHandler, parameterKey, "200");
                is = response.getInputStream();
                
                if ("xml".equalsIgnoreCase(responseType))
                {
                    // DO NOT USE SitemapSource.toSAX in this case
                    _saxParser.parse(new InputSource(is), responseHandler);
                }
                else 
                {
                    responseHandler.startDocument();
                    
                    String data = IOUtils.toString(is, "UTF-8");
                    if ("xml2text".equalsIgnoreCase(responseType))
                    {
                        // removing xml prolog and surrounding 'text' tag
                        data = data.substring(data.indexOf(">", data.indexOf("?>") + 2) + 1, data.lastIndexOf("<"));
                    }
                    XMLUtils.data(responseHandler, data);
                    
                    responseHandler.endDocument();
                }
            }
            catch (Exception e)
            {
                String message = String.format("Can not dispatch request '%s' : '%s' '%s' '%s'",  parameterKey , plugin,  relativeUrl,  requestParameters);
                // Ensure SAXException are unrolled the right way
                getLogger().error(message, new LocatedException(message, e));
                
                Throwable t = e;
                while (t.getCause() != null || (t instanceof SAXException && ((SAXException) t).getException() != null))
                {
                    if (t instanceof SAXException)
                    {
                        t = ((SAXException) t).getException();
                    }
                    else
                    {
                        t = t.getCause();
                    }
                }
                
                String code = "500";
                if (t instanceof ResourceNotFoundException || t.toString().startsWith("org.apache.cocoon.ResourceNotFoundException:"))
                {
                    code = "404";
                }
                
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("id", parameterKey);
                attrs.addCDATAAttribute("code", code);
                
                String exceptionMessage = t.getMessage();
                
                XMLUtils.startElement(contentHandler, "response", attrs);
                XMLUtils.createElement(contentHandler, "message", _escape(exceptionMessage != null ? exceptionMessage : ""));
                XMLUtils.createElement(contentHandler, "stacktrace", _escape(ExceptionUtils.getFullStackTrace(t)));
                XMLUtils.endElement(contentHandler, "response");
            }
            finally
            {
                IOUtils.closeQuietly(is);
                _resolver.release(response);
                
                for (String extension : _dispatchPostProcessExtensionPoint.getExtensionsIds())
                {
                    DispatchRequestPostProcess processor = _dispatchPostProcessExtensionPoint.getExtension(extension);
                    processor.process(ObjectModelHelper.getRequest(objectModel));
                }
                
                _restoreRequestAttributes(attributes);
            }
        }
    }
    
    /**
     * Clean the requests attributes abd add those in the map
     * @param attributes
     */
//    @SuppressWarnings("unchecked")
    private void _restoreRequestAttributes(Map<String, Object> attributes)
    {
//        Request request = ObjectModelHelper.getRequest(objectModel);
//
//        List<String> attrNames = Collections.list(request.getAttributeNames());
//        for (String attrName : attrNames)
//        {
//            request.removeAttribute(attrName);
//        }
//        
//        for (String attrName : attributes.keySet())
//        {
//            request.setAttribute(attrName, attributes.get(attrName));
//        }
    }

    /**
     * Transforms the request attributes into a map and clean the attributes
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> _saveRequestAttributes()
    {
        Map<String, Object> attrs = new HashMap<String, Object>();
        
//        Request request = ObjectModelHelper.getRequest(objectModel);
//        
//        Enumeration<String> attrNames = request.getAttributeNames();
//        while (attrNames.hasMoreElements())
//        {
//            String attrName = attrNames.nextElement();
//            Object value = request.getAttribute(attrName);
//            
//            attrs.put(attrName, value);
//        }

        return attrs;
    }

    private String _escape(String value)
    {
        return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;".replaceAll(">", "&gt;"));
    }
    
    @SuppressWarnings("unchecked")
    private String _createUrl(String plugin, String relativeUrl, Map<String, Object> requestParameters)
    {
        StringBuffer url = new StringBuffer("cocoon://");
        if (plugin != null)
        {
            url.append("_plugins/");
            url.append(plugin);
            url.append("/");
        }
        if (relativeUrl.length() != 0 && relativeUrl.charAt(0) == '/')
        {
            url.append(relativeUrl.substring(1));
        }
        else
        {
            url.append(relativeUrl);
        }
        
        if (relativeUrl.indexOf("?") == -1 && requestParameters != null)
        {
            // no existing parameters in request
            url.append("?");
            
            for (String key : requestParameters.keySet())
            {
                Object value = requestParameters.get(key);
                if (value instanceof List)
                {
                    List<Object> valueAsList = (List<Object>) value;
                    for (Object v : valueAsList)
                    {
                        url.append(key);
                        url.append("=");
                        url.append(String.valueOf(v).replaceAll("%", "%25").replaceAll("=", "%3D").replaceAll("&", "%26").replaceAll("\\+", "%2B"));
                        url.append("&");
                    }
                }
                else 
                {
                    url.append(key);
                    url.append("=");
                    url.append((String.valueOf(value)).replaceAll("%", "%25").replaceAll("=", "%3D").replaceAll("&", "%26").replaceAll("\\+", "%2B"));
                    url.append("&");
                }
            }
            // TODO Faire l'opération inverse : paramètres de requetes présents => create map
        }
        
        return url.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> _getMapFromJsonString(String jsonString)
    {
        try 
        {
            if (StringUtils.isNotBlank(jsonString)) 
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Transforming json string into map '" + jsonString + "'");
                }

                JsonParser jParser = _jsonFactory.createJsonParser(new StringReader(jsonString));
                Map<String, Object> map = _objectMapper.readValue(jParser, LinkedHashMap.class);
                return map;
            } 
            else 
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Transforming empty or null json string into map.");
                }

                return Collections.EMPTY_MAP;
            }
        } 
        catch (Exception e) 
        {
            getLogger().error("An error occured while transforming jsonstring into map '" + jsonString + "'", e);
            return Collections.EMPTY_MAP;
        }
    }
    
    /**
     * Wrap the handler ignore start and end document, but adding a response tag. 
     */
    public class ResponseHandler extends IgnoreRootHandler
    {
        private String _parameterKey;
        private ContentHandler _handler;
        private String _code;
        
        /**
         * Create the wrapper
         * @param handler The content handler to wrap
         * @param parameterKey The id of the response
         * @param code The status code of the response
         */
        public ResponseHandler(ContentHandler handler, String parameterKey, String code)
        {
            super(handler);
            _handler = handler;
            _parameterKey = parameterKey;
            _code = code;
        }
        
        @Override
        public void startDocument() throws SAXException
        {
            super.startDocument();

            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", _parameterKey);
            attrs.addCDATAAttribute("code", _code);

            XMLUtils.startElement(_handler, "response", attrs);
        }
        
        @Override
        public void endDocument() throws SAXException
        {
            XMLUtils.endElement(_handler, "response");
            super.endDocument();
        }
    }
}
