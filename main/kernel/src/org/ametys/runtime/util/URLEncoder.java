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
