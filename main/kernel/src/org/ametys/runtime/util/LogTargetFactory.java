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
package org.ametys.runtime.util;

import java.io.File;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.util.log.CocoonTargetFactory;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;

import org.ametys.runtime.authentication.AuthorizationRequiredException;


/**
 * Simple LogTargetFactory not logging internal exceptions, such as AuthorizationRequiredException.
 * Its configuration is strictly the same as CocoonTargetFactory
 */
@SuppressWarnings("deprecation")
public class LogTargetFactory extends CocoonTargetFactory
{
    @Override
    protected LogTarget createTarget(File file, Configuration configuration) throws ConfigurationException
    {
        return new WrapperLogTarget(super.createTarget(file, configuration));
    }
    
    private class WrapperLogTarget implements LogTarget
    {
        private LogTarget _target;
        
        WrapperLogTarget(LogTarget target)
        {
            _target = target;
        }

        public void processEvent(LogEvent event)
        {
            Throwable t = event.getThrowable();
            if (t == null || !(t instanceof AuthorizationRequiredException))
            {
                _target.processEvent(event);
            }
        }
    }
}
