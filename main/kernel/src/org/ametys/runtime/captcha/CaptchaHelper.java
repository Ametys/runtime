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
package org.ametys.runtime.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.captcha.Captcha;
import nl.captcha.Captcha.Builder;
import nl.captcha.gimpy.DropShadowGimpyRenderer;
import nl.captcha.gimpy.FishEyeGimpyRenderer;
import nl.captcha.gimpy.RippleGimpyRenderer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.renderer.DefaultWordRenderer;

/**
 * Helper for generating image captcha to PNG format
 */
public final class CaptchaHelper
{
    private static final String STATIC_PREFIX_KEY = "STATIC-";
    private static final String DYNAMIC_PREFIX_KEY = "DYNAMIC-";
    
    private static Map<String, List<ValidableCaptcha>> _mapStaticCaptcha = new HashMap<String, List<ValidableCaptcha>>();
    private static Map<String, ValidableCaptcha> _mapDynamicCaptcha = new HashMap<String, ValidableCaptcha>();
    
    private CaptchaHelper ()
    {
        // Nothing
    }
    
    /**
     * Check a captcha
     * @param key The key
     * @param value The value to check
     * @return The image captcha
     */
    public static synchronized boolean checkAndInvalidate(String key, String value)
    {
        if (key.startsWith(STATIC_PREFIX_KEY))
        {
            boolean atLeastAnInvalid = false;
            boolean foundOne = false;
            
            List<ValidableCaptcha> list = _mapStaticCaptcha.get(key);
            if (list != null)
            {
                for (ValidableCaptcha c : list)
                {
                    if (c.isValid())
                    {
                        Captcha captcha = c.getCaptcha();
                        if (captcha.isCorrect(value))
                        {
                            foundOne = true;
        
                            c.invalidate();
                            atLeastAnInvalid = true;
                            
                            break;
                        }
                    }
                    else
                    {
                        atLeastAnInvalid = true;
                    }
                }
            }
            
            if (atLeastAnInvalid)
            {
                cleanOldCaptchas();
            }
            
            return foundOne;
        }
        else if (key.startsWith(DYNAMIC_PREFIX_KEY))
        {
            ValidableCaptcha vc = _mapDynamicCaptcha.get(key);
            if (vc == null)
            {
                return false;
            }
            else if (!vc.isValid())
            {
                _mapDynamicCaptcha.remove(key);
                return false;
            }
            else
            {
                _mapDynamicCaptcha.remove(key);

                Captcha c = vc.getCaptcha();
                return c.isCorrect(value);
            }
        }
        else
        {
            throw new IllegalArgumentException("The key '" + key + "' is not a valid captcha key because it does not starts with '" + DYNAMIC_PREFIX_KEY + "' or '" + STATIC_PREFIX_KEY + "'");
        }
    }
    
    /**
     * Remove a captcha
     * @param key the key value
     */
    public static synchronized void removeCaptcha (String key)
    {
        if (key.startsWith(STATIC_PREFIX_KEY))
        {
            _mapStaticCaptcha.remove(key);
        }
        else if (key.startsWith(DYNAMIC_PREFIX_KEY))
        {
            _mapDynamicCaptcha.remove(key);
        }
    }
    
    /**
     * Clean the outdated captchas
     */
    public static synchronized void cleanOldCaptchas()
    {
        _cleanOldStaticCaptchas();
        _cleanOldDynamicCaptchas();
    }
    
    private static synchronized void _cleanOldDynamicCaptchas()
    {
        Iterator<String> cIt = _mapDynamicCaptcha.keySet().iterator();
        while (cIt.hasNext())
        {
            String id = cIt.next();
            ValidableCaptcha vc = _mapDynamicCaptcha.get(id);
            if (!vc.isValid())
            {
                cIt.remove();
            }
        }
    }
    
    private static synchronized void _cleanOldStaticCaptchas()
    {
        Iterator<String> cIt = _mapStaticCaptcha.keySet().iterator();
        while (cIt.hasNext())
        {
            String id = cIt.next();
            List<ValidableCaptcha> c = _mapStaticCaptcha.get(id);
            
            Iterator<ValidableCaptcha> it = c.iterator();
            while (it.hasNext())
            {
                ValidableCaptcha vc  = it.next();
                if (!vc.isValid())
                {
                    it.remove();
                }
            }
            
            if (c.isEmpty())
            {
                cIt.remove();
            }
        }
    }

    /**
     * Generate an image captcha to PNG format. The key has to be unique.
     * If you can not give a unique id use generateImageCaptch without the key argument : but this is less secure.
     * @param key the wanted key. Can be not null. MUST START with "STATIC-" or "DYNAMIC-". If the key starts with 'STATIC-' this key may be used several times (e.g. for a cached page with a unique id for several display), if the key starts with 'DYNAMIC-' the key will unique (removing an existing captcha with the same key).
     * @return The corresponding image
     */
    public static BufferedImage generateImageCaptcha (String key)
    {
        return generateImageCaptcha(key, 0x000000);
    }
    
