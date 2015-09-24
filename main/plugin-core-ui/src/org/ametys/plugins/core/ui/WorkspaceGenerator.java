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
package org.ametys.plugins.core.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

import org.ametys.core.ui.ClientSideElement;
import org.ametys.core.ui.MessageTargetFactoriesManager;
import org.ametys.core.ui.RelationsManager;
import org.ametys.core.ui.RibbonAppMenuControlsManager;
import org.ametys.core.ui.RibbonConfigurationManager;
import org.ametys.core.ui.RibbonControlsManager;
import org.ametys.core.ui.RibbonTabsManager;
import org.ametys.core.ui.SAXClientSideElementHelper;
import org.ametys.core.ui.StaticFileImportsManager;
import org.ametys.core.ui.UIToolsConfigurationManager;
import org.ametys.core.ui.UIToolsFactoriesManager;
import org.ametys.core.ui.widgets.ClientSideWidget;
import org.ametys.core.ui.widgets.WidgetsManager;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UsersManager;
import org.ametys.core.util.JSONUtils;

/**
 * Generates the uitools factories definition using the component associated 
 */
public class WorkspaceGenerator extends ServiceableGenerator implements Contextualizable
{
    /** The ribbon control manager */
    protected RibbonControlsManager _ribbonControlManager;
    /** The ribbon tab manager */
    protected RibbonTabsManager _ribbonTabManager;
    /** The Ametys menu manager */
    protected RibbonAppMenuControlsManager _appControlsManager;
    /** The list of existing message target factories */
    protected MessageTargetFactoriesManager _messageTargetFactoriesManager;
    /** The ui tools factories manager */
    protected UIToolsFactoriesManager _uitoolsFactoriesManager;
    /** The relations manager */
    protected RelationsManager _relationsManager;
    /** The widgets manager */
    protected WidgetsManager _widgetsManager;
    /** The sax clientside element helper */
    protected SAXClientSideElementHelper _saxClientSideElementHelper;
    /** The static files import manager */
    protected StaticFileImportsManager _fileImportsManager;
    /** The Excalibur source resolver */
    protected SourceResolver _resolver;
    /** Cocoon context */
    protected org.apache.cocoon.environment.Context _cocoonContext;
    /** The current user provider component */
    protected CurrentUserProvider _currentUserProvider;
    /** The users manager instance */
    protected UsersManager _usersManager;
    /** The json utils component */
    protected JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _messageTargetFactoriesManager = (MessageTargetFactoriesManager) smanager.lookup(MessageTargetFactoriesManager.ROLE);
        _uitoolsFactoriesManager = (UIToolsFactoriesManager) smanager.lookup(UIToolsFactoriesManager.ROLE);
        _ribbonTabManager = (RibbonTabsManager) smanager.lookup(RibbonTabsManager.ROLE);
        _ribbonControlManager = (RibbonControlsManager) smanager.lookup(RibbonControlsManager.ROLE);
        _appControlsManager = (RibbonAppMenuControlsManager) smanager.lookup(RibbonAppMenuControlsManager.ROLE);
        _relationsManager = (RelationsManager) smanager.lookup(RelationsManager.ROLE);
        _widgetsManager = (WidgetsManager) smanager.lookup(WidgetsManager.ROLE);
        _saxClientSideElementHelper = (SAXClientSideElementHelper) smanager.lookup(SAXClientSideElementHelper.ROLE);
        _fileImportsManager = (StaticFileImportsManager) smanager.lookup(StaticFileImportsManager.ROLE);
        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
        _currentUserProvider = (CurrentUserProvider) smanager.lookup(CurrentUserProvider.ROLE);
        _usersManager = (UsersManager) smanager.lookup(UsersManager.ROLE);
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        doGenerate(new HashMap<String, Object>());
    }
    
    /**
     * Generates the UI factories definitions, with parameters
     * @param contextParameters context parameters.
     * @throws IOException if an error occurred
     * @throws SAXException if an error occurred
     * @throws ProcessingException if an error occurred
     */
    protected void doGenerate(Map<String, Object> contextParameters) throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "workspace");
        
        if (!_currentUserProvider.isSuperUser())
        {
            String login = _currentUserProvider.getUser();
            if (StringUtils.isNotBlank(login))
            {
                _usersManager.saxUser(login, contentHandler);
            }
        }
        
        try (InputStream config = getRibbonConfiguration())
        {
            RibbonConfigurationManager ribbonManager = new RibbonConfigurationManager (_ribbonControlManager, _ribbonTabManager, _saxClientSideElementHelper, _resolver, config);
            ribbonManager.saxRibbonDefinition(contentHandler, contextParameters);
        }
        
        try (InputStream config = getUIToolsConfiguration())
        {
            UIToolsConfigurationManager uitoolsManager = new UIToolsConfigurationManager(_uitoolsFactoriesManager, _saxClientSideElementHelper, getUIToolsConfiguration(), ObjectModelHelper.getRequest(objectModel));
            uitoolsManager.saxDefaultState(contentHandler, contextParameters);
        }
        
        saxAppItems ();
        saxMessageTargetFactories(contextParameters);
        saxRelationsHandlers(contextParameters);
        saxWidgets(contextParameters);
        saxStaticFileImports (contextParameters);
        saxAdditionnalInfo(contextParameters);
        
        XMLUtils.endElement(contentHandler, "workspace");
        contentHandler.endDocument();
    }
    
    /**
     * Get the ribbon configuration
     * @return the ribbon configuration
     * @throws IOException if an errors occurs getting the ribbon configuration
     */
    protected InputStream getRibbonConfiguration() throws IOException
    {
        String ribbonFileName = parameters.getParameter("ribbonFileName", "ribbon");
        String mode = parameters.getParameter("mode", null);
        File configFile = new File (_cocoonContext.getRealPath("/WEB-INF/param/" + ribbonFileName + (mode != null ? "-" + mode : "") + ".xml"));
        return new FileInputStream(configFile);
    }
    
    /**
     * Get the UI tools configuration
     * @return the UI tools configuration
     * @throws IOException if an errors occurs getting the UI tools configuration
     */
    protected InputStream getUIToolsConfiguration() throws IOException
    {
        String toolsFileName = parameters.getParameter("toolsFileName", "uitools");
        String mode = parameters.getParameter("mode", null);
        File configFile = new File (_cocoonContext.getRealPath("/WEB-INF/param/" + toolsFileName + (mode != null ? "-" + mode : "") + ".xml"));
        return new FileInputStream(configFile);
    }
    
    /** 
     * SAX the application items
     * @throws SAXException if an error occurred
     */
    protected void saxAppItems () throws SAXException
    {
        String mode = parameters.getParameter("mode", null);
        if (mode == null)
        {
            // SAX application item only in "normal" mode
            XMLUtils.startElement(contentHandler, "app-menu");
            Set<String> itemIds = _appControlsManager.getExtensionsIds();
            for (String itemId : itemIds)
            {
                ClientSideElement control = _appControlsManager.getExtension(itemId);
                _saxClientSideElementHelper.saxDefinition(itemId, "item", control, contentHandler, new HashMap<String, Object>());
            }
            XMLUtils.endElement(contentHandler, "app-menu");
        }
    }
    
    /**
     * SAX the message target factories
     * @param contextParameters the context parameters
     * @throws SAXException if an error occurred
     */
    protected void saxMessageTargetFactories(Map<String, Object> contextParameters) throws SAXException
    {
        contentHandler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        XMLUtils.startElement(contentHandler, "messagetarget-factories");

        for (String extensionId : _messageTargetFactoriesManager.getExtensionsIds())
        {
            ClientSideElement element = _messageTargetFactoriesManager.getExtension(extensionId);
            _saxClientSideElementHelper.saxDefinition(extensionId, "messagetarget-factory", element, contentHandler, contextParameters);
        }
        
        XMLUtils.endElement(contentHandler, "messagetarget-factories");
        contentHandler.endPrefixMapping("i18n");
    }
    
    /**
     * SAX the relations handlers
     * @param contextParameters the context parameters
     * @throws SAXException if an error occurred
     */
    protected void saxRelationsHandlers(Map<String, Object> contextParameters) throws SAXException
    {
        contentHandler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        XMLUtils.startElement(contentHandler, "relations-handlers");

        for (String extensionId: _relationsManager.getExtensionsIds())
        {
            ClientSideElement element = _relationsManager.getExtension(extensionId);
            _saxClientSideElementHelper.saxDefinition(extensionId, "relation-handler", element, contentHandler, contextParameters);
        }
        
        XMLUtils.endElement(contentHandler, "relations-handlers");
        contentHandler.endPrefixMapping("i18n");
    }
    
    /**
     * SAX the widgets
     * @param contextParameters the context parameters
     * @throws SAXException if an error occurred
     */
    protected void saxWidgets(Map<String, Object> contextParameters) throws SAXException
    {
        contentHandler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        
        Map<String, Map<String, Map<String, String>>> defaultWidgets = _widgetsManager.getDefaultWidgets();
        AttributesImpl wattrs = new AttributesImpl();
        wattrs.addCDATAAttribute("default-widgets", _jsonUtils.convertObjectToJson(defaultWidgets.get(WidgetsManager.MODE_CONFIG_NORMAL)));
        wattrs.addCDATAAttribute("default-widgets-enumerated", _jsonUtils.convertObjectToJson(defaultWidgets.get(WidgetsManager.MODE_CONFIG_ENUMERATED)));
        
        XMLUtils.startElement(contentHandler, "widgets", wattrs);

        for (String extensionId: _widgetsManager.getExtensionsIds())
        {
            ClientSideWidget element = _widgetsManager.getExtension(extensionId);
            
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("ftypes", StringUtils.join(element.getFormTypes(contextParameters), ","));
            attrs.addCDATAAttribute("supports-enumerated", Boolean.toString(element.supportsEnumerated(contextParameters)));
            attrs.addCDATAAttribute("supports-non-enumerated", Boolean.toString(element.supportsNonEnumerated(contextParameters)));
            attrs.addCDATAAttribute("supports-multiple", Boolean.toString(element.supportsMultiple(contextParameters)));
            attrs.addCDATAAttribute("supports-non-multiple", Boolean.toString(element.supportsNonMultiple(contextParameters)));
            
            XMLUtils.startElement(contentHandler, "widget-wrapper", attrs);
            _saxClientSideElementHelper.saxDefinition(extensionId, "widget", element, contentHandler, contextParameters);
            XMLUtils.endElement(contentHandler, "widget-wrapper"); 
        }
        
        XMLUtils.endElement(contentHandler, "widgets");
        contentHandler.endPrefixMapping("i18n");
    }
    
    /**
     * SAX the static file imports
     * @param contextParameters the context parameters
     * @throws SAXException if an error occurred
     */
    protected void saxStaticFileImports (Map<String, Object> contextParameters) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "static-imports");
        for (String extensionId : _fileImportsManager.getExtensionsIds())
        {
            ClientSideElement element = _fileImportsManager.getExtension(extensionId);
            _saxClientSideElementHelper.saxDefinition(extensionId, "import", element, contentHandler, contextParameters);
        }
        XMLUtils.endElement(contentHandler, "static-imports");
    }

    /**
     * Use this method when inheritng the WorkspaceGenerator to sax additionnal data 
     * @param contextParameters the context parameters
     * @throws SAXException if an error occurred
     */
    protected void saxAdditionnalInfo(Map<String, Object> contextParameters) throws SAXException
    {
        // Nothing
    }
}
