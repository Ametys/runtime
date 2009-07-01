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
package org.ametys.runtime.plugins.core.util.ldap;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.directory.SearchControls;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.Enumerator;

/**
 * {@link Enumerator} for listing scopes supported in a LDAP query.
 */
public class ScopeEnumerator implements Enumerator
{
    private static final String __CORE_CATALOGUE = "plugin.core";
    private static final Map<Object, I18nizableText> __SCOPES;
    
    static
    {
        __SCOPES = new LinkedHashMap<Object, I18nizableText>(3);
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
