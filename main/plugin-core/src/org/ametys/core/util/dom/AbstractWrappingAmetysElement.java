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

package org.ametys.core.util.dom;

import org.w3c.dom.Element;

/**
 * DOM layer on top if an object hierarchy.
 * @param <T> the actual type of the wrapped object.
 */
public abstract class AbstractWrappingAmetysElement<T> extends AbstractAmetysElement
{
    /** The wrapper object. */
    protected T _object;
    
    /**
     * Constructor.
     * @param object the underlying object.
     */
    public AbstractWrappingAmetysElement(T object)
    {
        this(object, null);
    }

    /**
     * Constructor.
     * @param tagName the tag name.
     * @param object the underlying object.
     */
    public AbstractWrappingAmetysElement(String tagName, T object)
    {
        this(tagName, object, null);
    }

    /**
     * Constructor.
     * @param object the underlying object.
     * @param parent the parent {@link Element}.
     */
    public AbstractWrappingAmetysElement(T object, Element parent)
    {
        super(parent);
        _object = object;
    }

    /**
     * Constructor.
     * @param tagName the tag name.
     * @param object the underlying object.
     * @param parent the parent {@link Element}.
     */
    public AbstractWrappingAmetysElement(String tagName, T object, Element parent)
    {
        super(tagName, parent);
        _object = object;
    }
    
    /**
     * Returns the wrapped object.
     * @return the wrapped object.
     */
    public T getWrappedObject()
    {
        return _object;
    }
}
