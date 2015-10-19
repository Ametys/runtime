/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.plugins.core.ui.help;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.core.ui.Callable;

/**
 * Provides the url associated to an help identifier
 */
public class HelpUrlProvider implements Contextualizable, Configurable, Component
{
    /** Key of the default url */
    protected static final String __DEFAULT_HELP_ID = "default";
    
    /** The map that links help identifiers and urls */
    protected static Map<String, String> _helpMapping;
    
    /** Cocoon context */
    protected Context _context;
    
    private long _lastUpdate;
    
    @Override
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        _context = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _updateHelpUrls();
    }
    
    /**
     * Get the default help url
     * @return The result (map with an url key, or possible error)
     */
    @Callable
    public Map<String, Object> getDefaultHelpUrl()
    {
        return getHelpUrl(__DEFAULT_HELP_ID);
    }
    
    /**
     * Get the default help url
     * @param helpId The help identifer
     * @return The result (map with an url key, or possible error)
     */
    @Callable
    public Map<String, Object> getHelpUrl (String helpId)
    {
        Map<String, Object> resultMap = new HashMap<>();
        
        resultMap.put("helpId", helpId);
        
        String url = _getHelpUrl(helpId);
        resultMap.put("url", url);
        
        if (StringUtils.isEmpty(url))
        {
            resultMap.put("error", "true");
            resultMap.put("error-cause", "not-found");
        }
        
        return resultMap;
    }
    
    private String _getHelpUrl (String helpId)
    {
        try
        {
            _updateHelpUrls();
            return _helpMapping.get(helpId);
        }
        catch (ConfigurationException e)
        {
            return null;
        }
    }
    
    private void _updateHelpUrls () throws ConfigurationException
    {
        try
        {
            File helpMappingFile = new File(_context.getRealPath("/WEB-INF/param/help.xml"));
            
            if (helpMappingFile.exists() && helpMappingFile.lastModified() > _lastUpdate)
            {
                _lastUpdate = new Date().getTime();
                _helpMapping = new HashMap<>(); 
                
                Configuration helpMappingCfg = new DefaultConfigurationBuilder().buildFromFile(helpMappingFile);
                
                _helpMapping.put(__DEFAULT_HELP_ID, helpMappingCfg.getAttribute("defaultUrl"));
                
                for (Configuration linkCfg : helpMappingCfg.getChildren("link"))
                {
                    _helpMapping.put(linkCfg.getAttribute("id"), linkCfg.getValue(""));
                }
            }
            else if (!helpMappingFile.exists())
            {
                _helpMapping = new HashMap<>(); 
            }
        }
        catch (SAXException e)
        {
            throw new ConfigurationException("SAXException during configuration.", e);
        }
        catch (IOException e)
        {
            throw new ConfigurationException("IOException during configuration.", e);
        }
    }
}

