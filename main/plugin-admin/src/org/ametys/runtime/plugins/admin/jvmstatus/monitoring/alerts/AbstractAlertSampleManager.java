/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AlertSampleManager.Threshold.Operator;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.sample.AbstractSampleManager;

/**
 * AbstractAlertSampleManager gives you the infrastructure for easily
 * deploying an {@link AlertSampleManager}.
 * If the configuration mailBody is i18n, it can include two parameters :
 * the first one is the current value, the second one is the threshold value
 */
public abstract class AbstractAlertSampleManager extends AbstractSampleManager implements AlertSampleManager
{
    /** The subject of the mail */
    protected I18nizableText _subject;
    /** The body of the mail */
    protected I18nizableText _body;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        
        boolean isSubjectI18n = configuration.getChild("mailSubject").getAttributeAsBoolean("i18n", false);
        String subject =  configuration.getChild("mailSubject").getValue(null);
        boolean isBodyI18n = configuration.getChild("mailBody").getAttributeAsBoolean("i18n", false);
        String body =  configuration.getChild("mailBody").getValue(null);
        
        if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(body))
        {
            throw new ConfigurationException("Missing <mailSubject> or <mailBody>", configuration);
        }
        
        if (isSubjectI18n)
        {
            _subject = new I18nizableText("plugin." + _pluginName, subject);
        }
        else
        {
            _subject = new I18nizableText(subject);
        }

        if (isBodyI18n)
        {
            _body = new I18nizableText("plugin." + _pluginName, body);
        }
        else
        {
            _body = new I18nizableText(body);
        }
    }
    
    @Override
    public Map<String, Threshold> getThresholdValues()
    {
        if (Config.getInstance() == null)
        {
            return null;
        }
        
        Map<String, Threshold> result = new HashMap<>();
        Map<String, String> configNames = getThresholdConfigNames();
        Map<String, Operator> operators = getOperators();
        for (String datasourceName : configNames.keySet())
        {
            String configName = configNames.get(datasourceName);
            Object value = _getTypedValue(configName);
            result.put(datasourceName, new Threshold(operators.get(datasourceName), datasourceName, value, _subject, _body));
        }
        return result;
    }
    
    private Object _getTypedValue(String configName)
    {
        String stringValue = Config.getInstance().getValueAsString(configName);
        if (stringValue == null || "".equals(stringValue))
        {
            return null;
        }
        
        Long longValue = Config.getInstance().getValueAsLong(configName);
        if (longValue != null)
        {
            return longValue;
        }
        
        Double doubleValue = Config.getInstance().getValueAsDouble(configName);
        return doubleValue;
    }
    
    /**
     * Provides the configuration names for each datasource an alert is attached to.
     * This method must return a map with the same keys as {@link #getOperators()}
     * @return the configuration names for each datasource an alert is attached to.
     */
    protected abstract Map<String, String> getThresholdConfigNames();
    
    /**
     * Provides the kind of operator for triggering the alert for each datasource an alert is attached to.
     * This method must return a map with the same keys as {@link #getThresholdConfigNames()}
     * @return the kind of operator for triggering the alert for each datasource an alert is attached to.
     */
    protected abstract Map<String, Operator> getOperators();
}
