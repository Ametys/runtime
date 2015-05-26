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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * DOM Layer over a string
 */
public class StringElement extends AbstractAmetysElement<String>
{
    private String _tagName;
    private String _attributName;
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
        super(attributeValue);
        this._tagName = tagName;
        this._attributName = attributeName;
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
        Map<String, AmetysAttribute> attrs = new HashMap<>();
        if (_attributName != null)
        {
            attrs.put(_attributName, new AmetysAttribute(_attributName, _attributName, null, _object, this));
        }
        return attrs;
    }
    
    @Override
    public Node getFirstChild()
    {
        return this._data != null ? new AmetysText(this._data, this) : null;
    }
}
