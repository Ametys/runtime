/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 * Manager for retrieving uploaded files.
 */
public interface UploadManager
{
    /** Avalon role. */
    public static final String ROLE = UploadManager.class.getName();
    
    /**
     * Stores a file uploaded by an user.
     * @param login the user login.
     * @param filename the upload filename.
     * @param is the upload data.
     * @return the upload.
     * @throws IOException if an error occurs.
     */
    Upload storeUpload(String login, String filename, InputStream is) throws IOException;
    
    /**
     * Retrieves a previous file uploaded by an user.
     * @param login the user login.
     * @param id the upload id.
     * @return the upload.
     * @throws NoSuchElementException if there is no upload
     *                                for this parameters.
     */
    Upload getUpload(String login, String id) throws NoSuchElementException;
}
