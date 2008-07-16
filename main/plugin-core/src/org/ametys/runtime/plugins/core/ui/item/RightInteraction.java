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
package org.ametys.runtime.plugins.core.ui.item;

import org.ametys.runtime.ui.item.Interaction;
import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.util.I18nizableText;

/**
 * This interaction also handle a right
 */
public class RightInteraction extends Interaction
{
    /** The name of the right needed */
    protected String _right;
    
    /**
     * Create an interaction
     * @param label The label of the interaction. Cannot be null.
     * @param description The description. Cannot be null.
     * @param iconSet The set of icon. Cannot be null.
     */
    public RightInteraction(I18nizableText label, I18nizableText description, IconSet iconSet)
    {
        super(label, description, iconSet);
    }
    
    /**
     * Get the right.
     * @return the right name. Can be null or empty.
     */
    public String getRight()
    {
        return _right;
    }
    
    /**
     * Set the right
     * @param right The right name
     */
    public void setRight(String right)
    {
        _right = right;
    }
}
