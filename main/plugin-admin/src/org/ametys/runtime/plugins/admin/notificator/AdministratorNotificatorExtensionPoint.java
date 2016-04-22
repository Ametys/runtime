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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ametys.core.ui.Callable;
import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * {@link ExtensionPoint} in charge of collecting all the administrator notifications to send.
 */
public class AdministratorNotificatorExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<AdministratorNotificator>
{
    /** Avalon Role */
    public static final String ROLE = AdministratorNotificatorExtensionPoint.class.getName();
    
    /**
     * Gets the notifications to send to the admin.
     * @return The list of all notifications to send.
     */
    @Callable
    public List<Map<String, Object>> getNotifications()
    {
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        Iterator<String> it = getExtensionsIds().iterator();
        while (it.hasNext())
        {
            AdministratorNotificator notificator = getExtension(it.next());
            notifications.addAll(notificator.getNotifications().stream().map(Notification::toMap).collect(Collectors.toList()));
        }
        
        return notifications;
    }
}
