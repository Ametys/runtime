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
package org.ametys.runtime.util.cocoon;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * This generator generates a ActionResult tag surounding parameters.<br>
 * Usefull for pipeline that needs no generator.
 */
public class ActionResultGenerator extends AbstractGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "ActionResult");
        
        for (String parameterName : parameters.getNames())
        {
            XMLUtils.createElement(contentHandler, parameterName, parameters.getParameter(parameterName, ""));
        }
        
        XMLUtils.endElement(contentHandler, "ActionResult");
        contentHandler.endDocument();
    }
}
