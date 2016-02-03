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
package org.ametys.plugins.core.impl.user.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.datasource.ConnectionHelper.DatabaseType;
import org.ametys.core.user.User;
import org.ametys.core.user.UsersManager;
import org.ametys.core.util.CachingComponent;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.AbstractParameterParser;
import org.ametys.runtime.parameter.DefaultValidator;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Use a jdbc driver for getting the list of users.<br>
 * The main method to override is <code>_createUserFromResultSet</code>
 */
public class JdbcUsersManager extends CachingComponent<User> implements UsersManager, Configurable, ThreadSafe, Component, Serviceable, Contextualizable, PluginAware, Disposable
{
    /** The base plugin (for i18n key) */
    protected static final String BASE_PLUGIN_NAME = "core";

    /** Plugin name */
    protected String _pluginName;

    /** Connection pool */
    protected String _poolName;

    /** JDBC table name */
    protected String _tableName;

    /** Model */
    protected Map<String, JdbcParameter> _parameters;
    
    /** The avalon service manager */
    protected ServiceManager _manager;
    
    /** The avalon context */
    protected Context _context;
    
    // ComponentManager pour les Validator
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    //ComponentManager pour les Enumerator
    private ThreadSafeComponentManager<Enumerator> _enumeratorManager;
    
    public void setPluginInfo(String pluginName, String featureName)
    {
        _pluginName = pluginName;
    }
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _validatorManager = new ThreadSafeComponentManager<>();
        _validatorManager.setLogger(getLogger());
        _validatorManager.contextualize(_context);
        _validatorManager.service(_manager);
        
        _enumeratorManager = new ThreadSafeComponentManager<>();
        _enumeratorManager.setLogger(getLogger());
        _enumeratorManager.contextualize(_context);
        _enumeratorManager.service(_manager);
        
        _poolName = configuration.getChild("pool").getValue();
        _tableName = configuration.getChild("table").getValue("Users");

        _parameters = new LinkedHashMap<>();
        
        JdbcParameterParser jdbcParameterParser = new JdbcParameterParser(_enumeratorManager, _validatorManager);

        Configuration[] parametersConfigurations = configuration.getChildren("param");
        for (Configuration parameterConfiguration : parametersConfigurations)
        {
            String id = parameterConfiguration.getAttribute("id");
            String column = parameterConfiguration.getAttribute("column", id);

            JdbcParameter parameter = _configureParameter(jdbcParameterParser, id, column, parameterConfiguration);
            _parameters.put(parameter.getId(), parameter);
        }

        try
        {
            jdbcParameterParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to lookup parameter local components", configuration, e);
        }

