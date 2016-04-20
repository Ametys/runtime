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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * JSON helper
 */
public class JSONUtils implements Component, ThreadSafe, Serviceable, Initializable
{
    /** The avalon role */
    public static final String ROLE = JSONUtils.class.getName();
    
    private JsonFactory _jsonFactory = new JsonFactory();
    private ObjectMapper _objectMapper = new ObjectMapper();

    private I18nizableTextSerializer _i18nizableTextSerializer;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _i18nizableTextSerializer = (I18nizableTextSerializer) manager.lookup(I18nizableTextSerializer.ROLE);
    }
    
    @Override
    public void initialize() throws Exception
    {
        // Register new serializer for I18nizableText
        SimpleModule i18nModule = new SimpleModule("AmetysI18nModule", new Version(1, 0, 0, null, null, null));
        i18nModule.addSerializer(_i18nizableTextSerializer);
        _objectMapper.registerModule(i18nModule);
    }
    
    /**
     * Parse a JSON string to a {@link Map} object
     * @param jsonString the string to parse
     * @return object the infos as a Map.
     */
    public Map<String, Object> convertJsonToMap (String jsonString)
    {
        try 
        {
            if (StringUtils.isNotBlank(jsonString)) 
            {
                JsonParser jParser = _jsonFactory.createParser(new StringReader(jsonString));
                Map<String, Object> map = _objectMapper.readValue(jParser, LinkedHashMap.class);
                return map;
            } 
            else 
            {
                return Collections.emptyMap();
            }
        } 
        catch (Exception e) 
        {
            throw new IllegalArgumentException("The json string " + jsonString + " can not be parsed as a Map.", e);
        }
    }
    
    /**
     * Parse a JSON string to a {@link List} object.
     * @param jsonString the string to parse.
     * @return the infos as a List.
     */
    public List<Object> convertJsonToList(String jsonString)
    {
        try 
        {
            if (StringUtils.isNotBlank(jsonString)) 
            {
                JsonParser jParser = _jsonFactory.createParser(new StringReader(jsonString));
                List<Object> list = _objectMapper.readValue(jParser, ArrayList.class);
                return list;
            }
            else
            {
                return Collections.emptyList();
            }
        } 
        catch (Exception e) 
        {
            throw new IllegalArgumentException("The json string " + jsonString + " can not be parsed as a List.", e);
        }
    }
    
    /**
     * Parse a JSON string to an Object array.
     * @param jsonString the JSON string to parse.
     * @return the converted Object array.
     */
    public Object[] convertJsonToArray(String jsonString)
    {
        try 
        {
            if (StringUtils.isNotBlank(jsonString)) 
            {
                JsonParser jParser = _jsonFactory.createParser(new StringReader(jsonString));
                Object[] array = _objectMapper.readValue(jParser, Object[].class);
                return array;
            }
            else
            {
                return new Object[0];
            }
        } 
        catch (Exception e) 
        {
            throw new IllegalArgumentException("The json string " + jsonString + " can not be parsed as an array.", e);
        }
    }
    
    /**
     * Parse a JSON string to a String array.
     * @param jsonString the JSON string to parse.
     * @return the converted String array.
     */
    public String[] convertJsonToStringArray(String jsonString)
    {
        try 
        {
            if (StringUtils.isNotBlank(jsonString)) 
            {
                JsonParser jParser = _jsonFactory.createParser(new StringReader(jsonString));
                String[] array = _objectMapper.readValue(jParser, String[].class);
                return array;
            }
            else
            {
                return new String[0];
            }
        } 
        catch (Exception e) 
        {
            throw new IllegalArgumentException("The json string " + jsonString + " can not be parsed as a String array.", e);
        }
    }
    
    /**
     * Convert an object to JSON string using specified output stream.
     * The out stream is closed after processing.
     * @param out The output stream
     * @param parameters The object to convert
     */
    public void convertObjectToJson (OutputStream out, Object parameters)
    {
        try
        {
            JsonGenerator jsonGenerator = _jsonFactory.createGenerator(out, JsonEncoding.UTF8);
            _objectMapper.writeValue(jsonGenerator, parameters);
            
            IOUtils.closeQuietly(out);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The object can not be converted to json string", e);
        }
    }
    
    /**
     * Convert an object to a JSON string
     * @param parameters The object to convert (List, Map ..)
     * @return The JSON string
     */
    public String convertObjectToJson (Object parameters)
    {
        try
        {
            StringWriter writer = new StringWriter();
            
            JsonGenerator jsonGenerator = _jsonFactory.createGenerator(writer);
            _objectMapper.writeValue(jsonGenerator, parameters);
            
            return writer.toString();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The object can not be converted to json string", e);
        }
    }
}
