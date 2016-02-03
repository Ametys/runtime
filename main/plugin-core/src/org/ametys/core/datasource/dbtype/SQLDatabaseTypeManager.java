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
            databaseType.put("value", staticSQLDatabaseType.getDriver());
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
