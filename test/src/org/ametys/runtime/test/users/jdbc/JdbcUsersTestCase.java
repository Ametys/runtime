/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.test.users.jdbc;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ametys.runtime.plugins.core.user.jdbc.JdbcUsersManager;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.User;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Node;

/**
 * Tests the JdbcUsersManager
 */
public class JdbcUsersTestCase extends AbstractJDBCUsersManagerTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _resetDB("runtime4.xml", "config1.xml");
    }
    
    @Override
    protected File[] getScripts()
    {
        // Use non auth script by default
        return new File[] {new File("main/plugin-core/scripts/mysql/jdbc_users.sql")};
    }

    /**
     * Provide the scripts to run for populating database.
     * @return the scripts to run.
     */
    protected File[] getFilledScripts()
    {
        return new File[] {new File("test/environments/scripts/fillJDBCUsers.sql")};
    }
    
    /**
     * Test the getting of users on mysql
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // JDBC IMPL
        assertTrue(_usersManager instanceof JdbcUsersManager);

        // NOT MODIFIABLE
        assertFalse(_usersManager instanceof ModifiableUsersManager);
        
        // NOT CREDENTIAL AWARE
        assertFalse(_usersManager instanceof CredentialsAwareUsersManager);
    }
    
    /**
     * Test an empty db
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        // Empty DB
        User user = null;
        user = _usersManager.getUser("foo");
        assertNull(user);
        
        Collection<User> users = null;
        users = _usersManager.getUsers();
        assertEquals(users.size(), 0);
    }
    
    /**
     * Test a filled db
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        User user = null;
        Collection<User> users = null;

        // Fill DB
        _setDatabase(Arrays.asList(getFilledScripts()));
        
        // Get unexisting user
        user = _usersManager.getUser("foo");
        assertNull(user);

        // Get existing user
        user = _usersManager.getUser("test");
        assertNotNull(user);
        assertEquals(user.getName(), "test");
        assertEquals(user.getFullName(), "Test TEST");
        assertEquals(user.getEmail(), "test@test.te");

        // Get users
        users = _usersManager.getUsers();
        assertEquals(users.size(), 2);
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
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals("2", xpath.evaluateAsString(handler.getDocument(), "/users/total"));

        // Sax all
        handler = dom.createDOMHandler();
        handler.startDocument();
        _usersManager.toSAX(handler, -1, 0, new HashMap<String, String>());
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[1]/@*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[2]/@*)"));
        assertEquals(3.0 + (_usersManager instanceof CredentialsAwareUsersManager ? 1.0 : 0.0), xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[1]/*)"));
        assertEquals(3.0 + (_usersManager instanceof CredentialsAwareUsersManager ? 1.0 : 0.0), xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user[2]/*)"));
        
        testNode = xpath.selectSingleNode(handler.getDocument(), "/users/user[@login = 'test']");
        assertNotNull(testNode);
        assertEquals("TEST", xpath.evaluateAsString(testNode, "lastname"));
        assertEquals("Test", xpath.evaluateAsString(testNode, "firstname"));
        assertEquals("test@test.te", xpath.evaluateAsString(testNode, "email"));
        testNode = xpath.selectSingleNode(handler.getDocument(), "/users/user[@login = 'test2']");
        assertNotNull(testNode);
        assertEquals("TEST2", xpath.evaluateAsString(testNode, "lastname"));
        assertEquals("Test2", xpath.evaluateAsString(testNode, "firstname"));
        assertEquals("test2@test.te", xpath.evaluateAsString(testNode, "email"));
        
        assertEquals("2", xpath.evaluateAsString(handler.getDocument(), "/users/total"));

        // Sax any
        handler = dom.createDOMHandler();
        handler.startDocument();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("pattern", "test2");
        _usersManager.toSAX(handler, -1, 0, parameters);
        handler.endDocument();

        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user/@*)"));
        assertEquals(3.0 + (_usersManager instanceof CredentialsAwareUsersManager ? 1.0 : 0.0), xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user/*)"));
        
        testNode = xpath.selectSingleNode(handler.getDocument(), "/users/user[@login = 'test2']");
        assertNotNull(testNode);
        assertEquals("TEST2", xpath.evaluateAsString(testNode, "lastname"));
        assertEquals("Test2", xpath.evaluateAsString(testNode, "firstname"));
        assertEquals("test2@test.te", xpath.evaluateAsString(testNode, "email"));
        
        assertEquals("1", xpath.evaluateAsString(handler.getDocument(), "/users/total"));
    }
}
