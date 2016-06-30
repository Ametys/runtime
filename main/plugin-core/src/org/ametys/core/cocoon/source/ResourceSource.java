/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.cocoon.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.AbstractSource;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Note : duplication of the final class org.apache.excalibur.source.impl.ResourceSource
/**
 * Description of a source which is described by the resource protocol which
 * gets a resource from the classloader.
 */
public final class ResourceSource extends AbstractSource
{
    private static final Logger __LOGGER = LoggerFactory.getLogger(ResourceSource.class);
    
    /** Location of the resource */
    private URL _location;

    private String _mimeType;
    
    private String _path;

    /**
     * Constructor.<br>
     * @param systemId URI of the desired resource
     * @throws MalformedURLException if the systemId is not like &lt;scheme&gt;://&lt;path&gt;
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
        _path = NetUtils.normalize(systemId.substring(pos + "://".length())).replace('\\', '/'); // A path with / and \ mixed will fail in ZipEntry

        if (_path.startsWith("/"))
        {
            _path = _path.substring(1);
        }
        
        setSystemId(scheme + "://" + _path);
        _location = getClassLoader().getResource(_path);
        setScheme(scheme);
    }

    public boolean exists()
    {
        return _location != null;
    }

    @Override
    protected void getInfos()
    {
        URLConnection connection = null;
        try
        {
            _mimeType = URLConnection.getFileNameMap().getContentTypeFor(_path);

            connection = _location.openConnection();

            // Sun's JDK contains a bug leaking file descriptors when calling JarURLConnection.getLastModified()
            // In case of JarURLConnection, we could access directly to the underlying jar file.
            if (connection instanceof JarURLConnection)
            {
                URL jarFileURL = ((JarURLConnection) connection).getJarFileURL();
                if ("file".equals(jarFileURL.getProtocol()))
                {
                    File file = new File(jarFileURL.toURI());
                    
                    try (JarFile jarFile = new JarFile(file))
                    {
                        ZipEntry entry = jarFile.getEntry(_path);
    
                        setLastModified(entry.getTime());
                        setContentLength(entry.getSize());
                    }
                }
                else
                {
                    // Not that bad since we know the JAR lastmodified >= entry lastmodified
                    URLConnection jarconnection = jarFileURL.openConnection();
                    setLastModified(jarconnection.getLastModified());
                    jarconnection.getInputStream().close();
                    
                    setContentLength(connection.getContentLength());
                }
            }
            else
            {
                setLastModified(connection.getLastModified());
                setContentLength(connection.getContentLength());
            }
        }
        catch (Exception e)
        {
            __LOGGER.error("An error occurred while accessing info for resource " + _path, e);
            
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
        if (_location == null)
        {
            throw new SourceNotFoundException("Resource not found for URI : " + getURI());
        }
        
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
