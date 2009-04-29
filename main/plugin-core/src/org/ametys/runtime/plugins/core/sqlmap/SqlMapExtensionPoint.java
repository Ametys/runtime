package org.ametys.runtime.plugins.core.sqlmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
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

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;
import org.ametys.runtime.util.ConnectionHelper;

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
    
    /** The service manager. */
    private ServiceManager _cocoonManager;
    /** The SqlMaps defined as extensions grouped by pool. */
    private Map<String, Set<String>> _sqlMaps;
    /** The SqlMapClients grouped by pool. */
    private Map<String, SqlMapClient> _sqlMapsClients;
    /** The extensions with their data sources. */
    private Map<String, Set<String>> _extensions;
    /** Source resolver. */
    private SourceResolver _sourceResolver;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);
        _cocoonManager = manager;
    }
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        _sqlMaps = new HashMap<String, Set<String>>();
        _sqlMapsClients = new HashMap<String, SqlMapClient>();
        _extensions = new HashMap<String, Set<String>>();
        _sourceResolver = (SourceResolver) _cocoonManager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    protected void addComponent(String pluginName, String featureName, String role, Class<SqlMapClientComponentProvider> clazz, Configuration configuration) throws ComponentException
    {
        Set<String> dataSources = new HashSet<String>();
        
        try
        {
            Configuration[] sqlMaps = configuration.getChildren("sqlMap");
            
            for (Configuration sqlMap : sqlMaps)
            {
                String dataSource = sqlMap.getAttribute("datasource", ConnectionHelper.CORE_POOL_NAME);
                String src = sqlMap.getAttribute("resource");
                Set<String> sqlMapsForCurrentDataSource = _sqlMaps.get(dataSource);
                
                if (sqlMapsForCurrentDataSource == null)
                {
                    sqlMapsForCurrentDataSource = new HashSet<String>();
                    
                    _sqlMaps.put(dataSource, sqlMapsForCurrentDataSource);
                }
                
                sqlMapsForCurrentDataSource.add(src);
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
            Set<String> sqlMaps = _sqlMaps.get(poolName);
            
            // Retrieve data source
            DataSource dataSource = null;
            
            try
            {
                dataSource = new DataSourceWrapper(_getDataSourceComponent(poolName));
            }
            catch (ServiceException e)
            {
                throw new Exception("Invalid pool name: " + poolName, e);
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
            
            Map<String, SqlMapClient> instances = new HashMap<String, SqlMapClient>();
            
            for (String poolName : poolNames)
            {
                instances.put(poolName, _sqlMapsClients.get(poolName));
            }
            
            SqlMapClientComponentProvider sqlMapClientComponentProvider = _manager.lookup(componentProvider);
            
            // Retrieve the target component which will be injected with SqlMap
            Object component = _cocoonManager.lookup(sqlMapClientComponentProvider.getComponentRole());
            
            if (!(component instanceof SqlMapClientsAware))
            {
                throw new Exception("Invalid class: " + component.getClass().getName() + " must implements: "
                                  + SqlMapClientsAware.class.getName());
            }

            // Inject SqlMap instances
            ((SqlMapClientsAware) component).setSqlMapClients(instances);
        }
    }

    private DataSourceComponent _getDataSourceComponent(String poolName) throws ServiceException
    {
        ServiceSelector selector = null;
        
        try
        {
            selector = (ServiceSelector) _cocoonManager.lookup(DataSourceComponent.ROLE + "Selector");
            
            return (DataSourceComponent) selector.select(poolName);
        }
        finally
        {
            if (selector != null)
            {
                _cocoonManager.release(selector);
            }
        }
    }

    private InputStream _createConfigFile(Set<String> sqlMaps) throws Exception
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try
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
        finally
        {
            os.close();
        }
    }

    private void _saxConfig(ContentHandler ch, Set<String> sqlMaps) throws Exception
    {
        ch.startDocument();
        XMLUtils.startElement(ch, "sqlMapConfig");
        
        XMLUtils.createElement(ch, "settings", _getSettingsAttributes());
        
        for (String source : sqlMaps)
        {
            AttributesImpl sqlMapAttrs = new AttributesImpl();
            
            sqlMapAttrs.addCDATAAttribute("resource", source);
            
            XMLUtils.createElement(ch, "sqlMap", sqlMapAttrs);
        }
        
        XMLUtils.endElement(ch, "sqlMapConfig");
        ch.endDocument();
    }
    
    private Attributes _getSettingsAttributes() throws Exception
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        Map<String, String> attributes = new LinkedHashMap<String, String>(4);
        
        // Récupérer les valeurs par défaut
        InputStream defaultConfigIs = getClass().getResourceAsStream("default-sql-map-config.xml");
        
        try
        {
            saxParser.parse(defaultConfigIs, new RootAttributesToMapHandler(attributes));
        }
        finally
        {
            try
            {
                defaultConfigIs.close();
            }
            catch (IOException e)
            {
                getLogger().error("Unable to close input stream", e);
            }
        }
        
        String configFileName = "context://WEB-INF/param/sql-map-config.xml";
        
        Source configSource = null;
        InputStream configStream = null;
        
        try
        {
            configSource = _sourceResolver.resolveURI(configFileName);
            
            if (configSource.exists())
            {
                configStream = configSource.getInputStream();
                // Surcharger avec les préférences de l'application
                saxParser.parse(configStream, new RootAttributesToMapHandler(attributes));
            }
            else
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Unable to read SqlMap configuration file: '" + configFileName + "', fallback to default settings");
                }                
            }
        }
        finally
        {
            if (configStream != null)
            {
                try
                {
                    configStream.close();
                }
                catch (IOException e)
                {
                    if (getLogger().isWarnEnabled())
                    {
                        getLogger().warn("An error occured while closing config file '" + configFileName + "'", e);
                    }
                }
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
     * Data source wrapper for Avalon DataSourceComponent.
     */
    protected static final class DataSourceWrapper implements DataSource
    {
        /** Wrapped data source component. */
        protected final DataSourceComponent _datasource;
        /** Log writer. */
        protected PrintWriter _writer = new PrintWriter(System.out);
        /** Timeout. */
        protected int _timeout;
        
        /**
         * Data source wrapper constructor.
         * @param d the wrapped data source component.
         */
        public DataSourceWrapper(DataSourceComponent d)
        {
            _datasource = d;
        }

        public Connection getConnection() throws SQLException
        {
            return _datasource.getConnection();
        }

        public Connection getConnection(String username, String password) throws SQLException
        {
            return null;
        }

        public int getLoginTimeout() throws SQLException
        {
            return _timeout;
        }

        public PrintWriter getLogWriter() throws SQLException
        {
            return _writer;
        }

        public void setLoginTimeout(int seconds) throws SQLException
        {
            _timeout = seconds;
        }

        public void setLogWriter(PrintWriter out) throws SQLException
        {
            _writer = out;
        }
        
        // JDBC 4.0
        public boolean isWrapperFor(Class< ? > iface) throws SQLException
        {
            return false;
        }

        // JDBC 4.0
        public <T> T unwrap(Class<T> iface) throws SQLException
        {
            throw new SQLException("DataSourceWrapper is not really a wrapper.");
        }
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
}
