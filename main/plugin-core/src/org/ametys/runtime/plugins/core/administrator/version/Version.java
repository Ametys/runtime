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
package org.ametys.runtime.plugins.core.administrator.version;

import java.util.Date;

/**
 * Represents a given version of a component, given its unique name and build date.<br>
 * It is used in the administrator area to display the versions of the main used components.
 */
public final class Version
{
    private String _name;
    private String _version;
    private Date _date;
    
    /**
     * Constructor
     * @param name the name of the component
     * @param version the name of this version
     * @param date the build date of this version. May be null.
     */
    public Version(String name, String version, Date date)
    {
        _name = name;
        _version = version;
        _date = date;
    }
    
    /**
     * Returns the name of the component
     * @return the name of the component
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Returns the name of this version
     * @return the name of this version
     */
    public String getVersion()
    {
        return _version;
    }
    
    /**
     * Returns the build date of this version
     * @return the build date of this version
     */
    public Date getDate()
    {
        return _date;
    }
}
