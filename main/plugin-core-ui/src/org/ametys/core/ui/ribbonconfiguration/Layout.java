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
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.RibbonConfigurationManager.CONTROLSIZE;
import org.ametys.core.ui.RibbonConfigurationManager.LAYOUTALIGN;
import org.ametys.core.ui.RibbonManager;

/**
 * A layout of controls
 */
public class Layout implements Element
{
    /** The layout alignment */
    protected LAYOUTALIGN _layoutAlign;
    
    /** The size of the control */
    protected CONTROLSIZE _size;
    
    /** Number of columns used by the control upon some layouts. 1 is the common and default value */
    protected int _cols;
    
    /** The elements in the layout. Can be controls or toolbars */
    protected List<Element> _elements = new ArrayList<>();

    /** Logger */
    protected Logger _layoutLogger;

    /**
     * Creates a layout of controls
     * @param layoutConfiguration The configuration for the layout
     * @param ribbonManager The ribbon manager 
     * @param logger The logger
     * @throws ConfigurationException if an error occurred
     */
    public Layout(Configuration layoutConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
    {
        this(layoutConfiguration.getAttributeAsInteger("cols", 1), CONTROLSIZE.createsFromString(layoutConfiguration.getAttribute("size", null)), LAYOUTALIGN.createsFromString(layoutConfiguration.getAttribute("align", null)), logger);

        Configuration[] elementsConfigurations = layoutConfiguration.getChildren();
        for (Configuration elementConfiguration : elementsConfigurations)
        {
            if ("control".equals(elementConfiguration.getName()))
            {
                ControlRef control = new ControlRef(elementConfiguration, ribbonManager, _layoutLogger);
                _elements.add(control);
            }
            else if ("toolbar".equals(elementConfiguration.getName()))
            {
                Toolbar toolbar = new Toolbar(elementConfiguration, ribbonManager, _layoutLogger);
                _elements.add(toolbar);
            }
            else if (_layoutLogger.isWarnEnabled())
            {
                _layoutLogger.warn("During configuration of the ribbon, the layout use an unknow tag '" + elementConfiguration.getName() + "'");
            }
        }
    }
    
    /**
     * Create a new layout by duplicating an existing layout
     * @param layout The original layout
     * @param size The new layout size
     */
    public Layout(Layout layout, CONTROLSIZE size)
    {
        this(layout._cols, size, layout._layoutAlign, layout._layoutLogger);
    }
    
    /**
     * Creates a layout of controls
     * @param cols The number of columns
     * @param size The size
     * @param align The alignment
     * @param logger The logger
     */
    public Layout(int cols, CONTROLSIZE size, LAYOUTALIGN align, Logger logger)
    {
        this._layoutLogger = logger;
        
        this._cols = cols;
        if (_layoutLogger.isDebugEnabled())
        {
            _layoutLogger.debug("Control colspan is " + this._cols);
        }
        
        this._layoutAlign = align; 
        if (_layoutLogger.isDebugEnabled())
        {
            _layoutLogger.debug("Control align is " + this._layoutAlign);
        }
        
        _size = size;
        if (_layoutLogger.isDebugEnabled())
        {
            _layoutLogger.debug("Control size is " + this._size);
        }
    }
    
    /**
     * Get the size of the layout
     * @return The size
     */
    public CONTROLSIZE getSize()
    {
        return _size;
    }
    
    @Override
    public List<Element> getChildren()
    {
        return _elements;
    }
    
    @Override
    public void toSAX(ContentHandler handler) throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        
        if (_layoutAlign != null)
        {
            attrs.addCDATAAttribute("align", _layoutAlign.toString());
        }

        if (_size != null)
        {
            attrs.addCDATAAttribute("size", _size.toString());
        }

        attrs.addCDATAAttribute("cols", Integer.toString(_cols));
        
        XMLUtils.startElement(handler, "layout", attrs);
        
        for (Element element : _elements)
        {
            element.toSAX(handler);
        }
        
        XMLUtils.endElement(handler, "layout");
    }

    public boolean isSame(Element element)
    {
        if (!(element instanceof Layout))
        {
            return false;
        }
        
        Layout layout = (Layout) element;
        if (layout._cols != _cols || layout._layoutAlign != _layoutAlign || layout._size != _size)
        {
            return false;
        }
        
        List<Element> layoutElements = layout.getChildren();
        if (layoutElements.size() != _elements.size())
        {
            return false;
        }
        for (int i = 0; i < _elements.size(); i++)
        {
            Element child = _elements.get(i);
            Element objElement = layoutElements.get(i);
            if (child == null || objElement == null || !child.isSame(objElement))
            {
                return false;
            }
        }
        
        return true;
    }
}

