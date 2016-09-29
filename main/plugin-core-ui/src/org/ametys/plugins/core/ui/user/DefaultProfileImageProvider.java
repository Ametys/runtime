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
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import org.ametys.core.upload.Upload;
import org.ametys.core.upload.UploadManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.userpref.UserPreferencesManager;
import org.ametys.core.util.ImageHelper;
import org.ametys.core.util.JSONUtils;

/**
 * Helper providing images that are used for user profiles
 */
public class DefaultProfileImageProvider extends SafeProfileImageProvider implements Contextualizable, Component
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
    
    /** The pref context for user profile */
    public static final String USER_PROFILE_PREF_CONTEXT = "/profile";

    /** The profile image user pref id */
    public static final String USERPREF_PROFILE_IMAGE = "profile-image";
    
    /** Name of the avatar directory */
    protected static final String __AVATAR_DIR_NAME = "avatar";
    
    /** Name of the initials directory */
    protected static final String __INITIALS_DIR_NAME = "initials";
    
    /** The map of paths to avatar images, keys are id */
    protected static Map<String, String> __avatarPaths;
    
    /** Ordered list of paths to available backgrounds for 'initials' images */
    protected static List<String> __initialsBgPaths;
    
    /** Users manager */
    protected UserManager _userManager;
    
    /** Upload manager */
    protected UploadManager _uploadManager;
    
    /** User pref manager */
    protected UserPreferencesManager _userPreferencesManager;
    
    /** JSON Utils */
    protected JSONUtils _jsonUtils;

    private Context _context;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _uploadManager = (UploadManager) smanager.lookup(UploadManager.ROLE);
        _userPreferencesManager = (UserPreferencesManager) smanager.lookup(UserPreferencesManager.ROLE); 
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public UserProfileImage getImage(UserIdentity user, String imageSource, int size, int maxSize) throws ProcessingException
    {
        ProfileImageSource profileImageSource = getProfileImageSource(imageSource);
        if (profileImageSource == null)
        {
            profileImageSource = ProfileImageSource.USERPREF; // default
        }
        
        // Get parameters for source
        Map<String, Object> sourceParams = _extractSourceParameters(user, profileImageSource);
        
        UserProfileImage image = null;
        if (sourceParams != null)
        {
            // Add size params
            if (size > 0)
            {
                sourceParams.put("size", size);
            }
            if (maxSize > 0)
            {
                sourceParams.put("maxSize", maxSize);
            }
            
            image = getImage(profileImageSource, user, sourceParams);
            
            if (image == null && ProfileImageSource.USERPREF.equals(profileImageSource))
            {
                // Reading from userpref, but no userpref set.
                // Try gravatar, then initials
                image = getGravatarImage(user, size > 0 ? size : maxSize);
                if (image == null)
                {
                    image = getInitialsImage(user);
                }
            }
        }
        
        if (image == null)
        {
            image = getDefaultImage();
            
            // still null?
            if (image == null)
            {
                throw new ProcessingException(String.format("Not able to provide an image from source '%s' for user '%s' because no image was found.", profileImageSource, user));
            }
        }
        
        return image;
    }
    
    
    // TODO Cache: ametys home user-profiles? For remote image (gravatar) at least
    
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
     * Provides the necessary parameters to retrieves the image from a given source.
     * @param user The user
     * @param profileImageSource The image source type
     * @return A map of parameters
     * @throws ResourceNotFoundException In case of a unhandled source type or if parameters could not be extracted 
     */
    
    protected Map<String, Object> _extractSourceParameters(UserIdentity user, ProfileImageSource profileImageSource) throws ResourceNotFoundException
    {
        Request request = ContextHelper.getRequest(_context);
        
        switch (profileImageSource)
        {
            case UPLOAD:
                return _extractUploadParameters(request, user);
            case LOCALIMAGE:
                return _extractLocalImageParameters(request, user);
            case BASE64:
                return _extractBase64Parameters(request, user);
            case INITIALS:
            case USERSMANAGER:
            case USERPREF:
            case GRAVATAR:
            case DEFAULT:
                // nothing special
                return new HashMap<>();
            default:
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("Cannot extract image source parameters for user '%s'. Unhandled profile image source '%s'", user, profileImageSource));
                }
                return null;
        }
    }
    
    /**
     * Extracts parameters for an uploaded image
     * @param request The request
     * @param user The user
     * @return A map containing the uploaded file id (key=id)
     */
    protected Map<String, Object> _extractUploadParameters(Request request, UserIdentity user)
    {
        String uploadId = request.getParameter("id");
        
        if (StringUtils.isEmpty(uploadId))
        {
            getLogger().error("Missing mandatory uploaded file id parameter to retrieve the uploaded file for user " + user + ".");
            return null;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("id", uploadId);
        
        return params;
    }
    
    /**
     * Extracts parameters for a local image
     * @param request The request
     * @param user The user
     * @return A map containing the local image id (key=id)
     */
    protected Map<String, Object> _extractLocalImageParameters(Request request, UserIdentity user)
    {
        String localFileId = request.getParameter("id");
        
        if (StringUtils.isEmpty(localFileId))
        {
            getLogger().error("Missing mandatory local file id parameter to retrieve the local file for user " + user + ".");
            return null;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("id", localFileId);
        
        return params;
    }
    
    /**
     * Extracts parameters for a local image
     * @param request The request
     * @param user The user
     * @return A map containing the local image id (key=id)
     */
    protected Map<String, Object> _extractBase64Parameters(Request request, UserIdentity user)
    {
        String data = request.getParameter("data");
        
        if (StringUtils.isEmpty(data))
        {
            getLogger().error("Missing mandatory data parameter for user image of type base 64 user " + user + ".");
            return null;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("data", data);
        
        String filename = request.getParameter("filename");
        if (StringUtils.isNotEmpty(filename))
        {
            params.put("filename", filename);
        }
        
        return params;
    }
    
    /**
     * Get the image input stream
     * @param source The image source type
     * @param user The user
     * @param sourceParams The parameters used by the source
     * @return The UserProfileImage for the image or null if not found
     */
    public UserProfileImage getImage(ProfileImageSource source, UserIdentity user, Map<String, Object> sourceParams)
    {
        switch (source)
        {
            case USERPREF:
                return getUserPrefImage(user, sourceParams);
            case GRAVATAR:
                return getGravatarImage(user, _getGravatarSize(sourceParams));
            case UPLOAD:
                return getUploadedImage(user, (String) sourceParams.get("id"));
            case LOCALIMAGE:
                return getLocalImage(user, (String) sourceParams.get("id"));
            case INITIALS:
                return getInitialsImage(user);
            case BASE64:
                return getBase64Image(user, (String) sourceParams.get("data"), (String) sourceParams.get("filename"));
            case DEFAULT:
                return getDefaultImage();
            case USERSMANAGER:
                // not implemented yet
            default:
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("Cannot get image for user '%s'. Unhandled profile image source '%s'", user, source));
                }
        }
        
        return null;
    }
    
    /**
     * Get the image from a base 64 string
     * @param user The user
     * @param data The base64 data representing the image
     * @param filename The filename or null if not known
     * @return The UserProfileImage for the image or null if not set
     */
    public UserProfileImage getBase64Image(UserIdentity user, String data, String filename)
    {
        if (StringUtils.isEmpty(data))
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(String.format("No data provided. Unable to retrieve the base64 image for user '%s'.", user));
            }
            return null;
        }
        
        InputStream is = new ByteArrayInputStream(new Base64(true).decode(data));
        return new UserProfileImage(is, StringUtils.defaultIfBlank(filename, null), null);
    }
    
    /**
     * Get the image from the user pref
     * @param user The user
     * @param baseSourceParams The base source params to be merge with the params stored in the user pref
     * @return The UserProfileImage for the image or null if not set
     */
    public UserProfileImage getUserPrefImage(UserIdentity user, Map<String, Object> baseSourceParams)
    {
        Map<String, Object> userPrefImgData = _getRawUserPrefImage(user);
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
            
            return getImage(profileImageSource, user, sourceParams);
        }
        
        return null;
    }
    
    /**
     * Test this user as a profile image set in its user pref
     * @param user The user
     * @return The map stored in the user pref
     */
    public Map<String, Object> hasUserPrefImage(UserIdentity user)
    {
        Map<String, Object> userPrefImgData = _getRawUserPrefImage(user);
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
     * @param user The user
     * @return The map stored in the user pref
     */
    public Map<String, Object> _getRawUserPrefImage(UserIdentity user)
    {
        try
        {
            String userPrefImgJson = _userPreferencesManager.getUserPreferenceAsString(user, USER_PROFILE_PREF_CONTEXT, Collections.EMPTY_MAP, USERPREF_PROFILE_IMAGE);
            if (StringUtils.isNotEmpty(userPrefImgJson))
            {
                return _jsonUtils.convertJsonToMap(userPrefImgJson);
            }
        }
        catch (Exception e)
        {
            getLogger().error(String.format("Unable to retrieve the '%s' userpref on context '%s' for user '%s'", USERPREF_PROFILE_IMAGE, USER_PROFILE_PREF_CONTEXT, user), e);
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
    
    /**
     * Get gravatar image
     * @param user The user
     * @param size The requested size
     * @return The UserProfileImage or null if not found
     */
    public UserProfileImage getGravatarImage(UserIdentity user, Integer size)
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
    }
    
    /**
     * Get the source of a gravatar image
     * @param userIdentity The user
     * @param size The requested size
     * @return The source or null
     * @throws IOException If an error occurs while resolving the source uri
     */
    protected Source _getGravatarImageSource(UserIdentity userIdentity, Integer size) throws IOException
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
     * Get the uploaded image
     * @param user The user
     * @param uploadId The upload identifier
     * @return The UserProfileImage for the image or null if not found
     */
    public UserProfileImage getUploadedImage(UserIdentity user, String uploadId)
    {
        if (StringUtils.isEmpty(uploadId))
        {
            return null;
        }
        
        Upload upload = null;
        try
        {
            upload = _uploadManager.getUpload(user, uploadId);
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
                getLogger().error(String.format("Unable to provide the uploaded cropped image for user '%s'and upload id '%s'.", user, uploadId), e); 
            }
        }
        catch (NoSuchElementException e)
        {
            // Invalid upload id
            getLogger().error(String.format("Cannot find the temporary uploaded file for id '%s' and login '%s'.", uploadId, user), e);
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
     * @param user The user
     * @param localFileId The local file identifier
     * @return The UserProfileImage for the image or null if not found
     */
    public UserProfileImage getLocalImage(UserIdentity user, String localFileId)
    {
        Source imgSource = null;
        
        try
        {
            imgSource = _getLocalImageSource(localFileId);
            
            if (imgSource == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(String.format("Unable to retrieve the local image for id '%s' and login '%s'.", localFileId, user));
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
                getLogger().warn(String.format("Unable to find any local image with id '%s' for user '%s'", localFileId, user));
            }
        }
        catch (IOException e)
        {
            getLogger().error(String.format("Unable to retrieve the local image for id '%s' and login '%s'.", localFileId, user), e);
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
        _initializeLocalImagePaths();
        return __avatarPaths;
    }
    
    /**
     * Initializes the map of local image paths
     */
    private void _initializeLocalImagePaths()
    {
        synchronized (DefaultProfileImageProvider.class)
        {
            if (__avatarPaths == null)
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
        }
    }
    
    /**
     * Test if the initials image is available for a given user
     * @param userIdentity The user
     * @return True if the image exists
     */
    public boolean hasInitialsImage(UserIdentity userIdentity)
    {
        User user = _userManager.getUser(userIdentity.getPopulationId(), userIdentity.getLogin());
        if (user == null)
        {
            getLogger().warn("Unable to test the initials image - user not found " + userIdentity);
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
            getLogger().error(String.format("Unable to test initials image for user '%s' with fullname '%s'.", userIdentity, user.getFullName()), e);
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
     * @param userIdentity The user
     * @return The UserProfileImage for the image or null if not found
     */
    public UserProfileImage getInitialsImage(UserIdentity userIdentity)
    {
        User user = _userManager.getUser(userIdentity.getPopulationId(), userIdentity.getLogin());
        if (user == null)
        {
            getLogger().warn("Unable to get initials image - user not found " + userIdentity);
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
                        InputStream imageIsWithBackground = _addImageBackground(userIdentity, is);
                        return new UserProfileImage(imageIsWithBackground, filename, null);
                    }
                    catch (IOException e)
                    {
                        getLogger().error(
                                String.format("Unable to add the background image to the initials image for user '%s' with fullname '%s'. Only the initial image will be used.",
                                        userIdentity, user.getFullName()), e);
                        
                        
                        // Return image without background
                        _sourceResolver.release(imgSource);
                        imgSource = _getInitialsImageSource(initial);
                        return new UserProfileImage(imgSource.getInputStream(), filename, null);
                    }
                }
            }
            
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(String.format("Unable to find the initials image for user '%s' with fullname '%s'", userIdentity, user.getFullName()));
            }
        }
        catch (IOException e)
        {
            getLogger().error(String.format("Unable to retrieve the initials image for user '%s' with fullname '%s'.", userIdentity, user.getFullName()), e);
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
     * @param user The user used to determine which background will be used (based on a hash representation of the login)
     * @param is The inputstream of the image
     * @return The inputstream of the final image with the background
     * @throws IOException If any sort of IO error occurs during the process 
     */
    protected InputStream _addImageBackground(UserIdentity user, InputStream is) throws IOException
    {
        BufferedImage image = ImageIO.read(is);
        Source bgSource = null;
        try
        {
            bgSource = _getInitialsBackgroundSource(user);
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
     * @param user The user
     * @return The source
     * @throws IOException If an error occurs while resolving the source uri
     */
    protected Source _getInitialsBackgroundSource(UserIdentity user) throws IOException
    {
        // Hashing the login then choose a background given the available ones.
        long hash = Math.abs(HashUtil.hash(user.getLogin()));
        
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
        synchronized (DefaultProfileImageProvider.class)
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
    }
}

