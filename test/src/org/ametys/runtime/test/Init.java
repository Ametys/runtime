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
package org.ametys.runtime.test;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * An init class to let the plugin manager accessible<br>
 * Use by most test cases.
 */
public class Init implements org.ametys.runtime.plugin.Init, Component, Serviceable
{
    /** the plugin service manager */
    protected static ServiceManager _manager;
    
    private static boolean _ok;
    
    /**
     * Constructor
     */
    public Init()
    {
        _ok = false;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    /**
     * Returns the plugin service manager
     * @return the plugin service manager
     */
    public static ServiceManager getPluginServiceManager()
    {
        return _manager;
    }
    
    public void init() throws Exception
    {
        // nothing
        _ok = true;
    }
    
    /**
     * Returns true if the init() method has been called
     * @return true if the init() method has been called
     */
    public static boolean isOk()
    {
        return _ok;
    }
}
