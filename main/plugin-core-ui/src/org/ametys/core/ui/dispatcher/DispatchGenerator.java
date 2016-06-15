/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.ui.dispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ametys.core.util.IgnoreRootHandler;
import org.ametys.core.util.JSONUtils;

/**
 * This generator read the request incoming from the client org.ametys.servercomm.ServerComm component,
 * then dispatch it to given url
 * and aggregate the result 
 */
public class DispatchGenerator extends ServiceableGenerator
{
    private SourceResolver _resolver;
    private SAXParser _saxParser;
    private DispatchProcessExtensionPoint _dispatchProcessExtensionPoint;
    private JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        
        _resolver = (SourceResolver) smanager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
        _dispatchProcessExtensionPoint = (DispatchProcessExtensionPoint) manager.lookup(DispatchProcessExtensionPoint.ROLE);
        _jsonUtils = (JSONUtils) manager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String parametersAsJSONString = _getRequestBody();
        Map<String, Object> parametersAsMap = _jsonUtils.convertJsonToMap(parametersAsJSONString);
        
        String contextAsJSONString = _getRequestContext();
        Map<String, Object> contextAsMap = _jsonUtils.convertJsonToMap(contextAsJSONString);

        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "responses");
        
        _dispatching(parametersAsMap, contextAsMap);
        
        XMLUtils.endElement(contentHandler, "responses");
        contentHandler.endDocument();
    }

    private String _getRequestBody()
    {
        return ObjectModelHelper.getRequest(objectModel).getParameter("content");
    }

    private String _getRequestContext()
    {
        return ObjectModelHelper.getRequest(objectModel).getParameter("context.parameters");
    }

    @SuppressWarnings("unchecked")
    private void _dispatching(Map<String, Object> parametersAsMap, Map<String, Object> contextAsMap) throws SAXException
    {
        Map<String, Object> attributes = _saveRequestAttributes();
        
        for (String parameterKey : parametersAsMap.keySet())
        {
            for (String extension : _dispatchProcessExtensionPoint.getExtensionsIds())
            {
                DispatchRequestProcess processor = _dispatchProcessExtensionPoint.getExtension(extension);
                processor.preProcess(ObjectModelHelper.getRequest(objectModel));
            }

            _setContextInRequestAttributes(contextAsMap);

            Map<String, Object> parameterObject = (Map<String, Object>) parametersAsMap.get(parameterKey);

            String pluginOrWorkspace = (String) parameterObject.get("pluginOrWorkspace");
            String relativeUrl = (String) parameterObject.get("url");
            String responseType = (String) parameterObject.get("responseType");
            
            Map<String, Object> requestParameters = (Map<String, Object>) parameterObject.get("parameters");
            
            Source response = null;

            ResponseHandler responseHandler = null;
            try
            {
                String url = _createUrl(pluginOrWorkspace, relativeUrl, requestParameters != null ? requestParameters : new HashMap<String, Object>());
                
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Dispatching url '" + url + "'");
                }

                response = _resolver.resolveURI(url, null, requestParameters);

                responseHandler = new ResponseHandler(contentHandler, parameterKey, "200");
                
                try (InputStream is = response.getInputStream())
                {
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
            }
            catch (Throwable e)
            {
                String message = String.format("Can not dispatch request '%s' : '%s' '%s' '%s'",  parameterKey , pluginOrWorkspace,  relativeUrl,  requestParameters);
                
                // Ensure SAXException are unrolled the right way
                getLogger().error(message, new LocatedException(message, e));
                
                // Makes the output xml ok 
                if (responseHandler != null)
                {
                    responseHandler.exceptionFinish();
                }
                
                Throwable t = _unroll(e);
                
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
                _resolver.release(response);
                
                for (String extension : _dispatchProcessExtensionPoint.getExtensionsIds())
                {
                    DispatchRequestProcess processor = _dispatchProcessExtensionPoint.getExtension(extension);
                    processor.postProcess(ObjectModelHelper.getRequest(objectModel));
                }
                
                _restoreRequestAttributes(attributes);
            }
        }
    }
    
    private Throwable _unroll(Throwable initial)
    {
        Throwable t = initial;
        while (t.getCause() != null || t instanceof SAXException && ((SAXException) t).getException() != null)
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
        
        return t;
    }
    
    /**
     * Clean the requests attributes and add those in the map
     * @param attributes The attributes to restore
     */
    @SuppressWarnings("unchecked")
    private void _restoreRequestAttributes(Map<String, Object> attributes)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        List<String> attrNames = Collections.list(request.getAttributeNames());
        for (String attrName : attrNames)
        {
            request.removeAttribute(attrName);
        }
        
        for (String attrName : attributes.keySet())
        {
            request.setAttribute(attrName, attributes.get(attrName));
        }
    }
    
    private void _setContextInRequestAttributes(Map<String, Object> contextAsMap)
    {
        if (contextAsMap != null)
        {
            Request request = ObjectModelHelper.getRequest(objectModel);
    
            for (String name : contextAsMap.keySet())
            {
                request.setAttribute(name, contextAsMap.get(name));
            }
        }
    }

    /**
     * Transforms the request attributes into a map and clean the attributes
     * @return A copy of all the request attributes
     */
    private Map<String, Object> _saveRequestAttributes()
    {
        Map<String, Object> attrs = new HashMap<>();
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        Enumeration<String> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements())
        {
            String attrName = attrNames.nextElement();
            Object value = request.getAttribute(attrName);
            
            attrs.put(attrName, value);
        }

        return attrs;
    }

    private String _escape(String value)
    {
        return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;".replaceAll(">", "&gt;"));
    }
    
    /**
     * Create url to call
     * @param pluginOrWorkspace the plugin or workspace name
     * @param relativeUrl the relative url
     * @param requestParameters the request parameters. Can not be null.
     * @return the full url
     */
    @SuppressWarnings("unchecked")
    protected String _createUrl(String pluginOrWorkspace, String relativeUrl, Map<String, Object> requestParameters)
    {
        StringBuilder url = new StringBuilder();
        
        String urlPrefix = _getUrlPrefix(pluginOrWorkspace);
        url.append(urlPrefix);
        
        url.append(_getRelativePath(relativeUrl));
        
        int queryIndex = relativeUrl.indexOf("?");
        
        if (queryIndex == -1 && !requestParameters.isEmpty())
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
                        if (v != null)
                        {
                            url.append(_buildQueryParameter(key, v));
                        }
                    }
                }
                else if (value != null)
                {
                    url.append(_buildQueryParameter(key, value));
                }
            }
        }
        else if (queryIndex > 0)
        {
            url.append("?");
            
            String queryUrl = relativeUrl.substring(queryIndex + 1, relativeUrl.length());
            String[] queryParameters = queryUrl.split("&");
            
            for (String queryParameter : queryParameters)
            {
                if (StringUtils.isNotBlank(queryParameter))
                {
                    String[] part = queryParameter.split("=");
                    String key = part[0];
                    String v = part.length > 1 ? part[1] : "";
                    try
                    {
                        String value = URLDecoder.decode(v, "UTF-8");
                        url.append(_buildQueryParameter(key, value));
                        
                        if (!requestParameters.containsKey(key))
                        {
                            requestParameters.put(key, value);
                        }
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        getLogger().error("Unsupported encoding for request parameter '" + key + "' and value '" + v + "'", e);
                    }
                }
            }
        }
        
        return url.toString();
    }
    
    private String _getRelativePath(String url)
    {
        int beginIndex = url.length() != 0 && url.charAt(0) == '/' ? 1 : 0;
        int endIndex = url.indexOf("?");
        return endIndex == -1 ? url.substring(beginIndex) : url.substring(beginIndex, endIndex);
    }
    
    private StringBuilder _buildQueryParameter(String key, Object value)
    {
        StringBuilder queryParameter = new StringBuilder();
        queryParameter.append(key);
        queryParameter.append("=");
        queryParameter.append(String.valueOf(value).replaceAll("%", "%25").replaceAll("=", "%3D").replaceAll("&", "%26").replaceAll("\\+", "%2B"));
        queryParameter.append("&");
        
        return queryParameter;
    }
    
    /**
     * Get the url prefix
     * @param pluginOrWorkspace the plugin or workspace name
     * @return the url prefix
     */
    protected String _getUrlPrefix (String pluginOrWorkspace)
    {
        StringBuffer url = new StringBuffer("cocoon://");
        if (pluginOrWorkspace != null && !pluginOrWorkspace.startsWith("_"))
        {
            url.append("_plugins/");
            url.append(pluginOrWorkspace);
            url.append("/");
        }
        else if (pluginOrWorkspace != null)
        {
            url.append(pluginOrWorkspace);
            url.append("/");
        }
        
        return url.toString();
    }
    
    /**
     * Wrap the handler ignore start and end document, but adding a response tag. 
     */
    public static class ResponseHandler extends IgnoreRootHandler
    {
        private final String _parameterKey;
        private final ContentHandler _handler;
        private final String _code;
        
        private final List<String> _startedElements;
        
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
            _startedElements = new ArrayList<>();
        }
        
        /**
         * Finish abruptly this handler to obtain a correct XML
         * @throws SAXException if an error occurred
         */
        public void exceptionFinish() throws SAXException
        {
            while (_startedElements.size() > 0)
            {
                XMLUtils.endElement(_handler, _startedElements.get(_startedElements.size() - 1));
                _startedElements.remove(_startedElements.size() - 1);
            }
        }
        
        @Override
        public void startDocument() throws SAXException
        {
            super.startDocument();

            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", _parameterKey);
            attrs.addCDATAAttribute("code", _code);
            XMLUtils.startElement(_handler, "response", attrs);

            _startedElements.add("response");
        }
        
        @Override
        public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException
        {
            super.startElement(uri, loc, raw, a);
            _startedElements.add(loc);
        }

        @Override
        public void endElement(String uri, String loc, String raw) throws SAXException
        {
            super.endElement(uri, loc, raw);
            
            if (!StringUtils.equals(_startedElements.get(_startedElements.size() - 1), loc))
            {
                throw new SAXException("Sax events are not consistents. Cannot close <" + loc + "> while it should be <" + _startedElements.get(_startedElements.size() - 1) + ">");
            }
            
            _startedElements.remove(_startedElements.size() - 1);
        }
        
        @Override
        public void endDocument() throws SAXException
        {
            XMLUtils.endElement(_handler, "response");
            
            if (_startedElements.size() != 1)
            {
                throw new SAXException("Sax events are not consistents. Remaining " + _startedElements.size() + " events (should be one).");
            }
            _startedElements.remove(_startedElements.size() - 1);
            super.endDocument();
        }
    }
}
