/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.plugins.core.impl.authentication.token;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.codec.digest.DigestUtils;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.util.LoggerFactory;

/**
 * Credentials coming from more than one source.
 * If the credentials come from a SSO (like CAS), they are already authenticated.
 */
public class TokenCredentials extends Credentials
{
    private static Logger _LOGGER = LoggerFactory.getLoggerFor(TokenCredentials.class); 
    
    /** 15 days in seconds */
    private static final int COOKIE_LIFETIME = 1209600;

    /**
     * Constructor
     * @param login The user's login
     * @param token The user's token
     */
    public TokenCredentials(String login, String token)
    {
        super(login, token);
    }
    
    @Override
    public String getPassword()
    {
        throw new UnsupportedOperationException("TokenCredentials does not allow to use password");
    }

    /**
     * Test if the user is already authenticated by the CredentialsProvider ?
     * @return true if the user is already authenticated by the CredentialsProvider, false otherwise.
     */
    public boolean checkToken()
    {
        try (Connection connection = ConnectionHelper.getConnection(ConnectionHelper.CORE_POOL_NAME))
        {
            // Delete 2 weeks or more old entries
            try (PreparedStatement deleteStatement = _getDeleteOldUserTokenStatement(connection))
            {
                deleteStatement.executeUpdate();
            }

            // Retrieve entries corresponding to this login
            String login = getLogin();
            String token = _passwd;
            
            try (PreparedStatement selectStatement = _getSelectUserTokenStatement(connection, login);
                 ResultSet resultSet = selectStatement.executeQuery())
            {
                // Find the database entry using this token
                while (resultSet.next())
                {
                    if (resultSet.getString("token").equals(DigestUtils.sha512Hex(token + resultSet.getString("salt"))))
                    {
                        // Delete it
                        _deleteUserToken(connection, resultSet.getString("token"));
                        return true;
                    }
                }
                    
                return false;
            }
        }
        catch (Exception e)
        {
            _LOGGER.error("Communication error with the database", e); 
            return false;
        }
    }
    
    /**
     * Generates the sql statement that deletes the entries of the users token database that are at least 2 weeks old
     * @param connection the database's session
     * @return statement the delete statement
     * @throws SQLException if a sql exception occurs
     */
    private PreparedStatement _getDeleteOldUserTokenStatement(Connection connection) throws SQLException
    {
        String sqlRequest = null;
        sqlRequest = "DELETE FROM UsersToken WHERE creation_date < ?";

        Date thresholdDate = new Date(System.currentTimeMillis() - COOKIE_LIFETIME * 1000);

        PreparedStatement statement = connection.prepareStatement(sqlRequest);
        statement.setDate(1, thresholdDate);

        return statement;
    }
    
    /**
     * Generates the statement that selects the users having the specified login in the UsersToken table
     * @param connection the database's session
     * @param login the user's login
     * @return the retrieve statement
     * @throws SQLException if a sql exception occurs
     */
    private PreparedStatement _getSelectUserTokenStatement(Connection connection, String login) throws SQLException
    {
        String sqlRequest = "SELECT id, token, salt FROM UsersToken WHERE login= ?";
        
        PreparedStatement statement = connection.prepareStatement(sqlRequest);
        statement.setString(1, login);

        return statement;
    }
    
    /**
     * Deletes the database entry that has this token 
     * @param connection the database's session
     * @param token the token
     * @throws SQLException if an error occurred
     */
    private void _deleteUserToken(Connection connection, String token) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM UsersToken WHERE token = ?"))
        {
            statement.setString(1, token);
            statement.executeUpdate();
        }
    }
}
