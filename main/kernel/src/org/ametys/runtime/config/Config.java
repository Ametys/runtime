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
package org.ametys.runtime.config;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.framework.logger.Logger;

import org.ametys.core.util.LoggerFactory;
import org.ametys.core.util.MapHandler;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;


/**
 * Config bean <br>
 * Reads/Write config file. <br>
 */
public final class Config
{
    // Logger for traces
    private static Logger _logger = LoggerFactory.getLoggerFor(Config.class);
    
    // config file
    private static String __filename;
    
    // shared instance for optimization
    private static Config __config;
    
    // Initialization status
    private static boolean _initialized;
    
    // Typed value (filled after read)
    private Map<String, String> _values;

    private Config() throws Exception
    {
        if (_logger.isInfoEnabled())
        {
            _logger.info("Loading configuration values from file");
        }
        
        _values = read();
    }

    /**
     * Get the instance of Config using the config file.<br>
     * @return An instance of Config containing config file value, or null if config file cannot be read.
     */
    public static Config getInstance()
    {
        if (!_initialized)
        {
            return null;
        }
        
        if (__config == null)
        {
            try
            {
                __config = new Config();
            }
            catch (Exception e)
            {
                if (_logger.isWarnEnabled())
                {
                    _logger.warn("Exception creating Config, it won't be accessible.", e);
                }
                
                return null;
            }
        }
        
        return __config;
    }
    
    /**
     * Dispose this Config instance
     */
    public static void dispose()
    {
        __config = null;
    }

    /**
     * Set the config filename
     * @param filename Name with path of the config file
     */
    public static void setFilename(String filename)
    {
        __filename = filename;
    }
    
    /**
     * Set the initialization status of the configuration
     * @param initialized the initialization status of the configuration
     */
    public static void setInitialized(boolean initialized)
    {
        _initialized = initialized;
    }

    /**
     * Return the typed value as String
     * @param id Id of the parameter to get
     * @return the typed value as String
     */
    public String getValueAsString(String id)
    {
        return _values.get(id);
    }

    /**
     * Return the typed value as Date
     * @param id Id of the parameter to get
     * @return the typed value as Date
     */
    public Date getValueAsDate(String id)
    {
        String value = _values.get(id);
        
        return (Date) ParameterHelper.castValue(value, ParameterType.DATE);
    }

    /**
     * Return the typed value as long
     * @param id Id of the parameter to get
     * @return the typed value as long
     */
    public Long getValueAsLong(String id)
    {
        String value = _values.get(id);
        
        return (Long) ParameterHelper.castValue(value, ParameterType.LONG);
    }

    /**
     * Return the typed value as boolean
     * @param id Id of the parameter to get
     * @return the typed value as boolean
     */
    public Boolean getValueAsBoolean(String id)
    {
        String value = _values.get(id);
        
        return (Boolean) ParameterHelper.castValue(value, ParameterType.BOOLEAN);
    }

    /**
     * Return the typed value casted in double
     * @param id Id of the parameter to get
     * @return the typed value casted in double
     */
    public Double getValueAsDouble(String id)
    {
        String value = _values.get(id);
        
        return (Double) ParameterHelper.castValue(value, ParameterType.DOUBLE);
    }

    /**
     * Read config file and get untyped values (String object)
     * @return Map (key, untyped value) representing the config file
     * @throws Exception if a problem occurs reading values
     */
    public static Map<String, String> read() throws Exception
    {
        Map<String, String> configValues = new HashMap<>();

        // Lit le fichier de déploiement pour déterminer les valeurs non typées
        File configFile = new File(__filename);
        
        if (configFile.exists())
        {
            SAXParserFactory.newInstance().newSAXParser().parse(configFile, new MapHandler(configValues));
        }

        return configValues;
    }
}
