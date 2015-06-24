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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HttpSessionListener counting sessions. 
 */
public class SessionCountListener implements HttpSessionListener
{
    private static AtomicInteger _count = new AtomicInteger();
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
        _count.incrementAndGet();
    }

    public void sessionDestroyed(HttpSessionEvent se)
    {
        _active = true;
        if (!_count.compareAndSet(0, 0))
        {
            _count.decrementAndGet();
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
                _logger = LoggerFactory.getLogger(SessionCountListener.class);
            }
            if (_logger.isWarnEnabled())
            {
                _logger.warn(message);
            }
            throw new IllegalStateException(message);
        }
        
        return _count.intValue();
    }
}
