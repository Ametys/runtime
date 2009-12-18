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
package org.ametys.runtime.plugins.core.authentication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import jcifs.http.NtlmHttpFilter;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.servlet.CocoonServlet;

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
public class JcifsCredentialsProvider extends RemoteUserCredentialsProvider
{
    private RuntimeFilter _filter;
    private Context _avalonContext;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _avalonContext = context;
        super.contextualize(context);
    }
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
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

        ServletConfig servletConfig = (ServletConfig) _avalonContext.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        ServletContext servletContext = servletConfig.getServletContext(); 
        
        RuntimeFilter runtimeFilter = new RuntimeFilter(new NtlmHttpFilter());
        runtimeFilter.init(parameters, servletContext);
        _filter = runtimeFilter;
    }
    
    @Override
    public boolean validate(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_avalonContext);
        _filter.doFilter(objectModel, redirector);
        return super.validate(redirector);
    }
}
