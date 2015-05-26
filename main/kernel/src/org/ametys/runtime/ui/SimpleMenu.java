/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.ribbon.RibbonControlsManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.LoggerFactory;

/**
 * This element creates a control button with a menu
 */
public class SimpleMenu extends StaticClientSideElement implements MenuClientSideElement
{
    /** The client side element component manager for menu items. */
    protected ThreadSafeComponentManager<ClientSideElement> _menuItemManager;
    /** The client side element component manager for gallery items. */
    protected ThreadSafeComponentManager<ClientSideElement> _galleryItemManager;
    /** The ribbon control manager */
    protected RibbonControlsManager _ribbonControlManager;
    /** The service manager */
    protected ServiceManager _smanager;
    
    
    /** The menu items */
    protected List<ClientSideElement> _menuItems;
    /** The gallery items */
    protected List<GalleryItem> _galleryItems;
    /** The primary menu item */
    protected ClientSideElement _primaryMenuItem;
    /** The unresolved menu items */
    protected List<UnresolvedItem> _unresolvedMenuItems;
    
    List<ClientSideElement> _referencedClientSideElement;
    
    
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _smanager = smanager;
        _ribbonControlManager = (RibbonControlsManager) smanager.lookup(RibbonControlsManager.ROLE);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        try
        {
            _menuItemManager = new ThreadSafeComponentManager<>();
            _menuItemManager.enableLogging(LoggerFactory.getLoggerFor("cms.plugin.threadsafecomponent"));
            _menuItemManager.service(_smanager);
            
            _galleryItemManager = new ThreadSafeComponentManager<>();
            _galleryItemManager.enableLogging(LoggerFactory.getLoggerFor("cms.plugin.threadsafecomponent"));
            _galleryItemManager.service(_smanager);
        }
        catch (ServiceException e)
        {
            throw new ConfigurationException("Unable to initialize local client side element manager", e);
        }
        
        super.configure(configuration);
        
        _referencedClientSideElement = new ArrayList<>();
        _galleryItems = new ArrayList<>();
        
        _menuItems = new ArrayList<>();
        _unresolvedMenuItems = new ArrayList<>();
        
