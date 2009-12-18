/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.right.ui.generators;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableGenerator;


/**
 * Generate data for the assignment view screen.
 */
public class ApplicationContextGenerator extends CurrentUserProviderServiceableGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "Application");
        
        XMLUtils.createElement(contentHandler, "AdministratorUI", _isSuperUser() ? "true" : "false");
        
        XMLUtils.endElement(contentHandler, "Application");
        contentHandler.endDocument();
    }
}
