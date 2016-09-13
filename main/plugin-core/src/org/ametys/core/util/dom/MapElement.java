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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM Layer over a Map. Only String, List&lt;Object&gt; and Map&lt;String, Object&gt; values are allowed (recursively).
 */
public class MapElement extends AbstractAmetysElement
{
    private Map<String, ? extends Object> _values;
    private Map<String, String> _attributes;
    
    /**
     * Constructor.
     * @param tagName the tag name.
     * @param values the values.
     */
    public MapElement(String tagName, Map<String, ? extends Object> values)
    {
        this(tagName, Collections.emptyMap(), values);
    }
    
    /**
     * Constructor.
     * @param tagName the tag name.
     * @param attributes The attributes names and values.
     * @param values the values.
     */
    public MapElement(String tagName, Map<String, String> attributes, Map<String, ? extends Object> values)
    {
        super(tagName);
        _values = values;
        _attributes = attributes;
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
    
    /**
     * Constructor.
     * @param tagName the tag name.
     * @param attributes The attributes names and values.
     * @param values the values.
     * @param parent the parent {@link Element} if any.
     */
    public MapElement(String tagName, Map<String, String> attributes, Map<String, ? extends Object> values, Element parent)
    {
        super(tagName, parent);
        _values = values;
        _attributes = attributes;
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
                Object rawValue = entry.getValue();
                List<Object> values = new ArrayList<>();
                if (rawValue instanceof List)
                {
                    values.addAll((List) rawValue);
                }
                else
                {
                    values.add(rawValue);
                }
                
                for (Object value : values)
                {
                    Object realValue = value;
                    Map<String, String> attributes = null;
                    if (value instanceof MapNode)
                    {
                        realValue = ((MapNode) value).getValue();
                        attributes = ((MapNode) value).getAttributes();
                    }
                    
                    if (realValue == null)
                    {
                        list.add(new StringElement(entry.getKey(), attributes, "", this));
                    }
                    else if (realValue instanceof String)
                    {
                        list.add(new StringElement(entry.getKey(), attributes, (String) realValue, this));
                    }
                    else if (realValue instanceof Map)
                    {
                        list.add(new MapElement(entry.getKey(), attributes, (Map<String, ? extends Object>) realValue, this));
                    }
                    else
                    {
                        throw new IllegalArgumentException("MapElement only handles String, List<Object> or Map<String, Object> recursively");
                    }
                }
            }
        }
        
        return new AmetysNodeList(list);
    }
    
    /**
     * This class represents a element 
     *
     */
    public static class MapNode
    {
        private Object _value;
        private Map<String, String> _attrs;

        /**
         * Constructor
         * @param value The value
         * @param attributes The attributes
         */
        public MapNode (Object value, Map<String, String> attributes)
        {
            _value = value;
            _attrs = attributes;
        }
        
        /**
         * Get the attributes
         * @return the attributes
         */
        public Map<String, String> getAttributes()
        {
            return _attrs;
        }
        
        /**
         * Get the value
         * @return the value
         */
        public Object getValue ()
        {
            return _value;
        }
    }
}
