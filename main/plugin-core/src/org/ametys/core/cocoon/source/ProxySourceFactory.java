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
package org.ametys.core.cocoon.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * Factory for reading files in one protocol and switch to others if not found.
 * Configuration should be like:<br><pre><code>
 *       &lt;match&gt;kernel://(.*)&lt;/match&gt;
 *       &lt;protocols&gt;
 *         &lt;protocol&gt;resource://org/ametys/runtime/kernel/{1}&lt;/protocol&gt;
 *         &lt;protocol&gt;context://kernel/{1}&lt;/protocol&gt;
 *       &lt;/protocols&gt;</code></pre>
 */
public class ProxySourceFactory extends AbstractLogEnabled implements SourceFactory, ThreadSafe, Serviceable, Configurable
{
    /** URI matcher */
    protected Pattern _matcher;
    
    /** Proxied sources */
    protected List<String> _protocols;

    private SourceResolver _sourceResolver;
    private ServiceManager _manager;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _matcher = Pattern.compile(configuration.getChild("match").getValue());
        
        _protocols = new ArrayList<>();
        for (Configuration protocolConf : configuration.getChild("protocols").getChildren("protocol"))
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

        Matcher matcher = _matcher.matcher(location);
        if (!matcher.matches())
        {
            throw new MalformedURLException("Invalid format for source : " + location);
        }

        Source source = null;
        for (String protocol : _protocols)
        {
            String uri = protocol;
            try
            {
                for (int groupIndex = 0; groupIndex <= matcher.groupCount(); groupIndex++)
                {
                    uri = uri.replaceAll("\\{" + groupIndex + "\\}", matcher.group(groupIndex));
                }
                
                source = _sourceResolver.resolveURI(uri, null, parameters);
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
                // nothing to do...
            }
        }
        
        throw new SourceNotFoundException("Source not found '" + location + "'. (tried successively in " + _protocols + ")");
    }
    
    @Override
    public void release(Source source)
    {
        // Should never be called
    }
}
