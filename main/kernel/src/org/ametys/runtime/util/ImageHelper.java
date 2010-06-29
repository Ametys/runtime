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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

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
        
        int destHeight = srcHeight;
        int destWidth = srcWidth;
        
        if (height > 0)
        {
            // heigth is specified
            destHeight = height;
            
            if (width > 0)
            {
                // additionnally, height is also specified
                destWidth = width;
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
                double destRatio = 1.0 * maxWidth / maxHeight;
                double srcRatio = 1.0 * srcWidth / srcHeight;
                if (destRatio < srcRatio)
                {
                    destWidth = maxWidth;
                    destHeight = srcHeight * destWidth / srcWidth;
                }
                else
                {
                    destHeight = maxHeight;
                    destWidth = srcWidth * destHeight / srcHeight;
                }
            }
            else
            {
                destHeight = maxHeight;
                destWidth = srcWidth * destHeight / srcHeight;
            }
        }
        else if (maxWidth > 0)
        {
            destWidth = maxWidth;
            destHeight = srcHeight * destWidth / srcWidth;
        }
        
        BufferedImage thumbImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
//        Boolean finished = false;
//        ImageObserver observer = new ImageObserver()
//        {
//            @Override
//            public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h)
//            {
//                finished = (infoflags & ImageObserver.ALLBITS) != 0;
//                return finished;
//            }
//        };

        boolean finished = graphics2D.drawImage(src, 0, 0, destWidth, destHeight, null);
        
        if (!finished)
        {
            LoggerFactory.getLoggerFor(ImageHelper.class.getName()).warn("drawImage not finished for image " + src);
        }
        
        return thumbImage;
    }
    
    
}
