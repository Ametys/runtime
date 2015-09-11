/*
 *  Copyright 2015 Anyware Services
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM Layer over a Map. Only String and Object values are allowed.
 */
public class MapElement extends AbstractAmetysElement
{
    private Map<String, ? extends Object> _values;
    
    /**
     * Constructor.
     * @param tagName the tag name.
     * @param values the values.
     */
    public MapElement(String tagName, Map<String, ? extends Object> values)
    {
        super(tagName);
        _values = values;
    }

    /**
     * Constructor.
     * @param tagName the tag name.
     * @param values the values.
     * @param parent the parent {@link Element} if any.
     */
    public MapElement(String tagName, Map<String, ? extends Object> values, Element parent)
    {
        super(tagName, parent);
        _values = values;
    }

    @Override
    protected Map<String, AmetysAttribute> _lookupAttributes()
    {
        return Collections.emptyMap();
    }
    
    @Override
    public Node getFirstChild()
    {
        NodeList list = getChildNodes();
        
        if (list.getLength() == 0)
        {
            return null;
        }
        
        return list.item(0);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected NodeList _getChildNodes()
    {
        List<Element> list = new ArrayList<>();
        
        if (_values != null)
        {
            for (Entry<String, ? extends Object> entry : _values.entrySet())
            {
                Object value = entry.getValue();
                
                if (value instanceof String)
                {
                    list.add(new StringElement(entry.getKey(), (String) value, this));
                }
                else if (value instanceof Map)
                {
                    list.add(new MapElement(entry.getKey(), (Map<String, ? extends Object>) value, this));
                }
                else
                {
                    throw new IllegalArgumentException("MapElement only handles String or Map");
                }
            }
        }
        
        return new AmetysNodeList(list);
    }
}
