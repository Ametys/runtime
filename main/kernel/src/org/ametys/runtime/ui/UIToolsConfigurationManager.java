/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.LoggerFactory;

/**
 * Handles the configuration of uitools factories 
 */
public class UIToolsConfigurationManager
{
    private static Logger _logger = LoggerFactory.getLoggerFor(UIToolsConfigurationManager.class);
    
    /** The default opened tools */
    protected Map<String, String> _defaultUITools = new LinkedHashMap<String, String>();
    /** The automatically refreshed tools */
    protected List<String> _refreshUITools = new ArrayList<String>();
    
    /** The ui tools factories manager */
    protected UIToolsFactoriesManager _uitoolsFactoriesManager;

    /** The sax clientside element helper */
    protected SAXClientSideElementHelper _saxClientSideElementHelper;
    
    /** Additional default tools to open */
    protected String[] _additionalDefaultTools;

    /**
     * Constructor
     * @param uitoolsFactoriesManager
     * @param saxClientSideElementHelper
     * @param configFile
     * @param request The request to open by default additionally to those configured
     */
    public UIToolsConfigurationManager (UIToolsFactoriesManager uitoolsFactoriesManager, SAXClientSideElementHelper saxClientSideElementHelper, File configFile, Request request)
    {
        _uitoolsFactoriesManager = uitoolsFactoriesManager;
        _saxClientSideElementHelper = saxClientSideElementHelper;
        
        InputStream is = null;
        try
        {
            is = new FileInputStream(configFile);
            Configuration configuration = new DefaultConfigurationBuilder().build(is);
            
            _configure(configuration);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to read the configuration file", e);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
        
        _additionalDefaultTools = request.getParameterValues("uitool");
    }
    
    
    
    private void _configure(Configuration configuration) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Starting reading uitools configuration");
        }
        
        Configuration[] defaultUIToolFactoryConfigurations = configuration.getChild("default").getChildren("uitool-factory");
        for (Configuration uitoolFactoryConfiguration : defaultUIToolFactoryConfigurations)
        {
            String id = uitoolFactoryConfiguration.getAttribute("id");
            String parameters = uitoolFactoryConfiguration.getValue("");
            
            _defaultUITools.put(id, parameters);
        }

        Configuration[] refreshUIToolFactoryConfigurations = configuration.getChild("refresh").getChildren("uitool-factory");
        for (Configuration uitoolFactoryConfiguration : refreshUIToolFactoryConfigurations)
        {
            String id = uitoolFactoryConfiguration.getAttribute("id");
            _refreshUITools.add(id);
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Ending reading uitools configuration");
        }
    }
    
    /**
     * SAX the default state of uitools to know which ones are opened
     * @param handler Where to SAX
     * @param contextualParameters Contextuals parameters transmitted by the environment.
     * @throws SAXException if an error occurs
     */
    public void saxDefaultState(ContentHandler handler, Map<String, Object> contextualParameters) throws SAXException
    {
        handler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        XMLUtils.startElement(handler, "uitools-factories");

        XMLUtils.startElement(handler, "default");
        for (String id : _defaultUITools.keySet())
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", id);
            XMLUtils.createElement(handler, "uitool-factory", attrs, _defaultUITools.get(id));
        }
        XMLUtils.endElement(handler, "default");

        XMLUtils.startElement(handler, "additionnal");
        if (_additionalDefaultTools != null)
        {
            for (String additionnalTool : _additionalDefaultTools)
            {
                if (!StringUtils.isEmpty(additionnalTool))
                {
                    int i = additionnalTool.indexOf(",");
                    String id = additionnalTool.trim();
                    String params = "";
                    if (i != -1)
                    {
                        id = additionnalTool.substring(0, i).trim();
                        params = additionnalTool.substring(i + 1).trim();
                    }
                    
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addCDATAAttribute("id", id);
                    XMLUtils.createElement(handler, "uitool-factory", attrs, params);
                }
            }
        }
        XMLUtils.endElement(handler, "additionnal");

        XMLUtils.startElement(handler, "refresh");
        for (String id : _refreshUITools)
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", id);
            XMLUtils.createElement(handler, "uitool-factory", attrs);
        }
        XMLUtils.endElement(handler, "refresh");

        for (String factoryId : _uitoolsFactoriesManager.getExtensionsIds())
        {
            ClientSideElement factory = _uitoolsFactoriesManager.getExtension(factoryId);
            _saxClientSideElementHelper.saxDefinition(factoryId, "uitool-factory", factory, handler, contextualParameters);
        }
        
        XMLUtils.endElement(handler, "uitools-factories");
        handler.endPrefixMapping("i18n");
    }
}
