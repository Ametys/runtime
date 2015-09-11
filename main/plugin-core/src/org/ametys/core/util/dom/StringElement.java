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
package org.ametys.core.util.dom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * DOM Layer over a tag name, attributes and String value.
 */
public class StringElement extends AbstractAmetysElement<String>
{
    private String _tagName;
    private Map<String, String> _attributes;
    
    /**
     * Create a string element
     * @param tagName The tag name
     * @param data The data value.
     */
    public StringElement(String tagName, String data)
    {
        this(tagName, (Map<String, String>) null, data);
    }

    /**
     * Create a string element
     * @param tagName The tag name
     * @param attributeName The attribute name.
     * @param attributeValue The attribute value.
     */
    public StringElement(String tagName, String attributeName, String attributeValue)
    {
        this(tagName, Collections.singletonMap(attributeName, attributeValue), null);
    }

    /**
     * Create a string element
     * @param tagName The tag name
     * @param attributeName The attribute with the value.
     * @param attributeValue The attribute value.
     * @param data The data value.
     */
    public StringElement(String tagName, String attributeName, String attributeValue, String data)
    {
        this(tagName, Collections.singletonMap(attributeName, attributeValue), data);
    }

    /**
     * Create a string element
     * @param attributes The attributes names and values.
     * @param tagName The tag name.
     */
    public StringElement(String tagName, Map<String, String> attributes)
    {
        this(tagName, attributes, null);
    }

    /**
     * Create a string element
     * @param tagName The tag name
     * @param attributes The attributes names and values.
     * @param data The data value.
     */
    public StringElement(String tagName, Map<String, String> attributes, String data)
    {
        super(data);
        _tagName = tagName;
        _attributes = attributes;
    }

    @Override
    public String getTagName()
    {
        return _tagName;
    }

    @Override
    protected Map<String, AmetysAttribute> _lookupAttributes()
    {
        Map<String, AmetysAttribute> attrs = new HashMap<>();
        
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
        return _object != null ? new AmetysText(_object, this) : null;
    }
}
