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
package org.ametys.runtime.plugins.core.administrator.jvmstatus;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.ametys.runtime.util.LoggerFactory;
import org.apache.avalon.framework.logger.Logger;


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
                _logger = LoggerFactory.getLoggerFor(RequestCountListener.class);
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
                _logger = LoggerFactory.getLoggerFor(RequestCountListener.class);
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
