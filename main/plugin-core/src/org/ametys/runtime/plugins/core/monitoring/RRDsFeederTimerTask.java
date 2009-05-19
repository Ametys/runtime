package org.ametys.runtime.plugins.core.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.rrd4j.core.RrdDb;

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
public class RRDsFeederTimerTask extends TimerTask implements Component, LogEnabled, Contextualizable, Serviceable, Initializable, Disposable, MonitoringConstants
{
    private Logger _logger;
    private MonitoringExtensionPoint _monitoringExtensionPoint;
    private Timer _timer;
    private String _rrdStoragePath;
    
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }

    public void contextualize(Context context) throws ContextException
    {
        org.apache.cocoon.environment.Context cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _rrdStoragePath = cocoonContext.getRealPath(RRD_STORAGE_PATH);
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _monitoringExtensionPoint = (MonitoringExtensionPoint) manager.lookup(MonitoringExtensionPoint.ROLE);
    }
    
    public void initialize() throws Exception
    {
        _logger.debug("Starting timer");
        // Daemon thread
        _timer = new Timer(false);
        // Start in 30s and refresh each minutes
        _timer.scheduleAtFixedRate(this, 30000, FEEDING_PERIOD * 1000);
    }

    @Override
    public void run()
    {
        _logger.info("Time to collect data");
        
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);
            
            if (sampleManager != null)
            {
                String sampleName = sampleManager.getName();
                File rrdFile = new File(_rrdStoragePath, sampleName + RRD_EXT);
                
                if (_logger.isInfoEnabled())
                {
                    _logger.info("Collecting sample for: " + sampleName);
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
