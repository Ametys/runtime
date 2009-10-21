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

/**
 * This interface encapsulate all interactions 
 * @deprecated
 */
public interface UIItem
{
    /**
     * Constant for a system wide bar separator
     */
    public static final UIItem SEPARATOR_BAR = new BarSeparator();
    
    /**
     * Constant for a system wide space separator
     */
    public static final UIItem SEPARATOR_SPACE = new SpaceSeparator();
    
    /**
     * Bar separator
     */
    public class BarSeparator implements UIItem
    {
        // empty
    }
    
    /**
     * Space separator
     */
    public class SpaceSeparator implements UIItem
    {
        // empty
    }
}
