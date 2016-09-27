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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import org.ametys.plugins.core.impl.checker.LDAPConnectionChecker;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.util.AmetysHomeHelper;

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
    
    /** The id of the internal DataSource */
    public static final String LDAP_DATASOURCE_PREFIX = "LDAP-";
    
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
        
        return new File(AmetysHomeHelper.getAmetysHomeConfig(), "datasources-ldap.xml");
    }
    
    @Override
    protected String getDataSourcePrefixId()
    {
        return LDAP_DATASOURCE_PREFIX;
    }
    
    @Override
    public void checkParameters(Map<String, String> rawParameters) throws ParameterCheckerTestFailureException
    {
        // Order the parameters
        List<String> values = new ArrayList<> ();
        values.add(rawParameters.get(PARAM_BASE_URL));
        values.add(rawParameters.get(PARAM_AUTHENTICATION_METHOD));
        values.add(rawParameters.get(PARAM_ADMIN_DN));
        values.add(rawParameters.get(PARAM_ADMIN_PASSWORD));
        values.add(rawParameters.get(PARAM_USE_SSL));
        values.add(rawParameters.get(PARAM_FOLLOW_REFERRALS));
        values.add(rawParameters.get(PARAM_BASE_DN));
        
        ParameterChecker paramChecker = new LDAPConnectionChecker();
        paramChecker.check(values);
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
    
    @Override
    protected void internalSetDefaultDataSource()
    {
        // Arbitrarily set the LDAP default data source
        if (MapUtils.isNotEmpty(_dataSourcesDef))
        {
            DataSourceDefinition defaultDataSourceDef = _dataSourcesDef.values().iterator().next();
            defaultDataSourceDef.setDefault(true);
            _dataSourcesDef.put(defaultDataSourceDef.getId(), defaultDataSourceDef);
            
            saveConfiguration();
            editDataSource(defaultDataSourceDef);
            
        }
    }
}
