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
package org.ametys.core.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.datasource.DataSourceConsumer;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryFactory;
import org.ametys.core.group.directory.GroupDirectoryModel;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Implementation of {@link DataSourceConsumer} allowing to know whether a data source is used by the group directories or not.
 * It also allows to retrieve the data source ids that are currently in use.
 */
public class GroupDirectoriesDataSourceConsumer implements DataSourceConsumer, Component, Serviceable
{
    private GroupDirectoryDAO _groupDirectoryDAO;
    private GroupDirectoryFactory _groupDirectoryFactory;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _groupDirectoryDAO = (GroupDirectoryDAO) manager.lookup(GroupDirectoryDAO.ROLE);
        _groupDirectoryFactory = (GroupDirectoryFactory) manager.lookup(GroupDirectoryFactory.ROLE);
    }
    @Override
    public boolean isInUse(String id)
    {
        for (String gdModelId : _groupDirectoryFactory.getExtensionsIds())
        {
            GroupDirectoryModel gdModel = _groupDirectoryFactory.getExtension(gdModelId);
            
            // for this model, which parameters are of type "datasource"
            List<String> datasourceParameters = new ArrayList<>();
            Map<String, ? extends Parameter<ParameterType>> parameters = gdModel.getParameters();
            for (String parameterId : parameters.keySet())
            {
                if (ParameterType.DATASOURCE.equals(parameters.get(parameterId).getType()))
                {
                    datasourceParameters.add(parameterId);
                }
            }
            
            // search the group directories of this model
            for (GroupDirectory groupDirectory : _groupDirectoryDAO.getGroupDirectories())
            {
                if (groupDirectory.getGroupDirectoryModelId().equals(gdModelId))
                {
                    for (String datasourceParameter : datasourceParameters)
                    {
                        // return true if it is the datasource id we're looking for, continue otherwise
                        if (id.equals(groupDirectory.getParameterValues().get(datasourceParameter)))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    @Override
    public Set<String> getUsedDataSourceIds()
    {
        Set<String> result = new HashSet<>();
        
        for (String gdModelId : _groupDirectoryFactory.getExtensionsIds())
        {
            GroupDirectoryModel gdModel = _groupDirectoryFactory.getExtension(gdModelId);
            
            // for this model, which parameters are of type "datasource"
            List<String> datasourceParameters = new ArrayList<>();
            Map<String, ? extends Parameter<ParameterType>> parameters = gdModel.getParameters();
            for (String parameterId : parameters.keySet())
            {
                if (ParameterType.DATASOURCE.equals(parameters.get(parameterId).getType()))
                {
                    datasourceParameters.add(parameterId);
                }
            }
            
            // search the group directories of this model
            for (GroupDirectory groupDirectory : _groupDirectoryDAO.getGroupDirectories())
            {
                if (groupDirectory.getGroupDirectoryModelId().equals(gdModelId))
                {
                    for (String datasourceParameter : datasourceParameters)
                    {
                        // this datasource value is used
                        result.add((String) groupDirectory.getParameterValues().get(datasourceParameter));
                    }
                }
            }
        }
        
        return result;
    }
}
