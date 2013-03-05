package org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.sample;

import java.util.HashMap;
import java.util.Map;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;

/**
 * Sigar-based implementation of {@link DiskIOMonitor}. 
 */
public class SigarDiskIOMonitor implements DiskIOMonitor
{
    private final Sigar _sigar;
    private final SigarProxy _proxy;

    private long _reads;
    private long _readBytes;
    private long _writes;
    private long _writeBytes;
    
    /**
     * Constructor.
     */
    public SigarDiskIOMonitor()
    {
        _sigar = new Sigar();
        _proxy = SigarProxyCache.newInstance(_sigar);

        refresh();
    }

    @Override
    public void refresh()
    {
        _reads = 0;
        _readBytes = 0;
        _writes = 0;
        _writeBytes = 0;

        FileSystem[] fsList;

        try
        {
            fsList = _proxy.getFileSystemList();
        }
        catch (SigarException e)
        {
            return;
        }

        for (FileSystem fs : fsList)
        {
            if (fs.getType() == FileSystem.TYPE_LOCAL_DISK)
            {
                try
                {
                    FileSystemUsage usage = _sigar.getFileSystemUsage(fs.getDirName());

                    _reads += usage.getDiskReads();
                    _readBytes += usage.getDiskReadBytes();
                    _writes += usage.getDiskWrites();
                    _writeBytes += usage.getDiskWriteBytes();
                }
                catch (SigarException e)
                {
                    continue;
                }
            }
        }
    }

    @Override
    public double getReads()
    {
        return _reads;
    }

    @Override
    public double getReadBytes()
    {
        return _readBytes;
    }

    @Override
    public double getWrites()
    {
        return _writes;
    }

    @Override
    public double getWriteBytes()
    {
        return _writeBytes;
    }

    @Override
    public Map<String, Double> toMap()
    {
        Map<String, Double> map = new HashMap<String, Double>();

        map.put(DiskIOMonitor.READS, getReads());
        map.put(DiskIOMonitor.READ_BYTES, getReadBytes());
        map.put(DiskIOMonitor.WRITES, getWrites());
        map.put(DiskIOMonitor.WRITE_BYTES, getWriteBytes());

        return map;
    }
}
