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
package org.ametys.core.userpref;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.util.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;

/**
 * Enumerates user preferences.
 */
public class UserPreferencesEnumerator extends AbstractLogEnabled implements Enumerator, Contextualizable, Serviceable
{
    
    /** The user preferences extension point. */
    protected UserPreferencesExtensionPoint _userPrefEP;
    
    /** The avalon context */
    protected Context _context;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _userPrefEP = (UserPreferencesExtensionPoint) serviceManager.lookup(UserPreferencesExtensionPoint.ROLE);
    }
    
    @Override
    public Map<Object, I18nizableText> getEntries() throws Exception
    {
        Map<String, String> contextVars = getContextVars();
        
        Map<Object, I18nizableText> entries = new LinkedHashMap<>();
        for (List<UserPreference> prefs : _userPrefEP.getCategorizedPreferences(contextVars).values())
        {
            for (UserPreference pref : prefs)
            {
                entries.put(pref.getId(), pref.getLabel());
            }
        }
        return entries;
    }
    
    @Override
    public I18nizableText getEntry(String value) throws Exception
    {
        Map<String, String> contextVars = getContextVars();
        
        return _userPrefEP.getUserPreference(contextVars, value).getLabel();
    }
    
    /**
     * Get the preferences context variables.
     * @return the preferences context as a Map.
     */
    protected Map<String, String> getContextVars()
    {
        return Collections.emptyMap();
    }

}
