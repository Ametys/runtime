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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.xml.sax.SAXException;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.core.datasource.DataSourceDAO.DataSourceType;
import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.StaticClientSideElement;

/**
 * This element creates a ribbon button to delete a data source if it is not currently used.
 */
public class DeleteDataSourceClientSideElement extends StaticClientSideElement 
{
    private SQLDataSourceManager _sqlDataSourceManager;
    private LDAPDataSourceManager _ldapSourceManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _sqlDataSourceManager = (SQLDataSourceManager) smanager.lookup(SQLDataSourceManager.ROLE);
        _ldapSourceManager = (LDAPDataSourceManager) smanager.lookup(LDAPDataSourceManager.ROLE);
    }
    
    /**
     * Get state of data sources
     * @param datasourceIds the ids of data sources with their type.
     * @return informations on datasource's state
     * @throws IOException if an error occurred while reading configuration file
     * @throws SAXException if an error occurred while parsing configuration file
     * @throws ConfigurationException if an error occurred while parsing configuration reading file
     */
    @SuppressWarnings("unchecked")
    @Callable
    public Map<String, Object> getStatus(Map<String, String> datasourceIds) throws ConfigurationException, SAXException, IOException
    {
        Map<String, Object> results = new HashMap<>();
        
        results.put("allright-datasources", new ArrayList<Map<String, Object>>());
        results.put("inuse-datasources", new ArrayList<Map<String, Object>>());
        results.put("unknown-datasources", new ArrayList<String>());
        
        for (String id : datasourceIds.keySet())
        {
            DataSourceDefinition dsDef = null;
            boolean isInUse = false;
            
            DataSourceType type = DataSourceType.valueOf(datasourceIds.get(id));
            switch (type)
            {
                case SQL:
                    dsDef = _sqlDataSourceManager.getDataSourceDefinition(id);
                    isInUse = dsDef != null ? _sqlDataSourceManager.isInUse(id) : false;
                    break;
                case LDAP:
                    dsDef = _ldapSourceManager.getDataSourceDefinition(id);
                    isInUse = dsDef != null ? _ldapSourceManager.isInUse(id) : false;
                    break;
                default:
                    break;
            }
            
            if (dsDef != null)
            {
                if (isInUse)
                {
                    List<Map<String, Object>> inUseDataSources = (List<Map<String, Object>>) results.get("inuse-datasources");
                    inUseDataSources.add(_getDataSourceParameters(dsDef));
                }
                else
                {
                    List<Map<String, Object>> allRightDataSources = (List<Map<String, Object>>) results.get("allright-datasources");
                    allRightDataSources.add(_getDataSourceParameters(dsDef));
                }
            }
            else
            {
                List<String> unknownDataSources = (List<String>) results.get("unknown-datasources");
                unknownDataSources.add(id);
            }
            
        }
        
        return results;
    }
    
    private Map<String, Object> _getDataSourceParameters (DataSourceDefinition datasource)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("id", datasource.getId());
        params.put("name", datasource.getName());
        
        return params;
    }
}
