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
import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

/**
 * Implementation SourceFactory giving access to ZipSource, implementation of Source related to a ZipEntry
 */
public class ZipSourceFactory implements SourceFactory
{
    public Source getSource(String location, Map parameters) throws IOException
    {
        return new ZipSource(location);
    }

    public void release(Source source)
    {
        ((ZipSource) source).close();
    }
}
