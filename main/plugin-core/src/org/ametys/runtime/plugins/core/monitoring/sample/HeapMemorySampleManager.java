package org.ametys.runtime.plugins.core.monitoring.sample;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.ametys.runtime.plugins.core.monitoring.SampleManager;

/**
 * {@link SampleManager} for collecting JVM heap memory status.
 */
public class HeapMemorySampleManager extends AbstractMemorySampleManager
{
    public String getName()
    {
        return "memory-heap";
    }
    
    @Override
    protected MemoryUsage _getMemoryUsage(MemoryMXBean memoryMXBean)
    {
        return  memoryMXBean.getHeapMemoryUsage();
    }

    @Override
    protected String _getGraphTitle()
    {
        return "JVM Heap Memory";
    }
}
