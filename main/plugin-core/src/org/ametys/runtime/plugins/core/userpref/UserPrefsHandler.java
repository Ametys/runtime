/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.plugins.core.userpref;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is intended to be use as a simple helper to construct Maps from
 * SAX events. <br>
 * The incoming SAX document must follow this structure :<br>
 * 
 * &lt;UserPreferences&gt; <br>
 * &nbsp;&nbsp;&lt;Name1&gt;value1&lt;/Name1&gt; <br>
 * &nbsp;&nbsp;&lt;Name2&gt;value2&lt;/Name2&gt; <br>
 * &nbsp;&nbsp;... <br>
 * &lt;/UserPreferences&gt; <br>
 * <br>
 * 
 * or<br>
 * &lt;UserPreferences&gt; <br>
 * &nbsp;&nbsp;&lt;preference id="Name1"&gt;value1&lt;/preference&gt; <br>
 * &nbsp;&nbsp;&lt;preference id="Name1"&gt;value2&lt;/preference&gt; <br>
 * &nbsp;&nbsp;... <br>
 * &lt;/UserPreferences&gt; <br>
 * <br>
 * Each pair Name/Value will be put (as Strings) in the constructed Map <br>
 */
public class UserPrefsHandler extends DefaultHandler
{
    // The object being constructed
    private Map<String, String> _map;

    // level in the XML tree. Root is at level 1, and data at level 2
    private int _level;

    // current characters from SAX events
    private StringBuffer _currentString;

    private int _version;
    
    private String _userPrefId;
    
    /**
     * Create a map handler
     * @param map Map to fill
     */
    public UserPrefsHandler(Map<String, String> map)
    {
        _map = map;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (_level == 0)
        {
            _version = attributes.getValue("version") != null ? Integer.valueOf(attributes.getValue("version")) : 1;
        }
            
        _level++;
        _currentString = new StringBuffer();
        
        if (_level == 2 && _version == 2)
        {
            _userPrefId = attributes.getValue("id");
        }
        
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
            
            if (_version == 2)
            {
                _map.put(_userPrefId, strValue);
                _userPrefId = null;
            }
            else
            {
                _map.put(qName, strValue);
            }
            
        }

        _level--;
    }
}
