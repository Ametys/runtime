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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * Extension point holding all {@link UserPreference} definitions.
 */
public class UserPreferencesExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<UserPreferenceProvider>
{
    
    /** Avalon Role */
    public static final String ROLE = UserPreferencesExtensionPoint.class.getName();
    
    /** User preference parser. */
    private UserPrefOrderComparator _comparator;
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        _comparator = new UserPrefOrderComparator();
    }
    
    /**
     * Get all the declared user preferences.
     * @param contextVars The context variables including environment elements 
     * @param id The preference id
     * @return the user preferences (read-only collection).
     */
    public UserPreference getUserPreference(Map<String, String> contextVars, String id)
    {
        for (String extensionId : getExtensionsIds())
        {
            UserPreferenceProvider provider = getExtension(extensionId);
            
            for (UserPreference preference : provider.getPreferences(contextVars))
            {
                if (preference.getId().equals(id))
                {
                    return preference;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all the declared user preferences.
     * @param contextVars The context variables including environment elements 
     * @return the user preferences (read-only collection).
     */
    public Map<String, UserPreference> getUserPreferences(Map<String, String> contextVars)
    {
        return Collections.unmodifiableMap(getPreferencesMap(contextVars));
    }
    
    /**
     * Get all the preferences, classified by group and ordered.
     * @param contextVars The context variables including environment elements 
     * @return the preferences classified by group and ordered.
     */
    public Map<I18nizableText, List<UserPreference>> getCategorizedPreferences(Map<String, String> contextVars)
    {
        return Collections.unmodifiableMap(getCategorizedPreferencesMap(contextVars));
    }
    
    /**
     * Validate preference values.
     * @param contextVars The context variables including environment elements 
     * @param values the values.
     * @param errors the errors object to fill in.
     */
    public void validatePreferences(Map<String, String> contextVars, Map<String, String> values, UserPreferencesErrors errors)
    {
        Map<String, UserPreference> preferences = getPreferencesMap(contextVars);
        
        for (Entry<String, String> entry : values.entrySet())
        {
            UserPreference pref = preferences.get(entry.getKey());
            if (pref != null)
            {
                String value = entry.getValue();
                
                Object castValue = ParameterHelper.castValue(value, pref.getType());
                if (StringUtils.isNotEmpty(value) && castValue == null)
                {
                    errors.addError(pref.getId(), new I18nizableText("plugin.core", "PLUGINS_CORE_UI_USER_PREFERENCES_INVALID_TYPE"));
                }
                else
                {
                    Validator validator = pref.getValidator();
                    if (validator != null)
                    {
                        Errors fieldErrors = new Errors();
                        pref.getValidator().validate(castValue == null ? value : castValue, fieldErrors);
                        
                        if (fieldErrors.hasErrors())
                        {
                            errors.addErrors(pref.getId(), fieldErrors.getErrors());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Compute the preferences map.
     * @param contextVars The context variables including environment elements 
     * @return the preferences map.
     */
    protected Map<String, UserPreference> getPreferencesMap(Map<String, String> contextVars)
    {
        Map<String, UserPreference> preferences = new HashMap<>();
        
        for (String extensionId : getExtensionsIds())
        {
            UserPreferenceProvider provider = getExtension(extensionId);
            
            for (UserPreference preference : provider.getPreferences(contextVars))
            {
                preferences.put(preference.getId(), preference);
            }
        }
        
        return preferences;
    }
    
    /**
     * Compute the grouped preferences map.
     * @param contextVars The context variables including environment elements 
     * @return the grouped preferences map.
     */
    protected Map<I18nizableText, List<UserPreference>> getCategorizedPreferencesMap(Map<String, String> contextVars)
    {
        Map<I18nizableText, List<UserPreference>> preferences = new TreeMap<>(new I18nizableTextComparator());
        
        for (String extensionId : getExtensionsIds())
        {
            UserPreferenceProvider provider = getExtension(extensionId);
            
            for (UserPreference preference : provider.getPreferences(contextVars))
            {
                I18nizableText groupName = preference.getDisplayGroup();

                // Get the map of preferences of the group.
                List<UserPreference> group = preferences.get(groupName);
                if (group == null)
                {
                    group = new ArrayList<>();
                    preferences.put(groupName, group);
                }
                
                group.add(preference);
            }
        }
        
        // Sort all groups.
        for (List<UserPreference> group : preferences.values())
        {
            Collections.sort(group, _comparator);
        }
        
        return preferences;
    }
    
    /**
     * Compares user preferences on their "order" attribute.
     */
    class UserPrefOrderComparator implements Comparator<UserPreference>
    {
        @Override
        public int compare(UserPreference pref1, UserPreference pref2)
        {
            return pref1.getOrder() - pref2.getOrder();
        }
    }
    
    class I18nizableTextComparator implements Comparator<I18nizableText>
    {
        @Override
        public int compare(I18nizableText t1, I18nizableText t2)
        {
            return t1.toString().compareTo(t2.toString());
        }
    }
    
}
