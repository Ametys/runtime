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
package org.ametys.runtime.test.users.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugins.core.user.ldap.LdapUsersManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

/**
 * Tests the LdapUsersManager
 */
public class LdapUsersTestCase extends AbstractRuntimeTestCase
{
    /** the user manager */
    protected UsersManager _usersManager;
    
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime8.xml");
        Config.setFilename("test/environments/configs/config3.xml");
        
        _startCocoon("test/environments/webapp1");

        _usersManager = (UsersManager) Init.getPluginServiceManager().lookup(UsersManager.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test the getting of users on mysql
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // JDBC IMPL
        assertTrue(_usersManager instanceof LdapUsersManager);

        // NOT MODIFIABLE
        assertFalse(_usersManager instanceof ModifiableUsersManager);
        
        // NOT CREDENTIAL AWARE
        assertFalse(_usersManager instanceof CredentialsAwareUsersManager);
    }
    
    /**
     * Test a filled db
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        User user = null;
        Collection<User> users = null;

        // Get unexisting user
        user = _usersManager.getUser("foo");
        assertNull(user);

        // Get existing user
        user = _usersManager.getUser("user1");
        assertNotNull(user);
        assertEquals(user.getName(), "user1");
        assertEquals(user.getFullName(), "User1 USER1");
        assertEquals(user.getEmail(), "user1@ametys.org");

        // Get users
        users = _usersManager.getUsers();
        assertEquals(10, users.size());
        assertTrue(users.contains(user));
        
        // SAX USERS
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler;
        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        Node testNode;

        // Sax none
        handler = dom.createDOMHandler();
        handler.startDocument();
        _usersManager.toSAX(handler, 0, 0, new HashMap<String, String>());
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(0.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));

        // Sax all
        handler = dom.createDOMHandler();
        handler.startDocument();
        _usersManager.toSAX(handler, -1, 0, new HashMap<String, String>());
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(11.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(10.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        assertEquals(0.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[count(@*) != 1])"));
        assertEquals(0.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[count(*) != 3])"));
        
        testNode = xpath.selectSingleNode(handler.getDocument(), "/users/user[@login = 'user1']");
        assertNotNull(testNode);
        assertEquals("USER1", xpath.evaluateAsString(testNode, "lastname"));
        assertEquals("User1", xpath.evaluateAsString(testNode, "firstname"));
        assertEquals("user1@ametys.org", xpath.evaluateAsString(testNode, "email"));
        
        testNode = xpath.selectSingleNode(handler.getDocument(), "/users/user[@login = 'user10']");
        assertNotNull(testNode);
        assertEquals("USER10", xpath.evaluateAsString(testNode, "lastname"));
        assertEquals("User10", xpath.evaluateAsString(testNode, "firstname"));
        assertEquals("user10@ametys.org", xpath.evaluateAsString(testNode, "email"));
        
        assertEquals("10", xpath.evaluateAsString(handler.getDocument(), "/users/total"));

        // Sax any
        handler = dom.createDOMHandler();
        handler.startDocument();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("pattern", "user1");
        _usersManager.toSAX(handler, -1, 0, parameters);
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        assertEquals(0.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[count(@*) != 1])"));
        assertEquals(0.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[count(*) != 3])"));
        
        testNode = xpath.selectSingleNode(handler.getDocument(), "/users/user[@login = 'user1']");
        assertNotNull(testNode);
        assertEquals("USER1", xpath.evaluateAsString(testNode, "lastname"));
        assertEquals("User1", xpath.evaluateAsString(testNode, "firstname"));
        assertEquals("user1@ametys.org", xpath.evaluateAsString(testNode, "email"));
        
        assertEquals("2", xpath.evaluateAsString(handler.getDocument(), "/users/total"));

        // Sax all by part
        Set<String> results = new HashSet<String>();
        
        handler = dom.createDOMHandler();
        handler.startDocument();
        _usersManager.toSAX(handler, 4, 0, new HashMap<String, String>());
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        
        NodeList list = xpath.selectNodeList(handler.getDocument(), "/users/user");
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            results.add(node.getAttributes().getNamedItem("login").getNodeValue());
        }
        
        handler = dom.createDOMHandler();
        handler.startDocument();
        _usersManager.toSAX(handler, 4, 4, new HashMap<String, String>());
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        
        list = xpath.selectNodeList(handler.getDocument(), "/users/user");
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            results.add(node.getAttributes().getNamedItem("login").getNodeValue());
        }

        handler = dom.createDOMHandler();
        handler.startDocument();
        _usersManager.toSAX(handler, 4, 8, new HashMap<String, String>());
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        
        list = xpath.selectNodeList(handler.getDocument(), "/users/user");
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            results.add(node.getAttributes().getNamedItem("login").getNodeValue());
        }
        
        assertEquals(10, results.size());
        assertTrue(results.contains("user1"));
        assertTrue(results.contains("user2"));
        assertTrue(results.contains("user3"));
        assertTrue(results.contains("user4"));
        assertTrue(results.contains("user5"));
        assertTrue(results.contains("user6"));
        assertTrue(results.contains("user7"));
        assertTrue(results.contains("user8"));
        assertTrue(results.contains("user9"));
        assertTrue(results.contains("user10"));
    }
}
