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
package org.ametys.plugins.core.ui.user;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;

/**
 * Image provider working in safe mode
 */
public class SafeProfileImageProvider extends AbstractLogEnabled implements ProfileImageProvider, Serviceable
{
    /** Relative path of the user profiles directory, which contains all the image subdirectories */
    protected static final String __USER_PROFILES_DIR_PATH = "user-profiles";
    
    /** Name of the default image */
    protected static final String __DEFAULT_FILE_NAME = "default.png";
    
    /** Source resolver */
    protected SourceResolver _sourceResolver;
    
    /** Users manager */
    protected UserManager _userManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _sourceResolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
    }
    
    public UserProfileImage getImage(UserIdentity user, String imageSource, int size, int maxSize) throws ProcessingException
    {
        UserProfileImage image = getGravatarImage(user, size > 0 ? size : maxSize);
        if (image == null)
        {
            return getDefaultImage();
        }
        else
        {
            return image;
        }
    }
    
    /**
     * Get the default user image
     * @return The UserProfileImage for the default image
     */
    protected UserProfileImage getDefaultImage()
    {
        String location = "plugin:core-ui://resources/img/" + __USER_PROFILES_DIR_PATH + "/" + __DEFAULT_FILE_NAME;
        Source imgSource = null;
        
        try
        {
            imgSource = _sourceResolver.resolveURI(location);
            if (imgSource.exists())
            {
                return new UserProfileImage(imgSource.getInputStream(), __DEFAULT_FILE_NAME, null);
            }
            
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Unable to find the default user image");
            }
        }
        catch (IOException e)
        {
            getLogger().error("Unable to retrieve the default user image");
        }
        finally
        {
            if (imgSource != null)
            {
                _sourceResolver.release(imgSource);
            }
        }
        
        return null;
    }
    
    /**
     * Get gravatar image
     * @param user The user
     * @param size The image size
     * @return The UserProfileImage or null if not found
     */
    protected UserProfileImage getGravatarImage(UserIdentity user, int size)
    {
        // Resolve an http source
        Source httpSource = null;
        try
        {
            httpSource = _getGravatarImageSource(user, size);
            if (httpSource != null && httpSource.exists())
            {
                return new UserProfileImage(httpSource.getInputStream());
            }
        }
        catch (IOException e)
        {
            getLogger().error("Unable to retrieve gravatar image for user '" + user + "'.", e);
        }
        finally
        {
            if (httpSource != null)
            {
                _sourceResolver.release(httpSource);
            }
        }
        
        return null;
    }    /**
     * Get the source of a gravatar image
     * @param userIdentity The user
     * @param size The requested size
     * @return The source or null
     * @throws IOException If an error occurs while resolving the source uri
     */
    private Source _getGravatarImageSource(UserIdentity userIdentity, Integer size) throws IOException
    {
        User user = _userManager.getUser(userIdentity.getPopulationId(), userIdentity.getLogin());
        if (user == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Unable to get gravatar image source - user not found " + userIdentity);
            }
            return null;
        }
        
        String email = user.getEmail();
        if (StringUtils.isEmpty(email))
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("Unable to get gravatar image for user '%s' - an email is mandatory", userIdentity));
            }
            return null;
        }
        
        // Compute hex MD5 hash
        String hash = null;
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(StandardCharsets.UTF_8.encode(email));
            byte[] hexBytes = new Hex(StandardCharsets.UTF_8).encode(md5.digest());
            hash = new String(hexBytes, StandardCharsets.UTF_8);
        }
        catch (NoSuchAlgorithmException e)
        {
            // This error exception not be raised since MD5 is embedded in the JDK
            getLogger().error("Cannot encode the user email to md5Base64", e);
            return null;
        }
        
        // Build gravatar URL request
        List<NameValuePair> qparams = new ArrayList<>(1);
        qparams.add(new BasicNameValuePair("d", "404")); // 404 if no image for this user
        
        if (size != null && size > 0)
        {
            qparams.add(new BasicNameValuePair("s", Integer.toString(size)));
        }
        
        String uri = new URIBuilder()
            .setScheme("http")
            .setHost("www.gravatar.com")
            .setPath("/avatar/" + hash + ".png") // force png
            .setParameters(qparams).toString();
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(String.format("Build gravatar uri for user '%s' : %s", userIdentity, uri));
        }
        
        return _sourceResolver.resolveURI(uri);
    }
    
    /**
     * Test if the gravatar image exists
     * @param user The user
     * @return True if the image exists
     */
    public boolean hasGravatarImage(UserIdentity user)
    {
        Source httpSource = null;
        try
        {
            httpSource = _getGravatarImageSource(user, null);
            return httpSource != null && httpSource.exists();
        }
        catch (IOException e)
        {
            getLogger().error("Unable to test the gravatar image for user '" + user + "'.", e);
        }
        finally
        {
            if (httpSource != null)
            {
                _sourceResolver.release(httpSource);
            }
        }
        
        return false;
    }
}
