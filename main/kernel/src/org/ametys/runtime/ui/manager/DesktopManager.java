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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.ui.item.Interaction;
import org.ametys.runtime.ui.item.UIItem;
import org.ametys.runtime.ui.item.UIItemFactory;
import org.ametys.runtime.ui.item.UIItemManager;


/**
 * This manager handles the home page of spaces
 */
public class DesktopManager extends AbstractLogEnabled implements Serviceable, Configurable, ThreadSafe, Disposable, Component
{
    /** The avalon service manager */
    protected ServiceManager _manager;
    
    /** The ui item extension point */
    protected UIItemManager _uiItemManager;

    /** The items configured */
    protected Map<String, List<String>> _categorizedItemsIds;
    
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
        
        Configuration actionsConfiguration = configuration.getChild("actions", false);
        if (actionsConfiguration != null)
        {
            _configureActions(actionsConfiguration);
        }
        else
        {
            String errorMessage = "The desktop manager cannot be configured correctly : its configuration has no 'actions' element.";
            getLogger().error(errorMessage);
            throw new ConfigurationException(errorMessage, configuration);
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
        for (List<String> itemsIds : _categorizedItemsIds.values())
        {
            for (String itemId : itemsIds)
            {
                // Vérifie qu'il s'agit bien d'une factory valide
                UIItemFactory factory = _uiItemManager.getExtension(itemId);
                if (factory == null)
                {
                    String errorMessage = "An item referes an unexisting item factory with id '" + itemId + "'";
                    getLogger().error(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
            }
        }

        _initialized = true;
    }
    
    private void _configureActions(Configuration configuration) throws ConfigurationException
    {
        _categorizedItemsIds = new LinkedHashMap<String, List<String>>();

        Configuration[] catConf = configuration.getChildren("category");
        for (int i = 0; i < catConf.length; i++)
        {
            List<String> categorizedItemsIds = new ArrayList<String>();

            String categoryName = catConf[i].getAttribute("name", "");
            if (categoryName.length() == 0)
            {
                String errorMessage = "The category n°" + i + " does not have its mandatory 'name' attribute valued.";
                getLogger().error(errorMessage);
                throw new ConfigurationException(errorMessage, configuration);
            }

            if (_categorizedItemsIds.get(categoryName) != null)
            {
                String errorMessage = "Two or more categories has the same name.";
                getLogger().error(errorMessage);
                throw new ConfigurationException(errorMessage, configuration);
            }

            Configuration[] itemsConf = catConf[i].getChildren("item");
            for (int j = 0; j < itemsConf.length; j++)
            {
                // Récupère l'identifiant de la factory
                String id = itemsConf[j].getAttribute("id", "");
                if (id.length() == 0)
                {
                    String errorMessage = "The item n°" + j + " of the category n°" + i
                            + " does not have its mandatory 'id' attribute valued.";
                    getLogger().error(errorMessage);
                    throw new ConfigurationException(errorMessage, configuration);
                }

                // L'ajoute à la liste
                categorizedItemsIds.add(id);
            }

            _categorizedItemsIds.put(categoryName, categorizedItemsIds);
        }
    }

    /**
     * SAX the desktop
     * @param handler The sax content handler
     * @throws SAXException If an error occured while saxing
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        _lazyInitialize();
        
        // Transforme la liste d'identifiant en liste d'items
        for (String categoryName : _categorizedItemsIds.keySet())
        {
            List<UIItem> items = new ArrayList<UIItem>();

            List<String> itemsIds = _categorizedItemsIds.get(categoryName);
            for (String itemId : itemsIds)
            {
                UIItemFactory factory = _uiItemManager.getExtension(itemId);
                List<UIItem> factoryItems = factory.getUIItems();
                items.addAll(factoryItems);
            }
         
            toSAX(handler, categoryName, items);
        }
    }
    
    /**
     * SAX a category
     * @param handler The sax content handler
     * @param categoryName The i18n key for category
     * @param items The items in the category
     * @throws SAXException If an error occured
     * @throws IllegalArgumentException If one of the items is not an Interaction
     */
    protected void toSAX(ContentHandler handler, String categoryName, List<UIItem> items) throws SAXException
    {
        if (categoryName == null || categoryName.length() == 0 || items == null || items.size() == 0)
        {
            return;
        }
        
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", "CDATA", categoryName);
        XMLUtils.startElement(handler, "category", atts);
        
        for (UIItem item : items)
        {
            if (!(item instanceof Interaction))
            {
                String message = "DesktopManager allows only Interaction for UIItem. Error occured for category " + categoryName;
                getLogger().error(message);
                throw new IllegalArgumentException(message);
            }
            
            Interaction interaction = (Interaction) item;
            SaxUIHelper.toSAX(handler, interaction);
        }
        
        XMLUtils.endElement(handler, "category");
    }
}