    /**
     * Generate an image captcha to PNG format. The key has to be unique.
     * If you can not give a unique id use generateImageCaptch without the key argument : but this is less secure.
     * @param key the wanted key. Can be not null. MUST START with "STATIC-" or "DYNAMIC-". If the key starts with 'STATIC-' this key may be used several times (e.g. for a cached page with a unique id for several display), if the key starts with 'DYNAMIC-' the key will unique (removing an existing captcha with the same key).
     * @param addNoise true to add noise to captcha image
     * @param fisheye true to add fish eye background to captcha image
     * @return The corresponding image
     */
    public static BufferedImage generateImageCaptcha (String key, boolean addNoise, boolean fisheye)
    {
        return generateImageCaptcha(key, 0x000000, addNoise, fisheye, 200, 50);
    }
    
    /**
     * Generate an image captcha to PNG format. The key has to be unique, if you cannot generate a key use the other form of the method.
     * @param key the wanted key. Can not be null. You can use RandomStringUtils.randomAlphanumeric(10) to generates one
     * @param color The color for font
     * @return The corresponding image
     */
    public static synchronized BufferedImage generateImageCaptcha (String key, Integer color)
    {
        return generateImageCaptcha(key, color, false, false, 200, 50);
    }
    
    /**
     * Generate an image captcha to PNG format. The key has to be unique, if you cannot generate a key use the other form of the method.
     * @param key the wanted key. Can not be null. You can use RandomStringUtils.randomAlphanumeric(10) to generates one
     * @param color The color for font
     * @param addNoise true to add noise to captcha image
     * @param fisheye true to add fish eye background to captcha image
     * @return The corresponding image
     */
    public static synchronized BufferedImage generateImageCaptcha (String key, Integer color, boolean addNoise, boolean fisheye)
    {
        return generateImageCaptcha (key, color, addNoise, fisheye, 200, 50);
    }
    
    /**
     * Generate an image captcha to PNG format. The key has to be unique, if you cannot generate a key use the other form of the method.
     * @param key the wanted key. Can not be null. You can use RandomStringUtils.randomAlphanumeric(10) to generates one
     * @param color The color for font
     * @param addNoise true to add noise to captcha image
     * @param fisheye true to add fish eye background to captcha image
     * @param width The image width
     * @param height The image height
     * @return The corresponding image
     */
    public static synchronized BufferedImage generateImageCaptcha (String key, Integer color, boolean addNoise, boolean fisheye, int width, int height)
    {
        Captcha captcha = _generateImageCaptcha(color, addNoise, fisheye, width, height);
        ValidableCaptcha vc = new ValidableCaptcha(captcha);
        
        if (key.startsWith(STATIC_PREFIX_KEY))
        {
            if (!_mapStaticCaptcha.containsKey(key))
            {   
                _mapStaticCaptcha.put(key, new ArrayList<ValidableCaptcha>());
            }
            List<ValidableCaptcha> captchas = _mapStaticCaptcha.get(key);
            captchas.add(new ValidableCaptcha(captcha));
        }
        else if (key.startsWith(DYNAMIC_PREFIX_KEY))
        {
            // If there were a key there, we scrach it! (this is a way to invalidate it - this may happen when the user do 'back' in its browser
            _mapDynamicCaptcha.put(key, vc);
        }
        else
        {
            throw new IllegalArgumentException("The key '" + key + "' is not a valid captcha key because it does not starts with '" + DYNAMIC_PREFIX_KEY + "' or '" + STATIC_PREFIX_KEY + "'");
        }
        
        return vc.getCaptcha().getImage();
    }
    
    private static Captcha _generateImageCaptcha(Integer color, boolean addNoise, boolean fisheye, int width, int height)
    {
        List<Color> colors = new ArrayList<Color>();
        colors.add(new Color(color));
        
        List<Font> fonts = new ArrayList<Font>();
        fonts.add(new Font("Arial", Font.BOLD, 40));
        fonts.add(new Font("Courier", Font.BOLD, 40));
        
        Builder builder = new Captcha.Builder(width, height)
            .addText(new DefaultTextProducer(6, "abcdefghijklmnopqrstuvwxyz".toCharArray()), new DefaultWordRenderer(colors, fonts))
            .gimp(new RippleGimpyRenderer())
            .addNoise(new CurvedLineNoiseProducer(new Color(color), 3));
        
        if (addNoise)
        {
            builder.addNoise(new CurvedLineNoiseProducer(new Color(color), 3))
                .gimp(new DropShadowGimpyRenderer());
        }
        
        if (fisheye)
        {
            builder.gimp(new FishEyeGimpyRenderer());
        }
        
        return builder.build();
    }
    
    /**
     * Bean for a captcha and a validity date 
     */
    static class ValidableCaptcha
    {
        private Captcha _captcha;
        private Date _date;
        private boolean _valid;
        
        /**
         * Build the captcha wrapper
         * @param c the captcha to wrap
         */
        public ValidableCaptcha(Captcha c)
        {
            _captcha = c;
            _date = new Date();
            _valid = true;
        }

        /**
         * Determine if the validity date has not expired
         * @return true if the captcha is still valid
         */
        public boolean isValid()
        {
            if (!_valid)
            {
                return false;
            }
            
            Calendar validity = new GregorianCalendar();
            validity.add(Calendar.MINUTE, -20);
            
            return _date.after(validity.getTime()); 
        }
        
        /**
         * Get the wrapper captcha
         * @return captcha
         */
        public Captcha getCaptcha()
        {
            return _captcha;
        }
        
        /**
         * Mark the captcha as invalid 
         */
        public void invalidate()
        {
            _valid = false;
        }
    }
}
