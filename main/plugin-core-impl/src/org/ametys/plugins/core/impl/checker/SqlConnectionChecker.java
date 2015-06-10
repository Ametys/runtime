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

package org.ametys.plugins.core.impl.checker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.parameter.checker.ParameterCheckerTestFailureException;
import org.ametys.runtime.parameter.ParameterChecker;

/**
 * Checks the SQL connection with the data written in the configuration panel
 */
public class SqlConnectionChecker extends AbstractLogEnabled implements ParameterChecker, Configurable
{
    private Object _paramDriver;
    private String _paramURL;
    private String _paramUser;
    private String _paramPasswd;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration[] config = configuration.getChild("linked-params").getChildren();
        if (config.length != 4)
        {
            throw new ConfigurationException("The SqlConnectionChecker should have 4 linked params in the right order: driver, url, user, password");
        }
        
        int i = 0;
        _paramDriver = config[i++].getAttribute("id");
        _paramURL = config[i++].getAttribute("id");
        _paramUser = config[i++].getAttribute("id");
        _paramPasswd = config[i++].getAttribute("id");
    }
    
    @Override
    public void check(Map<String, String> configurationParameters) throws ParameterCheckerTestFailureException
    {
        String password = configurationParameters.get(_paramPasswd);
        String driver = configurationParameters.get(_paramDriver);
        String url = configurationParameters.get(_paramURL);
        String login = configurationParameters.get(_paramUser);
        
        Connection connection = null;
        try
        {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, login, password);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new ParameterCheckerTestFailureException("The given driver classpath was not found. Try to put the driver's jar in the WEB-INF/lib folder and reboot the application", cnfe);
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
