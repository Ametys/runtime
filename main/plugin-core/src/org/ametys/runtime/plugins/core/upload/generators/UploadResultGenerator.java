package org.ametys.runtime.plugins.core.upload.generators;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.upload.Upload;
import org.ametys.runtime.util.URLEncoder;

/**
 * {@link Generator} for SAXing the upload result.
 */
public class UploadResultGenerator extends AbstractGenerator
{
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String result = parameters.getParameter("result", "ko");
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String requestPath = request.getRequestURI();
        requestPath = requestPath.substring(0, requestPath.lastIndexOf('/'));
        Upload upload = (Upload) request.getAttribute("upload");
        String uploadId = upload.getId();
        AttributesImpl attrs = new AttributesImpl();
        
        if (result.equals("ok"))
        {
            attrs.addCDATAAttribute("id", uploadId);
            attrs.addCDATAAttribute("filename", upload.getFilename());
            attrs.addCDATAAttribute("href", request.getContextPath() + requestPath + "/get/" + uploadId + "/"
                   + URLEncoder.encode(upload.getFilename()));
        }
        else
        {
            attrs.addCDATAAttribute("reject", "true");
        }
        
        contentHandler.startDocument();
        XMLUtils.createElement(contentHandler, "upload", attrs);
        contentHandler.endDocument();
    }
}
