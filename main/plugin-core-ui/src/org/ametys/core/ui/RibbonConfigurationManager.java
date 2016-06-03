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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
        top("top"),
        /** The controls are middly aligned. Can be used with 2 controls only. */ 
        middle("middle");
        
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

    /** The ribbon control extension point */
    protected RibbonControlsManager _ribbonControlManager;
    /** The ribbon tab extension point */
    protected RibbonTabsManager _ribbonTabManager;
    /** The sax clientside element helper */
    protected SAXClientSideElementHelper _saxClientSideElementHelper;
    /** The excalibur source resolver */
    protected SourceResolver _resolver;

    /** The tabs of the ribbon */
    protected List<Tab> _tabs = new ArrayList<>();

    /** The App menu elements of the ribbon */
    protected List<Element> _appMenu = new ArrayList<>();
    
    /** The user menu elements of the ribbon */
    protected List<Element> _userMenu = new ArrayList<>();
    
    /** The controls referenced by the ribbon */
    protected Set<String> _controlsReferences = new HashSet<>();
    /** The tabs referenced by the ribbon */
    protected Set<String> _tabsReferences = new HashSet<>();

    private boolean _initialized;
    
    /**
     * Constructor
     * @param ribbonControlManager the ribbon control manager
     * @param ribbonTabManager the ribbon tab manager
     * @param saxClientSideElementHelper the helper to SAX client side element
     * @param resolver the excalibur source resolver
     * @param dependenciesManager The dependencies manager
     * @param config the ribbon configuration
     * @throws RuntimeException if an error occurred
     */
    public RibbonConfigurationManager (RibbonControlsManager ribbonControlManager, RibbonTabsManager ribbonTabManager, SAXClientSideElementHelper saxClientSideElementHelper, SourceResolver resolver, ClientSideElementDependenciesManager dependenciesManager, InputStream config)
    {
        _ribbonControlManager = ribbonControlManager;
        _ribbonTabManager = ribbonTabManager;
        _saxClientSideElementHelper = saxClientSideElementHelper;
        _resolver = resolver;
        
        try
        {
            Configuration configuration = new DefaultConfigurationBuilder().build(config);
            _configure(configuration, dependenciesManager);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to read the configuration file", e);
        }
    }

    private void _configure(Configuration configuration, ClientSideElementDependenciesManager dependenciesManager) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Starting reading ribbon configuration");
        }
        
        Configuration[] appMenuConfigurations = configuration.getChild("app-menu").getChildren();
        this._appMenu.addAll(_configureElement(appMenuConfigurations));

        Configuration[] userMenuConfigurations = configuration.getChild("user-menu").getChildren();
        this._userMenu.addAll(_configureElement(userMenuConfigurations));
        
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
                Tab tab = new Tab(tabConfiguration, _logger);
                _tabs.add(tab);
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
                        try (InputStream is = src.getInputStream())
                        {
                            Configuration importedConfiguration = new DefaultConfigurationBuilder().build(is);
                            _configure(importedConfiguration, dependenciesManager);
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
    
    private List<Element> _configureElement(Configuration[] configurations) throws ConfigurationException
    {
        List<Element> elements = new ArrayList<>();
        for (Configuration configuration : configurations)
        {
            if ("control".equals(configuration.getName()))
            {
                elements.add(new ControlRef(configuration, _logger));
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
     * @param contextualParameters Contextual parameters
     * @throws IllegalStateException if an item does not exist
     */
    private synchronized void _lazyInitialize(Map<String, Object> contextualParameters) 
    {
        if (_initialized)
        {
            return;
        }
        
        // check that all refered items does exist
        for (Tab tab : _tabs)
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
                _lazyInitialize(group._largeSize, contextualParameters);
                _lazyInitialize(group._mediumSize, contextualParameters);
                _lazyInitialize(group._smallSize, contextualParameters);
            }
        }

        _lazyInitialize(this._appMenu, contextualParameters);
        _lazyInitialize(this._userMenu, contextualParameters);
        
        _initialized = true;
    }

    private void _lazyInitialize(GroupSize groupSize, Map<String, Object> contextualParameters)
    {
        _lazyInitialize(groupSize._elements, contextualParameters);

        for (Element element : groupSize._elements)
        {
            if (element instanceof Layout)
            {
                Layout layout = (Layout) element;

                _lazyInitialize(layout._elements, contextualParameters);

                for (Element layoutElement : layout._elements)
                {
                    if (element instanceof Toolbar)
                    {
                        Toolbar toolbar = (Toolbar) layoutElement;

                        _lazyInitialize(toolbar._elements, contextualParameters);
                    }
                }                        
            }
        }
    }
    
    private void _lazyInitialize(List<Element> elements, Map<String, Object> contextualParameters)
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

                    // Resolve the list of scripts associated with this control
                    List<String> scriptIds = new ArrayList<>();
                    for (Script script : ribbonControl.getScripts(contextualParameters))
                    {
                        scriptIds.add(script.getId());
                    }
                    control.setScriptIds(scriptIds);
                }
            }
            else if (element instanceof Toolbar)
            {
                Toolbar toolbar = (Toolbar) element;
                
                _lazyInitialize(toolbar._elements, contextualParameters);
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
        _lazyInitialize(contextualParameters);
        
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
        for (Element appMenu : this._appMenu)
        {
            appMenu.toSAX(handler);
        }
        XMLUtils.endElement(handler, "app-menu");
        
        XMLUtils.startElement(handler, "user-menu");
        for (Element userMenu : this._userMenu)
        {
            userMenu.toSAX(handler);
        }
        XMLUtils.endElement(handler, "user-menu");

        XMLUtils.startElement(handler, "tabs");
        for (Tab tab : this._tabs)
        {
            tab.toSAX(handler);
        }
        XMLUtils.endElement(handler, "tabs");

        XMLUtils.endElement(handler, "ribbon");
        handler.endPrefixMapping("i18n");
    }
    
    private void _saxReferencedControl (MenuClientSideElement menu, ContentHandler handler, Map<String, Object> contextualParameters) throws SAXException
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
     * A tab of the ribbon
     */
    public class Tab
    {
        /** The label of the tab */
        protected I18nizableText _label;
        
        /** The optionnal id of the contextual client side element determining the state of the ribbon */
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
         * @param logger The logger
         * @throws ConfigurationException if an error occures in the configuration
         */
        public Tab(Configuration tabConfiguration, Logger logger) throws ConfigurationException
        {
            _log = logger;
            
            if (_log.isDebugEnabled())
            {
                _log.debug("Creating tab");
            }

            _configureId(tabConfiguration);
            _configureLabel(tabConfiguration);
            _configureGroups(tabConfiguration);
        }
        
        /**
         * Configure tab optional id
         * @param tabConfiguration One tab configuration
         * @throws ConfigurationException if an error occured
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
            
            if (_log.isDebugEnabled())
            {
                _log.debug("Tab id is " + this._id);
            }
        }
        
        /**
         * Configure one tab label
         * @param tabConfiguration One tab configuration
         * @throws ConfigurationException if an error occured
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
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureGroups(Configuration tabConfiguration) throws ConfigurationException
        {
            Configuration[] groupsConfigurations = tabConfiguration.getChild("groups").getChildren("group");
            for (Configuration groupConfiguration : groupsConfigurations)
            {
                Group group = new Group(groupConfiguration, _log);
                _groups.add(group);
            }
        }
        
        /**
         * Sax the the configuration of the tab.
         * @param handler The content handler where to sax
         * @throws SAXException if an error occurs
         */
        public void toSAX(ContentHandler handler) throws SAXException
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
            for (Group group : _groups)
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
         * @param logger The logger
         * @throws ConfigurationException if an error occures in the configuration
         */
        public Group(Configuration groupConfiguration, Logger logger) throws ConfigurationException
        {
            _groupLogger = logger;
            _dialogBoxLauncher = groupConfiguration.getAttribute("dialog-box-launcher", "");
            _priority = groupConfiguration.getAttributeAsInteger("priority", 0);
            _configureLabelAndIcon(groupConfiguration);
            _configureSize(groupConfiguration);
        }
        
        /**
         * Configure one group label
         * @param groupConfiguration One group configuration
         * @throws ConfigurationException if an error occured
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
         * Configure the differents size of the group
         * @param groupConfiguration One group configuration
         * @throws ConfigurationException if an error occurred
         */
        protected void _configureSize(Configuration groupConfiguration) throws ConfigurationException
        {
            _largeSize = new GroupSize(groupConfiguration.getChild("large"), _groupLogger);
            _mediumSize = new GroupSize(groupConfiguration.getChild("medium"), _groupLogger);
            _smallSize = new GroupSize(groupConfiguration.getChild("small"), _groupLogger);
            
            _checkSizeConsistency(groupConfiguration);
        }
        
        private void _checkSizeConsistency(Configuration groupConfiguration) throws ConfigurationException
        {
            Set<String> largeControlIds = _largeSize.getControlIds();
            Set<String> mediumControlIds = _mediumSize.getControlIds();
            Set<String> smallControlIds = _smallSize.getControlIds();
            
            if (smallControlIds.size() > 0)
            {
                Collection disjunction = CollectionUtils.disjunction(smallControlIds, mediumControlIds);
                if (disjunction.size() > 0)
                {
                    String disjunctionAdString = StringUtils.join(disjunction, ", ");
                    throw new ConfigurationException("The small configuration of the group does not have the same elements as the medium one (" + disjunctionAdString + ")", groupConfiguration);
                }
            }
            if (largeControlIds.size() > 0)
            {
                Collection disjunction = CollectionUtils.disjunction(largeControlIds, mediumControlIds);
                if (disjunction.size() > 0)
                {
                    String disjunctionAdString = StringUtils.join(disjunction, ", ");
                    throw new ConfigurationException("The large configuration of the group does not have the same elements as the medium one (" + disjunctionAdString + ")", groupConfiguration);
                }
            }
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
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public GroupSize(Configuration groupSizeConfiguration, Logger logger) throws ConfigurationException
        {
            this._groupSizeLogger = logger;
            
            Configuration[] elementsConfigurations = groupSizeConfiguration.getChildren();
            for (Configuration elementConfiguration : elementsConfigurations)
            {
                if ("control".equals(elementConfiguration.getName()))
                {
                    ControlRef control = new ControlRef(elementConfiguration, _groupSizeLogger);
                    _elements.add(control);
                }
                else if ("layout".equals(elementConfiguration.getName()))
                {
                    Layout layout = new Layout(elementConfiguration, _groupSizeLogger);
                    _elements.add(layout);
                }
                else if (_groupSizeLogger.isWarnEnabled())
                {
                    _groupSizeLogger.warn("During configuration of the ribbon, the group use an unknow tag '" + elementConfiguration.getName() + "'");
                }
            }
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
    }   
    
    /**
     * An element in the ribbon
     */
    public interface Element
    {
        /**
         * Sax the the configuration of the element.
         * @param handler The content handler where to sax
         * @throws SAXException if an error occurs
         */
        public void toSAX(ContentHandler handler) throws SAXException;
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
        
        /** List of script ids of this control */
        protected List<String> _scriptIds;
        
        /**
         * Creates a control reference
         * @param controlConfiguration The configuration for the control
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public ControlRef(Configuration controlConfiguration, Logger logger) throws ConfigurationException
        {
            _scriptIds = null;
            this._controlLogger = logger;
            
            this._id = controlConfiguration.getAttribute("id");
            if (_controlLogger.isDebugEnabled())
            {
                _controlLogger.debug("Control id is " + this._id);
            }
            
            this._colspan = controlConfiguration.getAttributeAsInteger("colspan", 1);
            if (_controlLogger.isDebugEnabled())
            {
                _controlLogger.debug("Control colspan is " + this._colspan);
            }
        }
        
        /**
         * Set the list of script ids associated with this control reference.
         * @param scriptIds The list of script ids.
         */
        public void setScriptIds(List<String> scriptIds)
        {
            _scriptIds = scriptIds;
        }
        
        @Override
        public void toSAX(ContentHandler handler) throws SAXException
        {
            if (_scriptIds != null)
            {
                for (String id :_scriptIds)
                {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addCDATAAttribute("id", id);
                    attrs.addCDATAAttribute("colspan", Integer.toString(_colspan));
                    XMLUtils.createElement(handler, "control", attrs);
                }
            }
            else
            {
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("id", _id);
                attrs.addCDATAAttribute("colspan", Integer.toString(_colspan));
                XMLUtils.createElement(handler, "control", attrs);
            }
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
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public Layout(Configuration layoutConfiguration, Logger logger) throws ConfigurationException
        {
            this._layoutLogger = logger;
            
            this._cols = layoutConfiguration.getAttributeAsInteger("cols", 1);
            if (_layoutLogger.isDebugEnabled())
            {
                _layoutLogger.debug("Control colspan is " + this._cols);
            }
            
            String align = layoutConfiguration.getAttribute("align", null);
            this._layoutAlign = LAYOUTALIGN.createsFromString(align); 
            if (_layoutLogger.isDebugEnabled())
            {
                _layoutLogger.debug("Control align is " + this._layoutAlign);
            }
            
            String definedSize = layoutConfiguration.getAttribute("size", null);
            _size = CONTROLSIZE.createsFromString(definedSize);
            if (_layoutLogger.isDebugEnabled())
            {
                _layoutLogger.debug("Control size is " + this._size);
            }

            Configuration[] elementsConfigurations = layoutConfiguration.getChildren();
            for (Configuration elementConfiguration : elementsConfigurations)
            {
                if ("control".equals(elementConfiguration.getName()))
                {
                    ControlRef control = new ControlRef(elementConfiguration, _layoutLogger);
                    _elements.add(control);
                }
                else if ("toolbar".equals(elementConfiguration.getName()))
                {
                    Toolbar toolbar = new Toolbar(elementConfiguration, _layoutLogger);
                    _elements.add(toolbar);
                }
                else if (_layoutLogger.isWarnEnabled())
                {
                    _layoutLogger.warn("During configuration of the ribbon, the layout use an unknow tag '" + elementConfiguration.getName() + "'");
                }
            }
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
         * @param logger The logger
         * @throws ConfigurationException if an error occurred
         */
        public Toolbar(Configuration toolbarConfiguration, Logger logger) throws ConfigurationException
        {
            this._toolbarLogger = logger;
            
            this._colspan = toolbarConfiguration.getAttributeAsInteger("colspan", 1);
            if (_toolbarLogger.isDebugEnabled())
            {
                _toolbarLogger.debug("Control colspan is " + this._colspan);
            }
            
            Configuration[] elementsConfigurations = toolbarConfiguration.getChildren();
            for (Configuration elementConfiguration : elementsConfigurations)
            {
                if ("control".equals(elementConfiguration.getName()))
                {
                    ControlRef control = new ControlRef(elementConfiguration, _toolbarLogger);
                    _elements.add(control);
                }
                else if (_toolbarLogger.isWarnEnabled())
                {
                    _toolbarLogger.warn("During configuration of the ribbon, the toolbar use an unknow tag '" + elementConfiguration.getName() + "'");
                }
            }
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
    }
    
    /**
     * A menu separator
     */
    public class Separator implements Element
    {
        @Override
        public void toSAX(ContentHandler handler) throws SAXException
        {
            XMLUtils.createElement(handler, "separator");
        }
    }
}
