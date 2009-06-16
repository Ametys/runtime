package org.ametys.runtime.cocoon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.serialization.AbstractSerializer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;

/**
 * FOP 0.95 (and newer) based serializer.
 */
public class FOPNGSerializer extends AbstractSerializer implements Configurable, CacheableProcessingComponent, Serviceable, URIResolver, Disposable
{
    /** The source resolver */
    protected SourceResolver _resolver;

    /**
     * Factory to create fop objects
     */
    protected FopFactory _fopfactory = FopFactory.newInstance();

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
                _fopfactory.setUserConfig(configHandler.getConfiguration());
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

        _fopfactory.setURIResolver(this);

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

    @SuppressWarnings("unchecked")
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

    // From URIResolver, copied from TraxProcessor
    public javax.xml.transform.Source resolve(String href, String base) throws TransformerException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("resolve(href = " + href + ", base = " + base + "); resolver = " + _resolver);
        }

        StreamSource streamSource = null;
        Source source = null;
        try
        {
            if (base == null || href.indexOf(":") > 1)
            {
                // Null base - href must be an absolute URL
                source = _resolver.resolveURI(href);
            }
            else if (href.length() == 0)
            {
                // Empty href resolves to base
                source = _resolver.resolveURI(base);
            }
            else
            {
                // is the base a file or a real m_url
                if (!base.startsWith("file:"))
                {
                    int lastPathElementPos = base.lastIndexOf('/');
                    if (lastPathElementPos == -1)
                    {
                        // this should never occur as the base should
                        // always be protocol:/....
                        return null; // we can't resolve this
                    }
                    else
                    {
                        source = _resolver.resolveURI(base.substring(0, lastPathElementPos) + "/" + href);
                    }
                }
                else
                {
                    File parent = new File(base.substring(5));
                    File parent2 = new File(parent.getParentFile(), href);
                    source = _resolver.resolveURI(parent2.toURI().toURL().toExternalForm());
                }
            }

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("source = " + source + ", system id = " + source.getURI());
            }

            streamSource = new StreamSource(new ReleaseSourceInputStream(source.getInputStream(), source, _resolver), source.getURI());
        }
        catch (SourceException e)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", e);
            }

            // CZ: To obtain the same behaviour as when the resource is
            // transformed by the XSLT Transformer we should return null here.
            return null;
        }
        catch (java.net.MalformedURLException mue)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", mue);
            }

            return null;
        }
        catch (IOException ioe)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", ioe);
            }

            return null;
        }
        finally
        {
            // If streamSource is not null, the source should only be released
            // when the input stream
            // is not needed anymore.
            if (streamSource == null)
            {
                _resolver.release(source);
            }
        }
        return streamSource;
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
