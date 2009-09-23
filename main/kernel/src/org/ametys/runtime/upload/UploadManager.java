package org.ametys.runtime.upload;

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
