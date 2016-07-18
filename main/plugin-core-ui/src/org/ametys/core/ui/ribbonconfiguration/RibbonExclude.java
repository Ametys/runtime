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
package org.ametys.core.ui.ribbonconfiguration;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.slf4j.Logger;

/**
 * The class represent an exclusion declared in a ribbon file
 */
public class RibbonExclude
{
    /**
     * Type of exclusion in the ribbon configuration
     */
    public enum EXCLUDETYPE
    {
        /** Excluding an import */
        IMPORT("import"),
        /** Excluding an extension */
        APPMENU("app-menu"),
        /** Excluding a file */
        USERMENU("user-menu"),
        /** Excluding a tab */
        TAB("tab"),
        /** Excluding a control*/
        CONTROL("control");
        
        private String _value;
        
        private EXCLUDETYPE(String value)
        {
            this._value = value;   
        }
           
        @Override
        public String toString() 
        {
            return _value;
        }   
        
        /**
         * Converts a string to a EXCLUDETYPE
         * @param type The type to convert
         * @return The exclude type corresponding to the string or null if unknown
         */
        public static EXCLUDETYPE createsFromString(String type)
        {
            for (EXCLUDETYPE v : EXCLUDETYPE.values())
            {
                if (v.toString().equals(type))
                {
                    return v;
                }
            }
            return null;
        }
    }
    
    /**
     * Target of exclusion in the ribbon configuration
     */
    public enum EXCLUDETARGET
    {
        /** Excluding by plugin */
        PLUGIN("plugin"),
        /** Excluding by extension */
        EXTENSION("extension"),
        /** Excluding by file */
        FILE("file"),
        /** Excluding by label */
        LABEL("label"),
        /** Excluding by id */
        ID("id");
        
        private String _value;
        
        private EXCLUDETARGET(String value)
        {
            this._value = value;   
        }
           
        @Override
        public String toString() 
        {
            return _value;
        }   
        
        /**
         * Converts a string to a EXCLUDETARGET
         * @param target The type to convert
         * @return The exclude type corresponding to the string or null if unknown
         */
        public static EXCLUDETARGET createsFromString(String target)
        {
            for (EXCLUDETARGET v : EXCLUDETARGET.values())
            {
                if (v.toString().equals(target))
                {
                    return v;
                }
            }
            return null;
        }
    }
    
    private EXCLUDETYPE _type;
    private EXCLUDETARGET _target;
    private String _value;
    private Logger _logger;
    
    /**
     * Configure a new exclusion
     * @param configuration The ribbon configuration for the exclude tag
     * @param logger The logger
     * @throws ConfigurationException If an error occurs
     */
    public RibbonExclude(Configuration configuration, Logger logger) throws ConfigurationException
    {
        _logger = logger;
        
        String type = configuration.getName();
        String target = configuration.getAttribute("type", null);
        _value = configuration.getValue();

        _type = EXCLUDETYPE.createsFromString(type);
        
        if (EXCLUDETYPE.CONTROL.equals(_type) && target == null)
        {
            target = "id";
        }
        
        _target = EXCLUDETARGET.createsFromString(target);
        
        if (_type == null || _target == null || _value == null)
        {
            throw new ConfigurationException("Invalid exclude tag '" + type + "' in the ribbon configuration", configuration);
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("RibbonConfigurationManager : Exclusion of " +  type + " with target '" + target + "' and value '" + _value + "'");
        }
    }
    
    /**
     * Get the type of exclusion
     * @return The type
     */
    public EXCLUDETYPE getType()
    {
        return _type;
    }
    
    /**
     * Get the target of exclusion
     * @return The target
     */
    public EXCLUDETARGET getTarget()
    {
        return _target;
    }
    
    /**
     * Get the exclusion value
     * @return The value
     */
    public String getValue()
    {
        return _value;
    }
}
