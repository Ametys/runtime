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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.user.UserIdentity;

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
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _sourceResolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
    }
    
    public UserProfileImage getImage(UserIdentity user, String imageSource, int size, int maxSize) throws ProcessingException
    {
        return getDefaultImage();
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
}
