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
package org.ametys.core.util.ldap;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.directory.SearchControls;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;

/**
 * {@link Enumerator} for listing scopes supported in a LDAP query.
 */
public class ScopeEnumerator implements Enumerator
{
    private static final String __CORE_CATALOGUE = "plugin.core";
    private static final Map<Object, I18nizableText> __SCOPES;
    
    static
    {
        __SCOPES = new LinkedHashMap<>(3);
        __SCOPES.put("object", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_USERS_LDAPUSER_CONFIG_SEARCH_SCOPE_ENUM_OBJECT"));
        __SCOPES.put("one", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_USERS_LDAPUSER_CONFIG_SEARCH_SCOPE_ENUM_ONE"));
        __SCOPES.put("sub", new I18nizableText(__CORE_CATALOGUE, "PLUGINS_CORE_USERS_LDAPUSER_CONFIG_SEARCH_SCOPE_ENUM_SUB"));
    }
    
    /**
     * Parses a scope config parameter into a <code>int</code> for using it as
     * {@link SearchControls}.
     * @param scope the scope string representation.
     * @return the scope as a <code>SearchControls.*_SCOPE</code>.
     * @throws IllegalArgumentException if the given scope is not valid. 
     */
    public static int parseScope(String scope) throws IllegalArgumentException
    {
        if ("one".equals(scope))
        {
            return SearchControls.ONELEVEL_SCOPE;
        }
        else if ("sub".equals(scope))
        {
            return SearchControls.SUBTREE_SCOPE;
        }
        else if ("object".equals(scope))
        {
            return SearchControls.OBJECT_SCOPE;
        }
        else
        {
            throw new IllegalArgumentException("Invalid scope: " + scope);
        }
    }
    
    public I18nizableText getEntry(String value)
    {
        return __SCOPES.get(value);
    }
    
    public Map<Object, I18nizableText> getEntries()
    {
        return __SCOPES;
    }
}
