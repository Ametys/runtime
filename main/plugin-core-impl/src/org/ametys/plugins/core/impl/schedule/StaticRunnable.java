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
package org.ametys.plugins.core.impl.schedule;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.schedule.Runnable;
import org.ametys.core.schedule.Runnable.MisfirePolicy;
import org.ametys.core.schedule.RunnableExtensionPoint;
import org.ametys.core.schedule.Schedulable;
import org.ametys.core.schedule.SchedulableExtensionPoint;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.plugin.component.PluginAware;

/**
 * Static implementation of {@link Runnable} which is configurable
 */
public class StaticRunnable implements Runnable, Component, Configurable, PluginAware, Serviceable
{
    /** The extension point for {@link Schedulable}s */
    protected SchedulableExtensionPoint _schedulableEP;
    /** The name of the plugin that declared this component */
    protected String _pluginName;
    /** The name of the feature that declared this component */
    protected String _featureName;
    /** The id of this extension */
    protected String _id;
    /** The label */
    protected I18nizableText _label;
    /** The description */
    protected I18nizableText _description;
    /** true to run at startup */
    protected boolean _runAtStartup;
    /** The CRON expression for scheduling the job */
    protected String _cronExpression;
    /** The id of the {@link Schedulable} to execute */
    protected String _schedulableId;
    /** Can the runnable be removed */
    protected boolean _removable;
    /** Can the runnable be edited */
    protected boolean _modifiable;
    /** Can the runnable be deactivated */
    protected boolean _deactivatable;
    /** The misfire policy. Default to {@link MisfirePolicy#DO_NOTHING} */
    protected MisfirePolicy _misfirePolicy;
    /** The parameter values */
    protected Map<String, Object> _parameterValues;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _schedulableEP = (SchedulableExtensionPoint) manager.lookup(SchedulableExtensionPoint.ROLE);
    }
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
        _featureName = featureName;
        _id = id;
    }

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _label = I18nizableText.parseI18nizableText(configuration.getChild("label"), "plugin." + _pluginName);
        _description = I18nizableText.parseI18nizableText(configuration.getChild("description"), "plugin." + _pluginName);
        _runAtStartup = configuration.getChild("run-at-startup").getValueAsBoolean(false);
        _cronExpression = configuration.getChild("cron").getValue("0 0 2 * * ? *");
        _schedulableId = configuration.getChild("schedulableId").getValue();
        if (!_schedulableEP.hasExtension(_schedulableId))
        {
            String message = String.format("The extension '%s' of point '%s' declared in the feature '%s' in the plugin '%s' references the Schedulable extension '%s' but it seems to not exist.", 
                    _id, RunnableExtensionPoint.class.getName(), _featureName, _pluginName, _schedulableId);
            throw new ConfigurationException(message, configuration);
        }
        
        _removable = configuration.getChild("removable").getValueAsBoolean(false);
        _modifiable = configuration.getChild("modifiable").getValueAsBoolean(false);
        _deactivatable = configuration.getChild("deactivatable").getValueAsBoolean(false);
        _misfirePolicy = MisfirePolicy.valueOf(configuration.getChild("misfire-policy").getValue("do_nothing").toUpperCase());
        _configureParameterValues(configuration.getChild("parameters"));
    }

    @Override
    public String getId()
    {
        return _id;
    }

    @Override
    public I18nizableText getLabel()
    {
        return _label;
    }

    @Override
    public I18nizableText getDescription()
    {
        return _description;
    }

    @Override
    public boolean runAtStartup()
    {
        return _runAtStartup;
    }
    
    @Override
    public String getCronExpression()
    {
        return _cronExpression;
    }

    @Override
    public String getSchedulableId()
    {
        return _schedulableId;
    }

    @Override
    public boolean isRemovable()
    {
        return _removable;
    }

    @Override
    public boolean isModifiable()
    {
        return _modifiable;
    }

    @Override
    public boolean isDeactivatable()
    {
        return _deactivatable;
    }

    @Override
    public MisfirePolicy getMisfirePolicy()
    {
        return _misfirePolicy;
    }
    
    @Override
    public boolean isVolatile()
    {
        // A configurable runnable is read every time the server restart, so it must be volatile
        return true;
    }
    
    @Override
    public Map<String, Object> getParameterValues()
    {
        return _parameterValues;
    }
    
    private void _configureParameterValues(Configuration paramConfigs) throws ConfigurationException
    {
        _parameterValues = new HashMap<>();
        
        Map<String, Parameter<ParameterType>> declaredParameters = _schedulableEP.getExtension(_schedulableId).getParameters();
        
        for (String paramId : declaredParameters.keySet())
        {
            Configuration paramConf = paramConfigs.getChild(paramId, false);
            if (paramConf == null)
            {
                String message = String.format("The parameter '%s' is missing for the Runnable of id '%s'.", paramId, _id);
                throw new ConfigurationException(message, paramConfigs);
            }
            else
            {
                String valueAsString = paramConf.getValue("");
                
                Parameter<ParameterType> parameter = declaredParameters.get(paramId);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(valueAsString, type);
                _parameterValues.put(paramId, typedValue);
            }
        }
    }
}
