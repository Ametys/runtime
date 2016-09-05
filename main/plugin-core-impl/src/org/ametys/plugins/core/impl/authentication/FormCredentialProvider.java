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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.http.HttpCookie;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.BlockingCredentialProvider;
import org.ametys.core.authentication.Credentials;
import org.ametys.core.authentication.LogoutCapable;
import org.ametys.core.authentication.NonBlockingCredentialProvider;
import org.ametys.core.captcha.CaptchaHelper;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.plugins.core.impl.authentication.token.TokenCredentials;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * This manager gets the credentials coming from an authentication form. <br>
 * This manager can create a cookie to save credentials
 * <br>
 * Parameters are : - The name of the pool<br>
 *                  - The html field name for user name<br>
 *                  - The html field name for user password<br>
 *                  - The html field name for the check box which allow to create a cookie, must return 'true' when checked<br>
 *                  - A boolean, to activate or not the user info saving by cookie <br>
 *                  - The cookie name, to retrieve info<br>
 *                  - The cookie duration (in seconds), by default set to 1 week<br>
 *                  - A login url (do not start with a "/")<br>
 *                  - A failure login url (do not start with a "/"). The failure Url can receive the login entered by the visitor.<br>
 *                  - A list of URL prefixes that are accessible without authentication. The login and failure URLs are always accessible without authentication.<br><br>
 * 
 * For example :<br>
 *               &lt;username-field&gt;Username&lt;/username-field&gt;<br>
 *               &lt;password-field&gt;Password&lt;/password-field&gt;<br>
 *               &lt;cookie&gt;<br>
 *                   &nbsp;&nbsp;&lt;cookieEnabled&gt;true&lt;/cookieEnabled&gt;<br>
 *                   &nbsp;&nbsp;&lt;cookieLifeTime&gt;604800&lt;/cookieLifeTime&gt;<br>
 *                   &nbsp;&nbsp;&lt;cookieName&gt;AmetysAuthentication&lt;/cookieName&gt;<br>
 *               &lt;/cookie&gt;<br>
 *               &lt;loginUrl internal="true"&gt;login.html&lt;/loginUrl&gt;<br>
 *               &lt;loginFailedUrl provideLoginParameter="true" internal="true"&gt;login_failed.html&lt;/loginFailedUrl&gt;<br>
 *               &lt;unauthenticated&gt;<br>
 *                   &nbsp;&nbsp;&lt;urlPrefix&gt;subscribe.html&lt;/urlPrefix&gt;<br>
 *                   &nbsp;&nbsp;&lt;urlPrefix&gt;lostPassword/&lt;/urlPrefix&gt;<br>
 *               &lt;/unauthenticated&gt;<br>
 * 
 */
public class FormCredentialProvider extends AbstractCredentialProvider implements NonBlockingCredentialProvider, BlockingCredentialProvider, LogoutCapable, Contextualizable, Configurable
{
    /** Password value in case of info retrieved from cookie */
    public static final String AUTHENTICATION_BY_COOKIE = "authentication_by_cookie";
    /** Low security level */
    public static final String SECURITY_LEVEL_LOW = "low";
    /** High security level */
    public static final String SECURITY_LEVEL_HIGH = "high";
    /** Number of connection attempts allowed */
    public static final Integer NB_CONNECTION_ATTEMPTS = 3;
    /** Default cookie lifetime (15 days in seconds) */
    public static final int DEFAULT_COOKIE_LIFETIME = 1209600;
    
    /** Name of the parameter holding the security level */
    private static final String __PARAM_SECURITY_LEVEL = "runtime.authentication.form.security.level";
    
    /** Name of the user name html field */
    protected String _usernameField;

    /** Name of the user password html field */
    protected String _passwordField;
    
    /** Name of the "remember me" html field */
    protected String _rememberMeField;
    
    /** Name of the captcha answer html field */
    protected String _captchaField;
    
    /** Name of the captcha key html field */
    protected String _captchaKeyField;

    /** Indicates if the user credentials must be saved by a cookie */
    protected boolean _cookieEnabled;

