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
package org.ametys.runtime.util.cocoon.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * Implementation of a Source giving access to a zip entry
 */
public class ZipSource extends AbstractSource
{
    private ZipFile _zipFile;

    private ZipEntry _zipEntry;

    /**
     * Instanciate a ZipSource
     * @param location the ZipSource URI, looking like : zip://path/to/file!path/to/entry
     */
    public ZipSource(String location)
    {
        int index = location.indexOf("://");
        
        if (index == -1)
        {
            throw new IllegalArgumentException("A ZipSource URI must be like zip://path/to/file!path/to/entry");
        }
        
        setScheme(location.substring(0, index));
        setSystemId(location.substring(index + 3));

        int fileIndex = location.indexOf('!');
        
        if (fileIndex < index)
        {
            throw new IllegalArgumentException("A ZipSource URI must be like zip://path/to/file!path/to/entry");
        }
        
        String file = location.substring(index + 1, fileIndex);
        try
        {
            _zipFile = new ZipFile(file);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to open zip file", e);
        }

        String entry = location.substring(fileIndex + 1);
        _zipEntry = _zipFile.getEntry(entry);
    }

    public boolean exists()
    {
        return _zipFile != null && _zipEntry != null;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        if (_zipFile == null || _zipEntry == null)
        {
            throw new SourceNotFoundException("ZipSource not available");
        }
        
        return _zipFile.getInputStream(_zipEntry);
    }

    /**
     * Releases resources associated with this ZipSource.
     */
    public void close()
    {
        _zipEntry = null;

        try
        {
            _zipFile.close();
            _zipFile = null;
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to close zip file");
        }
    }
}
