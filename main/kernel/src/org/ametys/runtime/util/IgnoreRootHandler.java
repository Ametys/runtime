/*
 *  Copyright 2012 Anyware Services
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
