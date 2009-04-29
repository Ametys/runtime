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
