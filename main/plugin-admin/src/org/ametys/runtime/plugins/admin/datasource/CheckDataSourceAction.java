package org.ametys.runtime.plugins.admin.datasource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.core.cocoon.ActionResultGenerator;
import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.core.datasource.DataSourceDAO.DataSourceType;
import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.datasource.dbtype.SQLDatabaseTypeManager;
import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * This action checks the validity of a data source's parameters
 */
public class CheckDataSourceAction extends ServiceableAction
{
    /** The manager for SQL data source */
    private SQLDataSourceManager _sqlDataSourceManager;
    
    /** The manager for SQL data source */
    private LDAPDataSourceManager _ldapDataSourceManager;
    
    /** The manager for SQL database types */
    private SQLDatabaseTypeManager _sqlDatabaseTypeManager;
    
    /** Utility methods helping the management of internationalizable text */
    private I18nUtils _i18nUtils;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _ldapDataSourceManager = (LDAPDataSourceManager) serviceManager.lookup(LDAPDataSourceManager.ROLE);
        _sqlDataSourceManager = (SQLDataSourceManager) serviceManager.lookup(SQLDataSourceManager.ROLE);
        _sqlDatabaseTypeManager = (SQLDatabaseTypeManager) serviceManager.lookup(SQLDatabaseTypeManager.ROLE);
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, String> result = new HashMap<> ();
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        String type = request.getParameter("type");
        DataSourceType dsType = DataSourceType.valueOf(type);
        
        try 
        {
            switch (dsType)
            {
                case SQL:
                    _checkSQLParameters (request);
                    break;
                    
                case LDAP:
                    _checkLDAPParameters(request);
                    break;
                
                default:
                    throw new IllegalArgumentException("Unknow data source of type'" + type + "'. Unable to check data source parameters.");
            }
        }
        catch (Throwable t)
        {
            getLogger().error("Data source test failed: \n" + t.getMessage(), t);
            String msg = t.getMessage() != null ? t.getMessage() : "Unknown error";
            result.put(request.getParameter("paramCheckersIds"), msg);
        }
        
        request.setAttribute(ActionResultGenerator.MAP_REQUEST_ATTR, result);
        return result;
    }
    
    private void _checkSQLParameters (Request request) throws ParameterCheckerTestFailureException, ConfigurationException, SAXException, IOException
    {
        Map<String, String> sqlParameters = new HashMap<> ();
        
        String driver = request.getParameter("driver");
        if (StringUtils.isNotEmpty(driver))
        {
            I18nizableText driverNotFoundMessageKey = _sqlDatabaseTypeManager.getClassNotFoundMessage(driver);
            sqlParameters.put("driverNotFoundMessage", _i18nUtils.translate(driverNotFoundMessageKey));
        }
        
        sqlParameters.put("url", request.getParameter("url"));
        sqlParameters.put("driver", request.getParameter("driver"));
        sqlParameters.put("user", request.getParameter("user"));
        
        String password = request.getParameter("password");
        if (password == null)
        {
            // Get password from registered data source if exists
            String dataSourceId = request.getParameter("id");
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
    
    private void _checkLDAPParameters (Request request) throws ParameterCheckerTestFailureException, ConfigurationException, SAXException, IOException
    {
        Map<String, String> ldapParameters = new HashMap<> ();
        
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements())
        {
            String paramName = (String) parameterNames.nextElement();
            ldapParameters.put(paramName, request.getParameter(paramName));
        }
        
        ldapParameters.put(LDAPDataSourceManager.PARAM_BASE_URL, request.getParameter(LDAPDataSourceManager.PARAM_BASE_URL));
        ldapParameters.put(LDAPDataSourceManager.PARAM_BASE_DN, request.getParameter(LDAPDataSourceManager.PARAM_BASE_DN));
        ldapParameters.put(LDAPDataSourceManager.PARAM_USE_SSL, request.getParameter(LDAPDataSourceManager.PARAM_USE_SSL));
        ldapParameters.put(LDAPDataSourceManager.PARAM_FOLLOW_REFERRALS, request.getParameter(LDAPDataSourceManager.PARAM_FOLLOW_REFERRALS));
        
        String authenticationMethod = request.getParameter(LDAPDataSourceManager.PARAM_AUTHENTICATION_METHOD);
        ldapParameters.put(LDAPDataSourceManager.PARAM_AUTHENTICATION_METHOD, authenticationMethod);
        ldapParameters.put(LDAPDataSourceManager.PARAM_ADMIN_DN, request.getParameter(LDAPDataSourceManager.PARAM_ADMIN_DN));
        
        String adminPassword = request.getParameter(LDAPDataSourceManager.PARAM_ADMIN_DN);
        
        // A null password + an authentication method => the password is recorded
        if (adminPassword == null && !authenticationMethod.equals("none"))
        {
            String dataSourceId = request.getParameter("id");
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
