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
package org.ametys.core.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of String management utility methods.
 */
public final class StringUtils
{
    private static final Logger __LOGGER = LoggerFactory.getLogger(StringUtils.class);
    
    private StringUtils()
    {
        // empty private constructor
    }
    
    /**
     * Extract String values from a comma seprated list.
     * @param values the comma separated list
     * @return a collection of String or an empty collection if string is null or empty.
     */
    public static Collection<String> stringToCollection(String values)
    {
        Collection<String> result = new ArrayList<>();
        if ((values != null) && (values.length() > 0))
        {
            // Explore the string list with a stringtokenizer with ','.
            StringTokenizer stk = new StringTokenizer(values, ",");

            while (stk.hasMoreTokens())
            {
                // Don't forget to trim
                result.add(stk.nextToken().trim());
            }
        }

        return result;
    }

    /**
     * Extract String values from a comma seprated list.
     * @param values the comma separated list
     * @return an array of String
     */
    public static String[] stringToStringArray(String values)
    {
        Collection<String> coll = stringToCollection(values);
        return coll.toArray(new String[coll.size()]);
    }
    
    /**
     * Generates a unique String key, based on System.currentTimeMillis()
     * @return a unique String value
     */
    public static String generateKey()
    {
        long value;
        
        // Find a new value
        synchronized (StringUtils.class)
        {
            value = System.currentTimeMillis();

            try
            {
                Thread.sleep(15);
            }
            catch (InterruptedException e)
            {
                // does nothing, continue
            }
        }

        // Convert it to a string using radix 36 (more compact)
        String longString = Long.toString(value, Character.MAX_RADIX);
    
        return longString;
    }
    
    /**
     * Encrypte a password by using first MD5 Hash and base64 encoding.
     * @param password The password to be encrypted.
     * @return The password encrypted or null if the MD5 is not supported
     */
    public static String md5Base64(String password)
    {
        if (password == null)
        {
            return null;
        }
        
        MessageDigest md5;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            // This error exception not be raised since MD5 is embedded in the JDK
            __LOGGER.error("Cannot encode the password to md5Base64", e);
            return null;
        }
        
        // MD5-hash the password.
        md5.reset();
        try
        {
            md5.update(password.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException(e);
        }
        byte [] hash = md5.digest();
        
        // Base64-encode the result.
        try
        {
            return new String(Base64.encodeBase64(hash), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Encode an url using UTF-8 encoding, except that spaces are encoded using %20
     * @param url The url to encode
     * @return The url encoded
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
            return url;
        }
    }
}
