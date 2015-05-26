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
package org.ametys.runtime.cocoon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Inherits from cocoon's serializers block XHTMLSerializer.<p>
 * The following configuration can be used:
 * <pre>
 * &lt;omit-xml-declaration&gt;yes|no&lt;/omit-xml-declaration&gt;
 * &lt;tags-to-collapse&gt;input,meta&lt;/tags-to-collapse&gt;
 * &lt;namespace-allowed&gt;&lt;/namespace-allowed&gt;
 * &lt;namespace-allowed&gt;http://www.w3.org/XML/1998/namespace&lt;/namespace-allowed&gt;
 * &lt;namespace-allowed&gt;http://www.w3.org/1999/xhtml&lt;/namespace-allowed&gt;
 * </pre>
 * Empty tags are not collapsed except the ones configured with
 * <code>tags-to-collapse</code>.<br>
 * If there is no such configuration, default tags to collaspe are:
 * <ul>
 *   <li>input</li>
 *   <li>img</li>
 *   <li>meta</li>
 *   <li>link</li>
 *   <li>hr</li>
 *   <li>br</li>
 * </ul>
 * Namespace tags and attributes are filtered to product valid XHTML.
 * This is configureable using <code>namespace-allowed</code>, by default
 * the only following namespaces are allowed:
 * <ul>
 *   <li>"" (empty namespace)</li>
 *   <li>"http://www.w3.org/XML/1998/namespace"</li>
 *   <li>"http://www.w3.org/1999/xhtml"</li>
 *   <li>"http://www.w3.org/2000/svg"</li>
 *   <li>"http://www.w3.org/1998/Math/MathML"</li>
 * </ul>
 * Content of <code>script</code> tags will be exported in a single comment.<p>
 * <code>omit-xml-declaration</code> is set to <code>yes</code> by default for
 * compatibility purpose (IE 6).
 * If <code>omit-xml-declaration</code> is set to <code>no</code>,
 * <code>Content-Type</code> meta tag will be dropped if present.<br>
 * @since 1.1.5 this serializer is JAXP compliant with the processing instruction
 *              <code>javax.xml.transform.*-output-escaping processing</code>.
 * @see Result
 */
public class XHTMLSerializer extends org.apache.cocoon.components.serializers.XHTMLSerializer implements LogEnabled, Serviceable
{   
    /** List of the tags to collapse. */
    private static final Set<String> __NAMESPACES_ALLOWED = new HashSet<>(Arrays.asList(
            new String[] {"", "http://www.w3.org/XML/1998/namespace", XHTML1_NAMESPACE, "http://www.w3.org/2000/svg",
                "http://www.w3.org/1998/Math/MathML"}));

    /** List of the tags to collapse. */
    private static final Set<String> __COLLAPSE_TAGS = new HashSet<>(Arrays.asList(
            new String[] {"input", "img", "meta", "link", "hr", "br"}));

    /** Head tag. */
    private static final String __HEAD_TAG = "head";
    /** Meta tag. */
    private static final String __META_TAG = "meta";
    /** Meta HTTP equiv attribute name. */
    private static final String __META_HTTP_EQUIV_ATTR = "http-equiv";
    /** Meta HTTP equiv attribute value for content type. */
    private static final String __META_HTTP_EQUIV_CTYPE_VALUE = "Content-Type";
    /** Meta content attribute name. */
    private static final String __META_CONTENT_ATTR = "content";
    /** Script tag. */
    private static final String __SCRIPT_TAG = "script";
    /** Style tag. */
    private static final String __STYLE_TAG = "style";

    /** The XHTMLSerializerExtensionPoint instance */
    protected XHTMLSerializerExtensionPoint _xhtmlSerializerExtensionPoint;

    /** Buffer to store script tag content. */
    private StringBuilder _buffer;

    /** Buffer to store tag to collapse. */
    private Set<String> _tagsToCollapse;

    /** Namespaces allowed. */
    private Set<String> _namespacesAllowed;

    /** Namespaces prefixe filtered. */
    private Set<String> _namespacesPrefixFiltered;

    /** Inside filtered tag: greater than 0 if we are inside a filtered tag. */
    private int _insideFilteredTag;

    /** Inline resource context: greater than 0 if we are inside a style or a script tag. */
    private int _insideInlineResourceTag;
    private int _tagsInsideInlineResourceTag;

    /**
     * Flag for disabling output escaping states encountered with
     * <code>javax.xml.transform.*-output-escaping</code> processing instructions.
     */
    private boolean _disableOutputEscaping;

    /** Define whether to put XML declaration in the head of the document. */
    private boolean _omitXmlDeclaration;

    /** Meta http-equiv="Content-Type" context. True if we are inside a meta "content-type" tag.*/
    private boolean _isMetaContentType;

    private Logger _logger;

