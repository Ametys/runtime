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
package org.ametys.plugins.core.impl.user.population;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.authentication.CredentialProvider;
import org.ametys.core.authentication.CredentialProviderFactory;
import org.ametys.core.authentication.CredentialProviderModel;
import org.ametys.core.datasource.DataSourceConsumer;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.core.user.directory.UserDirectoryModel;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Implementation of {@link DataSourceConsumer} allowing to know whether a data source is used by the populations or not.
 * It also allows to retrieve the data source ids that are currently in use.
 */
public class PopulationDataSourceConsumer implements DataSourceConsumer, Component, Serviceable
{
    /** Avalon Role */
    public static final String ROLE = PopulationDataSourceConsumer.class.getName();
    
    /** The DAO for {@link UserPopulation}s */
    private UserPopulationDAO _userPopulationDAO;
    
    /** The user directories factory  */
    private UserDirectoryFactory _userDirectoryFactory;

    /** The credential providers factory  */
    private CredentialProviderFactory _credentialProviderFactory;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _userDirectoryFactory = (UserDirectoryFactory) manager.lookup(UserDirectoryFactory.ROLE);
        _credentialProviderFactory = (CredentialProviderFactory) manager.lookup(CredentialProviderFactory.ROLE);
    }
    
    @Override
    public boolean isInUse(String id)
    {
        return SQLDataSourceManager.AMETYS_INTERNAL_DATASOURCE_ID.equals(id) || _isInUseByUserDirectories(id) || _isInUseByCredentialProviders(id);
    }
    
    private boolean _isInUseByUserDirectories(String id)
    {
        for (String udModelId : _userDirectoryFactory.getExtensionsIds())
        {
            UserDirectoryModel udModel = _userDirectoryFactory.getExtension(udModelId);
            
            // for this model, which parameters are of type "datasource"
            List<String> datasourceParameters = new ArrayList<>();
            Map<String, ? extends Parameter<ParameterType>> parameters = udModel.getParameters();
            for (String parameterId : parameters.keySet())
            {
                if (ParameterType.DATASOURCE.equals(parameters.get(parameterId).getType()))
                {
                    datasourceParameters.add(parameterId);
                }
            }
            
            // search the user directories of this model
            for (UserPopulation population : _userPopulationDAO.getUserPopulations(false)) // Admin uses internal, no need to test
            {
                for (UserDirectory userDirectory : population.getUserDirectories())
                {
                    if (userDirectory.getUserDirectoryModelId().equals(udModelId))
                    {
                        for (String datasourceParameter : datasourceParameters)
                        {
                            // return true if it is the datasource id we're looking for, continue otherwise
                            if (id.equals(userDirectory.getParameterValues().get(datasourceParameter)))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean _isInUseByCredentialProviders(String id)
    {
        for (String cpModelId : _credentialProviderFactory.getExtensionsIds())
        {
            CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(cpModelId);
            
            // for this model, which parameters are of type "datasource"
            List<String> datasourceParameters = new ArrayList<>();
            Map<String, ? extends Parameter<ParameterType>> parameters = cpModel.getParameters();
            for (String parameterId : parameters.keySet())
            {
                if (ParameterType.DATASOURCE.equals(parameters.get(parameterId).getType()))
                {
                    datasourceParameters.add(parameterId);
                }
            }
            
            // search the credential providers of this model
            for (UserPopulation population : _userPopulationDAO.getUserPopulations(false)) // Admin uses internal, no need to test
            {
                for (CredentialProvider credentialProvider : population.getCredentialProviders())
                {
                    if (credentialProvider.getCredentialProviderModelId().equals(cpModelId))
                    {
                        for (String datasourceParameter : datasourceParameters)
                        {
                            // return true if it is the datasource id we're looking for, continue otherwise
                            if (id.equals(credentialProvider.getParameterValues().get(datasourceParameter)))
                            {
                                return true;
                            }
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
        result.add(SQLDataSourceManager.AMETYS_INTERNAL_DATASOURCE_ID);

        for (String udModelId : _userDirectoryFactory.getExtensionsIds())
        {
            UserDirectoryModel udModel = _userDirectoryFactory.getExtension(udModelId);
            
            // for this model, which parameters are of type "datasource"
            List<String> datasourceParameters = new ArrayList<>();
            Map<String, ? extends Parameter<ParameterType>> parameters = udModel.getParameters();
            for (String parameterId : parameters.keySet())
            {
                if (ParameterType.DATASOURCE.equals(parameters.get(parameterId).getType()))
                {
                    datasourceParameters.add(parameterId);
                }
            }
            
            // search the user directories of this model
            for (UserPopulation population : _userPopulationDAO.getUserPopulations(false)) // admin uses internal
            {
                for (UserDirectory userDirectory : population.getUserDirectories())
                {
                    if (userDirectory.getUserDirectoryModelId().equals(udModelId))
                    {
                        for (String datasourceParameter : datasourceParameters)
                        {
                            // this datasource value is used
                            result.add((String) userDirectory.getParameterValues().get(datasourceParameter));
                        }
                    }
                }
            }
        }
        
        // same procedure for credential providers
        for (String cpModelId : _credentialProviderFactory.getExtensionsIds())
        {
            CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(cpModelId);
            
            // for this model, which parameters are of type "datasource"
            List<String> datasourceParameters = new ArrayList<>();
            Map<String, ? extends Parameter<ParameterType>> parameters = cpModel.getParameters();
            for (String parameterId : parameters.keySet())
            {
                if (ParameterType.DATASOURCE.equals(parameters.get(parameterId).getType()))
                {
                    datasourceParameters.add(parameterId);
                }
            }
            
            // search the credential providers of this model
            for (UserPopulation population : _userPopulationDAO.getUserPopulations(false)) // admin uses internal
            {
                for (CredentialProvider credentialProvider : population.getCredentialProviders())
                {
                    if (credentialProvider.getCredentialProviderModelId().equals(cpModelId))
                    {
                        for (String datasourceParameter : datasourceParameters)
                        {
                            // this datasource value is used
                            result.add((String) credentialProvider.getParameterValues().get(datasourceParameter));
                        }
                    }
                }
            }
        }
        
        return result;
    }
}
