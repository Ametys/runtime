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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.ClientSideElement.Script;
import org.ametys.core.ui.ribbonconfiguration.ControlRef;
import org.ametys.core.ui.ribbonconfiguration.Element;
import org.ametys.core.ui.ribbonconfiguration.Group;
import org.ametys.core.ui.ribbonconfiguration.GroupSize;
import org.ametys.core.ui.ribbonconfiguration.Layout;
import org.ametys.core.ui.ribbonconfiguration.RibbonConfiguration;
import org.ametys.core.ui.ribbonconfiguration.Separator;
import org.ametys.core.ui.ribbonconfiguration.Tab;
import org.ametys.core.ui.ribbonconfiguration.Toolbar;

/**
 * This class handles the ribbon configuration needed for client side display.
 */
public class RibbonConfigurationManager
{
    private static Logger _logger = LoggerFactory.getLogger(RibbonConfigurationManager.class);
    
    private static Pattern _PLUGINNAMEPATTERN = Pattern.compile("^plugin:([^:]+)://.*$");

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
         * @return The size corresponding to the string or null if unknown
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
         * @return The align corresponding to the string or null if unknown
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
    
    /**
     * Type of exclusion in the ribbon configuration
     */
    public enum EXCLUDETYPE
    {
        /** Excluding an entire plugin */
        PLUGIN("plugin"),
        /** Excluding an extension */
        EXTENSION("extension"),
        /** Excluding a file */
        FILE("file"),
        /** Excluding a tab by label */
        TABLABEL("tablabel"),
        /** Excluding a control by id */
        CONTROL("control");
        
        private String _value;
        
        private EXCLUDETYPE(String value)
        {
            this._value = value;   
        }  
           
        @Override
        public String toString() 
        {
            return _value;
        }   

        /**
         * Converts a string to a EXCLUDETYPE
         * @param size The type to convert
         * @return The exclude type corresponding to the string or null if unknown
         */
        public static EXCLUDETYPE createsFromString(String size)
        {
            for (EXCLUDETYPE v : EXCLUDETYPE.values())
            {
                if (v.toString().equals(size))
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
    /** The ribbon controls manager */
    protected RibbonControlsManager _ribbonControlManager;
    /** The ribbon import manager */
    protected RibbonImportManager _ribbonImportManager;
    
    private boolean _initialized;
    
    /**
     * Constructor
     * @param ribbonControlManager The ribbon control manager
     * @param ribbonManager The ribbon manager for this config
     * @param ribbonTabManager the ribbon tab manager
     * @param ribbonImportManager The ribbon import manager
     * @param saxClientSideElementHelper the helper to SAX client side element
     * @param resolver the excalibur source resolver
     * @param dependenciesManager The dependencies manager
     * @param ribbonManagerCache The ribbon manager cache helper
     * @param config the ribbon configuration
     * @param workspaceName The current workspace name
     * @throws RuntimeException if an error occurred
     */
    public RibbonConfigurationManager (RibbonControlsManager ribbonControlManager, RibbonManager ribbonManager, RibbonTabsManager ribbonTabManager, RibbonImportManager ribbonImportManager, SAXClientSideElementHelper saxClientSideElementHelper, SourceResolver resolver, ClientSideElementDependenciesManager dependenciesManager, RibbonManagerCache ribbonManagerCache, Source config, String workspaceName)
    {
        _ribbonControlManager = ribbonControlManager;
        _ribbonManager = ribbonManager;
        _ribbonTabManager = ribbonTabManager;
        _ribbonImportManager = ribbonImportManager;
        _saxClientSideElementHelper = saxClientSideElementHelper;
        _resolver = resolver;
        
        try
        {
            _configure(config, dependenciesManager, ribbonManagerCache, workspaceName);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to read the configuration file", e);
        }
        
        for (Entry<String, List<String>> entry : _ribbonConfig.getDependencies().entrySet())
        {
            String extensionPoint = entry.getKey();
            for (String extensionId : entry.getValue())
            {
                dependenciesManager.register(extensionPoint, extensionId);
            }
        }
    }

    private void _configure(Source config, ClientSideElementDependenciesManager dependenciesManager, RibbonManagerCache ribbonManagerCache, String workspaceName) throws Exception
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
            Map<EXCLUDETYPE, List<String>> excludedList = _configureExclude(configuration);
            _configure(configuration, dependenciesManager, importsValidity, excludedList, false);
            
            _configureAutomaticImports(dependenciesManager, workspaceName, excludedList, importsValidity);
            Map<String, Tab> tabsLabelMapping = _ribbonConfig.getTabs().stream().filter(tab -> !tab.isOverride()).collect(Collectors.toMap(Tab::getLabel, Function.identity(), (tab1, tab2) -> tab1));
            _configureTabOverride(tabsLabelMapping);
            _configureTabOrder(tabsLabelMapping);
            
            ribbonManagerCache.addCachedConfiguration(_ribbonManager, _ribbonConfig, importsValidity);
            _ribbonManager.initializeExtensions();
        }
    }
    
