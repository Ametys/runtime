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
package org.ametys.runtime.util.cocoon;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.FileGenerator;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * This generator reads a text file that contains xml tags, and add xml
 * declaration and a firstlevel tag.<br>
 * The following file:<br>
 * I am a <i18n:text i18n:key="test"/>.<br>
 * Will be read as:<br>
 * <?xml version="1.0" encoding="UTF-8"><xml
 * xmlns:i18n="http://apache.org/cocoon/i18n/2.1">I am a <i18n:text
 * i18n:key="test"/>.</xml><br>
 * And so will sax events correctly.
 */
public class XMLizableTextGenerator extends FileGenerator implements Configurable
{
    private String _tagName;

    private String _encoding;

    private String _nameSpaces;

    /*
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _tagName = "xml";
        _encoding = "UTF-8";
        _nameSpaces = "";

        Configuration tagConfig = configuration.getChild("tag", false);
        if (tagConfig != null)
        {
            String tagName = tagConfig.getValue(null);
            if (tagName != null && tagName.length() != 0)
            {
                _tagName = tagName;
            }
        }

        Configuration encodingConfig = configuration.getChild("encoding", false);
        if (encodingConfig != null)
        {
            String encoding = encodingConfig.getValue(null);
            if (encoding != null && encoding.length() != 0)
            {
                _encoding = encoding;
            }
        }

        Configuration nsConfig = configuration.getChild("namespaces", false);
        if (nsConfig != null)
        {
            String ns = nsConfig.getValue(null);
            if (ns != null && ns.length() != 0)
            {
                _nameSpaces = ns;
            }
        }
    }

    @Override
    public void setup(SourceResolver res, Map model, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        super.setup(res, model, src, par);

        this.inputSource = new XMLizableTextSource(inputSource, _tagName, _encoding, _nameSpaces);
    }

    /**
     * This class wrap a source and is a source.<br>
     * The input stream of the source wrapped is filtered using
     * <code>XMLizableTextInputStream</code>. The source will add xml tag
     * declaration to the wrapped source.
     */
    protected class XMLizableTextSource implements Source
    {
        private Source _wrappedSource;

        private String _sourceTagName;

        private String _sourceEncoding;

        private String _sourceNameSpaces;

        /**
         * Create the SourceWrapper
         * 
         * @param src Source to wrap
         * @param tagName The name of the first level tag
         * @param encoding The encoding to put on the xml declaration
         * @param nameSpaces The namespaces declarations (such as:
         *            xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
         *            xmlns:xsl="..."). Will be copied as it in the first level
         *            tag.
         */
        public XMLizableTextSource(Source src, String tagName, String encoding, String nameSpaces)
        {
            _wrappedSource = src;
            _sourceTagName = tagName;
            _sourceEncoding = encoding;
            _sourceNameSpaces = nameSpaces;
        }

        public InputStream getInputStream() throws IOException
        {
            return new XMLizableTextInputStream(_wrappedSource.getInputStream(), _sourceTagName, _sourceEncoding,
                    _sourceNameSpaces);
        }

        public boolean exists()
        {
            return _wrappedSource.exists();
        }

        public long getContentLength()
        {
            return _wrappedSource.getContentLength();
        }

        public long getLastModified()
        {
            return _wrappedSource.getLastModified();
        }

        public String getMimeType()
        {
            return _wrappedSource.getMimeType();
        }

        public String getScheme()
        {
            return _wrappedSource.getScheme();
        }

        public String getURI()
        {
            return _wrappedSource.getURI();
        }

        public SourceValidity getValidity()
        {
            return _wrappedSource.getValidity();
        }

        public void refresh()
        {
            _wrappedSource.refresh();
        }
    }

    /**
     * This filter input stream wrap a stream and add it an xml prefix and an
     * xml postfix.<br>
     * Prefix and postfix are contructed using tagname, encoding and nameSpaces
     * given.<br>
     */
    protected class XMLizableTextInputStream extends FilterInputStream
    {
        /** The stream position is in prefix */
        protected static final int CURSOR_IN_PREFIX = -1;

        /** The stream position is in filtered stream */
        protected static final int CURSOR_IN_STREAM = 0;

        /** The stream position is in postfix */
        protected static final int CURSOR_IN_POSTFIX = +1;

        /** Content of the prefix to add at begining of the filtered input stream */
        protected String _prefix;

        /** Content of the post to add at ending of the filtered input stream */
        protected String _postfix;

        /**
         * Contains the current position. Legal values are
         * <code>CURSOR_IN_PREFIX</code>, <code>CURSOR_IN_STREAM</code> and
         * <code>CURSOR_IN_POSTFIX</code>
         */
        protected int _cursorGlobalPosition;

        // The position of cursor into prefix or postfix
        private int _cursorPosition;

