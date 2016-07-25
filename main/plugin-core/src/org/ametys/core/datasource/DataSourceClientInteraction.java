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
import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
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
    
    /** The LDAP data source manager */
    private LDAPDataSourceManager _ldapDataSourceManager;
    
    /** The extension for data source clients */
    private DataSourceConsumerExtensionPoint _dataSourceConsumerEP;
    
    /** Component gathering utility method allowing to handle internationalizable text */
    private I18nUtils _i18nUtils;
    
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
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _sqlDataSourceManager = (SQLDataSourceManager) serviceManager.lookup(SQLDataSourceManager.ROLE);
        _ldapDataSourceManager = (LDAPDataSourceManager) serviceManager.lookup(LDAPDataSourceManager.ROLE);
        _dataSourceConsumerEP = (DataSourceConsumerExtensionPoint) serviceManager.lookup(DataSourceConsumerExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
    }
    
    /**
     * Get the existing data sources
     * @param type the data source type. Can be empty or null to get all data sources
     * @param includePrivate true to include private data sources
     * @param includeInternal true to include internal data sources
     * @param includeDefault true to include the default data sources
     * @return the existing data sources
     * @throws Exception if an error occurred
     */
    @Callable
    public List<Map<String, Object>> getDataSources (String type, boolean includePrivate, boolean includeInternal, boolean includeDefault) throws Exception
    {
        List<Map<String, Object>> datasources = new ArrayList<>();
        
        if (StringUtils.isEmpty(type) || type.equals(DataSourceType.SQL.toString()))
        {
            Map<String, DataSourceDefinition> sqlDataSources = _sqlDataSourceManager.getDataSourceDefinitions(includePrivate, includeInternal, includeDefault);
            for (String id : sqlDataSources.keySet())
            {
                datasources.add(getSQLDataSource(id));
            }
        }
        
        if (StringUtils.isEmpty(type) || type.equals(DataSourceType.LDAP.toString()))
        {
            Map<String, DataSourceDefinition> ldapDataSources = _ldapDataSourceManager.getDataSourceDefinitions(includePrivate, includeInternal, includeDefault);
            for (String id : ldapDataSources.keySet())
            {
                datasources.add(getLDAPDataSource(id));
            }
        }
        
        return datasources;
    }
    
    /**
     * Get the existing data sources regardless of their type
     * @param includePrivate true to include private data sources
     * @param includeInternal true to include internal data sources
     * @param includeDefault true to include default data sources
     * @return the existing data sources
     * @throws Exception if an error occurred
     */
    @Callable
    public List<Map<String, Object>> getDataSources (boolean includePrivate, boolean includeInternal, boolean includeDefault) throws Exception
    {
        return getDataSources(null, includePrivate, includeInternal, includeDefault);
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
                getLogger().error("Unable to get data source: unknown data source type '" + type + "'.");
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
        Map<String, Object> def2json = _dataSourceDefinition2Json(DataSourceType.LDAP.toString(), ldapDefinition);
        if (ldapDefinition == null)
        {
            getLogger().error("Unable to find the data source definition for the id '" + id + "'.");
        }
        else
        {
            def2json.put("id", id); // Keep the 'LDAP-default-datasource' id
            def2json.put("type", "LDAP");
            
            // The configuration data source consumer refers to the stored values of the configuration
            // For the default data source, it is "LDAP-default-datasource"
            boolean isInUse = _dataSourceConsumerEP.isInUse(ldapDefinition.getId()) || (ldapDefinition.isDefault() && _dataSourceConsumerEP.isInUse(_ldapDataSourceManager.getDefaultDataSourceId()));
            def2json.put("isInUse", isInUse);
            
            if ((_ldapDataSourceManager.getDataSourcePrefixId() + AbstractDataSourceManager.DEFAULT_DATASOURCE_SUFFIX).equals(id))
            {
                _setDefaultDataSourceName(def2json);
            }
        }
        
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
        
        Map<String, Object> def2json = _dataSourceDefinition2Json(DataSourceType.SQL.toString(), sqlDefinition);
        if (sqlDefinition == null)
        {
            getLogger().error("Unable to find the data source definition for the id '" + id + "'.");
        }
        else
        {
            def2json.put("id", id); // Keep the 'SQL-default-datasource' id
            def2json.put("type", "SQL");
            
            // The configuration data source consumer refers to the stored values of the configuration
            // For the default data source, it is "SQL-default-datasource"
            boolean isInUse = _dataSourceConsumerEP.isInUse(sqlDefinition.getId()) || (sqlDefinition.isDefault() && _dataSourceConsumerEP.isInUse(_sqlDataSourceManager.getDefaultDataSourceId()));
            def2json.put("isInUse", isInUse);
            
            if (_sqlDataSourceManager.getDefaultDataSourceId().equals(id))
            {
                _setDefaultDataSourceName(def2json);
            }
        }
        
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
            throw new IllegalArgumentException("Unable to add data source: unknown data source type '" + type + "'.");
        }
        
        return _dataSourceDefinition2Json(type, def);
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
            if (previousdataSourceDefinition != null)
            {
                // Inject the recorded password before overriding the existing data source (saved passwords are not sent)
                String previousPassword = previousdataSourceDefinition.getParameters().get("password");
                if (parameters.get("password") == null && StringUtils.isNotEmpty(previousPassword))
                {
                    parameters.put("password", previousPassword);
                }
            }
            else
            {
                getLogger().error("The data source of id '" + id + "' was not found. Unable to get the previous password.");
            }

            def = _sqlDataSourceManager.edit(id, new I18nizableText(name), new I18nizableText(description), parameters, isPrivate);
        }
        else if (type.equals(DataSourceType.LDAP.toString()))
        {
            DataSourceDefinition previousdataSourceDefinition = _ldapDataSourceManager.getDataSourceDefinition(id);
            if (previousdataSourceDefinition != null)
            {
                // Inject the recorded password before overriding the existing data source (saved passwords are not sent)
                String previousPassword = previousdataSourceDefinition.getParameters().get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD);
                if (parameters.get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD) == null && StringUtils.isNotEmpty(previousPassword))
                {
                    parameters.put(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD, previousPassword);
                }
            }
            else
            {
                getLogger().error("The data source of id '" + id + "' was not found. Unable to get the previous password.");
            }
            
            def = _ldapDataSourceManager.edit(id, new I18nizableText(name), new I18nizableText(description), parameters, isPrivate);
        }
        else
        {
            throw new IllegalArgumentException("Unable to edit data source: unknown data source type '" + type + "'.");
        }
        
        return _dataSourceDefinition2Json(type, def);
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
            throw new IllegalArgumentException("Unable to delete data sources: unknown data source type '" + type + "'.");
        }
    }
    
    /**
     * Set the data source of the given id as the default data source for the given type
     * @param type the type of the data source
     * @param id the id of the data source
     * @return the {@link DataSourceDefinition} of data source set as default in JSON
     */
    @Callable
    public Map<String, Object> setDefaultDataSource(String type, String id)
    {
        DataSourceDefinition def = null;
        if (type.equals(DataSourceType.SQL.toString()))
        {
            def = _sqlDataSourceManager.setDefaultDataSource(id);
        }
        else if (type.equals(DataSourceType.LDAP.toString()))
        {
            def = _ldapDataSourceManager.setDefaultDataSource(id);
        }
        else
        {
            throw new IllegalArgumentException("Unable set to default the data source: unknown data source type '" + type + "'.");
        }
        
        return _dataSourceDefinition2Json(type, def);
    }
    
    private void _setDefaultDataSourceName(Map<String, Object> dataSourceAsJSON)
    {
        String defaultDataSourceName = _i18nUtils.translate(new I18nizableText("plugin.core", "PLUGINS_CORE_DEFAULT_DATASOURCE_NAME_PREFIX"));
        defaultDataSourceName += _i18nUtils.translate((I18nizableText) dataSourceAsJSON.get("name"));
        defaultDataSourceName += _i18nUtils.translate(new I18nizableText("plugin.core", "PLUGINS_CORE_DEFAULT_DATASOURCE_NAME_SUFFIX"));
        
        dataSourceAsJSON.put("name", defaultDataSourceName);
    }
    
    private Map<String, Object> _dataSourceDefinition2Json (String type, DataSourceDefinition dataSourceDef)
    {
        Map<String, Object> infos = new HashMap<>();
        if (dataSourceDef != null)
        {
            infos.put("id", dataSourceDef.getId());
            infos.put("name", dataSourceDef.getName());
            infos.put("description", dataSourceDef.getDescription());
            infos.put("private", dataSourceDef.isPrivate());
            infos.put("isDefault", dataSourceDef.isDefault());
            infos.put("isInUse", _dataSourceConsumerEP.isInUse(dataSourceDef.getId()) || (dataSourceDef.isDefault() && _dataSourceConsumerEP.isInUse(_ldapDataSourceManager.getDefaultDataSourceId())));

            Map<String, String> parameters = dataSourceDef.getParameters();
            for (String paramName : parameters.keySet())
            {
                infos.put(paramName, parameters.get(paramName));
            }
            
            // Is the data source valid ?
            infos.put("isValid", _isValid(type, parameters));
        }
        
        return infos;
    }

    private boolean _isValid(String type, Map<String, String> parameters)
    {
        boolean isValid = true;
        if (type.equals(DataSourceType.SQL.toString()))
        {
            try
            {
                _sqlDataSourceManager.checkParameters(parameters);
            }
            catch (ParameterCheckerTestFailureException e)
            {
                isValid = false;
            }
        }
        else if (type.equals(DataSourceType.LDAP.toString()))
        {
            try
            {
                _ldapDataSourceManager.checkParameters(parameters);
            }
            catch (ParameterCheckerTestFailureException e)
            {
                isValid = false;
            }
        }
        else
        {
            throw new IllegalArgumentException("Unable to convert a data source definition to JSON : unknown data source type '" + type + "'.");
        }
        
        return isValid;
    }
}
