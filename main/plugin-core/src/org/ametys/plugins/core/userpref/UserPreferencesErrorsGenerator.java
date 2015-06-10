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
package org.ametys.plugins.core.userpref;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.core.userpref.UserPreferencesErrors;
import org.ametys.core.util.I18nizableText;
import org.ametys.runtime.parameter.Errors;

/**
 * Generates a list of errors.
 */
public class UserPreferencesErrorsGenerator extends AbstractGenerator
{

    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        UserPreferencesErrors errors = (UserPreferencesErrors) request.getAttribute("user-prefs-errors");
        
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "errors");
        
        if (errors != null)
        {
            for (Entry<String, Errors> fieldErrors : errors.getErrors().entrySet())
            {
                String fieldId = fieldErrors.getKey();
                Errors errorLabels = fieldErrors.getValue();
                
                AttributesImpl atts = new AttributesImpl();
                atts.addCDATAAttribute("id", fieldId);
                XMLUtils.startElement(contentHandler, "field", atts);
                
                for (I18nizableText label : errorLabels.getErrors())
                {
                    label.toSAX(contentHandler, "error");
                }
                
                XMLUtils.endElement(contentHandler, "field");
            }
        }
        
        XMLUtils.endElement(contentHandler, "errors");
        
        contentHandler.endDocument();
    }

}