    /** The name of the cookie */
    protected String _cookieName;

    /** Cookie duration in seconds, by default 1 week */
    protected long _cookieLifetime;

    /** Redirection in case of no login infos were found from request params or cookie */
    protected String _loginUrl;

    /** Redirection when the authentication fails */
    protected String _loginFailedUrl;

    /** When redirecting on failure send the entered login */
    protected boolean _provideLoginParameter;

    /** Indicates if the login url redirection is internal (default : false). */
    protected boolean _loginUrlInternal;

    /** Indicates if the failed login url redirection is internal or external (default : false). */
    protected boolean _loginFailedUrlInternal;

    /** Set of accepted url prefixes (default : empty). */
    protected Set<String> _acceptedUrlPrefixes;
    
    /** A list of accepted url patterns */
    protected Collection<Pattern> _acceptedUrlPatterns = Arrays.asList(new Pattern[]{Pattern.compile("^plugins/core/captcha/[^/]+/image.png")});   // captcha  

    /** The security level */
    protected String _securityLevel;
    
    /** Context */
    protected Context _context;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void init(String cpModelId, Map<String, Object> paramValues)
    {
        super.init(cpModelId, paramValues);
        _securityLevel = (String) paramValues.get(__PARAM_SECURITY_LEVEL);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _usernameField = configuration.getChild("username-field").getValue("Username");
        _passwordField = configuration.getChild("password-field").getValue("Password");
        _rememberMeField =  configuration.getChild("rememberMe-field").getValue("rememberMe");
        _captchaField =  configuration.getChild("capcha-field").getValue("Captcha");
        _captchaKeyField =  configuration.getChild("captchaKey-field").getValue("CaptchaKey");
        _cookieEnabled = configuration.getChild("cookie").getChild("cookieEnabled").getValueAsBoolean(true);
        _cookieLifetime = configuration.getChild("cookie").getChild("cookieLifeTime").getValueAsLong(604800);
        _cookieName = configuration.getChild("cookie").getChild("cookieName").getValue("AmetysAuthentication");
        _loginUrl = configuration.getChild("loginUrl").getValue("login.html");
        _loginFailedUrl = configuration.getChild("loginFailedUrl").getValue("login_failed.html");
        _provideLoginParameter = configuration.getChild("loginFailedUrl").getAttributeAsBoolean("provideLoginParameter", false);
        _loginUrlInternal = configuration.getChild("loginUrl").getAttributeAsBoolean("internal", false);
        _loginFailedUrlInternal = configuration.getChild("loginFailedUrl").getAttributeAsBoolean("internal", false);
        _acceptedUrlPrefixes = new HashSet<>();
        for (Configuration prefixConf : configuration.getChild("unauthenticated").getChildren("urlPrefix"))
        {
            String prefix = prefixConf.getValue(null);
            if (prefix != null)
            {
                _acceptedUrlPrefixes.add(prefix);
            }
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(
                    "FormBasedCredentialsProvider values : " + " Name field=" + _usernameField + ", Pwd field="
                            + _passwordField + ", CookieEnabled=" + _cookieEnabled + ", Cookie duration="
                            + _cookieLifetime + ", Cookie name=" + _cookieName + ", Login url=" + _loginUrl
                            + " [" + (_loginUrlInternal ? "internal" : "external") + "]"
                            + ", Login failed url=" + _loginFailedUrl
                            + " [" + (_loginFailedUrlInternal ? "internal" : "external")
                            + ", provide login on redirection : " + _provideLoginParameter + "]"
                            + ", accepted prefixes : [" + StringUtils.join(_acceptedUrlPrefixes, ", ") + "]");
        }
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection()
    {
        return ConnectionHelper.getConnection(Config.getInstance().getValueAsString("runtime.login.form.datasource"));
    }
    
    /**
     * Get the login url
     * @return the login url
     */
    protected String getLoginURL()
    {
        return _loginUrl;
    }
    
    /**
     * Get the login failed url
     * @return the login failed url
     */
    protected String getLoginFailedURL()
    {
        return _loginFailedUrl;
    }

    @Override
    public boolean logout(Redirector redirector)
    {
        Request request = ContextHelper.getRequest(_context);
        deleteCookie(request,  ContextHelper.getResponse(_context), _cookieName, (int) _cookieLifetime);
        return true;
    }

    @Override
    public boolean validateBlocking(Redirector redirector) throws Exception
    {
        return true;
    }
    
    @Override
    public boolean validateNonBlocking(Redirector redirector) throws Exception
    {
        return true;
    }

    @Override
    public boolean acceptBlocking()
    {
        Request request = ContextHelper.getRequest(_context);
        
        boolean accept = false;
        
        String login = request.getParameter(_usernameField);
        String password = request.getParameter(_passwordField);
        
        // URL without server context and leading slash.
        String url = request.getRequestURI();
        if (url.startsWith(request.getContextPath()))
        {
            url = url.substring(request.getContextPath().length());
        }
        if (url.startsWith("/"))
        {
            url = url.substring(1);
        }
        
        // Always accept the login failed page.
        accept = getLoginFailedURL().equals(url);
        
        // Accept the other urls only if the user didn't provide credentials
        // (if credentials are provided, the user is trying to connect). 
        if (login == null || password == null)
        {
            if (!accept)
            {
                accept = getLoginURL().equals(url);
            }
            
            if (!accept)
            {
                for (String prefix : _acceptedUrlPrefixes)
                {
                    if (url.startsWith(prefix))
                    {
                        accept = true;
                        break;
                    }
                }
            }
            
            if (!accept)
            {
                for (Pattern pattern : _acceptedUrlPatterns)
                {
                    if (pattern.matcher(url).matches())
                    {
                        accept = true;
                        break;
                    }
                }
            }
        }
        
        if (accept && getLogger().isInfoEnabled())
        {
            getLogger().info("URL accepted : " + url);
        }
        
        return accept;
    }
    
    @Override
    public boolean acceptNonBlocking()
    {
        return acceptBlocking();
    }

    @Override
    public Credentials getCredentialsBlocking(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);

        String login = request.getParameter(_usernameField);
        String password = request.getParameter(_passwordField);

        if (login != null && password != null)
        {
            if (SECURITY_LEVEL_HIGH.equals(_securityLevel))
            {
                Integer nbConnect = _requestNbConnectBDD(login);
                if (nbConnect >= NB_CONNECTION_ATTEMPTS)
                {
                    String answer = request.getParameter(_captchaField);
                    String captchaKey = request.getParameter(_captchaKeyField);

                    if (!CaptchaHelper.checkAndInvalidate(captchaKey, answer)) 
                    {
                        // Captcha is invalid
                        return null;
                    }
                }
            }
            
            return new Credentials(login, password);
        }

        String redirectUrl;
        if (_loginUrlInternal)
        {
            redirectUrl = "cocoon://" + getLoginURL();
        }
        else
        {
            redirectUrl = request.getContextPath() + "/" + getLoginURL();
        }
        redirector.redirect(false, redirectUrl);
        return null;
    }
    
