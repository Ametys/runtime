package org.ametys.runtime.plugins.core.monitoring.sample;

import java.io.IOException;
import java.util.Date;

import org.ametys.runtime.plugins.core.monitoring.MonitoringConstants;
import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraphDef;

/**
 * AbstractSampleManager gives you the infrastructure for easily
 * deploying a {@link SampleManager}.
 */
public abstract class AbstractSampleManager implements SampleManager, MonitoringConstants, ThreadSafe, LogEnabled
{
    /** Logger available to subclasses. */
    protected Logger _logger;
    
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }
    
    public void configure(RrdDef rrdDef)
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Configuring RRD definition for sample manager: " + getName());
        }
        
        rrdDef.setStartTime(new Date());
        
        _configureDatasources(rrdDef);
        
        // Keep 60 slots of data for storing data of the last hour
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 1, 60);
        // Keep an average of 30 minutes data in 72 slots for storing data
        // of the last 24 hours
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 30, 72);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 20, 72);
        // Keep an average of 2 hours data in 84 slots for storing data
        // of the last 7 days
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 120, 84);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 240, 84);
        // Keep an average of 12 hours data in 62 slots for storing data
        // of the last 31 days
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 720, 62);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 720, 62);
        // Keep an average of 6 days data in 61 slots for storing data
        // of the last 12 months
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 8640, 61);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 8640, 61);
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

    public void collect(Sample sample) throws IOException
    {
        sample.setTime(Util.getTime());
        
        _internalCollect(sample);
        
        if (_logger.isInfoEnabled())
        {
            _logger.info("Data collected: " + sample.dump());
        }
        
        sample.update();
    }
    
    /**
     * Collect data into the Round Robin Database.
     * @param sample the sample to collect.
     * @throws IOException thrown in case of I/O error.
     */
    protected abstract void _internalCollect(Sample sample) throws IOException;

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

        _populateGraphDefinition(graphDef, rrdFilePath);
        
        return graphDef;
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
