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
package org.ametys.plugins.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.InvalidModificationException;
import org.ametys.core.user.ModifiableUsersManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UsersManager;
import org.ametys.core.util.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;

/**
 * DAO for manipulating {@link User}
 *
 */
public class UserDAO extends AbstractLogEnabled implements Serviceable, Component
{
    /** The avalon role */
    public static final String ROLE = UserDAO.class.getName();
    
    /** The service manager */
    protected ServiceManager _smanager;
    /** The current user provider. */
    protected CurrentUserProvider _currentUserProvider;

    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
    }
    
    /**
     * Get user's information
     * @param login The user's login
     * @return The user's information
     * @throws ServiceException 
     */
    @Callable
    public Map<String, Object> getUser (String login) throws ServiceException
    {
        return getUser(login, null);
    }
    
    /**
     * Get user's information
     * @param login The user's login
     * @param userManagerRole The users manager's role. Can be null or empty to use the default one.
     * @return The user's information
     * @throws ServiceException 
     */
    @Callable
    public Map<String, Object> getUser (String login, String userManagerRole) throws ServiceException
    {
        UsersManager users = (UsersManager) _smanager.lookup(StringUtils.isEmpty(userManagerRole) ? UsersManager.ROLE : userManagerRole);
        return users.user2JSON(login);
    }
    
    /**
     * Creates a User
     * @param untypedValues The untyped user's parameters
     * @return The created user as JSON object
     * @throws ServiceException
     * @throws InvalidModificationException 
     */
    @Callable
    public Map<String, Object> addUser (Map<String, String> untypedValues) throws ServiceException, InvalidModificationException
    {
        return addUser(untypedValues, null);
    }
    
    /**
     * Creates a User
     * @param untypedValues The untyped user's parameters
     * @param userManagerRole The users manager's role. Can be null or empty to use the default one.
     * @return The created user as JSON object
     * @throws ServiceException
     * @throws InvalidModificationException 
     */
    @Callable
    public Map<String, Object> addUser (Map<String, String> untypedValues, String userManagerRole) throws ServiceException, InvalidModificationException
    {
        Map<String, Object> result = new HashMap<>();
        
        UsersManager u = (UsersManager) _smanager.lookup(StringUtils.isEmpty(userManagerRole) ? UsersManager.ROLE : userManagerRole);
        if (!(u instanceof ModifiableUsersManager))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        
        String login = untypedValues.get("login");
        
        try
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is adding a new user '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), login));
            }

            users.add(untypedValues);
            return users.user2JSON(login);
        }
        catch (InvalidModificationException e)
        {
            Map<String, Errors> fieldErrors = e.getFieldErrors();
            
            if (fieldErrors != null && fieldErrors.size() > 0)
            {
                result.put("errors", fieldErrors);
            }
            else
            {
                throw e;
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's edition");
        }
        
        return result;
    }
    
    /**
     * Edits a User
     * @param untypedValues The untyped user's parameters
     * @return The update user as JSON object
     * @throws ServiceException
     * @throws InvalidModificationException 
     */
    @Callable
    public Map<String, Object> editUser (Map<String, String> untypedValues) throws ServiceException, InvalidModificationException
    {
        return editUser(untypedValues, null);
    }
    
    /**
     * Edits a User
     * @param untypedValues The untyped user's parameters
     * @param userManagerRole The users manager's role. Can be null or empty to use the default one.
     * @return The update user as JSON object
     * @throws ServiceException
     * @throws InvalidModificationException 
     */
    @Callable
    public Map<String, Object> editUser (Map<String, String> untypedValues, String userManagerRole) throws ServiceException, InvalidModificationException
    {
        Map<String, Object> result = new HashMap<>();
        
        UsersManager u = (UsersManager) _smanager.lookup(StringUtils.isEmpty(userManagerRole) ? UsersManager.ROLE : userManagerRole);
        if (!(u instanceof ModifiableUsersManager))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        
        String login = untypedValues.get("login");
        
        try
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is updating information about user '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), login));
            }

            users.update(untypedValues);
            return users.user2JSON(login);
        }
        catch (InvalidModificationException e)
        {
            Map<String, Errors> fieldErrors = e.getFieldErrors();
            
            if (fieldErrors != null && fieldErrors.size() > 0)
            {
                result.put("errors", fieldErrors);
            }
            else
            {
                throw e;
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's edition");
        }
        
        return result;
    }
    
    /**
     * Deletes users
     * @param logins The logins of users to delete
     * @throws ServiceException
     * @throws InvalidModificationException
     */
    @Callable
    public void deleteUsers (List<String> logins) throws ServiceException, InvalidModificationException
    {
        deleteUsers(logins, null);
    }
    
    /**
     * Deletes users
     * @param logins The logins of users to delete
     * @param userManagerRole The users manager's role. Can be null or empty to use the default one.
     * @throws ServiceException
     * @throws InvalidModificationException
     */
    @Callable
    public void deleteUsers (List<String> logins, String userManagerRole) throws ServiceException, InvalidModificationException
    {
        UsersManager u = (UsersManager) _smanager.lookup(StringUtils.isEmpty(userManagerRole) ? UsersManager.ROLE : userManagerRole);
        if (!(u instanceof ModifiableUsersManager))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        
        for (String login : logins)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is is removing user '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), login));
            }
            
            users.remove(login);
        }
       
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's removal");
        }
    }
    
    /**
     * Get the users edition model 
     * @throws ServiceException
     * @throws InvalidModificationException
     * @throws ProcessingException 
     */
    @Callable
    public Map<String, Object> getEditionModel () throws ServiceException, InvalidModificationException, ProcessingException
    {
        return getEditionModel(null);
    }
    
    /**
     * Get the users edition model 
     * @param userManagerRole The users manager's role. Can be null or empty to use the default one.
     * @throws ServiceException
     * @throws InvalidModificationException
     * @throws ProcessingException 
     */
    @Callable
    public Map<String, Object> getEditionModel (String userManagerRole) throws ServiceException, InvalidModificationException, ProcessingException
    {
        Map<String, Object> model = new LinkedHashMap<>();
        
        UsersManager u = (UsersManager) _smanager.lookup(StringUtils.isEmpty(userManagerRole) ? UsersManager.ROLE : userManagerRole);
        if (!(u instanceof ModifiableUsersManager))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        Collection< ? extends Parameter<ParameterType>> parameters = users.getModel();
        for (Parameter<ParameterType> parameter : parameters)
        {
            Map<String, Object> param2json = new HashMap<>();
                    
            param2json.put("id", parameter.getId());
            param2json.put("label", parameter.getLabel());
            param2json.put("description", parameter.getDescription());
            param2json.put("type", ParameterHelper.typeToString(parameter.getType()));
            
            param2json.put("widget", parameter.getWidget());
            
            Validator validator = parameter.getValidator();
            if (validator != null)
            {
                param2json.put("validation", validator.toJson());
            }
            
            String widget = parameter.getWidget();
            
            if (widget != null)
            {
                param2json.put("widget", widget);
            }
            
            Map<String, I18nizableText> widgetParameters = parameter.getWidgetParameters();
            if (widgetParameters != null && widgetParameters.size() > 0)
            {
                param2json.put("widget-params", parameter.getWidgetParameters());
            }
            
            Object defaultValue = parameter.getDefaultValue();
            if (defaultValue != null)
            {
                param2json.put("default-value", parameter.getDefaultValue());
            }

            Enumerator enumerator = parameter.getEnumerator();
            
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
                    
                    param2json.put("enumeration", options);
                }
                catch (Exception e)
                {
                    throw new ProcessingException("Unable to enumerate entries with enumerator: " + enumerator, e);
                }
            }
            
            model.put(parameter.getId(), param2json);
        }
        
        return model;
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected String _getCurrentUser()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) _smanager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new IllegalStateException(e);
            }
        }
        
        if (!_currentUserProvider.isSuperUser())
        {
            return _currentUserProvider.getUser();
        }
        
        return "admin";
    }
    
    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    protected boolean _isSuperUser()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) _smanager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new IllegalStateException(e);
            }
        }
        
        return _currentUserProvider.isSuperUser();
    }
    
    
}