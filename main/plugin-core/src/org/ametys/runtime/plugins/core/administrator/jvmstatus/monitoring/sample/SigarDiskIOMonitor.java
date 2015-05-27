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
        Map<String, Double> map = new HashMap<>();

        map.put(DiskIOMonitor.READS, getReads());
        map.put(DiskIOMonitor.READ_BYTES, getReadBytes());
        map.put(DiskIOMonitor.WRITES, getWrites());
        map.put(DiskIOMonitor.WRITE_BYTES, getWriteBytes());

        return map;
    }
}
