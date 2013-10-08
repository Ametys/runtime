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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.ServiceableReader;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.JSONUtils;

/**
 * Serialize as json
 */
public class JSonReader extends ServiceableReader
{
    /** The map to */
    public static final String OBJECT_TO_READ = JSonReader.class.getName() + "$result-map";
    
    private JSONUtils _jsonUtils;
    
    @Override
    public String getMimeType()
    {
        return "text/html";
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> mapToRead = (Map<String, Object>) request.getAttribute(OBJECT_TO_READ);
     
        _jsonUtils.convertObjectToJson(out, mapToRead);
    }
    
}
