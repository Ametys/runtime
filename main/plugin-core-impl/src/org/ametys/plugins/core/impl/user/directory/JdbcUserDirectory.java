/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.plugins.core.impl.user.directory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.ObservationConstants;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.ObservationManager;
import org.ametys.core.script.SQLScriptHelper;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.InvalidModificationException;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.util.CachingComponent;
import org.ametys.plugins.core.impl.user.jdbc.JdbcParameter;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.AbstractParameterParser;
import org.ametys.runtime.parameter.DefaultValidator;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Use a jdbc driver for getting the list of users, modifying them and also
 * authenticate them.<br>
 * Passwords need to be encrypted with MD5 and encoded in base64.<br>
 */
public class JdbcUserDirectory extends CachingComponent<User> implements ModifiableUserDirectory, Component, Serviceable, Contextualizable, PluginAware, Disposable
{
    /** The base plugin (for i18n key) */
    protected static final String BASE_PLUGIN_NAME = "core";
    
    static final String[] __COLUMNS = new String[] {"login", "password", "firstname", "lastname", "email"};
    
    /** Name of the parameter holding the datasource id */
    private static final String __DATASOURCE_PARAM_NAME = "runtime.users.jdbc.datasource";
    /** Name of the parameter holding the table users' name */
    private static final String __USERS_TABLE_PARAM_NAME = "runtime.users.jdbc.table";
    
    private static final String __COLUMN_LOGIN = "login";
    private static final String __COLUMN_PASSWORD = "password";
    private static final String __COLUMN_FIRSTNAME = "firstname";
    private static final String __COLUMN_LASTNAME = "lastname";
    private static final String __COLUMN_EMAIL = "email";
    private static final String __COLUMN_SALT = "salt";
    
    /** The identifier of data source */
    protected String _dataSourceId;
    /** The name of users' SQL table */
    protected String _userTableName;
    
    /** Model */
    protected Map<String, JdbcParameter> _model;
    
    /** Plugin name */
    protected String _pluginName;
    
    /** The avalon service manager */
    protected ServiceManager _manager;
    
    /** The avalon context */
    protected Context _context;
    
    /** The cocoon source resolver */
    protected SourceResolver _sourceResolver;

    // ComponentManager for the Validators
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    //ComponentManager for the Enumerators
    private ThreadSafeComponentManager<Enumerator> _enumeratorManager;
    
    private ObservationManager _observationManager;
    private CurrentUserProvider _currentUserProvider;
    
    private String _udModelId;
    private Map<String, Object> _paramValues;
    private String _populationId;
    
    private String _label;

    private String _id;

    private boolean _lazyInitialized;
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void dispose()
    {
        _enumeratorManager.dispose();
        _enumeratorManager = null;
        
        _validatorManager.dispose();
        _validatorManager = null;
    }
    
    public String getId()
    {
        return _id;
    }
    
    public String getLabel()
    {
        return _label;
    }
    
    @Override
    public void init(String id, String udModelId, Map<String, Object> paramValues, String label)
    {
        _id = id;
        _udModelId = udModelId;
        _paramValues = paramValues;
        _label = label;
        
        _userTableName = (String) paramValues.get(__USERS_TABLE_PARAM_NAME);
        _dataSourceId = (String) paramValues.get(__DATASOURCE_PARAM_NAME);
        
        configureModelParameters();
    }
    
