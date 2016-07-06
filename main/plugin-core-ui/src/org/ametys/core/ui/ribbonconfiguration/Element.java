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

import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * An element in the ribbon
 */
public interface Element
{
    /**
     * Retrieve the list of children elements in this element.
     * @return The list of elements.
     */
    public List<Element> getChildren();
    
    /**
     * Sax the the configuration of the element.
     * @param handler The content handler where to sax
     * @throws SAXException if an error occurs
     */
    public void toSAX(ContentHandler handler) throws SAXException;
    
    
    /**
     * Test if an element is equal to another element
     * @param element The element to compare to
     * @return True if equals
     */
    boolean isSame(Element element);
    
    /**
     * Get the size taken by this element, in columns
     * @return The size
     */
    public int getColumns();
    
    /**
     * Set the size to take by this element
     * @param size The size in number of columns
     */
    public void setColumns(int size);
}

