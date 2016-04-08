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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.util.ImageHelper;
import org.ametys.plugins.core.ui.user.ProfileImageProvider.ProfileImageSource;
import org.ametys.plugins.core.ui.user.ProfileImageProvider.UserProfileImage;

/**
 * {@link Reader} for generating the user profile image stored in the 'profile-image' user pref.
 * The source of the image can also be forced to retrieve the image of a given source (gravatar, initial, upload etc...)
 */
public class ProfileImageReader extends ServiceableReader
{
    /** Allowed image format */
    public static final Collection<String> ALLOWED_IMG_FORMATS = Arrays.asList("png", "gif", "jpg", "jpeg");
    
    /** Current user provider */
    protected CurrentUserProvider _currentUserProvider;
    
    /** User profile image provider */
    protected ProfileImageProvider _profileImageProvider;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        // Delayed initialized to ensure safe mode do not fail to load
        if (_profileImageProvider == null)
        {
            try
            {
                _profileImageProvider = (ProfileImageProvider) manager.lookup(ProfileImageProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException("Lazy initialization failed.", e);
            }
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        Response response = ObjectModelHelper.getResponse(objectModel);
        
        String imageSourceStr = StringUtils.defaultString(parameters.getParameter("image-source", null));
        
        ProfileImageSource profileImageSource = _profileImageProvider.getProfileImageSource(imageSourceStr);
        if (profileImageSource == null)
        {
            profileImageSource = ProfileImageSource.USERPREF; // default
        }
        
        String login =  parameters.getParameter("login", StringUtils.EMPTY);
        String populationId =  parameters.getParameter("population", StringUtils.EMPTY);
        UserIdentity user;
        // Default to current user if login not provided, except for the default source for which a login is not needed.
        if (!ProfileImageSource.DEFAULT.equals(profileImageSource) && (StringUtils.isEmpty(login) || StringUtils.isEmpty(populationId)))
        {
            user =  _currentUserProvider.getUser();
        }
        else
        {
            user = new UserIdentity(login, populationId);
        }
        
        boolean download = parameters.getParameterAsBoolean("download", false);
        
        // parameters for image resizing, size of the sides of the image (square)
        int size = parameters.getParameterAsInteger("size", 0);
        int maxSize = parameters.getParameterAsInteger("maxSize", 0);
        
        // Get parameters for source
        Map<String, Object> sourceParams = _extractSourceParameters(request, user, profileImageSource);
        
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
            
            image = _profileImageProvider.getImage(profileImageSource, user, sourceParams);
            
            if (image == null && ProfileImageSource.USERPREF.equals(profileImageSource))
            {
                // Reading from userpref, but no userpref set.
                // Try gravatar, then initials
                image = _profileImageProvider.getGravatarImage(user, size > 0 ? size : maxSize);
                if (image == null)
                {
                    image = _profileImageProvider.getInitialsImage(user);
                }
            }
        }
        
        if (image == null)
        {
            image = _profileImageProvider.getDefaultImage();
            
            // still null?
            if (image == null)
            {
                throw new ProcessingException(String.format("Not able to provide an image from source '%s' for user '%s' because no image was found.", profileImageSource, user));
            }
        }
        
        // Read the image
        try
        {
            _readImage(response, user, image, download, size, maxSize);
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
    
    /**
     * Provides the necessary parameters to retrieves the image from a given source.
     * @param request The request
     * @param user The user
     * @param profileImageSource The image source type
     * @return A map of parameters
     * @throws ResourceNotFoundException In case of a unhandled source type or if parameters could not be extracted 
     */
    
    protected Map<String, Object> _extractSourceParameters(Request request, UserIdentity user, ProfileImageSource profileImageSource) throws ResourceNotFoundException
    {
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
                    getLogger().warn(String.format("Cannot extract image source parameters for user '%s'. Unhandled profile image source '%s'", user, source));
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
     * Read the image from an input stream
     * @param response The response
     * @param user The user
     * @param image The user profile image to read
     * @param download To request a download
     * @param size The desired size
     * @param maxSize The max size
     * @throws IOException If an I/O error occurs while manipulating streams
     */
    protected void _readImage(Response response, UserIdentity user, UserProfileImage image, boolean download, int size, int maxSize) throws IOException
    {
        try (InputStream is = image.getInputstream())
        {
            String filename = StringUtils.defaultIfEmpty(image.getFilename(), user.getLogin() + ".png");
            String format = FilenameUtils.getExtension(filename);
            
            format = ALLOWED_IMG_FORMATS.contains(format) ? format : "png";
            
            if (download)
            {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            }
            
            if (size > 0 || maxSize > 0)
            {
                ImageHelper.generateThumbnail(is, out, format, size, size, maxSize, maxSize);
            }
            else
            {
                Long length = image.getLength();
                if (length != null && length > 0)
                {
                    response.setHeader("Content-Length", Long.toString(length));
                }
                
                IOUtils.copy(is, out);
            }
        }
    }
}
