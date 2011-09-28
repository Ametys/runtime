/*
 *  Copyright 2011 Anyware Services
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

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * JSON helper
 *
 */
public final class JSONUtils
{
    private static JsonFactory _jsonFactory = new JsonFactory();
    private static ObjectMapper _objectMapper = new ObjectMapper();
    
    private JSONUtils ()
    {
        // empty
    }
    
    /**
     * Parse JSON string to {@link Map} object
     * @param jsonString the string to parse
     * @return object 
     */
    public static Map<String, Object> parse (String jsonString)
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
}
