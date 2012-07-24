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

package org.ametys.runtime.util.dom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Implementation of {@link NamedNodeMap} to store attributes names and values.
 */
public class AmetysNamedNodeMap implements NamedNodeMap
{
    private Map<String, AmetysAttribute> _map = new LinkedHashMap<String, AmetysAttribute>();
    private ArrayList<AmetysAttribute> _list = new ArrayList<AmetysAttribute>();
    
    /**
     * Contructor.
     * @param map a &lt;name, attribute&gt; map.
     */
    public AmetysNamedNodeMap(Map<String, AmetysAttribute> map)
    {
        _map = map;
        
        for (String name : map.keySet())
        {
            _list.add(map.get(name));
        }
    }
    
    @Override
    public Node getNamedItem(String name)
    {
        return _map.get(name);
    }

    @Override
    public Node item(int index)
    {
        if (index < 0 || index > getLength())
        {
            return null;
        }
        
        return _list.get(index);
    }

    @Override
    public int getLength()
    {
        return _map.keySet().size();
    }

    @Override
    public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException
    {
        return getNamedItem(localName);
    }

    @Override
    public Node setNamedItemNS(Node arg) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setNamedItemNS");
    }

    @Override
    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "removeNamedItemNS");
    }
    
    @Override
    public Node setNamedItem(Node arg) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setNamedItem");
    }

    @Override
    public Node removeNamedItem(String name) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "removeNamedItem");
    }
}
