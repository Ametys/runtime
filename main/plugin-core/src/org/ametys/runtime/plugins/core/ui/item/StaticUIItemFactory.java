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
package org.ametys.runtime.plugins.core.ui.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;

import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.ui.item.UIItem;
import org.ametys.runtime.ui.item.UIItemFactory;
import org.ametys.runtime.ui.item.part.Action;
import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.ui.item.part.Shortcut;
import org.ametys.runtime.user.UserHelper;
import org.ametys.runtime.util.I18nizableText;


/**
 * This factory handle interactions that is fully configured and that never change.<br/>
 * It can remove an entry if a given right is not defined for the user.
 */
public class StaticUIItemFactory extends AbstractLogEnabled implements UIItemFactory, Configurable, ThreadSafe, Serviceable, PluginAware, Contextualizable
{
    /** The plugin name */
    protected String _pluginName;
    
    /** The feature name */
    protected String _featureName;
    
    /** The list of items */
    protected List<UIItem> _itemsList;
    
    /** The avalon service manager */
    protected ServiceManager _sManager;
    
    /** The service manager */
    protected RightsManager _rightManager;
    
    /** The avalon context */
    protected Context _avalonContext;

    public void setPluginInfo(String pluginName, String pluginId)
    {
        _featureName = pluginId;
        _pluginName = pluginName;
    }
    
    public void contextualize(Context avalonContext) throws ContextException
    {
        _avalonContext = avalonContext;
    }
    
    public void service(ServiceManager sManager) throws ServiceException
    {
        _sManager = sManager;
        _rightManager = (RightsManager) sManager.lookup(RightsManager.ROLE);
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        List<UIItem> items = new ArrayList<UIItem>();
        
        Configuration[] interactionsConfiguration = configuration.getChildren();
        for (int i = 0; i < interactionsConfiguration.length; i++)
        {
            try
            {
                UIItem item = _configureItem(interactionsConfiguration[i]);
                items.add(item);
            }            
            catch (ConfigurationException e)
            {
                String message = "An interaction (number " + (i + 1) + ") in the plugin " + _pluginName + "/" + _featureName + " was not properly configured.";
                getLogger().error(message, e);
                throw new ConfigurationException(message, e);
            }
        }
        
        _itemsList = items;
    }

    public List<UIItem> getUIItems()
    {
        List<UIItem> items = new ArrayList<UIItem>();
        
        for (UIItem item : _itemsList)
        {
            if (item instanceof RightInteraction)
            {
                RightInteraction ri = (RightInteraction) item;
                String right = ri.getRight();
                if (hasRight(right))
                {
                    items.add(ri);
                }
            }
            else
            {
                items.add(item);
            }
        }
        
        return items;
    }
    
