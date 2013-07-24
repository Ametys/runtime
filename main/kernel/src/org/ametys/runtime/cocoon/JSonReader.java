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
package org.ametys.runtime.cocoon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nUtils;
import org.ametys.runtime.util.I18nizableText;

/**
 * Serialize as json
 */
public class JSonReader extends ServiceableReader
{
    /** The map to */
    public static final String MAP_TO_READ = JSonReader.class.getName() + "$result-map";
    
    private static JsonFactory _jsonFactory = new JsonFactory();
    private static ObjectMapper _objectMapper = new ObjectMapper();

    private I18nUtils _i18nUtils;
    
    @Override
    public String getMimeType()
    {
        return "text/html";
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _i18nUtils = (I18nUtils) smanager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> mapToRead = (Map<String, Object>) request.getAttribute(MAP_TO_READ);
     
        /** 
         * Solution 2 = Utiliser AmetysSerializerProvider au lieu de _convertMap ??
        if (_objectMapper == null)
        {
            _objectMapper = new ObjectMapper (null, new AmetysSerializerProvider(_i18nUtils), null);
        }*/
        
        JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
        _objectMapper.writeValue(jsonGenerator, _convertMap(mapToRead));
        
        IOUtils.closeQuietly(out);
    }
    
    private Map _convertMap (Map mapToRead)
    {
        Map<Object, Object> convertedMap = new HashMap<Object, Object>();
        
        for (Object key : mapToRead.keySet())
        {
            Object value = mapToRead.get(key);
            convertedMap.put(key, _convertValue(value));
        }
        
        return convertedMap;
    }
    
    private Collection<Object> _convertCollection (Collection colToRead)
    {
        List<Object> convertedList = new ArrayList<Object>();
        
        for (Object value : colToRead)
        {
            convertedList.add(_convertValue(value));
        }
        
        return convertedList;
    }
    
    private Object _convertValue (Object value)
    {
        if (value instanceof I18nizableText)
        {
            return _i18nUtils.translate((I18nizableText) value);
        }
        else if (value instanceof Collection)
        {
            return _convertCollection ((Collection) value);
        } 
        else if (value instanceof Map)
        {
            return _convertMap ((Map) value);
        }
        else
        {
            return value;
        }
    }
}
