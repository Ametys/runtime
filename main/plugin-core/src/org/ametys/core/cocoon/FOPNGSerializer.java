/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.cocoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.serialization.AbstractSerializer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

/**
 * FOP 0.95 (and newer) based serializer.
 */
public class FOPNGSerializer extends AbstractSerializer implements Configurable, CacheableProcessingComponent, Serviceable, ResourceResolver, Disposable, Contextualizable
{
    /** The source resolver */
    protected SourceResolver _resolver;

    /**
     * Factory to create fop objects
     */
    protected FopFactory _fopfactory;

    /**
     * The FOP instance.
     */
    protected Fop _fop;

    /**
     * The current <code>mime-type</code>.
     */
    protected String _mimetype;

    /**
     * Should we set the content length ?
     */
    protected boolean _setContentLength = true;

    /**
     * Manager to get URLFactory from.
     */
    protected ServiceManager _manager;

    private Map _rendererOptions;
    
    private Context _context;
    
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        _context = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * Set the component manager for this serializer.
     */
    public void service(ServiceManager smanager) throws ServiceException
    {
        this._manager = smanager;
        this._resolver = (SourceResolver) this._manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException
    {
        // should the content length be set
        this._setContentLength = conf.getChild("set-content-length").getValueAsBoolean(true);

        Configuration config = null;
        String configUrl = conf.getChild("user-config").getValue(null);
        if (configUrl != null)
        {
            Source configSource = null;
            SourceResolver resolver = null;
            try
            {
                resolver = (SourceResolver) this._manager.lookup(SourceResolver.ROLE);
                configSource = resolver.resolveURI(configUrl);
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Loading configuration from " + configSource.getURI());
                }
                SAXConfigurationHandler configHandler = new SAXConfigurationHandler();
                SourceUtil.toSAX(configSource, configHandler);
                
                config = configHandler.getConfiguration();
            }
            catch (Exception e)
            {
                getLogger().warn("Cannot load configuration from " + configUrl);
                throw new ConfigurationException("Cannot load configuration from " + configUrl, e);
            }
            finally
            {
                if (resolver != null)
                {
                    resolver.release(configSource);
                    _manager.release(resolver);
                }
            }
        }

        File base = new File(_context.getRealPath("/"));
        FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(base.toURI(), this);
        
        if (config != null)
        {
            fopFactoryBuilder.setConfiguration(config);
        }
        
        _fopfactory = fopFactoryBuilder.build();

        // Get the mime type.
        this._mimetype = conf.getAttribute("mime-type");

        Configuration confRenderer = conf.getChild("renderer-config");
        if (confRenderer != null)
        {
            Configuration[] parameters = confRenderer.getChildren("parameter");
            if (parameters.length > 0)
            {
                _rendererOptions = new HashMap();
                for (int i = 0; i < parameters.length; i++)
                {
                    String name = parameters[i].getAttribute("name");
                    String value = parameters[i].getAttribute("value");

                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("renderer " + String.valueOf(name) + " = " + String.valueOf(value));
                    }
                }
            }
        }
    }

    /**
     * Recycle serializer by removing references
     */
    @Override
    public void recycle()
    {
        super.recycle();
        this._fop = null;
    }

    public void dispose()
    {
        if (this._resolver != null)
        {
            this._manager.release(this._resolver);
            this._resolver = null;
        }
        this._manager = null;
    }

    // -----------------------------------------------------------------

    /**
     * Return the MIME type.
     */
    @Override
    public String getMimeType()
    {
        return _mimetype;
    }

    @Override
    public void setOutputStream(OutputStream out) throws IOException
    {

        // Give the source resolver to Batik which is used by FOP
        // SourceProtocolHandler.setup(this.resolver);

        FOUserAgent userAgent = _fopfactory.newFOUserAgent();
        if (this._rendererOptions != null)
        {
            userAgent.getRendererOptions().putAll(this._rendererOptions);
        }
        try
        {
            this._fop = _fopfactory.newFop(getMimeType(), userAgent, out);
            setContentHandler(this._fop.getDefaultHandler());
        }
        catch (FOPException e)
        {
            getLogger().error("FOP setup failed", e);
            throw new IOException("Unable to setup fop: " + e.getLocalizedMessage());
        }
    }

    /**
     * Generate the unique key. This key must be unique inside the space of this
     * component. This method must be invoked before the generateValidity()
     * method.
     * 
     * @return The generated key or <code>0</code> if the component is currently
     *         not cacheable.
     */
    public Serializable getKey()
    {
        return "1";
    }

    /**
     * Generate the validity object. Before this method can be invoked the
     * generateKey() method must be invoked.
     * 
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity()
    {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Test if the component wants to set the content length
     */
    @Override
    public boolean shouldSetContentLength()
    {
        return this._setContentLength;
    }
    
    public Resource getResource(URI uri) throws IOException
    {
        String href = uri.toString();
        
        if (href.indexOf(':') != -1)
        {
            Source source = _resolver.resolveURI(href);
            return new Resource(source.getMimeType(), source.getInputStream());
        }
        else
        {
            String base = _context.getRealPath("/");
            
            if (href.startsWith("/"))
            {
                href = href.substring(1);
            }
            
            File source = new File(base, href);
            return new Resource(new FileInputStream(source));
        }
    }
    
    public OutputStream getOutputStream(URI uri) throws IOException
    {
        throw new UnsupportedOperationException("getOutputStream");
    }

    /**
     * An InputStream which releases the Cocoon/Avalon source from which the
     * InputStream has been retrieved when the stream is closed.
     */
    public static final class ReleaseSourceInputStream extends InputStream
    {
        private InputStream _delegate;

        private Source _source;

        private SourceResolver _sourceResolver;

        ReleaseSourceInputStream(InputStream delegate, Source source, SourceResolver sourceResolver)
        {
            this._delegate = delegate;
            this._source = source;
            this._sourceResolver = sourceResolver;
        }

        @Override
        public void close() throws IOException
        {
            _delegate.close();
            _sourceResolver.release(_source);
        }

        @Override
        public int read() throws IOException
        {
            return _delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException
        {
            return _delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            return _delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException
        {
            return _delegate.skip(n);
        }

        @Override
        public int available() throws IOException
        {
            return _delegate.available();
        }

        @Override
        public synchronized void mark(int readlimit)
        {
            _delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException
        {
            _delegate.reset();
        }

        @Override
        public boolean markSupported()
        {
            return _delegate.markSupported();
        }
    }
}
