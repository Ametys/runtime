/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.plugins.core.impl.group.mixed;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.group.ModifiableGroupsManager;
import org.ametys.core.user.UserListener;
import org.ametys.core.util.IgnoreRootHandler;
import org.ametys.plugins.core.impl.group.jdbc.ModifiableJdbcGroupsManager;
import org.ametys.plugins.core.impl.group.ldap.GroupDrivenLdapGroupsManager;

/**
 * Mixed-source groups manager that searches a group first in a LDAP directory, then in a database.<br>
 * The relations between LDAP groups and users are group-driven, i.e. the group LDAP entry has a "list of members" attribute.
 */
public class GroupDrivenLdapAndJdbcGroupsManager extends GroupDrivenLdapGroupsManager implements ModifiableGroupsManager, UserListener, Serviceable//, Contextualizable, PluginAware
{
    
    /** The fallback groups manager. */
    protected ModifiableJdbcGroupsManager _fallbackGroupsManager;
    
    /** The service manager. */
    protected ServiceManager _serviceManager;
    
    /** The fallback groups manager configuration. */
    protected Configuration _fbConfiguration;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _serviceManager = manager;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        // Configure the main GroupsManager (superclass).
        super.configure(configuration);
        
        // Then store the configuration of the fallback groups manager.
        _fbConfiguration = configuration.getChild("FallbackGroupsManagerConfiguration");
    }
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        // Create the fallback GroupsManager and execute all its lifecycle operations.
        _fallbackGroupsManager = _createFallbackGroupsManager();
    }
    
    /**
     * Create the impl for jdbc groups manager.
     * @return The instance. Cannot be null.
     * @throws Exception If an error occured during instanciation
     */
    protected ModifiableJdbcGroupsManager _createFallbackGroupsManager() throws Exception
    {
        ModifiableJdbcGroupsManager fallbackGM = new ModifiableJdbcGroupsManager();
        fallbackGM.service(_serviceManager);
        fallbackGM.configure(_fbConfiguration);
        fallbackGM.initialize();
        fallbackGM.enableLogging(getLogger());
        return fallbackGM;
    }
    
    // GroupManager methods: search first in the LDAP (super), then in the database (fallback) if not found. //
    
    @Override
    public Group getGroup(String groupID)
    {
        Group group = super.getGroup(groupID);
        
        if (group == null)
        {
            group = _fallbackGroupsManager.getGroup(groupID);
        }
        
        return group;
    }
    
    @Override
    public Set<Group> getGroups()
    {
        Set<Group> groups = new HashSet<>();
        
        groups.addAll(super.getGroups());
        groups.addAll(_fallbackGroupsManager.getGroups());
        
        return groups;
    }
    
    @Override
    public Set<String> getUserGroups(String login)
    {
        Set<String> userGroups = new HashSet<>();
        
        userGroups.addAll(super.getUserGroups(login));
        userGroups.addAll(_fallbackGroupsManager.getUserGroups(login));
        
        return userGroups;
    }
    
    @Override
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException
    {
        TagCountHandler tagCountHandler = new TagCountHandler(handler, "group");
        
        super.toSAX(tagCountHandler, count, offset, parameters);
        
        int tagCount = tagCountHandler.getSaxedTagCount();
        int newCount = count > 0 ? count - tagCount : count;
        
        _fallbackGroupsManager.toSAX(handler, newCount, offset, parameters);
    }
    
    // ModifiableGroupManager methods: delegate all to the JDBC groups manager (fallback). //
    
    @Override
    public Group add(String name) throws InvalidModificationException
    {
        return _fallbackGroupsManager.add(name);
    }
    
    @Override
    public void update(Group userGroup) throws InvalidModificationException
    {
        _fallbackGroupsManager.update(userGroup);
    }
    
    @Override
    public void remove(String groupID) throws InvalidModificationException
    {
        _fallbackGroupsManager.remove(groupID);
    }
    
    @Override
    public void registerListener(GroupListener listener)
    {
        _fallbackGroupsManager.removeListener(listener);
    }
    
    @Override
    public void removeListener(GroupListener listener)
    {
        _fallbackGroupsManager.removeListener(listener);
    }
    
    @Override
    public List<GroupListener> getListeners()
    {
        return _fallbackGroupsManager.getListeners();
    }
    
    @Override
    public void userRemoved(String login)
    {
        _fallbackGroupsManager.userRemoved(login);
    }
    
    @Override
    public void userAdded(String login)
    {
        _fallbackGroupsManager.userAdded(login);
    }
    
    @Override
    public void userUpdated(String login)
    {
        _fallbackGroupsManager.userUpdated(login);
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
