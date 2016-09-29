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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.util.ImageHelper;
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
    private ProfileImageProvider _profileImageProvider;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
    }
    
    private ProfileImageProvider getProfileImageProvider()
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
        
        return _profileImageProvider;
    }
    
    @Override
    public void generate() throws IOException, ProcessingException
    {
        String imageSourceStr = StringUtils.defaultString(parameters.getParameter("image-source", null));
        UserIdentity user = _getUser();
        boolean download = parameters.getParameterAsBoolean("download", false);
        // parameters for image resizing, size of the sides of the image (square)
        int size = parameters.getParameterAsInteger("size", 0);
        int maxSize = parameters.getParameterAsInteger("maxSize", 0);

        UserProfileImage image = getProfileImageProvider().getImage(user, imageSourceStr, size, maxSize);
        
        // Read the image
        try
        {
            Response response = ObjectModelHelper.getResponse(objectModel);
            _readImage(response, user, image, download, size, maxSize);
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
    
    private UserIdentity _getUser()
    {
        String login =  parameters.getParameter("login", StringUtils.EMPTY);
        String populationId =  parameters.getParameter("population", StringUtils.EMPTY);
        // Default to current user if login not provided, except for the default source for which a login is not needed.
        if (StringUtils.isEmpty(login) || StringUtils.isEmpty(populationId))
        {
            return _currentUserProvider.getUser();
        }
        else
        {
            return new UserIdentity(login, populationId);
        }
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
