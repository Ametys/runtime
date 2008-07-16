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

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.util.LoggerFactory;


/**
 * This component count the active requests
 */
public class RequestCountListener implements ServletRequestListener
{
    private static boolean _active;
    
    private static int _count;
    
    private static Logger _logger;
    
    /**
     * Activate the counter
     */
    public RequestCountListener()
    {
        _active = true;
    }
    
    public void requestDestroyed(ServletRequestEvent event)
    {
        _count--;
    }
    
    public void requestInitialized(ServletRequestEvent event)
    {
        _count++;
    }
    
    
    /**
     * Returns the numer of active Requests
     * @return the numer of active Requests
     * @throws IllegalStateException if the listener is not registred
     */
    public static int getSessionCount()
    {
        if (!_active)
        {
            String message = "The RequestCountListener is not installed. Please add in the file 'WEB-INF/web.xml' the following line under the root element :\n&lt;listener&gt;&lt;listener-class&gt;" + RequestCountListener.class.getName() + "&lt;/listener-class&gt;&lt;/listener&gt;";
            if (_logger == null)
            {
                _logger = LoggerFactory.getLoggerFor(SessionCountListener.class);
            }
            if (_logger.isWarnEnabled())
            {
                _logger.warn(message);
            }
            throw new IllegalStateException(message);
        }
        
        return _count;
    }
}
