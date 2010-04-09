/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.util.cocoon;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.Errors;

/**
 * Generator for SAXing a map.
 */
public class ActionMapResultGenerator extends AbstractGenerator
{
    /** Request attribute name containing the map to use. */
    public static final String MAP_REQUEST_ATTR = ActionMapResultGenerator.class.getName() + ";map";
    
    @SuppressWarnings("unchecked")
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> map = (Map<String, Object>) request.getAttribute(MAP_REQUEST_ATTR);
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "ActionResult");

        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Errors)
            {
                XMLUtils.startElement(contentHandler, key);
                
                for (I18nizableText errorLabel : ((Errors) value).getErrors())
                {
                    errorLabel.toSAX(contentHandler, "error");
                }
                
                XMLUtils.endElement(contentHandler, key);
            }
            else if (value instanceof Collection)
            {
                for (Object item : (Collection) value)
                {
                    if (item != null)
                    {
                        XMLUtils.createElement(contentHandler, key, item.toString());
                    }
                }
            }
            else
            {
                String stringValue = "";
                
                if (entry.getValue() != null)
                {
                    stringValue = entry.getValue().toString();
                }
                
                XMLUtils.createElement(contentHandler, key, stringValue);
            }
        }
        
        XMLUtils.endElement(contentHandler, "ActionResult");
        contentHandler.endDocument();
    }
}