        /**
         * Construct a filter input stream with prefix and post fix
         * 
         * @param is The input stream to wrap
         * @param tagName The xml first level tag name to add
         * @param encoding The xml encoding to put in xml declaration
         * @param nameSpaces The xml namespaces to put in the firstlevel tag
         */
        public XMLizableTextInputStream(InputStream is, String tagName, String encoding, String nameSpaces)
        {
            super(is);

            _prefix = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?><" + tagName + " " + nameSpaces + ">";
            _postfix = "</" + tagName + ">";

            _cursorGlobalPosition = CURSOR_IN_PREFIX;
            _cursorPosition = 0;
        }

        /*
         * @see java.io.FilterInputStream#available()
         */
        @Override
        public int available() throws IOException
        {
            int val;

            switch (_cursorGlobalPosition)
            {
                case CURSOR_IN_PREFIX:
                    val = super.available() + _prefix.length() - _cursorPosition + _postfix.length();
                    break;
                case CURSOR_IN_POSTFIX:
                    val = _postfix.length() - _cursorPosition;
                    break;
                default:
                    val = super.available() + _postfix.length();
                    break;
            }

            return val;
        }

        /*
         * @see java.io.FilterInputStream#read()
         */
        @Override
        public int read() throws IOException
        {
            int val;

            switch (_cursorGlobalPosition)
            {
                case CURSOR_IN_PREFIX:
                    if (_cursorPosition >= _prefix.length())
                    {
                        _cursorGlobalPosition = CURSOR_IN_STREAM;
                        val = read();
                    }
                    else
                    {
                        val = _prefix.getBytes()[_cursorPosition];
                        _cursorPosition++;
                    }
                    break;
                case CURSOR_IN_POSTFIX:
                    if (_cursorPosition >= _postfix.length())
                    {
                        val = -1;
                    }
                    else
                    {
                        val = _postfix.getBytes()[_cursorPosition];
                        _cursorPosition++;
                    }
                    break;
                default:
                    val = super.read();
                    if (val == -1)
                    {
                        _cursorGlobalPosition = CURSOR_IN_POSTFIX;
                        _cursorPosition = 0;
                        val = read();
                    }
                    break;
            }

            return val;
        }

        /*
         * @see java.io.FilterInputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            int val = 0;

            for (int i = off; i < len; i++)
            {
                int v = read();
                if (v == -1)
                {
                    if (val == 0)
                    {
                        val = -1;
                    }
                    break;
                }

                b[i] = (byte) v;
                val++;
            }

            return val;
        }

        /*
         * @see java.io.FilterInputStream#reset()
         */
        @Override
        public synchronized void reset() throws IOException
        {
            super.reset();

            _cursorGlobalPosition = CURSOR_IN_PREFIX;
            _cursorPosition = 0;
        }

        /*
         * @see java.io.FilterInputStream#skip(long)
         */
        @Override
        public long skip(long n) throws IOException
        {
            long val;

            switch (_cursorGlobalPosition)
            {
                case CURSOR_IN_PREFIX:
                    val = skipInPrefix(n);
                    break;
                case CURSOR_IN_POSTFIX:
                    val = skipInPostfix(n);
                    break;
                default:
                    val = skipInStream(n);
                    break;
            }

            return val;
        }

        private long skipInPrefix(long n) throws IOException
        {
            long val;

            // Dans le cas, où on est plus dans prefix
            if (_cursorPosition >= _prefix.length())
            {
                // Fait le skip dans le stream
                _cursorGlobalPosition = CURSOR_IN_STREAM;
                val = skip(n);
            }
            // Si on est toujours dans prefix
            else
            {
                // Réalise le skip
                _cursorPosition += n;
                // Vérifie le dépassement
                long notSkippedYet = _cursorPosition - _prefix.length();

                // Il y a du dépassement
                if (notSkippedYet > 0)
                {
                    // Réalise le dépassement dans le stream
                    val = n - notSkippedYet;
                    _cursorGlobalPosition = CURSOR_IN_STREAM;
                    val += skip(notSkippedYet);
                }
                // Pas de dépassement
                else
                {
                    val = n;
                }
            }

            return val;
        }

        private long skipInStream(long n) throws IOException
        {
            long val;

            // Réalise le skip
            val = super.skip(n);
            // Vérifie le dépassement
            long notSkippedYet = n - val;

            // Si le skip est incomplet
            if (notSkippedYet > 0)
            {
                // Réalise le dépassement dans le post
                _cursorGlobalPosition = CURSOR_IN_POSTFIX;
                _cursorPosition = 0;
                val += skip(notSkippedYet);
            }

            return val;
        }

        private long skipInPostfix(long n)
        {
            long val;

            // Dans le cas, où on est plus dans postfix
            if (_cursorPosition >= _postfix.length())
            {
                val = 0;
            }
            // Si on est toujours dans postfix
            else
            {
                // Réalise le skip
                _cursorPosition += n;
                // Vérifie le dépassement
                long notSkippedYet = _cursorPosition - _prefix.length();

                // Il y a du dépassement
                if (notSkippedYet > 0)
                {
                    val = n - notSkippedYet;
                }
                // Pas de dépassement
                else
                {
                    val = n;
                }
            }

            return val;
        }
    }
}
