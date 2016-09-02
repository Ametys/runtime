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
import org.ametys.plugins.core.impl.group.directory.ldap.GroupDrivenLdapGroupDirectory;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.test.Init;

/**
 * Tests the LdapGroupsManager
 */
public class GroupDrivenLdapGroupsTestCase extends AbstractLdapGroupsTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _startApplication("test/environments/runtimes/runtime8.xml", "test/environments/configs/config3.xml", "test/environments/datasources/datasource-ldap.xml", "test/environments/webapp1");
        
        _groupDirectory = _createGroupDrivenLdapGroupDirectory();
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
        assertTrue(_groupDirectory instanceof GroupDrivenLdapGroupDirectory);

        // MODIFIABLE
        assertFalse(_groupDirectory instanceof ModifiableGroupDirectory);
    }
    
    private GroupDirectory _createGroupDrivenLdapGroupDirectory() throws Exception
    {
        // We need a LDAP User Directory where to retrieve the users
        String populationId = _createPopulation();
        
        // Create the Group Directory
        String modelId = "org.ametys.plugins.core.group.directory.GroupDrivenLdap";
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("runtime.groups.ldap.datasource", "LDAP-test-groups");
        parameters.put("runtime.groups.ldap.population", populationId);
        parameters.put("runtime.groups.ldap.groupDN", "ou=groups");
        parameters.put("runtime.groups.ldap.filter", "(objectclass=groupOfNames)");
        parameters.put("runtime.groups.ldap.scope", "sub");
        parameters.put("runtime.groups.ldap.id", "cn");
        parameters.put("runtime.groups.ldap.description", "description");
        parameters.put("runtime.groups.ldap.member", "member");
        parameters.put("runtime.users.ldap.peopleDN", "ou=people");
        parameters.put("runtime.users.ldap.loginAttr", "uid");
        return ((GroupDirectoryFactory) Init.getPluginServiceManager().lookup(GroupDirectoryFactory.ROLE)).createGroupDirectory("directory", new I18nizableText("Group Driven LDAP"), modelId, parameters);
    }
    
    private String _createPopulation() throws Exception
    {
        Map<String, String> userDirectoryParameters = new LinkedHashMap<>();
        String udModelId = "org.ametys.plugins.core.user.directory.Ldap";
        userDirectoryParameters.put("udModelId", udModelId);
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.datasource", "LDAP-test-users");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.peopleDN", "ou=people");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.baseFilter", "(objectclass=inetOrgPerson)");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.scope", "sub");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.loginAttr", "uid");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.firstnameAttr", "givenName");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.lastnameAttr", "sn");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.emailAttr", "mail");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.emailMandatory", "false");
        userDirectoryParameters.put(udModelId + "$" + "runtime.users.ldap.serverSideSorting", "true");
        
        Map<String, String> credentialProviderParameters = new LinkedHashMap<>();
        String cpModelId = "org.ametys.core.authentication.Defined";
        credentialProviderParameters.put("cpModelId", cpModelId);
        credentialProviderParameters.put(cpModelId + "$" + "runtime.authentication.defined.user", "anonymous");
        
        return ((UserPopulationDAO) Init.getPluginServiceManager().lookup(UserPopulationDAO.ROLE)).add("ldap_population", "LDAP Population", Collections.singletonList(userDirectoryParameters), Collections.singletonList(credentialProviderParameters));
    }
    
    private void _deletePopulation() throws Exception
    {
        ((UserPopulationDAO) Init.getPluginServiceManager().lookup(UserPopulationDAO.ROLE)).remove("ldap_population");
    }
}
