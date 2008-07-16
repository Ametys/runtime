/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
