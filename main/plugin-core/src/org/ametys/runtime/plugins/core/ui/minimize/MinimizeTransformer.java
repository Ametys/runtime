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

import java.io.IOException;
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
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
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
    private Long _debugMode;
    private Long _configuredDebugMode;
    private int _randomJSCode;
    private int _randomCSSCode;
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
        _configuredDebugMode = Config.getInstance() != null && Config.getInstance().getValueAsLong("runtime.debug.ui") != null ? Config.getInstance().getValueAsLong("runtime.debug.ui") : 2;
        _defaultPluginCoreUrl = configuration.getChild("plugin-core-url").getValue(null);
    }
    
    @Override
    public void setup(SourceResolver res, Map om, String src, Parameters params) throws ProcessingException, SAXException, IOException
    {
        super.setup(res, om, src, params);
        
        String debugModeStr = params.getParameter("debug-mode-request", null);
        _debugMode = StringUtils.isNotEmpty(debugModeStr) ? Long.parseLong(debugModeStr) : _configuredDebugMode;
    }
    
    
    /**
     * Get the list of files for js
     * @return Return a modifiable file list registrer in session for the current _randomJSCode. Will never be null.
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
        
        if (!codesAndFiles.containsKey(_randomJSCode))
        {            
            codesAndFiles.put(_randomJSCode, new ArrayList<String>());
        }
        return codesAndFiles.get(_randomJSCode);
    }
    
    /**
     * Get the list of files for css
     * @return Return a modifiable file list registrer in session for the curren _randomCSSCode. Will never be null.
     */
    private List<String> _getCSSFileList()
    {
        Request request = ContextHelper.getRequest(_context);
        Session session = request.getSession(true);
        
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$css-tmp");
        if (codesAndFiles == null)
        {
            session.setAttribute(MinimizeTransformer.class.getName() + "$css-tmp", new HashMap<Integer, List<String>>());
            codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$css-tmp");
        }
        
        if (!codesAndFiles.containsKey(_randomCSSCode))
        {            
            codesAndFiles.put(_randomCSSCode, new ArrayList<String>());
        }
        return codesAndFiles.get(_randomCSSCode);
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
            attrs.addCDATAAttribute("src", StringUtils.defaultIfEmpty(source, _defaultPluginCoreUrl) + "/jsfilelist/" + jsFileList.hashCode() + "-" + (_debugMode != 0 ? "true" : "false") + ".js");
            super.startElement("", "script", "script", attrs);
            super.endElement("", "script", "script");
        }
        
        // Remove the file list for temporary manipulations
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$js-tmp");
        codesAndFiles.remove(_randomJSCode);

        // Change random code to avoid conflict between threads (of a same session)
        _randomJSCode = (int) (Math.random() * Integer.MAX_VALUE);
    }
    
    /**
     * At this point, if a minimizable list of files is know, generates a css tag here.
     * Starts a new list of files.
     */
    private void _cssCheckPoint() throws SAXException
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
            attrs.addCDATAAttribute("type", "text/css");
            attrs.addCDATAAttribute("rel", "stylesheet");
            attrs.addCDATAAttribute("href", StringUtils.defaultIfEmpty(source, _defaultPluginCoreUrl) + "/cssfilelist/" + cssFileList.hashCode() + "-" + (_debugMode != 0 ? "true" : "false") + ".css");
            super.startElement("", "link", "link", attrs);
            super.endElement("", "link", "link");
        }
        
        // Remove the file list for temporary manipulations
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + "$css-tmp");
        codesAndFiles.remove(_randomCSSCode);

        // Change random code to avoid conflict between threads (of a same session)
        _randomCSSCode = (int) (Math.random() * Integer.MAX_VALUE);
    }

    
    
    
    

    
    
    @Override
    public void startDocument() throws SAXException
    {
        _path = "";
        super.startDocument();
    }
    
    @Override
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException
    {
        if (_debugMode != 2)
        {
            _path += "/" + loc;
            
            if (StringUtils.countMatches(_path, "/") == 2)
            {
                // we are googin in third level (/html/head/* or /html/body/* 
                super.startElement(uri, loc, raw, a);
                _cssCheckPoint();
                _jsCheckPoint();
            }
            else if (StringUtils.countMatches(_path, "/") > 2 && _isAScriptTag(loc, a))
            {
                int index = _getAttributeIndex(a, "src");
                if (index == -1)
                {
                    // A local script in between... stop 
                    _cssCheckPoint();
                    _jsCheckPoint();
                    super.startElement(uri, loc, raw, a);
                }
                else
                { 
                    // A distant script
                    String fileName = _relativize(a.getValue(index));
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("For random code '" + _randomJSCode + "', adding js file '" + fileName + "'");
                    }
                    _getJSFileList().add(fileName);
                    _removedPath = _path;
                }
            }
            else if (StringUtils.countMatches(_path, "/") > 2 && _isAStyleOrLinkTag(loc, a))
            {
                int index = _getAttributeIndex(a, "href");
                if (index == -1)
                {
                    // A local style in between... stop 
                    _cssCheckPoint();
                    super.startElement(uri, loc, raw, a);
                }
                else
                {
                    // A distant script
                    String fileName = _relativize(a.getValue(index));
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("For random code '" + _randomCSSCode + "', adding css file '" + fileName + "'");
                    }
                    List<String> cssFileList = _getCSSFileList();
                    if (_debugMode == 1 && cssFileList.size() > 31)
                    {
                        // Too many css for IE
                        _cssCheckPoint();
                    }
                    
                    cssFileList.add(fileName);
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
        if (_debugMode != 2)
        { 
            if (StringUtils.countMatches(_path, "/") == 2)
            {
                _cssCheckPoint();
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
     * Determine if the element is a inline css style
     * @param loc The tag name
     * @param a The attributes
     * @return true if it is a link
     */
    private boolean _isAStyleOrLinkTag(String loc, Attributes a)
    {
        if (StringUtils.equalsIgnoreCase(loc, "style"))
        {
            for (int i = 0; i < a.getLength(); i++)
            {
                if (StringUtils.equalsIgnoreCase(a.getLocalName(i), "type"))
                {
                    return StringUtils.equals("text/css", a.getValue(i));
                }
            }
            return true;
        }
        else if (StringUtils.equalsIgnoreCase(loc, "link"))
        {
            boolean isAStylesheet = false;
            for (int i = 0; i < a.getLength(); i++)
            {
                if (StringUtils.equalsIgnoreCase(a.getLocalName(i), "type") && !StringUtils.equals("text/css", a.getValue(i)))
                {
                    return false;
                }
                else if (StringUtils.equalsIgnoreCase(a.getLocalName(i), "rel") && StringUtils.equals("stylesheet", a.getValue(i)))
                {
                    isAStylesheet = true;
                }
            }
            return isAStylesheet;
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
