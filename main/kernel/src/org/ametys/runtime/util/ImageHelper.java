/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;
import net.coobird.thumbnailator.resizers.ResizerFactory;

/**
 * Helper for manipulating images.
 */
public final class ImageHelper
{
    private ImageHelper()
    {
        // empty constructor
    }
    
    /**
     * Generates a thumbnail from a source InputStream.
     * @param is the source.
     * @param os the destination.
     * @param format the image format. Must be one of "gif", "png" or "jpg".
     * @param height the specified height. Ignored if negative.
     * @param width the specified width. Ignored if negative.
     * @param maxHeight the maximum image height. Ignored if height or width is specified.
     * @param maxWidth the maximum image width. Ignored if height or width is specified.
     * @throws IOException if an error occurs when manipulating streams.
     */
    public static void generateThumbnail(InputStream is, OutputStream os, String format, int height, int width, int maxHeight, int maxWidth) throws IOException
    {
        BufferedImage src = ImageIO.read(is);
        BufferedImage dest = generateThumbnail(src, height, width, maxHeight, maxWidth);
        
        ImageIO.write(dest, format, os);
    }
    
    /**
     * Generates a BufferedImage with specified size instructions, scaling if necessary.<br>
     * @param src the source image.
     * @param height the specified height. Ignored if negative.
     * @param width the specified width. Ignored if negative.
     * @param maxHeight the maximum image height. Ignored if height or width is specified.
     * @param maxWidth the maximum image width. Ignored if height or width is specified.
     * @return a scaled BufferedImage.
     */
    public static BufferedImage generateThumbnail(BufferedImage src, int height, int width, int maxHeight, int maxWidth)
    {
        int srcHeight = src.getHeight();
        int srcWidth = src.getWidth();
        
        int destHeight = 0;
        int destWidth = 0;
        
        boolean keepAspectRatio = true;
        
        if (height > 0)
        {
            // heigth is specified
            destHeight = height;
            
            if (width > 0)
            {
                // additionnally, height is also specified
                destWidth = width;
                keepAspectRatio = false;
            }
            else
            {
                // width is computed
                destWidth = srcWidth * destHeight / srcHeight;
            }
        }
        else if (width > 0)
        {
            // width is specified, height is computed
            destWidth = width;
            destHeight = srcHeight * destWidth / srcWidth;
        }
        else if (maxHeight > 0)
        {
            if (maxWidth > 0)
            {
                if (srcHeight < maxHeight && srcWidth < maxWidth)
                {
                    // the source image is already smaller than the destination box
                    return src;
                }
                
                destWidth = maxWidth;
                destHeight = maxHeight;
            }
            else
            {
                if (srcHeight < maxHeight)
                {
                    // the source image is already smaller than the destination box
                    return src;
                }
                
                destHeight = maxHeight;
                destWidth = srcWidth * destHeight / srcHeight;
            }
        }
        else if (maxWidth > 0)
        {
            if (srcWidth < maxWidth)
            {
                // the source image is already smaller than the destination box
                return src;
            }
            
            destWidth = maxWidth;
            destHeight = srcHeight * destWidth / srcWidth;
        }
        
        if (destHeight == srcHeight && destWidth == srcWidth)
        {
            // already the good format, don't change anything
            return src;
        }
        
        Dimension srcDimension = new Dimension(srcWidth, srcHeight);
        Dimension thumbnailDimension = new Dimension(destWidth, destHeight);
        
        BufferedImage thumbImage = new FixedSizeThumbnailMaker(destWidth, destHeight, keepAspectRatio)
                                   .resizer(ResizerFactory.getResizer(srcDimension, thumbnailDimension))
                                   .imageType(src.getType() != BufferedImage.TYPE_CUSTOM ? src.getType() : BufferedImage.TYPE_INT_ARGB)
                                   .make(src); 
        
        return thumbImage;
    }
}
