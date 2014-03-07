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

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is intended to be use as a simple helper to construct Maps from
 * SAX events. <br>
 * The incoming SAX document must follow this structure :<br>
 * 
 * &lt;root> <br>
 * &nbsp;&nbsp;&lt;Name1>value1&lt;/Name1> <br>
 * &nbsp;&nbsp;&lt;Name2>value2&lt;/Name2> <br>
 * &nbsp;&nbsp;... <br>
 * &lt;/root> <br>
 * <br>
 * Each pair Name/Value will be put (as Strings) in the constructed Map <br>
 */
public class MapHandler extends DefaultHandler
{
    // The object being constructed
    private Map<String, String> _map;

    // level in the XML tree. Root is at level 1, and data at level 2
    private int _level;

    // current characters from SAX events
    private StringBuffer _currentString;

    /**
     * Create a map handler
     * @param map Map to fill
     */
    public MapHandler(Map<String, String> map)
    {
        _map = map;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        _level++;
        _currentString = new StringBuffer();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        _currentString.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (_level == 2)
        {
            // If we are at the "data" level
            String strValue = _currentString.toString();
            _map.put(qName, strValue);
        }

        _level--;
    }
}
