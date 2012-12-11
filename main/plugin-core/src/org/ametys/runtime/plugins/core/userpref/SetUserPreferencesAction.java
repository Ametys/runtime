/*
 *  Copyright 2011 Anyware Services
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * Action which saves the user preferences values into the database.
 */
public class SetUserPreferencesAction extends CurrentUserProviderServiceableAction
{
    
    /** The input date format. */
    protected static final Set<DateFormat> _INPUT_DATE_FORMATS = new HashSet<DateFormat>();
    static
    {
        _INPUT_DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd"));
        _INPUT_DATE_FORMATS.add(new SimpleDateFormat("dd/MM/yyyy"));
    }
    
    /** The user preferences extension point. */
    protected UserPreferencesExtensionPoint _userPrefEP;
    
    /** The user preferences manager. */
    protected UserPreferencesManager _userPrefManager;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _userPrefEP = (UserPreferencesExtensionPoint) serviceManager.lookup(UserPreferencesExtensionPoint.ROLE);
        _userPrefManager = (UserPreferencesManager) serviceManager.lookup(UserPreferencesManager.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> parentContext = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        String context = parameters.getParameter("prefContext", request.getParameter("prefContext"));
        String username = parameters.getParameter("username", _getCurrentUser());
        String submit = request.getParameter("submit");
        
        Map<String, String> results = new HashMap<String, String>();
        
        if ("true".equals(submit))
        {
            Collection<String> preferenceIds = getPreferenceIds(request, parentContext);
            
            results = setUserPreferences(request, context, username, preferenceIds);
        }
        
        return results;
    }
    
    /**
     * Set user preferences.
     * @param request the request.
     * @param context the preferences context.
     * @param username the user name.
     * @return the results.
     * @throws UserPreferencesException
     */
    protected Map<String, String> setUserPreferences(Request request, String context, String username) throws UserPreferencesException
    {
        return setUserPreferences(request, context, username, Collections.<String>emptySet());
    }
    
    /**
     * Set user preferences.
     * @param request the request.
     * @param context the preferences context.
     * @param username the user name.
     * @param preferenceIds a collection of the IDs of preferences to set.
     * @return the results.
     * @throws UserPreferencesException
     */
    protected Map<String, String> setUserPreferences(Request request, String context, String username, Collection<String> preferenceIds) throws UserPreferencesException
    {
        Map<String, String> results = new HashMap<String, String>();
        
        results.put("status", "error");
        
        Map<String, String> values = _userPrefManager.getUnTypedUserPrefs(username, context);
        
        UserPreferencesErrors errors = new UserPreferencesErrors();
        
        // Override the old values with the new ones, but keep old values when new preferences are not in the request.
        values.putAll(_getValues(request, preferenceIds, errors));
        
        // Validate the user preferences, filling in potential errors.
        _userPrefEP.validatePreferences(values, errors);
        
        if (!errors.hasErrors())
        {
            _userPrefManager.setUserPreferences(username, context, values);
            results.put("status", "success");
        }
        else
        {
            request.setAttribute("user-prefs-errors", errors);
        }
        
        return results;
    }
    
    /**
     * Get the preferences values from the request.
     * @param request the request.
     * @param preferenceIds a collection of the IDs of preferences to set.
     * @param errors the errors object to fill in.
     * @return the user preferences values as a Map.
     */
    protected Map<String, String> _getValues(Request request, Collection<String> preferenceIds, UserPreferencesErrors errors)
    {
        Map<String, String> preferences = new HashMap<String, String>();
        
        for (UserPreference preference : _userPrefEP.getUserPreferences().values())
        {
            String id = preference.getId();
            if (preference.getType() == ParameterType.DATE)
            {
                String value = request.getParameter(id);
                if (value != null)
                {
                    if (StringUtils.isBlank(value))
                    {
                        preferences.put(id, value);
                    }
                    else
                    {
                        Date date = _parseDate(value);
                        if (date != null)
                        {
                            preferences.put(id, ParameterHelper.valueToString(date));
                        }
                    }
                }
            }
            else if (preference.getType() == ParameterType.PASSWORD)
            {
                // Password: if no new value is provided, keep the old value.
                String value = request.getParameter(id);
                if (StringUtils.isNotBlank(value))
                {
                    // Check if the confirmation match.
                    String confirmationValue = request.getParameter(id + "-confirmation");
                    if (!value.equals(confirmationValue))
                    {
                        errors.addError(id, new I18nizableText("plugin.core", "PLUGINS_CORE_USER_PREFERENCES_PWD_CONFIRMATION_DOESNT_MATCH"));
                    }
                    preferences.put(id, value);
                }
            }
            else if (preference.getType() == ParameterType.BOOLEAN && preferenceIds.contains(id))
            {
                // Boolean value: if the preference is set if in the request, it's true, if it's not, it's false.
                // So, check that the preference was wanted (present in preferenceIds).
                String value = request.getParameter(id);
                String valueStr = String.valueOf("true".equals(value));
                preferences.put(id, valueStr);
            }
            else
            {
                String[] values = request.getParameterValues(id);
                if (values != null)
                {
                    String valuesStr = StringUtils.join(values, ',');
                    preferences.put(id, valuesStr);
                }
            }
        }
        
        return preferences;
    }
    
    /**
     * Parse a user-submitted date.
     * @param value the date value as a String.
     * @return the Date.
     */
    protected Date _parseDate(String value)
    {
        Date date = null;
        for (DateFormat format : _INPUT_DATE_FORMATS)
        {
            try
            {
                date = format.parse(value);
                return date;
            }
            catch (ParseException e)
            {
                // Ignore.
            }
        }
        return date;
    }
    
    /**
     * Get the preferences to set.
     * @param request the request.
     * @param parentContext the parent context.
     * @return a collection of the IDs of preferences to set.
     */
    protected Collection<String> getPreferenceIds(Request request, Map<String, Object> parentContext)
    {
        return Collections.emptySet();
    }
    
}
