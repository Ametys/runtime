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
package org.ametys.runtime.util;

import org.apache.excalibur.xml.sax.ContentHandlerProxy;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * "Pipe" handler ignoring startDocument() and endDocument() calls
 */
public class IgnoreRootHandler extends ContentHandlerProxy
{
    /**
     * Constructor
     * @param contentHandler the contentHandler to pass SAX events to
     */
    public IgnoreRootHandler(ContentHandler contentHandler)
    {
        super(contentHandler);
    }

    @Override
    public void startDocument() throws SAXException
    {
        // empty method
    }
    
    @Override
    public void endDocument() throws SAXException
    {
        // empty method
    }
}
