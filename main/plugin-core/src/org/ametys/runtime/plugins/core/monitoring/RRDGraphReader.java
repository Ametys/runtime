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

import java.awt.Graphics;
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
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _monitoringExtensionPoint = (MonitoringExtensionPoint) smanager.lookup(MonitoringExtensionPoint.ROLE);
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
            String sampleName = sampleManager.getId();
            
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
        
        File rrdFile = new File(_rrdStoragePath, sampleManagerToUse.getId() + RRD_EXT);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Using RRD file: " + rrdFile);
        }
        
        RrdGraphDef graphDef = sampleManagerToUse.getGraph(rrdFile.getPath(), 400, 200, period);
        RrdGraph graph = new RrdGraph(graphDef);
        BufferedImage bi = new BufferedImage(graph.getRrdGraphInfo().getWidth(), graph.getRrdGraphInfo().getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bi.getGraphics();
        try
        {
            graph.render(graphics);
            ImageIO.write(bi, "PNG", out);
        }
        finally
        {
            graphics.dispose();
        }
    }
}