        if (!_parameters.containsKey("login"))
        {
            throw new ConfigurationException("Missing the mandatory parameter 'login'", configuration);
        }
        if (!_parameters.containsKey("lastname"))
        {
            throw new ConfigurationException("Missing the mandatory parameter 'lastname'", configuration);
        }
        if (!_parameters.containsKey("email"))
        {
            throw new ConfigurationException("Missing the mandatory parameter 'email'", configuration);
        }
    }
    
    public void dispose()
    {
        _enumeratorManager.dispose();
        _enumeratorManager = null;
        
        _validatorManager.dispose();
        _validatorManager = null;
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection ()
    {
        String dataSourceId = Config.getInstance().getValueAsString(_poolName);
        return ConnectionHelper.getConnection(dataSourceId);
    }

    /**
     * Configure the parameter (for special handling)
     * @param jdbcParameterParser the {@link JdbcParameter} parser.
     * @param id Id the of the parameter
     * @param column Column name of the parameter
     * @param configuration Configuration of the parameter
     * @return The parameter created
     * @throws ConfigurationException if a configuration problem occurs
     */
    protected JdbcParameter _configureParameter(JdbcParameterParser jdbcParameterParser, String id, final String column, Configuration configuration) throws ConfigurationException
    {
        JdbcParameter parameter = new JdbcParameter();
        
        String catalog = "plugin." + BASE_PLUGIN_NAME;
        
        try
        {
            if ("login".equals(id))
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
                parameter = jdbcParameterParser.parseParameter(_manager, _pluginName, configuration);
            }
            
            parameter.setId(id);
            parameter.setColumn(column);
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Configuration for parameter '" + id + "' is invalid", configuration, e);
        }
        
        return parameter;
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
        String login = rs.getString(_parameters.get("login").getColumn());
        String lastName = rs.getString(_parameters.get("lastname").getColumn());
        String firstName = _parameters.containsKey("firstname") ? rs.getString(_parameters.get("firstname").getColumn()) : null;
        String email = rs.getString(_parameters.get("email").getColumn());

        return new User(login, lastName, firstName, email);
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
        
        SelectUsersJdbcQueryExecutor<List<User>> queryExecutor = new SelectUsersJdbcQueryExecutor<List<User>>(pattern, boundedCount, boundedOffset) {
            @Override
            protected List<User> processResultSet(ResultSet rs) throws SQLException
            {
                return _getUsersProcessResultSet(rs);
            }
        };
        
        return queryExecutor.run();
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
                String login = rs.getString(_parameters.get("login").getColumn());
                user = getObjectFromCache(login);
            }
            
            // Or create from result set
            if (user == null)
            {
                user = _createUserFromResultSet(rs);
                
                if (isCacheEnabled())
                {
                    addObjectInCache(user.getName(), user);
                }
            }
            
            users.add(user);
        }
        
        return users;
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
        
        SelectUserJdbcQueryExecutor<User> queryExecutor = new SelectUserJdbcQueryExecutor<User>(login) {
            @Override
            protected User processResultSet(ResultSet rs) throws SQLException
            {
                return _getUserProcessResultSet(rs, login);
            }
        };
        
        return queryExecutor.run();
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
            // Récupérer les informations sur l'utilisateur
            User user = _createUserFromResultSet(rs);
            
            if (isCacheEnabled())
            {
                addObjectInCache(login, user);
            }
            
            return user;
        }
        else
        {
            // aucun utilisateur avec ce login n'existe dans la base
            return null;
        }
    }
    
    @Override
    @Deprecated
    public List<Map<String, Object>> users2JSON(int count, int offset, Map parameters)
    {
        String pattern = StringUtils.defaultIfEmpty((String) parameters.get("pattern"), null);
        
        return internalUsers2JSON(pattern, count >= 0 ? count : Integer.MAX_VALUE, offset >= 0 ? offset : 0);
    }
    
    /**
     * Get all users as an object
     * @param pattern The search pattern
     * @param length The max results
     * @param offset The offset in results
     * @return The list of corresponding users objects
     */
    @Deprecated
    protected List<Map<String, Object>> internalUsers2JSON (String pattern, int length, int offset)
    {
        SelectUsersJdbcQueryExecutor<List<Map<String, Object>>> queryExecutor = new SelectUsersJdbcQueryExecutor<List<Map<String, Object>>>(pattern, length, offset) {
            
            @Override
            protected List<Map<String, Object>> processResultSet(ResultSet rs) throws SQLException
            {
                List<Map<String, Object>> users = new ArrayList<>();
                
                while (rs.next())
                {
                    users.add(resultSetToJson(rs));
                }
                
                return users;
            }
        };
        
        return queryExecutor.run();
    }
    
    /**
     * Sax the user list.
     * @param handler The content handler to sax in.
     * @param count The maximum number of users to sax.
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters containing a pattern to match :
     *            <ul>
     *            <li>"pattern" =&gt; The pattern to match (String) or null to get all the users.</li>
     *            </ul>
     * @throws SAXException If an error occurs while saxing.
     */
    @Override
    @Deprecated
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException
    {
        String pattern = StringUtils.defaultIfEmpty((String) parameters.get("pattern"), null);
        
        XMLUtils.startElement(handler, "users");

        toSAXInternal(handler, pattern, count >= 0 ? count : Integer.MAX_VALUE, offset >= 0 ? offset : 0);

        // Récupérer le nombre total d'utilisateurs correspondant au motif
        String total = Integer.toString(getUsersCount(pattern));
        XMLUtils.createElement(handler, "total", total);

        XMLUtils.endElement(handler, "users");
    }
    
    /**
     * Sax the user list.
     * 
     * @param handler The content handler to sax in.
     * @param pattern The pattern to match (none if null).
     * @param length The maximum number of users to sax.
     * @param offset The offset to start with, first is 0.
     * @throws SAXException If an error occurs while saxing.
     */
    @Deprecated
    public void toSAXInternal(ContentHandler handler, String pattern, int length, int offset) throws SAXException
    {
        SelectUsersJdbcQueryExecutor queryExecutor = new SelectUsersJdbcQueryExecutor(pattern, length, offset) {
            @Override
            protected Void processResultSet(ResultSet rs) throws SQLException, SAXException
            {
                while (rs.next())
                {
                    resultSetToSAX(handler, rs);
                }
                return null;
            }
        };
        
        try
        {
            queryExecutor.runWithException();
        }
        catch (SAXException e)
        {
            // throw SAX Exception
            throw e;
        }
        catch (Exception e)
        {
            getLogger().error("Unexpected error during the SAXing of the user list", e);
            throw new RuntimeException("Unexpected error during the SAXing of the user list", e);
        }
    }
    
    /**
     * Get attributes from a result set
     * @param attr the attributes
     * @param rs The result set to sax
     * @throws SQLException If an error occurs while getting result information.
     */
    @Deprecated
    protected void getUserAttributesFromResultSet (AttributesImpl attr, ResultSet rs) throws SQLException
    {
        attr.addCDATAAttribute("login", rs.getString(_parameters.get("login").getColumn()));
    }

    /**
     * Sax a result set from a database.
     * 
     * @param handler The content handler to sax in.
     * @param rs The result set to sax.
     * @throws SAXException If an error occurs while saxing.
     * @throws SQLException If an error occurs while getting result information.
     */
    @Deprecated
    protected void resultSetToSAX(ContentHandler handler, ResultSet rs) throws SAXException, SQLException
    {
        AttributesImpl attr = new AttributesImpl();
        getUserAttributesFromResultSet (attr, rs);
        XMLUtils.startElement(handler, "user", attr);

        for (String id : _parameters.keySet())
        {
            if (!"login".equals(id))
            {
                JdbcParameter parameter = _parameters.get(id);
                Object typedValue; 
                
                if (parameter.getType() == ParameterType.BOOLEAN)
                {
                    typedValue = rs.getBoolean(parameter.getColumn());
                }
                else if (parameter.getType() == ParameterType.DATE)
                {
                    java.sql.Date date = rs.getDate(parameter.getColumn());
                    typedValue = date != null ? new Date(date.getTime()) : null;
                }
                else if (parameter.getType() == ParameterType.DOUBLE)
                {
                    typedValue = rs.getDouble(parameter.getColumn());
                }
                else if (parameter.getType() == ParameterType.LONG)
                {
                    typedValue = rs.getLong(parameter.getColumn());
                }
                else if (parameter.getType() == ParameterType.PASSWORD)
                {
                    typedValue = "PASSWORD";
                }
                else
                {
                    typedValue = rs.getString(parameter.getColumn());
                }
     
                String valueAsString = ParameterHelper.valueToString(typedValue);
                
                XMLUtils.createElement(handler, parameter.getId(), valueAsString != null ? valueAsString : "");
            }
        }

        XMLUtils.endElement(handler, "user");
    }
    
    /**
     * Get the result set from a database.
     * @param rs The result set to sax.
     * @return The map of values 
     * @throws SQLException If an error occurs while getting result information.
     */
    @Deprecated
    protected Map<String, Object> resultSetToJson(ResultSet rs) throws SQLException
    {
        Map<String, Object> user = new HashMap<>();
        
        for (String id : _parameters.keySet())
        {
            JdbcParameter parameter = _parameters.get(id);
            
            if (parameter.getType() == ParameterType.BOOLEAN)
            {
                user.put(parameter.getColumn(), rs.getBoolean(parameter.getColumn()));
            }
            else if (parameter.getType() == ParameterType.DATE)
            {
                java.sql.Date date = rs.getDate(parameter.getColumn());
                if (date != null)
                {
                    user.put(parameter.getColumn(), ParameterHelper.valueToString(new Date(date.getTime())));
                }
            }
            else if (parameter.getType() == ParameterType.DOUBLE)
            {
                user.put(parameter.getColumn(), rs.getDouble(parameter.getColumn()));
            }
            else if (parameter.getType() == ParameterType.LONG)
            {
                user.put(parameter.getColumn(), rs.getLong(parameter.getColumn()));
            }
            else if (parameter.getType() == ParameterType.PASSWORD)
            {
                user.put(parameter.getColumn(), "PASSWORD");
            }
            else
            {
                user.put(parameter.getColumn(), rs.getString(parameter.getColumn()));
            }
        }
        
        return user;
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
     * Get the number of users matching a pattern.
     * @param pattern The pattern to match (may be null).
     * @return The number of users matching a pattern.
     */
    public int getUsersCount(String pattern)
    {
        AbstractJdbcQueryExecutor<Integer> queryExecutor = new AbstractJdbcQueryExecutor<Integer>()
        {
            @Override
            protected String getSqlQuery(Connection connection)
            {
                return _getUserCountSqlQuery(pattern);
            }
            
            @Override
            protected PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException
            {
                PreparedStatement stmt = super.prepareStatement(connection, sql);
                return _getUserCountPrepareStatement(stmt, pattern);
            }
            
            @Override
            protected Integer processResultSet(ResultSet rs) throws SQLException
            {
                return rs.next() ? rs.getInt(1) : 0;
            }
        };
        
        return queryExecutor.run();
    }
    
    /**
     * Return the SQL query to get the user count
     * @param pattern The pattern to match (may be null).
     * @return The SQL query
     */
    protected String _getUserCountSqlQuery(String pattern)
    {
        // Contruire la requête pour récupérer l'éventuel utilisateur
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) FROM " + _tableName);

        if (pattern != null)
        {
            sql.append(" WHERE " + _parameters.get("login").getColumn() + " LIKE ? ");
            sql.append(" OR " + _parameters.get("lastname").getColumn() + " LIKE ? ");
            if (_parameters.containsKey("firstname"))
            {
                sql.append(" OR " + _parameters.get("firstname").getColumn() + " LIKE ? ");
            }
        }
        
        return sql.toString();
    }
    
    /**
     * Prepare the statement for the get user count SQL query
     * @param stmt The prepared statement
     * @param pattern The pattern to match (may be null).
     * @return The prepared statement
     * @throws SQLException If an SQL Exception occurs
     */
    protected PreparedStatement _getUserCountPrepareStatement(PreparedStatement stmt, String pattern) throws SQLException
    {
        if (pattern != null)
        {
            int i = 1;
            String patternToMatch = "%" + pattern + "%";

            stmt.setString(i++, patternToMatch);
            stmt.setString(i++, patternToMatch);
            if (_parameters.containsKey("firstname"))
            {
                stmt.setString(i++, patternToMatch);
            }
        }
        
        return stmt;
    }

    @Override
    @Deprecated
    public Map<String, Object> user2JSON(String login)
    {
        SelectUserJdbcQueryExecutor<Map<String, Object>> queryExecutor = new SelectUserJdbcQueryExecutor<Map<String, Object>>(login) {
            @Override
            protected Map<String, Object> processResultSet(ResultSet rs) throws SQLException
            {
                if (rs.next())
                {
                    return resultSetToJson(rs);
                }
                return null;
            }
        };
        
        return queryExecutor.run();
    }
    
    @Override
    @Deprecated
    public void saxUser(String login, ContentHandler handler) throws SAXException
    {
        SelectUserJdbcQueryExecutor queryExecutor = new SelectUserJdbcQueryExecutor(login) {
            @Override
            protected Void processResultSet(ResultSet rs) throws SQLException, SAXException
            {
                if (rs.next())
                {
                    resultSetToSAX(handler, rs);
                }
                return null;
            }
        };
        
        try
        {
            queryExecutor.runWithException();
        }
        catch (SAXException e)
        {
            // throw SAX Exception
            throw e;
        }
        catch (Exception e)
        {
            String errorMsg = String.format("Unexpected error during the SAXing of an user with login '%s'", login);
            getLogger().error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
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
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try
            {
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
            for (String id : _parameters.keySet())
            {
                JdbcParameter parameter = _parameters.get(id);
                if (selectClause.length() > 0)
                {
                    selectClause.append(", ");
                }
                selectClause.append(parameter.getColumn());
            }

            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(selectClause).append(" FROM ").append(_tableName);
            sql.append(" WHERE ").append(_parameters.get("login").getColumn()).append(" = ?");
            
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
            for (String id : _parameters.keySet())
            {
                JdbcParameter parameter = _parameters.get(id);
                if (selectClause.length() > 0)
                {
                    selectClause.append(", ");
                }
                selectClause.append(parameter.getColumn());
            }

            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(selectClause).append(" FROM ").append(_tableName);
            
            // Ajoute le pattern
            _mandatoryPredicate = _getMandatoryPredicate(_pattern);
            if (_mandatoryPredicate != null)
            {
                sql.append(" WHERE ").append(_mandatoryPredicate.getPredicate());
            }
            
            _patternToMatch = _getPatternToMatch(_pattern);
            if (_patternToMatch != null)
            {
                sql.append(_mandatoryPredicate != null ? " AND (" : " WHERE ")
                    .append(_parameters.get("login").getColumn()).append(" LIKE ? OR ")
                    .append(_parameters.get("lastname").getColumn()).append(" LIKE ?");
                if (_parameters.containsKey("firstname"))
                {
                    sql.append(" OR ").append(_parameters.get("firstname").getColumn()).append(" LIKE ?");
                }
                if (_mandatoryPredicate != null)
                {
                    sql.append(')');
                }
            }
            
            // Ajoute les filtres de taille
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
            
            DatabaseType datatype = ConnectionHelper.getDatabaseType(con);
            
            if (datatype == DatabaseType.DATABASE_MYSQL || datatype == DatabaseType.DATABASE_POSTGRES || datatype == DatabaseType.DATABASE_HSQLDB)
            {
                sql.append(" LIMIT " + length + " OFFSET " + offset);
                return sql;
            }
            else if (datatype == DatabaseType.DATABASE_ORACLE)
            {
                return new StringBuilder("select " + selectClause.toString() + " from (select rownum r, " + selectClause.toString() + " from (" + sql.toString()
                        + ")) where r BETWEEN " + (offset + 1) + " AND " + (offset + length));
            }
            else if (datatype == DatabaseType.DATABASE_DERBY)
            {
                return new StringBuilder("select ").append(selectClause)
                        .append(" from (select ROW_NUMBER() OVER () AS ROWNUM, ").append(selectClause.toString())
                        .append(" from (").append(sql.toString()).append(") AS TR ) AS TRR where ROWNUM BETWEEN ")
                        .append(offset + 1).append(" AND ").append(offset + length);
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The request will not have the limit and offet set, since its type is unknown");
            }
            
            return sql;
        }
        
        @Override
        protected PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException
        {
            PreparedStatement stmt = super.prepareStatement(connection, sql);
            
            int i = 1;
            // Value les parametres s'il y a un pattern
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
                if (_parameters.containsKey("firstname"))
                {
                    stmt.setString(i++, _patternToMatch);
                }
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
        protected List<String> _paramValues;
        
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
            this._paramValues = values;
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
            return _paramValues;
        }
        
        /**
         * Set the parameter values.
         * @param values the parameter values to set.
         */
        public void setValues(List<String> values)
        {
            this._paramValues = values;
        }
    }
}
