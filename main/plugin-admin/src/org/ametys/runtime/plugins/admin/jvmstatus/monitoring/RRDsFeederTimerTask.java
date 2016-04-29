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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.MessagingException;

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

import org.ametys.core.util.I18nUtils;
import org.ametys.core.util.mail.SendMailHelper;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AlertSampleManager;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AlertSampleManager.Threshold;
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
    private static final String __CONFIG_ALERTS_ENABLED = "runtime.system.alerts.enable";
    private static final String __CONFIG_FROM_MAIL = "smtp.mail.from";
    private static final String __CONFIG_ADMIN_MAIL = "smtp.mail.sysadminto";
    
    private Logger _logger;
    private MonitoringExtensionPoint _monitoringExtensionPoint;
    private Timer _timer;
    private String _rrdStoragePath;
    private I18nUtils _i18nUtils;
    
    /** Tells if there is a current alert, i.e. if we already sent an alert email
     * This is  a map {sampleManagerId -> datasourceName -> wasAlertedLastTime} */
    private Map<String, Map<String, Boolean>> _currentAlerts;
    
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _monitoringExtensionPoint = (MonitoringExtensionPoint) manager.lookup(MonitoringExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    public void initialize() throws Exception
    {
        _rrdStoragePath = FileUtils.getFile(RuntimeConfig.getInstance().getAmetysHome(), RRD_STORAGE_DIRECTORY).getPath();
        
        _logger.debug("Starting timer");
        // Daemon thread
        _timer = new Timer("RRDFeeder", true);
        // Start in 30s and refresh each minutes
        _timer.scheduleAtFixedRate(this, 30000, FEEDING_PERIOD * 1000);
        
        _currentAlerts = new LinkedHashMap<>();
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
                    Map<String, Object> collectedValues = sampleManager.collect(rrdDb.createSample());
                    if (sampleManager instanceof AlertSampleManager)
                    {
                        _checkIfAlert((AlertSampleManager) sampleManager, collectedValues);
                    }
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
    
    private void _checkIfAlert(AlertSampleManager sampleManager, Map<String, Object> collectedValues)
    {
        if (Config.getInstance() == null)
        {
            return;
        }
        
        if (Config.getInstance().getValueAsBoolean(__CONFIG_ALERTS_ENABLED))
        {
            if (_currentAlerts.get(sampleManager.getId()) == null)
            {
                _currentAlerts.put(sampleManager.getId(), new HashMap<>());
            }
            
            Map<String, Threshold> thresholds = sampleManager.getThresholdValues();
            for (String datasourceName : thresholds.keySet())
            {
                if (_currentAlerts.get(sampleManager.getId()).get(datasourceName) == null)
                {
                    _currentAlerts.get(sampleManager.getId()).put(datasourceName, false);
                }
                
                Threshold threshold = thresholds.get(datasourceName);
                if (threshold.isExceeded(collectedValues.get(datasourceName)) &&  !_currentAlerts.get(sampleManager.getId()).get(datasourceName))
                {
                    // Send the mail
                    _sendAlertMail(threshold.getMailSubject(), threshold.getMailBody(), collectedValues.get(datasourceName).toString(), threshold.getValue().toString());
                    // Memorize to not send a mail again
                    _currentAlerts.get(sampleManager.getId()).put(datasourceName, true);
                }
                else if (!threshold.isExceeded(collectedValues.get(datasourceName)) &&  _currentAlerts.get(sampleManager.getId()).get(datasourceName))
                {
                    // Next check, we would possibly send a mail
                    _currentAlerts.get(sampleManager.getId()).put(datasourceName, false);
                }
            }
        }
    }
    
    private void _sendAlertMail(I18nizableText subject, I18nizableText body, String currentValue, String thresholdValue)
    {
        String toMail = Config.getInstance().getValueAsString(__CONFIG_ADMIN_MAIL);
        String fromMail = Config.getInstance().getValueAsString(__CONFIG_FROM_MAIL);
        try
        {
            String subjectStr = _i18nUtils.translate(subject, "fr"); //FIXME fr hardcoded
            
            List<String> bodyParams = new ArrayList<>();
            bodyParams.add(currentValue);
            bodyParams.add(thresholdValue);
            I18nizableText bodyWithParams = body.isI18n() ? new I18nizableText(body.getCatalogue(), body.getKey(), bodyParams) : body;
            String bodyStr = _i18nUtils.translate(bodyWithParams, "fr");
            
            SendMailHelper.sendMail(subjectStr, null, bodyStr, toMail, fromMail);
        }
        catch (MessagingException e)
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("Could not send an alert e-mail to " + toMail, e);
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
