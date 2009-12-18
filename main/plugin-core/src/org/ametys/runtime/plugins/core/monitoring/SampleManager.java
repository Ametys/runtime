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

import java.io.IOException;

import org.ametys.runtime.plugins.core.monitoring.MonitoringConstants.Period;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * Interface to be implemented for monitoring samples of data.
 */
public interface SampleManager
{
    /**
     * Provides the human readable name to use.<br>
     * Must be unique in the application.
     * @return the human readable name.
     */
    String getName();
    
    /**
     * Provides the definition to use for this RRD file.
     * Called only when the RRD file is about to be
     * created.
     * @param rrdDef the Round Robin Database definition.
     */
    void configure(RrdDef rrdDef);
    
    /**
     * Collect data into the Round Robin Database.
     * @param sample the sample to collect.
     * @throws IOException thrown in case of I/O error.
     */
    void collect(Sample sample) throws IOException;
    
    /**
     * Provides the graph definition to use for rendering a period.
     * @param rrdFilePath the path to the RRD file.
     * @param width the width of the image.
     * @param height the height of the image.
     * @param period the period.
     * @return the graph definition.
     */
    RrdGraphDef getGraph(String rrdFilePath, int width, int height, Period period);
}
