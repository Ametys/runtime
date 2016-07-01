/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Component with XML utils methods
 */
public class XMLUtils extends AbstractLogEnabled implements Component, Serviceable
{
    /** The avalon role */
    public static final String ROLE = XMLUtils.class.getName();
    
    /** The sax parser */
    protected SAXParser _saxParser;

    public void service(ServiceManager manager) throws ServiceException
    {
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
    }
    
    /**
     * Get a XML as a string and extract the text only
     * @param is The inputstream of XML
     * @return The text or null if the XML is not well formed
     */
    public String toString(InputStream is)
    {
        try
        {
            TxtHandler txtHandler = new TxtHandler();
            _saxParser.parse(new InputSource(is), txtHandler);
            return txtHandler.getValue();
        }
        catch (IOException | SAXException e)
        {
            getLogger().error("Cannot parse inputstream", e);
            return null;
        }
    }
    
    final class TxtHandler extends DefaultHandler
    {
        private StringBuilder _internal = new StringBuilder();
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            _internal.append(new String(ch, start, length));
        }
        
        public String getValue()
        {
            return _internal.toString();
        }
    }
}
