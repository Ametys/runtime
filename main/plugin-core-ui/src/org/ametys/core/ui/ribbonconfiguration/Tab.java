/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.ui.ribbonconfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.RibbonManager;
import org.ametys.core.ui.RibbonTabsManager;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * A tab of the ribbon
 */
public class Tab
{
    /** The label of the tab */
    protected I18nizableText _label;
    
    /** The optional id of the contextual client side element determining the state of the ribbon */
    protected String _controlId;
    
    /** The color (between 1 and 6) for a contextual tab */
    protected String _contextualColor;
    
    /** The id of the contextual group (can be null for single contextual tab) */
    protected String _contextualGroup;
    
    /** The label of the contextual group */
    protected I18nizableText _contextualLabel;
    
    /** The tab order */
    protected Object _order;
    
    /** True to order before a tab specified by _order */
    protected Boolean _orderBefore;
    
    /** True to override an existing tab instead of defining a new one */
    protected Boolean _override;
    
    /** The list of groups in the tab */
    protected List<Group> _groups = new ArrayList<>();
    
    /** helper for group injection */
    protected RibbonElementsInjectionHelper<Group> _tabOverrideHelper;
    
    /** Logger */
    protected Logger _log;
    
    /**
     * Creates a tab
     * @param tabConfiguration The configuration of the tab
     * @param ribbonManager The ribbon manager
     * @param defaultOrder The default tab order, if not specified. Can be null
     * @param logger The logger
     * @throws ConfigurationException if an error occurs in the configuration
     */
    public Tab(Configuration tabConfiguration, RibbonManager ribbonManager, Integer defaultOrder, Logger logger) throws ConfigurationException
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
        
        this._label = new I18nizableText("application", tabConfiguration.getAttribute("label"));
        if (_log.isDebugEnabled())
        {
            _log.debug("Tab label is " + this._label);
        }
        
        this._override = tabConfiguration.getAttributeAsBoolean("override", false);

        _configureGroups(tabConfiguration, ribbonManager);
        _configureOrder(tabConfiguration, defaultOrder);
    }
    
    /**
     * Get the id of this tab;
     * @return the id
     */
    public String getId()
    {
        return _controlId;
    }
    
    /**
     * Return true if the tab is contextual
     * @return true if the tab is contextual
     */
    public Boolean isContextual()
    {
        return _controlId != null;
    }

    /**
     * Configure tab optional id
     * @param tabConfiguration One tab configuration
     * @throws ConfigurationException if an error occurred
     */
    protected void _configureId(Configuration tabConfiguration) throws ConfigurationException
    {
        this._controlId = tabConfiguration.getAttribute("controlId", null);
        this._contextualColor = tabConfiguration.getAttribute("contextualColor", null);
        this._contextualGroup = tabConfiguration.getAttribute("contextualGroup", null);
        
        String contextualLabelString = tabConfiguration.getAttribute("contextualLabel", null);
        if (contextualLabelString != null)
        {
            this._contextualLabel = new I18nizableText("application", contextualLabelString);
        }
        
        if (_log.isDebugEnabled() && this._controlId != null)
        {
            _log.debug("Tab control id is " + this._controlId);
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
        if (this._controlId == null)
        {
            this._controlId = UUID.randomUUID().toString();
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
        
        ribbonManager.addExtension(this._controlId, "core-ui", null, defaultConfig);
        
        if (_log.isDebugEnabled())
        {
            _log.debug("Generated Tab control id is " + this._controlId);
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
    
    private void _configureOrder(Configuration tabConfiguration, Integer defaultOrder)
    {
        String order = tabConfiguration.getAttribute("order", null);
        try
        {
            _order = Integer.parseInt(order);
        }
        catch (NumberFormatException e)
        {
            _order = order != null ? order : defaultOrder;
        }
        
        _orderBefore = tabConfiguration.getAttributeAsBoolean("order-before", false);
    }
    
    /**
     * Get the tab label
     * @return Return the tab label
     */
    public String getLabel()
    {
        return _label.toString();
    }
    
    /**
     * Get the order attribute of the tab
     * @return Return the order as a String, or null
     */
    public String getOrderAsString()
    {
        if (_order instanceof String)
        {
            return (String) _order;
        }
        return null;
    }
    
    /**
     * Get the order attribute of the tab
     * @return Return the order as an Integer, or null
     */
    public Integer getOrderAsInteger()
    {
        if (_order instanceof Integer)
        {
            return (Integer) _order;
        }
        return null;
    }
    
    /**
     * Set the order attribute of the tab
     * @param order The new order value, either a String or an Integer
     */
    public void setOrder(Object order)
    {
        _order = order;
    }
    
    /**
     * True if the tab should be ordered before the tab referenced by the attribute order
     * @return True if the tab should be ordered before the tab referenced by the attribute order
     */
    public boolean orderBefore()
    {
        return _orderBefore;
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
     * Return true if this tab overrides an existing tab
     * @return True if overrides
     */
    public boolean isOverride()
    {
        return _override;
    }
    
    /**
     * Inject a list of groups into this tab
     * @param groups The list of groups to inject
     */
    public void injectGroups(List<Group> groups)
    {
        for (Group group : groups)
        {
            if (!group.isOverride())
            {
                if (_tabOverrideHelper == null)
                {
                    _tabOverrideHelper = new RibbonElementsInjectionHelper<>(_groups, _log);
                }
                
                if (_log.isDebugEnabled())
                {
                    _log.debug("RibbonConfigurationManager : new group '" + group._label.toString() + "' injected into tab '" + _label.toString() + "'");
                }

                _tabOverrideHelper.injectElements(group, group.getOrder());
            }
        }
    }
    
    /**
     * Inject a list of overriding groups into this tab
     * @param groups The list of groups to inject
     */
    public void injectGroupsOverride(List<Group> groups)
    {
        for (Group group : groups)
        {
            if (group.isOverride())
            {
                for (Group selfGroup : _groups)
                {
                    if (selfGroup._label.equals(group._label))
                    {
                        if (_log.isDebugEnabled())
                        {
                            _log.debug("RibbonConfigurationManager : overriding group '" + group._label + "' of tab '" + _label + "' to inject new controls");
                        }
                        
                        selfGroup.injectGroup(group);
                    }
                }
            }
        }
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
        
        if (_controlId != null)
        {
            attrs.addCDATAAttribute("controlId", _controlId);
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
    
    @Override
    public String toString()
    {
        return super.toString() + "[" + _label + "]";
    }
}
