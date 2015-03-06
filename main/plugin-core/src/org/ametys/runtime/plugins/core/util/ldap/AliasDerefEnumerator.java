/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugins.core.util.ldap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.Enumerator;

/**
 * {@link Enumerator} for listing alias dereferencing modes supported in a LDAP query.
 */
public class AliasDerefEnumerator implements Enumerator
{
    private static final String __CORE_CATALOGUE = "plugin.core";
    private static final Map<Object, I18nizableText> __ALIAS_DEREF_MODES;
    
    static
    {
        __ALIAS_DEREF_MODES = new LinkedHashMap<Object, I18nizableText>(4);
        __ALIAS_DEREF_MODES.put("always", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_ALWAYS"));
        __ALIAS_DEREF_MODES.put("never", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_NEVER"));
        __ALIAS_DEREF_MODES.put("finding", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_FINDING"));
        __ALIAS_DEREF_MODES.put("searching", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_SEARCHING"));
    }
    
    @Override
    public I18nizableText getEntry(String value)
    {
        return __ALIAS_DEREF_MODES.get(value);
    }
    
    @Override
    public Map<Object, I18nizableText> getEntries()
    {
        return Collections.unmodifiableMap(__ALIAS_DEREF_MODES);
    }
    
}
