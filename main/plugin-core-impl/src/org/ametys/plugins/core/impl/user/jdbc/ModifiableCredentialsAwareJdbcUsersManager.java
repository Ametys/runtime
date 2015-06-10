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
import java.util.Map;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.parameter.DefaultValidator;
import org.ametys.core.user.CredentialsAwareUsersManager;
import org.ametys.core.util.I18nizableText;
import org.ametys.core.util.StringUtils;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;


/**
 * Use a jdbc driver for getting the list of users, modifying them and also
 * authenticate them.<br>
 * Passwords need to be encrypted with MD5 and encoded in base64.<br>
 * This driver depends of the config parameters needed by the ModifyingJdbcUsers
 * extension plus this parameter :<br>
 * @see org.ametys.runtime.plugins.core.impl.user.jdbc.ModifiableJdbcUsersManager
 */
public class ModifiableCredentialsAwareJdbcUsersManager extends ModifiableJdbcUsersManager implements CredentialsAwareUsersManager
{
    /** The name of column storing salt key */
    protected String _saltColumn;
    
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
        
        _saltColumn = configuration.getChild("salt", true).getAttribute("column", "salt");
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
            throw new ConfigurationException("Configuration for parameter '" + id + "' is invalid", configuration, e);
        }

        return parameter;
    }

    public boolean checkCredentials(Credentials credentials)
    {
        String login = credentials.getLogin();
        String password = credentials.getPassword();
        
        boolean updateNeeded = false;
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Effectuer la connexion à la base de données
            // via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour authentifier l'utilisateur
            String sql = "SELECT " + _parameters.get("login").getColumn() + ", " + _parameters.get("password").getColumn() + ", " + _saltColumn + " FROM " + _tableName + " WHERE " + _parameters.get("login").getColumn() + " = ?";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }

            stmt = con.prepareStatement(sql);
            stmt.setString(1, login);

            // Effectuer la requête
            rs = stmt.executeQuery();

            if (rs.next()) 
            {
                String storedPassword = rs.getString(_parameters.get("password").getColumn());
                String salt = rs.getString(_saltColumn);
                
                if (salt == null && _isMD5Encrypted(storedPassword))
                {
                    String encryptedPassword = StringUtils.md5Base64(password);
                    
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
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
            
            if (updateNeeded)
            {
                _updateToSSHAPassword(login, password);
            }
        }
        
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
            con = ConnectionHelper.getConnection(_poolName);

            String generateSaltKey = RandomStringUtils.randomAlphanumeric(48);
            String newEncryptedPassword = DigestUtils.sha512Hex(generateSaltKey + password);

            String sqlUpdate = "UPDATE " + _tableName + " SET " + _parameters.get("password").getColumn() + " = ?, " + _saltColumn + " = ? WHERE " + _parameters.get("login").getColumn() + " = ?";
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
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
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
    
    @Override
    protected PreparedStatement createAddStatement(Connection con, Map<String, String> userInformation) throws SQLException
    {
        String beginClause = "INSERT INTO " + _tableName + " (";
        String middleClause = ") VALUES (";
        String endClause = ")";
        
        StringBuffer intoClause = new StringBuffer();
        StringBuffer valueClause = new StringBuffer();
        
        intoClause.append(_saltColumn);
        valueClause.append("?");
        
        for (JdbcParameter parameter : _parameters.values())
        {
            intoClause.append(", " + parameter.getColumn());
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
        
        for (JdbcParameter parameter : _parameters.values())
        {
            if (parameter.getType() == ParameterType.PASSWORD)
            {
                String encryptedPassword = DigestUtils.sha512Hex(generatedSaltKey + userInformation.get(parameter.getId()));
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
                stmt.setString(i++, userInformation.get(parameter.getId()));
            }
        }
        
        return stmt;
    }
    
    @Override
    protected PreparedStatement createModifyStatement(Connection con, Map<String, String> userInformation) throws SQLException
    {
        // Contruire la requête pour modifier un utilisateur
        String beginClause = "UPDATE " + _tableName + " SET ";
        String endClause = " WHERE " + _parameters.get("login").getColumn() + " = ?";

        StringBuffer columnNames = new StringBuffer("");
        
        boolean passwordUpdate = false;
        for (String id : userInformation.keySet())
        {
            JdbcParameter parameter = _parameters.get(id);
            if (parameter != null && !"login".equals(id) && !(parameter.getType() == ParameterType.PASSWORD && (userInformation.get(parameter.getId()) == null)))
            {
                if (parameter.getType() == ParameterType.PASSWORD)
                {
                    passwordUpdate = true;
                }
                
                if (columnNames.length() > 0)
                {
                    columnNames.append(", ");
                }
                columnNames.append(parameter.getColumn() + " = ?");
            }
        }
          
        if (passwordUpdate)
        {
            columnNames.append(", " + _saltColumn + " = ?");
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
    
    @Override
    protected void _fillModifyStatement(PreparedStatement stmt, Map<String, String> userInformation) throws SQLException
    {
        int index = 1;
        
        String generateSaltKey = RandomStringUtils.randomAlphanumeric(48);
        boolean passwordUpdate = false;
        
        for (String id : userInformation.keySet())
        {
            JdbcParameter parameter = _parameters.get(id);
            if (parameter != null && !"login".equals(id))
            {
                if (parameter.getType() == ParameterType.PASSWORD)
                {
                    if (userInformation.get(parameter.getId()) != null)
                    {
                        String encryptedPassword = DigestUtils.sha512Hex(generateSaltKey + userInformation.get(parameter.getId()));
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
                    stmt.setString(index++, userInformation.get(parameter.getId()));
                }
            }
        }
        
        if (passwordUpdate)
        {
            stmt.setString(index++, generateSaltKey);
        }
        
        stmt.setString(index++, userInformation.get("login"));
    }
}