    /**
     * Lazy lookup the {@link ObservationManager}
     * @return the observation manager
     */
    protected ObservationManager getObservationManager()
    {
        if (_observationManager == null)
        {
            try
            {
                _observationManager = (ObservationManager) _manager.lookup(ObservationManager.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException("Unable to lookup ObservationManager component", e);
            }
        }
        return _observationManager;
    }
    
    /**
     * Lazy lookup the {@link CurrentUserProvider}
     * @return the current user provider
     */
    protected CurrentUserProvider getCurrentUserProvider()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) _manager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException("Unable to lookup CurrentUserProvider component", e);
            }
        }
        return _currentUserProvider;
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    @SuppressWarnings("unchecked")
    protected Connection getSQLConnection()
    {
        Connection connection = ConnectionHelper.getConnection(_dataSourceId);
        
        if (!_lazyInitialized)
        {
            try
            {
                SQLScriptHelper.createTableIfNotExists(connection, _userTableName, "plugin:core://scripts/%s/jdbc_users.template.sql", _sourceResolver, 
                        (Map) ArrayUtils.toMap(new String[][] {{"%TABLENAME%", _userTableName}}));
            }
            catch (Exception e)
            {
                getLogger().error("The tables requires by the " + this.getClass().getName() + " could not be created. A degraded behavior will occur", e);
            }
            
            _lazyInitialized = true;
        }
        
        return connection;
    }
    
    @Override
    public void setPopulationId(String populationId)
    {
        _populationId = populationId;
    }
    
    @Override
    public String getPopulationId()
    {
        return _populationId;
    }
    
    @Override
    public Map<String, Object> getParameterValues()
    {
        return _paramValues;
    }
    
    @Override
    public String getUserDirectoryModelId()
    {
        return _udModelId;
    }
    
    @Override
    public Collection<User> getUsers()
    {
        return getUsers(Integer.MAX_VALUE, 0, Collections.EMPTY_MAP);
    }
    
    @Override
    public List<User> getUsers(int count, int offset, Map<String, Object> parameters)
    {
        String pattern = StringUtils.defaultIfEmpty((String) parameters.get("pattern"), null);
        int boundedCount = count >= 0 ? count : Integer.MAX_VALUE;
        int boundedOffset = offset >= 0 ? offset : 0;
        
        SelectUsersJdbcQueryExecutor<List<User>> queryExecutor = new SelectUsersJdbcQueryExecutor<List<User>>(pattern, boundedCount, boundedOffset) 
        {
            @Override
            protected List<User> processResultSet(ResultSet rs) throws SQLException
            {
                return _getUsersProcessResultSet(rs);
            }
        };
        
        return queryExecutor.run();
    }

    @Override
    public User getUser(String login)
    {
        if (isCacheEnabled())
        {
            User user = getObjectFromCache(login);
            if (user != null)
            {
                return user;
            }
        }
        
        SelectUserJdbcQueryExecutor<User> queryExecutor = new SelectUserJdbcQueryExecutor<User>(login)
        {
            @Override
            protected User processResultSet(ResultSet rs) throws SQLException
            {
                return _getUserProcessResultSet(rs, login);
            }
        };
        
        return queryExecutor.run();
    }

    @Override
    public boolean checkCredentials(String login, String password)
    {
        boolean updateNeeded = false;
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Connect to the database with connection pool
            con = getSQLConnection();

            // Build request for authenticating the user
            String sql = "SELECT " + __COLUMN_LOGIN + ", " + __COLUMN_PASSWORD + ", " + __COLUMN_SALT + " FROM " + _userTableName + " WHERE " + __COLUMN_LOGIN + " = ?";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }

            stmt = con.prepareStatement(sql);
            stmt.setString(1, login);

            // Do the request
            rs = stmt.executeQuery();

            if (rs.next()) 
            {
                String storedPassword = rs.getString(__COLUMN_PASSWORD);
                String salt = rs.getString(__COLUMN_SALT);
                
                if (salt == null && _isMD5Encrypted(storedPassword))
                {
                    String encryptedPassword = org.ametys.core.util.StringUtils.md5Base64(password);
                    
                    if (encryptedPassword == null)
                    {
                        getLogger().error("Unable to encrypt password");
                        return false;
                    }
                    
                    if (storedPassword.equals(encryptedPassword))
                    {
                        updateNeeded = true;
                        return true;
                    }
                    
                    return false;
                }
                else
                {
                    String encryptedPassword = DigestUtils.sha512Hex(salt + password);
                    
                    if (encryptedPassword == null)
                    {
                        getLogger().error("Unable to encrypt password");
                        return false;
                    }
                    
                    return storedPassword.equals(encryptedPassword);
                }
            }
            
            return false;
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
            return false;
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
            
            if (updateNeeded)
            {
                _updateToSSHAPassword(login, password);
            }
        }
    }

    @Override
    public void add(Map<String, String> userInformation) throws InvalidModificationException
    {
        Connection con = null;
        PreparedStatement stmt = null;

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting adding a new user");
        }
        
        // Check the presence of all parameters
        Map<String, Errors> errorFields = validate(userInformation);
        
        if (errorFields.size() > 0)
        {
            throw new InvalidModificationException("The creation of user failed because of invalid parameter values", errorFields);
        }
        
        String login = userInformation.get("login");

        try
        {
            // Connect to the database with connection pool
            con = getSQLConnection();

            stmt = createAddStatement(con, userInformation);

            // Do the request and check the result
            if (stmt.executeUpdate() != 1)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("The user to remove '" + login + "' was not removed.");
                }
                throw new InvalidModificationException("Error no user inserted");
            }

            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(ObservationConstants.ARGS_USER, new UserIdentity(login, _populationId));
            getObservationManager().notify(new Event(ObservationConstants.EVENT_USER_ADDED, getCurrentUserProvider().getUser(), eventParams));
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            throw new InvalidModificationException("Error during the communication with the database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
        
    }
    
    @Override
    public Map<String, Errors> validate(Map<String, String> userInformation)
    {
        Map<String, Errors> errorFields = new HashMap<>();
        for (JdbcParameter parameter : _model.values())
        {
            String untypedvalue = userInformation.get(parameter.getId());
            Object typedvalue = ParameterHelper.castValue(untypedvalue, parameter.getType());
            Validator validator = parameter.getValidator();
            
            if (validator != null)
            {
                Errors errors = new Errors();
                validator.validate(typedvalue, errors);
                
                if (errors.hasErrors())
                {
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("The field '" + parameter.getId() + "' is not valid");
                    }
                    
                    errorFields.put(parameter.getId(), errors);
                }
            }
        }
        return errorFields;
    }

    @Override
    public void update(Map<String, String> userInformation) throws InvalidModificationException
    {
        Connection con = null;
        PreparedStatement stmt = null;
        
        Map<String, Errors> errorFields = new HashMap<>();
        for (String id : userInformation.keySet())
        {
            JdbcParameter parameter = _model.get(id);
            if (parameter != null)
            {
                String untypedvalue = userInformation.get(parameter.getId());
                Object typedvalue = ParameterHelper.castValue(untypedvalue, parameter.getType());
                Validator validator = parameter.getValidator();
                
                if (validator != null)
                {
                    Errors errors = new Errors();
                    validator.validate(typedvalue, errors);
                    
                    if (errors.hasErrors())
                    {
                        if (getLogger().isDebugEnabled())
                        {
                            getLogger().debug("The field '" + parameter.getId() + "' is not valid");
                        }
                        
                        errorFields.put(parameter.getId(), errors);
                    }
                }
            }
        }
        
        if (errorFields.size() > 0)
        {
            throw new InvalidModificationException("The modification of user failed because of invalid parameter values", errorFields);
        }

        String login = userInformation.get("login");
        if (StringUtils.isEmpty(login))
        {
            throw new InvalidModificationException("Cannot update without login information");
        }

        try
        {
            // Connect to the database with connection pool
            con = getSQLConnection();

            stmt = createModifyStatement(con, userInformation);

            // Do the request
            if (stmt.executeUpdate() != 1)
            {
                throw new InvalidModificationException("Error. User '" + login + "' not updated");
            }

            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(ObservationConstants.ARGS_USER, new UserIdentity(login, _populationId));
            getObservationManager().notify(new Event(ObservationConstants.EVENT_USER_UPDATED, getCurrentUserProvider().getUser(), eventParams));
            
            if (isCacheEnabled())
            {
                removeObjectFromCache(login);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            throw new InvalidModificationException("Error communication with database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }

    @Override
    public void remove(String login) throws InvalidModificationException
    {
        Connection con = null;
        PreparedStatement stmt = null;

        try
        {
            // Connect to the database with connection pool
            con = getSQLConnection();

            // Build request for removing the user
            String sqlRequest = "DELETE FROM " + _userTableName + " WHERE " + __COLUMN_LOGIN + " = ?";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest);
            }

            stmt = con.prepareStatement(sqlRequest);
            stmt.setString(1, login);

            // Do the request and check the result
            if (stmt.executeUpdate() != 1)
            {
                throw new InvalidModificationException("Error user was not deleted");
            }

            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(ObservationConstants.ARGS_USER, new UserIdentity(login, _populationId));
            getObservationManager().notify(new Event(ObservationConstants.EVENT_USER_DELETED, getCurrentUserProvider().getUser(), eventParams));
            
            if (isCacheEnabled())
            {
                removeObjectFromCache(login);
            }
        }
        catch (SQLException e)
        {
            throw new InvalidModificationException("Error during the communication with the database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }
    
    @Override
    public Collection< ? extends Parameter<ParameterType>> getModel()
    {
        return Collections.unmodifiableCollection(_model.values());
    }
    
    /**
     * Configure the edition model parameters
     */
    protected void configureModelParameters()
    {
        _validatorManager = new ThreadSafeComponentManager<>();
        _validatorManager.setLogger(getLogger());
        _validatorManager.contextualize(_context);
        _validatorManager.service(_manager);
        
        _enumeratorManager = new ThreadSafeComponentManager<>();
        _enumeratorManager.setLogger(getLogger());
        _enumeratorManager.contextualize(_context);
        _enumeratorManager.service(_manager);
        
        _model = new LinkedHashMap<>();
        
        JdbcParameterParser jdbcParameterParser = new JdbcParameterParser(_enumeratorManager, _validatorManager);

        for (String column : __COLUMNS)
        {
            JdbcParameter parameter = _configureParameter(jdbcParameterParser, column, column);
            if (parameter != null)
            {
                _model.put(parameter.getId(), parameter);
            }
        }

        try
        {
            jdbcParameterParser.lookupComponents();
        }
        catch (Exception e)
        {
            getLogger().error("Unable to lookup parameter local components", e);
        }
    }
    
    /**
     * Configure the parameter (for special handling)
     * @param jdbcParameterParser the {@link JdbcParameter} parser.
     * @param id Id the of the parameter
     * @param column Column name of the parameter
     * @return The parameter created
     */
    protected JdbcParameter _configureParameter(JdbcParameterParser jdbcParameterParser, String id, final String column)
    {
        JdbcParameter parameter = new JdbcParameter();
        
        String catalog = "plugin." + BASE_PLUGIN_NAME;
        
        if ("password".equals(id))
        {
            parameter.setPluginName(BASE_PLUGIN_NAME);
            parameter.setLabel(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_PASSWORD_LABEL"));
            parameter.setDescription(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_PASSWORD_DESCRIPTION"));
            parameter.setType(ParameterType.PASSWORD);
            parameter.setValidator(new DefaultValidator(null, true));
        }
        else if ("login".equals(id))
        {
            parameter.setPluginName(BASE_PLUGIN_NAME);
            parameter.setLabel(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_LABEL"));
            parameter.setDescription(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_DESCRIPTION"));
            parameter.setType(ParameterType.STRING);
            I18nizableText invalidText = new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_INVALID");
            parameter.setValidator(new DefaultValidator("^[a-zA-Z0-9_\\-\\.@]{3,64}$", invalidText, true));
        }
        else if ("lastname".equals(id))
        {
            parameter.setPluginName(BASE_PLUGIN_NAME);
            parameter.setLabel(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_LASTNAME_LABEL"));
            parameter.setDescription(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_LASTNAME_DESCRIPTION"));
            parameter.setType(ParameterType.STRING);
            parameter.setValidator(new DefaultValidator(null, true));
        }
        else if ("firstname".equals(id))
        {
            parameter.setPluginName(BASE_PLUGIN_NAME);
            parameter.setLabel(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_FIRSTNAME_LABEL"));
            parameter.setDescription(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_FIRSTNAME_DESCRIPTION"));
            parameter.setType(ParameterType.STRING);
            parameter.setValidator(new DefaultValidator(null, true));
        }
        else if ("email".equals(id))
        {
            parameter.setPluginName(BASE_PLUGIN_NAME);
            parameter.setLabel(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_LABEL"));
            parameter.setDescription(new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_DESCRIPTION"));
            parameter.setType(ParameterType.STRING);
            I18nizableText invalidText = new I18nizableText(catalog, "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_INVALID");
            parameter.setValidator(new DefaultValidator("^([\\w\\-\\.])+@([\\w\\-\\.])+\\.([a-zA-Z])+$", invalidText, false));
        }
        else
        {
            return null;
        }
        
        parameter.setId(id);
        parameter.setColumn(column);
        
        return parameter;
    }

    /**
     * Get the mandatory predicate to use when querying users by pattern.
     * @param pattern The pattern to match, can be null.
     * @return a {@link JdbcPredicate}, can be null.
     */
    protected JdbcPredicate _getMandatoryPredicate(String pattern)
    {
        return null;
    }
    
    /**
     * Get the pattern to match user login
     * @param pattern the pattern
     * @return the pattern to match user login
     */
    protected String _getPatternToMatch(String pattern)
    {
        if (pattern != null)
        {
            return "%" + pattern + "%";
        }
        return null;
    }
    
    /**
     * Determines if the password is encrypted with MD5 algorithm
     * @param password The encrypted password
     * @return true if the password is encrypted with MD5 algorithm
     */
    protected boolean _isMD5Encrypted(String password)
    {
        return password.length() == 24;
    }
    
    /**
     * Generate a salt key and encrypt the password with the sha2 algorithm
     * @param login The user login
     * @param password The user pasword
     */
    protected void _updateToSSHAPassword(String login, String password)
    {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            con = getSQLConnection();

            String generateSaltKey = RandomStringUtils.randomAlphanumeric(48);
            String newEncryptedPassword = DigestUtils.sha512Hex(generateSaltKey + password);

            String sqlUpdate = "UPDATE " + _userTableName + " SET " + __COLUMN_PASSWORD + " = ?, " + __COLUMN_SALT + " = ? WHERE " + __COLUMN_LOGIN + " = ?";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlUpdate);
            }

            stmt = con.prepareStatement(sqlUpdate);
            stmt.setString(1, newEncryptedPassword);
            stmt.setString(2, generateSaltKey);
            stmt.setString(3, login);

            stmt.execute();
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }
     
     /**
      * Create Add statement
      * @param con The sql connection
      * @param userInformation the user informations
      * @return The statement
      * @throws SQLException if an error occurred
      */
    protected PreparedStatement createAddStatement(Connection con, Map<String, String> userInformation) throws SQLException
    {
        String beginClause = "INSERT INTO " + _userTableName + " (";
        String middleClause = ") VALUES (";
        String endClause = ")";

        StringBuffer intoClause = new StringBuffer();
        StringBuffer valueClause = new StringBuffer();

        intoClause.append(__COLUMN_SALT);
        valueClause.append("?");

        for (String column : __COLUMNS)
        {
            intoClause.append(", " + column);
            valueClause.append(", ?");
        }

        String sqlRequest = beginClause + intoClause.toString() + middleClause + valueClause + endClause;
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(sqlRequest);
        }

        PreparedStatement stmt = con.prepareStatement(sqlRequest);

        int i = 1;
        String generatedSaltKey = RandomStringUtils.randomAlphanumeric(48);
        stmt.setString(i++, generatedSaltKey);

        for (String column : __COLUMNS)
        {
            if ("password".equals(column))
            {
                String encryptedPassword = DigestUtils.sha512Hex(generatedSaltKey + userInformation.get(column));
                if (encryptedPassword == null)
                {
                    String message = "Cannot encode password";
                    getLogger().error(message);
                    throw new SQLException(message);
                }
                stmt.setString(i++, encryptedPassword);
            }
            else
            {
                stmt.setString(i++, userInformation.get(column));
            }
        }

        return stmt;
    }
     
     /**
      * Create statement to update database
      * @param con The sql connection
      * @param userInformation The user information
      * @return The statement
      * @throws SQLException if an error occurred
      */
    protected PreparedStatement createModifyStatement(Connection con, Map<String, String> userInformation) throws SQLException
    {
        // Build request for editing the user
        String beginClause = "UPDATE " + _userTableName + " SET ";
        String endClause = " WHERE " + __COLUMN_LOGIN + " = ?";

        StringBuffer columnNames = new StringBuffer("");

        boolean passwordUpdate = false;
        for (String id : userInformation.keySet())
        {
            if (ArrayUtils.contains(__COLUMNS, id) && !"login".equals(id) && !("password".equals(id) && (userInformation.get(id) == null)))
            {
                if ("password".equals(id))
                {
                    passwordUpdate = true;
                }

                if (columnNames.length() > 0)
                {
                    columnNames.append(", ");
                }
                columnNames.append(id + " = ?");
            }
        }

        if (passwordUpdate)
        {
            columnNames.append(", " + __COLUMN_SALT + " = ?");
        }

        String sqlRequest = beginClause + columnNames.toString() + endClause;
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(sqlRequest);
        }

        PreparedStatement stmt = con.prepareStatement(sqlRequest);
        _fillModifyStatement(stmt, userInformation);

        return stmt;
    }
     
     /**
      * Fill the statement with the user informations
      * @param stmt The statement of the sql request
      * @param userInformation the user informations
      * @throws SQLException if an error occurred
      */
    protected void _fillModifyStatement(PreparedStatement stmt, Map<String, String> userInformation) throws SQLException
    {
        int index = 1;

        String generateSaltKey = RandomStringUtils.randomAlphanumeric(48);
        boolean passwordUpdate = false;

        for (String id : userInformation.keySet())
        {
            if (ArrayUtils.contains(__COLUMNS, id) && !"login".equals(id))
            {
                if ("password".equals(id))
                {
                    if (userInformation.get(id) != null)
                    {
                        String encryptedPassword = DigestUtils.sha512Hex(generateSaltKey + userInformation.get(id));
                        if (encryptedPassword == null)
                        {
                            String message = "Cannot encrypt password";
                            getLogger().error(message);
                            throw new SQLException(message);
                        }
                        stmt.setString(index++, encryptedPassword);
                        passwordUpdate = true;
                    }
                }
                else
                {
                    stmt.setString(index++, userInformation.get(id));
                }
            }
        }

        if (passwordUpdate)
        {
            stmt.setString(index++, generateSaltKey);
        }

        stmt.setString(index++, userInformation.get("login"));
    }
     
     /**
      * Populate the user list with the result set
      * @param rs The result set
      * @return The user list
      * @throws SQLException If an SQL exception occurs
      */
    protected List<User> _getUsersProcessResultSet(ResultSet rs) throws SQLException
    {
        List<User> users = new ArrayList<>();

        while (rs.next())
        {
            User user = null;

            // Try to get in cache
            if (isCacheEnabled())
            {
                String login = rs.getString(__COLUMN_LOGIN);
                user = getObjectFromCache(login);
            }

            // Or create from result set
            if (user == null)
            {
                user = _createUserFromResultSet(rs);

                if (isCacheEnabled())
                {
                    addObjectInCache(user.getIdentity().getLogin(), user);
                }
            }

            users.add(user);
        }

        return users;
    }
     
     /**
      * Create the user implementation from the result set of the request
      * 
      * @param rs The result set where you can use get methods
      * @return The user refleting the current cursor position in the result set
      * @throws SQLException if an error occurred
      */
    protected User _createUserFromResultSet(ResultSet rs) throws SQLException
    {
        String login = rs.getString(__COLUMN_LOGIN);
        String lastName = rs.getString(__COLUMN_LASTNAME);
        String firstName = rs.getString(__COLUMN_FIRSTNAME);
        String email = rs.getString(__COLUMN_EMAIL);

        return new User(new UserIdentity(login, _populationId), lastName, firstName, email, this);
    }
     
     /**
      * Retrieve an user from a result set
      * @param rs The result set
      * @param login The user login
      * @return The retrieved user or null if not found
      * @throws SQLException If an SQL Exception occurs
      */
    protected User _getUserProcessResultSet(ResultSet rs, String login) throws SQLException
    {
        if (rs.next())
        {
            // Retrieve information of the user
            User user = _createUserFromResultSet(rs);

            if (isCacheEnabled())
            {
                addObjectInCache(login, user);
            }

            return user;
        }
        else
        {
            // no user with this login in the database
            return null;
        }
    }
     
     // ------------------------------
     //          INNER CLASSE
     // ------------------------------
     /**
      * An internal query executor.
      * @param <T> The type of the queried object
      */
    protected abstract class AbstractJdbcQueryExecutor<T>
    {
        /**
         * Main function, run the query process. Will not throw exception. Use
         * runWithException to catch non SQL exception thrown by
         * {@link #processResultSet(ResultSet)}
         * @return The queried object or null
         */
        @SuppressWarnings("synthetic-access")
        public T run()
        {
            try
            {
                return runWithException();
            }
            catch (Exception e)
            {
                getLogger().error("Exception during a query execution", e);
                throw new RuntimeException("Exception during a query execution", e);
            }
        }

        /**
         * Main function, run the query process.
         * @return The queried object or null
         * @throws Exception All non SQLException will be thrown
         */
        @SuppressWarnings("synthetic-access")
        public T runWithException() throws Exception
        {
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try
            {
                connection = getSQLConnection();
                
                String sql = getSqlQuery(connection);

                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Executing SQL query: " + sql);
                }

                stmt = prepareStatement(connection, sql);
                rs = executeQuery(stmt);

                return processResultSet(rs);
            }
            catch (SQLException e)
            {
                getLogger().error("Error during the communication with the database", e);
                throw new RuntimeException("Error during the communication with the database", e);
            }
            finally
            {
                ConnectionHelper.cleanup(rs);
                ConnectionHelper.cleanup(stmt);
                ConnectionHelper.cleanup(connection);
            }
        }

        /**
         * Must return the SQL query to execute
         * @param connection The pool connection
         * @return The SQL query
         */
        protected abstract String getSqlQuery(Connection connection);

        /**
         * Prepare the statement to execute
         * @param connection The pool connection
         * @param sql The SQL query
         * @return The prepared statement, ready to be executed
         * @throws SQLException If an SQL Exception occurs
         */
        protected PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException
        {
            return connection.prepareStatement(sql);
        }

        /**
         * Execute the prepared statement and retrieves the result set.
         * @param stmt The prepared statement
         * @return The result set
         * @throws SQLException If an SQL Exception occurs 
         */
        protected ResultSet executeQuery(PreparedStatement stmt) throws SQLException
        {
            return stmt.executeQuery();
        }

        /**
         * Process the result set
         * @param rs The result set
         * @return The queried object or null
         * @throws SQLException If an SQL exception occurs
         * @throws Exception Other exception will be thrown when using {@link #runWithException()}
         */
        protected T processResultSet(ResultSet rs) throws SQLException, Exception
        {
            return null;
        }
    }

    /**
     * Query executor in order to select an user
     * @param <T> The type of the queried object
     */
    protected class SelectUserJdbcQueryExecutor<T> extends AbstractJdbcQueryExecutor<T>
    {
        /** The user login */
        protected String _login;

        /** 
         * The constructor
         * @param login The user login
         */
        protected SelectUserJdbcQueryExecutor(String login)
        {
            _login = login;
        }

        @Override
        protected String getSqlQuery(Connection connection)
        {
            // Build SQL request
            StringBuilder selectClause = new StringBuilder();
            for (String id : __COLUMNS)
            {
                if (selectClause.length() > 0)
                {
                    selectClause.append(", ");
                }
                selectClause.append(id);
            }

            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(selectClause).append(" FROM ").append(_userTableName);
            sql.append(" WHERE login = ?");

            return sql.toString();
        }

        @Override
        protected PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException
        {
            PreparedStatement stmt = super.prepareStatement(connection, sql);

            stmt.setString(1, _login);
            return stmt;
        }
    }
     
    /**
     * Query executor in order to select users
     * @param <T> The type of the queried object
     */
    protected class SelectUsersJdbcQueryExecutor<T> extends AbstractJdbcQueryExecutor<T>
    {
        /** The pattern to match (none if null) */
        protected String _pattern;
        /** The maximum number of users to select */
        protected int _length;
        /** The offset to start with, first is 0 */
        protected int _offset;

        /** The mandatory predicate to use when querying users by pattern */
        protected JdbcPredicate _mandatoryPredicate;
        /** The pattern to match, extracted from the pattern */
        protected String _patternToMatch;

        /** 
         * The constructor
         * @param pattern The pattern to match (none if null).
         * @param length The maximum number of users to select.
         * @param offset The offset to start with, first is 0.
         */
        protected SelectUsersJdbcQueryExecutor(String pattern, int length, int offset)
        {
            _pattern = pattern;
            _length = length;
            _offset = offset;
        }

        @Override
        protected String getSqlQuery(Connection connection)
        {
            // Build SQL request
            StringBuilder selectClause = new StringBuilder();
            for (String column : __COLUMNS)
            {
                if (selectClause.length() > 0)
                {
                    selectClause.append(", ");
                }
                selectClause.append(column);
            }

            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(selectClause).append(" FROM ").append(_userTableName);

            // Add the pattern
            _mandatoryPredicate = _getMandatoryPredicate(_pattern);
            if (_mandatoryPredicate != null)
            {
                sql.append(" WHERE ").append(_mandatoryPredicate.getPredicate());
            }

            _patternToMatch = _getPatternToMatch(_pattern);
            if (_patternToMatch != null)
            {
                if (ConnectionHelper.getDatabaseType(connection) == ConnectionHelper.DATABASE_DERBY)
                {
                    // The LIKE operator in Derby is case sensitive
                    sql.append(_mandatoryPredicate != null ? " AND (" : " WHERE ")
                    .append("UPPER(").append(__COLUMN_LOGIN).append(") LIKE UPPER(?) OR ")
                    .append("UPPER(").append(__COLUMN_LASTNAME).append(") LIKE UPPER(?) OR ")
                    .append("UPPER(").append(__COLUMN_FIRSTNAME).append(") LIKE UPPER(?)");
                }
                else
                {
                    sql.append(_mandatoryPredicate != null ? " AND (" : " WHERE ")
                    .append(__COLUMN_LOGIN).append(" LIKE ? OR ")
                    .append(__COLUMN_LASTNAME).append(" LIKE ? OR ")
                    .append(__COLUMN_FIRSTNAME).append(" LIKE ?");
                }

                if (_mandatoryPredicate != null)
                {
                    sql.append(')');
                }
            }

            // Add length filters
            sql = _addQuerySize(_length, _offset, connection, selectClause, sql);

            return sql.toString();
        }

        @SuppressWarnings("synthetic-access")
        private StringBuilder _addQuerySize(int length, int offset, Connection con, StringBuilder selectClause, StringBuilder sql)
        {
            // Do not add anything if not necessary
            if (length == Integer.MAX_VALUE && offset == 0)
            {
                return sql;
            }

            String dbType = ConnectionHelper.getDatabaseType(con);

            if (ConnectionHelper.DATABASE_MYSQL.equals(dbType) || ConnectionHelper.DATABASE_POSTGRES.equals(dbType) || ConnectionHelper.DATABASE_HSQLDB.equals(dbType))
            {
                sql.append(" LIMIT " + length + " OFFSET " + offset);
                return sql;
            }
            else if (ConnectionHelper.DATABASE_ORACLE.equals(dbType))
            {
                return new StringBuilder("select " + selectClause.toString() + " from (select rownum r, " + selectClause.toString() + " from (" + sql.toString()
                        + ")) where r BETWEEN " + (offset + 1) + " AND " + (offset + length));
            }
            else if (ConnectionHelper.DATABASE_DERBY.equals(dbType))
            {
                return new StringBuilder("select ").append(selectClause)
                        .append(" from (select ROW_NUMBER() OVER () AS ROWNUM, ").append(selectClause.toString())
                        .append(" from (").append(sql.toString()).append(") AS TR ) AS TRR where ROWNUM BETWEEN ")
                        .append(offset + 1).append(" AND ").append(offset + length);
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The request will not have the limit and offset set, since its type is unknown");
            }

            return sql;
        }

        @Override
        protected PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException
        {
            PreparedStatement stmt = super.prepareStatement(connection, sql);

            int i = 1;
            // Value the parameters if there is a pattern
            if (_mandatoryPredicate != null)
            {
                for (String value : _mandatoryPredicate.getValues())
                {
                    stmt.setString(i++, value);
                }
            }

            if (_patternToMatch != null)
            {
                // One for the login, one for the lastname.
                stmt.setString(i++, _patternToMatch);
                stmt.setString(i++, _patternToMatch);
                // FIXME
                //if (_parameters.containsKey("firstname"))
                //{
                stmt.setString(i++, _patternToMatch);
                //}
            }

            return stmt;
        }
    }

    /**
     * {@link AbstractParameterParser} for parsing {@link JdbcParameter}.
     */
    protected static class JdbcParameterParser extends AbstractParameterParser<JdbcParameter, ParameterType>
    {
        JdbcParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
        {
            super(enumeratorManager, validatorManager);
        }

        @Override
        protected JdbcParameter _createParameter(Configuration parameterConfig) throws ConfigurationException
        {
            return new JdbcParameter();
        }

        @Override
        protected String _parseId(Configuration parameterConfig) throws ConfigurationException
        {
            return parameterConfig.getAttribute("id");
        }

        @Override
        protected ParameterType _parseType(Configuration parameterConfig) throws ConfigurationException
        {
            try
            {
                return ParameterType.valueOf(parameterConfig.getAttribute("type").toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new ConfigurationException("Invalid type", parameterConfig, e);
            }
        }

        @Override
        protected Object _parseDefaultValue(Configuration parameterConfig, JdbcParameter jdbcParameter)
        {
            String defaultValue = parameterConfig.getChild("default-value").getValue(null);
            return ParameterHelper.castValue(defaultValue, jdbcParameter.getType());
        }
    }

    /**
     * Class representing a SQL predicate (to use in a WHERE or HAVING clause),
     * with optional string parameters.
     */
    public class JdbcPredicate
    {

        /** The predicate string with optional "?" placeholders. */
        protected String _predicate;
        /** The predicate parameter values. */
        protected List<String> _predicateParamValues;

        /**
         * Build a JDBC predicate.
         * @param predicate the predicate string.
         * @param values the parameter values.
         */
        public JdbcPredicate(String predicate, String... values)
        {
            this(predicate, Arrays.asList(values));
        }

        /**
         * Build a JDBC predicate.
         * @param predicate the predicate string.
         * @param values the parameter values.
         */
        public JdbcPredicate(String predicate, List<String> values)
        {
            this._predicate = predicate;
            this._predicateParamValues = values;
        }

        /**
         * Get the predicate.
         * @return the predicate
         */
        public String getPredicate()
        {
            return _predicate;
        }

        /**
         * Set the predicate.
         * @param predicate the predicate to set
         */
        public void setPredicate(String predicate)
        {
            this._predicate = predicate;
        }

        /**
         * Get the parameter values.
         * @return the parameter values.
         */
        public List<String> getValues()
        {
            return _predicateParamValues;
        }

        /**
         * Set the parameter values.
         * @param values the parameter values to set.
         */
        public void setValues(List<String> values)
        {
            this._predicateParamValues = values;
        }
    }

}
