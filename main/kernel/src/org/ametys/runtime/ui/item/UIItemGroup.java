/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.ui.item;

import java.util.List;

import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.util.I18nizableText;


/**
 * Groups item 
 */
@Deprecated
public class UIItemGroup implements UIItem
{
    /** The label */
    protected I18nizableText _label;
    /** The description */
    protected I18nizableText _description;
    /** The icon set for display */
    protected IconSet _iconSet;
    /** The enabled state */
    protected boolean _enabled;
    /** The sub items */
    protected List<UIItem> _items;

    /**
     * Create an interaction
     * @param label The label of the interaction. Cannot be null.
     * @param description The description. Cannot be null.
     * @param iconSet The set of icon. Cannot be null.
     */
    public UIItemGroup(I18nizableText label, I18nizableText description, IconSet iconSet)
    {
        setLabel(label);
        setDescription(description);
        setIconSet(iconSet);
        setEnabled(false);
        setItems(null);
    }
    
    /**
     * The list of items of the group
     * @return The list. May be null.
     */
    public List<UIItem> getItems()
    {
        return _items;
    }
    
    /**
     * Set the list of items the group
     * @param items the list. Can be null.
     */
    public void setItems(List<UIItem> items)
    {
        _items = items;
    }
    
    /**
     * Determine if the interaction is enabled in the current environment
     * @return true if the interaction is not disabled.
     */
    public boolean isEnabled()
    {
        return true;
    }
    
    /**
     * Set the enabled state.
     * @param enabled The new state
     */
    public void setEnabled(boolean enabled)
    {
        _enabled = enabled;
    }
    
    /**
     * Get the main label of the interaction.
     * @return The label. Cannot be null.
     */
    public I18nizableText getLabel()
    {
        return _label;
    }
    
    /**
     * Set the label of the interaction
     * @param label The label of the interaction. Cannot be null.
     */
    public void setLabel(I18nizableText label)
    {
        if (label == null)
        {
            throw new NullPointerException("The label of an Interaction cannot be null");
        }
        else
        {
            _label = label;
        }
    }
    
    /**
     * Get the description of the interaction. It can be used for tooltip.
     * @return The description. Cannot be null.
     */
    public I18nizableText getDescription()
    {
        return _description;
    }
    
    /**
     * Set the description of the interaction
     * @param description The description. Cannot be null.
     */
    public void setDescription(I18nizableText description)
    {
        if (description == null)
        {
            throw new NullPointerException("The description of an Interaction cannot be null");
        }
        else
        {
            _description = description;
        }
    }

    /**
     * Get the icon set that represent the widget.
     * @return The icon set. Cannot be null.
     */
    public IconSet getIconSet()
    {
        return _iconSet;
    }
    
    /**
     * Set the iconset.
     * @param iconSet The set of icon. Cannot be null.
     */
    public void setIconSet(IconSet iconSet)
    {
        _iconSet = iconSet;
    }
}
