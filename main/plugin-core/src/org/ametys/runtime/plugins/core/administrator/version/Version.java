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
