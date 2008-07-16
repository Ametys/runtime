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
package org.ametys.runtime.config;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;


/**
 * Input module for getting config values.
 */
public class ConfigInputModule implements InputModule, ThreadSafe
{
    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        Config config = Config.getInstance();
        if (config == null)
        {
            return null;
        }

        return config.getValueAsString(name);
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        return null;
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        return new Object[] {getAttribute(name, modeConf, objectModel)};
    }
}
