/*
 *  Copyright 2015 Anyware Services
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Helper class providing methods to deal with common {@link Configuration} tasks.
 */
public final class ConfigurationHelper
{
    
    private ConfigurationHelper()
    {
        // Helper class, never to be instantiated.
    }
    
    /**
     * Parse an i18n text configuration, throwing an exception if empty.
     * @param config the configuration to use.
     * @param defaultCatalogue the i18n catalogue to use when not specified.
     * @return the i18n text.
     * @throws ConfigurationException if the configuration is not valid.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String defaultCatalogue) throws ConfigurationException
    {
        String text = config.getValue();
        return _parseI18nizableText(config, defaultCatalogue, text);
    }
    
    /**
     * Parse an i18n text configuration, with a default value.
     * @param config the configuration to use.
     * @param defaultCatalogue the i18n catalogue to use when not specified. 
     * @param defaultValue the default value.
     * @return the i18n text.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String defaultCatalogue, String defaultValue)
    {
        String text = config.getValue(defaultValue);
        return _parseI18nizableText(config, defaultCatalogue, text);
    }
    
    /**
     * Parse an i18n text configuration (can be a key or a "direct" string).
     * @param config The configuration to parse.
     * @param defaultCatalogue The i18n catalogue to use when not specified.  
     * @param text The i18n text, can be a key or a "direct" string.
     * @return
     */
    private static I18nizableText _parseI18nizableText(Configuration config, String defaultCatalogue, String text)
    {
        boolean isI18n = config.getAttributeAsBoolean("i18n", false);
        
        if (isI18n)
        {
            String catalogue = config.getAttribute("catalogue", defaultCatalogue);
            
            return new I18nizableText(catalogue, text);
        }
        else
        {
            return new I18nizableText(text);
        }
    }
    
}
