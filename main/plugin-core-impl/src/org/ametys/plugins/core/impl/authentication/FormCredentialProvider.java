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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
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
import org.apache.excalibur.source.SourceResolver;
import org.joda.time.DateTime;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.authentication.BlockingCredentialProvider;
import org.ametys.core.authentication.LogoutCapable;
import org.ametys.core.authentication.NonBlockingCredentialProvider;
import org.ametys.core.captcha.CaptchaHelper;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.script.SQLScriptHelper;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.authentication.AccessDeniedException;
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
public class FormCredentialProvider extends AbstractCredentialProvider implements NonBlockingCredentialProvider, BlockingCredentialProvider, LogoutCapable, Contextualizable, Configurable, Serviceable
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
    public static final int COOKIE_LIFETIME = 1209600;
    /** Duration in days a connection failure will last */
    protected static final Integer TIME_ALLOWED = 1;
    
    /** Name of the parameter holder the datasource id */
    private static final String __PARAM_DATASOURCE = "runtime.authentication.form.security.storage";
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

    /** Set of accepted url prefixes (default : empty). */
    protected Set<String> _acceptedUrlPrefixes;
    
    /** A list of accepted url patterns */
    protected Collection<Pattern> _acceptedUrlPatterns = Arrays.asList(new Pattern[]{Pattern.compile("^plugins/core/captcha/[^/]+/image.png")});   // captcha  

    /** The security level */
    protected String _securityLevel;
    
    /** Context */
    protected Context _context;
    
    /** The user population DAO */
    protected UserPopulationDAO _userPopulationDAO;
    
    /** The datasource id */
    protected String _datasourceId;
    /** The avalon source resolver */
    protected SourceResolver _sourceResolver;
    
    /** was lazy initialize done */
    protected boolean _lazyInitialized;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
        
        // Ensure statics methods will be available during initialize
        manager.lookup(ConnectionHelper.ROLE);
    }
    
    @Override
    public void init(String cpModelId, Map<String, Object> paramValues, String label)
    {
        super.init(cpModelId, paramValues, label);
        _securityLevel = (String) paramValues.get(__PARAM_SECURITY_LEVEL);
        _datasourceId = (String) paramValues.get(__PARAM_DATASOURCE);
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
                            + _cookieLifetime + ", Cookie name=" + _cookieName
                            + ", accepted prefixes : [" + StringUtils.join(_acceptedUrlPrefixes, ", ") + "]");
        }
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection()
    {
        if (!_lazyInitialized)
        {
            try
            {
                if (SECURITY_LEVEL_LOW.equals(_securityLevel))
                {
                    SQLScriptHelper.createTableIfNotExists(_datasourceId, "Users_Token", "plugin:core://scripts/%s/users_token.sql", _sourceResolver);
                }
                else
                {
                    SQLScriptHelper.createTableIfNotExists(_datasourceId, "Users_FormConnectionFailed", "plugin:core://scripts/%s/users_form_failed_connection.sql", _sourceResolver);
                }
            }
            catch (Exception e)
            {
                getLogger().error("The tables requires by the " +  this.getClass().getName() + " could not be created. A degraded behavior will occur", e);
            }
            
            _lazyInitialized = true;
        }
        
        return ConnectionHelper.getConnection(_datasourceId);
    }
    
    @Override
    public void logout(Redirector redirector)
    {
        Request request = ContextHelper.getRequest(_context);
        deleteCookie(request,  ContextHelper.getResponse(_context), _cookieName);
    }

    
    @Override
    public boolean nonBlockingIsStillConnected(UserIdentity userIdentity, Redirector redirector)
    {
        return true;
    }
    
    @Override
    public boolean blockingIsStillConnected(UserIdentity userIdentity, Redirector redirector)
    {
        return true;
    }

    @Override
    public boolean blockingGrantAnonymousRequest()
    {
        Request request = ContextHelper.getRequest(_context);
        
        boolean accept = false;
        
        String login = request.getParameter(_usernameField);
        String password = request.getParameter(_passwordField);
        
        // URL without server context and leading slash.
        String url = (String) request.getAttribute(WorkspaceMatcher.IN_WORKSPACE_URL);
        
        // Accept the other urls only if the user didn't provide credentials
        // (if credentials are provided, the user is trying to connect). 
        if (login == null || password == null)
        {
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
    public boolean nonBlockingGrantAnonymousRequest()
    {
        return blockingGrantAnonymousRequest();
    }

    @Override
    public UserIdentity blockingGetUserIdentity(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);

        try
        {
            UserIdentity userIdentity = _getUserIdentityFromRequest(request);
            if (userIdentity != null)
            {
                return userIdentity;
            }
            
            // The general login screen will display the form for us
            redirector.redirect(false, (String) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_LOGIN_URL));
            
            return null;
        }
        catch (AccessDeniedException e)
        {
            blockingUserNotAllowed(redirector);
            return null;
        }
    }
    
    @Override
    public UserIdentity nonBlockingGetUserIdentity(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);
        
        try
        {
            UserIdentity userIdentity = _getUserIdentityFromRequest(request);
            if (userIdentity != null)
            {
                return userIdentity;
            }
    
            if (SECURITY_LEVEL_LOW.equals(_securityLevel))
            {
                String value = getCookieValue(request, _cookieName);
                if (StringUtils.isNotEmpty(value))
                {
                    String [] values = value.split(",");
                    if (values.length == 3)
                    {
                        if (checkToken(values[0], values[1], values[2]))
                        {
                            return new UserIdentity(values[1], values[0]);
                        }
                    }
                    else
                    {
                        // old cookie, delete it
                        deleteCookie(request,  ContextHelper.getResponse(_context), _cookieName);
                    }
                }
            }
        }
        catch (AccessDeniedException e)
        {
            nonBlockingUserNotAllowed(redirector);
            return null;
        }

        return null;
    }
    
    private UserIdentity _getUserIdentityFromRequest(Request request) throws AccessDeniedException
    {

        String login = request.getParameter(_usernameField);
        String password = request.getParameter(_passwordField);

        if (StringUtils.isNotBlank(login) && password != null)
        {
            UserPopulation userPopulation = _getPopulation(request);
            
            if (SECURITY_LEVEL_HIGH.equals(_securityLevel))
            {
                Integer nbConnect = requestNbConnectBDD(login, userPopulation.getId());
                if (nbConnect >= NB_CONNECTION_ATTEMPTS)
                {
                    String answer = request.getParameter(_captchaField);
                    String captchaKey = request.getParameter(_captchaKeyField);

                    if (!CaptchaHelper.checkAndInvalidate(captchaKey, answer)) 
                    {
                        // Captcha is invalid
                        throw new AccessDeniedException("Captcha is invalid for user '" + login + "'");
                    }
                }
            }
            
            // Let's check password
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
                        throw new AccessDeniedException("Password is incorrect for user '" + login + "'");
                    }
                }
            }
            
            // User does not exists in any directory
            throw new AccessDeniedException("Unknown user '" + login + "'");
        }
        
        return null;
    }

    private UserPopulation _getPopulation(Request request)
    {
        // With forms, the population should always be known!
        @SuppressWarnings("unchecked")
        List<UserPopulation> userPopulations = (List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST);

        // If the list has one element only...
        if (userPopulations.size() == 1)
        {
            return userPopulations.get(0);
        }
        
        // In this list a population was maybe chosen?
        final String chosenUserPopulationId = (String) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_USER_POPULATION_ID);
        if (StringUtils.isNotBlank(chosenUserPopulationId))
        {
            UserPopulation chosenUserPopulation = userPopulations.stream().filter(userPopulation -> StringUtils.equals(userPopulation.getId(), chosenUserPopulationId)).findFirst().get();
            return chosenUserPopulation;
        }
        
        // Can not work here...
        throw new IllegalStateException("The " + this.getClass().getName() + " does not work when population is not known");
    }

    @Override
    public void blockingUserNotAllowed(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);

        String url = (String) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_LOGIN_URL);
        StringBuilder parameters = new StringBuilder();
        parameters.append(url.indexOf('?') >= 0 ? "&" : "?");
        parameters.append("login=");
        parameters.append(request.getParameter(_usernameField));
        parameters.append("&authFailure=true");

        if (SECURITY_LEVEL_HIGH.equals(_securityLevel))
        {
            String captchaKey = request.getParameter(_captchaKeyField);
            int nbConnect = _setNbConnectBDD(request.getParameter(_usernameField), _getPopulation(request).getId());
            int nbAttempts = NB_CONNECTION_ATTEMPTS - 1;
            
            if (nbConnect == nbAttempts || (captchaKey == null && nbConnect > nbAttempts))
            {
                parameters.append("&tooManyAttempts=" + true);
            }
        }
        
        if (StringUtils.isNotEmpty(getCookieValue(request, _cookieName)))
        {
            parameters.append("&cookieFailure=" + true);
            deleteCookie(request, ContextHelper.getResponse(_context), _cookieName); 
        }
        
        // The general login screen will display the form for us
        redirector.redirect(false, url + parameters);
    }
    
    @Override
    public void nonBlockingUserNotAllowed(Redirector redirector) throws Exception
    {
        // Do nothing
    }

    @Override
    public void blockingUserAllowed(UserIdentity userConnected)
    {
        if (!SECURITY_LEVEL_HIGH.equals(_securityLevel))
        {
            Request request = ContextHelper.getRequest(_context);
            if ("true".equals(request.getParameter(_rememberMeField)))
            {
                nonBlockingUserAllowed(userConnected);
            }
        }
        else
        {
            _deleteLoginFailedBDD(userConnected.getLogin(), userConnected.getPopulationId());
        }
    }
    
    @Override
    public void nonBlockingUserAllowed(UserIdentity userConnected)
    {
        // Hash token + salt
        String token = RandomStringUtils.randomAlphanumeric(16);
        String salt = RandomStringUtils.randomAlphanumeric(48);
        String hashedTokenAndSalt = DigestUtils.sha512Hex(token + salt);
        
        _insertUserToken(userConnected.getPopulationId(), userConnected.getLogin(), salt, hashedTokenAndSalt);
        updateCookie(userConnected.getPopulationId() + "," + userConnected.getLogin() + "," + token, _cookieName, (int) _cookieLifetime, _context);
    }

    @Override
    public boolean requiresNewWindow()
    {
        return false;
    }

    /*************************************************************************************************************
     * Connection failure management
     */
    
    /**
     * Delete all past failed connections
     */
    protected void _deleteAllPastLoginFailedBDD()
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            // Build request for authenticating the user
            String sql = "DELETE FROM Users_FormConnectionFailed WHERE last_connect < ?";
            
            DateTime dateToday = new DateTime();
            DateTime thresholdDate = dateToday.minusDays(TIME_ALLOWED);
            Timestamp threshold = new Timestamp(thresholdDate.getMillis());
            
            stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1, threshold);
            
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
     * Get the number of failed connections with this login
     * @param login The login to request
     * @param populationId The user's population
     * @return the number of connection failed
     */
    public Integer requestNbConnectBDD(String login, String populationId)
    {
        _deleteAllPastLoginFailedBDD();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            // Build request for authenticating the user
            String sql = "SELECT nb_connect FROM Users_FormConnectionFailed WHERE login = ? and population_id = ?";
            
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);
            stmt.setString(2, populationId);
    
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
     * @param populationId The population id of the user
     * @return the number of failed connection
     */
    protected Integer _setNbConnectBDD(String login, String populationId)
    {
        Integer nbConnect = requestNbConnectBDD(login, populationId);
        if (nbConnect == 0)
        {
            _insertLoginNbConnectBDD(login, populationId);
        }
        else
        {
            _updateLoginNbConnectBDD(login, populationId, nbConnect);
        }
        
        return nbConnect;
        
    }
    
    /**
     * Insert the login with one failed connection in the BDD
     * @param login The login to insert
     * @param populationId The population id
     */
    protected void _insertLoginNbConnectBDD(String login, String populationId)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sqlUpdate = "INSERT INTO Users_FormConnectionFailed (login, population_id, nb_connect, last_connect) VALUES (?, ?, ?, ?)";
           
            stmt = connection.prepareStatement(sqlUpdate);
            stmt.setString(1, login);
            stmt.setString(2, populationId);
            stmt.setInt(3, 1);
            
            DateTime dateToday = new DateTime(); 
            
            Timestamp date = new Timestamp(dateToday.getMillis());
            stmt.setTimestamp(4, date);
            
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
     * Delete the login from the table of the failed connection
     * @param login The login to remove
     * @param populationId The populationId of the user
     */
    protected void _deleteLoginFailedBDD(String login, String populationId)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            // Build request for authenticating the user
            String sql = "DELETE FROM Users_FormConnectionFailed WHERE login = ? and population_id = ?";
            
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);
            stmt.setString(2, populationId);
            
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
     * @param populationId The user's population
     * @param nbConnect The nb of connection to set
     */
    protected void _updateLoginNbConnectBDD(String login, String populationId, Integer nbConnect)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sqlUpdate = "UPDATE Users_FormConnectionFailed SET nb_connect = ? WHERE login = ? and population_id = ?";
           
            stmt = connection.prepareStatement(sqlUpdate);
            stmt.setInt(1, nbConnect + 1);
            stmt.setString(2, login);
            stmt.setString(3, populationId);
            
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
      */
    public static void deleteCookie(Request request, Response response, String cookieName)
    {
        Cookie cookie = new HttpCookie(cookieName, "");
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Inserts a new line into the users token table
     * @param populationId The user's population id
     * @param login the login of the user
     * @param salt the salt associated to this user
     * @param hashedTokenAndSalt token + salt hashed with SHA-512
     */
    protected void _insertUserToken(String populationId, String login, String salt, String hashedTokenAndSalt)
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
                
                statement = connection.prepareStatement("INSERT INTO Users_Token (id, login, population_id, token, salt, creation_date) VALUES (?, ?, ?, ?, ?, ?)");
                statement.setString(1, id);
                statement.setString(2, login);
                statement.setString(3, populationId);
                statement.setString(4, hashedTokenAndSalt);
                statement.setString(5, salt);
                statement.setDate(6, new java.sql.Date(System.currentTimeMillis()));
            }
            else
            {
                statement = connection.prepareStatement("INSERT INTO Users_Token (login, population_id, token, salt, creation_date) VALUES (?, ?, ?, ?, ?)");
                
                statement.setString(1, login);
                statement.setString(2, populationId);
                statement.setString(3, hashedTokenAndSalt);
                statement.setString(4, salt);
                statement.setDate(5, new java.sql.Date(System.currentTimeMillis()));
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
     * Test if the user is already authenticated by the CredentialsProvider ?
     * @param populationId The population id
     * @param login The login to check
     * @param token The token to check
     * @return true if the user is already authenticated by the CredentialsProvider, false otherwise.
     */
    public boolean checkToken(String populationId, String login, String token)
    {
        Connection connection = null;
        PreparedStatement deleteStatement = null;   
        
        try
        {
            connection = getSQLConnection();
                    
            // Delete 2 weeks or more old entries
            deleteStatement = _getDeleteOldUserTokenStatement(connection);
            deleteStatement.executeUpdate();

            try (PreparedStatement selectStatement = _getSelectUserTokenStatement(connection, populationId, login);
                 ResultSet resultSet = selectStatement.executeQuery())
            {
                // Find the database entry using this token
                while (resultSet.next())
                {
                    if (resultSet.getString("token").equals(DigestUtils.sha512Hex(token + resultSet.getString("salt"))))
                    {
                        // Delete it
                        _deleteUserToken(connection, resultSet.getString("token"));
                        return true;
                    }
                }
                    
                return false;
            }
        }
        catch (Exception e)
        {
            getLogger().error("Communication error with the database", e); 
            return false;
        }
        finally
        {
            ConnectionHelper.cleanup(deleteStatement);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /**
     * Generates the sql statement that deletes the entries of the users token database that are at least 2 weeks old
     * @param connection the database's session
     * @return statement the delete statement
     * @throws SQLException if a sql exception occurs
     */
    private PreparedStatement _getDeleteOldUserTokenStatement(Connection connection) throws SQLException
    {
        String sqlRequest = null;
        sqlRequest = "DELETE FROM Users_Token WHERE creation_date < ?";

        Date thresholdDate = new Date(System.currentTimeMillis() - COOKIE_LIFETIME * 1000);

        PreparedStatement statement = connection.prepareStatement(sqlRequest);
        statement.setDate(1, thresholdDate);

        return statement;
    }
    
    /**
     * Generates the statement that selects the users having the specified login in the Users_Token table
     * @param connection the database's session
     * @param populationId The population id of the user
     * @param login the user's login
     * @return the retrieve statement
     * @throws SQLException if a sql exception occurs
     */
    private PreparedStatement _getSelectUserTokenStatement(Connection connection, String populationId, String login) throws SQLException
    {
        String sqlRequest = "SELECT id, token, salt FROM Users_Token WHERE login= ? and population_id= ?";
        
        PreparedStatement statement = connection.prepareStatement(sqlRequest);
        statement.setString(1, login);
        statement.setString(2, populationId);

        return statement;
    }
    
    /**
     * Deletes the database entry that has this token 
     * @param connection the database's session
     * @param token the token
     * @throws SQLException if an error occurred
     */
    private void _deleteUserToken(Connection connection, String token) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Users_Token WHERE token = ?"))
        {
            statement.setString(1, token);
            statement.executeUpdate();
        }
    }
}
