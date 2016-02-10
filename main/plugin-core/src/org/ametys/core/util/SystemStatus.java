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
package org.ametys.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.commons.collections.CollectionUtils;

/**
 * Component that centralizes and provides the system status
 */
public class SystemStatus implements Component, Initializable, Disposable
{
    /** The Avalon Role */
    public static final String ROLE = SystemStatus.class.getName();
    
    private Set<String> _systemStatus;
    
    @Override
    public void initialize() throws Exception
    {
        if (_systemStatus == null)
        {
            _systemStatus = new HashSet<>();
        }
    }
    
    @Override
    public void dispose()
    {
        _systemStatus = null;
    }
    
    /**
     * Returns the system status
     * @return The collection of system status
     */
    public Collection<String> getStatus()
    {
        return CollectionUtils.unmodifiableCollection(_systemStatus);
    }
    
    /**
     * Add a system status
     * @param status The system status to add 
     * @return <tt>true</tt> if the system status did not already contain the specified status
     */
    public boolean addStatus(String status)
    {
        return _systemStatus.add(status);
    }
    
    /**
     * Add a system status
     * @param status The system status to remove 
     * @return <tt>true</tt> if the system status contained the specified element
     */
    public boolean removeStatus(String status)
    {
        return _systemStatus.remove(status);
    }
}
