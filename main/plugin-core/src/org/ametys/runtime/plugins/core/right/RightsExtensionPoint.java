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
package org.ametys.runtime.plugins.core.right;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.ExtensionPoint;


/**
 * This extension point handle a list of rights handled by the plugins or the application.
 */
public class RightsExtensionPoint extends AbstractLogEnabled implements ExtensionPoint<Right>, Initializable, ThreadSafe, Component
{
    /** The avalon role */
    public static final String ROLE = RightsExtensionPoint.class.getName();
    
    /** The map of rightId, Right of declared rights */
    protected Map<String, Right> _rights;

    public void initialize() throws Exception
    {
        _rights = new HashMap<String, Right>();
    }
    
    public boolean hasExtension(String id)
    {
        return _rights.containsKey(id);
    }

    public void addExtension(String pluginName, String pluginId, Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding rights from plugin " + pluginName + "/" + pluginId);
        }

        try
        {
            Configuration[] rightsConfigurations = configuration.getChildren("right");
            for (Configuration rightConfiguration : rightsConfigurations)
            {
                addRight(pluginName, rightConfiguration);
            }
        }
        catch (ConfigurationException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The plugin '" + pluginName + "." + pluginId + "' has a rights extension but has an incorrect configuration", e);
            }
        }
    }
    
    /**
     * Declare a new right (not as used)
     * @param pluginName The name of the plugin declaring the extension
     * @param configuration The configuration of the extension
     * @throws ConfigurationException if configuration if not complete
     */
    protected void addRight(String pluginName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id", "");
        if (id.length() == 0)
        {
            throw new ConfigurationException("Right declaration is incorrect since no 'Id' attribute is specified (or may be empty)", configuration);
        }

        String label = configuration.getChild("label").getValue("");
        if (label.length() == 0)
        {
            throw new ConfigurationException("Right declaration is incorrect since no 'label' element is specified (or may be empty)", configuration);
        }
        
        String description = configuration.getChild("description").getValue("");
        if (description.length() == 0)
        {
            throw new ConfigurationException("Right declaration is incorrect since no 'description' element is specified (or may be empty)", configuration);
        }
        
        String category = configuration.getChild("category").getValue("");
        if (category.length() == 0)
        {
            throw new ConfigurationException("Right declaration is incorrect since no 'category' element is specified (or may be empty)", configuration);
        }
        
        if (_rights.containsKey(id))
        {
            Right right = _rights.get(id);
            throw new ConfigurationException("Right with id '" + id + "' is already declared : '" + right.getDeclaration() + "'. This second declaration is ignored.", configuration);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding right ID : " + id);
        }

        Right right = new Right(id, label, description, category, "plugin." + pluginName, "Declared by plugin '" + pluginName + "'");
        if (_rights.containsKey(id))
        {
            Right oldright = _rights.get(id);
            throw new IllegalArgumentException("Right with id '" + id + "' is already declared : '" + oldright.getDeclaration() + "'. This second declaration is ignored.");
        }
        _rights.put(id, right);
    }
    
    /**
     * Declare a new right as used. Use this method to add a right programmatically.
     * @param id The id of the right (not null or empty)
     * @param labelKey The label of the right (i18n key) (not null or empty)
     * @param descriptionKey The description of the right (i18n key) (not null or empty)
     * @param categoryKey The category of the right (i18n key) (not null or empty)
     * @param catalogue The catalogue full identifier (not null or empty)
     * @throws IllegalArgumentException if the id is already declared
     */
    public void addRight(String id, String labelKey, String descriptionKey,  String categoryKey, String catalogue) throws IllegalArgumentException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding right from API with ID : " + id);
        }

        if (_rights.containsKey(id))
        {
            Right right = _rights.get(id);
            throw new IllegalArgumentException("Right with id '" + id + "' is already declared : '" + right.getDeclaration() + "'. This second declaration is ignored.");
        }

        Right right = new Right(id, labelKey, descriptionKey, categoryKey, catalogue, "Declared by API");
        _rights.put(id, right);
    }

    public Right getExtension(String id)
    {
        return _rights.get(id);
    }

    public Set<String> getExtensionsIds()
    {
        return new HashSet<String>(_rights.keySet());
    }

    public void initializeExtensions() throws Exception
    {
        // empty
    }
    
    /**
     * SAX all managed rights
     * 
     * @param handler the handler receiving SAX events
     * @throws SAXException if something wrong occurs
     */
    public void toSAX (ContentHandler handler) throws SAXException
    {
        for (String id : _rights.keySet())
        {
            Right right = _rights.get(id);
            right.toSAX(handler);
        }
    }
}
