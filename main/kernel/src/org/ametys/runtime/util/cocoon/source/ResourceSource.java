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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.AbstractSource;
import org.apache.excalibur.source.impl.validity.NOPValidity;

// Note : le code de cette classe est une recopie de
// org.apache.excalibur.source.impl.ResourceSource, déclarée "final"
/**
 * Description of a source which is described by the resource protocol which
 * gets a resource from the classloader.
 */
public final class ResourceSource extends AbstractSource implements Source
{
    /** Location of the resource */
    private URL _location;

    private String _mimeType;

    /**
     * Constructor.<br>
     * @param systemId URI of the desired resource
     * @throws MalformedURLException if the systemId is not like &lt;scheme>://&lt;path>
     * @throws SourceNotFoundException if the location can not be found in the class loader
     */
    public ResourceSource(final String systemId) throws MalformedURLException, SourceNotFoundException
    {
        final int pos = SourceUtil.indexOfSchemeColon(systemId);
        if (pos == -1 || !systemId.startsWith("://", pos))
        {
            throw new MalformedURLException("Invalid format for ResourceSource : " + systemId);
        }

        String scheme = systemId.substring(0, pos);
        String path = NetUtils.normalize(systemId.substring(pos + "://".length()));

        setSystemId(scheme + "://" + path);
        _location = getClassLoader().getResource(path);
        setScheme(scheme);
        
        if (_location == null)
        {
            throw new SourceNotFoundException("Resource not found for URI : " + getURI());
        }
    }

    public boolean exists()
    {
        return _location != null;
    }

    @Override
    protected void checkInfos()
    {
        super.checkInfos();

        URLConnection connection = null;
        try
        {
            connection = _location.openConnection();
            setLastModified(connection.getLastModified());
            setContentLength(connection.getContentLength());
            _mimeType = connection.getContentType();
        }
        catch (IOException ioe)
        {
            setLastModified(0);
            setContentLength(-1);
            _mimeType = null;
        }
    }

    @Override
    public String getMimeType()
    {
        return _mimeType;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        InputStream in = _location.openStream();
        if (in == null)
        {
            throw new SourceNotFoundException("Source '" + _location + "' was not found");
        }
        
        return in;
    }

    /**
     * Returns {@link NOPValidity#SHARED_INSTANCE}since a resource doesn't
     * change.
     *  
     */
    @Override
    public SourceValidity getValidity()
    {
        // we are always valid
        return NOPValidity.SHARED_INSTANCE;
    }

    private ClassLoader getClassLoader()
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null)
        {
            loader = getClass().getClassLoader();
        }

        return loader;
    }
}
