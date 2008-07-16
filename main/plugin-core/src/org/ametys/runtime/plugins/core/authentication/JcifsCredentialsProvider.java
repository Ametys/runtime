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
package org.ametys.runtime.plugins.core.authentication;

import java.util.HashMap;
import java.util.Map;

import jcifs.http.NtlmHttpFilter;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Redirector;

import org.ametys.runtime.authentication.filter.RuntimeFilter;
import org.ametys.runtime.config.Config;


/**
 * This manager is aimed to be used with JCIFS - for windows NT authentication -.
 * <br>
 * This class extends the manager RemoteUserAuthenticationManager. <br>
 * This manager can not get the password of the connected user: the user is
 * already authentified. This manager should not be associated with an
 * <code>AuthenticableBaseUser</code>
 */
public class JcifsCredentialsProvider extends RemoteUserCredentialsProvider implements Contextualizable
{
    private RuntimeFilter _filter;
    private Context _avalonContext;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _avalonContext = context;
        super.contextualize(context);
    }
    
    private void _lazyInitialize() throws Exception
    {
        if (_filter != null)
        {
            return;
        }
        
        Map<String, String> parameters = new HashMap<String, String>();

        boolean valueProd = Config.getInstance().getValueAsBoolean("runtime.authentication.jcifs.production");
        if (valueProd)
        {
            String clientDomain = Config.getInstance().getValueAsString("runtime.authentication.jcifs.clientDomain");
            String netBios = Config.getInstance().getValueAsString("runtime.authentication.jcifs.netBios");

            parameters.put("jcifs.smb.client.domain", clientDomain);
            parameters.put("jcifs.netbios.wins", netBios);
        }
        else
        {
            String domainController = Config.getInstance().getValueAsString("runtime.authentication.jcifs.domainController");

            parameters.put("jcifs.http.domainController", domainController);
        }

        // création du filtre
        Map objectModel = ContextHelper.getObjectModel(_avalonContext);
        
        RuntimeFilter runtimeFilter = new RuntimeFilter(new NtlmHttpFilter());
        runtimeFilter.init(parameters, objectModel);
        _filter = runtimeFilter;
    }
    
    @Override
    public boolean validate(Redirector redirector) throws Exception
    {
        _lazyInitialize();
        
        Map objectModel = ContextHelper.getObjectModel(_avalonContext);
        _filter.doFilter(objectModel, redirector);
        return super.validate(redirector);
    }
}
