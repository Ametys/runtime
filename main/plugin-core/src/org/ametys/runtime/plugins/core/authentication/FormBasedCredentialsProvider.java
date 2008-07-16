package org.ametys.runtime.plugins.core.authentication;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.authentication.CredentialsProvider;
import org.ametys.runtime.workspace.WorkspaceMatcher;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.http.HttpCookie;

/**
 * This manager gets the credentials coming from an authentification form. <br>
 * This manager can create a cookie to save credentials
 * <br>
 * Parameters are : - The html field name for user name<br>
 *                  - The html field name for user password<br>
 *                  - The html field name for the check box which allow to create a cookie, must return 'true' when checked<br>
 *                  - A boolean, to activate or not the user info saving by cookie <br>
 *                  - The cookie name, to retrieve info<br>
 *                  - The cookie duration (in seconds), by default set to 1 week<br>
 *                  - A login url (do not start with a "/")<br>
 *                  - A failure login url (do not start with a "/")<br><br>
 * 
 * For example :<br>
 *               &lt;username-field&gt;Username&lt;/username-field&gt;<br>
 *               &lt;password-field&gt;Password&lt;/password-field&gt;<br>
 *               &lt;cookie&gt;<br>
 *                   &lt;cookieEnabled&gt;true&lt;/cookieEnabled&gt;<br>
 *                   &lt;cookieLifeTime&gt;604800&lt;/cookieLifeTime&gt;<br>
 *                   &lt;cookieName&gt;AmetysAuthentication&lt;/cookieName&gt;<br>
 *               &lt;/cookie&gt;<br>
 *               &lt;loginUrl&gt;login.html&lt;/loginUrl&gt;<br>
 *               &lt;loginFailedUrl&gt;login_failed.html&lt;/loginFailedUrl&gt;<br>
 * 
 */
public class FormBasedCredentialsProvider extends AbstractLogEnabled implements ThreadSafe, CredentialsProvider,
        Configurable, Contextualizable
{
    /** Password value in case of info retrieved from cookie */
    public static final String AUTHENTICATION_BY_COOKIE = "authentication_by_cookie";
    
    /** Name of the user name html field */
    protected String _usernameField;

    /** Name of the user password html field */
    protected String _passwordField;
    
    /** Name of the "remember me" html field */
    protected String _rememberMeField;

    /** Indicates if the user credentials must be saved by a cookie */
    protected boolean _cookieEnabled;

    /** The name of the cookie */
    protected String _cookieName;

    /** Cookie duration in seconds, by default 1 week */
    protected long _cookieLifetime;

    /** Redirection in case of none login infos found from request params or cookie */
    protected String _loginUrl;

    /** Redirection when the authentication fails */
    protected String _loginFailedUrl;

    /** Context */
    protected Context _context;

    
    public boolean accept()
    {
        return false;
    }

    
    public void allowed(Redirector redirector)
    {
        Request request = ContextHelper.getRequest(_context);
        
        String value = getCookieValue(request, _cookieName);
        if (value != null && !"".equals(value))
        {
            updateCookie(value, _cookieName, (int) _cookieLifetime, _context);
        }
        else
        {
            String login = request.getParameter(_usernameField);
            String password = request.getParameter(_passwordField);
            String rememberMe = request.getParameter(_rememberMeField);
            if (rememberMe != null)
            {
                if (rememberMe.equalsIgnoreCase("true"))
                {
                    updateCookie(login + "/n" + password, _cookieName, (int) _cookieLifetime, _context);
                }
            }
        }
    }

    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);

        String value = getCookieValue(request, _cookieName);
        if (value != null && !"".equals(value))
        {
            String [] values = value.split("/n");
            return new Credentials(values[0], values[1]);
        }

        String login = request.getParameter(_usernameField);
        String password = request.getParameter(_passwordField);

        if (login != null && password != null)
        {
            return new Credentials(login, password);
        }

        redirector.redirect(false, request.getContextPath() + "/" + _loginUrl);
        return null;
    }

    public void notAllowed(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);

        redirector.redirect(false, request.getContextPath() + request.getAttribute(WorkspaceMatcher.WORKSPACE_URI) + "/" + _loginFailedUrl);
    }

    public boolean validate(Redirector redirector) throws Exception
    {
        return true;
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        _usernameField = configuration.getChild("username-field").getValue("Username");
        _passwordField = configuration.getChild("password-field").getValue("Password");
        _rememberMeField =  configuration.getChild("rememberMe-field").getValue("rememberMe");
        _cookieEnabled = configuration.getChild("cookie").getChild("cookieEnabled").getValueAsBoolean(true);
        _cookieLifetime = configuration.getChild("cookie").getChild("cookieLifeTime").getValueAsLong(604800);
        _cookieName = configuration.getChild("cookie").getChild("cookieName").getValue("AmetysAuthentication");
        _loginUrl = configuration.getChild("loginUrl").getValue("login.html");
        _loginFailedUrl = configuration.getChild("loginFailedUrl").getValue("login_failed.html");
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(
                    "FormBasedCredentialsProvider values : " + " Name field=" + _usernameField + ", Pwd field="
                            + _passwordField + ", CookieEnabled=" + _cookieEnabled + ", Cookie duration="
                            + _cookieLifetime + ", Cookie name=" + _cookieName + ", Login url=" + _loginUrl
                            + ", Login failed url=" + _loginFailedUrl);
        }
    }

    public void contextualize(Context context) throws ContextException
    {
        this._context = context;
    }
    
    
    
   /************************************************************************************************
    *    COOKIE EXCHANGES MANAGEMENT
    */
    
    /**
     * Return the cookie value corresponding to the searched name
     * 
     * @param request
     * @param cookieSearchedName
     * @return the value of the cookie or null if not
     */
    public static String getCookieValue(Request request, String cookieSearchedName)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                if (cookieSearchedName.equals(cookies[i].getName()))
                {
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }

    /**
     * Checks if cookie already exists
     * @param request
     * @param cookieSearchedName
     * @return boolean
     */
    public static boolean isCookieAlreadySet(Request request, String cookieSearchedName)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                if (cookieSearchedName.equals(cookies[i].getName()))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Update the cookie for client-side purpose
     * 
     * @param value
     * @param cookieName
     * @param cookieDuration
     * @param context
     */
    public static void updateCookie(String value, String cookieName, int cookieDuration, Context context)
    {
        Response response = ObjectModelHelper.getResponse(ContextHelper.getObjectModel(context));
        Request request = ObjectModelHelper.getRequest(ContextHelper.getObjectModel(context));
        Cookie cookie = new HttpCookie(cookieName, value);
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(cookieDuration);
        response.addCookie(cookie);
    }

    
    /**
     * Delete the cookie
     * 
     * @param request
     * @param response
     * @param cookieName
     * @param cookieDuration
     */
    public static void deleteCookie(Request request, Response response, String cookieName, int cookieDuration)
    {
        Cookie cookie = new HttpCookie(cookieName, "");
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(cookieDuration);
        response.addCookie(cookie);
    }


}
