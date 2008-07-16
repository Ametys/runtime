/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.ui.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.ui.item.Interaction;
import org.ametys.runtime.ui.item.UIItem;
import org.ametys.runtime.ui.item.UIItemFactory;
import org.ametys.runtime.ui.item.UIItemManager;


/**
 * The toolbars manager is in charge to load toolbar configuration and
 * also to sax this configuration in collaboration with the ui extension point
 */
public class ToolbarsManager extends AbstractLogEnabled implements Configurable, ThreadSafe, Serviceable, Disposable, Component
{
    /** The avalon service manager */
    protected ServiceManager _manager;
    
    /** The ui item extension point */
    protected UIItemManager _uiItemManager;
    
    /** The list of uiitems */
    protected List<ToolbarsElement> _toolbars;
    
    private boolean _initialized;

    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _uiItemManager = (UIItemManager) manager.lookup(UIItemManager.ROLE);
    }
    
    public void dispose()
    {
        _manager.release(_uiItemManager);
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _initialized = false;
        
        _toolbars = new ArrayList<ToolbarsElement>();

        Configuration[] elementsConfigurations = configuration.getChildren();
        for (Configuration elementConfiguration : elementsConfigurations)
        {
            ToolbarsElement element = _configureElement(elementConfiguration);
            if (element != null)
            {
                _toolbars.add(element);
            }
        }
    }
    
    /**
     * Check that the configuration was correct
     * @throws IllegalStateException if an item does not exists
     */
    private void _lasyInitialize() 
    {
        if (_initialized)
        {
            return;
        }
     
        // check that all refered items does exist
        for (ToolbarsElement element : _toolbars)
        {
            _checkElement(element);
        }
        
        _initialized = true;
    }
    
    private void _checkElement(ToolbarsElement element) throws IllegalStateException
    {
        if (element instanceof Separator)
        {
            // ok
        }
        else if (element instanceof Item)
        {
            Item item = (Item) element;
            
            // Vérifie qu'il s'agit bien d'une factory valide
            UIItemFactory factory = _uiItemManager.getExtension(item._id);
            if (factory == null)
            {
                String errorMessage = "An item referes an unexisting item factory with id '" + item._id + "'";
                getLogger().error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
        else
        {
            String errorMessage = "The toolbars list does not handle items of class '" + element.getClass().getName() + "'";
            getLogger().error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }
    
    /**
     * SAX the toolbar
     * 
     * @param handler The sax content handler
     * @throws SAXException If an error occured while saxing
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        _lasyInitialize();
        
        for (ToolbarsElement element : _toolbars)
        {
            _saxElement(handler, element);
        }        
    }

    private ToolbarsElement _configureElement(Configuration configuration) throws ConfigurationException
    {
        if ("item".equals(configuration.getName()))
        {
            Item item = _configureItem(configuration);
            return item;
        }
        else if ("separator".equals(configuration.getName()))
        {
            Separator separator = new Separator();
            return separator;
        }
        else
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The toolbar configuration use an unknown element '" + configuration.getName() + "'");
            }
            return null;
        }
    }
    
    private Item _configureItem(Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id", "");
        if (id.length() == 0)
        {
            String errorMessage = "An 'item' element has no or empty 'id' attribute.";
            getLogger().error(errorMessage);
            throw new ConfigurationException(errorMessage, configuration);
        }
        
        Item item = new Item(id);
        return item;
    }

    private void _saxSeparator(ContentHandler handler) throws SAXException
    {
        XMLUtils.createElement(handler, "separator");
    }

    private void _saxUIItem(ContentHandler handler, UIItem uiItem) throws SAXException
    {
        if (uiItem instanceof UIItem.BarSeparator || uiItem instanceof UIItem.SpaceSeparator)
        {
            _saxSeparator(handler);
        }
        else if (uiItem instanceof Interaction)
        {
            Interaction interaction = (Interaction) uiItem;
            SaxUIHelper.toSAX(handler, interaction);
        }
        else
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The toolbar list does not handle items of class '" + uiItem.getClass().getName() + "'");
            }
        }
    }
    
    private void _saxElement(ContentHandler handler, ToolbarsElement element) throws SAXException
    {
        if (element instanceof Separator)
        {
            _saxSeparator(handler);
        }
        else if (element instanceof Item)
        {
            Item item = (Item) element;
            UIItemFactory factory = _uiItemManager.getExtension(item._id);
            
            List<UIItem> uiitems = factory.getUIItems();
            for (UIItem uiitem : uiitems)
            {
                _saxUIItem(handler, uiitem);
            }
        }
        else
        {
            String errorMessage = "The toolbar list does not handle items of class '" + element.getClass().getName() + "'";
            getLogger().error(errorMessage);
            throw new SAXException(errorMessage);
        }
    }
    
    interface ToolbarsElement
    {
        // empty
    }
    
    class Separator implements ToolbarsElement
    {
        // extends
    }
    
    /**
     * Factory id 
     */
    class Item implements ToolbarsElement
    {
        String _id;
        
        Item(String id)
        {
            _id = id;
        }
    }
}