    private Map<EXCLUDETYPE, List<String>> _configureExclude(Configuration configuration) throws ConfigurationException
    {
        Map<EXCLUDETYPE, List<String>> excluded = new HashMap<>();
        for (EXCLUDETYPE excludetype : EXCLUDETYPE.values())
        {
            excluded.put(excludetype, new ArrayList<String>());
        }
        
        for (Configuration excludeConf : configuration.getChild("exclude").getChildren())
        {
            String tagName = excludeConf.getName();
            String value = excludeConf.getValue();
            String type = excludeConf.getAttribute("type", null);
            if (tagName.equals("import") && ("plugin".equals(type) || "extension".equals(type) || "file".equals(type)))
            {
                excluded.get(EXCLUDETYPE.createsFromString(type)).add(value);
            }
            else if (tagName.equals("tab") && "label".equals(type))
            {
                excluded.get(EXCLUDETYPE.TABLABEL).add(value);
            }
            else if (tagName.equals("control"))
            {
                excluded.get(EXCLUDETYPE.CONTROL).add(value);
            }
            else
            {
                throw new ConfigurationException("Invalid exclude tab in the ribbon configuration '" + tagName + "'", configuration);
            }
        }
        
        return excluded;
    }
    
    private void _configure(Configuration configuration, ClientSideElementDependenciesManager dependenciesManager, Map<String, Long> importValidity, Map<EXCLUDETYPE, List<String>> excludedList, boolean isImport) throws ConfigurationException
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
            
            _ribbonConfig.addDependency(extensionPoint, extensionId);
        }
            
