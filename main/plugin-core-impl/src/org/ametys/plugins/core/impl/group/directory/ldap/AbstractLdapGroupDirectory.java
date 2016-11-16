/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.plugins.core.impl.group.directory.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.ametys.core.group.Group;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryModel;
import org.ametys.core.util.ldap.AbstractLDAPConnector;
import org.ametys.core.util.ldap.ScopeEnumerator;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * This class is the base for group directories using LDAP
 */
public abstract class AbstractLdapGroupDirectory extends AbstractLDAPConnector implements GroupDirectory
{
    /** Name of the parameter holding the id of the associated population */
    protected static final String __PARAM_ASSOCIATED_POPULATION_ID = "runtime.groups.ldap.population";
    /** Name of the parameter holding the datasource id */
    protected static final String __PARAM_DATASOURCE_ID = "runtime.groups.ldap.datasource";
    /** Relative DN for groups. */
    protected static final String __PARAM_GROUPS_RELATIVE_DN = "runtime.groups.ldap.groupDN";
    /** Filter for limiting the search. */
    protected static final String __PARAM_GROUPS_OBJECT_FILTER = "runtime.groups.ldap.filter";
    /** The scope used for search. */
    protected static final String __PARAM_GROUPS_SEARCH_SCOPE = "runtime.groups.ldap.scope";
    /** Name of the id attribute. */
    protected static final String __PARAM_GROUPS_ID_ATTRIBUTE = "runtime.groups.ldap.id";
    /** Name of the decription attribute. */
    protected static final String __PARAM_GROUPS_DESCRIPTION_ATTRIBUTE = "runtime.groups.ldap.description";
    
    /** The id of the associated user population where the LDAP group will retrieve the users */
    protected String _associatedPopulationId;
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
    /** The LDAP search page size. */
    protected int _pageSize;
    
    /** The id */
    protected String _id;
    /** The label */
    protected I18nizableText _label;
    /** The id of the {@link GroupDirectoryModel} */
    private String _groupDirectoryModelId;
    /** The map of the values of the parameters */
    private Map<String, Object> _paramValues;
    
    @Override
    public String getId()
    {
        return _id;
    }
    
    @Override
    public I18nizableText getLabel()
    {
        return _label;
    }
    
    @Override
    public void setId(String id)
    {
        _id = id;
    }
    
    @Override
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }
    
    @Override
    public String getGroupDirectoryModelId ()
    {
        return _groupDirectoryModelId;
    }
    
    @Override
    public Map<String, Object> getParameterValues()
    {
        return _paramValues;
    }
    
    @Override
    public void init(String groupDirectoryModelId, Map<String, Object> paramValues)
    {
        _groupDirectoryModelId = groupDirectoryModelId;
        _paramValues = paramValues;
        
        _associatedPopulationId = (String) paramValues.get(__PARAM_ASSOCIATED_POPULATION_ID);
        
        _groupsRelativeDN = (String) paramValues.get(__PARAM_GROUPS_RELATIVE_DN);
        _groupsObjectFilter = (String) paramValues.get(__PARAM_GROUPS_OBJECT_FILTER);
        _groupsSearchScope = ScopeEnumerator.parseScope((String) paramValues.get(__PARAM_GROUPS_SEARCH_SCOPE));
        _groupsIdAttribute = (String) paramValues.get(__PARAM_GROUPS_ID_ATTRIBUTE);
        _groupsDescriptionAttribute = (String) paramValues.get(__PARAM_GROUPS_DESCRIPTION_ATTRIBUTE);
        
        String dataSourceId = (String) paramValues.get(__PARAM_DATASOURCE_ID);
        try
        {
            _delayedInitialize(dataSourceId);
        }
        catch (Exception e)
        {
            getLogger().error("An error occured during the initialization of LDAPUserDirectory", e);
        }
        
        _pageSize = __DEFAULT_PAGE_SIZE;
    }
    
    @Override
    public Map<String, Object> group2JSON(String id)
    {
        Group group = getGroup(id);
        return _group2JSON(group, false);
    }
    
    @Override
    public List<Map<String, Object>> groups2JSON(int count, int offset, Map parameters)
    {
        List<Map<String, Object>> groups = new ArrayList<>();
        
        String pattern = (String) parameters.get("pattern");
        
        Iterator iterator = getGroups().iterator();
        
        //int totalCount = 0;
        int currentOffset = offset;

        while (currentOffset > 0 && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            if (StringUtils.isEmpty(pattern) || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                currentOffset--;
                //totalCount++;
            }
        }
        
        int currentCount = count;
        while ((count == -1 || currentCount > 0) && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            
            if (StringUtils.isEmpty(pattern) || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                groups.add(_group2JSON (group, true));
                
                currentCount--;
                //totalCount++;
            }
        }
        
        while (iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            
            if (StringUtils.isEmpty(pattern) || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                //totalCount++;
            }
        }
        
        // TODO Total count matching the pattern
        // XMLUtils.createElement(ch, "total", String.valueOf(totalCount));
        
        return groups;
    }
    
    /**
     * Get group as JSON object
     * @param group the group
     * @param users true to get users' group
     * @return the group as JSON object
     */
    protected Map<String, Object> _group2JSON (Group group, boolean users)
    {
        Map<String, Object> group2json = new HashMap<>();
        group2json.put("id", group.getIdentity().getId());
        group2json.put("groupDirectory", group.getIdentity().getDirectoryId());
        group2json.put("groupDirectoryLabel", group.getGroupDirectory().getLabel());
        group2json.put("label", group.getLabel());
        if (users)
        {
            group2json.put("users", group.getUsers());
        }
        return group2json;
    }

}
