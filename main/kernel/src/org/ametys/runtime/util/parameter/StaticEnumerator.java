/*
 *  Copyright 2012 Anyware Services
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
