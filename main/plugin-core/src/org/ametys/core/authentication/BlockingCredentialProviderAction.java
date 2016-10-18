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
package org.ametys.core.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.user.population.UserPopulation;
import org.ametys.runtime.authentication.AccessDeniedException;

/**
 * This action will authenticate upon a parametrized blocking credential provider
 */
public class BlockingCredentialProviderAction extends AuthenticateAction
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        List<UserPopulation> chosenUserPopulations = new ArrayList<>();
        List<CredentialProvider> credentialProviders = new ArrayList<>();
        if (!_prepareUserPopulationsAndCredentialProviders(request, parameters, null, chosenUserPopulations, credentialProviders))
        {
            // The population was not determined (session expired?), so let's finish... that will close the popup and reload to restart the authentication process
            return EMPTY_MAP;
        }

        int credentialProviderIndex = Integer.parseInt(source);
        CredentialProvider credentialProvider = credentialProviders.get(credentialProviderIndex);
        
        if (_process(request, true, credentialProvider, redirector, chosenUserPopulations))
        {
            // Whatever the user was correctly authenticated or he just required a redirect: let's stop here for the moment
            return EMPTY_MAP;
        }
        
        throw new AccessDeniedException();
    }
    
    @Override
    protected List<String> _getContexts(Request request, Parameters parameters)
    {
        String contextAsString = request.getParameter("contexts");
        return Arrays.asList(StringUtils.split(contextAsString, ","));
    }
}
