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
package org.ametys.runtime.plugins.core.userpref;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.Parameter;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * Definition of a user preference.
 */
public class UserPreference extends Parameter<ParameterType>
{
    
    /** The display group. */
    protected I18nizableText _displayGroup;
    
    /** Indicates if the user preference is multiple. */
    protected boolean _multiple;
    
    /** The manager role. */
    protected String _managerRole;
    
    /** The preference order. */
    protected int _order;
    
    /**
     * Get the display group.
     * @return the display group.
     */
    public I18nizableText getDisplayGroup()
    {
        return _displayGroup;
    }
    
    /**
     * Set the parameter display group.
     * @param displayGroup the display group to set.
     */
    public void setDisplayGroup(I18nizableText displayGroup)
    {
        _displayGroup = displayGroup;
    }
    
    /**
     * Get the storage manager role.
     * @return the manager role. Can be null to use the default storage manager.
     */
    public String getManagerRole()
    {
        return _managerRole;
    }
    
    /**
     * Set the storage manager role.
     * @param managerRole the manager role to set. Can be null to use the default storage manager.
     */
    public void setManagerRole(String managerRole)
    {
        _managerRole = managerRole;
    }
    
    /**
     * Test if the preference is multiple-valued.
     * @return true if the preference is multiple-valued, false if the preference is single-valued.
     */
    public boolean isMultiple()
    {
        return _multiple;
    }
    
    /**
     * Set if the preference is multiple-valued.
     * @param multiple true if the preference is multiple-valued, false if the preference is single-valued.
     */
    public void setMultiple(boolean multiple)
    {
        _multiple = multiple;
    }
    
    /**
     * Get the preference order.
     * @return the preference order.
     */
    public int getOrder()
    {
        return _order;
    }
    
    /**
     * Set the preference order.
     * @param order the preference order.
     */
    public void setOrder(int order)
    {
        _order = order;
    }
    
    @Override
    public String toString()
    {
        return "Preference '" + getId() + "' (type:    " + (_multiple ? "multiple " : " ") + getType().name() + ", label:    " + getLabel().toString() + ", " + (getDefaultValue() != null ? ("default value: " + getDefaultValue()) : "no default value")  + ", " + (getEnumerator() != null ? ("enumerator: " + getEnumerator()) : "no enumerator") + ")";
    }
}
