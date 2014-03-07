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
