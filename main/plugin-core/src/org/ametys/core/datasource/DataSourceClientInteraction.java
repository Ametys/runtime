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
package org.ametys.core.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.core.ui.Callable;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Component gathering manipulation methods for SQL and LDAP data sources 
 */
public class DataSourceClientInteraction extends AbstractLogEnabled implements Component, Serviceable
{
    /** The Avalon role */
    public static final String ROLE = DataSourceClientInteraction.class.getName();
    
    /** The SQL data source manager */
    private SQLDataSourceManager _sqlDataSourceManager;
    
    /** The SQL data source manager */
    private LDAPDataSourceManager _ldapDataSourceManager;
    
    /** The extension for data source clients */
    private DataSourceConsumerExtensionPoint _dataSourceConsumerEP;
    
    /**
     * Enum for data source types
     */
    public enum DataSourceType 
    {
        /** SQL */
        SQL,
        /** LDAP */
        LDAP
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _sqlDataSourceManager = (SQLDataSourceManager) manager.lookup(SQLDataSourceManager.ROLE);
        _ldapDataSourceManager = (LDAPDataSourceManager) manager.lookup(LDAPDataSourceManager.ROLE);
        _dataSourceConsumerEP = (DataSourceConsumerExtensionPoint) manager.lookup(DataSourceConsumerExtensionPoint.ROLE);
    }
    
    /**
     * Get the existing data sources
     * @param type the data source type. Can be empty or null to get all data sources
     * @param includePrivate true to include private data sources
     * @param includeInternal true to include internal data sources
     * @return the existing data sources
     * @throws Exception if an error occurred
     */
    @Callable
    public List<Map<String, Object>> getDataSources (String type, boolean includePrivate, boolean includeInternal) throws Exception
    {
        List<Map<String, Object>> datasources = new ArrayList<>();
        
        if (StringUtils.isEmpty(type) || type.equals(DataSourceType.SQL.toString()))
        {
            Map<String, DataSourceDefinition> sqlDataSources = _sqlDataSourceManager.getDataSourceDefinitions(includePrivate, includeInternal);
            for (String id : sqlDataSources.keySet())
            {
                datasources.add(getSQLDataSource(id));
            }
        }
        
        if (StringUtils.isEmpty(type) || type.equals(DataSourceType.LDAP.toString()))
        {
            Map<String, DataSourceDefinition> ldapDataSources = _ldapDataSourceManager.getDataSourceDefinitions(includePrivate, includeInternal);
            for (String id : ldapDataSources.keySet())
            {
                datasources.add(getLDAPDataSource(id));
            }
        }
        
        return datasources;
    }
    
    /**
     * Get the existing data sources whatever their type
     * @param includePrivate true to include private data sources
     * @param includeInternal true to include internal data sources
     * @return the existing data sources
     * @throws Exception if an error occurred
     */
    @Callable
    public List<Map<String, Object>> getDataSources (boolean includePrivate, boolean includeInternal) throws Exception
    {
        return getDataSources(null, includePrivate, includeInternal);
    }
    
    /**
     * Get the ldap data source information
     * @param type the data source's type
     * @param id The id the data source
     * @return The data source's information
     * @throws Exception if an error occurred
     */
    @Callable
    public Map<String, Object> getDataSource (String type, String id) throws Exception
    {
        DataSourceType dsType = DataSourceType.valueOf(type);
        switch (dsType)
        {
            case SQL:
                return getSQLDataSource(id);

            case LDAP:
                return getLDAPDataSource(id);
            default:
                getLogger().error("Unable to get data source: unknown data source type '" + type + "'");
                return null;
        }
    }
    
    
    /**
     * Get the ldap data source information
     * @param id The id the data source
     * @return The data source's information
     * @throws Exception if an error occurred
     */
    @Callable
    public Map<String, Object> getLDAPDataSource (String id) throws Exception
    {
        DataSourceDefinition ldapDefinition = _ldapDataSourceManager.getDataSourceDefinition(id);

        Map<String, Object> def2json = _dataSourceDefinition2Json(ldapDefinition);
        def2json.put("type", "LDAP");
        def2json.put("isInUse", _dataSourceConsumerEP.isInUse(ldapDefinition.getId()));
        
        return def2json;
    }
    
    /**
     * Get the sql data source information
     * @param id The id the data source
     * @return The data source's information
     * @throws Exception if an error occurred
     */
    @Callable
    public Map<String, Object> getSQLDataSource (String id) throws Exception
    {
        DataSourceDefinition sqlDefinition = _sqlDataSourceManager.getDataSourceDefinition(id);
        
        Map<String, Object> def2json = _dataSourceDefinition2Json(sqlDefinition);
        def2json.put("type", "SQL");
        def2json.put("isInUse", _dataSourceConsumerEP.isInUse(sqlDefinition.getId()));
        
        return def2json;
    }
    