    /**
     * Configure an item
     * @param configuration The root configuration
     * @return The new item
     * @throws ConfigurationException
     */
    protected UIItem _configureItem(Configuration configuration) throws ConfigurationException
    {
        if ("Static".equals(configuration.getName()))
        {
            return _configureStatic(configuration);
        }
        else if ("Separator".equals(configuration.getName()))
        {
            return _configureSeparatorItem(configuration);
        }
        else
        {
            String message = "Unknown element " + configuration.getName();
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
    }
    
    /**
     * Configure an interaction
     * @param configuration The root configuration
     * @return The new interaction
     * @throws ConfigurationException if the configuration is incorrect
     */
    protected UIItem _configureStatic(Configuration configuration) throws ConfigurationException
    {
        // Détermine les élements de l'interaction en fonction de la configuration
        I18nizableText label = _configureLabel(configuration);
        I18nizableText description = _configureDescription(configuration);
        Shortcut shortcut = _configureShortcut(configuration);
        IconSet iconSet = _configureIconSet("Icons", configuration);
        Action action = _configureAction(configuration);
        String right = _configureRight(configuration);
        
        // Crée l'interaction
        RightInteraction interaction = new RightInteraction(label, description, iconSet);
        interaction.setAction(action);
        interaction.setShortcut(shortcut);
        interaction.setEnabled(true);
        interaction.setRight(right);
        
        return interaction;
    }

    /**
     * Configure a separator
     * @param configuration The root configuration
     * @return The separator
     * @throws ConfigurationException If the attribute type is incorrect
     */
    protected UIItem _configureSeparatorItem(Configuration configuration) throws ConfigurationException
    {
        String type = configuration.getAttribute("type", "");
        
        if ("space".equals(type))
        {
            return UIItem.SEPARATOR_SPACE;
        }
        else if ("bar".equals(type))
        {
            return UIItem.SEPARATOR_BAR;
        }
        else
        {
            String message = "The Separator/@type is unknown or missing";
            getLogger().error(message);
            throw new ConfigurationException(message);
        }
    }

    /**
     * Determine following the right parameter if the user has right to have this static entry
     * @param right The right name to check. Can be null.
     * @return true if the user has the right or if there is not right and false otherwise
     */
    protected boolean hasRight(String right)
    {
        if (right == null)
        {
            return true;
        }
        else
        {
            if (UserHelper.isAdministrator(ContextHelper.getObjectModel(_avalonContext)))
            {
                return true;
            }
            else
            {
                String userLogin = UserHelper.getCurrentUser(ContextHelper.getObjectModel(_avalonContext));
                if (userLogin == null)
                {
                    return false;
                }
                else
                {
                    return _rightManager.hasRight(userLogin, right, "/application") == RightsManager.RightResult.RIGHT_OK;
                }
            }
        }
    }

    /**
     * Create an i18nized label text following configuration
     * @param configuration The root configuration
     * @return The text of main label
     */
    protected I18nizableText _configureLabel(Configuration configuration)
    {
        String label = configuration.getChild("LabelKey").getValue("");
        if (label.length() == 0)
        {
            return null;
        }
        else
        {
            String catalogue = configuration.getChild("LabelKey").getAttribute("Catalogue", "plugin." + _pluginName);
            return new I18nizableText(catalogue, label);
        }
    }
    
    /**
     * Get the workflow name from configuration
     * @param configuration The root configuration
     * @return The name of configured workflow. Cannot be null or empty
     * @throws ConfigurationException If configuration is missing or incorrect.
     */
    protected String _configureWorkflowName(Configuration configuration) throws ConfigurationException
    {
        Configuration actionConfiguration = configuration.getChild("Action", false);
        if (actionConfiguration == null)
        {
            String message = "The Action element is empty.";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        String workflow = actionConfiguration.getAttribute("workflow", "");
        if (workflow.length() == 0)
        {
            String message = "The Action/@workflow element is missing or empty.";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        return workflow;
    }
    
    /**
     * Get the ids for a workflow action
     * @param configuration The root configuration
     * @return The list of ids. Cannot be null or empty
     * @throws ConfigurationException If configuration is missing or incorrect.
     */
    protected List<Integer> _configureWorkflowActionIds(Configuration configuration) throws ConfigurationException
    {
        Configuration actionConfiguration = configuration.getChild("Action", false);
        if (actionConfiguration == null)
        {
            String message = "The Action element is empty.";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        Configuration[] idsConfiguration = actionConfiguration.getChildren("Id");
        if (idsConfiguration.length == 0)
        {
            String message = "The Action element needs at least one Id element.";
            getLogger().error(message);
            throw new ConfigurationException(message, actionConfiguration);
        }
        
        List<Integer> waids = new ArrayList<Integer>();
        for (int i = 0; i < idsConfiguration.length; i++)
        {
            waids.add(idsConfiguration[i].getValueAsInteger());
        }
        return waids;
    }
    
    /**
     * Get the ids for a workflow state
     * @param configuration The root configuration
     * @return The list of ids. Cannot be null or empty
     * @throws ConfigurationException If configuration is missing or incorrect.
     */
    protected Set<Integer> _configureWorkflowStates(Configuration configuration) throws ConfigurationException
    {
        Configuration actionConfiguration = configuration.getChild("State", false);
        if (actionConfiguration == null)
        {
            String message = "The State element is empty.";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        Configuration[] idsConfiguration = actionConfiguration.getChildren("Id");
        if (idsConfiguration.length == 0)
        {
            String message = "The State element needs at least one Id element.";
            getLogger().error(message);
            throw new ConfigurationException(message, actionConfiguration);
        }
        
        Set<Integer> waids = new HashSet<Integer>();
        for (int i = 0; i < idsConfiguration.length; i++)
        {
            waids.add(idsConfiguration[i].getValueAsInteger());
        }
        return waids;
    }
    
    /**
     * Create an i18nized confirmation message text following configuration
     * @param configuration The root configuration
     * @return The text of message
     * @throws ConfigurationException If the message is not configuration properly
     */
    protected I18nizableText _configureConfirmationMessage(Configuration configuration) throws ConfigurationException
    {
        Configuration cmConfiguration = configuration.getChild("ConfirmationMessageKey", false);
        if (cmConfiguration != null)
        {
            String confirmationMessage = cmConfiguration.getValue("");
            if (confirmationMessage.length() == 0)
            {
                String message = "The optional ConfirmationMessageKey element is empty.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }
            
            String catalogue = cmConfiguration.getAttribute("Catalogue", "plugin." + _pluginName);
            
            return new I18nizableText(catalogue, confirmationMessage);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Create an i18nized description text following the configuration.
     * @param configuration The root configuration
     * @return The text of description
     */
    protected I18nizableText _configureDescription(Configuration configuration)
    {
        String description = configuration.getChild("DescriptionKey").getValue("");
        if (description.length() == 0)
        {
            return null;
        }
        else
        {
            String catalogue = configuration.getChild("DescriptionKey").getAttribute("Catalogue", "plugin." + _pluginName);
            return new I18nizableText(catalogue, description);
        }
    }

    /**
     * Create a shortcut following the configuration
     * @param configuration The root configuration
     * @return The shortcut. Can be null.
     * @throws ConfigurationException If the shortcut configuration is present but incorrect     */
    protected Shortcut _configureShortcut(Configuration configuration) throws ConfigurationException
    {
        Configuration shortcut = configuration.getChild("Shortcut", false);
        if (shortcut != null)
        {
            boolean shiftKey = "true".equals(shortcut.getAttribute("shift", "false"));
            boolean altKey = "true".equals(shortcut.getAttribute("alt", "false"));
            boolean ctrlKey = "true".equals(shortcut.getAttribute("ctrl", "false"));
            
            String key = shortcut.getValue(null);
            if (key == null)
            {
                String message = "The optional Shortcut element must have a value.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }
            
            return new Shortcut(ctrlKey, altKey, shiftKey, key);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Create an iconset following the configuration
     * @param rootElementName The root element name for this icon set
     * @param configuration The root configuration
     * @return the iconset. Cannot be null
     * @throws ConfigurationException if the configuration does not match
     */
    protected IconSet _configureIconSet(String rootElementName, Configuration configuration) throws ConfigurationException
    {
        Configuration iconset = configuration.getChild(rootElementName, false);
        if (iconset == null)
        {
            return null;
        }
        
        String smallIcon = iconset.getChild("Small").getValue("");
        String mediumIcon = iconset.getChild("Medium").getValue("");
        String largeIcon = iconset.getChild("Large").getValue("");
        
        if (smallIcon.length() == 0 || mediumIcon.length() == 0 || largeIcon.length() == 0)
        {
            String message = "The Icons element must have non empty Small, Medium and Large elements.";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        return new IconSet("/plugins/" + _pluginName + "/resources", smallIcon, mediumIcon, largeIcon);
    }

    /**
     * Create an action following the configuration
     * @param configuration The root configuration
     * @return the action or null
     * @throws ConfigurationException if an action is created but unfinished
     */
    protected Action _configureAction(Configuration configuration) throws ConfigurationException
    {
        Configuration actionConfiguration = configuration.getChild("Action", false);
        if (actionConfiguration != null)
        {
            String type = actionConfiguration.getAttribute("type", null);
            if ("url".equals(type))
            {
                String url = actionConfiguration.getChild("Url").getAttribute("value", "");
                if (url.length() == 0)
                {
                    String message = "The element Action[@type='url'] must a non-empty value attribute.";
                    getLogger().error(message);
                    throw new ConfigurationException(message, configuration);
                }
                else
                {
                    return Action.createLinkAction(_pluginName, url);
                }
            }
            else if ("function".equals(type))
            {
                String url = actionConfiguration.getChild("Function").getAttribute("value", "");
                if (url.length() == 0)
                {
                    String message = "The element Action[@type='url'] must a non-empty value attribute.";
                    getLogger().error(message);
                    throw new ConfigurationException(message, configuration);
                }
                else
                {
                    Set<String> imports = _configureActionImports(actionConfiguration.getChild("Imports", false));
                    Map<String, String> parameters = _configureActionParameters(actionConfiguration.getChild("Function", false));

                    return Action.createFunctionAction(_pluginName, url, imports, parameters);
                }
            }
            else if ("class".equals(type))
            {
                String url = actionConfiguration.getChild("Class").getAttribute("value", "");
                if (url.length() == 0)
                {
                    String message = "The element Action[@type='url'] must a non-empty value attribute.";
                    getLogger().error(message);
                    throw new ConfigurationException(message, configuration);
                }
                else
                {
                    Set<String> imports = _configureActionImports(actionConfiguration.getChild("Imports", false));
                    Map<String, String> parameters = _configureActionParameters(actionConfiguration.getChild("Class", false));

                    return Action.createClassAction(_pluginName, url, imports, parameters);
                }
            }
            else
            {
                String message = "The element Action/@type that is unknwown or missing.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }
        }
        else
        {
            return null;
        }
    }
    
    private Map<String, String> _configureActionParameters(Configuration configuration)
    {
        if (configuration != null)
        {
            Map<String, String> params = new HashMap<String, String>();
            
            Configuration[] paramsConfig = configuration.getChildren();
            for (int i = 0; i < paramsConfig.length; i++)
            {
                String paramName = paramsConfig[i].getName();
                String paramValue = paramsConfig[i].getValue("");
                
                params.put(paramName, paramValue);
            }
            
            return params;
        }
        else
        {
            return null;
        }
    }

    private Set<String> _configureActionImports(Configuration configuration) throws ConfigurationException
    {
        if (configuration != null)
        {
            Set<String> imports = new HashSet<String>();
            
            Configuration[] filesConfiguration = configuration.getChildren();
            for (int i = 0; i < filesConfiguration.length; i++)
            {
                String value = filesConfiguration[i].getValue("");
                if (!"File".equals(filesConfiguration[i].getName()))
                {
                    String message = "The element Action/Imports have an unknown element : " + filesConfiguration[i].getName();
                    getLogger().error(message);
                    throw new ConfigurationException(message, configuration);
                }
                else if (value.length() == 0)
                {
                    String message = "The element Action/Imports/File[" + i + "] have no value";
                    getLogger().error(message);
                    throw new ConfigurationException(message, configuration);
                }
                else
                {
                    imports.add("/plugins/" + _pluginName + "/resources/" + value);
                }
            }
            
            return imports;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Configure the right following the configuration
     * @param configuration The root configuration
     * @return The right name or null.
     * @throws ConfigurationException if a right element is present but empty
     */
    protected String _configureRight(Configuration configuration) throws ConfigurationException
    {
        Configuration rightConf = configuration.getChild("Right", false);
        if (rightConf != null)
        {
            String right = rightConf.getValue("");
            if (right.length() != 0)
            {
                return right;
            }
            else
            {
                String message = "The optional Right element is empty.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }
        }
        else
        {
            return null;
        }
    }
}
