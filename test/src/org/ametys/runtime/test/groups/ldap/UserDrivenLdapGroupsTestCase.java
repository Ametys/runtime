/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.test.groups.ldap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryFactory;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.plugins.core.impl.group.directory.ldap.UserDrivenLdapGroupDirectory;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.test.Init;

/**
 * Tests the LdapGroupsManager
 */
public class UserDrivenLdapGroupsTestCase extends AbstractLdapGroupsTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        _startApplication("test/environments/runtimes/runtime9.xml", "test/environments/configs/config4.xml", "test/environments/datasources/datasource-mysql.xml", "test/environments/datasources/datasource-ldap.xml", "test/environments/webapp1");
        _groupDirectory = _createUserDrivenLdapGroupDirectory();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _deletePopulation();
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test the choosen implementation
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // DEFAULT IMPL
        assertTrue(_groupDirectory instanceof UserDrivenLdapGroupDirectory);

        // MODIFIABLE
        assertFalse(_groupDirectory instanceof ModifiableGroupDirectory);
    }
    
    private GroupDirectory _createUserDrivenLdapGroupDirectory() throws Exception
    {
        // We need a LDAP User Directory where to retrieve the users
        String populationId = _createPopulation();
        
        // Create the Group Directory
        String modelId = "org.ametys.plugins.core.group.directory.UsersDrivenLdap";
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("runtime.groups.ldap.datasource", "LDAP-test-users");
        parameters.put("runtime.groups.ldap.population", populationId);
        parameters.put("runtime.groups.ldap.groupDN", "ou=groups");
        parameters.put("runtime.groups.ldap.filter", "(objectclass=organizationalUnit)");
        parameters.put("runtime.groups.ldap.scope", "one");
        parameters.put("runtime.groups.ldap.id", "ou");
        parameters.put("runtime.groups.ldap.description", "description");
        parameters.put("runtime.groups.ldap.memberof", "memberOf");
        parameters.put("runtime.users.ldap.peopleDN", "ou=people");
        parameters.put("runtime.users.ldap.baseFilter", "(objectclass=inetOrgPerson)");
        parameters.put("runtime.users.ldap.scope", "sub");
        parameters.put("runtime.users.ldap.loginAttr", "uid");
        return ((GroupDirectoryFactory) Init.getPluginServiceManager().lookup(GroupDirectoryFactory.ROLE)).createGroupDirectory("directory", new I18nizableText("User Driven LDAP"), modelId, parameters);
    }
    
    private String _createPopulation() throws Exception
    {
        Map<String, String> userDirectoryParameters = new LinkedHashMap<>();
        userDirectoryParameters.put("udModelId", "org.ametys.plugins.core.user.directory.Ldap");
        userDirectoryParameters.put("runtime.users.ldap.datasource", "LDAP-test-users");
        userDirectoryParameters.put("runtime.users.ldap.peopleDN", "ou=people");
        userDirectoryParameters.put("runtime.users.ldap.baseFilter", "(objectclass=inetOrgPerson)");
        userDirectoryParameters.put("runtime.users.ldap.scope", "sub");
        userDirectoryParameters.put("runtime.users.ldap.loginAttr", "uid");
        userDirectoryParameters.put("runtime.users.ldap.firstnameAttr", "givenName");
        userDirectoryParameters.put("runtime.users.ldap.lastnameAttr", "sn");
        userDirectoryParameters.put("runtime.users.ldap.emailAttr", "mail");
        userDirectoryParameters.put("runtime.users.ldap.emailMandatory", "false");
        userDirectoryParameters.put("runtime.users.ldap.serverSideSorting", "true");
        
        Map<String, String> credentialProviderParameters = new LinkedHashMap<>();
        credentialProviderParameters.put("cpModelId", "org.ametys.core.authentication.Defined");
        credentialProviderParameters.put("runtime.authentication.defined.user", "anonymous");
        
        return ((UserPopulationDAO) Init.getPluginServiceManager().lookup(UserPopulationDAO.ROLE)).add("ldap_population", "LDAP Population", Collections.singletonList(userDirectoryParameters), Collections.singletonList(credentialProviderParameters));
    }
    
    private void _deletePopulation() throws Exception
    {
        ((UserPopulationDAO) Init.getPluginServiceManager().lookup(UserPopulationDAO.ROLE)).remove("ldap_population");
    }
}
