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

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.log.Hierarchy;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.LoggerFactory;

/**
 * Abstract test case for all Runtime test cases
 *
 */
@SuppressWarnings("deprecation")
public abstract class AbstractTestCase extends TestCase
{
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    
    static
    {
        LoggerFactory.setup(Hierarchy.getDefaultHierarchy());
    }
    
    /**
     * Configures the RuntimeConfig with the given file
     * @param fileName the name of the config file
     */
    protected void _configureRuntime(String fileName)
    {
        Configuration conf;
        
        try
        {
            conf = new DefaultConfigurationBuilder().build(fileName);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        RuntimeConfig.configure(conf);
    }
    
    /**
     * Start the plugin manager
     * @param applicationPath the environment application
     * @throws Exception if an error occured
     */
    protected void _startCocoon(String applicationPath) throws Exception
    {
        /** Context avalon */
        DefaultContext context;
        /** Component manager avalon */
        CocoonComponentManager manager;
        /** Role manager*/
        RoleManager roleManager;
        
        CommandLineContext ctx = new CommandLineContext(applicationPath);
        ctx.enableLogging(LoggerFactory.getLoggerFor("ctx"));
        context = new DefaultContext();
        context.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, ctx);
        manager = new CocoonComponentManager();
        ContainerUtil.enableLogging(manager, LoggerFactory.getLoggerFor("manager"));
        ContainerUtil.contextualize(manager, context);
        manager.setLoggerManager(new LogKitLoggerManager(Hierarchy.getDefaultHierarchy()));
        
        Configuration conf = new DefaultConfigurationBuilder().build(getClass().getResourceAsStream("/org/apache/cocoon/cocoon.roles"));
        
        roleManager = new DefaultRoleManager();
        ContainerUtil.enableLogging(roleManager, LoggerFactory.getLoggerFor("roles"));
        ContainerUtil.configure(roleManager, conf);
        
        manager.setRoleManager(roleManager);

        Configuration conf2 = new DefaultConfigurationBuilder().build(applicationPath + "/WEB-INF/cocoon.xconf");
        ContainerUtil.configure(manager, conf2);
        ContainerUtil.initialize(manager);
    }
    
    /**
     * Validate an XML file against an XML schema
     * @param src the input XML file
     * @param xsd the XML schema
     * @throws Exception if an error occurs during validation
     */
    protected void _validate(InputStream src, InputStream xsd) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        factory.setAttribute(JAXP_SCHEMA_SOURCE, xsd);
        
        DocumentBuilder db = factory.newDocumentBuilder();
        db.setErrorHandler(new ErrorHandler()
        {
            public void error(SAXParseException exception) throws SAXException
            {
                throw exception;
            }
            
            public void fatalError(SAXParseException exception) throws SAXException
            {
                throw exception;
            }
            
            public void warning(SAXParseException exception) throws SAXException
            {
                throw exception;
            }
        });
        
        db.parse(src);
    }
}
