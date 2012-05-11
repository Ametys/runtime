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

package org.ametys.runtime.util.cocoon.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;

/**
 * Factory for reading files in one protocol and switch to others if not found.
 */
public class SwitchSourceFactory extends AbstractLogEnabled implements SourceFactory, ThreadSafe, Serviceable, Configurable
{
    private SourceResolver _sourceResolver;
    private List<String> _protocols;
    private ServiceManager _manager;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _protocols = new ArrayList<String>();
        for (Configuration protocolConf : configuration.getChildren("protocol"))
        {
            String protocol = protocolConf.getValue();
            if (!StringUtils.isBlank(protocol))
            {
                _protocols.add(protocol);
            }
            else
            {
                getLogger().warn("Trying to configure a blank protocol. Ignoring this line.");
            }
        }
        
        if (_protocols.size() == 0)
        {
            throw new ConfigurationException("Cannot configure this protocol with no redirecting protocols. Add <protocol>...</protocol> items in this component configuration.", configuration);
        }
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    @Override
    public Source getSource(String location, Map parameters) throws IOException, MalformedURLException
    {
        if (_sourceResolver == null)
        {
            try
            {
                _sourceResolver = (SourceResolver) _manager.lookup(SourceResolver.ROLE);
            }
            catch (ServiceException e)
            {
                throw new IOException(e);
            }
        }

        final int pos = SourceUtil.indexOfSchemeColon(location);
        if (pos == -1 || !location.startsWith("://", pos))
        {
            throw new MalformedURLException("Invalid format for source : " + location);
        }

        String sublocation = location.substring(pos + 3); 
        
        Source source = null;
        for (String protocol : _protocols)
        {
            try
            {
                source = _sourceResolver.resolveURI(protocol + sublocation, null, parameters);
                if (!source.exists())
                {
                    _sourceResolver.release(source);
                    source = null;
                }
                else
                {
                    return source;
                }
            }
            catch (IOException e)
            {
                // nothing to do specially
            }
        }
        
        throw new SourceNotFoundException("Source not found '" + location + "'. (tryed successively in " + _protocols + ")");
    }
    
    @Override
    public void release(Source source)
    {
        // Should never be called
    }
}
