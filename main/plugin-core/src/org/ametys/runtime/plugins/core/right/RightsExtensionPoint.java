/*
 *  Copyright 2012 Anyware Services
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
import org.ametys.runtime.util.I18nizableText;


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
        _rights = new HashMap<>();
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
        I18nizableText i18nLabel = new I18nizableText("plugin." + pluginName, label);
        
        String description = configuration.getChild("description").getValue("");
        if (description.length() == 0)
        {
            throw new ConfigurationException("Right declaration is incorrect since no 'description' element is specified (or may be empty)", configuration);
        }
        I18nizableText i18nDescription = new I18nizableText("plugin." + pluginName, description);
        
        String category = configuration.getChild("category").getValue("");
        if (category.length() == 0)
        {
            throw new ConfigurationException("Right declaration is incorrect since no 'category' element is specified (or may be empty)", configuration);
        }
        I18nizableText i18nCategory = new I18nizableText("plugin." + pluginName, category);
        
        if (_rights.containsKey(id))
        {
            Right right = _rights.get(id);
            throw new ConfigurationException("Right with id '" + id + "' is already declared : '" + right.getDeclaration() + "'. This second declaration is ignored.", configuration);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding right ID : " + id);
        }

        Right right = new Right(id, i18nLabel, i18nDescription, i18nCategory, "Declared by plugin '" + pluginName + "'");
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
     * @throws IllegalArgumentException if the id is already declared
     */
    public void addRight(String id, I18nizableText labelKey, I18nizableText descriptionKey,  I18nizableText categoryKey) throws IllegalArgumentException
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

        Right right = new Right(id, labelKey, descriptionKey, categoryKey, "Declared by API");
        _rights.put(id, right);
    }

    public Right getExtension(String id)
    {
        return _rights.get(id);
    }

    public Set<String> getExtensionsIds()
    {
        return new HashSet<>(_rights.keySet());
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
