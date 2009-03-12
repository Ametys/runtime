package org.ametys.runtime.plugins.core.monitoring;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.reading.ServiceableReader;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.xml.sax.SAXException;

/**
 * {@link Reader} for exposing a monitoring graph.
 */
public class RRDGraphReader extends ServiceableReader implements Contextualizable, MonitoringConstants
{
    private String _rrdStoragePath;
    private MonitoringExtensionPoint _monitoringExtensionPoint;

    public void contextualize(Context context) throws ContextException
    {
        org.apache.cocoon.environment.Context cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _rrdStoragePath = cocoonContext.getRealPath(RRD_STORAGE_PATH);
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);
        _monitoringExtensionPoint = (MonitoringExtensionPoint) manager.lookup(MonitoringExtensionPoint.ROLE);
    }

    @Override
    public String getMimeType()
    {
        return "image/png";
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        String periodValue = parameters.getParameter("period", null);
        Period period = Period.LAST_HOUR;
        
        if (periodValue != null)
        {
            if ("day".equals(periodValue))
            {
                period = Period.LAST_DAY;
            }
            else if ("week".equals(periodValue))
            {
                period = Period.LAST_WEEK;
            }
            else if ("month".equals(periodValue))
            {
                period = Period.LAST_MONTH;
            }
            else if ("year".equals(periodValue))
            {
                period = Period.LAST_YEAR;
            }
        }
        
        SampleManager sampleManagerToUse = null;
        
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);
            String sampleName = sampleManager.getName();
            
            if (sampleName.equals(source))
            {
                sampleManagerToUse = sampleManager;
                break;
            }
        }
        
        if (sampleManagerToUse == null)
        {
            throw new ProcessingException("");
        }
        
        File rrdFile = new File(_rrdStoragePath, sampleManagerToUse.getName() + RRD_EXT);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Using RRD file: " + rrdFile);
        }
        
        RrdGraphDef graphDef = sampleManagerToUse.getGraph(rrdFile.getPath(), 400, 200, period);
        RrdGraph graph = new RrdGraph(graphDef);
        BufferedImage bi = new BufferedImage(graph.getRrdGraphInfo().getWidth(), graph.getRrdGraphInfo().getHeight(), BufferedImage.TYPE_INT_RGB);
        graph.render(bi.getGraphics());
        ImageIO.write(bi, "PNG", out);
    }
}
