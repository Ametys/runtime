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
        _profileImageProvider = (ProfileImageProvider) sm.lookup(ProfileImageProvider.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, Object> jsParameters = _serverCommHelper.getJsParameters();

        String login = StringUtils.defaultIfEmpty((String) jsParameters.get("login"), _currentUserProvider.getUser());
        
        List<Map<String, Object>> images = new ArrayList<>();
        
        // Stored in userpref
        Map<String, Object> rawUserPrefImage = _addUserPrefImage(images, login);
        
        // Gravatar
        _addGravatarImage(images, login, rawUserPrefImage);
        
        // Initials
        _addInitialsImage(images, login, rawUserPrefImage);
        
        // Local images
        _addLocalImages(images, login, rawUserPrefImage);
        
        // Default
        _addDefaultImage(images, rawUserPrefImage);
        
        Map<String, Object> result = new HashMap<>();
        result.put("images", images);

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }
    
    /**
     * Add the userpref image
     * @param images The image list accumulator
     * @param login The user login
     * @return The map stored in the user pref
     */
    protected Map<String, Object> _addUserPrefImage(List<Map<String, Object>> images, String login)
    {
        Map<String, Object> rawUserPrefImage = _profileImageProvider.hasUserPrefImage(login);
        
        if (rawUserPrefImage != null)
        {
            Map<String, Object> image = new HashMap<>();
            image.put("source", ProfileImageSource.USERPREF.name().toLowerCase());
            image.put("userPrefSource", rawUserPrefImage.get("source"));
            
            images.add(image);
        }
        
        return rawUserPrefImage;
    }

    /**
     * Add the gravatar image to the list if existing
     * @param images The image list accumulator
     * @param login The user login
     * @param rawUserPrefImage The map stored in the user pref
     */
    protected void _addGravatarImage(List<Map<String, Object>> images, String login, Map<String, Object> rawUserPrefImage)
    {
        String gravatarSource = ProfileImageSource.GRAVATAR.name().toLowerCase();
        
        // Add only if not already the user preference and the gravatar image exists
        if (!gravatarSource.equals(rawUserPrefImage.get("source")) && _profileImageProvider.hasGravatarImage(login, null))
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
     * @param rawUserPrefImage The map stored in the user pref
     */
    protected void _addInitialsImage(List<Map<String, Object>> images, String login, Map<String, Object> rawUserPrefImage)
    {
        String initialsSource = ProfileImageSource.INITIALS.name().toLowerCase();
        
        // Add only if not already the user preference and the initials image exists
        if (!initialsSource.equals(rawUserPrefImage.get("source")) && _profileImageProvider.hasInitialsImage(login))
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
     * @param rawUserPrefImage The map stored in the user pref
     */
    protected void _addLocalImages(List<Map<String, Object>> images, String login, Map<String, Object> rawUserPrefImage)
    {
        List<String> localImageIds = _profileImageProvider.getLocalImageIds();
        String localImageSource = ProfileImageSource.LOCALIMAGE.name().toLowerCase();
        
        boolean userPrefIsLocalImage = localImageSource.equals(rawUserPrefImage.get("source"));
        String userPrefIdParam = _extractIdParam(rawUserPrefImage);
        
        for (String id : localImageIds)
        {
            // Add only if not already the user preference and the local image exists
            if (!(userPrefIsLocalImage && id.equals(userPrefIdParam)) && _profileImageProvider.hasLocalImage(id))
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
     * Extract the id for the raw user pref image map
     * @param rawUserPrefImage The raw user pref image map
     * @return The id parameter or null
     */
    protected String _extractIdParam(Map<String, Object> rawUserPrefImage)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) rawUserPrefImage.get("parameters");
        return params != null ? (String) params.get("id") : null;
    }

    /**
     * Add the default image
     * @param images The image list accumulator
     * @param rawUserPrefImage The map stored in the user pref
     */
    protected void _addDefaultImage(List<Map<String, Object>> images, Map<String, Object> rawUserPrefImage)
    {
        String defaultSource = ProfileImageSource.DEFAULT.name().toLowerCase();
        
        // Add only if not already the user preference
        if (!defaultSource.equals(rawUserPrefImage.get("source")))
        {
            Map<String, Object> image = new HashMap<>();
            image.put("source", defaultSource);
            
            images.add(image);
        }
    }
}

