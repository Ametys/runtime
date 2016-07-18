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
import java.sql.DriverManager;
import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * Checks that a sql connection can be established with the provided values
 */
public class SQLConnectionChecker extends AbstractLogEnabled implements ParameterChecker
{
    @Override
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        String url = values.get(0);
        String login = values.get(1);
        String password = values.get(2);
        
        Connection connection = null;
        try 
        {
            connection = DriverManager.getConnection(url, login, password);
        }
        catch (Exception e)
        {
            throw new ParameterCheckerTestFailureException(e.getMessage(), e);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    ConnectionHelper.cleanup(connection);
                }
                catch (Exception e)
                {
                    throw new ParameterCheckerTestFailureException(e.getMessage(), e);
                }
            }
        }
    }
}
