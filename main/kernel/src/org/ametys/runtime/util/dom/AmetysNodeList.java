/*
 *  Copyright 2011 Anyware Services
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

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple implementation of {@link NodeList} backed by a {@link List}.
 */
public class AmetysNodeList implements NodeList
{
    private List<? extends Node> _list;
    
    /**
     * Constructor.
     * @param list the wrapped list.
     */
    public AmetysNodeList(List<? extends Node> list)
    {
        _list = list;
    }
    
    @Override
    public int getLength()
    {
        return _list.size();
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
}
