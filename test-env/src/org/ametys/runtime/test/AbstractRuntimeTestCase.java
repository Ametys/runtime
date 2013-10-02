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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.logger.Log4JLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.IOUtils;
import org.xml.sax.XMLReader;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.LoggerFactory;

/**
 * Abstract test case for all Runtime test cases.
 */
public abstract class AbstractRuntimeTestCase extends TestCase
{
    static
    {
        LoggerFactory.setup(new Log4JLoggerManager());
    }
    
    /** The cocoon wrapper. */
    protected CocoonWrapper _cocoon;

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
    
    /**
     * Configures the RuntimeConfig with the given file
     * @param fileName the name of the config file
     * @param contextPath the test application path
     * @throws Exception if the Runtime config cannot be loaded
     */
    protected void _configureRuntime(String fileName, String contextPath) throws Exception
    {
        InputStream runtime = null;
        InputStream external = null;
        InputStream xsd = null;

        try
        {
            // Validation du runtime.xml sur le schéma runtime.xsd
            xsd = getClass().getResourceAsStream("/org/ametys/runtime/servlet/runtime.xsd");
            Schema schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(xsd));
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setSchema(schema);
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            
            DefaultConfigurationBuilder runtimeConfBuilder = new DefaultConfigurationBuilder(reader);
            
            File runtimeFile = new File(fileName);
            runtime = new FileInputStream(runtimeFile);
            Configuration runtimeConf = runtimeConfBuilder.build(runtime, fileName);
            
            // look for external.xml next to the runtime.xml file
            File externalFile = new File(runtimeFile.getParentFile(), "external-locations.xml");
            
            Configuration externalConf = null;
            if (externalFile.exists())
            {
                DefaultConfigurationBuilder externalConfBuilder = new DefaultConfigurationBuilder();
                external = new FileInputStream(externalFile);
                externalConf = externalConfBuilder.build(external, fileName);
            }
            
            RuntimeConfig.configure(runtimeConf, externalConf, contextPath);
        }
        finally
        {
            IOUtils.closeQuietly(xsd);
            IOUtils.closeQuietly(runtime);
            IOUtils.closeQuietly(external);
        }
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
     * @throws Exception if an error occured
     */
    protected CocoonWrapper _startApplication(String runtimeFile, String configFile, String contextPath) throws Exception
    {
        _configureRuntime(runtimeFile, contextPath);
        Config.setFilename(configFile);
        return _startCocoon(contextPath);
    }
}
