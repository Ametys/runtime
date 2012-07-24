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
package org.ametys.runtime.plugins.core.user.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.StringUtils;
import org.ametys.runtime.util.parameter.DefaultValidator;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

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
    protected JdbcParameter _configureParameter(JdbcParameterParser jdbcParameterParser, String id, String column, Configuration configuration) throws ConfigurationException
    {
        JdbcParameter parameter = null;

        try
        {
            if ("password".equals(id))
            {
                parameter = new JdbcParameter();
                parameter.setId(id);
                parameter.setPluginName(BASE_PLUGIN_NAME);
                parameter.setColumn(column);
                parameter.setLabel(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_PASSWORD_LABEL"));
                parameter.setDescription(new I18nizableText("plugin." + BASE_PLUGIN_NAME, "PLUGINS_CORE_USERS_JDBC_FIELD_PASSWORD_DESCRIPTION"));
                parameter.setType(ParameterType.PASSWORD);
                parameter.setValidator(new DefaultValidator(null, true));
            }
            else
            {
                parameter = super._configureParameter(jdbcParameterParser, id, column, configuration);
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
