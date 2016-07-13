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

import javax.sql.DataSource;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;

/**
 * Helper component used to retrieve java.sql.Connection from pools
 */
public final class ConnectionHelper implements Component, Serviceable
{
    /** The Avalon role */
    public static final String ROLE = ConnectionHelper.class.getName();
    
    /** Logger for traces */
    private static Logger _logger = LoggerFactory.getLogger(ConnectionHelper.class.getName());
    
    /** The manager for SQL data source */
    private static SQLDataSourceManager _sqlDataSourceManager;
    
    /** Enumeration for database type.&lt;br&gt; */
    public enum DatabaseType
    {
        /** Database type is unknown */
        DATABASE_UNKNOWN,
        
        /** Database type is Mysql */
        DATABASE_MYSQL,
        
        /** Database type is Oracle */
        DATABASE_ORACLE, 
              
        /** Database type is Postgres */
        DATABASE_POSTGRES,
        
        /** Database type is Derby */
        DATABASE_DERBY,

        /** Database type is Hsqldb */
        DATABASE_HSQLDB
    }
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _sqlDataSourceManager = (SQLDataSourceManager) serviceManager.lookup(SQLDataSourceManager.ROLE); 
    }

    /**
     * Get a connection to the internal sql data source
     * @return java.sql.Connection to query the internal SQL database
     */
    public static Connection getInternalSQLDataSourceConnection()
    {
        return _sqlDataSourceManager.getInternalSQLDataSourceConnection();
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
        
        try
        {
            dataSource =  _sqlDataSourceManager.getSQLDataSource(id);
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
     * @return DATABASE_UNKNOWN, DATABASE_MYSQL, DATABASE_POSTGRES or DATABASE_ORACLE
     */
    public static DatabaseType getDatabaseType(Connection connection)
    {
        try
        {
            return getDatabaseType (connection.getMetaData().getURL());
        }
        catch (SQLException e)
        {
            LoggerFactory.getLogger(ConnectionHelper.class).error("Cannot determine database type", e);
            return DatabaseType.DATABASE_UNKNOWN;
        }
    }
    
    /**
     * Determine the database type
     * @param jdbcURL The jdbc url used to connect to the database
     * @return DATABASE_UNKNOWN, DATABASE_MYSQL, DATABASE_POSTGRES or DATABASE_ORACLE
     */
    public static DatabaseType getDatabaseType(String jdbcURL)
    {
        if (jdbcURL.trim().startsWith("jdbc:mysql"))
        {
            return DatabaseType.DATABASE_MYSQL;
        }
        else if (jdbcURL.trim().startsWith("jdbc:oracle"))
        {
            return DatabaseType.DATABASE_ORACLE;
        }
        else if (jdbcURL.trim().startsWith("jdbc:postgresql"))
        {
            return DatabaseType.DATABASE_POSTGRES;
        }
        else if (jdbcURL.trim().startsWith("jdbc:derby"))
        {
            return DatabaseType.DATABASE_DERBY;
        }
        else if (jdbcURL.trim().startsWith("jdbc:hsqldb"))
        {
            return DatabaseType.DATABASE_HSQLDB;
        }
        else
        {
            return DatabaseType.DATABASE_UNKNOWN;
        }
    }
    
    /**
     * Returns the SQL {@link DataSourceDefinition} corresponding to the given id.
     * @param id the id of the data source
     * @return the {@link DataSourceDefinition}.
     */
    public static DataSourceDefinition getDataSourceDefinition(String id)
    {
        return _sqlDataSourceManager.getDataSourceDefinition(id);
    }
}
