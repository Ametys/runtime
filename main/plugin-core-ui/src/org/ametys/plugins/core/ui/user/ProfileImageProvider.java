/*
 *  Copyright 2015 Anyware Services
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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import org.ametys.core.upload.Upload;
import org.ametys.core.upload.UploadManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UsersManager;
import org.ametys.core.userpref.UserPreferencesManager;
import org.ametys.core.util.ImageHelper;
import org.ametys.core.util.JSONUtils;

/**
 * Helper providing images that are used for user profiles
 */
public class ProfileImageProvider extends AbstractLogEnabled implements Component, Serviceable
{
    /**
     * Profile image source enum
     */
    public enum ProfileImageSource
    {
        /** Local images */
        LOCALIMAGE,
        /** Gravatar */
        GRAVATAR,
        /** Provided by the users manager */
        USERSMANAGER,
        /** Image with the initial */
        INITIALS,
        /** Uploaded image */
        UPLOAD,
        /** Image stored in base64 */
        BASE64,
        /** To be extracted from userpref */
        USERPREF,
        /** The default image */
        DEFAULT
    }
    
    /** Avalon role */
    public static final String ROLE = ProfileImageProvider.class.getName();
    
    /** The pref context for user profile */
    public static final String USER_PROFILE_PREF_CONTEXT = "/profile";
    
    /** The profile image user pref id */
    public static final String USERPREF_PROFILE_IMAGE = "profile-image";
    
    /** Relative path of the user profiles directory, which contains all the image subdirectories */
    protected static final String __USER_PROFILES_DIR_PATH = "user-profiles";
    
    /** Name of the avatar directory */
    protected static final String __AVATAR_DIR_NAME = "avatar";
    
    /** Name of the initials directory */
    protected static final String __INITIALS_DIR_NAME = "initials";
    
    /** Name of the default image */
    protected static final String __DEFAULT_FILE_NAME = "default.png";
    
    /** The map of paths to avatar images, keys are id */
    protected static Map<String, String> __avatarPaths;
    
    /** Ordered list of paths to available backgrounds for 'initials' images */
    protected static List<String> __initialsBgPaths;
    
    /** Source resolver */
    protected SourceResolver _sourceResolver;
    
    /** Users manager */
    protected UsersManager _usersManager;
    
    /** Upload manager */
    protected UploadManager _uploadManager;
    
    /** User pref manager */
    protected UserPreferencesManager _userPreferencesManager;
    
    /** JSON Utils */
    protected JSONUtils _jsonUtils;
    
    /** Context. */
    protected org.apache.cocoon.environment.Context _context;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _sourceResolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
        _usersManager = (UsersManager) smanager.lookup(UsersManager.ROLE);
        _uploadManager = (UploadManager) smanager.lookup(UploadManager.ROLE);
        _userPreferencesManager = (UserPreferencesManager) smanager.lookup(UserPreferencesManager.ROLE); 
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    
    // TODO Cache: ametys home user-profiles? For remote image (gravatar) at least
    
    /**
     * Get the default user image
     * @return The {@link UserProfileImage} for the default image
     */
    public UserProfileImage getDefaultImage()
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
     * Get the profile image source given a source input string
     * @param imageSourceStr The input string representing the source
     * @return The profile image source.
     */
    public ProfileImageSource getProfileImageSource(String imageSourceStr)
    {
        ProfileImageSource profileImageSource = null;
        
        try
        {
            if (StringUtils.isNotEmpty(imageSourceStr))
            {
                profileImageSource = ProfileImageSource.valueOf(imageSourceStr.toUpperCase());
            }
        }
        catch (IllegalArgumentException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Unknown profile image source " + imageSourceStr + ".", e);
            }
        }
        
