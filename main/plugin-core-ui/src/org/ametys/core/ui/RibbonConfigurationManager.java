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
package org.ametys.core.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.ClientSideElement.Script;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * This class handles the ribbon configuration needed for client side display.
 */
public class RibbonConfigurationManager
{
    private static Logger _logger = LoggerFactory.getLogger(RibbonConfigurationManager.class);
    
    /** Size constants for controls*/
    public enum CONTROLSIZE 
    {
        /** Control is large size */ 
        LARGE("large"), 
        /** Control is small size */ 
        SMALL("small"),
        /** Control is very small (icon without text) size */ 
        VERYSMALL("very-small");
        
        private String _value;
        
        private CONTROLSIZE(String value)
        {
            this._value = value;   
        }  
           
        @Override
        public String toString() 
        {
            return _value;
        }   

        /**
         * Converts a string to a CONTROLSIZE
         * @param size The size to convert
         * @return The size corresponding to the string or null if unknwon
         */
        public static CONTROLSIZE createsFromString(String size)
        {
            for (CONTROLSIZE v : CONTROLSIZE.values())
            {
                if (v.toString().equals(size))
                {
                    return v;
                }
            }
            return null;
        }
    }

    /** Size constants for controls*/
    public enum LAYOUTALIGN 
    {
        /** The controls are top aligned in the layout. Can be use with 1, 2 or 3 controls */ 
        TOP("top"),
        /** The controls are middly aligned. Can be used with 2 controls only. */ 
        MIDDLE("middle");
        
        private String _value;
        
        private LAYOUTALIGN(String value)
        {
            this._value = value;   
        }   
           
        @Override  
        public String toString() 
        {
            return _value;
        }   
        
        /**
         * Converts a string to a layout align
         * @param align The align to convert
         * @return The align corresponding to the string or null if unknwon
         */
        public static LAYOUTALIGN createsFromString(String align)
        {
            for (LAYOUTALIGN v : LAYOUTALIGN.values())
            {
                if (v.toString().equals(align))
                {
                    return v;
                }
            }
            return null;
        }
    }

    /** The ribbon control manager */
    protected RibbonManager _ribbonManager;
    /** The ribbon tab extension point */
    protected RibbonTabsManager _ribbonTabManager;
    /** The sax clientside element helper */
    protected SAXClientSideElementHelper _saxClientSideElementHelper;
    /** The excalibur source resolver */
    protected SourceResolver _resolver;
    
    /** The ribbon configuration, as read from the configuration source */
    protected RibbonConfiguration _ribbonConfig;

    /** The controls referenced by the ribbon */
    protected Set<String> _controlsReferences = new HashSet<>();
    /** The tabs referenced by the ribbon */
    protected Set<String> _tabsReferences = new HashSet<>();
    
    private boolean _initialized;
    private RibbonControlsManager _ribbonControlManager;
    
    /**
     * Constructor
     * @param ribbonControlManager The ribbon control manager
     * @param ribbonManager The ribbon manager for this config
     * @param ribbonTabManager the ribbon tab manager
     * @param saxClientSideElementHelper the helper to SAX client side element
     * @param resolver the excalibur source resolver
     * @param dependenciesManager The dependencies manager
     * @param ribbonManagerCache The ribbon manager cache helper
     * @param config the ribbon configuration
     * @throws RuntimeException if an error occurred
     */
    public RibbonConfigurationManager (RibbonControlsManager ribbonControlManager, RibbonManager ribbonManager, RibbonTabsManager ribbonTabManager, SAXClientSideElementHelper saxClientSideElementHelper, SourceResolver resolver, ClientSideElementDependenciesManager dependenciesManager, RibbonManagerCache ribbonManagerCache, Source config)
    {
        _ribbonControlManager = ribbonControlManager;
        _ribbonManager = ribbonManager;
        _ribbonTabManager = ribbonTabManager;
        _saxClientSideElementHelper = saxClientSideElementHelper;
        _resolver = resolver;
        
        try
        {
            _configure(config, dependenciesManager, ribbonManagerCache);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to read the configuration file", e);
        }
    }

    private void _configure(Source config, ClientSideElementDependenciesManager dependenciesManager, RibbonManagerCache ribbonManagerCache) throws Exception
    {
        synchronized (_ribbonManager)
        {
            _ribbonConfig = ribbonManagerCache.getCachedConfiguration(_ribbonManager);
            if (_ribbonConfig != null)
            {
                // configuration already cached
                return;
            }
            
            _ribbonConfig = new RibbonConfiguration();
            Configuration configuration = new DefaultConfigurationBuilder().build(config.getInputStream());
            Map<String, Long> importsValidity = new HashMap<>();
            importsValidity.put(config.getURI(), config.getLastModified());
            _configure(configuration, dependenciesManager, importsValidity);
            
            ribbonManagerCache.addCachedConfiguration(_ribbonManager, _ribbonConfig, importsValidity);
            _ribbonManager.initializeExtensions();
        }
    }

