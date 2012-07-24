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
package org.ametys.runtime.plugins.core.upload.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.ametys.runtime.cocoon.JSonReader;
import org.ametys.runtime.upload.Upload;
import org.ametys.runtime.upload.UploadManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;

/**
 * {@link Action} for uploading a file and store it using the {@link UploadManager}.
 */
public class UploadAction extends CurrentUserProviderServiceableAction
{
    private UploadManager _uploadManager;
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        //Lazy initialize the upload manager because it cannot be found if the config is not complete  
        if (_uploadManager == null)
        {
            _uploadManager = (UploadManager) manager.lookup(UploadManager.ROLE);
        }
        
        Part partUploaded = (Part) request.get("file");
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        if (partUploaded == null)
        {
            throw new Exception("Missing request parameter file");
        }

        if (partUploaded.isRejected())
        {
            result.put("success", false);
            result.put("error", "rejected");
        }
        else
        {
            Upload upload = null;
    
            try
            {
                upload = _uploadManager.storeUpload(_getCurrentUser(), partUploaded.getFileName(), partUploaded.getInputStream());
                
                result.put("success", true);
                result.put("id", upload.getId());
                result.put("filename", upload.getFilename());
                result.put("size", upload.getLength());
                result.put("viewHref", _getUrlForView(upload));
                result.put("downloadHref", _getUrlForDownload(upload));
            }
            catch (IOException e)
            {
                getLogger().error("Unable to store uploaded file: " + partUploaded, e);
                
                result.put("success", false);
                
                Map<String, String> ex = new HashMap<String, String>();
                ex.put("message", e.getMessage());
                ex.put("stacktrace", ExceptionUtils.getFullStackTrace(e));

                result.put("error", ex);
            }
        }
        
        request.setAttribute(JSonReader.MAP_TO_READ, result);

        return EMPTY_MAP;
    }
    
    /**
     * Get the url for view the uploaded file
     * @param upload The file uploaded
     * @return The url for view
     */
    protected String _getUrlForView (Upload upload)
    {
        return "/plugins/core/upload/file?id=" + upload.getId();
    }
    
    /**
     * Get the url for download the uploaded file
     * @param upload The file uploaded
     * @return The url for view
     */
    protected String _getUrlForDownload (Upload upload)
    {
        return "/plugins/core/upload/file?id=" + upload.getId() + "&download=true";
    }
}
