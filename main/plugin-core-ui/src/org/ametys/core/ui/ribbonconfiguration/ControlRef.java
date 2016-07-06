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
import java.util.UUID;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.RibbonControlsManager;
import org.ametys.core.ui.RibbonManager;

/**
 * A control
 */
public class ControlRef implements Element
{
    /** The id of the control */
    protected String _id;

    /** Number of columns used by the control upon some layouts. 1 is the common and defaul value */
    protected int _colspan;
    
    /** Logger */
    protected Logger _controlLogger;
    
    /**
     * Creates a control reference
     * @param controlConfiguration The configuration for the control
     * @param ribbonManager The ribbon manager
     * @param logger The logger
     * @throws ConfigurationException if an error occurred
     */
    public ControlRef(Configuration controlConfiguration, RibbonManager ribbonManager, Logger logger) throws ConfigurationException
    {
        String refId = controlConfiguration.getAttribute("ref-id", null);
        if (refId == null && controlConfiguration.getChildren().length == 0)
        {
            _initialize(controlConfiguration.getAttribute("id"), controlConfiguration.getAttributeAsInteger("colspan", 1), logger);
        }
        else
        {
            String id = controlConfiguration.getAttribute("id", UUID.randomUUID().toString());
            DefaultConfiguration defaultConfig = new DefaultConfiguration(controlConfiguration);
            
            String classname = controlConfiguration.getAttribute("class", null);
            if (classname == null)
            {
                if (refId != null)
                {
                    defaultConfig.setAttribute("point", controlConfiguration.getAttribute("point", RibbonControlsManager.ROLE));
                }
                else
                {
                    classname = org.ametys.core.ui.SimpleMenu.class.getName();
                    defaultConfig.setAttribute("class", classname);
                }
            }
            
            ribbonManager.addExtension(id, "core-ui", null, defaultConfig);

            _initialize(id, controlConfiguration.getAttributeAsInteger("colspan", 1), logger);
        }
    }

    /**
     * Creates a control reference
     * @param id The id referenced by this control
     * @param colspan The colspan of this control
     * @param logger The logger
     */
    public ControlRef(String id, int colspan, Logger logger)
    {
        _initialize(id, colspan, logger);
    }

    private void _initialize(String id, int colspan, Logger logger)
    {
        this._controlLogger = logger;
        
        this._id = id;
        if (_controlLogger.isDebugEnabled())
        {
            _controlLogger.debug("Control id is " + this._id);
        }
        
        this._colspan = colspan;
        if (_controlLogger.isDebugEnabled())
        {
            _controlLogger.debug("Control colspan is " + this._colspan);
        }
    }
    
    /**
     * Get the id of the ControlRef
     * @return The id
     */
    public String getId()
    {
        return _id;
    }

    @Override
    public int getColumns()
    {
        return _colspan;
    }
    
    @Override
    public void setColumns(int size)
    {
        _colspan = size;
    }
    
    @Override
    public List<Element> getChildren()
    {
        return new ArrayList<>();
    }
    
    @Override
    public void toSAX(ContentHandler handler) throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("id", _id);
        attrs.addCDATAAttribute("colspan", Integer.toString(_colspan));
        XMLUtils.createElement(handler, "control", attrs);
    }

    public boolean isSame(Element element)
    {
        if (!(element instanceof ControlRef))
        {
            return false;
        }
        
        ControlRef controlRef = (ControlRef) element;
        return controlRef._id == _id && controlRef._colspan == _colspan;
    }
}
