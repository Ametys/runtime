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
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
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
import org.ametys.core.ui.ribbonconfiguration.RibbonExclude;
import org.ametys.core.ui.ribbonconfiguration.RibbonExclude.EXCLUDETARGET;
import org.ametys.core.ui.ribbonconfiguration.RibbonExclude.EXCLUDETYPE;
import org.ametys.core.ui.ribbonconfiguration.RibbonMenu;
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
            Configuration configuration;
            if (config.exists())
            {
                configuration = new DefaultConfigurationBuilder().build(config.getInputStream());
            }
            else
            {
                configuration = new DefaultConfiguration("ribbon");
            }
            
            Map<String, Long> importsValidity = new HashMap<>();
            importsValidity.put(config.getURI(), config.getLastModified());
            
            Map<String, Map<String, Object>> imports = new HashMap<>();
            Map<EXCLUDETYPE, List<RibbonExclude>> excluded = new HashMap<>();
            for (EXCLUDETYPE excludetype : EXCLUDETYPE.values())
            {
                excluded.put(excludetype, new ArrayList<RibbonExclude>());
            }
            _configureExcluded(configuration, imports, excluded, workspaceName);
            
            _configureRibbon(configuration, dependenciesManager, imports, importsValidity, excluded, null);
            
            for (Entry<String, Map<String, Object>> entry : imports.entrySet())
            {
                if ("automatic".equals(entry.getValue().get("type")))
                {
                    _configureImport(dependenciesManager, importsValidity, entry.getKey(), imports, excluded);
                }
            }
            
            Map<String, Tab> tabsLabelMapping = _ribbonConfig.getTabs().stream().filter(tab -> !tab.isOverride()).collect(Collectors.toMap(Tab::getLabel, Function.identity(), (tab1, tab2) -> tab1));
            _configureTabOverride(tabsLabelMapping);
            
            List<String> excludedControls = excluded.entrySet().stream().filter(entry -> EXCLUDETYPE.CONTROL.equals(entry.getKey())).flatMap(entry -> entry.getValue().stream()).filter(exclude -> EXCLUDETARGET.ID.equals(exclude.getTarget())).map(RibbonExclude::getValue).collect(Collectors.toList());
            for (Tab tab : _ribbonConfig.getTabs())
            {
                _removeExcludedControls(tab, excludedControls);   
            }
            
            _configureTabOrder(tabsLabelMapping);
            
            ribbonManagerCache.addCachedConfiguration(_ribbonManager, _ribbonConfig, importsValidity);
            _ribbonManager.initializeExtensions();
        }
    }
    
    private void _configureExcluded(Configuration configuration, Map<String, Map<String, Object>> imports, Map<EXCLUDETYPE, List<RibbonExclude>> excluded, String workspaceName) throws ConfigurationException
    {
        _configureExcludeFromImports(configuration, imports, excluded);
        
        // Automatic imports
        for (String extensionId : _ribbonImportManager.getExtensionsIds())
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
                            _configureExcludeFromImports(importUri, imports, excluded);
                            Map<String, Object> properties = imports.get(importUri);
                            if (properties != null)
                            {
                                properties.put("extension", extensionId);
                                properties.put("type", "automatic");
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void _configureExcludeFromImports(String url, Map<String, Map<String, Object>> imports, Map<EXCLUDETYPE, List<RibbonExclude>> excluded) throws ConfigurationException
    {
        if (!imports.containsKey(url))
        {
            Source src = null;
            try
            {
                src = _resolver.resolveURI(url);
                if (!src.exists())
                {
                    throw new SourceNotFoundException(url + " does not exists");
                }
                else
                {
                    try (InputStream is = src.getInputStream())
                    {
                        if (_logger.isDebugEnabled())
                        {
                            _logger.debug("RibbonConfigurationManager : new file imported '" + url + "'");
                        }
                        
                        Configuration importedConfiguration = new DefaultConfigurationBuilder().build(is);
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("configuration", importedConfiguration);
                        properties.put("lastModified", src.getLastModified());
                        imports.put(url, properties);
                        _configureExcludeFromImports(importedConfiguration, imports, excluded);
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
    
    private void _configureExcludeFromImports(Configuration configuration, Map<String, Map<String, Object>> imports, Map<EXCLUDETYPE, List<RibbonExclude>> excluded) throws ConfigurationException
    {
        for (Configuration excludeConf : configuration.getChild("exclude").getChildren())
        {
            RibbonExclude ribbonExclude = new RibbonExclude(excludeConf, _logger);
            excluded.get(ribbonExclude.getType()).add(ribbonExclude);
        }
        
        for (Configuration importConfig : configuration.getChild("tabs").getChildren("import"))
        {
            String url = importConfig.getValue();
            
            _configureExcludeFromImports(url, imports, excluded);
        }
    }
    
    private void _configureRibbon(Configuration configuration, ClientSideElementDependenciesManager dependenciesManager, Map<String, Map<String, Object>> imports, Map<String, Long> importValidity, Map<EXCLUDETYPE, List<RibbonExclude>> excludedList, String url) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Starting reading ribbon configuration");
        }
        
        for (Configuration appMenuConfig : configuration.getChildren("app-menu"))
        {
            _configureRibbonMenu(appMenuConfig, _ribbonConfig.getAppMenu(), excludedList.get(EXCLUDETYPE.APPMENU), url != null ? imports.get(url) : null, url);
        }
        for (Configuration userMenuConfig : configuration.getChildren("user-menu"))
        {
            _configureRibbonMenu(userMenuConfig, _ribbonConfig.getUserMenu(), excludedList.get(EXCLUDETYPE.USERMENU), url != null ? imports.get(url) : null, url);
        }
        
        Configuration[] dependenciesConfigurations = configuration.getChild("depends").getChildren();
        for (Configuration dependencyConfigurations : dependenciesConfigurations)
        {
            String extensionPoint = dependencyConfigurations.getName();
            String extensionId = dependencyConfigurations.getValue();
            
            _ribbonConfig.addDependency(extensionPoint, extensionId);
        }
        
        Configuration[] tabsConfigurations = configuration.getChild("tabs").getChildren();
        Integer defaultOrder = url != null ? Integer.MAX_VALUE : 0;
        for (Configuration tabConfiguration : tabsConfigurations)
        {
            if ("tab".equals(tabConfiguration.getName()))
            {
                Tab tab = new Tab(tabConfiguration, _ribbonManager, defaultOrder, _logger);
                
                if (url == null || !_isTabExcluded(tab, excludedList))
                {
                    _ribbonConfig.getTabs().add(tab);
                }
                
                // Only the first tab of the file has a default order
                defaultOrder = null;
            }
            else if ("import".equals(tabConfiguration.getName()))
            {
                String importUrl = tabConfiguration.getValue();
                _configureImport(dependenciesManager, importValidity, importUrl, imports, excludedList);
            }
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug("Ending reading ribbon configuration");
        }
    }

    private void _configureRibbonMenu(Configuration configuration, RibbonMenu ribbonMenu, List<RibbonExclude> excludedList, Map<String, Object> properties, String url) throws ConfigurationException
    {
        if (url != null && _isFileExcluded(properties, excludedList, url))
        {
            return;
        }
        
        List<String> excludedControls = excludedList.stream().filter(exclude -> EXCLUDETARGET.ID.equals(exclude.getTarget())).map(RibbonExclude::getValue).collect(Collectors.toList());
        
        List<Element> elements = new ArrayList<>();
        for (Configuration childConfig : configuration.getChildren())
        {
            if ("control".equals(childConfig.getName()))
            {
                if (!excludedControls.contains(childConfig.getAttribute("id", null)))
                {
                    elements.add(new ControlRef(childConfig, _ribbonManager, _logger));
                }
            }
            else if ("separator".equals(childConfig.getName()))
            {
                elements.add(new Separator());
            }
            else
            {
                _logger.warn("During configuration of the ribbon, the app-menu or user-menu use an unknow tag '" + configuration.getName() + "'");
            }
        }
        
        ribbonMenu.addElements(elements, configuration.getAttribute("order", "0.10"), _logger);
    }

    private void _configureImport(ClientSideElementDependenciesManager dependenciesManager, Map<String, Long> importValidity, String url, Map<String, Map<String, Object>> imports, Map<EXCLUDETYPE, List<RibbonExclude>> excludedList) throws ConfigurationException
    {
        if (!imports.containsKey(url))
        {
            // unknown import
            return;
        }
        
        if (importValidity.containsKey(url) || _isFileExcluded(imports.get(url), excludedList.get(EXCLUDETYPE.IMPORT), url))
        {
            return;
        }
        
        Map<String, Object> properties = imports.get(url);
        if (properties.containsKey("configuration"))
        {
            Configuration configuration = (Configuration) properties.get("configuration");
            importValidity.put(url, (long) properties.get("lastModified"));
            _configureRibbon(configuration, dependenciesManager, imports, importValidity, excludedList, url);
        }
    }

    private boolean _isFileExcluded(Map<String, Object> properties, List<RibbonExclude> excludedList, String url)
    {
        Matcher matcher = _PLUGINNAMEPATTERN.matcher(url);
        String pluginName = matcher.matches() ? matcher.group(1) : null;
        
        for (RibbonExclude ribbonExclude : excludedList)
        {
            if (EXCLUDETARGET.EXTENSION.equals(ribbonExclude.getTarget()) && properties.containsKey("extension") && ribbonExclude.getValue().equals(properties.get("extension")))
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("RibbonConfigurationManager : The import '" + url + "' was not resolved because its extension '" + properties.get("extension") + "' is excluded.");
                }

                return true;
            }
            else if (EXCLUDETARGET.PLUGIN.equals(ribbonExclude.getTarget()) && pluginName != null && ribbonExclude.getValue().equals(pluginName))
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("RibbonConfigurationManager : The import '" + url + "' was not resolved because its plugin '" + pluginName + "' is excluded.");
                }

                return true;
            }
            else if (EXCLUDETARGET.FILE.equals(ribbonExclude.getTarget()) && ribbonExclude.getValue().equals(url))
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("RibbonConfigurationManager : The import '" + url + "' was not resolved because the file url is excluded.");
                }

                return true;
            }
        }
        
        return false;
    }

    private boolean _isTabExcluded(Tab tab, Map<EXCLUDETYPE, List<RibbonExclude>> excludedList)
    {
        String tabLabel = tab.getLabel();
        for (RibbonExclude ribbonExclude : excludedList.get(EXCLUDETYPE.TAB))
        {
            if (EXCLUDETARGET.LABEL.equals(ribbonExclude.getTarget()) && ribbonExclude.getValue().equals(tabLabel))
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("RibbonConfigurationManager : The tab '" + tabLabel + "' was not added because it is excluded.");
                }
                
                return true;
            }
        }
        
        return tab.getGroups().size() == 0;
    }

    private void _removeExcludedControls(Tab tab, List<String> excludedList)
    {
        for (Group group : tab.getGroups())
        {
            GroupSize largeGroupSize = group.getLargeGroupSize();
            if (largeGroupSize != null)
            {
                _removeExcludedControls(largeGroupSize.getChildren(), excludedList);
            }
            GroupSize mediumGroupSize = group.getMediumGroupSize();
            if (mediumGroupSize != null)
            {
                _removeExcludedControls(mediumGroupSize.getChildren(), excludedList);
            }
            GroupSize smallGroupSize = group.getSmallGroupSize();
            if (smallGroupSize != null)
            {
                _removeExcludedControls(smallGroupSize.getChildren(), excludedList);
            }
        }
    }
    
    private void _removeExcludedControls(List<Element> elements,  List<String> excludedList)
    {
        List<Element> elementsToRemove = new ArrayList<>();
        
        for (Element element : elements)
        {
            if (element instanceof ControlRef)
            {
                ControlRef control = (ControlRef) element;
                if (excludedList.contains(control.getId()))
                {
                    if (_logger.isDebugEnabled())
                    {
                        _logger.debug("RibbonConfigurationManager : The control '" + control.getId() + "' was not added because it is excluded.");
                    }

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

    private void _configureTabOverride(Map<String, Tab> labelMapping)
    {
        List<Tab> tabsOverride = _ribbonConfig.getTabs().stream().filter(Tab::isOverride).collect(Collectors.toList());
        for (Tab tab : tabsOverride)
        {
            if (labelMapping.containsKey(tab.getLabel()))
            {
                labelMapping.get(tab.getLabel()).injectGroups(tab.getGroups());
            }
        }
        
        for (Tab tab : tabsOverride)
        {
            if (labelMapping.containsKey(tab.getLabel()))
            {
                labelMapping.get(tab.getLabel()).injectGroupsOverride(tab.getGroups());
            }
        }
        
        _ribbonConfig.getTabs().removeAll(tabsOverride);
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

        _lazyInitialize(this._ribbonConfig.getAppMenu().getElements());
        _lazyInitialize(this._ribbonConfig.getUserMenu().getElements());
        
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
        List<Element> currentAppMenu = _resolveReferences(contextualParameters, this._ribbonConfig.getAppMenu().getElements());
        List<Element> currentUserMenu = _resolveReferences(contextualParameters, this._ribbonConfig.getUserMenu().getElements());
        
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
        
        _checkTabConsistency(tabGroups);
        
        return tabGroups;
    }

    private void _checkTabConsistency(Map<Tab, List<Group>> tabGroups)
    {
        for (Entry<Tab, List<Group>> entry : tabGroups.entrySet())
        {
            for (Group group : entry.getValue())
            {
                GroupSize large = group.getLargeGroupSize();
                if (large != null)
                {
                    _checkTabConsistency(large);
                }
                GroupSize medium = group.getMediumGroupSize();
                if (medium != null)
                {
                    _checkTabConsistency(medium);
                }
                GroupSize small = group.getSmallGroupSize();
                if (small != null)
                {
                    _checkTabConsistency(small);
                }
            }
        }
    }

    private void _checkTabConsistency(GroupSize group)
    {
        List<Layout> layoutsBuffer = new ArrayList<>();
        List<Element> originalChildren = group.getChildren();
        for (int i = 0; i < originalChildren.size(); i++)
        {
            Element originalChild = originalChildren.get(i);
            if (originalChild instanceof Layout & originalChild.getColumns() == 1)
            {
                Layout originalLayout = (Layout) originalChild;
                if (layoutsBuffer.size() > 0)
                {
                    CONTROLSIZE originalLayoutSize = originalLayout.getSize();
                    LAYOUTALIGN originalLayoutAlign = originalLayout.getAlign();
                    if ((originalLayoutSize != null ? !originalLayoutSize.equals(layoutsBuffer.get(0).getSize()) : layoutsBuffer.get(0).getSize() != null) 
                        || (originalLayoutAlign != null ? !originalLayoutAlign.equals(layoutsBuffer.get(0).getAlign()) : layoutsBuffer.get(0).getAlign() != null))                            
                    {
                        _checkLayoutsConsistency(layoutsBuffer, group);
                    }
                }
                
                layoutsBuffer.add(originalLayout);
            }
            else if (layoutsBuffer.size() > 0)
            {
                _checkLayoutsConsistency(layoutsBuffer, group);
            }
        }
        
        if (layoutsBuffer.size() > 0)
        {
            _checkLayoutsConsistency(layoutsBuffer, group);
        }
    }

    private void _checkLayoutsConsistency(List<Layout> layoutsBuffer, GroupSize group)
    {
        LAYOUTALIGN align = layoutsBuffer.get(0).getAlign();
        CONTROLSIZE size = layoutsBuffer.get(0).getSize();
        int elementsPerLayout = LAYOUTALIGN.MIDDLE.equals(align) ? 2 : 3;
        
        while (layoutsBuffer.size() > 0)
        {
            Layout layout = layoutsBuffer.remove(0);
            
            // check if the existing colspan values are correct. If incorrect, offset the controls with more colspan
            List<Element> elements = layout.getChildren();
            int currentSize = elements.stream().mapToInt(Element::getColumns).sum();
            
            Layout newLayout = _checkLayoutsOverflow(size, elementsPerLayout, layout, elements, currentSize);
            if (newLayout != null)
            {
                layoutsBuffer.add(0, newLayout);
                group.getChildren().add(group.getChildren().indexOf(layout) + 1, newLayout);
            }
            
            _checkLayoutsMerge(layoutsBuffer, elementsPerLayout, layout, elements, currentSize, group);
        }
        
    }

    private Layout _checkLayoutsOverflow(CONTROLSIZE size, int elementsPerLayout, Layout layout, List<Element> elements, int currentSize)
    {
        int layoutCols = layout.getColumns();
        if (currentSize > layoutCols * elementsPerLayout)
        {
            // There are too many elements in this layout, probably due to increasing the colspan of an element. Split this layout into multiple layouts
            Layout newLayout = new Layout(layout, size);
            int position = 0;
            for (Element element : elements)
            {
                position += element.getColumns();
                if (position > layoutCols * elementsPerLayout)
                {
                    newLayout.getChildren().add(element);
                }
            }
            elements.removeAll(newLayout.getChildren());
            
            return newLayout;
        }
        
        return null;
    }

    private void _checkLayoutsMerge(List<Layout> layoutsBuffer, int elementsPerLayout, Layout layout, List<Element> elements, int layoutSize, GroupSize group)
    {
        int layoutCols = layout.getColumns();
        int currentSize = layoutSize;
        boolean canFitMore = currentSize < layoutCols * elementsPerLayout;
        
        while (canFitMore && layoutsBuffer.size() > 0)
        {
            // There is room for more elements, merge with the next layout
            Layout nextLayout = layoutsBuffer.get(0);
            
            if (nextLayout.getColumns() > layoutCols)
            {
                // increase layout cols to fit next layout elements
                layout.setColumns(nextLayout.getColumns());
                layoutCols = nextLayout.getColumns();
            }
            
            List<Element> nextChildren = nextLayout.getChildren();
            while (canFitMore && nextChildren.size() > 0)
            {
                Element nextChild = nextChildren.get(0);
                
                int nextChildColumns = nextChild.getColumns();
                if (nextChildColumns > layoutCols)
                {
                    // next element does not fit layout, due to an error in the original ribbon file. Increase layout size to fix it
                    layout.setColumns(nextChildColumns);
                    layoutCols = nextChildColumns;
                }
                
                int columnsLeft = layoutCols - (currentSize % layoutCols);
                if (columnsLeft < nextChildColumns)
                {
                    // increase colspan of previous element to fill the current line, so the next child can start at a new line to have enough space
                    Element previousElement = elements.get(elements.size() - 1);
                    previousElement.setColumns(previousElement.getColumns() + columnsLeft);
                    currentSize += columnsLeft;
                }
                
                if (currentSize + nextChildColumns <= layoutCols * elementsPerLayout)
                {
                    nextChildren.remove(nextChild);
                    elements.add(nextChild);
                    currentSize += nextChildColumns;
                }
                else
                {
                    canFitMore = false;   
                }
            }
            
            if (nextChildren.size() == 0)
            {
                layoutsBuffer.remove(nextLayout);
                group.getChildren().remove(nextLayout);
            }
            
            if (currentSize == layoutCols * elementsPerLayout)
            {
                canFitMore = false;
            }
        }
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
                    resolvedElements.add(new ControlRef(script.getId(), controlRef.getColumns(), _logger));
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
                    Toolbar resolvedToolbar = new Toolbar(_logger, toolbar.getColumns());
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
