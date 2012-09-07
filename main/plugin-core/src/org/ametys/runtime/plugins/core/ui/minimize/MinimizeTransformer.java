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

package org.ametys.runtime.plugins.core.ui.minimize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.transformation.ServiceableTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.ametys.runtime.config.Config;

/**
 * This transformer will minimize every scripts together
 */
public class MinimizeTransformer extends ServiceableTransformer implements Contextualizable, Configurable
{
    private String _path;
    private String _defaultPluginCoreUrl;
    private boolean _debugMode;
    private int _randomCode;
    private Context _context;
    private String _removedPath;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _debugMode = (Config.getInstance() != null && Config.getInstance().getValueAsBoolean("runtime.debug.ui") == true) ? true : false;
        _defaultPluginCoreUrl = configuration.getChild("plugin-core-url").getValue(null);
    }
    

    
    
    
    
    
    /**
     * Get the list of files
     * @return Return a modifiable file list registrer in session for the curren _randomCode. Will never be null.
     */
    private List<String> _getJSFileList()
    {
        Request request = ContextHelper.getRequest(_context);
        Session session = request.getSession(true);
        
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js-tmp");
        if (codesAndFiles == null)
        {
            session.setAttribute(MinimizeTransformer.class.getName() + "$js-tmp", new HashMap<Integer, List<String>>());
            codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js-tmp");
        }
        
        if (!codesAndFiles.containsKey(_randomCode))
        {            
            codesAndFiles.put(_randomCode, new ArrayList<String>());
        }
        return codesAndFiles.get(_randomCode);
    }
    
    /**
     * At this point, if a minimizable list of files is know, generates a script tag here.
     * Starts a new list of files.
     */
    private void _jsCheckPoint() throws SAXException
    {
        Request request = ContextHelper.getRequest(_context);
        Session session = request.getSession(true);
        
        List<String> jsFileList = _getJSFileList(); 
        if (jsFileList.size() > 0)
        {
            // Store the file list for future transformer
            Map<Integer, List<String>> finalCodesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js");
            if (finalCodesAndFiles == null)
            {
                session.setAttribute(MinimizeTransformer.class.getName() + "$js", new HashMap<Integer, List<String>>());
                finalCodesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js");
            }
            finalCodesAndFiles.put(jsFileList.hashCode(), jsFileList);
            
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("type", "text/javascript");
            attrs.addCDATAAttribute("src", StringUtils.defaultIfEmpty(source, _defaultPluginCoreUrl) + "/jsfilelist/" + jsFileList.hashCode() + "-" + (_debugMode ? "true" : "false") + ".js");
            super.startElement("", "script", "script", attrs);
            super.endElement("", "script", "script");
        }
        
        // Remove the file list for temporary manipulations
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js-tmp");
        codesAndFiles.remove(_randomCode);

        // Change random code to avoid conflict between threads (of a same session)
        _randomCode = (int) (Math.random() * Integer.MAX_VALUE);
    }
    
    /**
     * At this point, if a minimizable list of files is know, generates a css tag here.
     * Starts a new list of files.
     */
    /*private void _cssCheckPoint() throws SAXException
    {
        Request request = ContextHelper.getRequest(_context);
        Session session = request.getSession(true);
        
        List<String> cssFileList = _getCSSFileList(); 
        if (cssFileList.size() > 0)
        {
            // Store the file list for future transformer
            Map<Integer, List<String>> finalCodesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$css");
            if (finalCodesAndFiles == null)
            {
                session.setAttribute(MinimizeTransformer.class.getName() + "$css", new HashMap<Integer, List<String>>());
                finalCodesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$css");
            }
            finalCodesAndFiles.put(cssFileList.hashCode(), cssFileList);
            
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("type", "text/javascript");
            attrs.addCDATAAttribute("src", StringUtils.defaultIfEmpty(source, _defaultPluginCoreUrl) + "/jsfilelist/" + cssFileList.hashCode() + "-" + (_debugMode ? "true" : "false") + ".js");
            super.startElement("", "script", "script", attrs);
            super.endElement("", "script", "script");
        }
        
        // Remove the file list for temporary manipulations
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js-tmp");
        codesAndFiles.remove(_randomCode);

        // Change random code to avoid conflict between threads (of a same session)
        _randomCode = (int) (Math.random() * Integer.MAX_VALUE);
    }*/

    
    
    
    

    
    
    @Override
    public void startDocument() throws SAXException
    {
        _path = "";
        super.startDocument();
    }
    
    @Override
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException
    {
        if (!_debugMode)
        {
            _path += "/" + loc;
            
            if (StringUtils.countMatches(_path, "/") == 2)
            {
                // we are googin in third level (/html/head/* or /html/body/* 
                super.startElement(uri, loc, raw, a);
                _jsCheckPoint();
            }
            else if (StringUtils.countMatches(_path, "/") > 2 && _isAScriptTag(loc, a))
            {
                int index = _getAttributeIndex(a, "src");
                if (index == -1)
                {
                    // A local script in between... stop 
                    _jsCheckPoint();
                    super.startElement(uri, loc, raw, a);
                }
                else
                {
                    // A distant script
                    String fileName = _relativize(a.getValue(index));
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("For random code '" + _randomCode + "', adding js file '" + fileName + "'");
                    }
                    _getJSFileList().add(fileName);
                    _removedPath = _path;
                }
            }
            else
            {
                super.startElement(uri, loc, raw, a);
            }
        }
        else
        {
            super.startElement(uri, loc, raw, a);
        }
    }
    
    @Override
    public void endElement(String uri, String loc, String raw) throws SAXException
    {
        if (!_debugMode)
        { 
            if (StringUtils.countMatches(_path, "/") == 2)
            {
                _jsCheckPoint();
            }

            if (!StringUtils.startsWith(_path, _removedPath))
            {
                super.endElement(uri, loc, raw);
            }
            else
            {
                if (StringUtils.equals(_path, _removedPath))
                {
                    _removedPath = null;
                }
            }

            _path = StringUtils.removeEnd(_path, "/" + loc);
        }
        else
        {
            super.endElement(uri, loc, raw);
        }
    }
    
    
    
    
    
    /**
     * Determine if the element is a script
     * @param loc The tag name
     * @param a The attributes
     * @return true if it is a script
     */
    private boolean _isAScriptTag(String loc, Attributes a)
    {
        if (StringUtils.equalsIgnoreCase(loc, "script"))
        {
            for (int i = 0; i < a.getLength(); i++)
            {
                if (StringUtils.equalsIgnoreCase(a.getLocalName(i), "type"))
                {
                    return StringUtils.equals("text/javascript", a.getValue(i));
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Script or link tags are using external url, we have to convert it in internal url.
     * External can be: http://thisserver.com/context/path/to/file.js or /context/path/to/file.js or path/to/file.js
     * @param url The external url
     * @return The internalized url
     */
    private String _relativize(String url)
    {
        Request request = ContextHelper.getRequest(_context);
        
        // full absolute url
        if (StringUtils.startsWith(url, "http://") || StringUtils.startsWith(url, "https://"))
        {
            // An issue can happend, if the context path is blank but the wanted url is for another context on our server... we are internalizing it
            if (StringUtils.startsWith(url, request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()))
            {
                return "~" + StringUtils.removeStart(url, request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
            }
            else if (StringUtils.startsWith(url, request.getScheme() + "://" + request.getServerName() + request.getContextPath()))
            {
                return "~" + StringUtils.removeStart(url, request.getScheme() + "://" + request.getServerName() + request.getContextPath());
            }
            else
            {
                return url;
            }
        }
        
        // absolute url
        else if (StringUtils.startsWith(url, "/"))
        {
            // An issue can happend, if the context path is blank but the wanted url is for another context on our server... we are internalizing it
            if (StringUtils.startsWith(url, request.getContextPath()))
            {
                return "~" + StringUtils.removeStart(url, request.getContextPath());
            }
            else
            {
                return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + url;
            }
        }
        
        // relative url
        else
        {
            String internalCurrentURL = StringUtils.removeStart(request.getRequestURI(), request.getContextPath());
            internalCurrentURL = StringUtils.substringBeforeLast(internalCurrentURL, "/");
            
            return "~" + org.apache.cocoon.util.NetUtils.normalize(internalCurrentURL + "/" + url);
        }
    }
    
    /**
     * Determine if an attributes with this local name does exist and get its index
     * @param a The attributes
     * @param name The local name of an attribute
     * @return the index if tha attributes exists or -1 otherwise
     */
    private int _getAttributeIndex(Attributes a, String name)
    {
        for (int i = 0; i < a.getLength(); i++)
        {
            if (StringUtils.equalsIgnoreCase(a.getLocalName(i), name))
            {
                return i;
            }
        }
        return -1;
    }
}
