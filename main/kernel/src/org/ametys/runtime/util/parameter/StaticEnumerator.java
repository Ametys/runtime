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
package org.ametys.runtime.util.parameter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ametys.runtime.util.I18nizableText;


/**
 * This implementation enumerate the static configured elements
 */
public class StaticEnumerator implements Enumerator
{
    private Map<Object, I18nizableText> _entries = new LinkedHashMap<Object, I18nizableText>();

    /**
     * Adds a new entry.
     * @param label the entry label.
     * @param value the entry value.
     */
    public void add(I18nizableText label, String value)
    {
        _entries.put(value, label);
    }
    
    public I18nizableText getEntry(String value)
    {
        return _entries.get(value);
    }
    
    public Map<Object, I18nizableText> getEntries()
    {
        return _entries;
    }
}
