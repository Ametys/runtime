/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;

/**
 * A collection of String management utility methods.
 */
public final class StringUtils
{
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
        Collection<String> result = new ArrayList<String>();
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
     * @return The password encrypted.
     * @throws NoSuchAlgorithmException If the md5 algorithm is not found.
     */
    public static String md5Base64(String password) throws NoSuchAlgorithmException
    {
        if (password == null)
        {
            return null;
        }
        
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        
        // Crypter le mot de passe avec l'algorithme MD5
        md5.reset();
        md5.update(password.getBytes());
        byte [] hash = md5.digest();
        
        // Encodé le résultat en base 64
        return new String(Base64.encodeBase64(hash));
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
