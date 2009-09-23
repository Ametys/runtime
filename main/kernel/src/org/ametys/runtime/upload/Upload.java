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
