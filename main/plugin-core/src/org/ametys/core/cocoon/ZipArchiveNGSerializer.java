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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.serialization.AbstractSerializer;
import org.apache.cocoon.serialization.Serializer;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * ZIP archive serializer that makes use of apache commons compress ZipArchiveOutputStream instead of JavaSE's ZipOutputStream.
 * It's based on cocoon's ZipArchiveSerializer.
 */
public class ZipArchiveNGSerializer extends AbstractSerializer implements Disposable, Serviceable
{
    /**
     * The namespace for elements handled by this serializer,
     * "http://apache.org/cocoon/zip-archive/1.0".
     */
    public static final String ZIP_NAMESPACE = "http://apache.org/cocoon/zip-archive/1.0";

    private static final int START_STATE = 0;

    private static final int IN_ZIP_STATE = 1;

    private static final int IN_CONTENT_STATE = 2;

    /** The component manager */
    protected ServiceManager _manager;

    /** The serializer component selector */
    protected ServiceSelector _selector;

    /** The Zip stream where entries will be written */
    protected ZipArchiveOutputStream _zipOutput;

    /** The current state */
    protected int _state = START_STATE;

    /** The resolver to get sources */
    protected SourceResolver _resolver;

    /** Temporary byte buffer to read source data */
    protected byte[] _buffer;

    /** Serializer used when in IN_CONTENT state */
    protected Serializer _serializer;

    /** Current depth of the serialized content */
    protected int _contentDepth;

    /** Used to collect namespaces */
    private NamespaceSupport _nsSupport = new NamespaceSupport();

    /**
     * Store exception
     */
    private SAXException _exception;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        this._manager = manager;
        this._resolver = (SourceResolver) this._manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Returns default mime type for zip archives, <code>application/zip</code>. Can be overridden
     * in the sitemap.
     * 
     * @return application/zip
     */
    @Override
    public String getMimeType()
    {
        return "application/zip";
    }
    
