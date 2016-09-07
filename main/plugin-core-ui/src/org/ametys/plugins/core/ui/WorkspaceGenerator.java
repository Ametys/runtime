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
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

import org.ametys.core.ui.ClientSideElement;
import org.ametys.core.ui.ClientSideElementDependenciesManager;
import org.ametys.core.ui.MessageTargetFactoriesManager;
import org.ametys.core.ui.RelationsManager;
import org.ametys.core.ui.RibbonConfigurationManager;
import org.ametys.core.ui.RibbonControlsManager;
import org.ametys.core.ui.RibbonImportManager;
import org.ametys.core.ui.RibbonManager;
import org.ametys.core.ui.RibbonManagerCache;
import org.ametys.core.ui.RibbonTabsManager;
import org.ametys.core.ui.SAXClientSideElementHelper;
import org.ametys.core.ui.StaticFileImportsManager;
import org.ametys.core.ui.UIToolsConfigurationManager;
import org.ametys.core.ui.UIToolsFactoriesManager;
import org.ametys.core.ui.widgets.ClientSideWidget;
import org.ametys.core.ui.widgets.WidgetsManager;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.util.JSONUtils;
import org.ametys.plugins.core.user.UserHelper;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * Generates the uitools factories definition using the component associated 
 */
public class WorkspaceGenerator extends ServiceableGenerator implements Contextualizable
{
    /** The ribbon control manager */
    protected RibbonControlsManager _ribbonControlManager;
    /** The ribbon tab manager */
    protected RibbonTabsManager _ribbonTabManager;
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
    protected UserManager _userManager;
    /** The json utils component */
    protected JSONUtils _jsonUtils;
    /** The User Helper */
    protected UserHelper _userHelper;
    /** The ribbon manager cache helper */
    protected RibbonManagerCache _ribbonManagerCache;
    /** The ribbon import manager */
    protected RibbonImportManager _ribbonImportManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _messageTargetFactoriesManager = (MessageTargetFactoriesManager) smanager.lookup(MessageTargetFactoriesManager.ROLE);
        _uitoolsFactoriesManager = (UIToolsFactoriesManager) smanager.lookup(UIToolsFactoriesManager.ROLE);
        _ribbonTabManager = (RibbonTabsManager) smanager.lookup(RibbonTabsManager.ROLE);
        _ribbonControlManager = (RibbonControlsManager) smanager.lookup(RibbonControlsManager.ROLE);
        _relationsManager = (RelationsManager) smanager.lookup(RelationsManager.ROLE);
        _widgetsManager = (WidgetsManager) smanager.lookup(WidgetsManager.ROLE);
        _saxClientSideElementHelper = (SAXClientSideElementHelper) smanager.lookup(SAXClientSideElementHelper.ROLE);
        _fileImportsManager = (StaticFileImportsManager) smanager.lookup(StaticFileImportsManager.ROLE);
        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
        _currentUserProvider = (CurrentUserProvider) smanager.lookup(CurrentUserProvider.ROLE);
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
        _userHelper = (UserHelper) smanager.lookup(UserHelper.ROLE);
        _ribbonManagerCache = (RibbonManagerCache) smanager.lookup(RibbonManagerCache.ROLE);
        _ribbonImportManager = (RibbonImportManager) smanager.lookup(RibbonImportManager.ROLE);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        doGenerate(getContextualParameters());
    }
    
    /**
     * Get the contextual parameters
     * @return The contextual parameters
     */
    protected Map<String, Object> getContextualParameters()
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String workspaceName = (String) request.getAttribute(WorkspaceMatcher.WORKSPACE_NAME);
        
        Map<String, Object> contextParameters = new HashMap<>();
        contextParameters.put(WorkspaceMatcher.WORKSPACE_NAME, workspaceName);
        
        return contextParameters;
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
        
        UserIdentity currentUser = _currentUserProvider.getUser();
        if (currentUser != null)
        {
            String login = currentUser.getLogin();
            String userPopulationId = currentUser.getPopulationId();
            if (StringUtils.isNotBlank(login))
            {
                User user = _userManager.getUser(userPopulationId, login);
                _userHelper.saxUser(user, contentHandler);
            }
        }
        
        ClientSideElementDependenciesManager dependenciesManager = new ClientSideElementDependenciesManager(this.manager);
        
        Source ribbonConfig = getRibbonConfiguration();
        Map<String, List<ClientSideElement>> elementsToSax;
        RibbonManager ribbonManager = null;
        try
        {
            ribbonManager = _ribbonManagerCache.getManager(ribbonConfig.getURI());
            String workspaceName = (String) ObjectModelHelper.getRequest(objectModel).getAttribute("workspaceName");
            RibbonConfigurationManager ribbonConfigurationManager = new RibbonConfigurationManager(_ribbonControlManager, ribbonManager, _ribbonTabManager, _ribbonImportManager, _saxClientSideElementHelper, _resolver, dependenciesManager, _ribbonManagerCache, ribbonConfig, workspaceName);
            ribbonConfigurationManager.saxRibbonDefinition(contentHandler, contextParameters);
            elementsToSax = getElementsToSax(dependenciesManager, ribbonConfigurationManager);
        }
        catch (Exception e)
        {
            throw new ProcessingException("Unable to get or create a ribbon manager for ribbon specific components", e);
        }
        finally
        {
            if (ribbonManager != null)
            {
                _ribbonManagerCache.dispose(ribbonManager);
            }
        }
        
        saxUITools(contextParameters, elementsToSax.get(UIToolsFactoriesManager.ROLE));
        saxMessageTargetFactories(contextParameters, elementsToSax.get(MessageTargetFactoriesManager.ROLE));
        saxRelationsHandlers(contextParameters, elementsToSax.get(RelationsManager.ROLE));
        saxWidgets(contextParameters);
        saxStaticFileImports (contextParameters, elementsToSax.get(StaticFileImportsManager.ROLE));
        saxAdditionnalInfo(contextParameters);
        
        XMLUtils.endElement(contentHandler, "workspace");
        contentHandler.endDocument();
    }

    /**
     * Retrieve the list of elements to generate the Workspace
     * @param dependenciesManager The dependencies manager
     * @param ribbonManager The ribbon manager for this workspace
     * @return The list of elements, mapped by extension points.
     * @throws SAXException If an error occurs
     */
    protected Map<String, List<ClientSideElement>> getElementsToSax(ClientSideElementDependenciesManager dependenciesManager, RibbonConfigurationManager ribbonManager) throws SAXException
    {
        List<ClientSideElement> ribbonControls = ribbonManager.getControls();
        for (ClientSideElement control : ribbonControls)
        {
            dependenciesManager.register(control);
        }
        List<ClientSideElement> ribbonTabs = ribbonManager.getTabs();
        for (ClientSideElement control : ribbonTabs)
        {
            dependenciesManager.register(control);
        }
        
        for (String extensionId: _widgetsManager.getExtensionsIds())
        {
            ClientSideWidget element = _widgetsManager.getExtension(extensionId);
            dependenciesManager.register(element);
        }
        
        try
        {
            return dependenciesManager.computeDependencies();
        }
        catch (ServiceException e)
        {
            throw new SAXException("Unable to compute dependencies", e);
        }
    }

    /**
     * SAX the UI Tools
     * @param contextParameters the context parameters
     * @param elements The list of elements to sax
     * @throws IOException if an error occurred
     * @throws SAXException if an error occurred
     */
    protected void saxUITools(Map<String, Object> contextParameters, List<ClientSideElement> elements) throws IOException, SAXException
    {
        try (InputStream config = getUIToolsConfiguration())
        {
            UIToolsConfigurationManager uitoolsManager = new UIToolsConfigurationManager(_uitoolsFactoriesManager, _saxClientSideElementHelper, getUIToolsConfiguration(), ObjectModelHelper.getRequest(objectModel));
            uitoolsManager.saxDefaultState(contentHandler, contextParameters, elements);
        }
    }

    /**
     * Get the ribbon configuration
     * @return the ribbon configuration
     * @throws IOException if an errors occurs getting the ribbon configuration
     */
    protected Source getRibbonConfiguration() throws IOException
    {
        String ribbonFileName = parameters.getParameter("ribbonFileName", "ribbon");
        String mode = parameters.getParameter("mode", null);
        return _resolver.resolveURI("context://WEB-INF/param/" + ribbonFileName + (mode != null ? "-" + mode : "") + ".xml");
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
     * SAX the message target factories
     * @param contextParameters the context parameters
     * @param elements The list of elements for the message target factories 
     * @throws SAXException if an error occurred
     */
    protected void saxMessageTargetFactories(Map<String, Object> contextParameters, List<ClientSideElement> elements) throws SAXException
    {
        contentHandler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        XMLUtils.startElement(contentHandler, "messagetarget-factories");

        if (elements != null)
        {
            for (ClientSideElement element : elements)
            {
                _saxClientSideElementHelper.saxDefinition("messagetarget-factory", element, contentHandler, contextParameters);
            }
        }
        
        XMLUtils.endElement(contentHandler, "messagetarget-factories");
        contentHandler.endPrefixMapping("i18n");
    }
    
    /**
     * SAX the relations handlers
     * @param contextParameters the context parameters
     * @param elements The list of relation handlers
     * @throws SAXException if an error occurred
     */
    protected void saxRelationsHandlers(Map<String, Object> contextParameters, List<ClientSideElement> elements) throws SAXException
    {
        contentHandler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        XMLUtils.startElement(contentHandler, "relations-handlers");

        if (elements != null)
        {
            for (ClientSideElement element: elements)
            {
                _saxClientSideElementHelper.saxDefinition("relation-handler", element, contentHandler, contextParameters);
            }
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
            _saxClientSideElementHelper.saxDefinition("widget", element, contentHandler, contextParameters);
            XMLUtils.endElement(contentHandler, "widget-wrapper"); 
        }
        
        XMLUtils.endElement(contentHandler, "widgets");
        contentHandler.endPrefixMapping("i18n");
    }
    
    /**
     * SAX the static file imports
     * @param contextParameters the context parameters
     * @param elements The list of static file imports elements
     * @throws SAXException if an error occurred
     */
    protected void saxStaticFileImports (Map<String, Object> contextParameters, List<ClientSideElement> elements) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "static-imports");
        if (elements != null)
        {
            for (ClientSideElement element : elements)
            {
                _saxClientSideElementHelper.saxDefinition("import", element, contentHandler, contextParameters);
            }
        }
        XMLUtils.endElement(contentHandler, "static-imports");
    }

    /**
     * Use this method when inheriting the WorkspaceGenerator to sax additional data 
     * @param contextParameters the context parameters
     * @throws SAXException if an error occurred
     */
    protected void saxAdditionnalInfo(Map<String, Object> contextParameters) throws SAXException
    {
        if (PluginsManager.getInstance().isSafeMode())
        {
            XMLUtils.createElement(contentHandler, "safe-mode", PluginsManager.getInstance().getStatus().toString());
        }
    }
}
