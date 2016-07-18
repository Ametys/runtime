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
package org.ametys.core.datasource.dbtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.ui.Callable;
import org.ametys.core.util.I18nUtils;
import org.ametys.plugins.core.impl.datasource.StaticSQLDatabaseType;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * Manager handling the handled SQL database types
 */
public class SQLDatabaseTypeManager implements Component, Serviceable
{
    /** Avalon Role */
    public static final String ROLE = SQLDatabaseTypeManager.class.getName();
    
    /** The extension point for SQL database types */
    private SQLDatabaseTypeExtensionPoint _sqlDatabaseTypeExtensionPoint;
    
    /** Component containing i18n utility methods */
    private I18nUtils _i18nUtils;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _sqlDatabaseTypeExtensionPoint = (SQLDatabaseTypeExtensionPoint) serviceManager.lookup(SQLDatabaseTypeExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
    }
    
    /**
     * Retrieve the available sql drivers and the associated templates
     * @return the available drivers in JSON 
     */
    @Callable
    public Map<String, Object> getSQLDatabaseTypes()
    {
        Map<String, Object> result = new HashMap<> ();
        List<Map<String, Object>> databaseTypes = new ArrayList<>();
        
        Map<Object, I18nizableText> sqlDatabaseTypes = _sqlDatabaseTypeExtensionPoint.getSQLDatabaseTypes();
        for (Object extensionId : sqlDatabaseTypes.keySet())
        {
            Map<String, Object> databaseType = new HashMap<> ();
            StaticSQLDatabaseType staticSQLDatabaseType = (StaticSQLDatabaseType) _sqlDatabaseTypeExtensionPoint.getExtension((String) extensionId);
            
            databaseType.put("label", _i18nUtils.translate(staticSQLDatabaseType.getLabel()));
            databaseType.put("value", extensionId);
            databaseType.put("template", staticSQLDatabaseType.getTemplate());
            
            databaseTypes.add(databaseType);
        }
        
        result.put("databaseTypes", databaseTypes);
        return result;
    }
    
    /**
     * Get the class not found message corresponding to a driver
     * @param driver the driver
     * @return the message to use in case the class of the driver was not found
     */
    public I18nizableText getClassNotFoundMessage(String driver)
    {
        Map<Object, I18nizableText> sqlDatabaseTypes = _sqlDatabaseTypeExtensionPoint.getSQLDatabaseTypes();
        for (Object extensionId : sqlDatabaseTypes.keySet())
        {
            StaticSQLDatabaseType staticSQLDatabaseType = (StaticSQLDatabaseType) _sqlDatabaseTypeExtensionPoint.getExtension((String) extensionId);
            if (staticSQLDatabaseType.getDriver().equals(driver))
            {
                return staticSQLDatabaseType.getDriverNotFoundMessage();
            }
        }
        
        return null;
    }
}
