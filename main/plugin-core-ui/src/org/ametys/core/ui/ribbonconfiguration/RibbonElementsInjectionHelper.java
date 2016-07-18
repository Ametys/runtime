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

import org.slf4j.Logger;

/**
 * Helper for the ribbon, for injecting elements inside another elements' container
 * @param <T> The type of element to inject
 */
public class RibbonElementsInjectionHelper<T>
{
    /** The list of elements */
    protected List<T> _elements;

    /** Logger */
    protected Logger _logger;

    private  Map<Integer, ObjectOrderMapping> _mapping = new HashMap<>();

    /**
     * Map a list of elements by they order to allows for elements injection 
     * @param elements The list of elements
     * @param logger The logger
     */
    public RibbonElementsInjectionHelper(List<T> elements, Logger logger)
    {
        _logger = logger;
        _elements = elements;
        
        if (elements.size() > 0)
        {
            for (T element : elements)
            {
                _mapping.put(elements.indexOf(element) + 1, new ObjectOrderMapping(element)); 
            }
        }
        else
        {
            _mapping.put(1, new ObjectOrderMapping(null)); 
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
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("TabOverrideHelper : injecting element '" + inject.toString() + "' with order '" + primaryOrder + "', but that order is out of bound. The element is injected at the end, after element '" + _mapping.get(_mapping.size()).getInitialObject() + "'");
                }

                _mapping.get(_mapping.size()).injectObjectAfter(inject, null);
            }
            else
            {
                _mapping.get(primaryOrder).injectObjectBefore(inject, secondaryOrder);
                
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("TabOverrideHelper : injecting element '" + inject.toString() + "' with order '" + primaryOrder + "' before element '" + _mapping.get(primaryOrder).getInitialObject() + "'");
                }
            }
        }
        else
        {
            int relativeOrder = _mapping.size() + primaryOrder;
            if (relativeOrder < 0 || !_mapping.containsKey(relativeOrder))
            {
                // out of bound
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("TabOverrideHelper : injecting element '" + inject.toString() + "' with order '" + primaryOrder + "', but that order is out of bound. The element is injected at the start, before element '" + _mapping.get(1).getInitialObject() + "'");
                }

                _mapping.get(1).injectObjectBefore(inject, 0);
            }
            else
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("TabOverrideHelper : injecting element '" + inject.toString() + "' with order '" + primaryOrder + "' after element '" + _mapping.get(relativeOrder).getInitialObject() + "'");
                }
                
                _mapping.get(relativeOrder).injectObjectAfter(inject, secondaryOrder);
            }
        }
    }

    private class ObjectOrderMapping
    {   
        private T _initialObject;
        private int _initialObjectIndex;
        private List<T> _objectsBefore = new ArrayList<>();
        private Map<T, Integer> _objectsBeforeOrder = new HashMap<>();
        private List<T> _objectsAfter = new ArrayList<>();
        private Map<T, Integer> _objectsAfterOrder = new HashMap<>();

        public ObjectOrderMapping(T initialObject)
        {
            _initialObject = initialObject;
            _initialObjectIndex = _initialObject != null ? _elements.indexOf(_initialObject) : 0;
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
                
                _elements.add(_initialObject != null ? _elements.indexOf(_initialObject) : 0, object);
            }
            else
            {
                _inject(object, order, _objectsBefore, _objectsBeforeOrder);
            }
            _initialObjectIndex++;
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

                _elements.add(_initialObject != null ? _elements.indexOf(_initialObject) + 1 : _elements.size(), object);
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
        
        public T getInitialObject()
        {
            return _initialObject;
        }
    }
}
