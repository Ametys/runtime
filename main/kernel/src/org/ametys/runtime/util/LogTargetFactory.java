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
