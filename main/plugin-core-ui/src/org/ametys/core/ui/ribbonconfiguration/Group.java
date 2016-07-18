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

import java.util.Collection;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.RibbonManager;
import org.ametys.runtime.i18n.I18nizableText;

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
    
    /** True to override an existing group instead of defining a new one */
    protected Boolean _override;
    /** When overriding, defines the group position inside the tab */
    protected String _order;
    /** When overriding, defines the controls position in the original group */
    protected String _controlsOrder;
    /** Helper for controls injection */
    protected RibbonElementsInjectionHelper<Element> _largeOverrideHelper;
    /** Helper for controls injection */
    protected RibbonElementsInjectionHelper<Element> _mediumOverrideHelper;
    /** Helper for controls injection */
    protected RibbonElementsInjectionHelper<Element> _smallOverrideHelper;
    
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
        _configureOverride(groupConfiguration);
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
        _override = ribbonGroup._override;
        _order = ribbonGroup._order;
        _controlsOrder = ribbonGroup._controlsOrder;
        
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
    
    private void _configureOverride(Configuration groupConfiguration)
    {
        _override = groupConfiguration.getAttributeAsBoolean("override", false);
        _order = groupConfiguration.getAttribute("order", "0");
        _controlsOrder = groupConfiguration.getAttribute("controlsOrder", "0");
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
     * Return true if the group overrides another group
     * @return True if overrides
     */
    public boolean isOverride()
    {
        return _override;
    }
    
    /**
     * Return the order of the group
     * @return The order
     */
    public String getOrder()
    {
        return _order;
    }
    
    /**
     * Inject a group's controls into this group
     * @param group The group to inject
     */
    public void injectGroup(Group group)
    {
        if (_largeSize != null)
        {
            if (_largeOverrideHelper == null)
            {
                _largeOverrideHelper = new RibbonElementsInjectionHelper<>(_largeSize.getChildren(), _groupLogger);
            }
            injectGroup(group, _largeOverrideHelper, group.getLargeGroupSize(), group.getMediumGroupSize(), null);
        }
        if (_mediumSize != null)
        {
            if (_mediumOverrideHelper == null)
            {
                _mediumOverrideHelper = new RibbonElementsInjectionHelper<>(_mediumSize.getChildren(), _groupLogger);
            }
            injectGroup(group, _mediumOverrideHelper, group.getMediumGroupSize(), group.getLargeGroupSize(), null);
        }
        if (_smallSize != null)
        {
            if (_smallOverrideHelper == null)
            {
                _smallOverrideHelper = new RibbonElementsInjectionHelper<>(_smallSize.getChildren(), _groupLogger);
            }
            injectGroup(group, _smallOverrideHelper, group.getSmallGroupSize(), group.getMediumGroupSize(), group.getLargeGroupSize());
        }
    }
    
    private void injectGroup(Group group, RibbonElementsInjectionHelper<Element> overrideHelper, GroupSize fromSizeFirst, GroupSize fromSizeSecond, GroupSize fromSizeThird)
    {
        GroupSize fromSize = null;
        if (fromSizeFirst != null)
        {
            fromSize = fromSizeFirst;
        }
        else if (fromSizeSecond != null)
        {
            fromSize = fromSizeSecond;
        }
        else
        {
            fromSize = fromSizeThird;
        }
        
        if (fromSize != null)
        {
            for (Element element : fromSize.getChildren())
            {
                overrideHelper.injectElements(element, group._controlsOrder);
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

    @Override
    public String toString()
    {
        return super.toString() + "[" + _label + "]";
    }
    
}    
