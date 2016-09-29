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

import java.io.InputStream;

import org.apache.cocoon.ProcessingException;

import org.ametys.core.user.UserIdentity;

/**
 * Component providing images that are used for user profiles
 */
public interface ProfileImageProvider
{
    /** Avalon role */
    public static final String ROLE = ProfileImageProvider.class.getName();
    
    /**
     * Get the avatar
     * @param user The user
     * @param imageSource The image source. Can be null to get the default one
     * @param size The size in px. Can be 0.
     * @param maxSize The maxSize in px. Can be 0.
     * @return The image
     * @throws ProcessingException If an error occurred
     */
    public UserProfileImage getImage(UserIdentity user, String imageSource, int size, int maxSize) throws ProcessingException;
    
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
