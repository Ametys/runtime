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
package org.ametys.runtime.authentication;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * Extension point holding all <code>Authentication</code> components.<br>
 * An <code>Authentication</code> is responsible for granting access to a
 * user, given its <code>Credentials</code>.
 */
public class AuthenticationManager extends AbstractThreadSafeComponentExtensionPoint<Authentication> implements Configurable
{
    /** Avalon Role */
    public static final String ROLE = AuthenticationManager.class.getName();

    private Set<String> _restrictedExtensionsId;

    private SourceResolver _resolver;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);
        _resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        String configFileName = "context://WEB-INF/param/authentication.xml";
        
        Source configSource = null;
        
        try
        {
            configSource = _resolver.resolveURI(configFileName);
            if (configSource.exists())
            {
                try (InputStream configStream = configSource.getInputStream())
                {
                    Configuration configFileConfiguration = new DefaultConfigurationBuilder().build(configStream);
                    
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("Starting configuration upon file '" + configFileName + "'.");
                    }
                    
                    Set<String> ids = new LinkedHashSet<>();

                    Configuration[] authConfs = configFileConfiguration.getChildren("authentication");
                    for (Configuration authConf : authConfs)
                    {
                        ids.add(authConf.getValue(""));
                    }
                    
                    _restrictedExtensionsId = Collections.unmodifiableSet(ids);
                    
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("Configuration ended with " + ids.size() + " ids configured.");
                    }                
                }
            }
            else
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("The file '" + configFileName + "' cannot be found. All 'Authentication' will be accepted.");
                }                
            }
        }
        catch (Exception e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("An error occured. Configuration of AuthenticationManager will be ignored : all 'Authentication' will be accepted", e);
            }
        }
    }

    @Override
    public boolean hasExtension(String id)
    {
        return super.hasExtension(id) && (_restrictedExtensionsId != null ? _restrictedExtensionsId.contains(id) : true);
    }

    @Override
    public Authentication getExtension(String id)
    {
        if (hasExtension(id))
        {
            return super.getExtension(id);
        }
        else
        {
            throw new IllegalArgumentException("The authentication extension may exists but is not part of the restricted list");
        }
    }

    @Override
    public Set<String> getExtensionsIds()
    {
        return _restrictedExtensionsId != null ? _restrictedExtensionsId : super.getExtensionsIds();
    }
}
