/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.util.cocoon.source;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.excalibur.source.SourceFactory;

import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * {@link SourceFactory} looking for the kernel in the classpath first and then in the external kernel, if any.
 */
public class KernelSourceFactory extends ProxySourceFactory
{
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _matcher = Pattern.compile("kernel://(.*)");
        
        _protocols = new ArrayList<>();
        _protocols.add("resource://org/ametys/runtime/kernel/{1}");
        
        File externalKernel = RuntimeConfig.getInstance().getExternalKernel();
        
        if (externalKernel != null)
        {
            _protocols.add(externalKernel.toURI() + "{1}");
        }
    }
}
