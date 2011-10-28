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
package org.ametys.runtime.test.users.others;

import java.util.Collection;

import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugins.core.user.StaticUsersManager;
import org.ametys.runtime.plugins.core.user.jdbc.ModifiableJdbcUsersManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

/**
 * Tests the DefinedUsersTestCase
 */
public class StaticUsersTestCase extends AbstractRuntimeTestCase
{
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp1");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Tests that the <code>StaticUsersTestCase</code> is the default <code>UsersManager</code><br/>
     * Tests the values returned by it
     * @throws Exception if an error occurs
     */
    public void testStaticUsers() throws Exception
    {
        UsersManager usersManager = (UsersManager) Init.getPluginServiceManager().lookup(UsersManager.ROLE);
        
        // DEFAULT IMPL
        assertTrue(usersManager instanceof StaticUsersManager);
        
        // NOT MODIFIABLE
        assertFalse(usersManager instanceof ModifiableJdbcUsersManager);
        
        // CREDENTIAL AWARE
        assertTrue(usersManager instanceof CredentialsAwareUsersManager);
        
        // ONE USER
        User user;
        
        user = usersManager.getUser("foo");
        assertNull(user);
        
        user = usersManager.getUser("anonymous");
        assertNotNull(user);
        assertEquals(user.getName(), "anonymous");
        assertEquals(user.getFullName(), "Anonymous user");
        assertEquals(user.getEmail(), "");
        
        // ALL USERS
        Collection<User> users = usersManager.getUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.iterator().next(), user);
        
        // LOGIN
        Credentials credentials;
        
        credentials = new Credentials("foo", null);
        assertFalse(((CredentialsAwareUsersManager) usersManager).checkCredentials(credentials));

        credentials = new Credentials("anonymous", null);
        assertTrue(((CredentialsAwareUsersManager) usersManager).checkCredentials(credentials));

        // SAX USERS
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        handler.startDocument();
        usersManager.toSAX(handler, 0, 0, null);
        handler.endDocument();

        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user/@*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/users/user/*)"));
        assertEquals("anonymous", xpath.evaluateAsString(handler.getDocument(), "/users/user/@login"));
        assertEquals("Anonymous user", xpath.evaluateAsString(handler.getDocument(), "/users/user/lastname"));
        assertEquals("", xpath.evaluateAsString(handler.getDocument(), "/users/user/email"));
        assertEquals("1", xpath.evaluateAsString(handler.getDocument(), "/users/total"));
    }
}
