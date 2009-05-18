/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.util.LoggerFactory;

/**
 * Helper class to retrieve java.sql.Connection from pools
 */
public final class ConnectionHelper
{
    /**
     * Constant for core Connection pool name
     */
    public static final String CORE_POOL_NAME = "runtime.datasource.core.jdbc.pool";
    
    /**
     * Enumeration for database type.<br>
     * Currently, only MySQL and Oracle are supported.
     */
    public enum DatabaseType
    {
        /** Database type is unknown */
        DATABASE_UNKNOWN,
        
        /** Database type is Mysql */
        DATABASE_MYSQL,
        
        /** Database type is Oracle */
        DATABASE_ORACLE, 
              
        /** Database type is Postgres */
        DATABASE_POSTGRES
    }
    
    // Logger for traces
    private static Logger _logger = LoggerFactory.getLoggerFor(ConnectionHelper.class.getName());

    private static DataSourceExtensionPoint _extensionPoint;
    
    private ConnectionHelper()
    {
        // empty constructor
    }
    
    /**
     * Initializes the Datasources holding the actual Connections
     * @param extensionPoint the application DataSourceExtensionPoint 
     */
    public static void setExtensionPoint(DataSourceExtensionPoint extensionPoint)
    {
        _extensionPoint = extensionPoint;
    }
    
    /**
     * Returns a Connection from the pool.
     * @param poolName the name of the Avalon connection pool
     * @return a java.sql.Connection to query a SQL database
     */
    public static Connection getConnection(String poolName)
    {
        if (_extensionPoint == null)
        {
            throw new IllegalStateException("ComponentSelector has not been properly set up. The method setSelector(selector) must be called before getting any Connection.");
        }

        DataSource dataSource;
        Connection conn = null;

        try
        {
            dataSource =  _extensionPoint.getExtension(poolName);
            conn = dataSource.getConnection();
        }
        catch (SQLException e)
        {
            _logger.error("Unable to get Connection from pool " + poolName, e);
            throw new IllegalArgumentException("Unable to get Connection from pool " + poolName);
        }

        return conn;
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
                // Fermer la connexion à la base
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
                // Fermer la connexion à la base
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
                // Fermer la connexion à la base
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
            LoggerFactory.getLoggerFor(ConnectionHelper.class).error("Cannot determine database type", e);
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
        else
        {
            return DatabaseType.DATABASE_UNKNOWN;
        }
    }
}
