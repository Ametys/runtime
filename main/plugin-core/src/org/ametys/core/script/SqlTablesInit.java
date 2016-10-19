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

import java.sql.Connection;
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
            Connection connection = null;
            try
            {
                connection = ConnectionHelper.getConnection(_dataSourceId);
                
                String scriptFolder = ConnectionHelper.getDatabaseType(connection);
                
                for (InitScript initScript : _scripts)
                {
                    SQLScriptHelper.createTableIfNotExists(connection, initScript._testTable, "plugin:" + initScript._pluginName + "://scripts/" + scriptFolder + "/" + initScript._fileName, _sourceResolver);
                }
            }
            finally
            {
                ConnectionHelper.cleanup(connection);
            }
        }
        catch (Exception e)
        {
            String errorMsg = String.format("Error during SQL tables initialization for data source id: '%s'.", StringUtils.defaultString(_dataSourceId));
            getLogger().error(errorMsg, e);
        }
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
