/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.cocoon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.avalon.framework.parameters.Parameters;

import org.ametys.core.util.ImageHelper;

/**
 * Helper for image resources generation
 */
public final class ImageResourceHelper
{
    private ImageResourceHelper()
    {
    }
    
    /**
     * Generate an image based on parameters
     * @param is The input data
     * @param par The parameters
     * @param format The image format
     * @return The image formated.
     * @throws IOException If an error occurs
     */
    public static InputStream generateImage(InputStream is, Parameters par, String format) throws IOException
    {
        int width = par.getParameterAsInteger("width", 0);
        int height = par.getParameterAsInteger("height", 0);
        int maxWidth = par.getParameterAsInteger("maxWidth", 0);
        int maxHeight = par.getParameterAsInteger("maxHeight", 0);
        
        if (width == 0 && height == 0 && maxWidth == 0 && maxHeight == 0)
        {
            return is;
        }
        
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            ImageHelper.generateThumbnail(is, os, format, height, width, maxHeight, maxWidth);
            
            return new ByteArrayInputStream(os.toByteArray());
        }
    }
    
    /**
     * Generate a serializable key from a resource image and its parameters 
     * @param key The resource key
     * @param par The resource parameters
     * @return The key by parameters
     */
    public static String getSerializableKey(String key, Parameters par)
    {
        String width = par.getParameter("width", "0");
        String height = par.getParameter("height", "0");
        String maxWidth = par.getParameter("maxWidth", "0");
        String maxHeight = par.getParameter("maxHeight", "0");
        
        return key + "###" + width + "x" + height + "x" + maxWidth + "x" + maxHeight;
    }
}