        return profileImageSource;
    }
    
    /**
     * Get the image input stream
     * @param source The image source type
     * @param login The user login
     * @param sourceParams The parameters used by the source
     * @return The {@link UserProfileImage} for the image or null if not found
     */
    public UserProfileImage getImage(ProfileImageSource source, String login, Map<String, Object> sourceParams)
    {
        switch (source)
        {
            case USERPREF:
                return getUserPrefImage(login, sourceParams);
            case GRAVATAR:
                return getGravatarImage(login, _getGravatarSize(sourceParams));
            case UPLOAD:
                return getUploadedImage(login, (String) sourceParams.get("id"));
            case LOCALIMAGE:
                return getLocalImage(login, (String) sourceParams.get("id"));
            case INITIALS:
                return getInitialsImage(login);
            case BASE64:
                return getBase64Image(login, (String) sourceParams.get("data"), (String) sourceParams.get("filename"));
            case DEFAULT:
                return getDefaultImage();
            case USERSMANAGER:
                // not implemented yet
            default:
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("Cannot get image for user '%s'. Unhandled profile image source '%s'", login, source));
                }
        }
        
        return null;
    }
    
    /**
     * Get the image from a base 64 string
     * @param login The user login
     * @param data The base64 data representing the image
     * @param filename The filename or null if not known
     * @return The {@link UserProfileImage} for the image or null if not set
     */
    public UserProfileImage getBase64Image(String login, String data, String filename)
    {
        if (StringUtils.isEmpty(data))
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(String.format("No data provided. Unable to retrieve the base64 image for user '%s'.", login));
            }
            return null;
        }
        
        InputStream is = new ByteArrayInputStream(new Base64(true).decode(data));
        return new UserProfileImage(is, StringUtils.defaultIfBlank(filename, null), null);
    }
    
    /**
     * Get the image from the user pref
     * @param login The user login
     * @param baseSourceParams The base source params to be merge with the params stored in the user pref
     * @return The {@link UserProfileImage} for the image or null if not set
     */
    public UserProfileImage getUserPrefImage(String login, Map<String, Object> baseSourceParams)
    {
        Map<String, Object> userPrefImgData = _getRawUserPrefImage(login);
        if (userPrefImgData != null)
        {
            String rawImageSource = (String) userPrefImgData.remove("source");
            ProfileImageSource profileImageSource = getProfileImageSource(rawImageSource);
            
            if (profileImageSource == null || ProfileImageSource.USERPREF.equals(profileImageSource))
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("An profile image seems to be stored as an userpref but its image source is empty, not handled or corrupted");
                }
                return null;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceParams = (Map<String, Object>) userPrefImgData.get("parameters");
            if (sourceParams != null)
            {
                sourceParams.putAll(baseSourceParams);
            }
            else
            {
                sourceParams = new HashMap<>(baseSourceParams);
            }
            
            return getImage(profileImageSource, login, sourceParams);
        }
        
        return null;
    }
    
    /**
     * Test this user as a profile image set in its user pref
     * @param login The user login
     * @return The map stored in the user pref
     */
    public Map<String, Object> hasUserPrefImage(String login)
    {
        Map<String, Object> userPrefImgData = _getRawUserPrefImage(login);
        if (userPrefImgData != null)
        {
            String rawImageSource = (String) userPrefImgData.get("source");
            ProfileImageSource profileImageSource = getProfileImageSource(rawImageSource);
            if (profileImageSource != null)
            {
                return userPrefImgData;
            }
        }
        
        return null;
    }
    
    /**
     * Get the profile image user pref
     * @param login The user login
     * @return The map stored in the user pref
     */
    public Map<String, Object> _getRawUserPrefImage(String login)
    {
        try
        {
            String userPrefImgJson = _userPreferencesManager.getUserPreferenceAsString(login, USER_PROFILE_PREF_CONTEXT, Collections.EMPTY_MAP, USERPREF_PROFILE_IMAGE);
            if (StringUtils.isNotEmpty(userPrefImgJson))
            {
                return _jsonUtils.convertJsonToMap(userPrefImgJson);
            }
        }
        catch (Exception e)
        {
            getLogger().error(String.format("Unable to retrieve the '%s' userpref on context '%s' for user '%s'", USERPREF_PROFILE_IMAGE, USER_PROFILE_PREF_CONTEXT, login), e);
        }
        
        return null;
    }
    
    /**
     * Extract the gravatar size from the source params if any
     * @param sourceParams The source params
     * @return The requested image size for gravatar or null if not provided
     */
    private Integer _getGravatarSize(Map<String, Object> sourceParams)
    {
        Integer size = (Integer) sourceParams.get("size");
        if (size != null && size > 0)
        {
            return size;
        }
        
        Integer maxSize = (Integer) sourceParams.get("maxSize");
        if (maxSize != null && maxSize > 0)
        {
            return maxSize;
        }
        
        return null;
    }
    
    /**
     * Test if the gravatar image exists
     * @param login The user login
     * @return True if the image exists
     */
    public boolean hasGravatarImage(String login)
    {
        Source httpSource = null;
        try
        {
            httpSource = _getGravatarImageSource(login, null);
            return httpSource != null && httpSource.exists();
        }
        catch (IOException e)
        {
            getLogger().error("Unable to test the gravatar image for user '" + login + "'.", e);
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
    
    /**
     * Get gravatar image
     * @param login The user login
     * @param size The requested size
     * @return The {@link UserProfileImage} or null if not found
     */
    public UserProfileImage getGravatarImage(String login, Integer size)
    {
        // Resolve an http source
        Source httpSource = null;
        try
        {
            httpSource = _getGravatarImageSource(login, size);
            if (httpSource != null && httpSource.exists())
            {
                return new UserProfileImage(httpSource.getInputStream());
            }
        }
        catch (IOException e)
        {
            getLogger().error("Unable to retrieve gravatar image for user '" + login + "'.", e);
        }
        finally
        {
            if (httpSource != null)
            {
                _sourceResolver.release(httpSource);
            }
        }
        
        return null;
    }
    
    /**
     * Get the source of a gravatar image
     * @param login The user login
     * @param size The requested size
     * @return The source or null
     * @throws IOException If an error occurs while resolving the source uri
     */
    protected Source _getGravatarImageSource(String login, Integer size) throws IOException
    {
        User user = _usersManager.getUser(login);
        if (user == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Unable to get gravatar image source - user not found " + login);
            }
            return null;
        }
        
        String email = user.getEmail();
        if (StringUtils.isEmpty(email))
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(String.format("Unable to get gravatar image for user '%s' - an email is mandatory", login));
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
            getLogger().debug(String.format("Build gravatar uri for user '%s' : %s", login, uri));
        }
        
        return _sourceResolver.resolveURI(uri);
    }
    
    /**
     * Get the uploaded image
     * @param login The user login
     * @param uploadId The upload identifier
     * @return The {@link UserProfileImage} for the image or null if not found
     */
    public UserProfileImage getUploadedImage(String login, String uploadId)
    {
        if (StringUtils.isEmpty(uploadId))
        {
            return null;
        }
        
        Upload upload = null;
        try
        {
            upload = _uploadManager.getUpload(login, uploadId);
            try (InputStream is = upload.getInputStream())
            {
                BufferedImage croppedImage = cropUploadedImage(is);
                
                String filename =  upload.getFilename();
                String format = FilenameUtils.getExtension(filename);
                format = ProfileImageReader.ALLOWED_IMG_FORMATS.contains(format) ? format : "png";
                
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
                {
                    ImageIO.write(croppedImage, format, baos);
                    return new UserProfileImage(new ByteArrayInputStream(baos.toByteArray()), filename, null); // no length because image is cropped
                }
            }
            catch (IOException e)
            {
                getLogger().error(String.format("Unable to provide the uploaded cropped image for user '%s'and upload id '%s'.", login, uploadId), e); 
            }
        }
        catch (NoSuchElementException e)
        {
            // Invalid upload id
            getLogger().error(String.format("Cannot find the temporary uploaded file for id '%s' and login '%s'.", uploadId, login), e);
        }
        
        return null;
    }
    
    /**
     * Automatically crop the image to 64x64 pixels.
     * @param is The input stream of the uploaded file
     * @return The base64 string
     * @throws IOException If an exception occurs while manipulating streams
     */
    public BufferedImage cropUploadedImage(InputStream is) throws IOException
    {
        // Crop the image the get a square image, vertically centered to the input image.
        BufferedImage image = ImageIO.read(is);
        int width = image.getWidth();
        int height = image.getHeight();
        if (width != height)
        {
            int min = Math.min(width, height);
            image = image.getSubimage((width - min) / 2, 0, min, min);
        }
        
        // Scale square image to side of 64px 
        return ImageHelper.generateThumbnail(image, 0, 0, 64, 64);
    }
    
    /**
     * Test if the local image exists
     * @param localFileId The local file identifier
     * @return True if the image exists
     */
    public boolean hasLocalImage(String localFileId)
    {
        Source imgSource = null;
        try
        {
            imgSource = _getLocalImageSource(localFileId);
            if (imgSource == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("Unable to test the local image for id '%s.", localFileId));
                }
                return false;
            }
            
            return imgSource.exists();
        }
        catch (IOException e)
        {
            getLogger().error(String.format("Unable to  test the local image for id '%s' and login '%s'.", localFileId), e);
        }
        finally
        {
            if (imgSource != null)
            {
                _sourceResolver.release(imgSource);
            }
        }
        
        return false;
    }
    
    /**
     * Get the local image
     * @param login The user login
     * @param localFileId The local file identifier
     * @return The {@link UserProfileImage} for the image or null if not found
     */
    public UserProfileImage getLocalImage(String login, String localFileId)
    {
        Source imgSource = null;
        
        try
        {
            imgSource = _getLocalImageSource(localFileId);
            
            if (imgSource == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("Unable to retrieve the local image for id '%s' and login '%s'.", localFileId, login));
                }
                return null;
            }
            else if (imgSource.exists())
            {
                String avatarPath = _getLocalImagePaths().get(localFileId);
                return new UserProfileImage(imgSource.getInputStream(), FilenameUtils.getName(avatarPath), null);
            }
            
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(String.format("Unable to find any local image with id '%s' for user '%s'", localFileId, login));
            }
        }
        catch (IOException e)
        {
            getLogger().error(String.format("Unable to retrieve the local image for id '%s' and login '%s'.", localFileId, login), e);
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
     * Get the source of a local image
     * @param localFileId The local file identifier
     * @return The source or null
     * @throws IOException If an error occurs while resolving the source uri
     */
    protected Source _getLocalImageSource(String localFileId) throws IOException
    {
        Map<String, String> imgPaths = _getLocalImagePaths();
        String avatarPath = imgPaths != null ? imgPaths.get(localFileId) : StringUtils.EMPTY;
        
        if (StringUtils.isEmpty(avatarPath))
        {
            return null;
        }
        
        String location = "plugin:core-ui://resources/img/" + __USER_PROFILES_DIR_PATH + "/" + __AVATAR_DIR_NAME + "/" + avatarPath;
        return _sourceResolver.resolveURI(location);
    }
    
    /**
     * Get the list of local image identifiers
     * @return Ordered list of identifiers
     */
    public List<String> getLocalImageIds()
    {
        return new LinkedList<>(_getLocalImagePaths().keySet());
    }
    
    /**
     * Get the map containing the relative path for each local image.
     * Create the map if not existing yet.
     * @return Map where keys are ids and values are the relative paths
     */
    protected Map<String, String> _getLocalImagePaths()
    {
        if (__avatarPaths == null)
        {
            _initializeLocalImagePaths();
        }
        
        return __avatarPaths;
    }
    
    /**
     * Initializes the map of local image paths
     */
    private void _initializeLocalImagePaths()
    {
        __avatarPaths = new LinkedHashMap<>(); // use insertion order
        
        String location = "plugin:core-ui://resources/img/" + __USER_PROFILES_DIR_PATH + "/" + __AVATAR_DIR_NAME + "/" + __AVATAR_DIR_NAME + ".xml";
        Source source = null;
        try
        {
            source = _sourceResolver.resolveURI(location);
            
            try (InputStream is = source.getInputStream())
            {
                Configuration cfg = new DefaultConfigurationBuilder().build(is);
                for (Configuration imageCfg : cfg.getChildren("image"))
                {
                    __avatarPaths.put(imageCfg.getAttribute("id"), imageCfg.getValue());
                }
            }
        }
        catch (IOException | ConfigurationException | SAXException e)
        {
            getLogger().error("Unable to retrieve the map of local image paths", e);
        }
        finally
        {
            if (source != null)
            {
                _sourceResolver.release(source);
            }
        }
    }
    
    /**
     * Test if the initials image is available for a given user
     * @param login The user login
     * @return True if the image exists
     */
    public boolean hasInitialsImage(String login)
    {
        User user = _usersManager.getUser(login);
        if (user == null)
        {
            getLogger().warn("Unable to test the initials image - user not found " + login);
            return false;
        }
        
        String initial = user.getFullName().substring(0, 1).toLowerCase();
        Source imgSource = null;
        
        try
        {
            imgSource = _getInitialsImageSource(initial);
            return imgSource.exists();
        }
        catch (IOException e)
        {
            getLogger().error(String.format("Unable to test initials image for user '%s' with fullname '%s'.", login, user.getFullName()), e);
        }
        finally
        {
            if (imgSource != null)
            {
                _sourceResolver.release(imgSource);
            }
        }
        
        return false;
    }
    
    /**
     * Get the image with user initials
     * @param login The user login
     * @return The {@link UserProfileImage} for the image or null if not found
     */
    public UserProfileImage getInitialsImage(String login)
    {
        User user = _usersManager.getUser(login);
        if (user == null)
        {
            getLogger().warn("Unable to get initials image - user not found " + login);
            return null;
        }
        
        String initial = user.getFullName().substring(0, 1).toLowerCase();
        Source imgSource = null;
        
        try
        {
            imgSource = _getInitialsImageSource(initial);
            if (imgSource.exists())
            {
                String filename = initial + ".png";
                try (InputStream is = imgSource.getInputStream())
                {
                    try
                    {
                        InputStream imageIsWithBackground = _addImageBackground(login, is);
                        return new UserProfileImage(imageIsWithBackground, filename, null);
                    }
                    catch (IOException e)
                    {
                        getLogger().error(
                                String.format("Unable to add the background image to the initials image for user '%s' with fullname '%s'. Only the initial image will be used.",
                                        login, user.getFullName()), e);
                        
                        // Return image without background
                        return new UserProfileImage(imgSource.getInputStream(), filename, null);
                    }
                }
            }
            
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(String.format("Unable to find the initials image for user '%s' with fullname '%s'", login, user.getFullName()));
            }
        }
        catch (IOException e)
        {
            getLogger().error(String.format("Unable to retrieve the initials image for user '%s' with fullname '%s'.", login, user.getFullName()), e);
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
     * Get the source of the initials image
     * @param initial The initial
     * @return The source
     * @throws IOException If an error occurs while resolving the source uri
     */
    protected Source _getInitialsImageSource(String initial) throws IOException
    {
        String location = "plugin:core-ui://resources/img/" + __USER_PROFILES_DIR_PATH + "/" + __INITIALS_DIR_NAME + "/" + initial + ".png";
        return _sourceResolver.resolveURI(location);
    }
    
    /**
     * Add a background to an initials image
     * @param login The login used to determine which background will be used (based on a hash representation of the login)
     * @param is The inputstream of the image
     * @return The inputstream of the final image with the background
     * @throws IOException If any sort of IO error occurs during the process 
     */
    protected InputStream _addImageBackground(String login, InputStream is) throws IOException
    {
        BufferedImage image = ImageIO.read(is);
        Source bgSource = null;
        try
        {
            bgSource = _getInitialsBackgroundSource(login);
            BufferedImage background = null;
            
            try (InputStream backgroundIs = bgSource.getInputStream())
            {
                background = ImageIO.read(backgroundIs);
                Graphics backgroundGraphics = background.getGraphics();
                backgroundGraphics.drawImage(image, 0, 0, null);
            }
            
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                ImageIO.write(background, "png", baos);
                return new ByteArrayInputStream(baos.toByteArray());
            }
        }
        finally
        {
            _sourceResolver.release(bgSource);
        }
    }
    
    /**
     * Get the background image for the initials source.
     * The chosen background depend on the user login 
     * @param login The user login.
     * @return The source
     * @throws IOException If an error occurs while resolving the source uri
     */
    protected Source _getInitialsBackgroundSource(String login) throws IOException
    {
        // Hashing the login then choose a background given the available ones.
        long hash = Math.abs(HashUtil.hash(login));
        
        // Perform a modulo on the hash given number of available background
        _initializeInitialsBackgroundPaths();
        long nbBackground = __initialsBgPaths.size();
        if (nbBackground == 0)
        {
            throw new IOException("No backgrounds available.");
        }
        
        int indexBackground = (int) (hash % nbBackground);
        
        // Get file from list
        String path = __initialsBgPaths.get(indexBackground);
        
        String location = "plugin:core-ui://resources/img/" + __USER_PROFILES_DIR_PATH + "/" + __INITIALS_DIR_NAME + "/" + path;
        return _sourceResolver.resolveURI(location);
    }
    
    /**
     * Initializes the list of background paths for initials images
     */
    private void _initializeInitialsBackgroundPaths()
    {
        if (__initialsBgPaths == null)
        {
            __initialsBgPaths = new LinkedList<>();
            
            String location = "plugin:core-ui://resources/img/" + __USER_PROFILES_DIR_PATH + "/" + __INITIALS_DIR_NAME + "/" + __INITIALS_DIR_NAME + ".xml";
            Source source = null;
            
            try
            {
                source = _sourceResolver.resolveURI(location);
                
                try (InputStream is = source.getInputStream())
                {
                    Configuration cfg = new DefaultConfigurationBuilder().build(is);
                    
                    for (Configuration backgroundCfg : cfg.getChildren("background"))
                    {
                        __initialsBgPaths.add(backgroundCfg.getValue());
                    }
                }
            }
            catch (IOException | ConfigurationException | SAXException e)
            {
                getLogger().error("Unable to retrieve the list of available backgrounds for initials images", e);
            }
            finally
            {
                if (source != null)
                {
                    _sourceResolver.release(source);
                }
            }
        }
    }
    
    /**
     * Basic structure holding necessary data representing an user profile image
     */
    public static class UserProfileImage
    {
        private final InputStream _inputstream;
        private final String _filename;
        private final Long _length;
        
        /**
         * Constructor
         * @param inputstream The image input stream
         */
        public UserProfileImage(InputStream inputstream)
        {
            this(inputstream, null, null);
        }
        
        /**
         * Constructor
         * @param inputstream The image input stream
         * @param filename The file name or null if unknown
         * @param length The file length if known
         */
        public UserProfileImage(InputStream inputstream, String filename, Long length)
        {
            _inputstream = inputstream;
            _filename = filename;
            _length = length;
        }
        
        /**
         * Retrieves the input stream
         * @return the input stream
         */
        public InputStream getInputstream()
        {
            return _inputstream;
        }

        /**
         * Retrieves the filename
         * @return the filename or null if not defined
         */
        public String getFilename()
        {
            return _filename;
        }

        /**
         * Retrieves the length
         * @return the length or null if unknown
         */
        public Long getLength()
        {
            return _length;
        }
    }
}

