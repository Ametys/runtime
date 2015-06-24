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
package org.ametys.runtime.plugins.admin.jvmstatus;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component count the current active requests and count all
 * request since startup. 
 */
public class RequestCountListener implements ServletRequestListener
{
    private static AtomicInteger _current = new AtomicInteger();
    private static AtomicInteger _total = new AtomicInteger();
    private static boolean _active;
    private static Logger _logger;
    
    /**
     * Activate the counter
     */
    public RequestCountListener()
    {
        _active = true;
    }
    
    public void requestInitialized(ServletRequestEvent event)
    {
        _current.incrementAndGet();
        _total.incrementAndGet();
    }
    
    public void requestDestroyed(ServletRequestEvent event)
    {
        _current.decrementAndGet();
    }
    
    /**
     * Returns the current number of active requests.
     * @return the current number of active requests.
     * @throws IllegalStateException if the listener is not registered.
     */
    public static int getCurrentRequestCount()
    {
        if (!_active)
        {
            String message = "The RequestCountListener is not installed. Please add in the file 'WEB-INF/web.xml' the following line under the root element :\n&lt;listener&gt;&lt;listener-class&gt;" + RequestCountListener.class.getName() + "&lt;/listener-class&gt;&lt;/listener&gt;";
            if (_logger == null)
            {
                _logger = LoggerFactory.getLogger(RequestCountListener.class);
            }
            if (_logger.isWarnEnabled())
            {
                _logger.warn(message);
            }
            throw new IllegalStateException(message);
        }
        
        return _current.intValue();
    }
    
    /**
     * Returns the total number of requests since startup.
     * @return the total number of active since startup.
     * @throws IllegalStateException if the listener is not registered.
     */
    public static int getTotalRequestCount()
    {
        if (!_active)
        {
            String message = "The RequestCountListener is not installed. Please add in the file 'WEB-INF/web.xml' the following line under the root element :\n&lt;listener&gt;&lt;listener-class&gt;" + RequestCountListener.class.getName() + "&lt;/listener-class&gt;&lt;/listener&gt;";
            if (_logger == null)
            {
                _logger = LoggerFactory.getLogger(RequestCountListener.class);
            }
            if (_logger.isWarnEnabled())
            {
                _logger.warn(message);
            }
            throw new IllegalStateException(message);
        }
        
        return _total.intValue();
    }
}
