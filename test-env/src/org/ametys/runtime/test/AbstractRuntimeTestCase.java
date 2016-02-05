/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.data.AmetysHomeLock;
import org.ametys.runtime.data.AmetysHomeLockException;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * Abstract test case for all Runtime test cases.
 */
public abstract class AbstractRuntimeTestCase extends TestCase
{
    /** Ametys home directory path relative to the context path of the test environment. **/
    public static final String AMETYS_HOME_DIR = "/WEB-INF/data";

    /** Default SQL datasource file */
    private static String __DEFAULT_SQL_DATASOURCE_FILE = "test/environments/datasources/datasource-mysql.xml";
    
    /** Default LDAP datasource file */
    private static String __DEFAULT_LDAP_DATASOURCE_FILE = "test/environments/datasources/datasource-ldap.xml"; 
    
    /** The cocoon wrapper. */
    protected CocoonWrapper _cocoon;

    /** The lock on Ametys home */
    protected AmetysHomeLock _ametysHomeLock;
    
    /** Logger for traces */
    protected Logger _logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a test case.
     */
    public AbstractRuntimeTestCase()
    {
        // Nothing to do
    }

    /**
     * Constructs a test case with the given name.
     * @param name the test case name.
     */
    public AbstractRuntimeTestCase(String name)
    {
        super(name);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (_ametysHomeLock != null)
        {
            if (_logger.isInfoEnabled())
            {
                _logger.info("Releasing lock on Ametys home");
            }
            
            _ametysHomeLock.release();
            _ametysHomeLock = null;
        }
        
        super.tearDown();
    }
    
    /**
     * Initialize the Ametys home directory and acquire a lock on it.
     * @param contextPath the test application path
     * @return The Ametys home directory
     * @throws AmetysHomeLockException If an error occurs while acquiring the lock on Ametys home
     */
    protected File _initAmetysHome(String contextPath) throws AmetysHomeLockException
    {
        String ametysHomePath = contextPath + AMETYS_HOME_DIR;
        
        if (_logger.isInfoEnabled())
        {
            _logger.info("Acquiring lock on " + ametysHomePath);
        }
        
        // Acquire the lock on Ametys home
        File ametysHome = new File(ametysHomePath);
        ametysHome.mkdirs();
        
        _ametysHomeLock = new AmetysHomeLock(ametysHome);
        _ametysHomeLock.acquire();
        
        return ametysHome;
    }
    
    /**
     * Configures the RuntimeConfig with the given file
     * @param fileName the name of the config file
     * @param contextPath the test application path
     * @throws Exception if the Runtime config cannot be loaded
     */
    protected void _configureRuntime(String fileName, String contextPath) throws Exception
    {
        File ametysHome = _initAmetysHome(contextPath);
        
        File runtimeConfigFile = new File(fileName);
        
        Configuration runtimeConf = null;
        try
        {
            // XML Schema validation
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            
            URL schemaURL = getClass().getResource("/org/ametys/runtime/servlet/runtime-4.0.xsd");
            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaURL);
            factory.setSchema(schema);
            
            XMLReader reader = factory.newSAXParser().getXMLReader();
            DefaultConfigurationBuilder runtimeConfBuilder = new DefaultConfigurationBuilder(reader);
            
            try (InputStream runtime = new FileInputStream(runtimeConfigFile))
            {
                runtimeConf = runtimeConfBuilder.build(runtime, runtimeConfigFile.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            _logger.error("Unable to load runtime file at '" + fileName + "'. PluginsManager will enter in safe mode.", e);
        }
        
        Configuration externalConf = null;
        DefaultConfigurationBuilder externalConfBuilder = new DefaultConfigurationBuilder();

        File externalConfigFile = new File(runtimeConfigFile.getParentFile(), "external-locations.xml");
        if (externalConfigFile.exists())
        {
            try (InputStream external = new FileInputStream(externalConfigFile))
            {
                externalConf = externalConfBuilder.build(external, externalConfigFile.getAbsolutePath());
            }
        }
        
        RuntimeConfig.configure(runtimeConf, externalConf, ametysHome, contextPath);
    }
    
    /**
     * Start Cocoon (and the plugin manager)
     * @param applicationPath the environment application
     * @return the CocoonWrapper used to process URIs
     * @throws Exception if an error occured
     */
    protected CocoonWrapper _startCocoon(String applicationPath) throws Exception
    {
        // Set this property in order to avoid a System.err.println (CatalogManager.java)
        if (System.getProperty("xml.catalog.ignoreMissing") == null)
        {
            System.setProperty("xml.catalog.ignoreMissing", "true");
        }
        
        _cocoon = new CocoonWrapper(applicationPath, "tmp/work");
        _cocoon.initialize();
        
        return _cocoon;
    }
    
    /**
     * Starts the application. This a shorthand to _configureRuntime, then _startCocoon.
     * @param runtimeFile the name of the runtime file used
     * @param configFile the name of the config file
     * @param contextPath the environment application
     * @return the CocoonWrapper used to process URIs
     * @throws Exception if an error occurred
     */
    protected CocoonWrapper _startApplication(String runtimeFile, String configFile, String contextPath) throws Exception
    {
        return _startApplication(runtimeFile, configFile, __DEFAULT_SQL_DATASOURCE_FILE, __DEFAULT_LDAP_DATASOURCE_FILE, contextPath);
    }
    
    /**
     * Starts the application with the default LDAP data source and the provided sql data source file. This a shorthand to _configureRuntime, then _startCocoon.
     * @param runtimeFile the name of the runtime file used
     * @param configFile the name of the config file
     * @param sqlDataSourceFile the name of the SQL data source file
     * @param contextPath the environment application
     * @return the CocoonWrapper used to process URIs
     * @throws Exception if an error occurred
     */
    protected CocoonWrapper _startApplication(String runtimeFile, String configFile, String sqlDataSourceFile, String contextPath) throws Exception
    {
        return _startApplication(runtimeFile, configFile, sqlDataSourceFile, __DEFAULT_LDAP_DATASOURCE_FILE, contextPath);
    }
    
    /**
     * Starts the application with the provided SQL and LDAP data sources files. This a shorthand to _configureRuntime, then _startCocoon.
     * @param runtimeFile the name of the runtime file used
     * @param configFile the name of the config file
     * @param sqlDataSourceFile the name of the SQL data source file
     * @param ldapDataSourceFile the name of the LDAP data source file
     * @param contextPath the environment application
     * @return the CocoonWrapper used to process URIs
     * @throws Exception if an error occurred
     */
    protected CocoonWrapper _startApplication(String runtimeFile, String configFile, String sqlDataSourceFile, String ldapDataSourceFile, String contextPath) throws Exception
    {
        _configureRuntime(runtimeFile, contextPath);
        
        Config.setFilename(configFile);
        
        SQLDataSourceManager.setFilename(sqlDataSourceFile);
        LDAPDataSourceManager.setFilename(ldapDataSourceFile);
        
        return _startCocoon(contextPath);
    }
}
