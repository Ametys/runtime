/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.sample;

import java.util.Map;

/**
 * Interface providing access to disk I/O statistics.
 */
public interface DiskIOMonitor
{
    
    /** Read operations count. */
    public static final String READS = "reads";
    /** Bytes read count. */
    public static final String READ_BYTES = "read_bytes";
    /** Write operations count. */
    public static final String WRITES = "writes";
    /** Bytes written count. */
    public static final String WRITE_BYTES = "write_bytes";

    /** Internally refresh the data. */
    void refresh();
    
    /**
     * Get the read operation count.
     * @return the read operation count.
     */
    double getReads();
    
    /**
     * Get the byte read count.
     * @return the byte read count.
     */
    double getReadBytes();
    
    /**
     * Get the write operation count.
     * @return the write operation count.
     */
    double getWrites();
    
    /**
     * Get the byte written count.
     * @return the byte written count.
     */
    double getWriteBytes();
    
    /**
     * Return all the data as a Map.
     * @return the data as a Map.
     */
    Map<String, Double> toMap();
    
}
