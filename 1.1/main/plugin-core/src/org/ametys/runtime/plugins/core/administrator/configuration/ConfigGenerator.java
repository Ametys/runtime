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
package org.ametys.runtime.plugins.core.administrator.configuration;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.config.ConfigManager;


/**
 * SAX the configuration model with current values of configuration
 */
public class ConfigGenerator extends AbstractGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "config");
        
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.toSAX(contentHandler);

        XMLUtils.endElement(contentHandler, "config");
        contentHandler.endDocument();
    }
}
