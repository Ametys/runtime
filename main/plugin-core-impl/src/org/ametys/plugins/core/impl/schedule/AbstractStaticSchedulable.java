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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.quartz.JobExecutionContext;

import org.ametys.core.schedule.Schedulable;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.AbstractParameterParser;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Default static implementation of {@link Schedulable}
 * For implementing the {@link Schedulable} interface (while being {@link Configurable}), extends this class and implements the {@link #execute(org.quartz.JobExecutionContext)} method
 * <br>
 * For instance:
 * <pre>
 * public class SayHelloSchedulable extends AbstractStaticSchedulable
 * {
 *     public static final String FIRSTNAME_KEY = "firstName";
 *     
 *     private static final String __JOBDATAMAP_FIRSTNAME_KEY = Scheduler.PARAM_VALUES_PREFIX + FIRSTNAME_KEY;
 *     
 *     public void execute(JobExecutionContext context) throws Exception
 *     {
 *         JobKey jobKey = context.getJobDetail().getKey();
 *         JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
 *         String name = jobDataMap.getString(__JOBDATAMAP_FIRSTNAME_KEY);
 *         System.out.println("[" + jobKey + "] " + new Date() + " - Hello  " + name + "!");
 *     }
 * }
 * </pre>
 */
public abstract class AbstractStaticSchedulable extends AbstractLogEnabled implements Schedulable, Component, Configurable, PluginAware, Serviceable, Contextualizable
{
    /** The name of the plugin that has declared this component */
    protected String _pluginName;
    /** The id of this extension */
    protected String _id;
    /** The service manager */
    protected ServiceManager _smanager;
    /** The context */
    protected Context _context;
    /** The label */
    protected I18nizableText _label;
    /** The description */
    protected I18nizableText _description;
    /** The icon glyph */
    protected String _iconGlyph;
    /** The small icon */
    protected String _iconSmall;
    /** The medium icon */
    protected String _iconMedium;
    /** The large icon */
    protected String _iconLarge;
    /** True if the schedulable is private */
    protected boolean _private;
    /** The parameters */
    protected Map<String, Parameter<ParameterType>> _parameters;
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
        _id = id;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _smanager = manager;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _label = I18nizableText.parseI18nizableText(configuration.getChild("label"), "plugin." + _pluginName);
        _description = I18nizableText.parseI18nizableText(configuration.getChild("description"), "plugin." + _pluginName);
        _iconGlyph = configuration.getChild("icon-glyph").getValue("");
        _iconSmall = configuration.getChild("icon-small").getValue("");
        _iconMedium = configuration.getChild("icon-medium").getValue("");
        _iconLarge = configuration.getChild("icon-large").getValue("");
        _private = configuration.getChild("private").getValueAsBoolean(false);
        _configureParameters(configuration.getChild("parameters"));
    }
    
    private void _configureParameters(Configuration paramConfigs) throws ConfigurationException
    {
        _parameters = new LinkedHashMap<>();
        
        ThreadSafeComponentManager<Validator> validatorManager = new ThreadSafeComponentManager<>();
        validatorManager.setLogger(getLogger());
        validatorManager.contextualize(_context);
        validatorManager.service(_smanager);
        
        ThreadSafeComponentManager<Enumerator> enumeratorManager = new ThreadSafeComponentManager<>();
        enumeratorManager.setLogger(getLogger());
        enumeratorManager.contextualize(_context);
        enumeratorManager.service(_smanager);
        
        SchedulableParameterParser paramParser = new SchedulableParameterParser(enumeratorManager, validatorManager);
        for (Configuration paramConf : paramConfigs.getChildren("param"))
        {
            Parameter<ParameterType> parameter = paramParser.parseParameter(_smanager, _pluginName, paramConf);
            String id = parameter.getId();
            
            if (_parameters.containsKey(id))
            {
                throw new ConfigurationException("The parameter '" + id + "' is already declared. IDs must be unique.", paramConf);
            }
            
            _parameters.put(id, parameter);
        }
        
        try
        {
            paramParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to lookup parameter local components", paramConfigs, e);
        }
    }
    
    @Override
    public abstract void execute(JobExecutionContext context) throws Exception;
    
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
    public String getIconGlyph()
    {
        return _iconGlyph;
    }

    @Override
    public String getIconSmall()
    {
        return _iconSmall;
    }

    @Override
    public String getIconMedium()
    {
        return _iconMedium;
    }

    @Override
    public String getIconLarge()
    {
        return _iconLarge;
    }
    
    public boolean isPrivate()
    {
        return _private;
    }

    @Override
    public Map<String, Parameter<ParameterType>> getParameters()
    {
        return _parameters;
    }
    
    /**
     * Class for parsing parameters of a {@link Schedulable}
     */
    public class SchedulableParameterParser extends AbstractParameterParser<Parameter<ParameterType>, ParameterType>
    {
        /**
         * Constructor
         * @param enumeratorManager The manager for enumeration
         * @param validatorManager The manager for validation
         */
        public SchedulableParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
        {
            super(enumeratorManager, validatorManager);
        }
        
        @Override
        protected Parameter<ParameterType> _createParameter(Configuration parameterConfig) throws ConfigurationException
        {
            return new Parameter<>();
        }
        
        @Override
        protected String _parseId(Configuration parameterConfig) throws ConfigurationException
        {
            return parameterConfig.getAttribute("id");
        }
        
        @Override
        protected ParameterType _parseType(Configuration parameterConfig) throws ConfigurationException
        {
            try
            {
                return ParameterType.valueOf(parameterConfig.getAttribute("type").toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new ConfigurationException("Invalid parameter type", parameterConfig, e);
            }
        }
        
        @Override
        protected Object _parseDefaultValue(Configuration parameterConfig, Parameter<ParameterType> parameter) throws ConfigurationException
        {
            String defaultValue = parameterConfig.getChild("default-value").getValue(null);
            return ParameterHelper.castValue(defaultValue, parameter.getType());
        }
        
        @Override
        protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration parameterConfig, String parameterId, Parameter<ParameterType> parameter)
                throws ConfigurationException
        {
            super._additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
            parameter.setId(parameterId);
        }
    }
}
