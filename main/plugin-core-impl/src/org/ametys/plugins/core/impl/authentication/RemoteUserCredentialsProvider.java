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
package org.ametys.plugins.core.impl.authentication;

import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.authentication.CredentialsProvider;
import org.ametys.runtime.config.Config;


/**
 * This manager gets the credentials given by an authentification J2EE filter.<br>
 * The filter must set the 'remote user' header into the request.<br>
 * <br>
 * This manager can not get the password of the connected user: the user is 
 * already authentified. This manager should not be associated with an
 * <code>AuthenticableBaseUser</code>
 */
public class RemoteUserCredentialsProvider extends AbstractLogEnabled implements CredentialsProvider, Initializable, Contextualizable
{
    private String _realm;
    
    private Context _context;

    public void initialize() throws Exception
    {
        _realm = Config.getInstance().getValueAsString("runtime.authentication.remote.realm");
    }
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public boolean validate(Redirector redirector) throws Exception
    {
        // ce Manager est toujours valide
        return true;
    }

    public boolean accept()
    {
        // cette implémentation n'a pas de requête particulière à prendre en
        // compte
        return false;
    }

    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        String remoteLogin = ObjectModelHelper.getRequest(objectModel).getRemoteUser();
        if (remoteLogin == null)
        {
            getLogger().error("Remote User is null ! Missing filter ?");
            return null;
        }
        
        int begin = remoteLogin.indexOf("\\");
        if (begin <= 0)
        {
            /* Authentification de domaine mais non-conforme */
            getLogger().error("Remote User '" + remoteLogin + "' does not match realm\\login");
            return null;
        }

        String userLogin = remoteLogin.substring(begin + 1);
        String userRealm = remoteLogin.substring(0, begin);

        if (!_realm.equals(userRealm))
        {
            getLogger().error("Remote user realm '" + userRealm + "' does not match application realm '" + _realm + "'");
            return null;
        }

        return new Credentials(userLogin, "");
    }

    public void notAllowed(Redirector redirector) throws Exception
    {
        // nothing to do
    }

    public void allowed(Redirector redirector)
    {
        // empty method, nothing more to do
    }
}
