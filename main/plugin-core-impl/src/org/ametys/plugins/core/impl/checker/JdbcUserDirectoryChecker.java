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
package org.ametys.plugins.core.impl.checker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Tests the JDBC user directory defines existing tables
 */
public class JdbcUserDirectoryChecker extends AbstractLogEnabled implements ParameterChecker
{
    @Override
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        String datasourceId = values.get(0);
        String tableName = values.get(1);
        
        boolean schemaExists = false;
        try
        {
            Connection connection = ConnectionHelper.getConnection(datasourceId);
            ResultSet rs = null;
            
            String name = tableName;
            DatabaseMetaData metaData = connection.getMetaData();
            
            if (metaData.storesLowerCaseIdentifiers())
            {
                name = tableName.toLowerCase();
            }
            else if (metaData.storesUpperCaseIdentifiers())
            {
                name = tableName.toUpperCase();
            }
            
            try
            {
                rs = metaData.getTables(null, null, name, null);
                schemaExists = rs.next();
            }
            finally
            {
                ConnectionHelper.cleanup(rs);
            }
        }
        catch (Exception e)
        {
            throw new ParameterCheckerTestFailureException(e);
        }
        
        if (!schemaExists)
        {
            throw new ParameterCheckerTestFailureException("The table seems to not exist");
        }
    }
}
