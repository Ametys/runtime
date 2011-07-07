/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.plugins.core.user.mixed;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugins.core.user.jdbc.ModifiableCredentialsAwareJdbcUsersManager;
import org.ametys.runtime.plugins.core.user.ldap.CredentialsAwareLdapUsersManager;
import org.ametys.runtime.user.InvalidModificationException;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UserListener;
import org.ametys.runtime.util.IgnoreRootHandler;

/**
 * Try to find the user in the LDAP directory first. If not found, try to find him in the DBMS.
 */
public class CredentialsAwareLdapAndJdbcUsersManager extends CredentialsAwareLdapUsersManager implements ModifiableUsersManager, Serviceable, Contextualizable, PluginAware, Disposable
{
    /** Fallback users manager. */
    protected ModifiableCredentialsAwareJdbcUsersManager _fallbackUsersManager;
    
    /** The avalon context. */
    protected Context _context;
    /** The plugin name. */
    protected String _pluginName;
    /** The feature name. */
    protected String _featureName;
    /** The service manager. */
    protected ServiceManager _serviceManager;
    /** The fallback users manager configuration. */
    protected Configuration _fbConfiguration;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void setPluginInfo(String pluginName, String featureName)
    {
        _pluginName = pluginName;
        _featureName = featureName;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _serviceManager = manager;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        // Configure the main UsersManager (superclass).
        super.configure(configuration);
        
        // Then store the configuration of the fallback users manager.
        _fbConfiguration = configuration.getChild("FallbackUsersManagerConfiguration");
    }
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        // Create the fallback UsersManager and execute all its lifecycle operations.
        _fallbackUsersManager = _createJDBCUserManager();
    }
    
    /**
     * Create the impl for jdbc user manager
     * @return The instance. Cannot be null.
     * @throws Exception If an error occured during instanciation
     */
    protected ModifiableCredentialsAwareJdbcUsersManager _createJDBCUserManager() throws Exception
    {
        ModifiableCredentialsAwareJdbcUsersManager jdbcUM = new ModifiableCredentialsAwareJdbcUsersManager();
        jdbcUM.contextualize(_context);
        jdbcUM.setPluginInfo(_pluginName, _featureName);
        jdbcUM.service(_serviceManager);
        jdbcUM.configure(_fbConfiguration);
        jdbcUM.initialize();
        jdbcUM.enableLogging(getLogger());
        return jdbcUM;
    }
    
    @Override
    public void dispose()
    {
        _fallbackUsersManager.dispose();
        _fallbackUsersManager = null;
    }
    
    // UsersManager methods. //
    
    @Override
    public Collection<User> getUsers()
    {
        Set<User> users = new HashSet<User>();
        
        users.addAll(super.getUsers());
        users.addAll(_fallbackUsersManager.getUsers());
        
        return users;
    }
    
    @Override
    public User getUser(String login)
    {
        User user = super.getUser(login);
        
        if (user == null)
        {
            user = _fallbackUsersManager.getUser(login);
        }
        
        return user;
    }
    
    @Override
    public void saxUser(String login, ContentHandler handler) throws SAXException
    {
        TagCountHandler tagCountHandler = new TagCountHandler(handler, "user");
        
        super.saxUser(login, tagCountHandler);
        if (tagCountHandler.getSaxedTagCount() < 1)
        {
            _fallbackUsersManager.saxUser(login, handler);
        }
    }
    
    @Override
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException
    {
        TagCountHandler tagCountHandler = new TagCountHandler(handler, "user");
        
        super.toSAX(tagCountHandler, count, offset, parameters);
        
        int tagCount = tagCountHandler.getSaxedTagCount();
        int newCount = count > 0 ? count - tagCount : count;
        
        _fallbackUsersManager.toSAX(handler, newCount, offset, parameters);
    }
    
    // CredentialsAwareUsersManager methods. //
    
    @Override
    public boolean checkCredentials(Credentials credentials)
    {
        boolean authenticated = super.checkCredentials(credentials);
        
        if (!authenticated)
        {
            authenticated = _fallbackUsersManager.checkCredentials(credentials);
        }
        
        return authenticated;
    }
    
    // ModifableUsersManager methods. //
    
    @Override
    public void add(Map<String, String> userInformation) throws InvalidModificationException
    {
        _fallbackUsersManager.add(userInformation);
    }
    
    @Override
    public void update(Map<String, String> userInformation) throws InvalidModificationException
    {
        _fallbackUsersManager.update(userInformation);
    }
    
    @Override
    public void remove(String login) throws InvalidModificationException
    {
        _fallbackUsersManager.remove(login);
    }
    
    @Override
    public void saxModel(ContentHandler handler) throws SAXException
    {
        _fallbackUsersManager.saxModel(handler);
    }
    
    @Override
    public void registerListener(UserListener listener)
    {
        _fallbackUsersManager.registerListener(listener);
    }
    
    @Override
    public void removeListener(UserListener listener)
    {
        _fallbackUsersManager.removeListener(listener);
    }
    
    /**
     * Tag count handler.
     */
    protected class TagCountHandler extends IgnoreRootHandler
    {
        
        /** The tag to count. */
        protected String _tagQName;
        
        /** The tag count. */
        protected int _tagCount;
        
        /**
         * Constructor
         * @param contentHandler the contentHandler to pass SAX events to. In case the <code>ContentHandler</code> also implements the <code>LexicalHandler</code> interface, it will be honoured.
         * @param tagQName the tag qualified name.
         */
        public TagCountHandler(ContentHandler contentHandler, String tagQName)
        {
            super(contentHandler);
            _tagQName = tagQName;
            _tagCount = 0;
        }
        
        /**
         * Constructor
         * @param contentHandler the contentHandler to pass SAX events to
         * @param lexicalHandler the lexicalHandler to pass lexical events to. May be null.
         * @param tagQName the tag qualified name.
         */
        public TagCountHandler(ContentHandler contentHandler, LexicalHandler lexicalHandler, String tagQName)
        {
            super(contentHandler, lexicalHandler);
            _tagQName = tagQName;
            _tagCount = 0;
        }
        
        /**
         * Get the count of tags saxed through this handler.
         * @return the saxed tag count.
         */
        public int getSaxedTagCount()
        {
            return _tagCount;
        }
        
        /**
         * Reset the tag count.
         */
        public void resetTagCount()
        {
            _tagCount = 0;
        }
        
        @Override
        public void startDocument() throws SAXException
        {
            super.startDocument();
            resetTagCount();
        }
        
        @Override
        public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException
        {
            super.startElement(uri, loc, raw, a);
            if (_tagQName.equals(raw))
            {
                _tagCount++;
            }
        }
        
    }
    
}
