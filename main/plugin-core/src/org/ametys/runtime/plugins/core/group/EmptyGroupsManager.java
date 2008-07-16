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
package org.ametys.runtime.plugins.core.group;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;


/**
 * Empty implementation of the group manager.<br>
 * Used when no groups of users is needed.
 */
public class EmptyGroupsManager implements GroupsManager, Component
{    
    public Group getGroup(String groupID)
    {
        return null;
    }

    public Set<Group> getGroups()
    {
        return Collections.emptySet();
    }

    public Set<String> getUserGroups(String login)
    {
        return Collections.emptySet();
    }

    public void toSAX(ContentHandler ch, int count, int offset, Map parameters) throws SAXException
    {
        XMLUtils.createElement(ch, "groups");
    }
}
