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
package org.ametys.runtime.cocoon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.xslt.TraxErrorListener;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;
import org.apache.excalibur.xmlizer.XMLizer;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import org.ametys.runtime.config.Config;

/**
 * Adaptation of Excalibur's XSLTProcessor implementation to allow for better error reporting. This implementation is also threadsafe.<br>
 * It also handles a {@link Templates} cache, for performance purpose.
 */
public class ThreadSafeTraxProcessor extends AbstractLogEnabled implements XSLTProcessor, Serviceable, Initializable, Disposable, Parameterizable, URIResolver, Contextualizable
{
    private static final String __URI_CACHE_ATTR = "cache.xslt.resolvedURIs";
    
    /** The configured transformer factory to use */
    private String _transformerFactory;

    /** The trax TransformerFactory this component uses */
    private SAXTransformerFactory _factory;

    /** Is incremental processing turned on? (default for Xalan: no) */
    private boolean _incrementalProcessing;

    /** Resolver used to resolve XSLT document() calls, imports and includes */
    private SourceResolver _resolver;

    private XMLizer _xmlizer;

    /** The ServiceManager */
    private ServiceManager _manager;
    
    private Context _context;
    
    private boolean _dontUseCache;
    
    // the XSLT cache
    private Map<String, Collection<CachedTemplates>> _templatesCache = new HashMap<String, Collection<CachedTemplates>>();
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }

    /**
     * Compose. Try to get the store
     * 
     * @avalon.service interface="XMLizer"
     * @avalon.service interface="SourceResolver"
     * @avalon.service interface="Store/TransientStore" optional="true"
     */
    public void service(final ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _xmlizer = (XMLizer) _manager.lookup(XMLizer.ROLE);
        _resolver = (SourceResolver) _manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Initialize
     */
    public void initialize() throws Exception
    {
        _factory = _createTransformerFactory(_transformerFactory);
        _dontUseCache = Config.getInstance().getValueAsBoolean("runtime.cache.xslt");
    }

    /**
     * Disposable
     */
    public void dispose()
    {
        if (null != _manager)
        {
            _manager.release(_resolver);
            _manager.release(_xmlizer);
            _manager = null;
        }
        _xmlizer = null;
        _resolver = null;
        _templatesCache.clear();
    }

    /**
     * Configure the component
     */
    public void parameterize(final Parameters params) throws ParameterException
    {
        _incrementalProcessing = params.getParameterAsBoolean("incremental-processing", this._incrementalProcessing);
        _transformerFactory = params.getParameter("transformer-factory", null);
    }

    public void setTransformerFactory(final String classname)
    {
        throw new UnsupportedOperationException("This implementation is threadsafe, so the TransformerFactory cannot be changed");
    }

    public TransformerHandler getTransformerHandler(final Source stylesheet) throws XSLTProcessorException
    {
        return getTransformerHandler(stylesheet, null);
    }

    public TransformerHandler getTransformerHandler(final Source stylesheet, final XMLFilter filter) throws XSLTProcessorException
    {
        final XSLTProcessor.TransformerHandlerAndValidity validity = getTransformerHandlerAndValidity(stylesheet, filter);
        return validity.getTransfomerHandler();
    }

    public TransformerHandlerAndValidity getTransformerHandlerAndValidity(final Source stylesheet) throws XSLTProcessorException
    {
        return getTransformerHandlerAndValidity(stylesheet, null);
    }

    public TransformerHandlerAndValidity getTransformerHandlerAndValidity(Source stylesheet, XMLFilter filter) throws XSLTProcessorException
    {
        TraxErrorListener errorListener = new TraxErrorListener(getLogger(), stylesheet.getURI());
        try
        {
            // get Templates from cache or create it
            Templates template = _getTemplates(stylesheet, filter);
            
            // Create transformer handler
            TransformerHandler handler = _factory.newTransformerHandler(template);
            handler.getTransformer().setErrorListener(errorListener);
            handler.getTransformer().setURIResolver(this);

            // Create result
            SourceValidity validity = stylesheet.getValidity();
            TransformerHandlerAndValidity handlerAndValidity = new MyTransformerHandlerAndValidity(handler, validity);

            return handlerAndValidity;
        }
        catch (IOException e)
        {
            throw new XSLTProcessorException("Exception when getting Templates for " + stylesheet.getURI(), e);
        }
        catch (TransformerConfigurationException e)
        {
            Throwable realEx = errorListener.getThrowable();
            if (realEx == null)
            {
                realEx = e;
            }

            if (realEx instanceof RuntimeException)
            {
                throw (RuntimeException) realEx;
            }

            if (realEx instanceof XSLTProcessorException)
            {
                throw (XSLTProcessorException) realEx;
            }

            throw new XSLTProcessorException("Exception when creating Transformer from " + stylesheet.getURI(), realEx);
        }
    }
    
    private Templates _getTemplates(Source stylesheet, XMLFilter filter) throws XSLTProcessorException, IOException
    {
        String uri = stylesheet.getURI().intern();
        
        // synchronize on XSL name to avoid concurrent write access to the cache for a given stylesheet
        synchronized (uri)
        {
            Collection<CachedTemplates> cachedTemplates = _templatesCache.get(uri);
            
            if (!_dontUseCache)
            {
                if (cachedTemplates != null)
                {
                    CachedTemplates templates = _getCachedTemplates(cachedTemplates);
                    
                    if (templates != null)
                    {
                        if (getLogger().isDebugEnabled())
                        {
                            getLogger().debug("Found Templates in cache for stylesheet : " + uri);
                        }
                        
                        return templates.getTemplates();
                    }
                }
                else
                {
                    cachedTemplates = new ArrayList<CachedTemplates>();
                    _templatesCache.put(uri, cachedTemplates);
                }
            }
            
            CachedTemplates templates = _createTemplates(stylesheet, filter);
            
            if (getLogger().isDebugEnabled())
            {
                String[] rawURIs = templates.getRawURIs();
                String[] resolvedURIs = templates.getResolvedURIs();
                
                StringBuilder sb = new StringBuilder("Templates created for stylesheet : ");
                sb.append(uri);
                sb.append(" including the following stylesheets : ");
                for (int i = 0; i < rawURIs.length; i++)
                {
                    sb.append('\n');
                    sb.append(rawURIs[i]);
                    sb.append(" => ");
                    sb.append(resolvedURIs[i]);
                }
                
                getLogger().debug(sb.toString());
            }
            
            if (!_dontUseCache)
            {
                cachedTemplates.add(templates);
            }
            
            return templates.getTemplates();
        }
    }
    
    private CachedTemplates _getCachedTemplates(Collection<CachedTemplates> cachedTemplates) throws IOException
    {
        CachedTemplates outOfDateTemplates = null;
        
        Iterator<CachedTemplates> it = cachedTemplates.iterator();
        
        Request request = null;
        try
        {
            request = ContextHelper.getRequest(_context);
        }
        catch (Exception e)
        {
            // ignore, there's simply no current request
        }
        
        // very simple cache for storing raw/resolved URI pairs to avoid unnecessary calls to SourceResolver 
        Map<UnresolvedURI, ResolvedURI> resolutionCache = null;
        if (request != null)
        {
            resolutionCache = (Map<UnresolvedURI, ResolvedURI>) request.getAttribute(__URI_CACHE_ATTR);
            if (resolutionCache == null)
            {
                resolutionCache = new HashMap<UnresolvedURI, ResolvedURI>();
                request.setAttribute(__URI_CACHE_ATTR, resolutionCache);
            }
        }
        else
        {
            resolutionCache = new HashMap<UnresolvedURI, ResolvedURI>();
        }
        
        
        while (outOfDateTemplates == null && it.hasNext())
        {
            CachedTemplates templates = it.next();
            
            int validity = _isValid(templates, resolutionCache);
            if (validity == 1)
            {
                return templates;
            }
            
            if (validity == 0)
            {
                outOfDateTemplates = templates;
            }
        }
        
        if (outOfDateTemplates != null)
        {
            cachedTemplates.remove(outOfDateTemplates);
        }
        
        return null;
    }
    
    private int _isValid(CachedTemplates templates, Map<UnresolvedURI, ResolvedURI> resolutionCache) throws IOException
    {
        // the current Templates object is valid if and only if the resolution of raw URIs correspond to stored resolved URIs
        String[] rawURIs = templates.getRawURIs();
        String[] baseURIs = templates.getBaseURIs();
        String[] resolvedURIs = templates.getResolvedURIs();
        Long[] timestamps = templates.getTimestamps();
        
        boolean isOutOfDate = false;
        
        for (int i = 0; i < rawURIs.length; i++)
        {
            // small optimization in the case where the same resolution has already been requested in the current context
            UnresolvedURI unresolved = new UnresolvedURI(rawURIs[i], baseURIs[i]);
            ResolvedURI resolved = resolutionCache.get(new UnresolvedURI(rawURIs[i], baseURIs[i]));
            
            String resolvedURI;
            long lastModified;
            if (resolved != null)
            {
                resolvedURI = resolved._resolvedURI;
                lastModified = resolved._timestamp;
            }
            else
            {
                Source src = _resolve(rawURIs[i], baseURIs[i]);
                resolvedURI = src.getURI();
                lastModified = src.getLastModified();
                resolutionCache.put(unresolved, new ResolvedURI(resolvedURI, lastModified));
            }
            
            if (!resolvedURI.equals(resolvedURIs[i]))
            {
                return -1;
            }
            
            if (lastModified == 0 || timestamps[i] == 0 || lastModified != timestamps[i])
            {
                isOutOfDate = true;
            }
        }
        
        return isOutOfDate ? 0 : 1;
    }
    
    @SuppressWarnings("unchecked")
    private CachedTemplates _createTemplates(Source stylesheet, XMLFilter filter) throws XSLTProcessorException
    {
        String id = stylesheet.getURI();

        // Do not reuse the global SAXTransformerFactory, as we set a different URIResolver and ErrorListener
        TraxErrorListener errorListener = new TraxErrorListener(getLogger(), id);
        CachedTemplates cachedTemplates = new CachedTemplates();
        
        SAXTransformerFactory factory = _createTransformerFactory(_transformerFactory);
        factory.setErrorListener(errorListener);
        factory.setURIResolver(cachedTemplates);
        try
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Creating new Templates for " + id);
            }

            factory.setErrorListener(errorListener);

            // Create a Templates ContentHandler to handle parsing of the
            // stylesheet.
            TemplatesHandler templatesHandler = factory.newTemplatesHandler();

            // Set the system ID for the template handler since some
            // TrAX implementations (XSLTC) rely on this in order to obtain
            // a meaningful identifier for the Templates instances.
            templatesHandler.setSystemId(id);
            if (filter != null)
            {
                filter.setContentHandler(templatesHandler);
            }

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Source = " + stylesheet + ", templatesHandler = " + templatesHandler);
            }

            // Process the stylesheet.
            _sourceToSAX(stylesheet, filter != null ? (ContentHandler) filter : (ContentHandler) templatesHandler);

            // Get the Templates object (generated during the parsing of
            // the stylesheet) from the TemplatesHandler.
            final Templates template = templatesHandler.getTemplates();

            if (null == template)
            {
                throw new XSLTProcessorException("Unable to create templates for stylesheet: " + stylesheet.getURI());
            }

            // Must set base for Xalan stylesheet.
            // Otherwise document('') in logicsheet causes NPE.
            Class clazz = template.getClass();
            if (clazz.getName().equals("org.apache.xalan.templates.StylesheetRoot"))
            {
                Method method = clazz.getMethod("setHref", new Class[] {String.class});
                method.invoke(template, new Object[] {id});
            }
            
            cachedTemplates.setTemplates(template);
            
            return cachedTemplates;
        }
        catch (Exception e)
        {
            Throwable realEx = errorListener.getThrowable();
            if (realEx == null)
            {
                realEx = e;
            }

            if (realEx instanceof RuntimeException)
            {
                throw (RuntimeException) realEx;
            }

            if (realEx instanceof XSLTProcessorException)
            {
                throw (XSLTProcessorException) realEx;
            }

            throw new XSLTProcessorException("Exception when creating Transformer from " + stylesheet.getURI(), realEx);
        }
    }
    
    private void _sourceToSAX(Source source, ContentHandler handler) throws SAXException, IOException, SourceException
    {
        if (source instanceof XMLizable)
        {
            ((XMLizable) source).toSAX(handler);
        }
        else
        {
            final InputStream inputStream = source.getInputStream();
            final String mimeType = source.getMimeType();
            final String systemId = source.getURI();
            _xmlizer.toSAX(inputStream, mimeType, systemId, handler);
        }
    }

    public void transform(final Source source, final Source stylesheet, final Parameters params, final Result result) throws XSLTProcessorException
    {
        try
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Transform source = " + source + ", stylesheet = " + stylesheet + ", parameters = " + params + ", result = " + result);
            }
            final TransformerHandler handler = getTransformerHandler(stylesheet);
            if (params != null)
            {
                final Transformer transformer = handler.getTransformer();
                transformer.clearParameters();
                String[] names = params.getNames();
                for (int i = names.length - 1; i >= 0; i--)
                {
                    transformer.setParameter(names[i], params.getParameter(names[i]));
                }
            }

            handler.setResult(result);
            _sourceToSAX(source, handler);
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Transform done");
            }
        }
        catch (SAXException e)
        {
            // Unwrapping the exception will "remove" the real cause with
            // never Xalan versions and makes the exception message unusable
            final String message = "Error in running Transformation";
            throw new XSLTProcessorException(message, e);
            /*
             * if( e.getException() == null ) { final String message = "Error in running Transformation"; throw new XSLTProcessorException( message, e ); } else { final String message = "Got SAXException. Rethrowing cause exception."; getLogger().debug( message, e ); throw new XSLTProcessorException( "Error in running Transformation", e.getException() ); }
             */
        }
        catch (Exception e)
        {
            final String message = "Error in running Transformation";
            throw new XSLTProcessorException(message, e);
        }
    }

    /**
     * Get the TransformerFactory associated with the given classname. If the class can't be found or the given class doesn't implement the required interface, the default factory is returned.
     */
    private SAXTransformerFactory _createTransformerFactory(String factoryName)
    {
        SAXTransformerFactory saxFactory;

        if (null == factoryName)
        {
            saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        }
        else
        {
            try
            {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                if (loader == null)
                {
                    loader = getClass().getClassLoader();
                }
                
                saxFactory = (SAXTransformerFactory) loader.loadClass(factoryName).newInstance();
            }
            catch (ClassNotFoundException cnfe)
            {
                getLogger().error("Cannot find the requested TrAX factory '" + factoryName + "'. Using default TrAX Transformer Factory instead.");
                if (_factory != null)
                {
                    return _factory;
                }
                
                saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            }
            catch (ClassCastException cce)
            {
                getLogger().error("The indicated class '" + factoryName + "' is not a TrAX Transformer Factory. Using default TrAX Transformer Factory instead.");
                if (_factory != null)
                {
                    return _factory;
                }
                
                saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            }
            catch (Exception e)
            {
                getLogger().error("Error found loading the requested TrAX Transformer Factory '" + factoryName + "'. Using default TrAX Transformer Factory instead.");
                if (_factory != null)
                {
                    return _factory;
                }
                
                saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            }
        }

        saxFactory.setErrorListener(new TraxErrorListener(getLogger(), null));
        saxFactory.setURIResolver(this);

        if (saxFactory.getClass().getName().equals("org.apache.xalan.processor.TransformerFactoryImpl"))
        {
            saxFactory.setAttribute("http://xml.apache.org/xalan/features/incremental", BooleanUtils.toBooleanObject(_incrementalProcessing));
        }
        // SAXON 8 will not report errors unless version warning is set to false.
        if (saxFactory.getClass().getName().equals("net.sf.saxon.TransformerFactoryImpl"))
        {
            saxFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
        }

        return saxFactory;
    }

    /**
     * Called by the processor when it encounters an xsl:include, xsl:import, or document() function.
     * 
     * @param href An href attribute, which may be relative or absolute.
     * @param base The base URI in effect when the href attribute was encountered.
     * 
     * @return A Source object, or null if the href cannot be resolved, and the processor should try to resolve the URI itself.
     * 
     * @throws TransformerException if an error occurs when trying to resolve the URI.
     */
    public javax.xml.transform.Source resolve(String href, String base) throws TransformerException
    {
        return _resolve(href, base, null, null, null, null);
    }
    
    @SuppressWarnings("deprecation") 
    private Source _resolve(String href, String base) throws IOException
    {
        Source xslSource = null;

        if (base == null || href.indexOf(":") > 1)
        {
            // Null base - href must be an absolute URL
            xslSource = _resolver.resolveURI(href);
        }
        else if (href.length() == 0)
        {
            // Empty href resolves to base
            xslSource = _resolver.resolveURI(base);
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
                    xslSource = _resolver.resolveURI(base.substring(0, lastPathElementPos) + "/" + href);
                }
            }
            else
            {
                File parent = new File(base.substring(5));
                File parent2 = new File(parent.getParentFile(), href);
                xslSource = _resolver.resolveURI(parent2.toURL().toExternalForm());
            }
        }
        
        return xslSource;
    }
    
    javax.xml.transform.Source _resolve(String href, String base, Collection<String> rawURIs, Collection<String> baseURIs, Collection<Long> timestamps, Collection<String> resolvedURIs)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("resolve(href = " + href + ", base = " + base + "); resolver = " + _resolver);
        }

        Source xslSource = null;
        try
        {
            xslSource = _resolve(href, base);
            
            if (rawURIs != null)
            {
                rawURIs.add(href);
            }
            
            if (baseURIs != null)
            {
                baseURIs.add(base);
            }
            
            if (timestamps != null)
            {
                timestamps.add(xslSource.getLastModified());
            }

            if (resolvedURIs != null)
            {
                resolvedURIs.add(xslSource.getURI());
            }

            InputSource is = _getInputSource(xslSource);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("xslSource = " + xslSource + ", system id = " + xslSource.getURI());
            }

            return new StreamSource(is.getByteStream(), is.getSystemId());
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
            _resolver.release(xslSource);
        }
    }

    /**
     * Return a new <code>InputSource</code> object that uses the <code>InputStream</code> and the system ID of the <code>Source</code> object.
     * @throws IOException if I/O error occured.
     */
    private InputSource _getInputSource(final Source source) throws IOException, SourceException
    {
        final InputSource newObject = new InputSource(source.getInputStream());
        newObject.setSystemId(source.getURI());
        return newObject;
    }

    /**
     * Subclass to allow for instanciation, as for some unknown reason the constructor is protected....
     */
    static class MyTransformerHandlerAndValidity extends TransformerHandlerAndValidity
    {
        MyTransformerHandlerAndValidity(TransformerHandler handler, SourceValidity validity)
        {
            super(handler, validity);
        }
    }
    
    // all known Templates for a single input stylesheet
    private class CachedTemplates implements URIResolver
    {
        // non-resolved included/imported URIs for the input stylesheet
        private List<String> _rawURIs = new ArrayList<String>();
        
        // base URIs for resolution
        private List<String> _baseURIs = new ArrayList<String>();
        
        // resolved URIs
        private List<String> _resolvedURIs = new ArrayList<String>();
        
        // last modified timestamps for resolved URIs
        private List<Long> _timestamps = new ArrayList<Long>();
        
        // resulting templates
        private Templates _templates;
        
        CachedTemplates()
        {
            // empty
        }

        @Override
        public javax.xml.transform.Source resolve(String href, String base) throws TransformerException
        {
            return _resolve(href, base, _rawURIs, _baseURIs, _timestamps, _resolvedURIs);
        }
        
        String[] getRawURIs()
        {
            return _rawURIs.toArray(new String[]{});
        }
        
        String[] getBaseURIs()
        {
            return _baseURIs.toArray(new String[]{});
        }
        
        Long[] getTimestamps()
        {
            return _timestamps.toArray(new Long[]{});
        }
        
        String[] getResolvedURIs()
        {
            return _resolvedURIs.toArray(new String[]{});
        }
        
        Templates getTemplates()
        {
            return _templates;
        }
        
        void setTemplates(Templates templates)
        {
            _templates = templates;
        }
    }
    
    private static class UnresolvedURI
    {
        String _rawURI;
        String _baseURI;
        
        public UnresolvedURI(String rawURI, String baseURI)
        {
            _rawURI = rawURI;
            _baseURI = baseURI;
        }
        
        @Override
        public int hashCode()
        {
            if (_rawURI.indexOf(':') > 1)
            {
                // rawURI is absolute
                return _rawURI.hashCode();
            }
            
            int lastPathElementPos = _baseURI.lastIndexOf('/');
            if (lastPathElementPos == -1)
            {
                // this should never occur as the base should always be protocol:/....
                return _rawURI.hashCode();
            }
            else
            {
                String uri = _baseURI.substring(0, lastPathElementPos) + "/" + _rawURI;
                return uri.hashCode();
            }
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof UnresolvedURI))
            {
                return false;
            }
            
            UnresolvedURI unresolved = (UnresolvedURI) obj;
            
            if (_rawURI.indexOf(':') > 1)
            {
                // rawURI is absolute
                return _rawURI.equals(unresolved._rawURI);
            }
            
            int lastPathElementPos = _baseURI.lastIndexOf('/');
            if (lastPathElementPos == -1)
            {
                // this should never occur as the base should always be protocol:/....
                return false;
            }
            else if (unresolved._rawURI.length() <= lastPathElementPos)
            {
                return false;
            }
            else
            {
                String uri1 = _baseURI.substring(0, lastPathElementPos) + "/" + _rawURI;
                String uri2 = unresolved._baseURI.substring(0, lastPathElementPos) + "/" + unresolved._rawURI;
                
                return uri1.equals(uri2);
            }
        }
    }
    
    private static class ResolvedURI
    {
        String _resolvedURI;
        long _timestamp;
        
        public ResolvedURI(String resolvedURI, long timestamp)
        {
            _resolvedURI = resolvedURI;
            _timestamp = timestamp;
        }
    }
}
