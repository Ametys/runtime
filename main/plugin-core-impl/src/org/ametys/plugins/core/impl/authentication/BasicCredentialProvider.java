/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.plugins.core.impl.authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.authentication.BlockingCredentialProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.runtime.authentication.AuthorizationRequiredException;

/**
 * Basic http authentication.
 */
public class BasicCredentialProvider extends AbstractCredentialProvider implements BlockingCredentialProvider, Contextualizable
{
    /** Name of the parameter holding the authentication realm */
    protected static final String __PARAM_REALM = "runtime.authentication.basic.realm";
    
    private static final String BASIC_AUTHENTICATION_KEY = "BASIC ";

    /** The realm */
    protected String _realm;
    
    private Context _context;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void init(String id, String cpModelId, Map<String, Object> paramValues, String label)
    {
        super.init(id, cpModelId, paramValues, label);
        _realm = (String) paramValues.get(__PARAM_REALM);
    }
    
    @Override
    public boolean blockingIsStillConnected(UserIdentity userIdentity, Redirector redirector) throws Exception
    {
        // this manager is always valid
        return true;
    }
    
    @Override
    public boolean blockingGrantAnonymousRequest()
    {
        // this implementation does not have any particular request
        // to take into account
        return false;
    }

    @Override
    public UserIdentity blockingGetUserIdentity(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);
        
        // Check authentication header
        String auth = request.getHeader("Authorization");
        if (auth == null)
        {
            // no auth
            throw new AuthorizationRequiredException(_realm);
        }

        if (!auth.toUpperCase().startsWith(BASIC_AUTHENTICATION_KEY))
        {
            // we only do BASIC
            return null;
        }

        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = auth.substring(BASIC_AUTHENTICATION_KEY.length());

        // Decode it, using any base 64 decoder
        String userpassDecoded = new String(Base64.decodeBase64(userpassEncoded.getBytes("UTF-8")), "UTF-8");

        // Login and password are separated with a :
        StringTokenizer stk = new StringTokenizer(userpassDecoded, ":");
        String login = stk.hasMoreTokens() ? stk.nextToken() : "";
        String password = stk.hasMoreTokens() ? stk.nextToken() : "";

        // Let's check password
        List<UserPopulation> userPopulations = _getPopulations(request);
        for (UserPopulation userPopulation : userPopulations)
        {
            for (UserDirectory userDirectory : userPopulation.getUserDirectories())
            {
                if (userDirectory.getUser(login) != null)
                {
                    if (userDirectory.checkCredentials(login, password))
                    {
                        return new UserIdentity(login, userPopulation.getId());
                    }
                    else
                    {
                        throw new AuthorizationRequiredException(_realm);
                    }
                }
            }
        }
        
        throw new AuthorizationRequiredException(_realm);
    }
    
    private List<UserPopulation> _getPopulations(Request request)
    {
        // With forms, the population should always be known!
        @SuppressWarnings("unchecked")
        List<UserPopulation> userPopulations = (List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST);

        // In this list a population was maybe chosen?
        final String chosenUserPopulationId = (String) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_USER_POPULATION_ID);
        if (StringUtils.isNotBlank(chosenUserPopulationId))
        {
            UserPopulation chosenUserPopulation = userPopulations.stream().filter(userPopulation -> StringUtils.equals(userPopulation.getId(), chosenUserPopulationId)).findFirst().get();
            return Collections.singletonList(chosenUserPopulation);
        }
        
        // If the list has one element only...
        return userPopulations;
    }

    @Override
    public void blockingUserNotAllowed(Redirector redirector) throws Exception
    {
        // empty method, nothing more to do
    }

    @Override
    public void blockingUserAllowed(UserIdentity userIdentity)
    {
        // empty method, nothing more to do
    }

    @Override
    public boolean requiresNewWindow()
    {
        return false;
    }
}
