package org.ametys.runtime.plugins.core.upload.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.upload.Upload;
import org.ametys.runtime.upload.UploadManager;
import org.ametys.runtime.user.CurrentUserProvider;

/**
 * {@link Reader} for generating binary output of previously
 * uploaded file.
 */
public class UploadReader extends ServiceableReader
{
    private CurrentUserProvider _currentUserProvider;
    private UploadManager _uploadManager;
    private Upload _upload;

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
        String uploadId = source;
        
        if (uploadId.indexOf('/') != -1)
        {
            uploadId = uploadId.substring(0, uploadId.indexOf('/'));
        }
        
        try
        {
            _upload = _uploadManager.getUpload(_currentUserProvider.getUser(), uploadId);
        }
        catch (NoSuchElementException e)
        {
            // Invalid upload id
        }
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
        response.setHeader("Content-Length", Long.toString(_upload.getLength()));
        InputStream is = _upload.getInputStream();
        
        try
        {
            // Copy data in response
            IOUtils.copy(is, out);
        }
        finally
        {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }
}
