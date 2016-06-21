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
package org.ametys.runtime.util.dom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * DOM Layer over a string
 */
public class StringElement extends AbstractAmetysElement<String>
{
    private String _tagName;
    private Map<String, String> _attributes;
    private String _data;
    
    /**
     * Create a string element
     * @param tagName The tag name
     * @param attributeName The attribute with the value. Can be null.
     * @param attributeValue The attribute value. Can be null.
     * @param data The data value. Can be null.
     */
    public StringElement(String tagName, String attributeName, String attributeValue, String data)
    {
        super(tagName);
        this._tagName = tagName;
        this._attributes = Collections.singletonMap(attributeName, attributeValue);
        this._data = data;
    }
    
    /**
     * Create a string element
     * @param tagName The tag
     * @param attributeName The attribute with the value
     * @param attributeValue The attribute value
     */
    public StringElement(String tagName, String attributeName, String attributeValue)
    {
        super(tagName);
        this._tagName = tagName;
        this._attributes = Collections.singletonMap(attributeName, attributeValue);
        this._data = null;
    }
    
    /**
     * Create a string element
     * @param tagName The tag
     * @param attributes The attributes names and values.
     */
    public StringElement(String tagName, Map<String, String> attributes)
    {
        super(tagName);
        this._tagName = tagName;
        this._attributes = attributes;
        this._data = null;
    }
    
    /**
     * Create a string element
     * @param tagName The tag
     * @param attributes The attributes names and values.
     * @param data The data value. Can be null.
     */
    public StringElement(String tagName, Map<String, String> attributes, String data)
    {
        super(tagName);
        this._tagName = tagName;
        this._attributes = attributes;
        this._data = data;
    }

    @Override
    public String getTagName()
    {
        return _tagName;
    }

    @Override
    protected Map<String, AmetysAttribute> _lookupAttributes()
    {
        Map<String, AmetysAttribute> attrs = new HashMap<String, AmetysAttribute>();
        
        if (_attributes != null)
        {
            for (String name : _attributes.keySet())
            {
                attrs.put(name, new AmetysAttribute(name, name, null, _attributes.get(name), this));
            }
        }
        
        return attrs;
    }
    
    @Override
    public Node getFirstChild()
    {
        return this._data != null ? new AmetysText(this._data, this) : null;
    }
}
