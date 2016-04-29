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

import java.io.IOException;
import java.util.Map;

import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * Interface to be implemented for monitoring samples of data.
 */
public interface SampleManager
{
    /**
     * Provides the id of this manager.<br>
     * Must be unique in the application.
     * @return the id.
     */
    String getId();
    
    /**
     * Provides the human readable name to use.<br>
     * @return the human readable name.
     */
    I18nizableText getLabel();
    
    /**
     * Provides the human readable description.<br>
     * @return the human readable description.
     */
    I18nizableText getDescription();
    
    /**
     * Provides the definition to use for this RRD file.
     * Called only when the RRD file is about to be
     * created.
     * @param rrdDef the Round Robin Database definition.
     */
    void configureRRDDef(RrdDef rrdDef);
    
    /**
     * Collect data into the Round Robin Database.
     * @param sample the sample to collect.
     * @return The collected values for each datasource name.
     * @throws IOException thrown in case of I/O error.
     */
    Map<String, Object> collect(Sample sample) throws IOException;
    
    /**
     * Provides the graph definition to use for rendering a period.
     * @param rrdFilePath the path to the RRD file.
     * @param width the width of the image.
     * @param height the height of the image.
     * @param period the period.
     * @return the graph definition.
     */
    RrdGraphDef getGraph(String rrdFilePath, int width, int height, org.ametys.runtime.plugins.admin.jvmstatus.monitoring.MonitoringConstants.Period period);
}
