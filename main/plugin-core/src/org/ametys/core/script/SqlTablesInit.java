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
package org.ametys.core.script;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.Init;
import org.ametys.runtime.plugin.component.PluginAware;

/**
 * Creates necessary SQL tables (if not already existing) at initialization.
 */
public class SqlTablesInit extends AbstractLogEnabled implements Init, Serviceable, Configurable, PluginAware
{
    /** Plugin name */
    protected String _pluginName;
    
    /** The data source identifer */
    protected String _dataSourceId;
    
    /** The set of configured table init scripts */
    protected Set<InitScript> _scripts;
    
    /** SQL data source manager */
    protected SQLDataSourceManager _sqlDataSourceManager;
    
    /** Source resolver */
    protected SourceResolver _sourceResolver;
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _sqlDataSourceManager = (SQLDataSourceManager) manager.lookup(SQLDataSourceManager.ROLE);
        _sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration dataSourceConf = configuration.getChild("datasource", false);
        if (dataSourceConf == null)
        {
            throw new ConfigurationException("The 'datasource' configuration node must be defined.", dataSourceConf);
        }
        
        String dataSourceConfParam = dataSourceConf.getValue();
        String dataSourceConfType = dataSourceConf.getAttribute("type", "config");
        
        if (StringUtils.equals(dataSourceConfType, "config"))
        {
            _dataSourceId = Config.getInstance().getValueAsString(dataSourceConfParam);
        }
        else // expecting type="id"
        {
            _dataSourceId = dataSourceConfParam;
        }
        
        _scripts = new HashSet<>();
        Configuration[] scripts = configuration.getChildren("script");
        
        for (Configuration scriptConf : scripts)
        {
            String pluginName = scriptConf.getAttribute("plugin", _pluginName);
            
            String testTable = scriptConf.getAttribute("testTable");
            if (StringUtils.isBlank(testTable))
            {
                throw new ConfigurationException("The test table attribute cannot be blank.");
            }
            
            String fileName = scriptConf.getValue();
            if (StringUtils.isBlank(fileName))
            {
                throw new ConfigurationException("The SQL file name cannot be blank.");
            }
            
            _scripts.add(new InitScript(pluginName, fileName, testTable));
        }
    }
    
    @Override
    public void init() throws Exception
    {
        try
        {
            // Test and create tables
            Connection connection = ConnectionHelper.getConnection(_dataSourceId);
            
            try
            {
                initTables(connection);
            }
            finally
            {
                ConnectionHelper.cleanup(connection);
            }
        }
        catch (Exception e)
        {
            String errorMsg = String.format("Error during SQL tables initialization for data source id: '%s'.",
                    StringUtils.defaultString(_dataSourceId));
            getLogger().error(errorMsg, e);
        }
    }
    
    /**
     * Execute the SQL script to create the tables if the test table does not
     * exist.
     * @param connection The database connection
     * @throws SQLException In an SQL exception occurs
     */
    protected void initTables(Connection connection) throws SQLException
    {
        String scriptFolder = null;
        
        String dbType = ConnectionHelper.getDatabaseType(connection);
        switch (dbType)
        {
            case ConnectionHelper.DATABASE_DERBY:
                scriptFolder = "derby"; break;
            case ConnectionHelper.DATABASE_HSQLDB:
                scriptFolder = "hsqldb"; break;
            case ConnectionHelper.DATABASE_MYSQL:
                scriptFolder = "mysql"; break;
            case ConnectionHelper.DATABASE_ORACLE:
                scriptFolder = "oracle"; break;
            case ConnectionHelper.DATABASE_POSTGRES:
                scriptFolder = "postgresql"; break;
            default:
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("This data source is not compatible with the automatic creation of the SQL tables. The tables will not be created. Data source id: '%s'", _dataSourceId));
                }
                
                return;
        }
        
        for (InitScript initScript : _scripts)
        {
            // location = plugin:PLUGIN_NAME://scripts/SCRIPT_FOLDER/SQL_FILENAME
            StringBuilder sb = new StringBuilder("plugin:").append(initScript._pluginName).append("://scripts/").append(scriptFolder).append('/');
            String locationPrefix = sb.toString();
            
            if (!tableExists(connection, initScript._testTable))
            {
                String location = locationPrefix + initScript._fileName;
                Source source = null;
                
                try
                {
                    source = _sourceResolver.resolveURI(location);
                    ScriptRunner.runScript(connection, source.getInputStream());
                }
                catch (IOException | SQLException e)
                {
                    getLogger().error(String.format("Unable to run the SQL script for file at location: %s.\nAll pendings script executions are aborted.", location), e);
                    return;
                }
                finally
                {
                    if (source != null)
                    {
                        _sourceResolver.release(source);
                    }
                }
            }
        }
    }
    
    /**
     * Checks whether the given table exists in the database.
     * @param connection The database connection
     * @param tableName the name of the table
     * @return true is the table exists
     * @throws SQLException In an SQL exception occurs
     */
    public boolean tableExists(Connection connection, String tableName) throws SQLException
    {
        ResultSet rs = null;
        boolean schemaExists = false;
        
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
        
        return schemaExists;
    }
    
    private static class InitScript
    {
        final String _pluginName;
        final String _fileName;
        final String _testTable;
        
        public InitScript(String pluginNameArg, String fileNameArg, String testTableArg)
        {
            _pluginName = pluginNameArg;
            _fileName = fileNameArg;
            _testTable = testTableArg;
        }
    }
}
