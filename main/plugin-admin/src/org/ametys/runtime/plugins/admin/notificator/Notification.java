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

import java.util.LinkedHashMap;
import java.util.Map;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * Represents a client-side notification.  
 */
public class Notification
{
    /** The possible type of notification */
    public static enum NotificationType
    {
        /** INFO */
        INFO,
        /** WARN */
        WARN,
        /** ERROR */
        ERROR
    }
    
    /** The type of notification */
    protected NotificationType _type;
    /** The title of the notification */
    protected I18nizableText _title;
    /** The detailed message of the notification */
    protected I18nizableText _message;
    /** The icon of the notification */
    protected String _iconGlyph;
    /** The associated JS action */
    protected String _action;

    /**
     * Constructs a new notification.
     * @param type The type of notification
     * @param title The title of the notification
     * @param message The detailed message (description) of the notification (can contain HTML)
     * @param iconGlyph The icon of the notification
     * @param action The JS action to execute when clicking on '&lt;a&gt;' elements of description
     */
    public Notification(NotificationType type, I18nizableText title, I18nizableText message, String iconGlyph, String action)
    {
        _type = type;
        _title = title;
        _message = message;
        _iconGlyph = iconGlyph;
        _action = action;
    }
    
    /**
     * Gets the representation of a notification as a map
     * @return The notification as a map
     */
    public Map<String, Object> toMap()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", _type.toString().toLowerCase());
        map.put("title", _title);
        map.put("message", _message);
        map.put("iconGlyph", _iconGlyph);
        map.put("action", _action);
        return map;
    }
}
