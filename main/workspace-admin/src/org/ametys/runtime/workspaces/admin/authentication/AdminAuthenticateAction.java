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
package org.ametys.runtime.workspaces.admin.authentication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.codec.binary.Base64;
import org.xml.sax.InputSource;

import org.ametys.runtime.authentication.BasicCredentialsProvider;
import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.authentication.CredentialsProvider;
import org.ametys.runtime.user.User;

/**
 * Cocoon action for authenticating users in the administration workspace. 
 * Authentication is based on the file ADMINISTRATOR_PASSWORD_FILENAME which contains the
 * MD5 encrypted password.
 */
public class AdminAuthenticateAction extends AbstractAction implements ThreadSafe, Contextualizable, Initializable
{
    /** The request attribute name for telling that super user is logged in. */
    public static final String REQUEST_ATTRIBUTE_SUPER_USER = "Runtime:SuperUser";
    /** Location (from webapplication context) of the administrator password */
    public static final String ADMINISTRATOR_PASSWORD_FILENAME = "/WEB-INF/data/administrator/admin.xml";
    
    private static final String __SESSION_ADMINISTRATOR = "Runtime:Administrator";
    
    /** The cocoon context, initialized during the contextualize method */
    protected org.apache.avalon.framework.context.Context _context;

    /** The environment context */
    protected Context _envContext;

    private CredentialsProvider _credentialsProvider;

    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        _context = context;
        _envContext = (Context) _context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    public void initialize() throws Exception
    {
        BasicCredentialsProvider basicProvider = new BasicCredentialsProvider("Administration", _context);

        _credentialsProvider = basicProvider;
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (!_checkAuth(objectModel, redirector))
        {
            return null;
        }

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(REQUEST_ATTRIBUTE_SUPER_USER, Boolean.TRUE);
        
        return EMPTY_MAP;
    }

    private boolean _checkAuth(Map objectModel, Redirector redirector) throws Exception
    {
        // If user already registered, accept it.
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        User registeredUP = (User) request.getSession().getAttribute(__SESSION_ADMINISTRATOR);
        if (registeredUP != null)
        {
            // User already authenticated
            return true;
        }

        // Utilisateur inconnu
        // On recherche ses identifiants
        Credentials credentials = _credentialsProvider.getCredentials(redirector);

        // On teste son authentification
        if (credentials == null || !_allowUser(credentials))
        {
            _credentialsProvider.notAllowed(redirector);
            return false;
        }

        // L'authentification a réussi
        // On laisse une dernière fois la main au manager
        _credentialsProvider.allowed(redirector);

        // Et on enregistre l'utilisateur dans la session
        Session session = request.getSession(true);
        session.setAttribute(__SESSION_ADMINISTRATOR, new User("admin"));

        return true;
    }

    private boolean _allowUser(Credentials credentials)
    {
        String login = credentials.getLogin();
        String passwd = credentials.getPassword();

        try
        {
            if (!"admin".equals(login))
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("The administrator login must be 'admin' => authentication failed");
                }
                return false;
            }

            if (passwd == null)
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("The administrator password cannot be null => authentication failed");
                }
                return false;
            }

            try (InputStream is = new FileInputStream(_envContext.getRealPath(ADMINISTRATOR_PASSWORD_FILENAME)))
            {
                XPath xpath = XPathFactory.newInstance().newXPath();
                String pass = xpath.evaluate("admin/password", new InputSource(is));
                if (pass == null || "".equals(pass))
                {
                    if (getLogger().isWarnEnabled())
                    {
                        getLogger().warn("The administrator password cannot be null at reading => authentication failed");
                    }
                    return false;
                }

                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte[] encryptedPasswd = messageDigest.digest(passwd.getBytes());

                if (!MessageDigest.isEqual(Base64.decodeBase64(pass.getBytes()), encryptedPasswd))
                {
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("The user did not give the right password => authentication failed");
                    }
                    return false;
                }

                return true;
            }
            catch (FileNotFoundException e)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("The file '" + ADMINISTRATOR_PASSWORD_FILENAME + "' is missing. Default administrator password 'admin' is used.", e);
                }
                return "admin".equals(passwd);
            }
        }
        catch (Exception e)
        {
            getLogger().error("Authentication failed", e);
            return false;
        }
    }
}
