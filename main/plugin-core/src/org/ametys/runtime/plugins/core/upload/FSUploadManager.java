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
package org.ametys.runtime.plugins.core.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.ametys.runtime.upload.Upload;
import org.ametys.runtime.upload.UploadManager;

/**
 * {@link UploadManager} which stores uploaded files into
 * <code>/WEB-INF/data/uploads-user</code>.<p>
 * Note that this implementation is not cluster safe.
 */
public class FSUploadManager extends TimerTask implements UploadManager, ThreadSafe, Initializable, Contextualizable, LogEnabled, Disposable
{
    /** Global uploads directory. */
    public static final String UPLOADS_DIR = "/WEB-INF/data/uploads-user";

    /** Context. */
    protected org.apache.cocoon.environment.Context _context;
    /** Global uploads directory. */
    protected File _globalUploadsDir;
    /** Timer. */
    protected Timer _timer;
    /** Logger available to subclasses. */
    private Logger _logger;
    
    @Override
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        // Retrieve context-root from context
        _context = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _globalUploadsDir = new File(_context.getRealPath(UPLOADS_DIR));
    }
    
    @Override
    public void initialize() throws Exception
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Starting timer");
        }
        
        // Daemon thread
        _timer = new Timer("FSUploadManager", true);
        
        // Start in 15 minutes and refresh each 24 hours
        _timer.scheduleAtFixedRate(this, 15 * 60 * 1000, 24 * 60 * 60 * 1000);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void run()
    {
        if (_logger.isInfoEnabled())
        {
            _logger.info("Time to clean old uploads");
        }
        
        try
        {
            Calendar calendar = new GregorianCalendar();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            final long yesterday = calendar.getTimeInMillis();
            
            // Retrieve login directories
            String[] loginDirs = _globalUploadsDir.list(DirectoryFileFilter.INSTANCE);
            
            if (loginDirs != null)
            {
                for (String loginDir : loginDirs)
                {
                    File effectiveLoginDir = new File(_globalUploadsDir, loginDir);
                    // Compute upload directories to remove
                    String[] dirsToRemove = effectiveLoginDir.list(new AbstractFileFilter()
                    {
                        @Override
                        public boolean accept(File file)
                        {
                            if (!file.isDirectory())
                            {
                                return false;
                            }
                            
                            Collection<File> uploadFiles = FileUtils.listFiles(file, TrueFileFilter.INSTANCE, null);

                            if (uploadFiles.isEmpty())
                            {
                                // Remove empty directory
                                return true;
                            }
                            else
                            {
                                File firstFile = uploadFiles.iterator().next();
    
                                // Remove directory if the first file is one day old
                                return firstFile.lastModified() < yesterday;
                            }
                        }
                    });
                    
                    if (dirsToRemove != null)
                    {
                        for (String dirToRemove : dirsToRemove)
                        {
                            File uploadDir = new File(effectiveLoginDir, dirToRemove);
                            
                            if (_logger.isDebugEnabled())
                            {
                                _logger.debug("Removing directory: " + uploadDir);
                            }
            
                            FileUtils.deleteDirectory(uploadDir);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            _logger.error("Unable to clean old uploads", e);
        }
    }
    
    @Override
    public Upload storeUpload(String login, String filename, InputStream is) throws IOException
    {
        if (!_globalUploadsDir.exists())
        {
            if (!_globalUploadsDir.mkdirs())
            {
                throw new IOException("Unable to create directory: " + _globalUploadsDir);
            }
        }
        
        // Create unique id
        String id = UUID.randomUUID().toString();
        
        File uploadFile = null;

        try
        {
            uploadFile = new File(_getUploadDir(login, id), URLEncoder.encode(filename, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unsupported encoding", e);
        }
        
        if (_logger.isInfoEnabled())
        {
            _logger.info("Using file: " + uploadFile);
        }
        
        if (!uploadFile.getParentFile().mkdirs())
        {
            throw new IOException("Unable to create directory: " + uploadFile.getParent());
        }
        
        OutputStream os = new FileOutputStream(uploadFile);
        
        try
        {
            IOUtils.copy(is, os);
        }
        finally
        {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        
        return new FSUpload(_context, uploadFile);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Upload getUpload(String login, String id) throws NoSuchElementException
    {
        File uploadDir = _getUploadDir(login, id);
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Using directory: " + uploadDir);
        }
        
        if (!uploadDir.exists() || !uploadDir.isDirectory())
        {
            throw new NoSuchElementException("No directory: " + uploadDir);
        }
        
        Collection<File> files = FileUtils.listFiles(uploadDir, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE);
        
        if (_logger.isInfoEnabled())
        {
            _logger.info("Found files: " + files);
        }
        
        if (files.isEmpty())
        {
            throw new NoSuchElementException("No files in directory: " + uploadDir);
        }
        
        // Use first file found
        return new FSUpload(_context, files.iterator().next());
    }
    
    @Override
    public void dispose()
    {
        cancel();
        _timer.cancel();
        _globalUploadsDir = null;
        _logger = null;
    }
    
    /**
     * Retrieves the upload directory for a login and an upload id.
     * @param login the login.
     * @param id the upload id.
     * @return the upload directory.
     */
    protected File _getUploadDir(String login, String id)
    {
        try
        {
            return new File(new File(_globalUploadsDir, URLEncoder.encode(login, "UTF-8")), id);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }
    
    /**
     * {@link Upload} implementation on file system.
     */
    protected static class FSUpload implements Upload
    {
        private org.apache.cocoon.environment.Context _context;
        private File _file;
        
        /**
         * Creates a FSUpload from a file.
         * @param context the context.
         * @param file the file.
         */
        public FSUpload(org.apache.cocoon.environment.Context context, File file)
        {
            _context = context;
            _file = file;
        }

        @Override
        public String getId()
        {
            return _file.getParentFile().getName();
        }
        
        @Override
        public Date getUploadedDate()
        {
            return new Date(_file.lastModified());
        }
        
        @Override
        public String getFilename()
        {
            try
            {
                return URLDecoder.decode(_file.getName(), "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("Unsupported encoding", e);
            }
        }
        
        @Override
        public String getMimeType()
        {
            String mimeType = _context.getMimeType(getFilename());
            
            if (mimeType == null)
            {
                mimeType = "application/unknown";
            }
            
            return mimeType;
        }
        
        @Override
        public long getLength()
        {
            return _file.length();
        }
        
        @Override
        public InputStream getInputStream()
        {
            try
            {
                return new FileInputStream(_file);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Missing file: " + _file, e);
            }
        }
    }
}
