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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring;

import java.util.Collections;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugins.admin.notificator.AbstractConfigurableAdministratorNotificator;
import org.ametys.runtime.plugins.admin.notificator.Notification;

/**
 * Notificator for insufficient available free space
 */
public class DiskSpaceAdministratorNotificator extends AbstractConfigurableAdministratorNotificator implements Serviceable
{
    private DiskSpaceHelper _diskSpaceHelper;
    private long _limit;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _diskSpaceHelper = (DiskSpaceHelper) manager.lookup(DiskSpaceHelper.ROLE);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        _limit = configuration.getChild("freeSpaceLimit").getValueAsLong();
    }
    
    @Override
    public List<Notification> getNotifications()
    {
        if (_diskSpaceHelper.getAvailableSpace() < _limit)
        {
            return Collections.singletonList(new Notification(_type, new I18nizableText(null, _title), new I18nizableText(null, _message), _iconGlyph, _action));
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

}