    private void _configure(Configuration configuration, ClientSideElementDependenciesManager dependenciesManager, Map<String, Long> importUri) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Starting reading ribbon configuration");
        }
        
        Configuration[] appMenuConfigurations = configuration.getChild("app-menu").getChildren();
        _ribbonConfig.getAppMenu().addAll(_configureElement(appMenuConfigurations, _ribbonManager));

        Configuration[] userMenuConfigurations = configuration.getChild("user-menu").getChildren();
        _ribbonConfig.getUserMenu().addAll(_configureElement(userMenuConfigurations, _ribbonManager));
        
        Configuration[] dependenciesConfigurations = configuration.getChild("depends").getChildren();
        for (Configuration dependencyConfigurations : dependenciesConfigurations)
        {
            String extensionPoint = dependencyConfigurations.getName();
            String extensionId = dependencyConfigurations.getValue();
            
            dependenciesManager.register(extensionPoint, extensionId);
        }
            
        Configuration[] tabsConfigurations = configuration.getChild("tabs").getChildren();
        for (Configuration tabConfiguration : tabsConfigurations)
        {
            if ("tab".equals(tabConfiguration.getName()))
            {
                Tab tab = new Tab(tabConfiguration, _ribbonManager, _logger);
                _ribbonConfig.getTabs().add(tab);
            }
            else if ("import".equals(tabConfiguration.getName()))
            {
                String url = tabConfiguration.getValue();
                Source src = null;
                try
                {
                    src = _resolver.resolveURI(url);
                    if (!src.exists())
                    {
                        _logger.warn("Cannot import unexisting file " + url);
                    }
                    else
                    {
                        importUri.put(url, src.getLastModified());
                        
                        try (InputStream is = src.getInputStream())
                        {
                            Configuration importedConfiguration = new DefaultConfigurationBuilder().build(is);
                            _configure(importedConfiguration, dependenciesManager, importUri);
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Cannot import file " + url, e);
                }
                finally
                {
                    _resolver.release(src);
                }
            }
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug("Ending reading ribbon configuration");
        }
    }
    
    private List<Element> _configureElement(Configuration[] configurations, RibbonManager ribbonManager) throws ConfigurationException
    {
        List<Element> elements = new ArrayList<>();
        for (Configuration configuration : configurations)
        {
            if ("control".equals(configuration.getName()))
            {
                elements.add(new ControlRef(configuration, ribbonManager, _logger));
            }
            else if ("separator".equals(configuration.getName()))
            {
                elements.add(new Separator());
            }
            else
            {
                _logger.warn("During configuration of the ribbon, the app-menu or user-menu use an unknow tag '" + configuration.getName() + "'");
            }
        }
        return elements;
    }
    
    /**
     * Check that the configuration was correct
     * @throws IllegalStateException if an item does not exist
     */
    private synchronized void _lazyInitialize() 
    {
        if (_initialized)
        {
            return;
        }
        
        // check that all refered items does exist
        for (Tab tab : _ribbonConfig.getTabs())
        {
            // Check this is an existing factory
            if (tab._id != null)
            {
                ClientSideElement tabElement = _ribbonTabManager.getExtension(tab._id);
                if (tabElement == null)
                {
                    String errorMessage = "A tab item referes an unexisting item factory with id '" + tab._id + "'";
                    _logger.error(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                else
                {
                    this._tabsReferences.add(tab._id);
                }
            }
            
            // initialize groups
            for (Group group : tab._groups)
            {
                _lazyInitialize(group._largeSize);
                _lazyInitialize(group._mediumSize);
                _lazyInitialize(group._smallSize);
            }
        }

        _lazyInitialize(this._ribbonConfig.getAppMenu());
        _lazyInitialize(this._ribbonConfig.getUserMenu());
        
        _initialized = true;
    }

    private void _lazyInitialize(GroupSize groupSize)
    {
        if (groupSize != null)
        {
            _lazyInitialize(groupSize._elements);
    
            for (Element element : groupSize._elements)
            {
                if (element instanceof Layout)
                {
                    Layout layout = (Layout) element;
    
                    _lazyInitialize(layout._elements);
    
                    for (Element layoutElement : layout._elements)
                    {
                        if (element instanceof Toolbar)
                        {
                            Toolbar toolbar = (Toolbar) layoutElement;
    
                            _lazyInitialize(toolbar._elements);
                        }
                    }                        
                }
            }
        }
    }
    
    private void _lazyInitialize(List<Element> elements)
    {
        for (Element element : elements)
        {
            if (element instanceof ControlRef)
            {
                ControlRef control = (ControlRef) element;
                
                // Check its an existing factory
                ClientSideElement ribbonControl = _ribbonControlManager.getExtension(control._id);
                if (ribbonControl == null)
                {
                    String errorMessage = "An item referes an unexisting item factory with id '" + control._id + "'";
                    _logger.error(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                else
                {
                    this._controlsReferences.add(control._id);
                }
            }
            else if (element instanceof Toolbar)
            {
                Toolbar toolbar = (Toolbar) element;
                
                _lazyInitialize(toolbar._elements);
            }
        }
    }
    
    /**
     * Retrieve the list of controls referenced by the ribbon
     * @return The list of controls
     */
    public List<ClientSideElement> getControls()
    {
        List<ClientSideElement> controlsList = new ArrayList<>();
        for (String controlId : this._controlsReferences)
        {
            ClientSideElement control = _ribbonControlManager.getExtension(controlId);
            controlsList.add(control);
            
            if (control instanceof MenuClientSideElement)
            {
                controlsList.addAll(_getMenuControls((MenuClientSideElement) control));
            }
        }
        
        return controlsList;
    }
    
    /**
     * Retrieve the list of tabs referenced by the ribbon
     * @return The list of tabs
     */
    public List<ClientSideElement> getTabs()
    {
        List<ClientSideElement> tabsList = new ArrayList<>();
        for (String tabId : this._tabsReferences)
        {
            ClientSideElement tab = _ribbonTabManager.getExtension(tabId);
            tabsList.add(tab);
        }
        
        return tabsList;
    }
    
    private List<ClientSideElement> _getMenuControls(MenuClientSideElement menu)
    {
        List<ClientSideElement> controlsList = new ArrayList<>();
        for (ClientSideElement element : menu.getReferencedClientSideElements())
        {
            controlsList.add(element);
            
            if (element instanceof MenuClientSideElement)
            {
                controlsList.addAll(_getMenuControls((MenuClientSideElement) element));
            }
        }
        return controlsList;
    }

    /**
     * Sax the the initial configuration of the ribbon.
     * @param handler The content handler where to sax
     * @param contextualParameters Contextuals parameters transmitted by the environment.
     * @throws SAXException if an error occurs
     */
    public void saxRibbonDefinition(ContentHandler handler, Map<String, Object> contextualParameters) throws SAXException
    {
        _lazyInitialize();
        Map<Tab, List<Group>> userTabGroups = _generateTabGroups(contextualParameters);
        List<Element> currentAppMenu = _resolveReferences(contextualParameters, this._ribbonConfig.getAppMenu());
        List<Element> currentUserMenu = _resolveReferences(contextualParameters, this._ribbonConfig.getUserMenu());
        
        handler.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
        XMLUtils.startElement(handler, "ribbon");
        
        XMLUtils.startElement(handler, "controls");
        for (String controlId : this._controlsReferences)
        {
            ClientSideElement control = _ribbonControlManager.getExtension(controlId);
            _saxClientSideElementHelper.saxDefinition("control", control, handler, contextualParameters);
            
            if (control instanceof MenuClientSideElement)
            {
                _saxReferencedControl ((MenuClientSideElement) control, handler, contextualParameters);
            }
        }
        XMLUtils.endElement(handler, "controls");

        XMLUtils.startElement(handler, "tabsControls");
        for (String tabId : this._tabsReferences)
        {
            ClientSideElement tab = _ribbonTabManager.getExtension(tabId);
            _saxClientSideElementHelper.saxDefinition("tab", tab, handler, contextualParameters);
        }
        XMLUtils.endElement(handler, "tabsControls");

        XMLUtils.startElement(handler, "app-menu");
        for (Element appMenu : currentAppMenu)
        {
            appMenu.toSAX(handler);
        }
        XMLUtils.endElement(handler, "app-menu");
        
        XMLUtils.startElement(handler, "user-menu");
        for (Element userMenu : currentUserMenu)
        {
            userMenu.toSAX(handler);
        }
        XMLUtils.endElement(handler, "user-menu");

        XMLUtils.startElement(handler, "tabs");
        for (Entry<Tab, List<Group>> entry : userTabGroups.entrySet())
        {
            entry.getKey().saxGroups(handler, entry.getValue());
        }
        XMLUtils.endElement(handler, "tabs");

        XMLUtils.endElement(handler, "ribbon");
        handler.endPrefixMapping("i18n");
    }
    
    /**
     * Generate the tab groups for the current user and contextual parameters, based on the ribbon configured tab list _tabs.
     * @param contextualParameters The contextual parameters
     * @return The list of groups for the current user, mapped by tab
     */
    private Map<Tab, List<Group>> _generateTabGroups(Map<String, Object> contextualParameters)
    {
        Map<Tab, List<Group>> tabGroups = new LinkedHashMap<>();
        for (Tab tab : _ribbonConfig.getTabs())
        {
            List<Group> tabGroup = new ArrayList<>();
            
            for (Group group : tab.getGroups())
            {
                Group newGroup = _createGroupForUser(group, contextualParameters);
                if (!newGroup.isEmpty())
                {
                    tabGroup.add(newGroup);
                }
            }
            
            if (!tabGroup.isEmpty())
            {
                tabGroups.put(tab, tabGroup);
            }
        }
        return tabGroups;
    }

    /**
     * Create a contextualised group for the current user, based on the ribbon configuration group
     * @param ribbonGroup The group form the initial ribbon configuration
     * @param contextualParameters The contextual parameters
     * @return The group for the current user
     */
    private Group _createGroupForUser(Group ribbonGroup, Map<String, Object> contextualParameters)
    {
        Group group = new Group(ribbonGroup);
        GroupSize largeSize = group.getLargeGroupSize();
        GroupSize mediumSize = group.getMediumGroupSize();
        GroupSize smallSize = group.getSmallGroupSize();
        
        if (ribbonGroup.getLargeGroupSize() != null)
        {
            List<Element> largeElements = _resolveReferences(contextualParameters, ribbonGroup.getLargeGroupSize().getChildren());
            largeSize.getChildren().addAll(largeElements);
        }
        
        if (ribbonGroup.getMediumGroupSize() == null)
        {
            _generateGroupSizes(largeSize.getChildren(), mediumSize.getChildren(), false, largeSize.getControlIds().size());
            _generateGroupSizes(largeSize.getChildren(), smallSize.getChildren(), true, largeSize.getControlIds().size());
        }
        else
        {
            List<Element> mediumElements = _resolveReferences(contextualParameters, ribbonGroup.getMediumGroupSize().getChildren());
            mediumSize.getChildren().addAll(mediumElements);
            
            // Don't generate a small group if there is no <small> in the ribbon configuration
            if (ribbonGroup.getSmallGroupSize() != null)
            {
                List<Element> largeElements = _resolveReferences(contextualParameters, ribbonGroup.getSmallGroupSize().getChildren());
                smallSize.getChildren().addAll(largeElements);
            }
        }
        
        if (mediumSize.isSame(largeSize))
        {
            largeSize.getChildren().clear();
        }
        if (mediumSize.isSame(smallSize))
        {
            smallSize.getChildren().clear();
        }
        
        return group;
    }
    
    /**
     * Resolve all the controls references into the real ids for the contextual parameters, and the current user rights.
     * @param contextualParameters The contextual parameters
     * @param elements The elements to resolve
     * @return The list of resolved elements
     */
    private List<Element> _resolveReferences(Map<String, Object> contextualParameters, List<Element> elements)
    {
        List<Element> resolvedElements = new ArrayList<>();
        
        for (Element element : elements)
        {
            if (element instanceof ControlRef)
            {
                ControlRef controlRef = (ControlRef) element;
                ClientSideElement extension = _ribbonControlManager.getExtension(controlRef._id);
                for (Script script : extension.getScripts(contextualParameters))
                {
                    resolvedElements.add(new ControlRef(script.getId(), controlRef._colspan, controlRef._controlLogger));
                }
            }
            
            if (element instanceof Layout)
            {
                List<Element> layoutElements = _resolveReferences(contextualParameters, element.getChildren());
                if (layoutElements.size() > 0)
                {
                    Layout layout = (Layout) element;
                    Layout resolvedLayout = new Layout(layout, layout._size);
                    resolvedLayout.getChildren().addAll(layoutElements);
                    resolvedElements.add(resolvedLayout);
                }
            }
            
            if (element instanceof Toolbar)
            {
                List<Element> toolbarElements = _resolveReferences(contextualParameters, element.getChildren());
                if (toolbarElements.size() > 0)
                {
                    Toolbar toolbar = (Toolbar) element;
                    Toolbar resolvedToolbar = new Toolbar(_logger, toolbar._colspan);
                    resolvedToolbar.getChildren().addAll(toolbarElements);
                    resolvedElements.add(resolvedToolbar);
                }
            }
            
            if (element instanceof Separator)
            {
                resolvedElements.add(element);
            }
        }
        
        // Remove separators at the beginning and the end 
        while (resolvedElements.size() > 0 && resolvedElements.get(0) instanceof Separator)
        {
            resolvedElements.remove(0);
        }
        while (resolvedElements.size() > 0 && resolvedElements.get(resolvedElements.size() - 1) instanceof Separator)
        {
            resolvedElements.remove(resolvedElements.size() - 1);
        }
        
        return resolvedElements;
    }
    

    private void _generateGroupSizes(List<Element> largeElements, List<Element> groupSizeElements, boolean generateSmallSize, int groupTotalSize)
    {
        List<ControlRef> controlsQueue = new ArrayList<>();
        for (Element largeElement : largeElements)
        {
            if (largeElement instanceof ControlRef)
            {
                controlsQueue.add((ControlRef) largeElement);
            }
            
            if (largeElement instanceof Toolbar)
            {
                _processControlRefsQueue(controlsQueue, groupSizeElements, groupTotalSize, generateSmallSize);
                controlsQueue.clear();
                
                Toolbar toolbar = (Toolbar) largeElement;
                groupSizeElements.add(toolbar);
            }
            
            if (largeElement instanceof Layout)
            {
                _processControlRefsQueue(controlsQueue, groupSizeElements, groupTotalSize, generateSmallSize);
                controlsQueue.clear();
                
                Layout layout = (Layout) largeElement;
                Layout verySmallLayout = new Layout(layout, CONTROLSIZE.VERYSMALL);
                verySmallLayout.getChildren().addAll(layout.getChildren());
                
                groupSizeElements.add(verySmallLayout);
            }
        }
        
        _processControlRefsQueue(controlsQueue, groupSizeElements, groupTotalSize, generateSmallSize);
    }
    
    private void _processControlRefsQueue(List<ControlRef> controlsQueue, List<Element> groupSizeElements, int groupTotalSize, boolean generateSmallSize)
    {
        int queueSize = controlsQueue.size();
        int index = 0;

        while (index < queueSize)
        {
            // grab the next batch of controls, at least 1 and up to 3 controls
            List<ControlRef> controlsBuffer = new ArrayList<>();
            while (controlsBuffer.size() == 0 || (controlsBuffer.size() < 3 && controlsBuffer.size() + index != queueSize % 3))
            {
                controlsBuffer.add(controlsQueue.get(index + controlsBuffer.size()));
            }
            
            if (index == 0)
            {
                if (groupTotalSize > 1 && groupTotalSize <= 3)
                {
                    Layout newLayout = new Layout(1, CONTROLSIZE.SMALL, LAYOUTALIGN.TOP, _logger);
                    newLayout.getChildren().addAll(controlsBuffer);
                    groupSizeElements.add(newLayout);
                }
                else
                {
                    groupSizeElements.addAll(controlsBuffer);
                }
            }
            else
            {
                CONTROLSIZE controlSize = generateSmallSize && index >= 0 ? CONTROLSIZE.VERYSMALL : CONTROLSIZE.SMALL;
                Layout newLayout = new Layout(1, controlSize, LAYOUTALIGN.TOP, _logger);
                newLayout.getChildren().addAll(controlsBuffer);
                groupSizeElements.add(newLayout);
            }
            
            index += controlsBuffer.size();
        }
    }

    private void _saxReferencedControl(MenuClientSideElement menu, ContentHandler handler, Map<String, Object> contextualParameters) throws SAXException
    {
        List<ClientSideElement> referencedControl = menu.getReferencedClientSideElements();
        
        for (ClientSideElement element : referencedControl)
        {
            if (!this._controlsReferences.contains(element.getId()))
            {
                _saxClientSideElementHelper.saxDefinition("control", element, handler, contextualParameters);
            }
            
            if (element instanceof MenuClientSideElement)
            {
                _saxReferencedControl ((MenuClientSideElement) element, handler, contextualParameters);
            }
        }
    }
    
    /**
     * A ribbon configuration, with tab, user and app menus
     */
    public class RibbonConfiguration
    {
        /** The tabs of the ribbon */
        protected List<Tab> _tabs = new ArrayList<>();

        /** The App menu elements of the ribbon */
        protected List<Element> _appMenu = new ArrayList<>();
        
        /** The user menu elements of the ribbon */
        protected List<Element> _userMenu = new ArrayList<>();
        
        /**
         * Get the tabs for this configuration
         * @return the tabs
         */
        public List<Tab> getTabs()
        {
            return _tabs;
        }

        /**
         * Get the app menu elements for this configuration
         * @return the appMenu
         */
        public List<Element> getAppMenu()
        {
            return _appMenu;
        }

        /**
         * Get the user menu elements for this configuration
         * @return the userMenu
         */
        public List<Element> getUserMenu()
        {
            return _userMenu;
        }
    }
    
    /**
     * A tab of the ribbon
     */
    public class Tab
    {
        /** The label of the tab */
        protected I18nizableText _label;
        
        /** The optional id of the contextual client side element determining the state of the ribbon */
        protected String _id;
        
        /** The color (between 1 and 6) for a contextual tab */
        protected String _contextualColor;
        
        /** The id of the contextual group (can be null for single contextual tab) */
        protected String _contextualGroup;
        
        /** The label of the contextual group */
        protected I18nizableText _contextualLabel;
        
        /** The list of groups in the tab */
        protected List<Group> _groups = new ArrayList<>();
        
        /** Logger */
        protected Logger _log;
        
        /**
         * Creates a tab
         * @param tabConfiguration The configuration of the tab
         * @param ribbonManager The ribbon manager
         * @param logger The logger
         * @throws ConfigurationException if an error occurs in the configuration
         */
        public Tab(Configuration tabConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
        {
            _log = logger;
            
            if (_log.isDebugEnabled())
            {
                _log.debug("Creating tab");
            }

            _configureId(tabConfiguration);
            if (tabConfiguration.getAttribute("ref-id", null) != null || tabConfiguration.getChild("tab-control", false) != null)
            {
                _generateTabControl(tabConfiguration, ribbonManager);
            }
            _configureLabel(tabConfiguration);
            _configureGroups(tabConfiguration, ribbonManager);
        }
        
        /**
         * Get the id of this tab;
         * @return the id
         */
        public String getId()
        {
            return _id;
        }

        /**
         * Configure tab optional id
         * @param tabConfiguration One tab configuration
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureId(Configuration tabConfiguration) throws ConfigurationException
        {
            this._id = tabConfiguration.getAttribute("id", null);
            this._contextualColor = tabConfiguration.getAttribute("contextualColor", null);
            this._contextualGroup = tabConfiguration.getAttribute("contextualGroup", null);
            
            String contextualLabelString = tabConfiguration.getAttribute("contextualLabel", null);
            if (contextualLabelString != null)
            {
                this._contextualLabel = new I18nizableText("application", contextualLabelString);
            }
            
            if (_log.isDebugEnabled() && this._id != null)
            {
                _log.debug("Tab id is " + this._id);
            }
        }
        
        /**
         * Generate a new tab control on the fly
         * @param tabConfiguration The tab configuration
         * @param ribbonManager The ribbon manager
         * @throws ConfigurationException If an error occurs
         */
        protected void _generateTabControl(Configuration tabConfiguration, RibbonManager ribbonManager) throws ConfigurationException
        {
            if (this._id == null)
            {
                this._id = UUID.randomUUID().toString();
            }
            
            DefaultConfiguration defaultConfig = new DefaultConfiguration(tabConfiguration.getChild("tab-control"));
            String refId = defaultConfig.getAttribute("ref-id", null);
            
            String classname = tabConfiguration.getAttribute("class", null);
            if (classname == null)
            {
                if (refId != null)
                {
                    defaultConfig.setAttribute("point", tabConfiguration.getAttribute("point", RibbonTabsManager.ROLE));
                }
                else
                {
                    classname = org.ametys.core.ui.StaticClientSideElement.class.getName();
                    defaultConfig.setAttribute("class", classname);
                }
            }
            
            ribbonManager.addExtension(this._id, "core-ui", null, defaultConfig);
            
            if (_log.isDebugEnabled())
            {
                _log.debug("Generated Tab id is " + this._id);
            }
        }

        /**
         * Configure one tab label
         * @param tabConfiguration One tab configuration
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureLabel(Configuration tabConfiguration) throws ConfigurationException
        {
            this._label = new I18nizableText("application", tabConfiguration.getAttribute("label"));
            
            if (_log.isDebugEnabled())
            {
                _log.debug("Tab label is " + this._label);
            }
        }
        
        /**
         * Configure tabs groups
         * @param tabConfiguration One tab configuration
         * @param ribbonManager The ribbon manager
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureGroups(Configuration tabConfiguration, RibbonManager ribbonManager) throws ConfigurationException
        {
            Configuration[] groupsConfigurations = tabConfiguration.getChild("groups").getChildren("group");
            for (Configuration groupConfiguration : groupsConfigurations)
            {
                Group group = new Group(groupConfiguration, ribbonManager, _log);
                _groups.add(group);
            }
        }
        
        /**
         * Retrieve the list of configured groups
         * @return The list of groups
         */
        public List<Group> getGroups()
        {
            return _groups;
        }
        
        /**
         * Sax the configuration of the tab.
         * @param handler The content handler where to sax
         * @param groups The list of groups to sax
         * @throws SAXException if an error occurs
         */
        public void saxGroups(ContentHandler handler, List<Group> groups) throws SAXException
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("label", _label.getCatalogue() + ":" + _label.getKey());
            StringBuilder i18nAttr = new StringBuilder("label");
            
            if (_id != null)
            {
                attrs.addCDATAAttribute("id", _id);
                if (_contextualColor != null)
                {
                    attrs.addCDATAAttribute("contextualColor", _contextualColor);
                }
                if (_contextualGroup != null)
                {
                    attrs.addCDATAAttribute("contextualGroup", _contextualGroup);
                }
                if (_contextualLabel != null)
                {
                    attrs.addCDATAAttribute("contextualLabel", _contextualLabel.getCatalogue() + ":" + _contextualLabel.getKey());
                    i18nAttr.append(" contextualLabel");
                }
            }
            
            attrs.addCDATAAttribute("http://apache.org/cocoon/i18n/2.1", "attr", "i18n:attr", i18nAttr.toString());
            
            XMLUtils.startElement(handler, "tab", attrs);

            XMLUtils.startElement(handler, "groups");
            for (Group group : groups)
            {
                group.toSAX(handler);
            }
            XMLUtils.endElement(handler, "groups");
            
            XMLUtils.endElement(handler, "tab");
        }
    }
    
    /**
     * A group of a tab in the ribbon
     */
    public class Group
    {
        /** The label of the group */
        protected I18nizableText _label;
        /** The icon of the group (in collapsed state) */
        protected String _icon;
        /** The priority of the group (for resize purposes) */
        protected int _priority;
        /** The control to call when dialog box launcher is clicked (we exactly call the method in the param 'dialog-box-launcher') */
        protected String _dialogBoxLauncher;
        /** The large version of the group */
        protected GroupSize _largeSize;
        /** The medium version of the group */
        protected GroupSize _mediumSize;
        /** The short version of the group */
        protected GroupSize _smallSize;
        
        /** The logger */
        protected Logger _groupLogger;
        
        /**
         * Creates a group
         * @param groupConfiguration The configuration of the group
         * @param ribbonManager The ribbon manager
         * @param logger The logger
         * @throws ConfigurationException if an error occurs in the configuration
         */
        public Group(Configuration groupConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
        {
            _groupLogger = logger;
            _dialogBoxLauncher = groupConfiguration.getAttribute("dialog-box-launcher", "");
            _priority = groupConfiguration.getAttributeAsInteger("priority", 0);
            _configureLabelAndIcon(groupConfiguration);
            _configureSize(groupConfiguration, ribbonManager);
        }     

        /**
         * Creates an empty group
         * @param logger The logger
         */
        public Group(Logger logger)
        {
            _groupLogger = logger;
        }
        
        /**
         * Create an empty group by copying another group's parameters
         * @param ribbonGroup The ribbon group
         */
        public Group(Group ribbonGroup)
        {
            _label = ribbonGroup._label;
            _icon = ribbonGroup._icon;
            _priority = ribbonGroup._priority;
            _groupLogger = ribbonGroup._groupLogger;
            _dialogBoxLauncher = ribbonGroup._dialogBoxLauncher;
            
            _largeSize = new GroupSize(_groupLogger);
            _mediumSize = new GroupSize(_groupLogger);
            _smallSize = new GroupSize(_groupLogger);
        }

        /**
         * Configure one group label
         * @param groupConfiguration One group configuration
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureLabelAndIcon(Configuration groupConfiguration) throws ConfigurationException
        {
            this._label = new I18nizableText("application", groupConfiguration.getAttribute("label"));
            if (_groupLogger.isDebugEnabled())
            {
                _groupLogger.debug("Group label is " + this._label);
            }
            
            this._icon = groupConfiguration.getAttribute("icon", "");
            if (_groupLogger.isDebugEnabled())
            {
                _groupLogger.debug("Group icon is " + this._label);
            }
        }
        
        /**
         * Configure the different size of the group
         * @param groupConfiguration One group configuration
         * @param ribbonManager The ribbon manager
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureSize(Configuration groupConfiguration, RibbonManager ribbonManager) throws ConfigurationException
        {
            if (groupConfiguration.getChild("medium").getChildren().length > 0)
            {
                _largeSize = groupConfiguration.getChild("large", false) != null ? new GroupSize(groupConfiguration.getChild("large"), ribbonManager, _groupLogger) : null;
                _mediumSize = new GroupSize(groupConfiguration.getChild("medium"), ribbonManager, _groupLogger);
            }
            else
            {
                _largeSize = new GroupSize(groupConfiguration.getChild("large", false) != null ? groupConfiguration.getChild("large") : groupConfiguration, ribbonManager, _groupLogger);
                _mediumSize = null;
            }
            
            _smallSize = groupConfiguration.getChild("small", false) != null ? new GroupSize(groupConfiguration.getChild("small"), ribbonManager, _groupLogger) : null;
            
            _checkSizeConsistency(groupConfiguration);
        }
        
        private void _checkSizeConsistency(Configuration groupConfiguration) throws ConfigurationException
        {
            if (_mediumSize != null)
            {
                Set<String> mediumControlIds = _mediumSize.getControlIds();
                
                if (_smallSize != null && _smallSize.getControlIds().size() > 0)
                {
                    Collection disjunction = CollectionUtils.disjunction(_smallSize.getControlIds(), mediumControlIds);
                    if (disjunction.size() > 0)
                    {
                        String disjunctionAdString = StringUtils.join(disjunction, ", ");
                        throw new ConfigurationException("The small configuration of the group does not have the same elements as the medium one (" + disjunctionAdString + ")", groupConfiguration);
                    }
                }
                if (_largeSize != null && _largeSize.getControlIds().size() > 0)
                {
                    Collection disjunction = CollectionUtils.disjunction(_largeSize.getControlIds(), mediumControlIds);
                    if (disjunction.size() > 0)
                    {
                        String disjunctionAdString = StringUtils.join(disjunction, ", ");
                        throw new ConfigurationException("The large configuration of the group does not have the same elements as the medium one (" + disjunctionAdString + ")", groupConfiguration);
                    }
                }
            }
        }
        
        /**
         * Return true if the group does not have any controls
         * @return True if empty
         */
        public boolean isEmpty()
        {
            return _mediumSize == null || _mediumSize.getControlIds().size() <= 0;
        }
        
        /**
         * Retrieve the large group size from this group
         * @return The large group size. Can be null
         */
        public GroupSize getLargeGroupSize()
        {
            return _largeSize;
        }
        
        /**
         * Retrieve the medium group size from this group
         * @return The medium group size. Can be null
         */
        public GroupSize getMediumGroupSize()
        {
            return _mediumSize;
        }
        
        /**
         * Retrieve the small group size from this group
         * @return The small group size. Can be null
         */
        public GroupSize getSmallGroupSize()
        {
            return _smallSize;
        }

        /**
         * Sax the the configuration of the group.
         * @param handler The content handler where to sax
         * @throws SAXException if an error occurs
         */
        public void toSAX(ContentHandler handler) throws SAXException
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("icon", _icon);
            attrs.addCDATAAttribute("priority", Integer.toString(_priority));
            attrs.addCDATAAttribute("label", _label.getCatalogue() + ":" + _label.getKey());
            if (!StringUtils.isEmpty(_dialogBoxLauncher))
            {
                attrs.addCDATAAttribute("dialog-box-launcher", _dialogBoxLauncher);
            }
            attrs.addCDATAAttribute("http://apache.org/cocoon/i18n/2.1", "attr", "label");
            
            XMLUtils.startElement(handler, "group", attrs);

            _largeSize.toSAX("large", handler);
            _mediumSize.toSAX("medium", handler);
            _smallSize.toSAX("small", handler);
            
            XMLUtils.endElement(handler, "group");
        }
    }    
    
    /**
     * A group in a defined size
     */
    public class GroupSize
    {
        /** The list of elements in the group (controls or layouts) */
        protected List<Element> _elements = new ArrayList<>();
        
        /** The logger */
        protected Logger _groupSizeLogger;
        
        /**
         * Creates a group in a defined size
         * @param groupSizeConfiguration The configuration for the size
         * @param ribbonManager The ribbon manager
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public GroupSize(Configuration groupSizeConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
        {
            this._groupSizeLogger = logger;
            
            Configuration[] elementsConfigurations = groupSizeConfiguration.getChildren();
            for (Configuration elementConfiguration : elementsConfigurations)
            {
                if ("control".equals(elementConfiguration.getName()))
                {
                    ControlRef control = new ControlRef(elementConfiguration, ribbonManager, _groupSizeLogger);
                    _elements.add(control);
                }
                else if ("layout".equals(elementConfiguration.getName()))
                {
                    Layout layout = new Layout(elementConfiguration, ribbonManager, _groupSizeLogger);
                    _elements.add(layout);
                }
                else if (_groupSizeLogger.isWarnEnabled())
                {
                    _groupSizeLogger.warn("During configuration of the ribbon, the group use an unknow tag '" + elementConfiguration.getName() + "'");
                }
            }
        }
        
        /**
         * Creates an empty group in a defined size
         * @param logger The logger
         */
        public GroupSize(Logger logger)
        {
            this._groupSizeLogger = logger;
        }
        
        /**
         * Get a set of all referenced ids
         * @return A non null set of control ids
         */
        public Set<String> getControlIds()
        {
            return _getControlIds(_elements);
        }
        
        private Set<String> _getControlIds(List<Element> elements)
        {
            Set<String> ids = new HashSet<>();
            
            for (Element element : elements)
            {
                if (element instanceof ControlRef)
                {
                    ControlRef controlRef = (ControlRef) element;
                    ids.add(controlRef._id);
                }
                else if (element instanceof Layout)
                {
                    Layout layout = (Layout) element;
            
                    ids.addAll(_getControlIds(layout._elements));
                }
                else
                {
                    Toolbar toolbar = (Toolbar) element;
                    
                    ids.addAll(_getControlIds(toolbar._elements));
                }
            }
            
            return ids;
        }
        
        /**
         * Retrieve the list of children elements in this element.
         * @return The list of elements.
         */
        public List<Element> getChildren()
        {
            return _elements;
        }
        
        /**
         * Sax the the configuration of the group size.
         * @param elementName The name of the surrounding element to use
         * @param handler The content handler where to sax
         * @throws SAXException if an error occurs
         */
        public void toSAX(String elementName, ContentHandler handler) throws SAXException
        {
            XMLUtils.startElement(handler, elementName);

            for (Element element : _elements)
            {
                element.toSAX(handler);
            }

            XMLUtils.endElement(handler, elementName);
        }
        
        /**
         * Test if this GroupSize contains the same elements as another GroupSize
         * @param obj The other GroupSize
         * @return True if they are equals
         */
        public boolean isSame(GroupSize obj)
        {
            List<Element> objElements = obj.getChildren();
            if (objElements.size() != _elements.size())
            {
                return false;
            }
            for (int i = 0; i < _elements.size(); i++)
            {
                Element element = _elements.get(i);
                Element objElement = objElements.get(i);
                if (element == null || objElement == null || !element.isSame(objElement))
                {
                    return false;
                }
            }
            return true;
        }
    }   
    
    /**
     * An element in the ribbon
     */
    public interface Element
    {
        /**
         * Retrieve the list of children elements in this element.
         * @return The list of elements.
         */
        public List<Element> getChildren();
        
        /**
         * Sax the the configuration of the element.
         * @param handler The content handler where to sax
         * @throws SAXException if an error occurs
         */
        public void toSAX(ContentHandler handler) throws SAXException;
        
        
        /**
         * Test if an element is equal to another element
         * @param element The element to compare to
         * @return True if equals
         */
        boolean isSame(Element element);
    }
    
    /**
     * A control
     */
    public class ControlRef implements Element
    {
        /** The id of the control */
        protected String _id;

        /** Number of columns used by the control upon some layouts. 1 is the common and defaul value */
        protected int _colspan;
        
        /** Logger */
        protected Logger _controlLogger;
        
        /**
         * Creates a control reference
         * @param controlConfiguration The configuration for the control
         * @param ribbonManager The ribbon manager
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public ControlRef(Configuration controlConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
        {
            String refId = controlConfiguration.getAttribute("ref-id", null);
            if (refId == null && controlConfiguration.getChildren().length == 0)
            {
                _initialize(controlConfiguration.getAttribute("id"), controlConfiguration.getAttributeAsInteger("colspan", 1), logger);
            }
            else
            {
                String id = controlConfiguration.getAttribute("id", UUID.randomUUID().toString());
                DefaultConfiguration defaultConfig = new DefaultConfiguration(controlConfiguration);
                
                String classname = controlConfiguration.getAttribute("class", null);
                if (classname == null)
                {
                    if (refId != null)
                    {
                        defaultConfig.setAttribute("point", controlConfiguration.getAttribute("point", RibbonControlsManager.ROLE));
                    }
                    else
                    {
                        classname = org.ametys.core.ui.SimpleMenu.class.getName();
                        defaultConfig.setAttribute("class", classname);
                    }
                }
                
                ribbonManager.addExtension(id, "core-ui", null, defaultConfig);
    
                _initialize(id, controlConfiguration.getAttributeAsInteger("colspan", 1), logger);
            }
        }

        /**
         * Creates a control reference
         * @param id The id referenced by this control
         * @param colspan The colspan of this control
         * @param logger The logger
         */
        public ControlRef(String id, int colspan, Logger logger)
        {
            _initialize(id, colspan, logger);
        }

        private void _initialize(String id, int colspan, Logger logger)
        {
            this._controlLogger = logger;
            
            this._id = id;
            if (_controlLogger.isDebugEnabled())
            {
                _controlLogger.debug("Control id is " + this._id);
            }
            
            this._colspan = colspan;
            if (_controlLogger.isDebugEnabled())
            {
                _controlLogger.debug("Control colspan is " + this._colspan);
            }
        }
        
        @Override
        public List<Element> getChildren()
        {
            return new ArrayList<>();
        }
        
        @Override
        public void toSAX(ContentHandler handler) throws SAXException
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", _id);
            attrs.addCDATAAttribute("colspan", Integer.toString(_colspan));
            XMLUtils.createElement(handler, "control", attrs);
        }

        public boolean isSame(Element element)
        {
            if (!(element instanceof ControlRef))
            {
                return false;
            }
            
            ControlRef controlRef = (ControlRef) element;
            return controlRef._id == _id && controlRef._colspan == _colspan;
        }
    }
    
    /**
     * A layout of controls
     */
    public class Layout implements Element
    {
        /** The layout alignment */
        protected LAYOUTALIGN _layoutAlign;
        
        /** The size of the control */
        protected CONTROLSIZE _size;
        
        /** Number of columns used by the control upon some layouts. 1 is the common and defaul value */
        protected int _cols;
        
        /** The elements in the layout. Can be controls or toolbars */
        protected List<Element> _elements = new ArrayList<>();

        /** Logger */
        protected Logger _layoutLogger;

        /**
         * Creates a layout of controls
         * @param layoutConfiguration The configuration for the layout
         * @param ribbonManager The ribbon manager 
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public Layout(Configuration layoutConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
        {
            this(layoutConfiguration.getAttributeAsInteger("cols", 1), CONTROLSIZE.createsFromString(layoutConfiguration.getAttribute("size", null)), LAYOUTALIGN.createsFromString(layoutConfiguration.getAttribute("align", null)), logger);

            Configuration[] elementsConfigurations = layoutConfiguration.getChildren();
            for (Configuration elementConfiguration : elementsConfigurations)
            {
                if ("control".equals(elementConfiguration.getName()))
                {
                    ControlRef control = new ControlRef(elementConfiguration, ribbonManager, _layoutLogger);
                    _elements.add(control);
                }
                else if ("toolbar".equals(elementConfiguration.getName()))
                {
                    Toolbar toolbar = new Toolbar(elementConfiguration, ribbonManager, _layoutLogger);
                    _elements.add(toolbar);
                }
                else if (_layoutLogger.isWarnEnabled())
                {
                    _layoutLogger.warn("During configuration of the ribbon, the layout use an unknow tag '" + elementConfiguration.getName() + "'");
                }
            }
        }
        
        /**
         * Create a new layout by duplicating an existing layout
         * @param layout The original layout
         * @param size The new layout size
         */
        public Layout(Layout layout, CONTROLSIZE size)
        {
            this(layout._cols, size, layout._layoutAlign, layout._layoutLogger);
        }
        
        /**
         * Creates a layout of controls
         * @param cols The number of columns
         * @param size The size
         * @param align The alignment
         * @param logger The logger
         */
        public Layout(int cols, CONTROLSIZE size, LAYOUTALIGN align, Logger logger)
        {
            this._layoutLogger = logger;
            
            this._cols = cols;
            if (_layoutLogger.isDebugEnabled())
            {
                _layoutLogger.debug("Control colspan is " + this._cols);
            }
            
            this._layoutAlign = align; 
            if (_layoutLogger.isDebugEnabled())
            {
                _layoutLogger.debug("Control align is " + this._layoutAlign);
            }
            
            _size = size;
            if (_layoutLogger.isDebugEnabled())
            {
                _layoutLogger.debug("Control size is " + this._size);
            }
        }
        
        @Override
        public List<Element> getChildren()
        {
            return _elements;
        }
        
        @Override
        public void toSAX(ContentHandler handler) throws SAXException
        {
            AttributesImpl attrs = new AttributesImpl();
            
            if (_layoutAlign != null)
            {
                attrs.addCDATAAttribute("align", _layoutAlign.toString());
            }

            if (_size != null)
            {
                attrs.addCDATAAttribute("size", _size.toString());
            }

            attrs.addCDATAAttribute("cols", Integer.toString(_cols));
            
            XMLUtils.startElement(handler, "layout", attrs);
            
            for (Element element : _elements)
            {
                element.toSAX(handler);
            }
            
            XMLUtils.endElement(handler, "layout");
        }

        public boolean isSame(Element element)
        {
            if (!(element instanceof Layout))
            {
                return false;
            }
            
            Layout layout = (Layout) element;
            if (layout._cols != _cols || layout._layoutAlign != _layoutAlign || layout._size != _size)
            {
                return false;
            }
            
            List<Element> layoutElements = layout.getChildren();
            if (layoutElements.size() != _elements.size())
            {
                return false;
            }
            for (int i = 0; i < _elements.size(); i++)
            {
                Element child = _elements.get(i);
                Element objElement = layoutElements.get(i);
                if (child == null || objElement == null || !child.isSame(objElement))
                {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    /**
     * A toolbar of controls
     */
    public class Toolbar implements Element
    {
        /** Number of columns used by the control upon some layouts. 1 is the common and defaul value */
        protected int _colspan;
        
        /** The elements in the layout. Can be controls */
        protected List<Element> _elements = new ArrayList<>();

        /** Logger */
        protected Logger _toolbarLogger;
        
        /**
         * Creates a toolbar of controls
         * @param toolbarConfiguration The configuration for the layout
         * @param ribbonManager The ribbon manager
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public Toolbar(Configuration toolbarConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
        {
            this(logger, toolbarConfiguration.getAttributeAsInteger("colspan", 1));
            
            Configuration[] elementsConfigurations = toolbarConfiguration.getChildren();
            for (Configuration elementConfiguration : elementsConfigurations)
            {
                if ("control".equals(elementConfiguration.getName()))
                {
                    ControlRef control = new ControlRef(elementConfiguration, ribbonManager, _toolbarLogger);
                    _elements.add(control);
                }
                else if (_toolbarLogger.isWarnEnabled())
                {
                    _toolbarLogger.warn("During configuration of the ribbon, the toolbar use an unknow tag '" + elementConfiguration.getName() + "'");
                }
            }
        }

        /**
         * Creates an empty toolbar of controls
         * @param logger The logger
         * @param colspan The toolbar colspan
         */
        public Toolbar(Logger logger, int colspan)
        {
            this._toolbarLogger = logger;
            
            this._colspan = colspan;
            if (_toolbarLogger.isDebugEnabled())
            {
                _toolbarLogger.debug("Control colspan is " + this._colspan);
            }
        }
        
        @Override
        public List<Element> getChildren()
        {
            return _elements;
        }
        
        @Override
        public void toSAX(ContentHandler handler) throws SAXException
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("colspan", Integer.toString(_colspan));
            
            XMLUtils.startElement(handler, "toolbar", attrs);
            
            for (Element element : _elements)
            {
                element.toSAX(handler);
            }
            
            XMLUtils.endElement(handler, "toolbar");
        }

        public boolean isSame(Element element)
        {
            if (!(element instanceof Toolbar))
            {
                return false;
            }
            
            Toolbar toolbar = (Toolbar) element;
            if (toolbar._colspan != _colspan)
            {
                return false;
            }
            
            if (toolbar.getChildren().size() != _elements.size())
            {
                return false;
            }
            for (int i = 0; i < _elements.size(); i++)
            {
                Element child = _elements.get(i);
                Element objElement = toolbar.getChildren().get(i);
                if (child == null || objElement == null || !child.isSame(objElement))
                {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    /**
     * A menu separator
     */
    public class Separator implements Element
    {
        @Override
        public List<Element> getChildren()
        {
            return new ArrayList<>();
        }
        
        @Override
        public void toSAX(ContentHandler handler) throws SAXException
        {
            XMLUtils.createElement(handler, "separator");
        }

        public boolean isSame(Element element)
        {
            return element instanceof Separator;
        }
    }
}
