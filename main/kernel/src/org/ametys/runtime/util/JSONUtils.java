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

package org.ametys.runtime.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * JSON helper
 */
public class JSONUtils implements Component, ThreadSafe, Serviceable, Initializable
{
    /** The avalon role */
    public static final String ROLE = JSONUtils.class.getName();
    
    private static JsonFactory _jsonFactory = new JsonFactory();
    private static ObjectMapper _objectMapper = new ObjectMapper();

    private ServiceManager _smanager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _smanager = manager;
    }
    
    @Override
    public void initialize() throws Exception
    {
        // Register new serializer for I18nizableText
        SimpleModule i18nModule = new SimpleModule("AmetysI18nModule", new Version(1, 0, 0, null));
        i18nModule.addSerializer((I18nizableTextSerializer) _smanager.lookup(I18nizableTextSerializer.ROLE));
        _objectMapper.registerModule(i18nModule);
    }
    
    /**
     * Parse JSON string to {@link Map} object
     * @param jsonString the string to parse
     * @return object 
     */
    public Map<String, Object> convertJsonToMap (String jsonString)
    {
        try 
        {
            if (StringUtils.isNotBlank(jsonString)) 
            {
                JsonParser jParser = _jsonFactory.createJsonParser(new StringReader(jsonString));
                Map<String, Object> map = _objectMapper.readValue(jParser, LinkedHashMap.class);
                return map;
            } 
            else 
            {
                return Collections.EMPTY_MAP;
            }
        } 
        catch (Exception e) 
        {
            throw new IllegalArgumentException("The json string " + jsonString + " can not be parsed", e);
        }
    }
    
    /**
     * Convert a {@link Map} object to JSON string using specified output stream.
     * The out stream is closed after processing.
     * @param out The {@link Map} object
     * @param parameters The {@link Map} object
     */
    public void convertMapToJson (OutputStream out, Map<String, Object> parameters)
    {
        try
        {
            JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
            _objectMapper.writeValue(jsonGenerator, parameters);
            
            IOUtils.closeQuietly(out);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The object map can not be converted to json string", e);
        }
    }
    
    /**
     * Convert a {@link Map} object to JSON string
     * @param parameters The {@link Map} object
     * @return The JSON string
     */
    public String convertMapToJson (Map<String, Object> parameters)
    {
        try
        {
            StringWriter writer = new StringWriter();
            
            JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(writer);
            _objectMapper.writeValue(jsonGenerator, parameters);
            
            return writer.toString();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The object map can not be converted to json string", e);
        }
    }
}
