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
package org.ametys.runtime.cocoon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
 * Finally, if <code>omit-xml-declaration</code> is set to <code>false</code>
 * (default), <code>Content-Type</code> meta tag will be dropped if present.<br>
 * @since 1.1.5 this serializer is JAXP compliant with the processing instruction
 *              <code>javax.xml.transform.*-output-escaping processing</code>.
 * @see Result
 */
public class XHTMLSerializer extends org.apache.cocoon.components.serializers.XHTMLSerializer
{   
    /** List of the tags to collapse. */
    private static final Set<String> __COLLAPSE_TAGS = new HashSet<String>(Arrays.asList(
        new String[] {"input", "img", "meta", "link", "hr", "br"}));

    /** List of the tags to collapse. */
    private static final Set<String> __NAMESPACES_ALLOWED = new HashSet<String>(Arrays.asList(
        new String[] {"", "http://www.w3.org/XML/1998/namespace", XHTML1_NAMESPACE, "http://www.w3.org/2000/svg",
                      "http://www.w3.org/1998/Math/MathML"}));
    
    /** Script tag. */
    private static final String __SCRIPT_TAG = "script";
    
    /** Style tag. */
    private static final String __STYLE_TAG = "style";

    /** Buffer to store script tag content. */
    private StringBuilder _buffer;

    /** Buffer to store script tag content. */
    private Set<String> _tagsToCollapse;
    
    /** Namespaces allowed. */
    private Set<String> _namespacesAllowed;
    
    /** Namespaces prefixe filtered. */
    private Set<String> _namespacesPrefixFiltered;
    
    /** Inside filtered tag: greater than 0 if we are inside a filtered tag. */
    private int _insideFilteredTag;

    /** Inline resource context: greater than 0 if we are inside a style or a script tag. */
    private int _insideInlineResourceTag;

    /**
     * Flag for disabling output escaping states encountered with
     * <code>javax.xml.transform.*-output-escaping</code> processing instructions.
     */
    private boolean _disableOutputEscaping;

    /** Meta http-equiv="Content-Type" context. True if we are inside a meta "content-type" tag.*/
    private boolean _isMetaContentType;

    /** Define whether to put XML declaration in the head of the document. */
    private boolean _omitXmlDeclaration;

    @Override
    public void configure(Configuration conf) throws ConfigurationException
    {
        super.configure(conf);
        
        String omitXmlDeclaration = conf.getChild("omit-xml-declaration").getValue(null);
        // Default to false (do not omit).
        this._omitXmlDeclaration = "yes".equals(omitXmlDeclaration);
        
        // Tags to collapse
        String tagsToCollapse = conf.getChild("tags-to-collapse").getValue(null);
        
        if (tagsToCollapse != null)
        {
            _tagsToCollapse = new HashSet<String>();
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
            _namespacesAllowed = new HashSet<String>();
            for (Configuration namespaceAllowed : namespacesAllowed)
            {
                String namespace = namespaceAllowed.getValue(null);
                
                if (namespace != null)
                {
                    _namespacesAllowed.add(namespace.trim());
                }
            }
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
        if (_namespacesAllowed.contains(nsuri))
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

        // Ignore the content-type meta tag if omit xml declaration is activated
        _isMetaContentType = isMetaContentType(local, attributes);
        if (!_isMetaContentType || _omitXmlDeclaration)
        {
            super.startElementImpl(uri, local, qual, lNamespaces, attributes);
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
        if (_namespacesAllowed.contains(nsuri))
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

        if (local.equalsIgnoreCase(__SCRIPT_TAG) || local.equalsIgnoreCase(__STYLE_TAG))
        {
            _insideInlineResourceTag--;
            if (_buffer.length() > 0)
            {
                char[] content = new char[_buffer.length()];
                _buffer.getChars(0, _buffer.length(), content, 0);
                _buffer.setLength(0);
                super.comment(content, 0, content.length);
            }
        }

        if (XHTML1_NAMESPACE.equals(namespaceUri))
        {
            // If the element is not in the list of the tags to collapse, close it without collapsing
            if (!__COLLAPSE_TAGS.contains(local))
            {
                this.closeElement(false);
            }
        }
        
        // Ignore the content-type meta tag if xml declaration is present
        if (!_isMetaContentType || _omitXmlDeclaration)
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
        if (local.equalsIgnoreCase("meta"))
        {
            for (String[] attr : attributes)
            {
                if (attr[ATTRIBUTE_LOCAL].equalsIgnoreCase("http-equiv") && attr[ATTRIBUTE_VALUE].equalsIgnoreCase("Content-Type"))
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
            _namespacesPrefixFiltered = new HashSet<String>();
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