        _configureGalleries (configuration);
        _configureItemsMenu(configuration);
    }
    
    @Override
    public Map<String, Object> getParameters(Map<String, Object> contextualParameters)
    {
        try
        {
            _resolveMenuItems();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to lookup client side element local components", e);
        }
        
        Map<String, Object> parameters = super.getParameters(contextualParameters);
        
        if (_primaryMenuItem != null)
        {
            Map<String, Object> primaryParameters = _primaryMenuItem.getParameters(contextualParameters);
            parameters.put("primary-menu-item-id", _primaryMenuItem.getId());
            for (String paramId : primaryParameters.keySet())
            {
                if (!parameters.containsKey(paramId))
                {
                    parameters.put(paramId, primaryParameters.get(paramId));
                }
            }
        }
        
        // Gallery items
        _getGalleryItems(parameters, contextualParameters);

        // Menu items
        _getMenuItems(parameters, contextualParameters);
        
        return parameters;
    }
    
    /**
     * Get the gallery items
     * @param parameters Contextual the parameters given to the control script class.
     * @param contextualParameters Contextual parameters transmitted by the environment.
     */
    @SuppressWarnings("unchecked")
    protected void _getGalleryItems (Map<String, Object> parameters, Map<String, Object> contextualParameters)
    {
        if (_galleryItems.size() > 0)
        {
            parameters.put("gallery-item", new LinkedHashMap<String, Object>());
            
            for (GalleryItem galleryItem : _galleryItems)
            {
                Map<String, Object> galleryItems = (Map<String, Object>) parameters.get("gallery-item");
                galleryItems.put("gallery-groups", new ArrayList<>());
                
                for (GalleryGroup galleryGp : galleryItem.getGroups())
                {
                    List<Object> galleryGroups = (List<Object>) galleryItems.get("gallery-groups");
                    
                    Map<String, Object> groupParams = new LinkedHashMap<>();
                    
                    I18nizableText label = galleryGp.getLabel();
                    groupParams.put("label", label);
                    
                    // Group items
                    List<String> gpItems = new ArrayList<>();
                    for (ClientSideElement element : galleryGp.getItems())
                    {
                        gpItems.add(element.getId());
                    }
                    groupParams.put("items", gpItems);
                    
                    galleryGroups.add(groupParams);
                }
            }
        }
    }
    
    /**
     * Get the menu items
     * @param parameters Contextual the parameters given to the control script class.
     * @param contextualParameters Contextual parameters transmitted by the environment.
     */
    protected void _getMenuItems (Map<String, Object> parameters, Map<String, Object> contextualParameters)
    {
        if (_menuItems.size() > 0)
        {
            List<String> menuItems = new ArrayList<>();
            for (ClientSideElement element : _menuItems)
            {
                menuItems.add(element.getId());
            }
            parameters.put("menu-items", menuItems);
        }
    }
    
    @Override
    public List<ClientSideElement> getReferencedClientSideElements()
    {
        return _referencedClientSideElement;
    }
    
    /**
     * Configure the galleries
     * @param configuration the configuration
     * @throws ConfigurationException
     */
    protected void _configureGalleries(Configuration configuration) throws ConfigurationException
    {
        for (Configuration galleryConfiguration : configuration.getChildren("gallery-item"))
        {
            GalleryItem galleryItem = new GalleryItem();
            
            for (Configuration gpConfiguration : galleryConfiguration.getChildren("gallery-group"))
            {
                galleryItem.addGroup(_configureGroupGallery(gpConfiguration));
            }
            
            _galleryItems.add(galleryItem);
        }
        
        // FIXME
        if (_galleryItems.size() > 0)
        {
            try
            {
                _galleryItemManager.initialize();
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Unable to lookup parameter local components", configuration, e);
            }
        }
    }
    
    /**
     * Configure a group gallery
     * @param configuration the configuration
     * @return The configured group gallery
     * @throws ConfigurationException
     */
    protected GalleryGroup _configureGroupGallery(Configuration configuration) throws ConfigurationException
    {
        I18nizableText label;
        
        Configuration labelConfig = configuration.getChild("label");
        if (labelConfig.getAttributeAsBoolean("i18n", false))
        {
            label = new I18nizableText("plugin." + _pluginName, labelConfig.getValue(""));
        }
        else
        {
            label = new I18nizableText(labelConfig.getValue(""));
        }
        
        GalleryGroup galleryGroup = new GalleryGroup(label);
        
        for (Configuration itemConfig : configuration.getChildren("item"))
        {
            if (itemConfig.getAttribute("ref", null) != null)
            {
                galleryGroup.addItem(new UnresolvedItem(itemConfig.getAttribute("ref"), false));
            }
            else
            {
                String id = itemConfig.getAttribute("id");
                try
                {
                    DefaultConfiguration conf = new DefaultConfiguration("extension");
                    conf.setAttribute("id", id);
                    conf.addChild(itemConfig.getChild("class"));
                    
                    _galleryItemManager.addComponent(_pluginName, null, id, StaticClientSideElement.class, conf);
                    
                    galleryGroup.addItem(new UnresolvedItem(id, true));
                }
                catch (ComponentException e)
                {
                    throw new ConfigurationException("Unable to configure local client side element of id " + id, e);
                }
            }
        }
        
        return galleryGroup;
    }
    
    
    /**
     * Configure the items menu
     * @param configuration the configuration
     * @throws ConfigurationException
     */
    protected void _configureItemsMenu(Configuration configuration) throws ConfigurationException
    {
        for (Configuration menuItemConfiguration : configuration.getChildren("menu-items"))
        {
            for (Configuration itemConfig : menuItemConfiguration.getChildren("item"))
            {
                boolean isPrimary = itemConfig.getAttributeAsBoolean("primaryItem", false);
                
                if (itemConfig.getAttribute("ref", null) != null)
                {
                    _unresolvedMenuItems.add(new UnresolvedItem(itemConfig.getAttribute("ref"), false, isPrimary));
                }
                else
                {
                    String id = itemConfig.getAttribute("id");
                    try
                    {
                        DefaultConfiguration conf = new DefaultConfiguration("extension");
                        conf.setAttribute("id", id);
                        conf.addChild(itemConfig.getChild("class"));
                        
                        if (itemConfig.getChild("menu-items", false) != null || itemConfig.getChild("gallery-item", false) != null)
                        {
                            if (itemConfig.getChild("menu-items", false) != null)
                            {
                                conf.addChild(itemConfig.getChild("menu-items"));
                            }
                            
                            if (itemConfig.getChild("gallery-item", false) != null)
                            {
                                conf.addChild(itemConfig.getChild("gallery-item"));
                            }
                            
                            _menuItemManager.addComponent(_pluginName, null, id, SimpleMenu.class, conf);
                        }
                        else
                        {
                            _menuItemManager.addComponent(_pluginName, null, id, StaticClientSideElement.class, conf);
                        }
                        _unresolvedMenuItems.add(new UnresolvedItem(id, true, isPrimary));
                    }
                    catch (ComponentException e)
                    {
                        throw new ConfigurationException("Unable to configure local client side element of id " + id, e);
                    }
                }
            }
        }        
    }
    
    private void _resolveMenuItems () throws Exception
    {
        if (_unresolvedMenuItems != null)
        {
            _menuItemManager.initialize();
            
            for (UnresolvedItem unresolvedItem : _unresolvedMenuItems)
            {
                String id = unresolvedItem.getId();
                ClientSideElement element;
                if (unresolvedItem.isLocalItem())
                {
                    try
                    {
                        element = _menuItemManager.lookup(id);
                    }
                    catch (ComponentException e)
                    {
                        throw new Exception("Unable to lookup client side element role: '" + id + "'", e);
                    }
                }
                else
                {
                    element = _ribbonControlManager.getExtension(id);
                }
                
                if (unresolvedItem.isPrimary())
                {
                    _primaryMenuItem = element;
                }
                
                _menuItems.add(element);
                _referencedClientSideElement.add(element);
            }
        }
        
        _unresolvedMenuItems = null;
    }
    
    /**
     * Class representing a gallery item
     *
     */
    public class GalleryItem
    {
        private final List<GalleryGroup> _groups;
        
        /**
         * Constructor
         */
        public GalleryItem()
        {
            _groups = new ArrayList<>();
        }
        
        /**
         * Add a group of this gallery
         * @param group The gallery group to add
         */
        public void addGroup (GalleryGroup group)
        {
            _groups.add(group);
        }
        
        /**
         * Get gallery's groups
         * @return The gallery's group
         */
        public List<GalleryGroup> getGroups ()
        {
            return _groups;
        }
        
    }
    
    /**
     * Class representing a gallery group
     *
     */
    public class GalleryGroup
    {
        private final I18nizableText _label;
        private List<UnresolvedItem> _unresolvedGalleryItems;
        private final List<ClientSideElement> _items;
        
        /**
         * Constructor 
         * @param label The group's label
         */
        public GalleryGroup(I18nizableText label)
        {
            _label = label;
            _items = new ArrayList<>();
            _unresolvedGalleryItems = new ArrayList<>();
        }
        
        /**
         * Add a new item to group
         * @param item The item to add
         */
        public void addItem (UnresolvedItem item)
        {
            _unresolvedGalleryItems.add(item);
        }
        
        /**
         * Get the group's label
         * @return The group's label
         */
        public I18nizableText getLabel ()
        {
            return _label;
        }
        
        /**
         * Get the gallery item
         * @return The gallery item
         */
        public List<ClientSideElement> getItems ()
        {
            try
            {
                _resolveGalleryItems();
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Unable to lookup client side element local components", e);
            }
            
            return _items;
        }
        
        private void _resolveGalleryItems () throws Exception
        {
            if (_unresolvedGalleryItems != null)
            {
                // FIXME
//                _galleryItemManager.initialize();
                
                for (UnresolvedItem unresolvedItem : _unresolvedGalleryItems)
                {
                    ClientSideElement element;
                    String id = unresolvedItem.getId();
                    if (unresolvedItem.isLocalItem())
                    {
                        try
                        {
                            element = _galleryItemManager.lookup(id);
                        }
                        catch (ComponentException e)
                        {
                            throw new Exception("Unable to lookup client side element role: '" + id + "'", e);
                        }
                    }
                    else
                    {
                        element = _ribbonControlManager.getExtension(id);
                    }
                    
                    _items.add(element);
                    _referencedClientSideElement.add(element);
                }
            }
            _unresolvedGalleryItems = null;
        }
    }
    
    /**
     * The unresolved item
     *
     */
    protected class UnresolvedItem
    {
        private final String _itemId;
        private final boolean _local;
        private final boolean _primary;
        
        /**
         * Constructor
         * @param id The item id
         * @param local true if it is a local item
         */
        public UnresolvedItem(String id, boolean local)
        {
            _itemId = id;
            _local = local;
            _primary = false;
        }
        
        /**
         * Constructor
         * @param id The item id
         * @param local true if it is a local item
         * @param primary true if it is a primary item
         */
        public UnresolvedItem(String id, boolean local, boolean primary)
        {
            _itemId = id;
            _local = local;
            _primary = primary;
        }
        
        /**
         * Get the item id
         * @return the item id
         */
        public String getId ()
        {
            return _itemId;
        }
        
        /**
         * Return true if it is a local item
         * @return true if it is a local item
         */
        public boolean isLocalItem ()
        {
            return _local;
        }
        
        /**
         * Return true if it is a primary item
         * @return true if it is a primary item
         */
        public boolean isPrimary ()
        {
            return _primary;
        }
    }
}
