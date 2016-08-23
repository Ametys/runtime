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
package org.ametys.plugins.core.right.profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.right.RightManager;
import org.ametys.runtime.plugin.Init;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Creates the READER profile at initialization.
 */
public class CreateReaderProfileInit extends AbstractLogEnabled implements Init, Serviceable
{
    private RightManager _rightManager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _rightManager = (RightManager) manager.lookup(RightManager.ROLE);
    }
    
    @Override
    public void init() throws Exception
    {
        if (_rightManager.getProfile(RightManager.READER_PROFILE_ID) != null)
        {
            // already exist
            getLogger().info("READER profile already exists, it will not be created");
            return;
        }
        
        getLogger().info("Creating READER profile");
        String profileLabel = RightManager.READER_PROFILE_ID;
        String tableProfile = _rightManager.getTableProfile();
        String context = null;
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();

            statement = connection.prepareStatement("INSERT INTO " + tableProfile + " (Id, Label, Context) VALUES(?, ?, ?)");
            statement.setString(1, RightManager.READER_PROFILE_ID);
            statement.setString(2, profileLabel);
            statement.setString(3, context);

            statement.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(statement);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    private Connection getSQLConnection ()
    {
        return ConnectionHelper.getConnection(_rightManager.getDataSourceId());
    }
}
