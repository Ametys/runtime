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

import java.util.Map;

import org.apache.cocoon.environment.Redirector;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.NonBlockingCredentialProvider;
import org.ametys.core.user.UserIdentity;

/**
 * This implementation will always provide the same credentials
 */
public class DefinedCredentialProvider extends AbstractCredentialProvider implements NonBlockingCredentialProvider 
{
    /** Name of the parameter holding the defined user */
    private static final String __PARAM_USER = "runtime.authentication.defined.user";
    
    /** The credentials */
    private UserIdentity _userIdentity;
    
    @Override
    public void init(String cpModelId, Map<String, Object> paramValues)
    {
        super.init(cpModelId, paramValues);
        String login = (String) paramValues.get(__PARAM_USER);
        _userIdentity = new UserIdentity(login, null);
    }

    @Override
    public boolean nonBlockingIsStillConnected(UserIdentity userIdentity, Redirector redirector) throws Exception
    {
        return true;
    }

    @Override
    public boolean nonBlockingGrantAnonymousRequest()
    {
        return false;
    }

    @Override
    public UserIdentity nonBlockingGetUserIdentity(Redirector redirector) throws Exception
    {
        return _userIdentity;
    }

    @Override
    public void nonBlockingUserNotAllowed(Redirector redirector) throws Exception
    {
        // nothing
    }

    @Override
    public void nonBlockingUserAllowed(UserIdentity userIdentity)
    {
        // nothing
    }
}
