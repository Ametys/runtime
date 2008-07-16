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
        
        int currentOffset = offset;
        // Parcourir les groupes
        while (currentOffset > 0 && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            if (pattern == null || pattern.length() == 0 || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                currentOffset--;
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
            }
        }
        
        XMLUtils.endElement(ch, "groups");
    }
}
