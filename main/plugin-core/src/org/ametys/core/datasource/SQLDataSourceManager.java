/*
 *  Copyright 2015 Anyware Services
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.xml.sax.SAXException;

import org.ametys.plugins.core.impl.checker.SQLConnectionChecker;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.servlet.RuntimeConfig;

import com.sun.media.jfxmediaimpl.MediaDisposer.Disposable;

/**
 * This component handles SQL data sources. 
 * It is associated with the configuration file $AMETYS_HOME/datasources-sql.xml 
 */
public class SQLDataSourceManager extends AbstractDataSourceManager implements Disposable
{
    /** Avalon Role */
    public static final String ROLE = SQLDataSourceManager.class.getName();
    
    private static final String __AMETYS_INTERNAL_DATASOURCE_ID = "SQL-ametys-internal";
    private static final I18nizableText __AMETYS_INTERNAL_DATASOURCE_NAME = new I18nizableText("plugin.core", "PLUGINS_CORE_INTERNAL_DATASOURCE_LABEL");
    private static final I18nizableText __AMETYS_INTERNAL_DATASOURCE_DESCRIPTION = new I18nizableText("plugin.core", "PLUGINS_CORE_INTERNAL_DATASOURCE_LABEL");
    private static final String __AMETYS_INTERNAL_DATASOURCE_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    
    private static String __filename;

    private Map<String, DataSource> _sqlDataSources;
    private Map<String, ObjectPool> _pools;
    
    private DataSourceDefinition _internalDataSource;

    /**
     * Set the config filename. Only use for tests.
     * @param filename Name with path of the config file
     */
    public static void setFilename(String filename)
    {
        __filename = filename;
    }
    
    @Override
    public File getFileConfiguration()
    {
        if (__filename != null)
        {
            return new File(__filename);
        }
        
        return new File(RuntimeConfig.getInstance().getAmetysHome(), "data" + File.separator + "datasources-sql.xml");
    }
    
    
    @Override
    public void initialize() throws Exception
    {
        _sqlDataSources = new HashMap<>();
        _pools = new HashMap<>();
        
        super.initialize();
        
        // Add the internal and not editable DB
        Map<String, String> parameters = new HashMap<>();
        parameters.put ("driver", __AMETYS_INTERNAL_DATASOURCE_DRIVER);
        
        File dbFile = new File (RuntimeConfig.getInstance().getAmetysHome(), "data" + File.separator + "internal-db");
        parameters.put ("url", "jdbc:derby:" + dbFile.getAbsolutePath() + ";create=true");
        parameters.put ("user", ""); 
        parameters.put ("password", ""); 
        
        _internalDataSource = new DataSourceDefinition(__AMETYS_INTERNAL_DATASOURCE_ID, __AMETYS_INTERNAL_DATASOURCE_NAME, __AMETYS_INTERNAL_DATASOURCE_DESCRIPTION, parameters, true);
        createDataSource(_internalDataSource);
    }
    
    @Override
    public DataSourceDefinition getDataSourceDefinition(String id) throws ConfigurationException, SAXException, IOException
    {
        readConfiguration(false);
        
        if (__AMETYS_INTERNAL_DATASOURCE_ID.equals(id))
        {
            return _internalDataSource;
        }
        
        return super.getDataSourceDefinition(id);
    }

    @Override
    public Map<String, DataSourceDefinition> getDataSourceDefinitions(boolean includePrivate, boolean includeInternal) throws ConfigurationException, SAXException, IOException
    {
        readConfiguration(false);
        
        Map<String, DataSourceDefinition> datasources = new LinkedHashMap<>();
        
        if (includePrivate && includeInternal)
        {
            // Include the internal db
            datasources.put(__AMETYS_INTERNAL_DATASOURCE_ID, _internalDataSource);
        }
        
        datasources.putAll(super.getDataSourceDefinitions(includePrivate, includeInternal));
        return datasources;
    }
    
    @Override
    protected String getDataSourcePrefixId()
    {
        return "SQL-";
    }
    
    /**
     * Get the existing SQL data sources
     * @return the SQL data sources
     */
    public Map<String, DataSource> getSQLDataSources ()
    {
        return _sqlDataSources;
    }
    
    /**
     * Get the SQL data source by its identifier
     * @param id The id of data source
     * @return the SQL data source
     */
    public DataSource getSQLDataSource (String id)
    {
        return _sqlDataSources.get(id);
    }
    
    /**
     * Returns a Connection from the pool.
     * @param poolId the id of the connection pool
     * @return a java.sql.Connection to query a SQL database
     */
    public Connection getConnection(String poolId)
    {
        DataSource dataSource;
        Connection connection = null;

        try
        {
            dataSource =  getSQLDataSource(poolId);
            connection = dataSource.getConnection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to get Connection from pool " + poolId, e);
        }

        return connection;
    }
    
    @Override
    public void checkParameters(DataSourceDefinition dataSource) throws ParameterCheckerTestFailureException
    {
        checkParameters(dataSource.getParameters());
    }
    
    @Override
    public void checkParameters(Map<String, String> rawParameters) throws ParameterCheckerTestFailureException
    {
        ParameterChecker paramChecker = new SQLConnectionChecker();
        paramChecker.check(rawParameters);
    }
    
    @Override
    protected void editDataSource(DataSourceDefinition dataSource)
    {
        deleteDataSource(dataSource);
        createDataSource(dataSource);
    }
    
    @Override
    protected void createDataSource(DataSourceDefinition dataSourceDef)
    {
        Map<String, String> parameters = dataSourceDef.getParameters();
        
        String url = parameters.get("url");
        String user = parameters.get("user");
        String password = parameters.get("password");
        String driver = parameters.get("driver");
        
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        
        GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        connectionPool.setMaxTotal(-1);
        connectionPool.setMaxIdle(10);
        connectionPool.setMinIdle(2);
        connectionPool.setTestOnBorrow(true);
        connectionPool.setTestOnReturn(false);
        connectionPool.setTestWhileIdle(true);
        connectionPool.setTimeBetweenEvictionRunsMillis(1000 * 60 * 30);
        
        poolableConnectionFactory.setPool(connectionPool);
        poolableConnectionFactory.setValidationQuery(_getValidationQuery(driver));
        poolableConnectionFactory.setDefaultAutoCommit(true);
        poolableConnectionFactory.setDefaultReadOnly(false);
                 
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        
        String id = dataSourceDef.getId();
        
        // make sure the previous connection has been disposed
        if (_pools.containsKey(id))
        {
            _disposePool(id);
        }
        
        // Store the connection pools and the data sources
        _pools.put(id, connectionPool);
        _sqlDataSources.put(id, dataSource);
    }
    
    @Override
    protected void deleteDataSource(DataSourceDefinition dataSource)
    {
        _sqlDataSources.remove(dataSource.getId());
        _disposePool(dataSource.getId());
    }
    
    /**
     * Get the appropriate validation query
     * @param driver the driver 
     * @return the validation query
     */
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
    
    @Override
    public void dispose()
    {
        for (String id : _pools.keySet())
        {
            _disposePool(id);
        }
    }
    
    /**
     * Dispose of a connection pool
     * @param id the id of the connection pool to dispose of
     */
    private void _disposePool(String id)
    {
        try
        {
            _pools.get(id).close();
        }
        catch (Exception e)
        {
            getLogger().warn("Unable to close the edited connection pool", e);
        }        
        
        _pools.remove(id);
    }
}