    /**
     * Add a data source 
     * @param type The type of data source
     * @param parameters The parameters of the data source to create
     * @return the created data source as JSON object
     * @throws IOException if an error occurred 
     * @throws SAXException if an error occurred 
     * @throws ConfigurationException if an error occurred  
     * @throws ProcessingException if an error occurred  
     */
    @Callable
    public Map<String, Object> addDataSource (String type, Map<String, Object> parameters) throws ProcessingException, ConfigurationException, SAXException, IOException
    {
        String name = (String) parameters.get("name");
        String description = (String) parameters.get("description");
        boolean isPrivate = (boolean) parameters.get("private");
        
        parameters.remove("id");
        parameters.remove("name");
        parameters.remove("description");
        parameters.remove("private");
        parameters.remove("type");
        
        DataSourceDefinition def = null;
        if (type.equals(DataSourceType.SQL.toString()))
        {
            def = _sqlDataSourceManager.add(new I18nizableText(name), new I18nizableText(description), parameters, isPrivate);
        }
        else if (type.equals(DataSourceType.LDAP.toString()))
        {
            def = _ldapDataSourceManager.add(new I18nizableText(name), new I18nizableText(description), parameters, isPrivate);
        }
        else
        {
            throw new IllegalArgumentException("Unable to add data source: unknown data source type '" + type + "'");
        }
        
        return _dataSourceDefinition2Json(def);
    }

    /**
     * Edit a data source 
     * @param type The type of data source
     * @param parameters The parameters of the data source to edit
     * @return the edited data source as JSON object
     * @throws IOException if an error occurred  
     * @throws SAXException if an error occurred  
     * @throws ConfigurationException if an error occurred  
     * @throws ProcessingException if an error occurred  
     */
    @Callable
    public Map<String, Object> editDataSource (String type, Map<String, Object> parameters) throws ProcessingException, ConfigurationException, SAXException, IOException
    {
        String id = (String) parameters.get("id");
        String name = (String) parameters.get("name");
        String description = (String) parameters.get("description");
        boolean isPrivate = (boolean) parameters.get("private");
        
        parameters.remove("id");
        parameters.remove("name");
        parameters.remove("description");
        parameters.remove("private");
        parameters.remove("type");
        
        DataSourceDefinition def = null;
        if (type.equals(DataSourceType.SQL.toString()))
        {
            DataSourceDefinition previousdataSourceDefinition = _sqlDataSourceManager.getDataSourceDefinition(id); 
            
            // Inject the recorded password before overriding the existing data source (saved passwords are not sent)
            String previousPassword = previousdataSourceDefinition.getParameters().get("password");
            if (StringUtils.isNotEmpty(previousPassword))
            {
                parameters.put("password", previousPassword);
            }

            def = _sqlDataSourceManager.edit(id, new I18nizableText(name), new I18nizableText(description), parameters, isPrivate);
        }
        else if (type.equals(DataSourceType.LDAP.toString()))
        {
            DataSourceDefinition previousdataSourceDefinition = _ldapDataSourceManager.getDataSourceDefinition(id);
            
            // Inject the recorded password before overriding the existing data source (saved passwords are not sent)
            String previousPassword = previousdataSourceDefinition.getParameters().get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD);
            if (StringUtils.isNotEmpty(previousPassword))
            {
                parameters.put(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD, previousPassword);
            }
            
            def = _ldapDataSourceManager.edit(id, new I18nizableText(name), new I18nizableText(description), parameters, isPrivate);
        }
        else
        {
            throw new IllegalArgumentException("Unable to edit data source: unknown data source type '" + type + "'");
        }
        
        return _dataSourceDefinition2Json(def);
    }
    
    /**
     * Remove one or several data sources 
     * @param type The type of data source
     * @param ids the ids of the data sources to remove
     * @throws IOException if an error occurred while reading configuration file
     * @throws SAXException if an error occurred while parsing configuration file
     * @throws ConfigurationException if an error occurred while parsing configuration reading file
     * @throws ProcessingException if an error occurred while saving changes
     */
    @Callable
    public void removeDataSource (String type, List<String> ids) throws ConfigurationException, SAXException, IOException, ProcessingException
    {
        if (type.equals(DataSourceType.SQL.toString()))
        {
            _sqlDataSourceManager.delete(ids);
        }
        else if (type.equals(DataSourceType.LDAP.toString()))
        {
            _ldapDataSourceManager.delete(ids);
        }
        else
        {
            throw new IllegalArgumentException("Unable to delete data sources: unknown data source type '" + type + "'");
        }
    }
    
    private Map<String, Object> _dataSourceDefinition2Json (DataSourceDefinition dataSource)
    {
        Map<String, Object> infos = new HashMap<>();
        
        infos.put("id", dataSource.getId());
        infos.put("name", dataSource.getName());
        infos.put("description", dataSource.getDescription());
        infos.put("private", dataSource.isPrivate());
        
        Map<String, String> parameters = dataSource.getParameters();
        for (String paramName : parameters.keySet())
        {
            infos.put(paramName, parameters.get(paramName));
        }
        
        return infos;
    }
}
