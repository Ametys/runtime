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
import org.ametys.runtime.ui.item.UIItemGroup;
import org.ametys.runtime.ui.item.UIItemManager;
import org.ametys.runtime.util.I18nizableText;


/**
 * The menu manager is in charge to load menu configuration and
 * also to sax this configuration in collaboration with the ui extension point
 */
public class MenusManager extends AbstractLogEnabled implements Configurable, ThreadSafe, Serviceable, Disposable, Component
{
    /** The avalon service manager */
    protected ServiceManager _manager;
    
    /** The ui item extension point */
    protected UIItemManager _uiItemManager;
    
    /** The list of menus */
    protected List<Menu> _menus;
    
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
        
        _menus = new ArrayList<Menu>();

        Configuration[] menusConfigurations = configuration.getChildren("menu");
        for (Configuration menuConfiguration : menusConfigurations)
        {
            Menu menu = _configureMenu(menuConfiguration);
            _menus.add(menu);
        }
    }
    
    /**
     * Check that the configuration was correct
     * @throws IllegalStateException if an item does not exists
     */
    private void _lazyInitialize() 
    {
        if (_initialized)
        {
            return;
        }
     
        // check that all refered items does exist
        for (Menu menu : _menus)
        {
            _checkMenu(menu);
        }
        
        _initialized = true;
    }
    
    private void _checkMenu(Menu menu) throws IllegalStateException
    {
        for (MenuElement element : menu._elements)
        {
            if (element instanceof Menu)
            {
                _checkMenu((Menu) element);
            }
            else if (element instanceof Separator)
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
                String errorMessage = "The menu list does not handle items of class '" + element.getClass().getName() + "'";
                getLogger().error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
    }
    
    /**
     * SAX the menu
     * 
     * @param handler The sax content handler
     * @throws SAXException If an error occured while saxing
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        _lazyInitialize();
        
        for (Menu menu : _menus)
        {
            _saxMenu(handler, menu);
        }        
    }

    private Menu _configureMenu(Configuration configuration) throws ConfigurationException
    {
        String label = configuration.getAttribute("label", "");
        if (label.length() == 0)
        {
            String errorMessage = "A 'menu' element has no or empty 'label' attribute.";
            getLogger().error(errorMessage);
            throw new ConfigurationException(errorMessage, configuration);
        }
        
        List<MenuElement> menuElements = new ArrayList<MenuElement>();
        Configuration[] children = configuration.getChildren();
        for (Configuration child : children)
        {
            if ("item".equals(child.getName()))
            {
                Item item = _configureItem(child);
                menuElements.add(item);
            }
            else if ("separator".equals(child.getName()))
            {
                Separator separator = new Separator();
                menuElements.add(separator);
            }
            else if ("menu".equals(child.getName()))
            {
                Menu menu = _configureMenu(child);
                menuElements.add(menu);
            }
            else
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("A menu configuration use an unknown element '" + child.getName() + "'");
                }
            }
        }
        
        Menu menu = new Menu(label, menuElements);
        return menu;
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
        if (uiItem instanceof UIItemGroup)
        {
            UIItemGroup group = (UIItemGroup) uiItem;
            
            XMLUtils.startElement(handler, "menu");
            
            group.getLabel().toSAX(handler, "label");

            XMLUtils.startElement(handler, "items");

            List<UIItem> uiitems = group.getItems();
            for (UIItem uiitem : uiitems)
            {
                _saxUIItem(handler, uiitem);
            }
            
            XMLUtils.endElement(handler, "items");
            
            XMLUtils.endElement(handler, "menu");
        }
        else if (uiItem instanceof UIItem.BarSeparator || uiItem instanceof UIItem.SpaceSeparator)
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
                getLogger().warn("The menu list does not handle items of class '" + uiItem.getClass().getName() + "'");
            }
        }
    }
    
    private void _saxMenu(ContentHandler handler, Menu menu) throws SAXException
    {
        XMLUtils.startElement(handler, "menu");
        
        menu._label.toSAX(handler, "label");
        
        XMLUtils.startElement(handler, "items");
        for (MenuElement element : menu._elements)
        {
            if (element instanceof Menu)
            {
                _saxMenu(handler, (Menu) element);
            }
            else if (element instanceof Separator)
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
                String errorMessage = "The menu list does not handle items of class '" + element.getClass().getName() + "'";
                getLogger().error(errorMessage);
                throw new SAXException(errorMessage);
            }
        }
        XMLUtils.endElement(handler, "items");
        
        XMLUtils.endElement(handler, "menu");
    }
    
    interface MenuElement
    {
        // empty
    }
    
    class Separator implements MenuElement
    {
        // extends
    }
    
    /**
     * Factory id 
     */
    class Item implements MenuElement
    {
        String _id;
        
        Item(String id)
        {
            _id = id;
        }
    }
    
    /**
     * Hardcoded menu 
     */
    class Menu implements MenuElement
    {
        I18nizableText _label;
        List<MenuElement> _elements;
        
        Menu(String label, List<MenuElement> items)
        {
            _label = new I18nizableText("application", label);
            _elements = items;
        }
    }
}
