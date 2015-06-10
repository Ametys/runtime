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

package org.ametys.core.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.AbstractExtensionPoint;

/**
 * Extension point for declaring JDBC Datasources.
 */
public class DataSourceExtensionPoint extends AbstractExtensionPoint<DataSource> implements Disposable
{
    /** Avalon Role */
    public static final String ROLE = DataSourceExtensionPoint.class.getName();
    
    private static final String __CONFIG_ATTRIBUTE_NAME = "runtime-config-parameter";
    
    private Map<String, ObjectPool> _pools = new HashMap<>();

    @SuppressWarnings("unused")
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");
        
        String driver = _getValue(configuration.getChild("driver"));
        String url = _getValue(configuration.getChild("dburl"));
        String user = _getValue(configuration.getChild("user"));
        String pass = _getValue(configuration.getChild("password"));
        
        try
        {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e)
        {
            throw new ConfigurationException("Specified driver class does not exist: " + driver, e);
        }
        
        GenericObjectPool connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(-1);
        connectionPool.setMaxIdle(10);
        connectionPool.setMinIdle(2);
        connectionPool.setTestOnBorrow(true);
        connectionPool.setTestOnReturn(false);
        connectionPool.setTestWhileIdle(true);
        connectionPool.setTimeBetweenEvictionRunsMillis(1000 * 60 * 30);
        
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, pass);
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, _getValidationQuery(driver), false, true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        
        _pools.put(id, connectionPool);
        _extensions.put(id, dataSource);
    }
    
    private String _getValue(Configuration configuration)
    {
        String configName = configuration.getAttribute(__CONFIG_ATTRIBUTE_NAME, null);
        String value;
        
        if (configName == null)
        {
            // valeur du noeud courant
            value = configuration.getValue(null);
        }
        else
        {
            value = Config.getInstance().getValueAsString(configName);
        }
        
        return value;
    }
    
    private String _getValidationQuery(String driver)
    {
        if ("oracle.jdbc.driver.OracleDriver".equals(driver) || "oracle.jdbc.OracleDriver".equals(driver))
        {
            return "SELECT 1 FROM DUAL";
        }
        else if ("org.apache.derby.jdbc.EmbeddedDriver".equals(driver))
        {
            return "SELECT 1 FROM SYS.SYSTABLES";
        }
        else if ("org.hsqldb.jdbcDriver".equals(driver) || "org.hsqldb.jdbc.JDBCDriver".equals(driver))
        {
            return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
        }
        else
        {
            return "SELECT 1";
        }
    }

    public void initializeExtensions() throws Exception
    {
        ConnectionHelper.setExtensionPoint(this);
    }
    
    @Override
    public void dispose()
    {
        for (String id : _pools.keySet())
        {
            ObjectPool pool = _pools.get(id);
            
            try
            {
                pool.close();
            }
            catch (Exception e)
            {
                getLogger().warn("Unable to close pool when disposing", e);
            }
        }
    }
}
