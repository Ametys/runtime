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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;

/**
 * Helper component used to retrieve java.sql.Connection from pools
 */
public final class ConnectionHelper implements Component, Serviceable, Disposable
{
    /** The Avalon role */
    public static final String ROLE = ConnectionHelper.class.getName();

    /** ID of database extension for Unknown */
    public static final String DATABASE_UNKNOWN = "";
    /** ID of database extension for Mysql */
    public static final String DATABASE_MYSQL = "mysql";
    /** ID of database extension for Oracle */
    public static final String DATABASE_ORACLE = "oracle";
    /** ID of database extension for Postgres */
    public static final String DATABASE_POSTGRES = "postgresql";
    /** ID of database extension for Derby */
    public static final String DATABASE_DERBY = "derby";
    /** ID of database extension for Hsqldb */
    public static final String DATABASE_HSQLDB = "hsql";
    
    
    /** Logger for traces */
    private static Logger _logger = LoggerFactory.getLogger(ConnectionHelper.class.getName());
    
    /** The manager for SQL data source */
    private static SQLDataSourceManager _sqlDataSourceManager;
    
    private static ServiceManager _manager;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _manager = serviceManager;
    }
    
    public void dispose()
    {
        _sqlDataSourceManager = null;
    }
    
    private static SQLDataSourceManager getSQLDataSourceManager()
    {
        if (_sqlDataSourceManager == null)
        {
            try
            {
                _sqlDataSourceManager = (SQLDataSourceManager) _manager.lookup(SQLDataSourceManager.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException(e);
            }
        }
        return _sqlDataSourceManager;
    }

    /**
     * Get a connection to the internal sql data source
     * @return java.sql.Connection to query the internal SQL database
     */
    public static Connection getInternalSQLDataSourceConnection()
    {
        return getSQLDataSourceManager().getInternalSQLDataSourceConnection();
    }
    
    /**
     * Returns a Connection from the pool.
     * @param id the id of the data source
     * @return a java.sql.Connection to query a SQL database
     */
    public static Connection getConnection(String id)
    {
        DataSource dataSource;
        Connection connection = null;
        
        if (getSQLDataSourceManager() == null)
        {
            throw new RuntimeException("ConnectionHelper cannot be used statically during or before components initialization");
        }
        
        try
        {
            dataSource =  getSQLDataSourceManager().getSQLDataSource(id);
            connection = dataSource.getConnection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to get Connection from pool " + id, e);
        }

        return connection;
    }
    
    /**
     * Commit and closes a java.sql.Connection
     * @param con the Connection to close
     */
    public static void cleanup(Connection con)
    {
        if (con != null)
        {
            try
            {
                if (!con.getAutoCommit())
                {
                    con.commit();
                }
            }
            catch (SQLException s)
            {
                _logger.error("Error while closing database", s);
            }

            try
            {
                con.close();
            }
            catch (SQLException s)
            {
                _logger.error("Error while closing database", s);
            }
        }
    }
    
    /**
     * Closes a java.sql.Statement
     * @param stmt the Statement to close
     */
    public static void cleanup(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException s)
            {
                _logger.error("Error while closing statement", s);
            }
        }
    }
    
    /**
     * Closes a java.sql.ResultSet
     * @param rs the ResultSet to close
     */
    public static void cleanup(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException s)
            {
                _logger.error("Error while closing statement", s);
            }
        }
    }   

    /**
     * Determine the database type
     * @param connection The jdbc connection to the database
     * @return The database type id or empty string if unknown
     */
    public static String getDatabaseType(Connection connection)
    {
        try
        {
            return getDatabaseType(connection.getMetaData().getURL());
        }
        catch (SQLException e)
        {
            LoggerFactory.getLogger(ConnectionHelper.class).error("Cannot determine database type", e);
            return DATABASE_UNKNOWN;
        }
    }
    
    /**
     * Determine the database type
     * @param jdbcURL The jdbc url used to connect to the database
     * @return The database type id or null if unknown
     */
    public static String getDatabaseType(String jdbcURL)
    {
        Map<String, DataSourceDefinition> dataSourceDefinitions = getSQLDataSourceManager().getDataSourceDefinitions(true, true, false);
        for (DataSourceDefinition definition : dataSourceDefinitions.values())
        {
            // Get the definition url without jdbc parameters (e.g. internal-db have ;create=true)
            String url = StringUtils.substringBefore(definition.getParameters().get("url"), ";");
            if (StringUtils.equals(url, jdbcURL))
            {
                return definition.getParameters().get("dbtype");
            }
        }
        
        return DATABASE_UNKNOWN;
    }
    
    /**
     * Returns the SQL {@link DataSourceDefinition} corresponding to the given id.
     * @param id the id of the data source
     * @return the {@link DataSourceDefinition}.
     */
    public static DataSourceDefinition getDataSourceDefinition(String id)
    {
        return getSQLDataSourceManager().getDataSourceDefinition(id);
    }
}
