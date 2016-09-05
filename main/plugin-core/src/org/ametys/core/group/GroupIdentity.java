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
package org.ametys.core.group;

import org.apache.commons.lang3.StringUtils;

/**
 * Class containing a group identity, i.e. the id of the group and 
 * the id of its group directory.
 */
public class GroupIdentity
{
    /** The separator between the id and the group directory id for the string representation of a group identity */
    private static final String __SEPARATOR = "#"; 
    
    /** The id of the group */
    private String _id;
    
    /** The id of the group directory the group belongs to */
    private String _directoryId;
    
    /**
     * Constructs a group identity
     * @param id The id of the group
     * @param directoryId The id of the group directory the group belongs to
     */
    public GroupIdentity(String id, String directoryId)
    {
        _id = id;
        _directoryId = directoryId;
    }
    
    /**
     * Gets a string representation of a {@link GroupIdentity}
     * @param groupIdentity The group identity
     * @return The string representation of the group identity.
     */
    public static String groupIdentityToString(GroupIdentity groupIdentity)
    {
        return groupIdentity.getId() + __SEPARATOR + groupIdentity.getDirectoryId();
    }
    
    /**
     * Returns the {@link GroupIdentity} from its string representation
     * @param string The string representation of the group identity
     * @return The group identity from its string representation
     */
    public static GroupIdentity stringToGroupIdentity(String string)
    {
        String[] fields = StringUtils.split(string, __SEPARATOR);
        String id = fields[0];
        String groupDirectoryId = fields[1];
        return new GroupIdentity(id, groupDirectoryId);
    }
    

    /**
     * Get the if of the group
     * @return The id of the group
     */
    public String getId()
    {
        return _id;
    }

    /**
     * GetGet the group directory the group belongs to
     * @return The id of the group directory the group belongs to
     */
    public String getDirectoryId()
    {
        return _directoryId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_directoryId == null) ? 0 : _directoryId.hashCode());
        result = prime * result + ((_id == null) ? 0 : _id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        
        GroupIdentity other = (GroupIdentity) obj;
        if (_directoryId == null)
        {
            if (other._directoryId != null)
            {
                return false;
            }
        }
        else if (!_directoryId.equals(other._directoryId))
        {
            return false;
        }
        if (_id == null)
        {
            if (other._id != null)
            {
                return false;
            }
        }
        else if (!_id.equals(other._id))
        {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString()
    {
        return "Group [id=" + _id + ", directory=" + _directoryId + "]";
    }

}
