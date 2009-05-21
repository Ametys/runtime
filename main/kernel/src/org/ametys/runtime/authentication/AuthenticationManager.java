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
package org.ametys.runtime.authentication;

import java.io.IOException;
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

    private ServiceManager _serviceManager;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);
        _serviceManager = manager;
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        String configFileName = "context://WEB-INF/param/authentication.xml";
        
        SourceResolver resolver = null;
        Source configSource = null;
        InputStream configStream = null;
        try
        {
            resolver = (SourceResolver) _serviceManager.lookup(SourceResolver.ROLE);
            configSource = resolver.resolveURI(configFileName);
            if (configSource.exists())
            {
                configStream = configSource.getInputStream();
                Configuration configFileConfiguration = new DefaultConfigurationBuilder().build(configStream);
                
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Starting configuration upon file '" + configFileName + "'.");
                }
                
                Set<String> ids = new LinkedHashSet<String>();

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
                String message = "An error occured. Configuration of AuthenticationManager will be ignored : all 'Authentication' will be accepted";
                getLogger().warn(message, e);
            }
        }
        finally
        {
            if (configStream != null)
            {
                try
                {
                    configStream.close();
                }
                catch (IOException e)
                {
                    if (getLogger().isWarnEnabled())
                    {
                        String message = "An error occured while closing config file '" + configFileName + "'";
                        getLogger().warn(message, e);
                    }
                }
            }
            if (resolver != null)
            {
                if (configSource != null)
                {
                    resolver.release(configSource);
                }
                
                _serviceManager.release(resolver);
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
            throw new IllegalArgumentException(
                    "The authentication extension may exists but is not part of the restricted list");
        }
    }

    @Override
    public Set<String> getExtensionsIds()
    {
        return _restrictedExtensionsId != null ? _restrictedExtensionsId : super.getExtensionsIds();
    }
}
