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

package org.ametys.runtime.plugins.admin.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ametys.core.util.IgnoreRootHandler;

/**
 * Generates the runtime file with the list of the modifications
 */
public class ChangeRuntime extends ServiceableGenerator
{
    private SAXParser _saxParser;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "CMS");
        
        Source src = resolver.resolveURI("context://WEB-INF/param/runtime.xml");
         
        try (InputStream is = src.getInputStream())
        {
            _saxParser.parse(new InputSource(is), new IgnoreRootHandler(contentHandler));
        }
        finally
        {
            resolver.release(src);            
        }
        
        Map parentContextParameters = (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        if (parentContextParameters != null)
        {
            XMLUtils.startElement(contentHandler, "transformations");
            
            Map<String, Boolean> extensionPoints = (Map<String, Boolean>) parentContextParameters.get("EP");
            XMLUtils.startElement(contentHandler, "features");
            
            for (String ep : extensionPoints.keySet())
            {
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("name", ep);
                XMLUtils.createElement(contentHandler, "feature", attrs, String.valueOf(extensionPoints.get(ep)));
            }
            
            XMLUtils.endElement(contentHandler, "features");

            Map<String, String> components = (Map<String, String>) parentContextParameters.get("CMP");
            XMLUtils.startElement(contentHandler, "components");
            
            for (String cmp : components.keySet())
            {
                XMLUtils.createElement(contentHandler, cmp, String.valueOf(components.get(cmp)));
            }
            
            XMLUtils.endElement(contentHandler, "components");
            XMLUtils.endElement(contentHandler, "transformations");
        }
        
        XMLUtils.endElement(contentHandler, "CMS");
        contentHandler.endDocument();
    }
}
