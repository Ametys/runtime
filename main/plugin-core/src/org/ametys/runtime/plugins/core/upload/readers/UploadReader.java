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
package org.ametys.runtime.plugins.core.upload.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.upload.Upload;
import org.ametys.runtime.upload.UploadManager;
import org.ametys.runtime.user.CurrentUserProvider;
import org.ametys.runtime.util.ImageHelper;

/**
 * {@link Reader} for generating binary output of previously
 * uploaded file.
 */
public class UploadReader extends ServiceableReader
{
    private CurrentUserProvider _currentUserProvider;
    private UploadManager _uploadManager;
    private Upload _upload;
    
    private int _width;
    private int _height;
    private int _maxWidth;
    private int _maxHeight;
    
    private boolean _readForDownload;
    private Collection<String> _allowedFormats = Arrays.asList(new String[]{"png", "gif", "jpg", "jpeg"});

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
        _uploadManager = (UploadManager) serviceManager.lookup(UploadManager.ROLE);
    }
    
    @Override
    public void recycle()
    {
        super.recycle();
        _upload = null;
    }
    
    @Override
    public void setup(SourceResolver res, Map objModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        super.setup(res, objModel, src, par);
        
        Request request = ObjectModelHelper.getRequest(objModel);
        String uploadId = request.getParameter("id");

        try
        {
            _upload = _uploadManager.getUpload(_currentUserProvider.getUser(), uploadId);
        }
        catch (NoSuchElementException e)
        {
            // Invalid upload id
            getLogger().warn("Cannot find the temporary uploaded file with id " + (uploadId != null ? "'" + uploadId + "'" : "<null>"));
        }
        
        _readForDownload = par.getParameterAsBoolean("download", false);
        
        // parameters for image resizing
        _width = par.getParameterAsInteger("width", 0);
        _height = par.getParameterAsInteger("height", 0);
        _maxWidth = par.getParameterAsInteger("maxWidth", 0);
        _maxHeight = par.getParameterAsInteger("maxHeight", 0);
    }
    
    @Override
    public long getLastModified()
    {
        if (_upload != null)
        {
            return _upload.getUploadedDate().getTime();
        }
        
        return super.getLastModified();
    }
    
    @Override
    public String getMimeType()
    {
        if (_upload != null)
        {
            return _upload.getMimeType();
        }

        return super.getMimeType();
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        if (_upload == null)
        {
            throw new ResourceNotFoundException("No upload for source: " + source);
        }
        
        Response response = ObjectModelHelper.getResponse(objectModel);
        
        if (_readForDownload)
        {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + _upload.getFilename() + "\"");
        }
        
        InputStream is = _upload.getInputStream();
        
        try
        {
            if (_width > 0 || _height > 0 || _maxHeight > 0 || _maxWidth > 0)
            {
                // it's an image, which must be resized
                int i = _upload.getFilename().lastIndexOf('.');
                String format = i != -1 ? _upload.getFilename().substring(i + 1) : "png";
                format = _allowedFormats.contains(format) ? format : "png";
                
                ImageHelper.generateThumbnail(is, out, format, _height, _width, _maxHeight, _maxWidth);
            }
            else
            {
                // Copy data in response
                response.setHeader("Content-Length", Long.toString(_upload.getLength()));
                IOUtils.copy(is, out);
            }
        }
        finally
        {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }
}
