/*
 *  Copyright 2009 Anyware Services
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
