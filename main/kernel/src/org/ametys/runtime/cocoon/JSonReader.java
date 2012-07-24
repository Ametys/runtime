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
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.AbstractReader;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.SAXException;

/**
 * Serialize as json
 */
public class JSonReader extends AbstractReader
{
    /** The map to */
    public static final String MAP_TO_READ = JSonReader.class.getName() + "$result-map";
    
    private static JsonFactory _jsonFactory = new JsonFactory();
    private static ObjectMapper _objectMapper = new ObjectMapper();
    
    @Override
    public String getMimeType()
    {
        return "text/html";
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> mapToRead = (Map<String, Object>) request.getAttribute(MAP_TO_READ);
        
        JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
        _objectMapper.writeValue(jsonGenerator, mapToRead);
        
        IOUtils.closeQuietly(out);
    }
}
