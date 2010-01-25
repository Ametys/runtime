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
import org.xml.sax.ext.LexicalHandler;

/**
 * "Proxy" handler ignoring startDocument() and endDocument() calls
 */
public class IgnoreRootHandler extends ContentHandlerProxy implements LexicalHandler
{
    private LexicalHandler _lexicalHandler;
    
    /**
     * Constructor
     * @param contentHandler the contentHandler to pass SAX events to. In case the <code>ContentHandler</code> also implements the <code>LexicalHandler</code> interface, it will be honoured.
     */
    public IgnoreRootHandler(ContentHandler contentHandler)
    {
        super(contentHandler);
        
        if (contentHandler instanceof LexicalHandler)
        {
            _lexicalHandler = (LexicalHandler) contentHandler;
        }
    }

    /**
     * Constructor
     * @param contentHandler the contentHandler to pass SAX events to
     * @param lexicalHandler the lexicalHandler to pass lexical events to. May be null.
     */
    public IgnoreRootHandler(ContentHandler contentHandler, LexicalHandler lexicalHandler)
    {
        super(contentHandler);
        
        _lexicalHandler = lexicalHandler;
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
    
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.comment(ch, start, length);
        }
    }
    
    public void startCDATA() throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.startCDATA();
        }
    }
    
    public void endCDATA() throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.endCDATA();
        }
    }
    
    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.startDTD(name, publicId, systemId);
        }
    }
    
    public void endDTD() throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.endDTD();
        }
    }
    
    public void startEntity(String name) throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.startEntity(name);
        }
    }

    public void endEntity(String name) throws SAXException
    {
        if (_lexicalHandler != null)
        {
            _lexicalHandler.endEntity(name);
        }
    }
}
