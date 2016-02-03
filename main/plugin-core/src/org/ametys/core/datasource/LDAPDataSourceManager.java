/*
 *  Copyright 2015 Anyware Services
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

import java.io.File;
import java.util.Map;

import org.ametys.plugins.core.impl.checker.LDAPConnectionChecker;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * This component handles SQL data sources. 
 * It is associated with the configuration file $AMETYS_HOME/config/datasources-ldap.xml 
 */
public class LDAPDataSourceManager extends AbstractDataSourceManager
{
    /** Avalon Role */
    public static final String ROLE = LDAPDataSourceManager.class.getName();
    
    /** LDAP parameter's name for base URL */
    public static final String PARAM_BASE_URL = "baseURL";
    /** LDAP parameter's name for base DN */
    public static final String PARAM_BASE_DN = "baseDN";
    /** LDAP parameter's name for use SSL */
    public static final String PARAM_USE_SSL = "useSSL";
    /** LDAP parameter's name for alias dereferencing */
    public static final String PARAM_ALIAS_DEREFERENCING = "aliasDereferencing";
    /** LDAP parameter's name for follow referrals property */
    public static final String PARAM_FOLLOW_REFERRALS = "followReferrals";
    /** LDAP parameter's name for authentication method */
    public static final String PARAM_AUTHENTICATION_METHOD = "authenticationMethod";
    /** LDAP parameter's name for administrator DN */
    public static final String PARAM_ADMIN_DN = "adminDN";
    /** LDAP parameter's name for administration password */
    public static final String PARAM_ADMIN_PASSWORD = "adminPassword";
    
    private static String __filename;
    
    /**
     * Set the config filename. Only use for tests.
     * @param filename Name with path of the config file
     */
    public static void setFilename(String filename)
    {
        __filename = filename;
    }
    
    @Override
    public File getFileConfiguration()
    {
        if (__filename != null)
        {
            return new File(__filename);
        }
        
        return new File(RuntimeConfig.getInstance().getAmetysHome(), RuntimeConfig.AMETYS_HOME_CONFIG_DIR + File.separator + "datasources-ldap.xml");
    }
    
    @Override
    protected String getDataSourcePrefixId()
    {
        return "LDAP-";
    }
    
    @Override
    public void checkParameters(DataSourceDefinition dataSource) throws ParameterCheckerTestFailureException
    {
        checkParameters(dataSource.getParameters());
    }
    
    @Override
    public void checkParameters(Map<String, String> rawParameters) throws ParameterCheckerTestFailureException
    {
        ParameterChecker paramChecker = new LDAPConnectionChecker();
        paramChecker.check(rawParameters);
    }
    
    @Override
    protected void createDataSource(DataSourceDefinition dataSource)
    {
        // Empty
    }
    
    @Override
    protected void deleteDataSource(DataSourceDefinition dataSource)
    {
        // Empty
    }

    @Override
    protected void editDataSource(DataSourceDefinition dataSource)
    {
        // Empty
    }
}
