/*
 *  Copyright 2011 Anyware Services
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
package org.ametys.runtime.util.dom;

import java.util.Collections;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * Implementation for empty element
 *
 */
public class EmptyAmetysElement extends AmetysNode implements Element
{
    private String _tagName;
    
    /**
     * Constructor
     * @param tagName the tag name
     */
    public EmptyAmetysElement(String tagName)
    {
        _tagName = tagName;
    }
    
    @Override
    public String getTagName()
    {
        return _tagName;
    }
    
    @Override
    public String getNodeName()
    {
        return getTagName();
    }
    
    @Override
    public String getLocalName()
    {
        return getTagName();
    }

    @Override
    public short getNodeType()
    {
        return Node.ELEMENT_NODE;
    }

    @Override
    public Node getParentNode()
    {
        return null;
    }
    
    @Override
    public NamedNodeMap getAttributes()
    {
        return new AmetysNamedNodeMap(Collections.EMPTY_MAP);
    }

    @Override
    public String getAttribute(String name)
    {
        return "";
    }

    @Override
    public Attr getAttributeNode(String name)
    {
        return null;
    }
    
    @Override
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        return getAttribute(localName);
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException
    {
        return getAttributeNode(localName);
    }
    
    @Override
    public boolean hasAttributes()
    {
        return false;
    }

    @Override
    public boolean hasAttribute(String name)
    {
        return false;
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        return hasAttribute(localName);
    }
    
    // Unsupported methods

    @Override
    public TypeInfo getSchemaTypeInfo()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getSchemaTypeInfo");
    }

    @Override
    public NodeList getElementsByTagName(String name)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getElementsByTagName");
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getElementsByTagNameNS");
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setAttribute");
    }

    @Override
    public void removeAttribute(String name) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "removeAttribute");
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setAttributeNode");
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "removeAttributeNode");
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setAttributeNS");
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "removeAttributeNS");
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setAttributeNodeNS");
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setIdAttribute");
    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setIdAttributeNS");
    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setIdAttributeNode");
    }

    @Override
    public String getTextContent() throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getTextContent");
    }

}
