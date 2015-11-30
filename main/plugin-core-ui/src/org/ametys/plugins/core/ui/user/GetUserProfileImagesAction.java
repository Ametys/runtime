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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.util.ServerCommHelper;
import org.ametys.plugins.core.ui.user.ProfileImageProvider.ProfileImageSource;

/**
 * Retrieves the available user profile images
 */
public class GetUserProfileImagesAction extends ServiceableAction
{
    /** Servercomm helper */
    protected ServerCommHelper _serverCommHelper;
    
    /** Current user provider */
    protected CurrentUserProvider _currentUserProvider;
    
    /** User profile image provider */
    protected ProfileImageProvider _profileImageProvider;
    
    @Override
    public void service(ServiceManager sm) throws ServiceException
    {
        super.service(sm);
        _serverCommHelper = (ServerCommHelper) sm.lookup(ServerCommHelper.ROLE);
        _currentUserProvider = (CurrentUserProvider) sm.lookup(CurrentUserProvider.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
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
        
        Map<String, Object> jsParameters = _serverCommHelper.getJsParameters();

        String login = StringUtils.defaultIfEmpty((String) jsParameters.get("login"), _currentUserProvider.getUser());
        
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> images = new ArrayList<>();
        result.put("images", images);
        
        // Uploaded image
        _addUploadedImage(images, login);
        
        // Gravatar
        _addGravatarImage(images, login);
        
        // Initials
        _addInitialsImage(images, login);
        
        // Local images
        _addLocalImages(images, login);
        
        // Default
        _addDefaultImage(images);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }
    
    /**
     * Add the uploaded image from the userpref
     * @param images The image list accumulator
     * @param login The user login
     */
    protected void _addUploadedImage(List<Map<String, Object>> images, String login)
    {
        Map<String, Object> rawUserPrefImage = _profileImageProvider.hasUserPrefImage(login);
        
        // Only add user uploaded image here
        String uploadSource = ProfileImageSource.UPLOAD.name().toLowerCase();
        if (rawUserPrefImage != null && uploadSource.equals(rawUserPrefImage.get("source")))
        {
            Map<String, Object> image = new HashMap<>();
            image.put("source", uploadSource);
            
            images.add(image);
        }
    } 
    
    /**
     * Add the gravatar image to the list if existing
     * @param images The image list accumulator
     * @param login The user login
     */
    protected void _addGravatarImage(List<Map<String, Object>> images, String login)
    {
        String gravatarSource = ProfileImageSource.GRAVATAR.name().toLowerCase();
        
        if (_profileImageProvider.hasGravatarImage(login))
        {
            Map<String, Object> image = new HashMap<>();
            image.put("source", gravatarSource);
            
            images.add(image);
        }
    }
    
    /**
     * Add the image with initials to the list
     * @param images The image list accumulator
     * @param login The user login
     */
    protected void _addInitialsImage(List<Map<String, Object>> images, String login)
    {
        String initialsSource = ProfileImageSource.INITIALS.name().toLowerCase();
        
        if (_profileImageProvider.hasInitialsImage(login))
        {
            Map<String, Object> image = new HashMap<>();
            image.put("source", initialsSource);
            
            images.add(image);
        }
        
    }
    
    /**
     * Add the local images to the list
     * @param images The image list accumulator
     * @param login The user login
     */
    protected void _addLocalImages(List<Map<String, Object>> images, String login)
    {
        List<String> localImageIds = _profileImageProvider.getLocalImageIds();
        String localImageSource = ProfileImageSource.LOCALIMAGE.name().toLowerCase();
        
        for (String id : localImageIds)
        {
            if (_profileImageProvider.hasLocalImage(id))
            {
                Map<String, Object> image = new HashMap<>();
                image.put("source", localImageSource);
                
                // id parameter
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("id", id);
                
                image.put("parameters", parameters);
                
                images.add(image);
            }
        }
    }
    
    /**
     * Add the default image
     * @param images The image list accumulator
     */
    protected void _addDefaultImage(List<Map<String, Object>> images)
    {
        String defaultSource = ProfileImageSource.DEFAULT.name().toLowerCase();
        
        Map<String, Object> image = new HashMap<>();
        image.put("source", defaultSource);
        
        images.add(image);
    }
}

