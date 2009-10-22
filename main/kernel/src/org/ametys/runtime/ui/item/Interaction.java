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

import org.ametys.runtime.ui.item.part.Action;
import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.ui.item.part.Shortcut;
import org.ametys.runtime.util.I18nizableText;

/**
 * This interface represent any hmi interaction widget : icon, button, information, menu entry...
 */
@Deprecated
public class Interaction implements UIItem
{    
    /** The label */
    protected I18nizableText _label;
    /** The description */
    protected I18nizableText _description;
    /** The optional shortcut */
    protected Shortcut _shortcut;
    /** The icon set for display */
    protected IconSet _iconSet;
    /** The optional action */
    protected Action _action;
    /** The enabled status */
    protected boolean _enabled;
    
    /**
     * Create an interaction
     * @param label The label of the interaction
     * @param description The description
     * @param iconSet The set of icon
     */
    public Interaction(I18nizableText label, I18nizableText description, IconSet iconSet)
    {
        setLabel(label);
        setDescription(description);
        setShortcut(null);
        setIconSet(iconSet);
        setAction(null);
        setEnabled(true);
    }
    
    /**
     * Determine if the interaction is enabled in the current environment
     * @return true if the interaction is not disabled.
     */
    public boolean isEnabled()
    {
        return _enabled;
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
     * @return The label.
     */
    public I18nizableText getLabel()
    {
        return _label;
    }
   
    /**
     * Set the label of the interaction
     * @param label The label of the interaction.
     */
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }
    
    /**
     * Set the description of the interaction
     * @param description The description.
     */
    public void setDescription(I18nizableText description)
    {
        _description = description;
    }
    
    /**
     * Get the description of the interaction. It can be used for tooltip.
     * @return The description.
     */
    public I18nizableText getDescription()
    {
        return _description;
    }

    /**
     * Get the shortcut of the interaction.
     * @return The shortcut. Can be null.
     */
    public Shortcut getShortcut()
    {
        return _shortcut;
    }
    
    /**
     * Set the shortcut
     * @param shortcut The keyboard shortcut. Can be null.
     */
    public void setShortcut(Shortcut shortcut)
    {
        _shortcut = shortcut;
    }
    
    /**
     * Get the icon set that represent the widget.
     * @return The icon set
     */
    public IconSet getIconSet()
    {
        return _iconSet;
    }
    
    /**
     * Set the iconset.
     * @param iconSet The set of icon
     */
    public void setIconSet(IconSet iconSet)
    {
        _iconSet = iconSet;
    }
    
    /**
     * Get the action of the widget when the user interacts.
     * @return The action. Can be null if the widget is only for display.
     */
    public Action getAction()
    {
        return _action;
    }
    
    /**
     * Set the action
     * @param action The action resulting of the activation of the interaction. Can be null.     
     * */
    public void setAction(Action action)
    {
        _action = action;
    }
}