    @Override
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        if (manager.hasService(XHTMLSerializerExtensionPoint.ROLE))
        {
            _xhtmlSerializerExtensionPoint = (XHTMLSerializerExtensionPoint) manager.lookup(XHTMLSerializerExtensionPoint.ROLE);
        }
    }

    @Override
    public void configure(Configuration conf) throws ConfigurationException
    {
        super.configure(conf);

        String omitXmlDeclaration = conf.getChild("omit-xml-declaration").getValue(null);
        // Default to yes (omit).
        this._omitXmlDeclaration = !"no".equals(omitXmlDeclaration);

        // Tags to collapse
        String tagsToCollapse = conf.getChild("tags-to-collapse").getValue(null);

        if (tagsToCollapse != null)
        {
            _tagsToCollapse = new HashSet<>();
            for (String tag : tagsToCollapse.split(","))
            {
                _tagsToCollapse.add(tag.trim());
            }
        }
        else
        {
            _tagsToCollapse = __COLLAPSE_TAGS;
        }

        // Namespaces allowed
        Configuration[] namespacesAllowed = conf.getChildren("namespace-allowed");

        if (namespacesAllowed.length > 0)
        {
            _namespacesAllowed = new HashSet<>();
            for (Configuration namespaceAllowed : namespacesAllowed)
            {
                String namespace = namespaceAllowed.getValue("");
                _namespacesAllowed.add(namespace.trim());
            }
        }
        else if (_xhtmlSerializerExtensionPoint != null)
        {
            _namespacesAllowed = _xhtmlSerializerExtensionPoint.getAllowedNamespaces();
        }
        else
        {
            _namespacesAllowed = __NAMESPACES_ALLOWED;
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        if (_namespacesAllowed.contains(uri))
        {
            super.startPrefixMapping(prefix, uri);
        }
        else
        {
            _namespacesPrefixFiltered.add(prefix);
        }
    }

    @Override
    public void startElement(String nsuri, String local, String qual, Attributes attributes) throws SAXException
    {
        if  (_insideInlineResourceTag > 0)
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("Tags are forbidden inside a <script> or <style> tag : <" + local + ">");
            }

            _tagsInsideInlineResourceTag++;
        }
        else if (_namespacesAllowed.contains(nsuri))
        {
            if (_insideFilteredTag == 0)
            {
                super.startElement(nsuri, local, qual, _filterAttributes(attributes));
            }
        }
        else
        {
            // Ignore nested content as the namespace is filtered
            _insideFilteredTag++;
        }
    }

    private Attributes _filterAttributes(Attributes attributes)
    {
        AttributesImpl attributesFiltered = new AttributesImpl();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String uri = attributes.getURI(i);

            // Filter attribute with not allowed namespace
            if (_namespacesAllowed.contains(uri))
            {
                String qName = attributes.getQName(i);

                // Filter attribute xmlns and xmlns:xxx
                if (!qName.equals("xmlns") && !qName.startsWith("xmlns:"))
                {
                    attributesFiltered.addAttribute(uri, attributes.getLocalName(i), qName,
                            attributes.getType(i), attributes.getValue(i));
                }
            }
        }

        return attributesFiltered;
    }

    @Override
    public void startElementImpl(String uri, String local, String qual, String[][] lNamespaces, String[][] attributes) throws SAXException
    {
        if (local.equalsIgnoreCase(__SCRIPT_TAG) || local.equalsIgnoreCase(__STYLE_TAG))
        {
            _insideInlineResourceTag++;
        }

        _isMetaContentType = isMetaContentType(local, attributes);

        // Always ignore the content-type meta tag because we do not want
        // it in non omit mode and because we create it in omit mode (see below)
        if (!_isMetaContentType)
        {
            super.startElementImpl(uri, local, qual, lNamespaces, attributes);
        }

        // Create our own content-type meta tag in omit mode
        if (_omitXmlDeclaration && local.equalsIgnoreCase(__HEAD_TAG))
        {
            // Create our own meta content type element as Xalan creates one but
            // places it in the last children (after an potential title)
            String qua = namespaces.qualify(XHTML1_NAMESPACE, __META_TAG, __META_TAG);
            String[][] attrs = new String[2][ATTRIBUTE_LENGTH];

            attrs[0][ATTRIBUTE_NSURI] = "";
            attrs[0][ATTRIBUTE_LOCAL] = __META_HTTP_EQUIV_ATTR;
            attrs[0][ATTRIBUTE_QNAME] = __META_HTTP_EQUIV_ATTR;
            attrs[0][ATTRIBUTE_VALUE] = __META_HTTP_EQUIV_CTYPE_VALUE;
            attrs[1][ATTRIBUTE_NSURI] = "";
            attrs[1][ATTRIBUTE_LOCAL] = __META_CONTENT_ATTR;
            attrs[1][ATTRIBUTE_QNAME] = __META_CONTENT_ATTR;
            attrs[1][ATTRIBUTE_VALUE] = this.getMimeType();

            super.startElementImpl(XHTML1_NAMESPACE, __META_TAG, qua, new String[0][0], attrs);
            super.endElementImpl(XHTML1_NAMESPACE, __META_TAG, qua);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (_insideFilteredTag == 0)
        {
            if (_disableOutputEscaping)
            {
                // Close current element if necessary
                closeElement(false);
                // Let content pass through unchanged
                write(ch, start, length);
            }
            else
            {
                super.characters(ch, start, length);
            }
        }
    }

    @Override
    public void charactersImpl(char[] data, int start, int length) throws SAXException
    {
        if (_insideInlineResourceTag > 0)
        {
            _buffer.append(data, start, length);
        }
        else
        {
            super.charactersImpl(data, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] data, int start, int length) throws SAXException
    {
        if (_insideFilteredTag == 0)
        {
            super.ignorableWhitespace(data, start, length);
        }
    }

    @Override
    public void comment(char[] data, int start, int length) throws SAXException
    {
        if (_insideFilteredTag == 0)
        {
            if (_insideInlineResourceTag > 0)
            {
                _buffer.append(data, start, length);
            }
            else
            {
                super.comment(data, start, length);
            }
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        if (_insideFilteredTag == 0)
        {
            if (Result.PI_DISABLE_OUTPUT_ESCAPING.equals(target))
            {
                // Start unescaping
                _disableOutputEscaping = true;
            }
            else if (Result.PI_ENABLE_OUTPUT_ESCAPING.equals(target))
            {
                // Stop unescapping
                _disableOutputEscaping = false;
            }
            else
            {
                super.processingInstruction(target, data);
            }
        }
    }

    @Override
    public void endElement(String nsuri, String local, String qual) throws SAXException
    {
        if (_tagsInsideInlineResourceTag > 0)
        {
            _tagsInsideInlineResourceTag--;
        }
        else if (_namespacesAllowed.contains(nsuri))
        {
            if (_insideFilteredTag == 0)
            {
                super.endElement(nsuri, local, qual);
            }
        }
        else
        {
            // Finish to ignore parsed nested content as the namespace is filtered
            _insideFilteredTag--;
        }
    }

    @Override
    public void endElementImpl(String uri, String local, String qual) throws SAXException
    {
        String namespaceUri = uri;
        if (uri.length() == 0)
        {
            namespaceUri = XHTML1_NAMESPACE;
        }

        if (local.equalsIgnoreCase(__SCRIPT_TAG))
        {
            _insideInlineResourceTag--;
            if (_buffer.length() > 0)
            {
                char[] content = new char[_buffer.length() + 5];
                content[0] = '\n';
                content[content.length - 4] = '\n';
                content[content.length - 3] = '/';
                content[content.length - 2] = '/';
                content[content.length - 1] = ' ';
                _buffer.getChars(0, _buffer.length(), content, 1);
                _buffer.setLength(0);
                super.comment(content, 0, content.length);
            }
        }
        else if (local.equalsIgnoreCase(__STYLE_TAG))
        {
            _insideInlineResourceTag--;
            if (_buffer.length() > 0)
            {
                char[] content = new char[_buffer.length() + 2];
                content[0] = '\n';
                content[content.length - 1] = '\n';
                _buffer.getChars(0, _buffer.length(), content, 1);
                _buffer.setLength(0);
                super.comment(content, 0, content.length);
            }
        }

        if (XHTML1_NAMESPACE.equals(namespaceUri))
        {
            // If the element is not in the list of the tags to collapse, close it without collapsing
            if (!_tagsToCollapse.contains(local))
            {
                this.closeElement(false);
            }
        }

        // Ignore the content-type meta tag, see startElementImpl
        if (!_isMetaContentType)
        {
            super.endElementImpl(namespaceUri, local, qual);
        }
        else
        {
            _isMetaContentType = false;
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
        if (!_namespacesPrefixFiltered.contains(prefix))
        {
            super.endPrefixMapping(prefix);
        }
    }

    private boolean isMetaContentType(String local, String[][] attributes)
    {
        if (local.equalsIgnoreCase(__META_TAG))
        {
            for (String[] attr : attributes)
            {
                if (attr[ATTRIBUTE_LOCAL].equalsIgnoreCase(__META_HTTP_EQUIV_ATTR)
                        && attr[ATTRIBUTE_VALUE].equalsIgnoreCase(__META_HTTP_EQUIV_CTYPE_VALUE))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void recycle()
    {
        super.recycle();

        if (_buffer == null)
        {
            _buffer = new StringBuilder(512);
        }
        else
        {
            if (_buffer.capacity() >  100 * 1024)
            {
                // Garbage collect previous buffer is it exceed 100 Kb
                _buffer = new StringBuilder(512);
            }
            else
            {
                // Clear buffer but keep capacity
                _buffer.setLength(0);
            }
        }

        // Clear parsing state aware attributes
        if (_namespacesPrefixFiltered == null)
        {
            _namespacesPrefixFiltered = new HashSet<>();
        }
        else
        {
            _namespacesPrefixFiltered.clear();
        }
        _insideFilteredTag = 0;
        _insideInlineResourceTag = 0;
        _disableOutputEscaping = false;
        _isMetaContentType = false;
    }
}
