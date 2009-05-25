/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.user.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.CachingComponent;
import org.ametys.runtime.util.parameter.DefaultValidator;
import org.ametys.runtime.util.parameter.ParameterHelper;

/**
 * Use a jdbc driver for getting the list of users.<br/>
 * The main method to override is <code>_createUserFromResultSet</code>
 */
public class JdbcUsersManager extends CachingComponent implements UsersManager, Configurable, ThreadSafe, Component, Serviceable, Contextualizable, PluginAware
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
        _poolName = configuration.getChild("pool").getValue();
        _tableName = configuration.getChild("table").getValue("Users");

        _parameters = new LinkedHashMap<String, JdbcParameter>();

        Configuration[] parametersConfigurations = configuration.getChildren("param");
        for (Configuration parameterConfiguration : parametersConfigurations)
        {
            String id = parameterConfiguration.getAttribute("id");
            String column = parameterConfiguration.getAttribute("column", id);

            JdbcParameter parameter = _configureParameter(id, column, parameterConfiguration);
            _parameters.put(parameter.getId(), parameter);
        }

        if (!_parameters.containsKey("login"))
        {
            String message = "Missing the mandatory parameter 'login'";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        if (!_parameters.containsKey("lastname"))
        {
            String message = "Missing the mandatory parameter 'lastname'";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        if (!_parameters.containsKey("email"))
        {
            String message = "Missing the mandatory parameter 'email'";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
    }

    /**
     * Configure the parameter (for special handling)
     * @param id Id the of the paramter
     * @param column Column name of the parameter
     * @param configuration Configuration of the paramter
     * @return The parameter created
     * @throws ConfigurationException if a configuration problem occurs
     */
    protected JdbcParameter _configureParameter(String id, String column, Configuration configuration) throws ConfigurationException
    {
        JdbcParameter parameter = null;

        try
        {
            if ("login".equals(id))
            {
                DefaultValidator validator = new DefaultValidator("^[\\w]{3,16}$", true);
                validator.enableLogging(getLogger());
                
                parameter = new JdbcParameter(BASE_PLUGIN_NAME, id, column, "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_LABEL", "PLUGINS_CORE_USERS_JDBC_FIELD_LOGIN_DESCRIPTION", ParameterHelper.TYPE.STRING, null, null, validator);
            }
            else if ("lastname".equals(id))
            {
                DefaultValidator validator = new DefaultValidator(null, true);
                validator.enableLogging(getLogger());
                
                parameter = new JdbcParameter(BASE_PLUGIN_NAME, id, column, "PLUGINS_CORE_USERS_JDBC_FIELD_LASTNAME_LABEL", "PLUGINS_CORE_USERS_JDBC_FIELD_LASTNAME_DESCRIPTION", ParameterHelper.TYPE.STRING, null, null, validator);
            }
            else if ("firstname".equals(id))
            {
                DefaultValidator validator = new DefaultValidator(null, true);
                validator.enableLogging(getLogger());
                
                parameter = new JdbcParameter(BASE_PLUGIN_NAME, id, column, "PLUGINS_CORE_USERS_JDBC_FIELD_FIRSTNAME_LABEL", "PLUGINS_CORE_USERS_JDBC_FIELD_FIRSTNAME_DESCRIPTION", ParameterHelper.TYPE.STRING, null, null, validator);
            }
            else if ("email".equals(id))
            {
                DefaultValidator validator = new DefaultValidator("^([\\w\\-\\.])+@([\\w\\-\\.])+\\.([a-zA-Z])+$", false);
                validator.enableLogging(getLogger());
                
                parameter = new JdbcParameter(BASE_PLUGIN_NAME, id, column, "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_LABEL", "PLUGINS_CORE_USERS_JDBC_FIELD_EMAIL_DESCRIPTION", ParameterHelper.TYPE.STRING, null, null, validator);
            }
            else
            {
                parameter = new JdbcParameter(_pluginName, id, column, configuration, _context, _manager);
            }
        }
        catch (Exception e)
        {
            String message = "Configuration for parameter '" + id + "' is invalid";
            getLogger().error(message, e);
            throw new ConfigurationException(message, configuration, e);
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
            User user = (User) getObjectFromCache(login);
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

            StringBuffer sql = new StringBuffer("SELECT " + selectClause.toString() + " FROM " + _tableName);

            // Ajoute le pattern
            if (pattern != null)
            {
                sql.append(" WHERE " + _parameters.get("login").getColumn() + " LIKE ? ");
                sql.append(" OR " + _parameters.get("lastname").getColumn() + " LIKE ? ");
                if (_parameters.containsKey("firstname"))
                {
                    sql.append(" OR " + _parameters.get("firstname").getColumn() + " LIKE ? ");
                }
            }

            // Ajoute les filtres de taille
            if ((ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_MYSQL)  
                    || ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_POSTGRES)
            {
                sql.append(" LIMIT " + length + " OFFSET " + offset);
            }
            else if (ConnectionHelper.getDatabaseType(con) == ConnectionHelper.DatabaseType.DATABASE_ORACLE)
            {
                sql = new StringBuffer("select " + selectClause.toString() + " from (select rownum r, " + selectClause.toString() + " from (" + sql.toString()
                        + ")) where r BETWEEN " + offset + " AND " + (offset + length - 1));
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The request will not have the limit and offet set, since its type is unknown");
            }

            // Crée la requette elle-meme
            String sqlRequest = sql.toString();
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest);
            }
            stmt = con.prepareStatement(sqlRequest);

            // Value les parametres s'il y a un pattern
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
        attr.addAttribute("", "login", "login", "CDATA", rs.getString(_parameters.get("login").getColumn()));
        XMLUtils.startElement(handler, "user", attr);

        for (String id : _parameters.keySet())
        {
            if (!"login".equals(id))
            {
                JdbcParameter parameter = _parameters.get(id);
                Object typedValue; 
                
                if (parameter.getType() == ParameterHelper.TYPE.BOOLEAN)
                {
                    typedValue = rs.getBoolean(parameter.getColumn());
                }
                else if (parameter.getType() == ParameterHelper.TYPE.DATE)
                {
                    java.sql.Date date = rs.getDate(parameter.getColumn());
                    typedValue = date != null ? new Date(date.getTime()) : null;
                }
                else if (parameter.getType() == ParameterHelper.TYPE.DOUBLE)
                {
                    typedValue = rs.getDouble(parameter.getColumn());
                }
                else if (parameter.getType() == ParameterHelper.TYPE.LONG)
                {
                    typedValue = rs.getLong(parameter.getColumn());
                }
                else if (parameter.getType() == ParameterHelper.TYPE.PASSWORD)
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
}
