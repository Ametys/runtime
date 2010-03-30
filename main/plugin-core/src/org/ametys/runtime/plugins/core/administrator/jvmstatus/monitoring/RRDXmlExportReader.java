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
package org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring;

import java.io.File;
import java.io.IOException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.reading.AbstractReader;
import org.apache.cocoon.reading.Reader;
import org.rrd4j.core.RrdDb;
import org.xml.sax.SAXException;

/**
 * {@link Reader} for exporting sample datas.
 */
public class RRDXmlExportReader extends AbstractReader implements Contextualizable, MonitoringConstants
{
    private String _rrdStoragePath;

    public void contextualize(Context context) throws ContextException
    {
        org.apache.cocoon.environment.Context cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _rrdStoragePath = cocoonContext.getRealPath(RRD_STORAGE_PATH);
    }

    @Override
    public String getMimeType()
    {
        return "text/xml";
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        File rrdFile = new File(_rrdStoragePath, source + RRD_EXT);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Using RRD file: " + rrdFile);
        }
        
        if (!rrdFile.exists())
        {
            throw new ProcessingException("None sample manager exists for: " + source);
        }
        
        RrdDb rrdDb = new RrdDb(rrdFile.getPath());
        
        try
        {
            rrdDb.exportXml(out);
        }
        finally
        {
            rrdDb.close();
        }
    }
}