    @Override
    public Credentials getCredentialsNonBlocking(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);
        
        String value = getCookieValue(request, _cookieName);
        if (StringUtils.isNotEmpty(value))
        {
            if (value.contains(","))
            {
                String [] values = value.split(",");
                return new TokenCredentials(values[0], values[1]);
            }
            else
            {
                // old cookie, delete it
                deleteCookie(request,  ContextHelper.getResponse(_context), _cookieName, (int) _cookieLifetime);
            }
        }

        return null;
    }

    @Override
    public void notAllowedBlocking(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);

        StringBuffer parameters = new StringBuffer();
        parameters.append(getLoginFailedURL().indexOf('?') >= 0 ? "&" : "?");
        
        if (_provideLoginParameter)
        {
            parameters.append("login=" + request.getParameter(_usernameField));
        }
        
        if (SECURITY_LEVEL_HIGH.equals(_securityLevel))
        {
            String captchaKey = request.getParameter(_captchaKeyField);
            int nbConnect = _setNbConnectBDD(request.getParameter(_usernameField));
            int nbAttempts = NB_CONNECTION_ATTEMPTS - 1;
            
            if (nbConnect == nbAttempts || (captchaKey == null && nbConnect > nbAttempts))
            {
                parameters.append("&tooManyAttempts=" + true);
            }
        }
        
        if (StringUtils.isNotEmpty(getCookieValue(request, _cookieName)))
        {
            parameters.append("&cookieFailure=" + true);
            deleteCookie(request, ContextHelper.getResponse(_context), _cookieName, (int) _cookieLifetime); 
        }

        String redirectUrl;
        if (_loginFailedUrlInternal)
        {
            redirectUrl = "cocoon://" + getLoginFailedURL() + parameters.toString();
        }
        else
        {
            redirectUrl = request.getContextPath() + request.getAttribute(WorkspaceMatcher.WORKSPACE_URI) + "/" + getLoginFailedURL() + parameters.toString();
        }
        redirector.redirect(false, redirectUrl);
    }
    
    @Override
    public void notAllowedNonBlocking(Redirector redirector) throws Exception
    {
        // Do nothing
    }

    @Override
    public void allowedBlocking(Redirector redirector)
    {
        if (!SECURITY_LEVEL_HIGH.equals(_securityLevel))
        {
            Request request = ContextHelper.getRequest(_context);
            
            String cookieValue = getCookieValue(request, _cookieName);
            
            String login = null;
            String rememberMe = request.getParameter(_rememberMeField);
            
            if ("true".equals(rememberMe))
            {
                login = request.getParameter(_usernameField);
            }
            else if (StringUtils.isNotEmpty(cookieValue))
            {
                login = cookieValue.split(",")[0];
            }
            
            if (login != null)
            {
                // Hash token + salt
                String token = RandomStringUtils.randomAlphanumeric(16);
                String salt = RandomStringUtils.randomAlphanumeric(48);
                String hashedTokenAndSalt = DigestUtils.sha512Hex(token + salt);
                
                _insertUserToken(login, salt, hashedTokenAndSalt);
                updateCookie(login + "," + token, _cookieName, (int) _cookieLifetime, _context);
            }
        }
        else
        {
            Request request = ContextHelper.getRequest(_context);
            String login = request.getParameter(_usernameField);

            _deleteLoginFailedBDD(login);
            
        }
    }
    
    @Override
    public void allowedNonBlocking(Redirector redirector)
    {
        allowedBlocking(redirector);
    }
    
    /**
     * Get the number of failed connections with this login
     * @param login The login to request
     * @return nthe nb of connection failed
     */
    protected Integer _requestNbConnectBDD(String login)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            // Build request for authenticating the user
            String sql = "SELECT nb_connect FROM Users_FormConnectionFailed WHERE login = ?";
            
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);
    
            // Do the request
            rs = stmt.executeQuery();
    
            Integer nbConnect = 0;
            if (rs.next()) 
            {
                nbConnect = rs.getInt("nb_connect"); 
            }
            return nbConnect;
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
            return 0;
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /**
     * Get the number of failed connections with this login
     * @param login The login to set
     * @return the number of failed connection
     */
    protected Integer _setNbConnectBDD(String login)
    {
        Integer nbConnect = _requestNbConnectBDD(login);
        if (nbConnect == 0)
        {
            _insertLoginNbConnectBDD(login);
        }
        else
        {
            _updateLoginNbConnectBDD(login, nbConnect);
        }
        
        return nbConnect;
        
    }
    
    /**
     * Insert the login with one failed connection in the BDD
     * @param login The login to insert
     */
    protected void _insertLoginNbConnectBDD(String login)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sqlUpdate = "INSERT INTO Users_FormConnectionFailed (login, nb_connect, last_connect) VALUES (?, ?, ?)";
           
            stmt = connection.prepareStatement(sqlUpdate);
            stmt.setString(1, login);
            stmt.setInt(2, 1);
            
            DateTime dateToday = new DateTime(); 
            
            Timestamp date = new Timestamp(dateToday.getMillis());
            stmt.setTimestamp(3, date);
            
            stmt.execute();
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /**
     * Inserts a new line into the users token table
     * @param login the login of the user
     * @param salt the salt associated to this user
     * @param hashedTokenAndSalt token + salt hashed with SHA-512
     */
    protected void _insertUserToken(String login, String salt, String hashedTokenAndSalt)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            String dbType = ConnectionHelper.getDatabaseType(connection);
            
            if (ConnectionHelper.DATABASE_ORACLE.equals(dbType))
            {
                statement = connection.prepareStatement("SELECT seq_userstoken.nextval FROM dual");
                rs = statement.executeQuery();
                
                String id = null;
                if (rs.next())
                {
                    id = rs.getString(1);
                }
                ConnectionHelper.cleanup(rs);
                ConnectionHelper.cleanup(statement);
                
                statement = connection.prepareStatement("INSERT INTO UsersToken (id, login, token, salt, creation_date) VALUES (?, ?, ?, ?, ?)");
                statement.setString(1, id);
                statement.setString(2, login);
                statement.setString(3, hashedTokenAndSalt);
                statement.setString(4, salt);
                statement.setDate(5, new java.sql.Date(System.currentTimeMillis()));
            }
            else
            {
                statement = connection.prepareStatement("INSERT INTO UsersToken (login, token, salt, creation_date) VALUES (?, ?, ?, ?)");
                
                statement.setString(1, login);
                statement.setString(2, hashedTokenAndSalt);
                statement.setString(3, salt);
                statement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            }
            
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            getLogger().error("Communication error with the database", e);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);       
            ConnectionHelper.cleanup(connection);
        }
    }
    /**
     * Delete the login from the table of the failed connection
     * @param login The login to remove
     */
    protected void _deleteLoginFailedBDD(String login)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            // Build request for authenticating the user
            String sql = "DELETE FROM Users_FormConnectionFailed WHERE login = ?";
            
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);
            
            // Do the request
            stmt.execute();
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
        
    }
    
    /**
     * Update the number of failed connections of the login in the BDD
     * @param login The login to update
     * @param nbConnect The nb of connection to set
     */
    protected void _updateLoginNbConnectBDD(String login, Integer nbConnect)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sqlUpdate = "UPDATE Users_FormConnectionFailed SET nb_connect = ? WHERE login = ?";
           
            stmt = connection.prepareStatement(sqlUpdate);
            stmt.setInt(1, nbConnect + 1);
            stmt.setString(2, login);
            
            stmt.execute();
        }
        catch (SQLException e)
        {
            getLogger().error("Error during the connection to the database", e);
        }
        finally
        {
            // Close connections
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /************************************************************************************************
     *    COOKIE EXCHANGES MANAGEMENT
     */
     
     /**
      * Return the cookie value corresponding to the searched name
      * @param request the request
      * @param cookieSearchedName the cookie name
      * @return the value of the cookie or null if not
      */
    public static String getCookieValue(Request request, String cookieSearchedName)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookieSearchedName.equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

     /**
      * Checks if cookie already exists
      * @param request the request
      * @param cookieSearchedName the cookie name
      * @return boolean
      */
    public static boolean isCookieAlreadySet(Request request, String cookieSearchedName)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookieSearchedName.equals(cookie.getName()))
                {
                    return true;
                }
            }
        }

        return false;
    }
     
     /**
      * Update the cookie for client-side purpose
      * @param value the cookie value
      * @param cookieName the cookie name
      * @param cookieDuration the cookie duration
      * @param context the avalon Context
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
      * @param request the request
      * @param response the response
      * @param cookieName the cookie name 
      * @param cookieDuration the cookie duration
      */
    public static void deleteCookie(Request request, Response response, String cookieName, int cookieDuration)
    {
        Cookie cookie = new HttpCookie(cookieName, "");
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(cookieDuration);
        response.addCookie(cookie);
    }

}
