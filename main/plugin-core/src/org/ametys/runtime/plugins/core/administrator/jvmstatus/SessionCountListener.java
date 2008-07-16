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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.util.LoggerFactory;


/**
 * Simple HttpSessionListener counting sessions. 
 */
public class SessionCountListener implements HttpSessionListener
{
    private static int _count;
    
    private static boolean _active;
    
    private static Logger _logger;

    /**
     * Active the static listener
     */
    public SessionCountListener()
    {
        _active = true;
    }
    
    public void sessionCreated(HttpSessionEvent se)
    {
        _active = true;
        _count++;
    }

    public void sessionDestroyed(HttpSessionEvent se)
    {
        _active = true;
        if (_count > 0)
        {
            _count--;
        }
    }
    
    /**
     * Returns the numer of active Sessions
     * @return the numer of active Sessions
     * @throws IllegalStateException if the listener is not registred
     */
    public static int getSessionCount()
    {
        if (!_active)
        {
            String message = "The SessionCountListener is not installed. Please add in the file 'WEB-INF/web.xml' the following line under the root element :\n&lt;listener&gt;&lt;listener-class&gt;" + SessionCountListener.class.getName() + "&lt;/listener-class&gt;&lt;/listener&gt;";
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
