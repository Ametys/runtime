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
package org.ametys.runtime.plugins.core.group.ldap;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.plugins.core.util.ldap.AbstractLDAPConnector;

/**
 * This class is the base for groups manager using LDAP
 */
public abstract class AbstractLDAPGroupsManager extends AbstractLDAPConnector implements GroupsManager
{
    /** The group DN relative to baseDN */
    protected String _groupsRelativeDN;
    /** The filter to find groups */
    protected String _groupsObjectFilter;
    /** The scope used for search. */
    protected int _groupsSearchScope;
    /** The group id attribute */
    protected String _groupsIdAttribute;
    /** The group description attribute */
    protected String _groupsDescriptionAttribute;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        
        _groupsRelativeDN = _getConfigParameter(configuration, "RelativeDN");
        _groupsObjectFilter = _getFilter(configuration, "Filter");
        _groupsSearchScope = _getSearchScope(configuration, "SearchScope");
        _groupsIdAttribute = _getConfigParameter(configuration, "Id");
        _groupsDescriptionAttribute = _getConfigParameter(configuration, "Description");
    }

    public void toSAX(ContentHandler ch, int count, int offset, Map parameters) throws SAXException
    {
        XMLUtils.startElement(ch, "groups");
        
        String pattern = (String) parameters.get("pattern");
        
        Iterator iterator = getGroups().iterator();
        
        int totalCount = 0;
        int currentOffset = offset;
        // Parcourir les groupes
        while (currentOffset > 0 && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            if (pattern == null || pattern.length() == 0 || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                currentOffset--;
                totalCount++;
            }
        }
        
        int currentCount = count;
        // Parcourir les groupes
        while ((count == -1 || currentCount > 0) && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            
            if (pattern == null || pattern.length() == 0 || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "id", "id", "CDATA", group.getId());
                XMLUtils.startElement(ch, "group", attr);
                
                XMLUtils.createElement(ch, "label", group.getLabel());
    
                XMLUtils.startElement(ch, "users");
    
                // Parcourir les utilisateurs du groupe courant
                for (String login : group.getUsers())
                {
                    XMLUtils.createElement(ch, "user", login);
                }
    
                XMLUtils.endElement(ch, "users");
                XMLUtils.endElement(ch, "group");
                
                currentCount--;
                totalCount++;
            }
        }
        
        while (iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            
            if (pattern == null || pattern.length() == 0 || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                totalCount++;
            }
        }
        
        // Total count matching the pattern
        XMLUtils.createElement(ch, "total", String.valueOf(totalCount));
        
        XMLUtils.endElement(ch, "groups");
    }
}
