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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.DefaultValidator;
import org.ametys.runtime.parameter.Errors;

/**
 * This validator validates that a type of SQL data sources is an authorized database type  
 *
 */
public class SQLDatabaseTypeValidator extends DefaultValidator
{
    private Set<String> _allowedDbTypes;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _allowedDbTypes = new HashSet<>();
        
        Configuration validatorConfig = configuration.getChild("validation").getChild("custom-validator");
        
        _isMandatory = validatorConfig.getChild("mandatory", false) != null;

        String regexp = validatorConfig.getChild("regexp").getValue(null);
        if (regexp != null)
        {
            _regexp = Pattern.compile(regexp);
        }
        
        Configuration textConfig = validatorConfig.getChild("invalidText", false);
        if (textConfig != null)
        {
            _invalidText = I18nizableText.parseI18nizableText(textConfig, "plugin." + _pluginName);
        }
        
        
        Configuration dbtypesConfig = validatorConfig.getChild("allowed-dbtypes", false);
        if (dbtypesConfig != null)
        {
            String[] dbtypes = dbtypesConfig.getValue().split(",");
            for (String dbtype : dbtypes)
            {
                CollectionUtils.addIgnoreNull(_allowedDbTypes, StringUtils.trimToNull(dbtype));
            }
        }
    }
    
    /**
     * Validates a single value.
     * @param value the value to validate (can be <code>null</code>).
     * @param errors the structure to populate if the validation failed.
     */
    @Override
    protected void validateSingleValue (Object value, Errors errors)
    {
        super.validateSingleValue(value, errors);
        
        if (_allowedDbTypes.size() > 0 && value != null && value.toString().length() > 0)
        {
            String dataSourceId = value.toString();
            _validateDataSource(dataSourceId, errors);
        }
    }
    
    @Override
    protected void validateArrayValues (Object[] values, Errors errors)
    {
        if (_allowedDbTypes.size() > 0 && values != null && values.length > 0)
        {
            for (Object value : values)
            {
                String dataSourceId = value.toString();
                _validateDataSource(dataSourceId, errors);
            }
        }
    }
    
    private void _validateDataSource (String dataSourceId, Errors errors)
    {
        if (!_isValidDatasource(dataSourceId))
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The type of datasource of id '" + dataSourceId + "' is not an authorized database type.");
            }
            
            errors.addError(new I18nizableText("plugin.core", "PLUGINS_CORE_SQL_DATASOURCETYPE_VALIDATOR_FAILED"));
        }
    }
    
    private boolean _isValidDatasource (String dataSourceId)
    {
        // FIXME Add a static method to get all data source definition in safe-mode ?
        
        // Use static method because the SQLDataSourceManager may be not initialized yet
        Map<String, DataSourceDefinition> dsDefinitions = AbstractDataSourceManager.readDataSourceDefinition(SQLDataSourceManager.getStaticFileConfiguration());
        
        // Add the default datasource
        boolean findDefault = false;
        for (DataSourceDefinition dsDefinition : dsDefinitions.values())
        {
            if (dsDefinition.isDefault())
            {
                dsDefinitions.put(SQLDataSourceManager.SQL_DATASOURCE_PREFIX + AbstractDataSourceManager.DEFAULT_DATASOURCE_SUFFIX, dsDefinition.clone());
                findDefault = true;
                break;
            }
        }
        
        // Add the internal data source
        DataSourceDefinition internalDsDefinition = SQLDataSourceManager.getInternalDataSourceDefinition();
        dsDefinitions.put(internalDsDefinition.getId(), internalDsDefinition);
        
        if (!findDefault)
        {
            // The internal db is the default data source
            dsDefinitions.put(SQLDataSourceManager.SQL_DATASOURCE_PREFIX + AbstractDataSourceManager.DEFAULT_DATASOURCE_SUFFIX, internalDsDefinition.clone());
        }
        
        if (dsDefinitions.containsKey(dataSourceId))
        {
            String dbtype = dsDefinitions.get(dataSourceId).getParameters().get(SQLDataSourceManager.PARAM_DATABASE_TYPE);
            return _allowedDbTypes.contains(dbtype);
        }
        
        return false;
    }
    
    @Override
    public void saxConfiguration(ContentHandler handler) throws SAXException
    {
        super.saxConfiguration(handler);
        
        for (String dbtype : _allowedDbTypes)
        {
            XMLUtils.createElement(handler, "allowedDbTypes", dbtype);
        }
    }

    @Override
    public Map<String, Object> toJson()
    {
        Map<String, Object> jsonObject = super.toJson();
        
        jsonObject.put("allowedDbTypes", _allowedDbTypes);
        
        return jsonObject;
    }

    @Override
    public Map<String, Object> getConfiguration()
    {
        Map<String, Object> configuration = super.getConfiguration();
        configuration.putAll(toJson());
        return configuration;
    }

}