    @Override
    public void startDocument() throws SAXException
    {
        this._state = START_STATE;
        this._zipOutput = new ZipArchiveOutputStream(this.output);
        this._zipOutput.setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy.ALWAYS);
        this._zipOutput.setEncoding("UTF-8");
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     * 
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        if (_state == IN_CONTENT_STATE && this._contentDepth > 0)
        {
            // Pass to the serializer
            super.startPrefixMapping(prefix, uri);

        }
        else
        {
            // Register it if it's not our own namespace (useless to content)
            if (!uri.equals(ZIP_NAMESPACE))
            {
                this._nsSupport.declarePrefix(prefix, uri);
            }
        }
    }
    
    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
        if (_state == IN_CONTENT_STATE && this._contentDepth > 0)
        {
            // Pass to the serializer
            super.endPrefixMapping(prefix);
        }
    }

    // Note : no need to implement endPrefixMapping() as we just need to pass it through if there
    // is a serializer, which is what the superclass does.

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {

        // Damage control. Sometimes one exception is just not enough...
        if (this._exception != null)
        {
            throw this._exception;
        }

        switch (_state)
        {
            case START_STATE:
                // expecting "zip" as the first element
                if (namespaceURI.equals(ZIP_NAMESPACE) && localName.equals("archive"))
                {
                    this._nsSupport.pushContext();
                    this._state = IN_ZIP_STATE;
                }
                else
                {
                    this._exception = new SAXException("Expecting 'archive' root element (got '" + localName + "')");
                    throw this._exception;
                }
                break;

            case IN_ZIP_STATE:
                // expecting "entry" element
                if (namespaceURI.equals(ZIP_NAMESPACE) && localName.equals("entry"))
                {
                    this._nsSupport.pushContext();
                    // Get the source
                    addEntry(atts);
                }
                else
                {
                    this._exception = new SAXException("Expecting 'entry' element (got '" + localName + "')");
                    throw this._exception;
                }
                break;

            case IN_CONTENT_STATE:
                if (this._contentDepth == 0)
                {
                    // Give it any namespaces already declared
                    Enumeration prefixes = this._nsSupport.getPrefixes();
                    while (prefixes.hasMoreElements())
                    {
                        String prefix = (String) prefixes.nextElement();
                        super.startPrefixMapping(prefix, this._nsSupport.getURI(prefix));
                    }
                }

                this._contentDepth++;
                super.startElement(namespaceURI, localName, qName, atts);
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] buffer, int offset, int length) throws SAXException
    {
        // Propagate text to the serializer only if we have encountered the content's top-level
        // element. Otherwhise, the serializer may be confused by some characters occuring between
        // startDocument() and the first startElement() (e.g. Batik fails hard in that case)
        if (this._state == IN_CONTENT_STATE && this._contentDepth > 0)
        {
            super.characters(buffer, offset, length);
        }
    }

    /**
     * Add an entry in the archive.
     * 
     * @param atts the attributes that describe the entry
     * @throws SAXException 
     */
    protected void addEntry(Attributes atts) throws SAXException
    {
        String name = atts.getValue("name");
        if (name == null)
        {
            this._exception = new SAXException("No name given to the Zip entry");
            throw this._exception;
        }

        String src = atts.getValue("src");
        String serializerType = atts.getValue("serializer");

        if (src == null && serializerType == null)
        {
            this._exception = new SAXException("No source nor serializer given for the Zip entry '" + name + "'");
            throw this._exception;
        }

        if (src != null && serializerType != null)
        {
            this._exception = new SAXException("Cannot specify both 'src' and 'serializer' on a Zip entry '" + name + "'");
            throw this._exception;
        }

        Source source = null;
        try
        {
            if (src != null)
            {
                // Get the source and its data
                source = _resolver.resolveURI(src);
                
                try (InputStream sourceInput = source.getInputStream())
                {
                    // Create a new Zip entry with file modification time.
                    ZipArchiveEntry entry = new ZipArchiveEntry(name);
                    long lastModified = source.getLastModified();
                    if (lastModified != 0)
                    {
                        entry.setTime(lastModified);
                    }
                    // this.zipOutput.putNextEntry(entry);
                    this._zipOutput.putArchiveEntry(entry);

                    // Buffer lazily allocated
                    if (this._buffer == null)
                    {
                        this._buffer = new byte[8192];
                    }

                    // Copy the source to the zip
                    int len;
                    while ((len = sourceInput.read(this._buffer)) > 0)
                    {
                        this._zipOutput.write(this._buffer, 0, len);
                    }

                    // and close the entry
                    // this.zipOutput.closeEntry();
                    this._zipOutput.closeArchiveEntry();
                }
            }
            else
            {
                // Create a new Zip entry with current time.
                // ZipEntry entry = new ZipEntry(name);
                ZipArchiveEntry entry = new ZipArchiveEntry(name);
                // this.zipOutput.putNextEntry(entry);
                this._zipOutput.putArchiveEntry(entry);

                // Serialize content
                if (this._selector == null)
                {
                    this._selector = (ServiceSelector) this._manager.lookup(Serializer.ROLE + "Selector");
                }

                // Get the serializer
                this._serializer = (Serializer) this._selector.select(serializerType);

                // Direct its output to the zip file, filtering calls to close()
                // (we don't want the archive to be closed by the serializer)
                this._serializer.setOutputStream(new FilterOutputStream(this._zipOutput)
                {
                    @Override
                    public void close()
                    {
                        // nothing
                    }
                });

                // Set it as the current XMLConsumer
                setConsumer(_serializer);

                // start its document
                this._serializer.startDocument();

                this._state = IN_CONTENT_STATE;
                this._contentDepth = 0;
            }

        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (SAXException se)
        {
            this._exception = se;
            throw this._exception;
        }
        catch (Exception e)
        {
            this._exception = new SAXException(e);
            throw this._exception;
        }
        finally
        {
            this._resolver.release(source);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {

        // Damage control. Sometimes one exception is just not enough...
        if (this._exception != null)
        {
            throw this._exception;
        }

        if (_state == IN_CONTENT_STATE)
        {
            super.endElement(namespaceURI, localName, qName);
            this._contentDepth--;

            if (this._contentDepth == 0)
            {
                // End of this entry

                // close all declared namespaces.
                Enumeration prefixes = this._nsSupport.getPrefixes();
                while (prefixes.hasMoreElements())
                {
                    String prefix = (String) prefixes.nextElement();
                    super.endPrefixMapping(prefix);
                }

                super.endDocument();

                try
                {
                    // this.zipOutput.closeEntry();
                    this._zipOutput.closeArchiveEntry();
                }
                catch (IOException ioe)
                {
                    this._exception = new SAXException(ioe);
                    throw this._exception;
                }

                super.setConsumer(null);
                this._selector.release(this._serializer);
                this._serializer = null;

                // Go back to listening for entries
                this._state = IN_ZIP_STATE;
            }
        }
        else
        {
            this._nsSupport.popContext();
        }
    }

    @Override
    public void endDocument() throws SAXException
    {
        try
        {
            // Close the zip archive
            this._zipOutput.finish();

        }
        catch (IOException ioe)
        {
            throw new SAXException(ioe);
        }
    }

    @Override
    public void recycle()
    {
        this._exception = null;
        if (this._serializer != null)
        {
            this._selector.release(this._serializer);
        }
        if (this._selector != null)
        {
            this._manager.release(this._selector);
        }

        this._nsSupport.reset();
        super.recycle();
    }

    @Override
    public void dispose()
    {
        if (this._manager != null)
        {
            this._manager.release(this._resolver);
            this._resolver = null;
            this._manager = null;
        }
    }

}
