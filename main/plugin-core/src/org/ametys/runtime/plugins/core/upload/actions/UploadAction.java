package org.ametys.runtime.plugins.core.upload.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.servlet.multipart.Part;

import org.ametys.runtime.upload.Upload;
import org.ametys.runtime.upload.UploadManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;

/**
 * {@link Action} for uploading a file and store it using the
 * {@link UploadManager}.
 */
public class UploadAction extends CurrentUserProviderServiceableAction
{
    private UploadManager _uploadManager;

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _uploadManager = (UploadManager) serviceManager.lookup(UploadManager.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Part partUploaded = (Part) request.get("file");
        
        if (partUploaded == null)
        {
            throw new Exception("Missing request parameter file");
        }
        
        if (partUploaded.isRejected())
        {
            return Collections.singletonMap("result", "ko");
        }
        
        Upload upload = null;

        try
        {
            upload = _uploadManager.storeUpload(_getCurrentUser(), partUploaded.getFileName(),
                                                partUploaded.getInputStream());
        }
        catch (IOException e)
        {
            throw new Exception("Unable to store uploaded file: " + partUploaded, e);
        }
        
        request.setAttribute("upload", upload);
        return Collections.singletonMap("result", "ok");
    }
}
