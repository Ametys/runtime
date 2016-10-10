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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.Request;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.upload.Upload;
import org.ametys.core.upload.UploadManager;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.userpref.UserPreferencesErrors;
import org.ametys.core.util.JSONUtils;
import org.ametys.plugins.core.ui.user.DefaultProfileImageProvider.ProfileImageSource;
import org.ametys.plugins.core.userpref.SetUserPreferencesAction;

/**
 * Action which saves the user profile in user preferences
 */
public class SetUserProfileAction extends SetUserPreferencesAction
{
    /** JSON Utils */
    protected JSONUtils _jsonUtils;
    
    /** User profile image provider */
    protected DefaultProfileImageProvider _profileImageProvider;
    
    /** Upload manager */
    protected UploadManager _uploadManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    protected Map<String, String> _getValues(Request request, Map<String, String> contextVars, UserIdentity user, Collection<String> preferenceIds, UserPreferencesErrors errors)
    {
        // Delayed initialized to ensure safe mode do not fail to load
        if (_profileImageProvider == null)
        {
            try
            {
                _uploadManager = (UploadManager) manager.lookup(UploadManager.ROLE);
                _profileImageProvider = (DefaultProfileImageProvider) manager.lookup(ProfileImageProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException("Lazy initialization failed.", e);
            }
        }
        
        Map<String, String> preferences = super._getValues(request, contextVars, user, preferenceIds, errors);
        
        String userPrefImageJson = preferences.get(DefaultProfileImageProvider.USERPREF_PROFILE_IMAGE);
        if (StringUtils.isNotEmpty(userPrefImageJson))
        {
            Map<String, Object> userPrefImage = null;
            
            // Handle upload case.
            try
            {
                userPrefImage = _jsonUtils.convertJsonToMap(userPrefImageJson);
            }
            catch (Exception e)
            {
                getLogger().error(String.format("Unable to extract image user pref for user '%s'.", user), e);
            }
            
            if (userPrefImage != null)
            {
                // If upload, need to be cropped and stored as base64.
                ProfileImageSource profileImageSource = _profileImageProvider.getProfileImageSource((String) userPrefImage.get("source"));
                if (ProfileImageSource.USERPREF.equals(profileImageSource))
                {
                    // Nothing to do, userpref has not changed
                    preferences.remove(DefaultProfileImageProvider.USERPREF_PROFILE_IMAGE);
                }
                else if (ProfileImageSource.UPLOAD.equals(profileImageSource))
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sourceParams = (Map<String, Object>) userPrefImage.get("parameters");
                    String uploadId = sourceParams != null ? (String) sourceParams.get("id") : null;
                    
                    Map<String, Object> base64SourceParams = null;
                    
                    if (StringUtils.isNotEmpty(uploadId))
                    {
                        Upload upload = null;
                        try
                        {
                            upload = _uploadManager.getUpload(user, uploadId);
                            
                            base64SourceParams = new HashMap<>();
                            try (InputStream is = upload.getInputStream())
                            {
                                String filename = upload.getFilename();
                                base64SourceParams.put("data", _convertFile(filename, is));
                                base64SourceParams.put("filename", filename);
                            }
                            catch (IOException e)
                            {
                                base64SourceParams = null;
                                
                                getLogger().error(
                                        String.format("Unable to store the profile image user pref for user '%s'. Error while trying to convert the uploaded file '%s' to base64.",
                                                user, uploadId), e); 
                            }
                        }
                        catch (NoSuchElementException e)
                        {
                            // Invalid upload id
                            getLogger().error(String.format("Cannot find the temporary uploaded file for id '%s' and login '%s'.", uploadId, user), e);
                        }
                    }
                    
                    if (base64SourceParams != null)
                    {
                        Map<String, Object> base64UserPrefImage = new HashMap<>();
                        base64UserPrefImage.put("source", ProfileImageSource.BASE64.name().toLowerCase());
                        base64UserPrefImage.put("parameters", base64SourceParams);
                        
                        preferences.put(DefaultProfileImageProvider.USERPREF_PROFILE_IMAGE, _jsonUtils.convertObjectToJson(base64UserPrefImage));
                    }
                    else
                    {
                        // Upload seems corrupted, remove this data from the values.
                        preferences.remove(DefaultProfileImageProvider.USERPREF_PROFILE_IMAGE);
                    }
                }
            }
        }
        
        return preferences;
    }
    
    /**
     * Convert the uploaded file to base64.
     * Also automatically crop the image to 64x64 pixels.
     * @param filename The file name
     * @param is The input stream of the uploaded file
     * @return The base64 string
     * @throws IOException If an exception occurs while manipulating streams
     */
    protected String _convertFile(String filename, InputStream is) throws IOException
    {
        // Crop the image the get a square image, vertically centered to the input image.
        BufferedImage image = _profileImageProvider.cropUploadedImage(is);
        
        // Base64 encoding
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            String format = FilenameUtils.getExtension(filename);
            format = ProfileImageReader.ALLOWED_IMG_FORMATS.contains(format) ? format : "png";
            
            ImageIO.write(image, format, baos);
            return Base64.encodeBase64URLSafeString(baos.toByteArray());
        }
    }
}
