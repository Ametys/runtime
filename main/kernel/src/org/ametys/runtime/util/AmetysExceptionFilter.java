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
package org.ametys.runtime.util;

import java.net.SocketException;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import org.ametys.runtime.authentication.AuthorizationRequiredException;

/**
 * Simple {@link Filter} not logging internal exceptions, such as AuthorizationRequiredException.
 */
public class AmetysExceptionFilter extends Filter
{
    @Override
    public int decide(LoggingEvent event)
    {
        ThrowableInformation info = event.getThrowableInformation();
        if (info != null && info.getThrowable() != null)
        {
            Throwable t = info.getThrowable();
            if (_unroll(t) instanceof AuthorizationRequiredException)
            {
                return Filter.DENY;
            }
            
            if (_unroll(t) instanceof SocketException)
            {
                return Filter.DENY;
            }
        }
        
        return Filter.NEUTRAL;
    }
    
    private Throwable _unroll(Throwable t)
    {
        Throwable cause = t.getCause();
        
        if (cause == null)
        {
            return t;
        }
        else
        {
            return _unroll(cause);
        }
    }
}
