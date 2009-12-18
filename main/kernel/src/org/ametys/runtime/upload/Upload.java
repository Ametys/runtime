/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.upload;

import java.io.InputStream;
import java.util.Date;

/**
 * Access to a file uploaded previously.
 */
public interface Upload
{
    /**
     * Retrieves the upload id.
     * @return the upload id.
     */
    String getId();
    
    /**
     * Retrieves the uploaded date.
     * @return the uploaded date.
     */
    Date getUploadedDate();
    
    /**
     * Retrieves the filename.
     * @return the filename.
     */
    String getFilename();

    /**
     * Retrieves the mime type.
     * @return the mime type.
     */
    String getMimeType();

    /**
     * Retrieves the data length.
     * @return the data length in bytes.
     */
    long getLength();
    
    /**
     * Retrieves the input stream.<p>
     * Each call will return a new {@link InputStream}.
     * @return the input stream of the data.
     */
    InputStream getInputStream();
}
