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
