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
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.generation.AbstractGenerator;
import org.xml.sax.SAXException;

/**
 * SAX an i18nizableText placed in the parent context attributes (with the key in source)
 */
public class I18nGenerator extends AbstractGenerator
{
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Map parentContextAttr = (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        I18nizableText i18nizableText = (I18nizableText) parentContextAttr.get("i18n");
        
        contentHandler.startDocument();
        i18nizableText.toSAX(contentHandler, "i18n");
        contentHandler.endDocument();
    }

}
