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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.io.FileUtils;
import org.rrd4j.core.RrdDb;

import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * {@link TimerTask} for creating and feeding RRDs files in order to
 * produce graphs for monitoring:
 * <ul>
 *  <li>JVM uptime
 *  <li>JVM memory status
 *  <li>JVM thread count
 *  <li>Servlet Engine request count
 *  <li>Servlet Engine session count
 * </ul>
 */
public class RRDsFeederTimerTask extends TimerTask implements Component, LogEnabled, Serviceable, Initializable, Disposable, MonitoringConstants
{
    private Logger _logger;
    private MonitoringExtensionPoint _monitoringExtensionPoint;
    private Timer _timer;
    private String _rrdStoragePath;
    
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _monitoringExtensionPoint = (MonitoringExtensionPoint) manager.lookup(MonitoringExtensionPoint.ROLE);
    }
    
    public void initialize() throws Exception
    {
        _rrdStoragePath = FileUtils.getFile(RuntimeConfig.getInstance().getAmetysHome(), RRD_STORAGE_DIRECTORY).getPath();
        
        _logger.debug("Starting timer");
        // Daemon thread
        _timer = new Timer("RRDFeeder", true);
        // Start in 30s and refresh each minutes
        _timer.scheduleAtFixedRate(this, 30000, FEEDING_PERIOD * 1000);
    }

    @Override
    public void run()
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Time to collect data");
        }
        
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);
            
            if (sampleManager != null)
            {
                String sampleName = sampleManager.getId();
                File rrdFile = new File(_rrdStoragePath, sampleName + RRD_EXT);
                
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("Collecting sample for: " + sampleName);
                }
                
                RrdDb rrdDb = null;
                
                try
                {
                    rrdDb = new RrdDb(rrdFile.getPath());
                    sampleManager.collect(rrdDb.createSample());
                }
                catch (Exception e)
                {
                    _logger.error("Unable to collect sample for: " + sampleName, e);
                }
                finally
                {
                    if (rrdDb != null)
                    {
                        try
                        {
                            rrdDb.close();
                        }
                        catch (IOException e)
                        {
                            _logger.warn("Unable to close RRD file: " + rrdFile, e);
                        }
                    }
                }
            }
        }
    }
    
    public void dispose()
    {
        _logger = null;
        _monitoringExtensionPoint = null;
        _rrdStoragePath = null;
        cancel();
        _timer.cancel();
    }
}
