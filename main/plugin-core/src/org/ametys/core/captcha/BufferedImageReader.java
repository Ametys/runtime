/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.core.captcha;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.AbstractReader;
import org.xml.sax.SAXException;

/**
 * Read an image captcha given by its key
 */
public class BufferedImageReader extends AbstractReader
{
    /** The name of the request attribute which must contains the image */
    public static final String REQUEST_ATTRIBUTE = BufferedImageReader.class.getName();
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        BufferedImage image = (BufferedImage) request.getAttribute(REQUEST_ATTRIBUTE);
        
        ImageIO.write(image, "PNG", out);
        out.flush();
        out.close();
    }

    @Override
    public String getMimeType()
    {
        return "image/png";
    }
}
