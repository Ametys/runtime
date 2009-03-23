/*
 * Copyright (c) 2007 Anyware Technologies and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors: Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.ui.item;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.runtime.ui.item.UIItem;
import org.ametys.runtime.ui.item.part.Action;
import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.ui.item.part.Shortcut;
import org.ametys.runtime.util.I18nizableText;

/**
 * This factory handle interactions that is fully configured and that never
 * change.<br/> It can remove an entry if a given right is not defined for the
 * user.
 */
public class StaticAdminUIItemFactory extends StaticUIItemFactory
{
   
    /**
     * Configure an interaction
     * 
     * @param configuration The root configuration
     * @return The new interaction
     * @throws ConfigurationException if the configuration is incorrect
     */
    protected UIItem _configureStatic(Configuration configuration) throws ConfigurationException
    {
        // Détermine les élements de l'interaction en fonction de la
        // configuration
        I18nizableText label = _configureLabel(configuration);
        I18nizableText description = _configureDescription(configuration);
        Shortcut shortcut = _configureShortcut(configuration);
        IconSet iconSet = _configureIconSet("Icons", configuration);
        Action action = _configureAction(configuration);
        String right = _configureRight(configuration);
        String url = _configureUrl(configuration);
        
        // Crée l'interaction
        AdminInteractionImpl interaction = new AdminInteractionImpl(label, description, iconSet);
        interaction.setAction(action);
        interaction.setShortcut(shortcut);
        interaction.setEnabled(true);
        interaction.setRight(right);
        interaction.setUrl(url);

        return interaction;
    }
    
    /**
     * Create an i18nized label text following configuration
     * 
     * @param configuration The root configuration
     * @return The text of main label
     */
    protected String _configureUrl(Configuration configuration)
    {
        String pluginName = configuration.getChild("Url").getAttribute("plugin", _pluginName);
        String url = configuration.getChild("Url").getValue("");
        if (url.length() == 0)
        {
            return null;
        }
        return "_plugins/" + pluginName + "/" + url;
    }
}
