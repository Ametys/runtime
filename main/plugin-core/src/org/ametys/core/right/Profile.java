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
package org.ametys.core.right;

import java.util.HashMap;
import java.util.Map;

/**
 * This bean represents a profile
 */
public class Profile
{
    private String _id;
    private String _label;
    private String _context;
    
    /**
     * Constructor.
     * @param id the unique id of this profile
     * @param label the label of this profile
     */
    public Profile(String id, String label)
    {
        this(id, label, null);
    }
    
    /**
     * Constructor.
     * @param id the unique id of this profile
     * @param label the label of this profile
     * @param context the context
     */
    public Profile(String id, String label, String context)
    {
        _id = id;
        _label = label;
        _context = context;
    }
    
    /**
     * Set the id of profile
     * @param id The id to set
     */
    public void setId (String id)
    {
        _id = id;
    }
    
    /**
     * Returns the unique Id of this profile
     * @return the unique Id of this profile
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Returns the name of this profile
     * @return the name of this profile
     */
    public String getLabel()
    {
        return _label;
    }
    
    /**
     * Set the label of profile
     * @param label The label to set
     */
    public void setLabel (String label)
    {
        _label = label;
    }
    
    /**
     * Returns the context of this profile
     * @return the context of this profile. Can be null.
     */
    public String getContext()
    {
        return _context;
    }
    
    /**
     * Set the context of profile
     * @param context The context to set
     */
    public void setContext (String context)
    {
        _context = context;
    }
    
    /**
     * Get the JSON representation of this Profile
     * @return The profile's properties
     */
    public Map<String, Object> toJSON()
    {
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("id", _id);
        profile.put("label", _label);
        profile.put("context", getContext());
        
        return profile;
    }
    
    
    @Override
    public boolean equals(Object another)
    {
        if (another == null || !(another instanceof Profile))
        {
            return false;
        }

        Profile otherProfile = (Profile) another;

        return _id != null  || _id.equals(otherProfile.getId());
    }

    @Override
    public int hashCode()
    {
        return _id.hashCode();
    }
}
