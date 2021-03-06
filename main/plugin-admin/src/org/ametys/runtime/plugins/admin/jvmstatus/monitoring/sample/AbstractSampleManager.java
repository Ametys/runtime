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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraphConstants;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.MonitoringConstants;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.SampleManager;

/**
 * AbstractSampleManager gives you the infrastructure for easily
 * deploying a {@link SampleManager}.
 */
public abstract class AbstractSampleManager implements SampleManager, MonitoringConstants, ThreadSafe, LogEnabled, Configurable, PluginAware
{
    /** Logger available to subclasses. */
    protected Logger _logger;
    
    /** The name of the plugin that has declared this component */
    protected String _pluginName;
    /** The name of the feature that has declared this component */
    protected String _featureName;
    /** id */
    protected String _id;
    /** label */
    protected I18nizableText _label;
    /** description */
    protected I18nizableText _description;
    
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
 
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        boolean isLabelI18n = configuration.getChild("label").getAttributeAsBoolean("i18n", false);
        String label =  configuration.getChild("label").getValue(null);
        boolean isDescriptionI18n = configuration.getChild("description").getAttributeAsBoolean("i18n", false);
        String description =  configuration.getChild("description").getValue(null);
        
        if (StringUtils.isEmpty(label) || StringUtils.isEmpty(description))
        {
            throw new ConfigurationException("Missing <label> or <description>", configuration);
        }
        
        if (isLabelI18n)
        {
            _label = new I18nizableText("plugin." + _pluginName, label);
        }
        else
        {
            _label = new I18nizableText(label);
        }

        if (isDescriptionI18n)
        {
            _description = new I18nizableText("plugin." + _pluginName, description);
        }
        else
        {
            _description = new I18nizableText(description);
        }
    }
    
    /* (non-Javadoc)
     * @see org.ametys.runtime.plugin.component.PluginAware#setPluginInfo(java.lang.String, java.lang.String)
     */
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
        _featureName = featureName;
        _id = id;
    }
    
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }
    
    public void configureRRDDef(RrdDef rrdDef)
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Configuring RRD definition for sample manager: " + getId());
        }
        
        rrdDef.setStartTime(new Date());
        
        _configureDatasources(rrdDef);
        
        // Every minute for 24 hours
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60 * 24);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 1, 60 * 24);
        // Every 10 minutes for 7 days
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 6 * 24 * 7);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 10, 6 * 24 * 7);
        // Every 2 hours for a month
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 2 * 60, 12 * 30);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 2 * 60, 12 * 30);
        // Every 6 hours for 3 months
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 6 * 60, 4 * 30 * 3);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 6 * 60, 4 * 30 * 3);
        // Every 24 hours for a year
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 24 * 60, 365);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 24 * 60, 365);
        // Every week for 5 years
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 24 * 60 * 7, 52 * 5);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 24 * 60 * 7, 52 * 5);
    }

    /**
     * Provides the data sources.
     * @param rrdDef the Round Robin Database definition.
     */
    protected abstract void _configureDatasources(RrdDef rrdDef);
    
    /**
     * Register a new data source where heartbeat property is set internally.
     * @param rrdDef the Round Robin Database definition.
     * @param dsName the data source name.
     * @param dsType the data source type.
     * @param minValue the minimal acceptable value. Use <code>Double.NaN</code> if unknown.
     * @param maxValue the maximal acceptable value. Use <code>Double.NaN</code> if unknown.
     */
    protected void _registerDatasources(RrdDef rrdDef, String dsName, DsType dsType, double minValue, double maxValue)
    {
        rrdDef.addDatasource(dsName, dsType, FEEDING_PERIOD * 2, minValue, maxValue);
    }

    public Map<String, Object> collect(Sample sample) throws IOException
    {
        sample.setTime(Util.getTime());
        
        Map<String, Object> collectedValues = _internalCollect(sample);
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Data collected: " + sample.dump());
        }
        
        sample.update();
        return collectedValues;
    }
    
    /**
     * Collect data into the Round Robin Database.
     * @param sample the sample to collect.
     * @return The collected values for each datasource name.
     * @throws IOException thrown in case of I/O error.
     */
    protected abstract Map<String, Object> _internalCollect(Sample sample) throws IOException;

    public RrdGraphDef getGraph(String rrdFilePath, int width, int height, Period period)
    {
        RrdGraphDef graphDef = new RrdGraphDef();
        
        // Use collected date
        graphDef.setTimeSpan(-period.getTime() - 2 * FEEDING_PERIOD, -2 * FEEDING_PERIOD);
        
        _configureValueRange(graphDef);
        
        // Set image size
        graphDef.setWidth(width);
        graphDef.setHeight(height);
        
        // Hide rrd4j signature
        graphDef.setShowSignature(false);
        graphDef.setTitle(String.format("%s of the last %s", _getGraphTitle(), period.toString()));
        
        // Set common graph parameters.
        _setCommonParameters(graphDef);
        
        _populateGraphDefinition(graphDef, rrdFilePath);
        
        return graphDef;
    }
    
    /**
     * Set common graph definition parameters (graph style, ...)
     * @param graphDef the prepared graph definition.
     */
    protected void _setCommonParameters(RrdGraphDef graphDef)
    {
        // Common style.
        graphDef.setColor(RrdGraphConstants.COLOR_BACK, new Color(255, 255, 255));
        graphDef.setColor(RrdGraphConstants.COLOR_CANVAS, new Color(255, 255, 255));
        graphDef.setColor(RrdGraphConstants.COLOR_FRAME, new Color(255, 255, 255));
        graphDef.setColor(RrdGraphConstants.COLOR_MGRID, new Color(128, 128, 128));
        graphDef.setColor(RrdGraphConstants.COLOR_GRID, new Color(220, 220, 220));
        graphDef.setColor(RrdGraphConstants.COLOR_SHADEA, new Color(220, 220, 220));
        graphDef.setColor(RrdGraphConstants.COLOR_SHADEB, new Color(220, 220, 220));
    }
    
    /**
     * Configure the value range to be displayed.<br>
     * Default implementation set min value to <code>0</code>.
     * @param graphDef the graph definition.
     */
    protected void _configureValueRange(RrdGraphDef graphDef)
    {
        graphDef.setMinValue(0d);
    }

    /**
     * Provide the graph title.
     * @return the graph title.
     */
    protected abstract String _getGraphTitle();

    /**
     * Populate the graph definition to render.
     * @param graphDef the prepared graph definition.
     * @param rrdFilePath the path to the RRD file.
     */
    protected abstract void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath);
}
