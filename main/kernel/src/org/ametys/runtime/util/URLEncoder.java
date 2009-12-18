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
package org.ametys.runtime.util;

import java.io.UnsupportedEncodingException;

/**
 * Utility class for encoding URL.
 */
public final class URLEncoder
{
    private URLEncoder()
    {
        // empty
    }
    
    /**
     * Encode an URL using UTF-8 encoding, except that spaces are encoded using
     * <code>%20</code>.
     * @param url the URL to encode.
     * @return the URL encoded.
     */
    public static String encode(String url)
    {
        try
        {
            String encodedUrl = java.net.URLEncoder.encode(url, "UTF-8"); 
            return encodedUrl.replaceAll("\\+", "%20");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unable to encode URL", e);
        }
    }
}
