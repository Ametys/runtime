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
package org.ametys.runtime.plugins.admin.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.datasource.DataSourceConsumerExtensionPoint;
import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.plugins.admin.notificator.AbstractConfigurableAdministratorNotificator;
import org.ametys.runtime.plugins.admin.notificator.Notification;

/**
 * Notificator for unused datasources which have a invalid configuration
 */
public class InvalidDataSourceNotificator extends AbstractConfigurableAdministratorNotificator implements Serviceable
{
    private SQLDataSourceManager _sqlDataSourceManager;
    private LDAPDataSourceManager _ldapDataSourceManager;
    private DataSourceConsumerExtensionPoint _dataSourceConsumerEP;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _sqlDataSourceManager = (SQLDataSourceManager) smanager.lookup(SQLDataSourceManager.ROLE);
        _ldapDataSourceManager = (LDAPDataSourceManager) smanager.lookup(LDAPDataSourceManager.ROLE);
        _dataSourceConsumerEP = (DataSourceConsumerExtensionPoint) smanager.lookup(DataSourceConsumerExtensionPoint.ROLE);
    }
    
    @Override
    public List<Notification> getNotifications()
    {
        List<Notification> notifications = new ArrayList<>();
        Map<String, DataSourceDefinition> sqlDataSources = _sqlDataSourceManager.getDataSourceDefinitions(true, true, false);
        for (String id : sqlDataSources.keySet())
        {
            DataSourceDefinition datasource = sqlDataSources.get(id);
            if (!_dataSourceConsumerEP.isInUse(id))
            {
                _checkSqlDatasource(datasource, notifications);
            }
        }
        Map<String, DataSourceDefinition> ldapDataSources = _ldapDataSourceManager.getDataSourceDefinitions(true, true, false);
        for (String id : ldapDataSources.keySet())
        {
            DataSourceDefinition datasource = ldapDataSources.get(id);
            if (!_dataSourceConsumerEP.isInUse(id))
            {
                _checkLdapDatasource(datasource, notifications);
            }
        }
        return notifications;
    }
    
    private Notification _getNotification(String datasourceName)
    {
        I18nizableText message = new I18nizableText(_message.getCatalogue(), _message.getKey(), Collections.singletonList(datasourceName));
        return new Notification(_type, new I18nizableText(_title.getCatalogue(), _title.getKey()), message, _iconGlyph, _action);
    }
    
    private void _checkSqlDatasource(DataSourceDefinition datasource, List<Notification> notifications)
    {
        try
        {
            _sqlDataSourceManager.checkParameters (datasource.getParameters());
        }
        catch (ParameterCheckerTestFailureException e)
        {
            notifications.add(_getNotification(datasource.getName().getLabel()));
        }
    }
    
    private void _checkLdapDatasource(DataSourceDefinition datasource, List<Notification> notifications)
    {
        try
        {
            _ldapDataSourceManager.checkParameters (datasource.getParameters());
        }
        catch (ParameterCheckerTestFailureException e)
        {
            notifications.add(_getNotification(datasource.getName().getLabel()));
        }
    }

}
