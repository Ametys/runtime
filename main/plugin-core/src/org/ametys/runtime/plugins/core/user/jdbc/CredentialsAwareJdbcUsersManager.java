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

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.util.StringUtils;
import org.ametys.runtime.util.parameter.DefaultValidator;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;


/**
 * Use a jdbc driver for getting the list of users and also authenticating them.
 * <br>
 * Passwords need to be encrypted with MD5 and encoded in base64. <br>
 * This driver depends of the config parameters needed by the JdbcUsers
 * extension plus this parameter :<br>
 * users.jdbc.passwdColumn: Name of the password column; String
 * @see org.ametys.runtime.plugins.core.user.jdbc.JdbcUsersManager
 */
public class CredentialsAwareJdbcUsersManager extends JdbcUsersManager implements CredentialsAwareUsersManager
{
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);

        if (!_parameters.containsKey("password"))
        {
            String message = "Missing the mandatory parameter 'password'";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
    }

    @Override
    protected JdbcParameter _configureParameter(String id, String column, Configuration configuration) throws ConfigurationException
    {
        JdbcParameter parameter = null;

        try
        {
            if ("password".equals(id))
            {
                DefaultValidator validator = new DefaultValidator(null, true);
                validator.enableLogging(getLogger());
                
                parameter = new JdbcParameter(BASE_PLUGIN_NAME, id, column, "PLUGINS_CORE_USERS_JDBC_FIELD_PASSWORD_LABEL", "PLUGINS_CORE_USERS_JDBC_FIELD_PASSWORD_DESCRIPTION", ParameterHelper.TYPE.PASSWORD, null, null, validator);
            }
            else
            {
                parameter = super._configureParameter(id, column, configuration);
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

    public boolean checkCredentials(Credentials credentials)
    {
        String login = credentials.getLogin();
        String password = credentials.getPassword();
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Effectuer la connexion à la base de données
            // via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour authentifier l'utilisateur
            String sql = "SELECT " + _parameters.get("login").getColumn() + " FROM " + _tableName + " WHERE " + _parameters.get("login").getColumn() + " = ? " + " AND " + _parameters.get("password").getColumn() + " = ? ";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }

            String encryptedPassword = StringUtils.md5Base64(password);
            if (encryptedPassword == null)
            {
                getLogger().error("Unable to encrypt password");
                return false;
            }

            stmt = con.prepareStatement(sql);
            stmt.setString(1, login);
            stmt.setString(2, encryptedPassword);

            // Effectuer la requête
            rs = stmt.executeQuery();

            // L'utilisateur est authentifié si l'on a bien une ligne de résultat
            return rs.next();
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
            return false;
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
