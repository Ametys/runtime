package org.ametys.runtime.cocoon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.xslt.TraxErrorListener;
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

/**
 * Adaptation of Excalibur's XSLTProcessor implementation to allow for better error reporting. This implementation is also threadsafe.
 */
public class ThreadSafeTraxProcessor extends AbstractLogEnabled implements XSLTProcessor, Serviceable, Initializable, Disposable, Parameterizable, URIResolver
{
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
        _factory = _getTransformerFactory(_transformerFactory);
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
        final String id = stylesheet.getURI();
        TransformerHandlerAndValidity handlerAndValidity = null;

        TraxErrorListener errorListener = new TraxErrorListener(getLogger(), stylesheet.getURI());
        try
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Creating new Templates for " + id);
            }

            _factory.setErrorListener(errorListener);

            // Create a Templates ContentHandler to handle parsing of the
            // stylesheet.
            TemplatesHandler templatesHandler = _factory.newTemplatesHandler();

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

            // Initialize List for included validities
            SourceValidity validity = stylesheet.getValidity();

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

            // Create transformer handler
            final TransformerHandler handler = _factory.newTransformerHandler(template);
            handler.getTransformer().setErrorListener(new TraxErrorListener(getLogger(), stylesheet.getURI()));
            handler.getTransformer().setURIResolver(this);

            // Create result
            handlerAndValidity = new MyTransformerHandlerAndValidity(handler, validity);

            return handlerAndValidity;
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
    private SAXTransformerFactory _getTransformerFactory(String factoryName)
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
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("resolve(href = " + href + ", base = " + base + "); resolver = " + _resolver);
        }

        Source xslSource = null;
        try
        {
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

            InputSource is = _getInputSource(xslSource);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("xslSource = " + xslSource + ", system id = " + xslSource.getURI());
            }

            return new StreamSource(is.getByteStream(), is.getSystemId());
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
}
