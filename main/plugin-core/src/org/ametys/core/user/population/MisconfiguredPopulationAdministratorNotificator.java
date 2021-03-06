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
package org.ametys.core.user.population;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugins.admin.notificator.AbstractConfigurableAdministratorNotificator;
import org.ametys.runtime.plugins.admin.notificator.Notification;
import org.ametys.runtime.plugins.admin.notificator.Notification.NotificationType;

/**
 * Notificator for {@link UserPopulation}s in warning because of a invalid configuration (not fatal)
 */
public class MisconfiguredPopulationAdministratorNotificator extends AbstractConfigurableAdministratorNotificator implements Serviceable
{
    private UserPopulationDAO _userPopulationDAO;
    private I18nUtils _i18nUtils;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    public List<Notification> getNotifications()
    {
        Set<String> misconfiguredPopulations = _userPopulationDAO.getMisconfiguredPopulations();
        if (!misconfiguredPopulations.isEmpty())
        {
            I18nizableText message = _getMessage (misconfiguredPopulations);
            Notification notification = new Notification(_getType(misconfiguredPopulations), _title, message, _iconGlyph, _action);
            return Collections.singletonList(notification);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
    
    private NotificationType _getType (Set<String> upIds)
    {
        for (String id : upIds)
        {
            UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(id);
            if (userPopulation.isEnabled())
            {
                // If at least one misconfigured population is enabled, the notification should appear as a error
                return NotificationType.ERROR;
            }
        }
        return NotificationType.WARN;
    }
    
    private I18nizableText _getMessage(Set<String> upIds)
    {
        StringBuilder sb = new StringBuilder();
        
        for (String id : upIds)
        {
            UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(id);
            
            String upLabel = _i18nUtils.translate(userPopulation.getLabel());
            sb.append("<li>").append(upLabel).append("</li>");
        }
        
        String i18nParam = sb.toString();
        return new I18nizableText(_message.getCatalogue(), _message.getKey(), Collections.singletonList(i18nParam));
    }
}
