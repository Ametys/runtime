/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.plugins.core.userpref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.userpref.UserPreference;
import org.ametys.core.userpref.UserPreferenceProvider;
import org.ametys.core.userpref.UserPreferencesException;
import org.ametys.core.userpref.UserPreferencesExtensionPoint;
import org.ametys.core.userpref.UserPreferencesManager;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * Get user preferences as a JSON object
 */
public class GetUserPreferencesAction extends ServiceableAction
{
    /** The user preferences extension point. */
    protected UserPreferencesExtensionPoint _userPrefEP;
    /** The user preferences manager. */
    protected UserPreferencesManager _userPrefManager;
    /** The current user provider */
    private CurrentUserProvider _currentUserProvider;
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (_userPrefEP == null)
        {
            _userPrefEP = (UserPreferencesExtensionPoint) manager.lookup(UserPreferencesExtensionPoint.ROLE);
            _userPrefManager = (UserPreferencesManager) manager.lookup(UserPreferencesManager.ROLE);
            _currentUserProvider = (CurrentUserProvider) manager.lookup(CurrentUserProvider.ROLE);
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String storageContext = parameters.getParameter("prefContext", request.getParameter("prefContext"));
        
        boolean excludePrivate = parameters.getParameterAsBoolean("excludePrivate", false);
        
        Map<String, String> contextVars = getContextVars(request);
        UserIdentity user = getUser(parameters);
        
        if (StringUtils.isBlank(storageContext))
        {
            throw new ProcessingException("Preferences context can't be blank");
        }
        
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("username", user.getLogin());
        jsonObject.put("userpopulation", user.getPopulationId());
        jsonObject.put("context", storageContext);
        jsonObject.put("preferences", userPrefs2JsonObject(storageContext, contextVars, user, excludePrivate));
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, jsonObject);
        return EMPTY_MAP;
    }

    /**
     * Convert user preferences to JSON object
     * @param storageContext the preferences context.
     * @param contextVars The context vars
     * @param user the user
     * @param excludePrivate true to exclude private preferences
     * @return The JSON object representing the user preferences
     * @throws ProcessingException if an error occurred
     * @throws UserPreferencesException if an error occurred
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> userPrefs2JsonObject (String storageContext, Map<String, String> contextVars, UserIdentity user, boolean excludePrivate) throws ProcessingException, UserPreferencesException
    {
        Map<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("fieldsets", new ArrayList<Map<String, Object>>());
        
        Map<I18nizableText, List<UserPreference>> groups = _userPrefEP.getCategorizedPreferences(contextVars);
        Map<String, String> prefValues = _userPrefManager.getUnTypedUserPrefs(user, storageContext, contextVars);
        
        for (Entry<I18nizableText, List<UserPreference>> groupEntry : groups.entrySet())
        {
            Map<String, Object> fieldSetObject = new LinkedHashMap<>();
            fieldSetObject.put("role", "fieldset");
            fieldSetObject.put("label", groupEntry.getKey());
            
            fieldSetObject.put("elements", userPrefs2JsonObject(groupEntry.getValue(), prefValues, excludePrivate));
            
            List<Map<String, Object>> fieldsets = (List<Map<String, Object>>) jsonObject.get("fieldsets");
            fieldsets.add(fieldSetObject);
        }
        
        return jsonObject;
    }
    
    /**
     * Convert user preferences to JSON object
     * @param userPrefs the user preferences.
     * @param prefValues The values
     * @param excludePrivate true to exclude private preferences
     * @return The JSON object representing the user preferences
     * @throws ProcessingException if an error occurred
     */
    protected Map<String, Object> userPrefs2JsonObject (List<UserPreference> userPrefs, Map<String, String> prefValues, boolean excludePrivate) throws ProcessingException
    {
        Map<String, Object> jsonObject = new LinkedHashMap<>();
        
        for (UserPreference userPref : userPrefs)
        {
            if (!excludePrivate || !userPref.isPrivate())
            {
                jsonObject.put(userPref.getId(), userPref2JsonObject(userPref, prefValues.get(userPref.getId())));
            }
        }
        
        return jsonObject;
    }
    
    /**
     * Convert a user preference to JSON object
     * @param userPref the user preference.
     * @param value The value
     * @return The JSON object representing the user preferences
     * @throws ProcessingException if an error occurred
     */
    protected Map<String, Object> userPref2JsonObject (UserPreference userPref, Object value) throws ProcessingException
    {
        Map<String, Object> jsonObject = new LinkedHashMap<>();
        
        jsonObject.put("label", userPref.getLabel());
        jsonObject.put("description", userPref.getDescription());
        jsonObject.put("type", userPref.getType().name());
        jsonObject.put("pluginName", userPref.getPluginName());
        jsonObject.put("private", userPref.isPrivate());
        
        Validator validator = userPref.getValidator();
        if (validator != null)
        {
            jsonObject.put("validation", validator.toJson());
        }
        
        String widget = userPref.getWidget();
        
        if (widget != null)
        {
            jsonObject.put("widget", widget);
        }
        
        Map<String, I18nizableText> widgetParameters = userPref.getWidgetParameters();
        if (widgetParameters != null && widgetParameters.size() > 0)
        {
            jsonObject.put("widget-params", userPref.getWidgetParameters());
        }
        
        jsonObject.put("multiple", userPref.isMultiple());
        
        Object defaultValue = userPref.getDefaultValue();
        if (defaultValue != null)
        {
            jsonObject.put("default-value", userPref.getDefaultValue());
        }

        if (value != null)
        {
            jsonObject.put("value", value);
        }
        else
        {
            jsonObject.put("value", defaultValue);
        }
        
        Enumerator enumerator = userPref.getEnumerator();
        
        if (enumerator != null)
        {
            try
            {
                List<Map<String, Object>> options = new ArrayList<>();
                
                for (Map.Entry<Object, I18nizableText> entry : enumerator.getEntries().entrySet())
                {
                    String valueAsString = ParameterHelper.valueToString(entry.getKey());
                    I18nizableText entryLabel = entry.getValue();
                    
                    Map<String, Object> option = new HashMap<>();
                    option.put("label", entryLabel != null ? entryLabel : valueAsString);
                    option.put("value", valueAsString);
                    options.add(option);
                }
                
                jsonObject.put("enumeration", options);
            }
            catch (Exception e)
            {
                throw new ProcessingException("Unable to enumerate entries with enumerator: " + enumerator, e);
            }
        }
        return jsonObject;
    }
    
    /**
     * Get the user in the user manager.
     * @param parameters The parameters
     * @return the user .
     */
    protected UserIdentity getUser(Parameters parameters)
    {
        String login = parameters.getParameter("username", "");
        String populationId = parameters.getParameter("population", "");
        UserIdentity user;
        if (StringUtils.isEmpty(login) || StringUtils.isEmpty(populationId))
        {
            user = _currentUserProvider.getUser();
        }
        else
        {
            user = new UserIdentity(login, populationId);
        }
        return user;
    }
    

    /**
     * Get the preferences context.
     * @param request the request.
     * @return the preferences context as a Map.
     */
    protected Map<String, String> getContextVars(Request request)
    {
        Map<String, String> contextVars = new HashMap<>();
        contextVars.put(UserPreferenceProvider.CONTEXT_VAR_WORKSPACE, (String) request.getAttribute(WorkspaceMatcher.WORKSPACE_NAME));
        return contextVars;
    }
}
