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
package org.ametys.runtime.plugins.core.userpref;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for classes providing user preferences.
 */
public interface UserPreferenceProvider
{
    
    /**
     * Get this provider's preferences.
     * @param contextVars a Map of context variables.
     * @return a Collection of preferences for the context.
     */
    public Collection<UserPreference> getPreferences(Map<String, String> contextVars);
    
}
