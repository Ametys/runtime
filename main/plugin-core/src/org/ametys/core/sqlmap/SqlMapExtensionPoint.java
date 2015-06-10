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
package org.ametys.core.sqlmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.datasource.DataSourceExtensionPoint;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;

/**
 * This extension point is used to list the SqlMap handled by the plugins or the application,
 * create these SqlMap instances and inject them in avalon components.
 */
public class SqlMapExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<SqlMapClientComponentProvider>
{
    /** The avalon ROLE. */
    public static final String ROLE = SqlMapExtensionPoint.class.getName();
    
    /** The root context path. */
    String _contextPath;
    /** The SqlMaps defined as extensions grouped by pool. */
    private Map<String, Set<SqlMap>> _sqlMaps;
    /** The SqlMapClients grouped by pool. */
    private Map<String, SqlMapClient> _sqlMapsClients;
    /** The extensions with their data sources. */
    private Map<String, Set<String>> _extensions;
    /** Source resolver. */
    private SourceResolver _sourceResolver;
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        _sqlMaps = new HashMap<>();
        _sqlMapsClients = new HashMap<>();
        _extensions = new HashMap<>();
        _sourceResolver = (SourceResolver) _cocoonManager.lookup(SourceResolver.ROLE);
        
        Context ctx = (Context) _context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _contextPath = ctx.getRealPath("/");
    }
    
    @Override
    protected void addComponent(String pluginName, String featureName, String role, Class<? extends SqlMapClientComponentProvider> clazz, Configuration configuration) throws ComponentException
    {
        Set<String> dataSources = new HashSet<>();
        
        try
        {
            Configuration[] sqlMaps = configuration.getChildren("sqlMap");
            
            for (Configuration sqlMapConf : sqlMaps)
            {
                String dataSource = sqlMapConf.getAttribute("datasource", ConnectionHelper.CORE_POOL_NAME);
                
                String resourceSrc = sqlMapConf.getAttribute("resource", "");
                String configSrc = sqlMapConf.getAttribute("config", "");
                
                if (StringUtils.isBlank(resourceSrc) && StringUtils.isBlank(configSrc))
                {
                    throw new ConfigurationException("The sqlmap configuration must have a 'resource' or 'config' attribute.", sqlMapConf);
                }
                if (StringUtils.isNotBlank(resourceSrc) && StringUtils.isNotBlank(configSrc))
                {
                    throw new ConfigurationException("The sqlmap configuration can't have both 'resource' and 'config' attributes.", sqlMapConf);
                }
                
                Set<SqlMap> sqlMapsForCurrentDataSource = _sqlMaps.get(dataSource);
                
                if (sqlMapsForCurrentDataSource == null)
                {
                    sqlMapsForCurrentDataSource = new HashSet<>();
                    
                    _sqlMaps.put(dataSource, sqlMapsForCurrentDataSource);
                }
                
                SqlMap sqlMap = new SqlMap();
                sqlMap.setPluginName(pluginName);
                
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
                
                sqlMapsForCurrentDataSource.add(sqlMap);
                
                dataSources.add(dataSource);
            }
        }
        catch (ConfigurationException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The plugin '" + pluginName + "." + featureName + "' has a sqlmap extension but has an incorrect configuration", e);
            }
        }
        
        _extensions.put(role, dataSources);
        
        super.addComponent(pluginName, featureName, role, clazz, configuration);
    }

    @Override
    public void initializeExtensions() throws Exception
    {
        super.initializeExtensions();
        
        for (String poolName : _sqlMaps.keySet())
        {
            Set<SqlMap> sqlMaps = _sqlMaps.get(poolName);
            
            DataSourceExtensionPoint dsExtPoint = (DataSourceExtensionPoint) _cocoonManager.lookup(DataSourceExtensionPoint.ROLE);
            
            // Retrieve data source
            DataSource dataSource = dsExtPoint.getExtension(poolName);
            
            if (dataSource == null)
            {
                throw new Exception("Invalid pool name: " + poolName);
            }
                
            // Create config file
            InputStream configFileIs = _createConfigFile(sqlMaps);
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Creating ibatis SqlMapClient instance for pool: " + poolName);
            }
            
            // Create SqlMap instance
            SqlMapClient sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(configFileIs);
            
            if (!(sqlMapClient instanceof ExtendedSqlMapClient))
            {
                throw new IllegalStateException("Unable to set data source, invalid sql map implementation: " + sqlMapClient.getClass().getName());
            }
            
            ExtendedSqlMapClient extendedSqlMapClient = (ExtendedSqlMapClient) sqlMapClient;
            
            JdbcTransactionConfig transactionConfig = new JdbcTransactionConfig();
            transactionConfig.setDataSource(dataSource);
            transactionConfig.setMaximumConcurrentTransactions(extendedSqlMapClient.getDelegate().getMaxTransactions());
            
            TransactionManager transactionManager = new TransactionManager(transactionConfig);
            
            // Force commit to ensure data are always up to date
            transactionManager.setForceCommit(true);
            
            extendedSqlMapClient.getDelegate().setTxManager(transactionManager);
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("End of creating ibatis SqlMapClient instance for pool: " + poolName);
            }
             
            _sqlMapsClients.put(poolName, sqlMapClient);
        }
        
        for (String componentProvider : _extensions.keySet())
        {
            Set<String> poolNames = _extensions.get(componentProvider);
            
            Map<String, SqlMapClient> instances = new HashMap<>();
            
            for (String poolName : poolNames)
            {
                instances.put(poolName, _sqlMapsClients.get(poolName));
            }
            
            SqlMapClientComponentProvider sqlMapClientComponentProvider = _manager.lookup(componentProvider);
            
            // Retrieve the target component which will be injected with SqlMap
            Object component = _cocoonManager.lookup(sqlMapClientComponentProvider.getComponentRole());
            
            if (!(component instanceof SqlMapClientsAware))
            {
                throw new Exception("Invalid class: " + component.getClass().getName() + " must implement: "
                                  + SqlMapClientsAware.class.getName());
            }

            // Inject SqlMap instances
            ((SqlMapClientsAware) component).setSqlMapClients(instances);
        }
    }

    private InputStream _createConfigFile(Set<SqlMap> sqlMaps) throws Exception
    {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            // Créer un transformer
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();

            // Ecrire dans le buffer
            th.setResult(new StreamResult(os));

            Properties format = new Properties();
            
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.INDENT, "yes");
            format.put(OutputKeys.ENCODING, "UTF-8");
            format.put(OutputKeys.DOCTYPE_PUBLIC, "-//iBATIS.com//DTD SQL Map Config 2.0//EN");
            format.put(OutputKeys.DOCTYPE_SYSTEM, "http://www.ibatis.com/dtd/sql-map-config-2.dtd");
            
            th.getTransformer().setOutputProperties(format);

            // SAXer la configuration dans le transformer
            _saxConfig(th, sqlMaps);
            
            byte[] rawConfig = os.toByteArray();
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("SqlMap config used:" + System.getProperty("line.separator")
                                + new String(rawConfig, "UTF-8"));
            }
            
            return new ByteArrayInputStream(rawConfig);
        }
    }

    private void _saxConfig(ContentHandler ch, Set<SqlMap> sqlMaps) throws Exception
    {
        ch.startDocument();
        XMLUtils.startElement(ch, "sqlMapConfig");
        
        XMLUtils.createElement(ch, "settings", _getSettingsAttributes());
        
        for (SqlMap sqlMap : sqlMaps)
        {
            sqlMap.toSax(ch);
        }
        
        XMLUtils.endElement(ch, "sqlMapConfig");
        ch.endDocument();
    }
    
    private Attributes _getSettingsAttributes() throws Exception
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        Map<String, String> attributes = new LinkedHashMap<>(4);
        
        // Get default values
        try (InputStream defaultConfigIs = getClass().getResourceAsStream("default-sql-map-config.xml"))
        {
            saxParser.parse(defaultConfigIs, new RootAttributesToMapHandler(attributes));
        }
        
        String configFileName = "context://WEB-INF/param/sql-map-config.xml";
        
        Source configSource = _sourceResolver.resolveURI(configFileName);
        
        if (configSource.exists())
        {
            try (InputStream configStream = configSource.getInputStream())
            {
                // Override with application values 
                saxParser.parse(configStream, new RootAttributesToMapHandler(attributes));
            }
        }
        else
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Unable to read SqlMap configuration file: '" + configFileName + "', fallback to default settings");
            }                
        }
        
        // Convertir en attributs XML
        AttributesImpl settingsAttrs = new AttributesImpl();
        
        for (Map.Entry<String, String> entry : attributes.entrySet())
        {
            settingsAttrs.addCDATAAttribute(entry.getKey(), entry.getValue());
        }
        
        return settingsAttrs;
    }
    
    /**
     * Handler for storing root attributs into a {@link Map}.
     */
    protected static final class RootAttributesToMapHandler extends DefaultHandler
    {
        private Map<String, String> _attributes;
        // level in the XML tree. Root is at level 1, and data at level 2
        private int _level;

        /**
         * Create the handler.
         * @param attributes the map to store attributes into. 
         */
        public RootAttributesToMapHandler(Map<String, String> attributes)
        {
            _attributes = attributes;
        }
        
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
        {
            _level++;
            
            if (_level == 1)
            {
                // Ne stocker que les attributs de l'élément racine
                int length = attributes.getLength();
                
                for (int i = 0; i < length; i++)
                {
                    _attributes.put(attributes.getQName(i), attributes.getValue(i));
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException
        {
            _level--;
        }
    }
    
    /**
     * Class representing an SqlMap configuration.<br>
     * It can be directly SAXed into the sql map configuration content handler, will generate a &lt;sqlmap resource="..."&gt; or &lt;sqlmap url="..."&gt; depending on the source type.
     * <ul>
     *   <li>If the source type is "resource", the source is searched relatively to the class path and included in the "resource" attribute.</li>
     *   <li>If the source type is "config", the source is computed either from the root application context path if the source begins with "/", or from the declaring plugin folder if the source doesn't begin with "/". The computed path will be generated as the "url" sqlmap attribute, prepended with the "file://" protocol.</li>
     * </ul>  
     */
    protected class SqlMap
    {
        /** The source path. The base path depends on the source type. */
        protected String _source;
        
        /** The source type, can be "resource" or "config". */
        protected String _sourceType;
        
        /** The name of the plugin in which the SqlMap is declared, used to build the source path. */
        protected String _pluginName;
        
        /**
         * Build a SqlMap object.
         */
        public SqlMap()
        {
            this(null, null, null);
        }
        
        /**
         * Build a SqlMap object.
         * @param source the source path.
         * @param sourceType the source type, "resource" or "config".
         * @param pluginName the plugin name.
         */
        public SqlMap(String source, String sourceType, String pluginName)
        {
            super();
            this._source = source;
            this._sourceType = sourceType;
            this._pluginName = pluginName;
        }
        
        /**
         * Get the source.
         * @return the source
         */
        public String getSource()
        {
            return _source;
        }
        
        /**
         * Set the source.
         * @param source the source to set
         */
        public void setSource(String source)
        {
            this._source = source;
        }
        
        /**
         * Get the sourceType.
         * @return the sourceType
         */
        public String getSourceType()
        {
            return _sourceType;
        }
        
        /**
         * Set the sourceType.
         * @param sourceType the sourceType to set
         */
        public void setSourceType(String sourceType)
        {
            this._sourceType = sourceType;
        }
        
        /**
         * Get the pluginName.
         * @return the pluginName
         */
        public String getPluginName()
        {
            return _pluginName;
        }
        
        /**
         * Set the pluginName.
         * @param pluginName the pluginName to set
         */
        public void setPluginName(String pluginName)
        {
            this._pluginName = pluginName;
        }
        
        /**
         * Generate the SqlMap configuration.
         * @param handler the content handler to generate into.
         * @throws SAXException if an error occurs generating the XML events.
         */
        public void toSax(ContentHandler handler) throws SAXException
        {
            if ("config".equals(_sourceType))
            {
                File file = null;
                if (_source.startsWith("/"))
                {
                    // Absolute path (from the root context path).
                    file = new File(_contextPath, _source);
                }
                else
                {
                    // Relative path
                    File pluginDir = PluginsManager.getInstance().getPluginLocation(_pluginName);
                    file = new File(pluginDir, _source);
                }
                
                AttributesImpl sqlMapAttrs = new AttributesImpl();
                
                sqlMapAttrs.addCDATAAttribute("url", file.toURI().toASCIIString());
                
                XMLUtils.createElement(handler, "sqlMap", sqlMapAttrs);
            }
            else
            {
                AttributesImpl sqlMapAttrs = new AttributesImpl();
                
                sqlMapAttrs.addCDATAAttribute("resource", _source);
                
                XMLUtils.createElement(handler, "sqlMap", sqlMapAttrs);
            }
        }
    }
}
