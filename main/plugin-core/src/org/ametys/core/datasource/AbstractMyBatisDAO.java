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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.PluginAware;

/**
 * Interface to be implemented by any object that wishes to have
 * access to one or multiple SqlMapClient.
 */
public abstract class AbstractMyBatisDAO extends AbstractLogEnabled implements Contextualizable, Serviceable, PluginAware, Configurable, Initializable
{
    private SqlSessionFactory _sessionFactory;
    private SQLDataSourceManager _sqlDataSourceManager;
    private String _contextPath;
    private String _pluginName;
    private String _dataSourceId;
    private Set<SqlMap> _sqlMaps;
    
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        Context ctx = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _contextPath = ctx.getRealPath("/");
    }

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _sqlDataSourceManager = (SQLDataSourceManager) manager.lookup(SQLDataSourceManager.ROLE);
    }
    
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration dataSourceConf = configuration.getChild("datasource", true);
        String dataSourceConfParam = dataSourceConf.getValue(ConnectionHelper.CORE_POOL_CONFIG_PARAM);
        String dataSourceConfType = dataSourceConf.getAttribute("type", "config");
        
        if (StringUtils.equals(dataSourceConfType, "config"))
        {
            _dataSourceId = Config.getInstance().getValueAsString(dataSourceConfParam);
        }
        else // expecting type="id"
        {
            _dataSourceId = dataSourceConfParam;
        }
        
        _sqlMaps = new HashSet<>();
        Configuration[] sqlMaps = configuration.getChildren("sqlMap");
        for (Configuration sqlMapConf : sqlMaps)
        {
            String resourceSrc = sqlMapConf.getAttribute("resource", null);
            String configSrc = sqlMapConf.getAttribute("config", null);
            
            if (StringUtils.isBlank(resourceSrc) && StringUtils.isBlank(configSrc))
            {
                throw new ConfigurationException("The sqlmap configuration must have a 'resource' or 'config' attribute.", sqlMapConf);
            }
            if (StringUtils.isNotBlank(resourceSrc) && StringUtils.isNotBlank(configSrc))
            {
                throw new ConfigurationException("The sqlmap configuration can't have both 'resource' and 'config' attributes.", sqlMapConf);
            }
            
            SqlMap sqlMap = new SqlMap();
            
            if (StringUtils.isNotBlank(configSrc))
            {
                sqlMap.setSource(configSrc);
                sqlMap.setSourceType("config");
            }
            else
            {
                sqlMap.setSource(resourceSrc);
                sqlMap.setSourceType("resource");
            }
            
            _sqlMaps.add(sqlMap);
        }
    }
    
    public void initialize() throws Exception
    {
        DataSource dataSource = _sqlDataSourceManager.getSQLDataSource(_dataSourceId);
        
        if (dataSource == null)
        {
            throw new ConfigurationException("Invalid datasource id: " + _dataSourceId);
        }
        
        SqlSessionFactoryBuilder sessionFactoryBuilder = new SqlSessionFactoryBuilder();
        
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment env = new Environment(_dataSourceId, transactionFactory, dataSource);
        
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration(env);
        config.setCacheEnabled(true);
        config.setLazyLoadingEnabled(true);
        
        for (SqlMap sqlMap : _sqlMaps)
        {
            String sourceType = sqlMap.getSourceType();
            String source = sqlMap.getSource();
        
            @SuppressWarnings("resource")
            InputStream mapperStream = null;
            String mapperLocation = null;

            try
            {
                if ("config".equals(sourceType))
                {
                    File file = null;
                    if (source.startsWith("/"))
                    {
                        // Absolute path (from the root context path).
                        file = new File(_contextPath, source);
                    }
                    else
                    {
                        // Relative path
                        File pluginDir = PluginsManager.getInstance().getPluginLocation(_pluginName);
                        file = new File(pluginDir, source);
                    }
                    
                    mapperLocation = file.toURI().toASCIIString();
                    mapperStream = new FileInputStream(file);
                }
                else
                {
                    mapperLocation = source;
                    mapperStream = getClass().getResourceAsStream(source);
                }
                
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Initialized mybatis mapper at location '{}' for datasource id '{}'", mapperLocation, _dataSourceId);
                }

                XMLMapperBuilder mapperParser = new XMLMapperBuilder(mapperStream, config, mapperLocation, config.getSqlFragments());
                mapperParser.parse();
            }
            finally
            {
                IOUtils.closeQuietly(mapperStream);
            }
        }

        _sessionFactory = sessionFactoryBuilder.build(config);
    }
    
    /**
     * Returns the myBatis {@link SqlSession}.
     * @return the myBatis {@link SqlSession}.
     */
    protected SqlSession getSession()
    {
        return getSession(false);
    }
    
    /**
     * Returns the myBatis {@link SqlSession}.
     * @param autoCommit if the underlying Connection should auto commit statements.
     * @return the myBatis {@link SqlSession}.
     */
    protected SqlSession getSession(boolean autoCommit)
    {
        return _sessionFactory.openSession(autoCommit);
    }
    
    class SqlMap
    {
        private String _source;
        private String _sourceType;
        
        public String getSource()
        {
            return _source;
        }
        
        public void setSource(String source)
        {
            _source = source;
        }
        
        public String getSourceType()
        {
            return _sourceType;
        }
        
        public void setSourceType(String sourceType)
        {
            _sourceType = sourceType;
        }
    }
}
