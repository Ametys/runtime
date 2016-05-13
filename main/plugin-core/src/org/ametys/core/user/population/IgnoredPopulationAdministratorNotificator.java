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

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugins.admin.notificator.AbstractConfigurableAdministratorNotificator;
import org.ametys.runtime.plugins.admin.notificator.Notification;

/**
 * Notificator for {@link UserPopulation}s which are ignored because their configuration is invalid.
 */
public class IgnoredPopulationAdministratorNotificator extends AbstractConfigurableAdministratorNotificator implements Serviceable
{
    private UserPopulationDAO _userPopulationDAO;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
    }
    
    @Override
    public List<Notification> getNotifications()
    {
        Set<String> misconfiguredPopulations = _userPopulationDAO.getIgnoredPopulations();
        if (!misconfiguredPopulations.isEmpty())
        {
            String parameter = _getParameter(misconfiguredPopulations);
            I18nizableText message = new I18nizableText(_message.getCatalogue(), _message.getKey(), Collections.singletonList(parameter));
            Notification notification = new Notification(_type, _title, message, _iconGlyph, _action);
            return Collections.singletonList(notification);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
    
    private String _getParameter(Set<String> populations)
    {
        StringBuilder sb = new StringBuilder();
        
        for (String id : populations)
        {
            sb.append("<li>").append(id).append("</li>");
        }
        
        return sb.toString();
    }
}
