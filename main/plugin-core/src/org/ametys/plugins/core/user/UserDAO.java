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
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.InvalidModificationException;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.i18n.I18nizableText;
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
public class UserDAO extends AbstractLogEnabled implements Component, Contextualizable, Serviceable 
{
    /** The avalon role */
    public static final String ROLE = UserDAO.class.getName();
    
    /** The service manager */
    protected ServiceManager _smanager;
    /** The user manager */
    protected UserManager _userManager;
    /** The user population DAO */
    protected UserPopulationDAO _userPopulationDAO;
    /** The current user provider. */
    protected CurrentUserProvider _currentUserProvider;
    /** The Avalon context */
    protected Context _context;
    /** The user helper */
    protected UserHelper _userHelper;

    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        _userHelper = (UserHelper) smanager.lookup(UserHelper.ROLE);
    }
    
    /**
     * Get user's information
     * @param login The user's login
     * @param populationId The id of the population
     * @return The user's information
     */
    @Callable
    public Map<String, Object> getUser (String login, String populationId)
    {
        return _userHelper.user2json(_userManager.getUser(populationId, login), true);
    }
    
    /**
     * Checks if the user is modifiable
     * @param login The users's login
     * @param populationId The id of the population of the user
     * @return A map with the "isModifiable" at true if the user is modifiable
     */
    @Callable
    public Map<String, Object> isModifiable (String login, String populationId)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("isModifiable", _userManager.getUserDirectory(populationId, login) instanceof ModifiableUserDirectory);
        result.put("additionalDescription", new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_USERS_EDIT_NO_MODIFIABLE_DESCRIPTION"));
        return result;
    }
    
    /**
     * Checks if the user is removable
     * @param login The users's login
     * @param populationId The id of the population of the user
     * @return A map with the "isRemovable" at true if the user is removable
     */
    @Callable
    public Map<String, Object> isRemovable (String login, String populationId)
    {
        Map<String, Object> result = new HashMap<>();
        
        UserDirectory userDirectory = _userManager.getUserDirectory(populationId, login);
        if (userDirectory != null && populationId.equals(UserPopulationDAO.ADMIN_POPULATION_ID) && userDirectory.getUsers().size() == 1)
        {
            // Impossible to delete the last user of the admin population
            result.put("isRemovable", false);
            result.put("additionalDescription", new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_USERS_DELETE_LAST_ADMIN_DESCRIPTION"));
        }
        else
        {
            result.put("isRemovable", userDirectory instanceof ModifiableUserDirectory);
            result.put("additionalDescription", new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_USERS_DELETE_NO_MODIFIABLE_DESCRIPTION"));
        }
        return result;
    }
    
    /**
     * Creates a User
     * @param populationId The id of the user population
     * @param userDirectoryIndex The index of the user directory
     * @param untypedValues The untyped user's parameters
     * @return The created user as JSON object
     * @throws InvalidModificationException If modification is not possible 
     */
    @Callable
    public Map<String, Object> addUser (String populationId, int userDirectoryIndex, Map<String, String> untypedValues) throws InvalidModificationException
    {
        Map<String, Object> result = new HashMap<>();
        
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(populationId);
        UserDirectory userDirectory = userPopulation.getUserDirectories().get(userDirectoryIndex);
        
        if (!(userDirectory instanceof ModifiableUserDirectory))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) userDirectory;
        
        String login = untypedValues.get("login");
        
        try
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is adding a new user '%s'", _getCurrentUser(), login));
            }

            modifiableUserDirectory.add(untypedValues);
            return _userHelper.user2json(modifiableUserDirectory.getUser(login), true);
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
     * @param populationId The id of the population of the user to edit
     * @param untypedValues The untyped user's parameters
     * @return The update user as JSON object
     * @throws InvalidModificationException If modification is not possible 
     */
    @Callable
    public Map<String, Object> editUser (String populationId, Map<String, String> untypedValues) throws InvalidModificationException
    {
        Map<String, Object> result = new HashMap<>();
        
        String login = untypedValues.get("login");
        UserDirectory userDirectory = _userManager.getUserDirectory(populationId, login);
        
        if (!(userDirectory instanceof ModifiableUserDirectory))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) userDirectory;
        
        try
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is updating information about user '%s'", _getCurrentUser(), login));
            }

            modifiableUserDirectory.update(untypedValues);
            return _userHelper.user2json(modifiableUserDirectory.getUser(login), true);
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
     * @param users The users to delete
     * @throws InvalidModificationException If modification is not possible 
     */
    @Callable
    public void deleteUsers (List<Map<String, String>> users) throws InvalidModificationException
    {
        for (Map<String, String> user : users)
        {
            String login = user.get("login");
            String populationId = user.get("population");
            _deleteUser(login, populationId);
        }
       
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's removal");
        }
    }
    
    private void _deleteUser(String login, String populationId) throws InvalidModificationException
    {
        UserDirectory userDirectory = _userManager.getUserDirectory(populationId, login);
        
        if (!(userDirectory instanceof ModifiableUserDirectory))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        if (populationId.equals(UserPopulationDAO.ADMIN_POPULATION_ID) && userDirectory.getUsers().size() == 1)
        {
            // Impossible to delete the last user of the admin population
            getLogger().error("Deletion forbidden: last user of the 'admin' population.");
            throw new InvalidModificationException("You cannot delete the last user of the 'admin' population !");
        }
        
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) userDirectory;
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is removing user '%s'", _getCurrentUser(), login));
        }
        
        modifiableUserDirectory.remove(login);
    }
    
    /**
     * Get the users edition model 
     * @param login The user's login to edit
     * @param populationId The id of the population of the user to edit
     * @throws InvalidModificationException If modification is not possible 
     * @throws ProcessingException If there is another exception
     * @return The edition model as an object
     */
    @Callable
    public Map<String, Object> getEditionModel (String login, String populationId) throws InvalidModificationException, ProcessingException
    {
        UserDirectory userDirectory = _userManager.getUserDirectory(populationId, login);
        return _getEditionModel(userDirectory);
    }
    
    /**
     * Get the users edition model 
     * @param populationId The id of the population where to add a user
     * @param userDirectoryIndex The index of the user directory where to add a user
     * @throws InvalidModificationException If modification is not possible 
     * @throws ProcessingException If there is another exception
     * @return The edition model as an object
     */
    @Callable
    public Map<String, Object> getEditionModel (String populationId, int userDirectoryIndex) throws InvalidModificationException, ProcessingException
    {
        UserDirectory userDirectory = _userPopulationDAO.getUserPopulation(populationId).getUserDirectories().get(userDirectoryIndex);
        return _getEditionModel(userDirectory);
    }
    
    private Map<String, Object> _getEditionModel (UserDirectory userDirectory) throws InvalidModificationException, ProcessingException
    {
        Map<String, Object> model = new LinkedHashMap<>();
        
        if (!(userDirectory instanceof ModifiableUserDirectory))
        {
            getLogger().error("Users are not modifiable !");
            throw new InvalidModificationException("Users are not modifiable !");
        }
        
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) userDirectory;
        Collection< ? extends Parameter<ParameterType>> parameters = modifiableUserDirectory.getModel();
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
     * Impersonate the selected user
     * @param login the login of the user to impersonate
     * @param populationId The id of the population
     * @throws ServiceException If there is an issue with the service manager
     * @return a map of information on the user
     */
    @Callable
    public Map<String, String> impersonate(String login, String populationId) throws ServiceException
    {
        Map<String, String> result = new HashMap<>();
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
        
        // FIXME add a right to impersonate

        if (StringUtils.isEmpty(login))
        {
            throw new IllegalArgumentException("'login' parameter is null or empty");
        }
        
        UserManager usersManager = (UserManager) _smanager.lookup(UserManager.ROLE);
        User user = usersManager.getUser(populationId, login);
        if (user == null)
        {
            result.put("error", "unknown-user");   
        }
        else
        {
            Request request = ContextHelper.getRequest(_context);
            
            request.getSession(true).setAttribute(AuthenticateAction.SESSION_USERIDENTITY, new UserIdentity(login, populationId));
            request.getSession(true).setAttribute(AuthenticateAction.SESSION_CREDENTIALPROVIDER, null);
            
            result.put("login", login);
            result.put("population", populationId);
            result.put("name", user.getFullName());
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Impersonation of the user '" + login + "' from IP " + request.getRemoteAddr());
            }
        }
        
        return result;
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected UserIdentity _getCurrentUser()
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
        
        return _currentUserProvider.getUser();
    }
    
    
}
