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
package org.ametys.plugins.core.impl.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupsManager;


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
    
    public Map<String, Object> group2JSON(String id)
    {
        return null;
    }
    
    public List<Map<String, Object>> groups2JSON(int count, int offset, Map parameters)
    {
        return new ArrayList<Map<String,Object>>();
    }
}
