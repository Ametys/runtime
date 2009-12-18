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
package org.ametys.runtime.plugins.core.monitoring;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * {@link Generator} for SAXing {@link SampleManager}s.
 */
public class SampleManagersGenerator extends ServiceableGenerator implements MonitoringConstants
{
    private MonitoringExtensionPoint _monitoringExtensionPoint;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _monitoringExtensionPoint = (MonitoringExtensionPoint) smanager.lookup(MonitoringExtensionPoint.ROLE);
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "samples");

        XMLUtils.startElement(contentHandler, "periods");
        for (Period period : Period.values())
        {
            XMLUtils.createElement(contentHandler, "period", period.toString());
        }
        XMLUtils.endElement(contentHandler, "periods");
        
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);

            XMLUtils.startElement(contentHandler, "sample");
            XMLUtils.createElement(contentHandler, "name", sampleManager.getName());
            XMLUtils.endElement(contentHandler, "sample");
        }
        
        XMLUtils.endElement(contentHandler, "samples");
        
        contentHandler.endDocument();
    }
}