        Configuration[] tabsConfigurations = configuration.getChild("tabs").getChildren();
        Integer defaultOrder = isImport ? Integer.MAX_VALUE : 0;
        for (Configuration tabConfiguration : tabsConfigurations)
        {
            if ("tab".equals(tabConfiguration.getName()))
            {
                Tab tab = new Tab(tabConfiguration, _ribbonManager, defaultOrder, _logger);
                
                if (!_isTabExcluded(tab, excludedList, isImport))
                {
                    _ribbonConfig.getTabs().add(tab);
                }
                
                // Only the first tab of the file has a default order
                defaultOrder = null;
            }
            else if ("import".equals(tabConfiguration.getName()))
            {
                String url = tabConfiguration.getValue();
                _configureImport(dependenciesManager, importValidity, url, excludedList);
            }
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug("Ending reading ribbon configuration");
        }
    }

    private void _configureAutomaticImports(ClientSideElementDependenciesManager dependenciesManager, String workspaceName, Map<EXCLUDETYPE, List<String>> excludedList, Map<String, Long> importsValidity) throws ConfigurationException
    {
        for (String extensionId : _ribbonImportManager.getExtensionsIds())
        {
            if (!excludedList.get(EXCLUDETYPE.EXTENSION).contains(extensionId))
            {
                RibbonImport ribbonImportExtension = _ribbonImportManager.getExtension(extensionId);
                if (ribbonImportExtension != null)
                {
                    for (Entry<List<String>, Pattern> importFiles : ribbonImportExtension.getImports().entrySet())
                    {
                        if (importFiles.getValue().matcher(workspaceName).matches())
                        {
                            for (String importUri : importFiles.getKey())
                            {
                                _configureImport(dependenciesManager, importsValidity, importUri, excludedList);                                
                            }
                        }
                    }
                }
            }
        }
    }

    private void _configureImport(ClientSideElementDependenciesManager dependenciesManager, Map<String, Long> importValidity, String url, Map<EXCLUDETYPE, List<String>> excludedList) throws ConfigurationException
    {
        if (_ignoreImport(importValidity, excludedList, url))
        {
            return;
        }
        
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
                importValidity.put(url, src.getLastModified());
                
                try (InputStream is = src.getInputStream())
                {
                    Configuration importedConfiguration = new DefaultConfigurationBuilder().build(is);
                    _configure(importedConfiguration, dependenciesManager, importValidity, excludedList, true);
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

    private boolean _ignoreImport(Map<String, Long> importValidity, Map<EXCLUDETYPE, List<String>> excludedList, String url)
    {
        if (importValidity.containsKey(url))
        {
            // already imported
            return true;
        }
        
        Matcher matcher = _PLUGINNAMEPATTERN.matcher(url);
        if (matcher.matches())
        {
            String pluginName = matcher.group(1);
            if (excludedList.get(EXCLUDETYPE.PLUGIN).contains(pluginName))
            {
                return true;
            }
        }
        
        return excludedList.get(EXCLUDETYPE.FILE).contains(url);
    }

    private boolean _isTabExcluded(Tab tab, Map<EXCLUDETYPE, List<String>> excludedList, boolean isImport)
    {
        if (!isImport)
        {
            return false;
        }
        
        String tabLabel = tab.getLabel();
        if (excludedList.get(EXCLUDETYPE.TABLABEL).contains(tabLabel))
        {
            return true;
        }
        
        List<Group> emptyGroups = new ArrayList<>();
        for (Group group : tab.getGroups())
        {
            GroupSize largeGroupSize = group.getLargeGroupSize();
            int elementsCount = 0;
            if (largeGroupSize != null)
            {
                _removeExcludedControls(largeGroupSize.getChildren(), excludedList);
                elementsCount += largeGroupSize.getChildren().size();
            }
            GroupSize mediumGroupSize = group.getMediumGroupSize();
            if (mediumGroupSize != null)
            {
                _removeExcludedControls(mediumGroupSize.getChildren(), excludedList);
                elementsCount += mediumGroupSize.getChildren().size();
            }
            GroupSize smallGroupSize = group.getSmallGroupSize();
            if (smallGroupSize != null)
            {
                _removeExcludedControls(smallGroupSize.getChildren(), excludedList);
                elementsCount += smallGroupSize.getChildren().size();
            }
            
            if (elementsCount == 0)
            {
                emptyGroups.add(group);
            }
        }
        
        tab.getGroups().removeAll(emptyGroups);
        
        return tab.getGroups().size() == 0;
    }
    
    private void _removeExcludedControls(List<Element> elements, Map<EXCLUDETYPE, List<String>> excludedList)
    {
        List<Element> elementsToRemove = new ArrayList<>();
        
        for (Element element : elements)
        {
            if (element instanceof ControlRef)
            {
                ControlRef control = (ControlRef) element;
                if (excludedList.get(EXCLUDETYPE.CONTROL).contains(control.getId()))
                {
                    elementsToRemove.add(element);
                }
            }
            else
            {
                _removeExcludedControls(element.getChildren(), excludedList);
                if (element.getChildren().size() == 0)
                {
                    elementsToRemove.add(element);
                }
            }
        }
        
        elements.removeAll(elementsToRemove);
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
    
    private void _configureTabOverride(Map<String, Tab> labelMapping)
    {
        List<Tab> tabsToRemove = new ArrayList<>();
        for (Tab tab : _ribbonConfig.getTabs())
        {
            if (tab.isOverride())
            {
                if (labelMapping.containsKey(tab.getLabel()))
                {
                    labelMapping.get(tab.getLabel()).injectGroups(tab.getGroups());
                }
                tabsToRemove.add(tab);
            }
        }
        
        _ribbonConfig.getTabs().removeAll(tabsToRemove);
    }
    
    private void _configureTabOrder(Map<String, Tab> labelMapping)
    {
        LinkedList<Tab> tabs = _ribbonConfig.getTabs();
        
        // Sort by non-contextual first 
        Collections.sort(tabs, (tab1, tab2) -> tab1.isContextual() != tab2.isContextual() ? (tab1.isContextual() ? 1 : -1) : 0);
        
        // Move tabs whose order reference another tab
        List<Tab> tabsToMove = tabs.stream().filter(tab -> tab.getOrderAsString() != null).collect(Collectors.toList());
        for (Tab tab : tabsToMove)
        {
            String order = tab.getOrderAsString();
            Tab referencedTab = order != null ? labelMapping.get(order) : null;
            if (order != null && referencedTab != null && referencedTab != tab && referencedTab.isContextual() == tab.isContextual())
            {
                tabs.remove(tab);
                int index = tabs.indexOf(referencedTab);
                tabs.add(tab.orderBefore() ? index : index + 1, tab);
                tab.setOrder(null);
            }
            else
            {
                _logger.warn("Invalid tab attribute order with value '" + order + "' for tab '" + tab.getId() + "'. Default tab order will be used instead");
            }
        }
        
        // Set order value for all then sort
        Object previousOrder = null;
        for (Tab tab : tabs)
        {
            Integer tabOrder = tab.getOrderAsInteger();
            if (tabOrder == null)
            {
                tab.setOrder(previousOrder);
            }
            else
            {
                previousOrder = tabOrder;
            }
        }
        Collections.sort(tabs, (tab1, tab2) -> tab1.isContextual() == tab2.isContextual() ? tab1.getOrderAsInteger() - tab2.getOrderAsInteger() : 0);
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
        
        // check that all referred items does exist
        for (Tab tab : _ribbonConfig.getTabs())
        {
            // Check this is an existing factory
            if (tab.getId() != null)
            {
                ClientSideElement tabElement = _ribbonTabManager.getExtension(tab.getId());
                if (tabElement == null)
                {
                    String errorMessage = "A tab item referes an unexisting item factory with id '" + tab.getId() + "'";
                    _logger.error(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                else
                {
                    this._tabsReferences.add(tab.getId());
                }
            }
            
            // initialize groups
            for (Group group : tab.getGroups())
            {
                _lazyInitialize(group.getLargeGroupSize());
                _lazyInitialize(group.getMediumGroupSize());
                _lazyInitialize(group.getSmallGroupSize());
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
            _lazyInitialize(groupSize.getChildren());
    
            for (Element element : groupSize.getChildren())
            {
                if (element instanceof Layout)
                {
                    Layout layout = (Layout) element;
    
                    _lazyInitialize(layout.getChildren());
    
                    for (Element layoutElement : layout.getChildren())
                    {
                        if (element instanceof Toolbar)
                        {
                            Toolbar toolbar = (Toolbar) layoutElement;
    
                            _lazyInitialize(toolbar.getChildren());
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
                ClientSideElement ribbonControl = _ribbonControlManager.getExtension(control.getId());
                if (ribbonControl == null)
                {
                    String errorMessage = "An item referes an unexisting item factory with id '" + control.getId() + "'";
                    _logger.error(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                else
                {
                    this._controlsReferences.add(control.getId());
                }
            }
            else if (element instanceof Toolbar)
            {
                Toolbar toolbar = (Toolbar) element;
                
                _lazyInitialize(toolbar.getChildren());
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
                ClientSideElement extension = _ribbonControlManager.getExtension(controlRef.getId());
                for (Script script : extension.getScripts(contextualParameters))
                {
                    resolvedElements.add(new ControlRef(script.getId(), controlRef.getColspan(), _logger));
                }
            }
            
            if (element instanceof Layout)
            {
                List<Element> layoutElements = _resolveReferences(contextualParameters, element.getChildren());
                if (layoutElements.size() > 0)
                {
                    Layout layout = (Layout) element;
                    Layout resolvedLayout = new Layout(layout, layout.getSize());
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
                    Toolbar resolvedToolbar = new Toolbar(_logger, toolbar.getColspan());
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
}
