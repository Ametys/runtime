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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.cocoon.ActionResultGenerator;
import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.core.datasource.DataSourceClientInteraction.DataSourceType;
import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.datasource.dbtype.SQLDatabaseTypeExtensionPoint;
import org.ametys.core.datasource.dbtype.SQLDatabaseTypeManager;
import org.ametys.core.util.I18nUtils;
import org.ametys.core.util.JSONUtils;
import org.ametys.plugins.core.impl.datasource.StaticSQLDatabaseType;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.parameter.ParameterHelper;

/**
 * This action checks the validity of a data source's parameters
 */
public class CheckDataSourceAction extends ServiceableAction
{
    /** The id of the SQL data source checker */
    private static final String __SQL_DATASOURCE_CHECKER_ID = "sql-connection-checker-datasource";
    
    /** The manager for SQL data source */
    private SQLDataSourceManager _sqlDataSourceManager;
    
    /** The manager for SQL data source */
    private LDAPDataSourceManager _ldapDataSourceManager;
    
    /** The manager for SQL database types */
    private SQLDatabaseTypeManager _sqlDatabaseTypeManager;
    
    /** The extension point for SQL database types */
    private SQLDatabaseTypeExtensionPoint _sqlDatabaseTypeExtensionPoint;
    
    /** Utility methods helping the management of internationalizable text */
    private I18nUtils _i18nUtils;
    
    /** Helper component gathering utility methods for the management of JSON entities */
    private JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _ldapDataSourceManager = (LDAPDataSourceManager) serviceManager.lookup(LDAPDataSourceManager.ROLE);
        _sqlDataSourceManager = (SQLDataSourceManager) serviceManager.lookup(SQLDataSourceManager.ROLE);
        _sqlDatabaseTypeManager = (SQLDatabaseTypeManager) serviceManager.lookup(SQLDatabaseTypeManager.ROLE);
        _sqlDatabaseTypeExtensionPoint = (SQLDatabaseTypeExtensionPoint) serviceManager.lookup(SQLDatabaseTypeExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
        _jsonUtils = (JSONUtils) serviceManager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, String> result = new HashMap<> ();
        Request request = ObjectModelHelper.getRequest(objectModel);

        String fieldCheckersInfoJSON = request.getParameter("fieldCheckersInfo");
        Map<String, Object> fieldCheckersInfo = _jsonUtils.convertJsonToMap(fieldCheckersInfoJSON);
        
        Iterator<String> fieldCheckersIds = fieldCheckersInfo.keySet().iterator();
        
        while (fieldCheckersIds.hasNext())
        {
            String fieldCheckerId = fieldCheckersIds.next();
            
            @SuppressWarnings("unchecked")
            Map<String, List<String>> fieldCheckerInfo = (Map<String, List<String>>) fieldCheckersInfo.get(fieldCheckerId);
            List<String> values = fieldCheckerInfo.get("rawTestValues");
            
            String type = fieldCheckerId.equals(__SQL_DATASOURCE_CHECKER_ID) ? "SQL" : "LDAP";
            
            DataSourceType dsType = DataSourceType.valueOf(type);
            try 
            {
                switch (dsType)
                {
                    case SQL:
                        _checkSQLParameters(values);
                        break;
                        
                    case LDAP:
                        _checkLDAPParameters(values);
                        break;
                    
                    default:
                        throw new IllegalArgumentException("Unknow data source of type '" + type + "'. Unable to check data source parameters.");
                }
            }
            catch (Throwable t)
            {
                getLogger().error("Data source test failed: \n" + t.getMessage(), t);
                String msg = t.getMessage() != null ? t.getMessage() : "Unknown error";
                
                // We know we only have one parameter checker here
                result.put(fieldCheckerId, msg);
            }
        }
            
        request.setAttribute(ActionResultGenerator.MAP_REQUEST_ATTR, result);
        return result;
    }
    
    private void _checkSQLParameters (List<String> values) throws ParameterCheckerTestFailureException
    {
        Map<String, String> sqlParameters = new HashMap<> ();
        
        String driverExtensionId = values.get(1);
        StaticSQLDatabaseType sqlDatabaseType = (StaticSQLDatabaseType) _sqlDatabaseTypeExtensionPoint.getExtension(driverExtensionId);
        String driver = sqlDatabaseType.getDriver();
        
        sqlParameters.put("driver", driverExtensionId);
        if (StringUtils.isNotEmpty(driver))
        {
            I18nizableText driverNotFoundMessageKey = _sqlDatabaseTypeManager.getClassNotFoundMessage(driver);
            sqlParameters.put("driverNotFoundMessage", _i18nUtils.translate(driverNotFoundMessageKey));
        }
        
        sqlParameters.put("url", values.get(2));
        sqlParameters.put("user", values.get(3));
        
        String password = values.get(4);
        if (password == null)
        {
            // Get password from the registered data source if it exists
            String dataSourceId = values.get(0);
            DataSourceDefinition dataSourceDefinition = _sqlDataSourceManager.getDataSourceDefinition(dataSourceId);
            if (dataSourceDefinition != null)
            {
                sqlParameters.put("password", dataSourceDefinition.getParameters().get("password"));
            }
        }
        else
        {
            sqlParameters.put("password", password);
        }
        
        _sqlDataSourceManager.checkParameters(sqlParameters);
    }
    
    private void _checkLDAPParameters (List<String> values) throws ParameterCheckerTestFailureException
    {
        Map<String, String> ldapParameters = new HashMap<> ();
        
        ldapParameters.put(LDAPDataSourceManager.PARAM_BASE_URL, values.get(1));
        ldapParameters.put(LDAPDataSourceManager.PARAM_BASE_DN, values.get(2));
        ldapParameters.put(LDAPDataSourceManager.PARAM_USE_SSL, ParameterHelper.valueToString(values.get(3)));
        
        ldapParameters.put(LDAPDataSourceManager.PARAM_FOLLOW_REFERRALS, ParameterHelper.valueToString(values.get(4)));
        
        String authenticationMethod = values.get(5);
        ldapParameters.put(LDAPDataSourceManager.PARAM_AUTHENTICATION_METHOD, authenticationMethod);
        
        ldapParameters.put(LDAPDataSourceManager.PARAM_ADMIN_DN, values.get(6));
        
        // A null password + an authentication method => the password is recorded
        String adminPassword = values.get(7);
        if (adminPassword == null && !authenticationMethod.equals("none"))
        {
            String dataSourceId = values.get(0);
            DataSourceDefinition dataSourceDefinition = _ldapDataSourceManager.getDataSourceDefinition(dataSourceId);
            if (dataSourceDefinition != null)
            {
                ldapParameters.put(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD, dataSourceDefinition.getParameters().get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD));
            }
        }
        else
        {
            ldapParameters.put(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD, adminPassword);
        }
        
        _ldapDataSourceManager.checkParameters(ldapParameters);
    }
}
