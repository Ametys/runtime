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
package org.ametys.core.group.directory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ametys.core.group.Group;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * Abstraction for a directory of group.
 */
public interface GroupDirectory
{
    /**
     * Get the id of the group directory.
     * @return The id of the group directory
     */
    public String getId();
    
    /**
     * Get the label of the group directory.
     * @return The label of the group directory
     */
    public I18nizableText getLabel();
    
    /**
     * Set the id of the group directory.
     * @param id The id
     */
    public void setId(String id);
    
    /**
     * Set the label of the group directory.
     * @param label The label
     */
    public void setLabel(I18nizableText label);
    
    /**
     * Get the id of the {@link GroupDirectoryModel} extension point
     * @return the id of extension point
     */
    public String getGroupDirectoryModelId();
    
    /**
     * Get the values of parameters (from group directory model)
     * @return the parameters' values
     */
    public Map<String, Object> getParameterValues();
    
    /**
     * Initialize the group directory with given parameter values.
     * @param groupDirectoryModelId The id of group directory extension point
     * @param paramValues The parameters' values
     */
    public void init(String groupDirectoryModelId, Map<String, Object> paramValues);
    
    /**
     * Returns a particular group.
     * @param groupID The id of the group.
     * @return The group or null if the group does not exist.
     */
    public Group getGroup(String groupID);

    /**
     * Returns all groups.
     * @return The groups as a Set of UserGroup, empty if an error occurs.
     */
    public Set<Group> getGroups();

    /**
     * Get all groups a particular user is in.
     * @param login The login of the user.
     * @param populationId The id of the population of the user
     * @return The groups as a Set of String (group ID), empty if the login does not match.
     */
    public Set<String> getUserGroups(String login, String populationId);
    
    /**
     * Get groups
     * @param count The maximum number of groups to sax. (-1 to sax all)
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters for saxing user list differently, see implementation.
     * @return The matching groups as a json object
     */
    public List<Map<String, Object>> groups2JSON(int count, int offset, Map parameters);
    
    /**
     * Get group
     * @param id The group's id
     * @return The matching group as a json object
     */
    public Map<String, Object> group2JSON(String id);

}
