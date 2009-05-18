package org.ametys.runtime.datasource;

import javax.sql.DataSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.AbstractExtensionPoint;

/**
 * Extension point for declaring JDBC Datasources.
 */
public class DataSourceExtensionPoint extends AbstractExtensionPoint<DataSource>
{
    /** Avalon Role */
    public static final String ROLE = DataSourceExtensionPoint.class.getName();
    
    private static final String __CONFIG_ATTRIBUTE_NAME = "runtime-config-parameter";
    
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
            throw new ConfigurationException("Specified driver class does not exist", e);
        }
        
        GenericObjectPool connectionPool = new GenericObjectPool();
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, pass);
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        
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

    public void initializeExtensions() throws Exception
    {
        ConnectionHelper.setExtensionPoint(this);
    }
}
