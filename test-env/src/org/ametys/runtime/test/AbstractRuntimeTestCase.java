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
package org.ametys.runtime.test;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.LoggerFactory;
import org.apache.avalon.excalibur.logger.Log4JLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.xml.sax.XMLReader;

/**
 * Abstract test case for all Runtime test cases.
 */
public abstract class AbstractRuntimeTestCase extends TestCase
{
    static
    {
        LoggerFactory.setup(new Log4JLoggerManager());
    }

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
     * @throws Exception if the Runtime config cannot be loaded
     */
    protected void _configureRuntime(String fileName) throws Exception
    {
        // Validation du runtime.xml sur le schéma runtime.xsd
        InputStream xsd = getClass().getResourceAsStream("/org/ametys/runtime/servlet/runtime.xsd");
        Schema schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(xsd));
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setSchema(schema);
        factory.setNamespaceAware(true);
        XMLReader reader = factory.newSAXParser().getXMLReader();
        
        DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder(reader);
        
        InputStream is = new FileInputStream(fileName);
        
        Configuration conf = confBuilder.build(is, fileName);
        
        RuntimeConfig.configure(conf);
    }
    
    /**
     * Start Cocoon (and the plugin manager)
     * @param applicationPath the environment application
     * @return the CocoonWrapper used to process URIs
     * @throws Exception if an error occured
     */
    protected CocoonWrapper _startCocoon(String applicationPath) throws Exception
    {
        CocoonWrapper cocoon = new CocoonWrapper(applicationPath, "tmp/work");
        cocoon.initialize();
        
        return cocoon;
    }
}
