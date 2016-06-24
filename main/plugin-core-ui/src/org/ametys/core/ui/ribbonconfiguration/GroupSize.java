/**
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.XMLUtils;
import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.RibbonManager;

/**
 * A group in a defined size
 */
public class GroupSize
{
    /** The list of elements in the group (controls or layouts) */
    protected List<Element> _elements = new ArrayList<>();
    
    /** The logger */
    protected Logger _groupSizeLogger;
    
    /**
     * Creates a group in a defined size
     * @param groupSizeConfiguration The configuration for the size
     * @param ribbonManager The ribbon manager
     * @param logger The logger
     * @throws ConfigurationException if an error occurred
     */
    public GroupSize(Configuration groupSizeConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
    {
        this._groupSizeLogger = logger;
        
        Configuration[] elementsConfigurations = groupSizeConfiguration.getChildren();
        for (Configuration elementConfiguration : elementsConfigurations)
        {
            if ("control".equals(elementConfiguration.getName()))
            {
                ControlRef control = new ControlRef(elementConfiguration, ribbonManager, _groupSizeLogger);
                _elements.add(control);
            }
            else if ("layout".equals(elementConfiguration.getName()))
            {
                Layout layout = new Layout(elementConfiguration, ribbonManager, _groupSizeLogger);
                _elements.add(layout);
            }
            else if (_groupSizeLogger.isWarnEnabled())
            {
                _groupSizeLogger.warn("During configuration of the ribbon, the group use an unknow tag '" + elementConfiguration.getName() + "'");
            }
        }
    }
    
    /**
     * Creates an empty group in a defined size
     * @param logger The logger
     */
    public GroupSize(Logger logger)
    {
        this._groupSizeLogger = logger;
    }
    
    /**
     * Get a set of all referenced ids
     * @return A non null set of control ids
     */
    public Set<String> getControlIds()
    {
        return _getControlIds(_elements);
    }
    
    private Set<String> _getControlIds(List<Element> elements)
    {
        Set<String> ids = new HashSet<>();
        
        for (Element element : elements)
        {
            if (element instanceof ControlRef)
            {
                ControlRef controlRef = (ControlRef) element;
                ids.add(controlRef._id);
            }
            else if (element instanceof Layout)
            {
                Layout layout = (Layout) element;
        
                ids.addAll(_getControlIds(layout._elements));
            }
            else
            {
                Toolbar toolbar = (Toolbar) element;
                
                ids.addAll(_getControlIds(toolbar._elements));
            }
        }
        
        return ids;
    }
    
    /**
     * Retrieve the list of children elements in this element.
     * @return The list of elements.
     */
    public List<Element> getChildren()
    {
        return _elements;
    }
    
    /**
     * Sax the the configuration of the group size.
     * @param elementName The name of the surrounding element to use
     * @param handler The content handler where to sax
     * @throws SAXException if an error occurs
     */
    public void toSAX(String elementName, ContentHandler handler) throws SAXException
    {
        XMLUtils.startElement(handler, elementName);

        for (Element element : _elements)
        {
            element.toSAX(handler);
        }

        XMLUtils.endElement(handler, elementName);
    }
    
    /**
     * Test if this GroupSize contains the same elements as another GroupSize
     * @param obj The other GroupSize
     * @return True if they are equals
     */
    public boolean isSame(GroupSize obj)
    {
        List<Element> objElements = obj.getChildren();
        if (objElements.size() != _elements.size())
        {
            return false;
        }
        for (int i = 0; i < _elements.size(); i++)
        {
            Element element = _elements.get(i);
            Element objElement = objElements.get(i);
            if (element == null || objElement == null || !element.isSame(objElement))
            {
                return false;
            }
        }
        return true;
    }
}   
