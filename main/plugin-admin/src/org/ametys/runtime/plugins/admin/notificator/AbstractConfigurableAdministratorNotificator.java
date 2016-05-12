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
package org.ametys.runtime.plugins.admin.notificator;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugins.admin.notificator.Notification.NotificationType;

/**
 * Abstract {@link AdministratorNotificator} which is {@link Configurable}.
 */
public abstract class AbstractConfigurableAdministratorNotificator implements AdministratorNotificator, Configurable, PluginAware
{
    /** The name of the plugin that has declared this component */
    protected String _pluginName;
    /** The type of the notifications */
    protected NotificationType _type;
    /** The glyph icon of the  notifications */
    protected String _iconGlyph;
    /** The i18n key of the title of the notifications */
    protected I18nizableText _title;
    /** The i18n key of the description of the notifications */
    protected I18nizableText _message;
    /** The client-side action of the notifications */
    protected String _action;
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _type = NotificationType.valueOf(configuration.getChild("type").getValue("warn").toUpperCase());
        _iconGlyph = configuration.getChild("icon-glyph").getValue("");
        
        _title = I18nizableText.parseI18nizableText(configuration.getChild("title"), "plugin." + _pluginName);
        _message = I18nizableText.parseI18nizableText(configuration.getChild("message"), "plugin." + _pluginName);
        
        _action = configuration.getChild("action").getValue("Ext.emptyFn");
    }
}
