package org.ametys.runtime.plugins.core.monitoring.sample;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.ametys.runtime.plugins.core.monitoring.SampleManager;

/**
 * {@link SampleManager} for collecting JVM non heap memory status.
 */
public class NonHeapMemorySampleManager extends AbstractMemorySampleManager
{
    public String getName()
    {
        return "memory-non-heap";
    }
    
    @Override
    protected MemoryUsage _getMemoryUsage(MemoryMXBean memoryMXBean)
    {
        return  memoryMXBean.getNonHeapMemoryUsage();
    }

    @Override
    protected String _getGraphTitle()
    {
        return "JVM Non Heap Memory";
    }
}
