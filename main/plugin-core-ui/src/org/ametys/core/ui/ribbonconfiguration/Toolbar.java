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

import org.ametys.core.ui.RibbonManager;

/**
 * A toolbar of controls
 */
public class Toolbar implements Element
{
    /** Number of columns used by the control upon some layouts. 1 is the common and default value */
    protected int _colspan;
    
    /** The elements in the layout. Can be controls */
    protected List<Element> _elements = new ArrayList<>();

    /** Logger */
    protected Logger _toolbarLogger;
    
    /**
     * Creates a toolbar of controls
     * @param toolbarConfiguration The configuration for the layout
     * @param ribbonManager The ribbon manager
     * @param logger The logger
     * @throws ConfigurationException if an error occurred
     */
    public Toolbar(Configuration toolbarConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
    {
        this(logger, toolbarConfiguration.getAttributeAsInteger("colspan", 1));
        
        Configuration[] elementsConfigurations = toolbarConfiguration.getChildren();
        for (Configuration elementConfiguration : elementsConfigurations)
        {
            if ("control".equals(elementConfiguration.getName()))
            {
                ControlRef control = new ControlRef(elementConfiguration, ribbonManager, _toolbarLogger);
                _elements.add(control);
            }
            else if (_toolbarLogger.isWarnEnabled())
            {
                _toolbarLogger.warn("During configuration of the ribbon, the toolbar use an unknow tag '" + elementConfiguration.getName() + "'");
            }
        }
    }

    /**
     * Creates an empty toolbar of controls
     * @param logger The logger
     * @param colspan The toolbar colspan
     */
    public Toolbar(Logger logger, int colspan)
    {
        this._toolbarLogger = logger;
        
        this._colspan = colspan;
        if (_toolbarLogger.isDebugEnabled())
        {
            _toolbarLogger.debug("Control colspan is " + this._colspan);
        }
    }
    
    /**
     * Get the colspan of the toolbar
     * @return The colspan
     */
    public int getColspan()
    {
        return _colspan;
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
        attrs.addCDATAAttribute("colspan", Integer.toString(_colspan));
        
        XMLUtils.startElement(handler, "toolbar", attrs);
        
        for (Element element : _elements)
        {
            element.toSAX(handler);
        }
        
        XMLUtils.endElement(handler, "toolbar");
    }

    public boolean isSame(Element element)
    {
        if (!(element instanceof Toolbar))
        {
            return false;
        }
        
        Toolbar toolbar = (Toolbar) element;
        if (toolbar._colspan != _colspan)
        {
            return false;
        }
        
        if (toolbar.getChildren().size() != _elements.size())
        {
            return false;
        }
        for (int i = 0; i < _elements.size(); i++)
        {
            Element child = _elements.get(i);
            Element objElement = toolbar.getChildren().get(i);
            if (child == null || objElement == null || !child.isSame(objElement))
            {
                return false;
            }
        }
        
        return true;
    }
}
