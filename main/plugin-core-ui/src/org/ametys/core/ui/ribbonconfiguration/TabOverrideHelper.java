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
package org.ametys.core.ui.ribbonconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper for tab override, to inject elements inside another elements' container
 * @param <T> The type of element to inject
 */
public class TabOverrideHelper<T>
{
    /** The list of elements */
    protected List<T> _elements;

    private  Map<Integer, ObjectOrderMapping> _mapping = new HashMap<>();

    /**
     * Map a list of elements by they order to allows for elements injection 
     * @param elements The list of elements
     */
    public TabOverrideHelper(List<T> elements)
    {
        _elements = elements;
        
        for (T element : elements)
        {
            _mapping.put(elements.indexOf(element) + 1, new ObjectOrderMapping(element)); 
        }
    }
    
    /**
     * Inject an element into the list of elements
     * @param inject The element to inject
     * @param order The order property. 
     */
    public void injectElements(T inject, String order)
    {
        int indexOfDot = order.indexOf(".");
        int primaryOrder =  Integer.valueOf(indexOfDot > 0 ? order.substring(0, indexOfDot) : order);
        Integer secondaryOrder =  indexOfDot > 0 ? Integer.valueOf(order.substring(indexOfDot + 1)) : null;
        if (primaryOrder > 0)
        {
            if (primaryOrder > _mapping.size() || !_mapping.containsKey(primaryOrder))
            {
                // out of bound
                _mapping.get(_mapping.size()).injectObjectAfter(inject, null);
            }
            else
            {
                _mapping.get(primaryOrder).injectObjectBefore(inject, secondaryOrder);
            }
        }
        else
        {
            int relativeOrder = _mapping.size() + primaryOrder;
            if (relativeOrder < 0 || !_mapping.containsKey(relativeOrder))
            {
                // out of bound
                _mapping.get(1).injectObjectBefore(inject, 0);
            }
            else
            {
                _mapping.get(relativeOrder).injectObjectAfter(inject, secondaryOrder);
            }
        }
    }

    private class ObjectOrderMapping
    {   
        private T _initialObject;
        private List<T> _objectsBefore = new ArrayList<>();
        private Map<T, Integer> _objectsBeforeOrder = new HashMap<>();
        private List<T> _objectsAfter = new ArrayList<>();
        private Map<T, Integer> _objectsAfterOrder = new HashMap<>();

        public ObjectOrderMapping(T initialObject)
        {
            _initialObject = initialObject;
        }
        
        public void injectObjectBefore(T object, Integer order)
        {
            if (_objectsBefore.size() == 0)
            {
                _objectsBefore.add(object);
                if (order != null)
                {
                    _objectsBeforeOrder.put(object, order);
                }
                _elements.add(_elements.indexOf(_initialObject), object);
            }
            else
            {
                _inject(object, order, _objectsBefore, _objectsBeforeOrder);
            }
        }
        
        public void injectObjectAfter(T object, Integer order)
        {
            if (_objectsAfter.size() == 0)
            {
                _objectsAfter.add(object);
                if (order != null)
                {
                    _objectsAfterOrder.put(object, order);
                }

                _elements.add(_elements.indexOf(_initialObject) + 1, object);
            }
            else
            {
                _inject(object, order, _objectsAfter, _objectsAfterOrder);
            }
        }
        
        private void _inject(T object, Integer order, List<T> objects, Map<T, Integer> objectsOrder)
        {
            if (order == null)
            {
                T lastObject = objects.get(objects.size() - 1);
                _elements.add(_elements.indexOf(lastObject) + 1, object);
                objects.add(object);
                
                return;
            }
            
            T previous = null;
            for (int i = 0; i < objects.size() && objectsOrder.containsKey(objects.get(i)) &&  objectsOrder.get(objects.get(i)) <= order; i++)
            {
                previous = objects.get(i);
            }
            
            objectsOrder.put(object, order);
            if (previous == null)
            {
                T firstObject = objects.get(0);
                _elements.add(_elements.indexOf(firstObject), object);
                objects.add(0, object);
            }
            else
            {
                _elements.add(_elements.indexOf(previous) + 1, object);
                objects.add(objects.indexOf(previous) + 1, object);
            }
        }
    }
}
