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

import java.io.File;
import java.io.IOException;

import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;
import org.apache.cocoon.Constants;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;

/**
 * {@link ExtensionPoint} for collecting sample of data in order to be
 * monitored.
 */
public class MonitoringExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<SampleManager> implements MonitoringConstants
{
    /** Avalon role.*/
    public static final String ROLE = MonitoringExtensionPoint.class.getName();
    
    @Override
    public void initializeExtensions() throws Exception
    {
        super.initializeExtensions();
        
        org.apache.cocoon.environment.Context cocoonContext = (org.apache.cocoon.environment.Context) _context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        String rrdStoragePath = cocoonContext.getRealPath(RRD_STORAGE_PATH);
        File rrdStorageDir = new File(rrdStoragePath);

        if (!rrdStorageDir.exists())
        {
            if (!rrdStorageDir.mkdirs())
            {
                throw new Exception("Unable to create monitoring directory: " + rrdStorageDir);
            }
        }

        for (String extensionId : getExtensionsIds())
        {
            SampleManager sampleManager = getExtension(extensionId);
            
            String sampleName = sampleManager.getName();
            File rrdFile = new File(rrdStorageDir, sampleName + RRD_EXT);
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Creating RRD file: " + rrdFile);
            }
            
            if (!rrdFile.exists())
            {
                RrdDef rrdDef = new RrdDef(rrdFile.getPath(), FEEDING_PERIOD);
                
                sampleManager.configure(rrdDef);
                
                try
                {
                    RrdDb rrdDb = new RrdDb(rrdDef);
                    rrdDb.close();
                }
                catch (IOException e)
                {
                    throw new Exception("Unable to create RRD file: " + rrdFile);
                }
            }
        }
    }
}
