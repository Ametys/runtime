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
package org.ametys.runtime.ui.item.part;

/**
 * This class wrap a keyboard shortcut
 */
@Deprecated
public final class Shortcut
{
    private boolean _ctrl;
    private boolean _alt;
    private boolean _shift;
    private String _key;
    
    /**
     * Create a shortcut representation
     * @param crlKey true if the 'CTRL' key needs to be pressed
     * @param altKey true if the 'ALT' key need to be pressed
     * @param shiftKey true if the 'SHIFT' key needs to be pressed
     * @param key the char code of the key. Can be 
     * <ul>
     *  <li>between A and Z,</li>
     *  <li>F1 and F12,</li>
     *  <li>Return</li>
     *  <li>Esc</li>
     * </ul>
     */
    public Shortcut (boolean crlKey, boolean altKey, boolean shiftKey, String key)
    {
        _ctrl = crlKey;
        _alt = altKey;
        _shift = shiftKey;
        _key = key;
    }
    
    /**
     * Determine wheter the 'CTRL' key if part of the shortcut
     * @return true if the 'CTRL' key needs to be pressed
     */
    public boolean hasCtrl()
    {
        return _ctrl;
    }
    
    /**
     * Determine wheter the 'ALT' key if part of the shortcut
     * @return true if the 'ALT' key needs to be pressed
     */
    public boolean hasAlt()
    {
        return _alt;
    }
    
    /**
     * Determine wheter the 'SHIFT' key if part of the shortcut
     * @return true if the 'SHIFT' key needs to be pressed
     */
    public boolean hasShift()
    {
        return _shift;
    }
    
    /**
     * Get the main key of the shortcut
     * @return The main key of the shortcut. See {@link Shortcut} to view allowed values.
     */
    public String getKey()
    {
        return _key;
    }
}
