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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.NonBlockingCredentialProvider;
import org.ametys.core.user.UserIdentity;

/**
 * This manager gets the credentials given by a J2EE filter authentication.<br>
 * The filter must set the 'remote user' header into the request.<br>
 * <br>
 * This manager can not get the password of the connected user: the user is 
 * already authentified. This manager should not be associated with an
 * <code>AuthenticableBaseUser</code>
 */
public class RemoteUserCredentialProvider extends AbstractCredentialProvider implements NonBlockingCredentialProvider, Contextualizable
{
    /** Name of the parameter holding the authentication realm */
    private static final String __PARAM_REALM = "runtime.authentication.remote.realm";
    
    /** Name of the parameter holding the header name */
    private static final String __PARAM_HEADER_NAME = "runtime.authentication.remote.header.name";

    /** The realm */
    protected String _realm;

    /** The header name */
    protected String _headerName;
    
    private Context _context;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void init(String cpModelId, Map<String, Object> paramValues)
    {
        super.init(cpModelId, paramValues);
        _realm = (String) paramValues.get(__PARAM_REALM);
        _headerName = (String) paramValues.get(__PARAM_HEADER_NAME);
    }

    @Override
    public boolean nonBlockingIsStillConnected(UserIdentity userIdentity, Redirector redirector) throws Exception
    {
        // this manager is always valid
        return true;
    }

    @Override
    public boolean nonBlockingGrantAnonymousRequest()
    {
        // this implementation does not have any particular request
        // to take into account
        return false;
    }

    @Override
    public UserIdentity nonBlockingGetUserIdentity(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        String remoteLogin = ObjectModelHelper.getRequest(objectModel).getHeader(_headerName);
        if (remoteLogin == null)
        {
            getLogger().error("Remote User is null! Missing filter?");
            return null;
        }

        if (StringUtils.isNotBlank(_realm))
        {
            int begin = remoteLogin.indexOf("\\");
            if (begin <= 0)
            {
                /* Domain authentication but non compliant */
                getLogger().error("Remote User '{}' does not match realm\\login", remoteLogin);
                return null;
            }
    
            String userLogin = remoteLogin.substring(begin + 1);
            String userRealm = remoteLogin.substring(0, begin);
            
            if (!_realm.equals(userRealm))
            {
                getLogger().error("Remote user realm '{}' does not match application realm '{}'", userRealm, _realm);
                return null;
            }
            
            return new UserIdentity(userLogin, null);
        }
        else
        {
            return new UserIdentity(remoteLogin, null);
        }


    }

    @Override
    public void nonBlockingUserNotAllowed(Redirector redirector) throws Exception
    {
        // nothing to do
    }

    @Override
    public void nonBlockingUserAllowed(UserIdentity userIdentity)
    {
        // nothing to do
    }
}
