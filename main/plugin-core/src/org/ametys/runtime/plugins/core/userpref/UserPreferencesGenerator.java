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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableGenerator;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.Validator;

/**
 * Generates user preferences.
 */
public class UserPreferencesGenerator extends CurrentUserProviderServiceableGenerator
{
    
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
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String storageContext = parameters.getParameter("prefContext", request.getParameter("prefContext"));
        Map<String, String> contextVars = getContextVars(request);
        String username = getUsername();
        
        if (StringUtils.isBlank(storageContext))
        {
            throw new ProcessingException("Preferences context can't be blank");
        }
        
        try
        {
            contentHandler.startDocument();
            
            AttributesImpl atts = new AttributesImpl();
            atts.addCDATAAttribute("username", username);
            atts.addCDATAAttribute("context", storageContext);
            XMLUtils.startElement(contentHandler, "UserPreferences", atts);
            
            _saxPreferences(storageContext, contextVars, username);
            
            XMLUtils.endElement(contentHandler, "UserPreferences");
            
            contentHandler.endDocument();
        }
        catch (UserPreferencesException e)
        {
            getLogger().error("Cannot get user preferences.", e);
            throw new ProcessingException("Cannot get user preferences.", e);
        }
    }
    
    /**
     * Get the user name in the user manager.
     * @return the user name (login).
     */
    protected String getUsername()
    {
        return parameters.getParameter("username", _getCurrentUser());
    }
    
    /**
     * Get the preferences context.
     * @param request the request.
     * @return the preferences context as a Map.
     */
    protected Map<String, String> getContextVars(Request request)
    {
        return Collections.emptyMap();
    }
    
    /**
     * Generate the list of user preferences for a given user and context.
     * @param storageContext the preferences context.
     * @param contextVars 
     * @param username the user name.
     * @throws ProcessingException
     * @throws SAXException
     * @throws UserPreferencesException
     */
    protected void _saxPreferences(String storageContext, Map<String, String> contextVars, String username) throws ProcessingException, SAXException, UserPreferencesException
    {
        Map<I18nizableText, List<UserPreference>> groups = _userPrefEP.getCategorizedPreferences(contextVars);
        Map<String, String> prefValues = _userPrefManager.getUnTypedUserPrefs(username, storageContext, contextVars);
        
        XMLUtils.startElement(contentHandler, "groups");
        
        for (Entry<I18nizableText, List<UserPreference>> groupEntry : groups.entrySet())
        {
            I18nizableText groupLabel = groupEntry.getKey();
            List<UserPreference> groupPrefs = groupEntry.getValue();
            
            XMLUtils.startElement(contentHandler, "group");
            groupLabel.toSAX(contentHandler, "label");
            
            XMLUtils.startElement(contentHandler, "preferences");
            for (UserPreference preference : groupPrefs)
            {
                _saxPreference(preference, prefValues.get(preference.getId()));
            }
            XMLUtils.endElement(contentHandler, "preferences");
            
            XMLUtils.endElement(contentHandler, "group");
        }
        
        XMLUtils.endElement(contentHandler, "groups");
    }
    
    /**
     * Generate a preference definition and value.
     * @param preference The parameter to SAX
     * @param value The parameter value. Can be null.
     * @throws SAXException If an error occurred while SAXing
     * @throws ProcessingException If an error occurred
     */
    protected void _saxPreference(UserPreference preference, Object value) throws SAXException, ProcessingException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addCDATAAttribute("id", preference.getId());
        attr.addCDATAAttribute("plugin", preference.getPluginName());
        attr.addCDATAAttribute("order", Integer.toString(preference.getOrder()));
        attr.addCDATAAttribute("type", ParameterHelper.typeToString(preference.getType()));
        attr.addCDATAAttribute("multiple", Boolean.toString(preference.isMultiple()));
        XMLUtils.startElement(contentHandler, "preference", attr);
        
        if (preference.getLabel() != null)
        {
            preference.getLabel().toSAX(contentHandler, "label");
        }
        if (preference.getDescription() != null)
        {
            preference.getDescription().toSAX(contentHandler, "description");
        }
        
        Object defaultValue = preference.getDefaultValue();
        
        if (defaultValue != null)
        {
            XMLUtils.createElement(contentHandler, "defaultValue", ParameterHelper.valueToString(defaultValue));
        }
        
        if (value != null)
        {
            XMLUtils.createElement(contentHandler, "value", ParameterHelper.valueToString(value));
        }
        else if (defaultValue != null)
        {
            XMLUtils.createElement(contentHandler, "value", ParameterHelper.valueToString(defaultValue));
        }
        
        if (preference.getWidget() != null)
        {
            XMLUtils.createElement(contentHandler, "widget", preference.getWidget());
        }
        
        Map<String, I18nizableText> widgetParameters = preference.getWidgetParameters();
        if (widgetParameters != null && !widgetParameters.isEmpty())
        {
            XMLUtils.startElement(contentHandler, "widget-params");
            for (String paramName : widgetParameters.keySet())
            {
                XMLUtils.startElement(contentHandler, paramName);
                widgetParameters.get(paramName).toSAX(contentHandler);
                XMLUtils.endElement(contentHandler, paramName);
            }
            XMLUtils.endElement(contentHandler, "widget-params");
        }
        
        Enumerator enumerator = preference.getEnumerator();
        if (enumerator != null)
        {
            ParameterHelper.toSAXEnumerator(contentHandler, enumerator);
        }
        
        Validator validator = preference.getValidator();
        if (validator != null)
        {
            ParameterHelper.toSAXValidator(contentHandler, validator);
        }
        
        XMLUtils.endElement(contentHandler, "preference");
    }
    
}
