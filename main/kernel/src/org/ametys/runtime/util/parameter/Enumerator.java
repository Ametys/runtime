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

import java.util.Map;

import org.ametys.runtime.util.I18nizableText;

/**
 * Enumerator for listing values.<p>
 * Such values usually depends on environment (directory listing, DB table, ...).
 */
public interface Enumerator
{
    /**
     * Retrieves a single label from a value.
     * @param value the value.
     * @return the label or <code>null</code> if not found.
     * @throws Exception if an error occurs.
     */
    public I18nizableText getEntry(String value) throws Exception;
    
    /**
     * Provides the enumerated values with their optional label.
     * @return the enumerated values and their label.
     * @throws Exception if an error occurs.
     */
    public Map<Object, I18nizableText> getEntries() throws Exception;
}
