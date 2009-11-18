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
        return "text/plain";
    }
    
    @SuppressWarnings("unchecked")
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
