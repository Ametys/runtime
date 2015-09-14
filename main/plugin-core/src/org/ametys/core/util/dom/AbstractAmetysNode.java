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

package org.ametys.core.util.dom;

import java.util.Collections;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * VERY partial implementation of a read-only, non-namespace aware {@link Node} on top of an objects hierarchy.<br>
 * It is NOT intended to be used as a full-featured DOM implementation, but it aims to provide a thin DOM layer over objects usable e.g. in XPath expressions and XSL stylesheets.<br>
 * There's no text nodes, nor entities, documents, ...
 */
public abstract class AbstractAmetysNode implements Node
{
    private NodeList _childNodes;
    
    @Override
    public String getNodeValue() throws DOMException
    {
        return null;
    }

    @Override
    public Node getParentNode()
    {
        return null;
    }

    @Override
    public NamedNodeMap getAttributes()
    {
        return null;
    }
    
    @Override
    public NodeList getChildNodes()
    {
        if (_childNodes == null)
        {
            _childNodes = _getChildNodes();
        }
        
        return _childNodes;
    }
    
    /**
     * Actual processing of child nodes.
     * Sublclasses should override this method and not getChildNodes().
     * @return a NodeList containing all children.
     */
    protected NodeList _getChildNodes()
    {
        return new AmetysNodeList(Collections.<Node>emptyList());
    }

    @Override
    public boolean hasChildNodes()
    {
        return getFirstChild() != null;
    }

    @Override
    public String getNamespaceURI()
    {
        return null;
    }

    @Override
    public String getPrefix()
    {
        return null;
    }
    
    @Override
    public String getLocalName()
    {
        return null;
    }

    @Override
    public boolean hasAttributes()
    {
        return false;
    }

    @Override
    public String getBaseURI()
    {
        return null;
    }

    @Override
    public Node getFirstChild()
    {
        return null;
    }
    
    @Override
    public Node getNextSibling()
    {
        Node parent = getParentNode();
        if (parent == null)
        {
            return null;
        }
        
        NodeList siblings = parent.getChildNodes();
        
        boolean isNext = false;
        Node nextSibling = null;
        int i = 0;
        
        while (nextSibling == null && i < siblings.getLength())
        {
            Node child = siblings.item(i++);
            
            if (isNext)
            {
                nextSibling = child;
            }
            else if (child == this)
            {
                isNext = true;
            }
        }
        
        return nextSibling == null ? null : nextSibling;
    }
    
    // Unsupported methods

    @Override
    public Node getLastChild()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getLastChild");
    }

    @Override
    public Node getPreviousSibling()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getPreviousSibling");
    }

    @Override
    public Document getOwnerDocument()
    {
        //throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getOwnerDocument");
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setNodeValue");
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "insertBefore");
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "replaceChild");
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "removeChild");
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "appendChild");
    }

    @Override
    public Node cloneNode(boolean deep)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "cloneNode");
    }

    @Override
    public void normalize()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "normalize");
    }

    @Override
    public boolean isSupported(String feature, String version)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "isSupported");
    }

    @Override
    public void setPrefix(String prefix) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setPrefix");
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "compareDocumentPosition");
    }

    @Override
    public void setTextContent(String textContent) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setTextContent");
    }

    @Override
    public boolean isSameNode(Node other)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "isSameNode");
    }

    @Override
    public String lookupPrefix(String namespaceURI)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "lookupPrefix");
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "isDefaultNamespace");
    }

    @Override
    public String lookupNamespaceURI(String prefix)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "lookupNamespaceURI");
    }

    @Override
    public boolean isEqualNode(Node arg)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "isEqualNode");
    }

    @Override
    public Object getFeature(String feature, String version)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getFeature");
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setUserData");
    }

    @Override
    public Object getUserData(String key)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getUserData");
    }
}
