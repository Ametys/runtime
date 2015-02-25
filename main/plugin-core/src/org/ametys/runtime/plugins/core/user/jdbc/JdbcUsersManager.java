/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.user.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.CachingComponent;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.AbstractParameterParser;
import org.ametys.runtime.util.parameter.DefaultValidator;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.util.parameter.Validator;

/**
 * Use a jdbc driver for getting the list of users.<br/>
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
        try
        {
            _validatorManager = new ThreadSafeComponentManager<Validator>();
            _validatorManager.enableLogging(getLogger());
            _validatorManager.contextualize(_context);
            _validatorManager.service(_manager);
            
            _enumeratorManager = new ThreadSafeComponentManager<Enumerator>();
            _enumeratorManager.enableLogging(getLogger());
            _enumeratorManager.contextualize(_context);
            _enumeratorManager.service(_manager);
        }
        catch (ServiceException e)
        {
            throw new ConfigurationException("Unable to create local component managers", configuration, e);
        }
        
        _poolName = configuration.getChild("pool").getValue();
        _tableName = configuration.getChild("table").getValue("Users");

        _parameters = new LinkedHashMap<String, JdbcParameter>();
        
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

        try
        {
            parameter.setId(id);
            parameter.setColumn(column);
            
            if ("login".equals(id))
            {
                parameter.setPluginName(BASE_PLUGIN_NAME);
                parameter.setLabel(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_LABEL"));
                parameter.setDescription(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_DESCRIPTION"));
                parameter.setType(ParameterType.STRING);
                parameter.setValidator(new DefaultValidator("^[a-zA-Z0-9_\\-\\.@]{3,64}$", true));
            }
            else if ("lastname".equals(id))
            {
                parameter.setPluginName(BASE_PLUGIN_NAME);
                parameter.setLabel(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_LASTNAME_LABEL"));
                parameter.setDescription(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_LASTNAME_DESCRIPTION"));
                parameter.setType(ParameterType.STRING);
                parameter.setValidator(new DefaultValidator(null, true));
            }
            else if ("firstname".equals(id))
            {
                parameter.setPluginName(BASE_PLUGIN_NAME);
                parameter.setLabel(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_FIRSTNAME_LABEL"));
                parameter.setDescription(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_FIRSTNAME_DESCRIPTION"));
                parameter.setType(ParameterType.STRING);
                parameter.setValidator(new DefaultValidator(null, true));
            }
            else if ("email".equals(id))
            {
                parameter.setPluginName(BASE_PLUGIN_NAME);
                parameter.setLabel(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_LABEL"));
                parameter.setDescription(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_DESCRIPTION"));
                parameter.setType(ParameterType.STRING);
                parameter.setValidator(new DefaultValidator("^([\\w\\-\\.])+@([\\w\\-\\.])+\\.([a-zA-Z])+$", false));
            }
            else
            {
                parameter = jdbcParameterParser.parseParameter(_manager, _pluginName, configuration);
            }
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Configuration for parameter '" + id + "' is invalid", configuration, e);
        }
        
        return parameter;
    }

    /**
     * Create the inner part of the select clause of the sql request to retrive
     * all users
     * 
     * @return A sql list of columns (eg : 'login, lastname, email')
     */
    protected String _createGetUserSelectClause()
    {
        StringBuffer sql = new StringBuffer();

        for (JdbcParameter parameter : _parameters.values())
        {
            if (sql.length() > 0)
            {
                sql.append(", ");
            }     
            sql.append(parameter.getColumn());
        }

        return sql.toString();
    }

    /**
     * Create the user implementation from the result set of the request
     * 
     * @param rs The result set where you can use get methods
     * @return The user refleting the current cursor position in the result set
     * @throws SQLException if an error occured
     */
    protected User _createUserFromResultSet(ResultSet rs) throws SQLException
    {
        String login = rs.getString(_parameters.get("login").getColumn());

        StringBuffer fullname = new StringBuffer();
        if (_parameters.containsKey("firstname"))
        {
            fullname.append(rs.getString(_parameters.get("firstname").getColumn()) + " ");
        }
        fullname.append(rs.getString(_parameters.get("lastname").getColumn()));

        String email = rs.getString(_parameters.get("email").getColumn());

        User user = new User(login, fullname.toString(), email);
        return user;
    }
    

    public Collection<User> getUsers()
    {
        // Créer une liste d'utilisateurs
        List<User> users = new ArrayList<User>();
        // Vérifier si l'initialisation s'est bien passée

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Effectuer la connexion à la base de données via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour récupérer la liste des utilisateurs
            String sqlRequest = "SELECT " + _createGetUserSelectClause() + " FROM " + _tableName;
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest);
            }

            stmt = con.prepareStatement(sqlRequest);

            rs = stmt.executeQuery();

            // Remplir la liste des utilisateurs
            while (rs.next())
            {
                // Ajouter un nouveau principal à la liste
                User user = _createUserFromResultSet(rs);
                
                if (isCacheEnabled())
                {
                    addObjectInCache(user.getName(), user);
                }

                users.add(user);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the communication with the database", e);
            return Collections.emptySet();
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }

        // Retourner la liste des utilisateurs sous forme de collection
        // d'utilisateurs, éventuellement vide
        return users;
    }

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
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Effectuer la connexion à la base de données via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour récupérer l'éventuel utilisateur
            String sqlRequest = "SELECT " + _createGetUserSelectClause() + " FROM " + _tableName + " WHERE " + _parameters.get("login").getColumn() + " = ? ";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest + " [login:" + login + "]");
            }

            stmt = con.prepareStatement(sqlRequest);

            stmt.setString(1, login);

            // Effectuer la requête
            rs = stmt.executeQuery();
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
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            return null;
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }

    /**
     * Sax the user list.
     * 
     * @param handler The content handler to sax in.
     * @param count The maximum number of users to sax.
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters containing a pattern to match :
     *            <ul>
     *            <li>"pattern" => The pattern to match (String) or null to get
     *            all the users.
     *            </ul>
     * @throws SAXException If an error occurs while saxing.
     */
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException
    {
        String pattern = (String) parameters.get("pattern");

        if (pattern != null && pattern.length() == 0)
        {
            pattern = null;
        }

        XMLUtils.startElement(handler, "users");

        toSAXInternal(handler, pattern, count >= 0 ? count : Integer.MAX_VALUE, offset >= 0 ? offset : 0);

        // Récupérer le nombre total d'utilisateurs correspondant au motif
        String total = Integer.toString(getUsersCount(pattern));
        XMLUtils.createElement(handler, "total", total);

        XMLUtils.endElement(handler, "users");
    }
    
    /**
     * Get the pattern to match user login
     * @param pattern the pattern
     * @return the pattern to match user login
     */
    protected String _getPatternToMatch (String pattern)
    {
        if (pattern != null)
        {
            return "%" + pattern + "%";
        }
        return null;
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
     * Sax the user list.
     * 
     * @param handler The content handler to sax in.
     * @param pattern The pattern to match (none if null).
     * @param length The maximum number of users to sax.
     * @param offset The offset to start with, first is 0.
     * @throws SAXException If an error occurs while saxing.
     */
    public void toSAXInternal(ContentHandler handler, String pattern, int length, int offset) throws SAXException
    {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Effectuer la connexion à la base de données
            // via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour récupérer l'éventuel utilisateur
            StringBuffer selectClause = new StringBuffer();
            for (String id : _parameters.keySet())
            {
                JdbcParameter parameter = _parameters.get(id);
                if (selectClause.length() > 0)
                {
                    selectClause.append(", ");
                }
                selectClause.append(parameter.getColumn());
            }

            StringBuffer sql = new StringBuffer("SELECT ");
            sql.append(selectClause).append(" FROM ").append(_tableName);
            
            // Ajoute le pattern
            JdbcPredicate mandatoryPredicate = _getMandatoryPredicate(pattern);
            if (mandatoryPredicate != null)
            {
                sql.append(" WHERE ").append(mandatoryPredicate.getPredicate());
            }
            
            String patternToMatch = _getPatternToMatch (pattern);
            if (patternToMatch != null)
            {
                sql.append(mandatoryPredicate != null ? " AND (" : " WHERE ")
                    .append(_parameters.get("login").getColumn()).append(" LIKE ? OR ")
                    .append(_parameters.get("lastname").getColumn()).append(" LIKE ?");
                if (_parameters.containsKey("firstname"))
                {
                    sql.append(" OR ").append(_parameters.get("firstname").getColumn()).append(" LIKE ?");
                }
                if (mandatoryPredicate != null)
                {
                    sql.append(')');
                }
            }
            
            // Ajoute les filtres de taille
            sql = _addQuerySize(length, offset, con, selectClause, sql);

            // Crée la requête elle-meme
            String sqlRequest = sql.toString();
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Executing user SQL query: " + sqlRequest);
            }
            stmt = con.prepareStatement(sqlRequest);
            
            int i = 1;
            // Value les parametres s'il y a un pattern
            if (mandatoryPredicate != null)
            {
                for (String value : mandatoryPredicate.getValues())
                {
                    stmt.setString(i++, value);
                }
            }
            if (patternToMatch != null)
            {
                // One for the login, one for the lastname.
                stmt.setString(i++, patternToMatch);
                stmt.setString(i++, patternToMatch);
                if (_parameters.containsKey("firstname"))
                {
                    stmt.setString(i++, patternToMatch);
                }
            }

            // Effectuer la requête
            rs = stmt.executeQuery();
            while (rs.next())
            {
                resultSetToSAX(handler, rs);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the communication with the database", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }

    private StringBuffer _addQuerySize(int length, int offset, Connection con, StringBuffer selectClause, StringBuffer sql)
    {
        if ((ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_MYSQL)  
                || ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_POSTGRES)
        {
            sql.append(" LIMIT " + length + " OFFSET " + offset);
            return sql;
        }
        else if (ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_ORACLE)
        {
            return new StringBuffer("select " + selectClause.toString() + " from (select rownum r, " + selectClause.toString() + " from (" + sql.toString()
                    + ")) where r BETWEEN " + offset + " AND " + (offset + length - 1));
        }
        else if (ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_DERBY)
        {
            return new StringBuffer("select ").append(selectClause)
                    .append(" from (select ROW_NUMBER() OVER () AS ROWNUM, ").append(selectClause.toString())
                    .append(" from (").append(sql.toString()).append(") AS TR ) AS TRR where ROWNUM BETWEEN ")
                    .append(offset).append(" AND ").append(offset + length - 1);
        }
        else if (getLogger().isWarnEnabled())
        {
            getLogger().warn("The request will not have the limit and offet set, since its type is unknown");
        }
        return sql;
    }
    
    /**
     * Get attributes from a result set
     * @param attr the attributes
     * @param rs The result set to sax
     * @throws SQLException If an error occurs while getting result information.
     */
    protected void getUserAttributesFromResultSet (AttributesImpl attr, ResultSet rs) throws SQLException
    {
        attr.addAttribute("", "login", "login", "CDATA", rs.getString(_parameters.get("login").getColumn()));
    }

    /**
     * Sax a result set from a database.
     * 
     * @param handler The content handler to sax in.
     * @param rs The result set to sax.
     * @throws SAXException If an error occurs while saxing.
     * @throws SQLException If an error occurs while getting result information.
     */
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
     * Get the number of users matching a pattern.
     * @param pattern The pattern to match (may be null).
     * @return The number of users matching a pattern.
     */
    public int getUsersCount(String pattern)
    {
        int nbLignes = 0;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Effectuer la connexion à la base de données via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

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

            String sqlRequest = sql.toString();
            stmt = con.prepareStatement(sqlRequest);

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

            // Logger la requête
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest);
            }

            // Effectuer la requête
            rs = stmt.executeQuery();

            if (rs.next())
            {
                nbLignes = rs.getInt(1);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the communication with the database", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
        return nbLignes;
    }
    
    public void saxUser(String login, ContentHandler handler) throws SAXException
    {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Effectuer la connexion à la base de données via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour récupérer l'éventuel utilisateur
            String sqlRequest = "SELECT " + _createGetUserSelectClause() + " FROM " + _tableName + " WHERE " + _parameters.get("login").getColumn() + " = ? ";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest + " [login:" + login + "]");
            }

            stmt = con.prepareStatement(sqlRequest);

            stmt.setString(1, login);

            // Effectuer la requête
            rs = stmt.executeQuery();
            if (rs.next())
            {
                resultSetToSAX(handler, rs);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            throw new RuntimeException("Error communication with database", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
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
