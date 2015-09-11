package org.ametys.core.util.dom;

import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * Basic implementation of {@link Element}.
 */
public abstract class AbstractAmetysElement extends AbstractAmetysNode implements Element
{
    /** The parent Element or null if none. */
    protected Element _parent;
    
    private Map<String, AmetysAttribute> _attsMap;
    private String _tagName;
    
    /**
     * Constructor.
     */
    public AbstractAmetysElement()
    {
        // empty contructor
    }
    
    /**
     * Constructor.
     * @param tagName the tag name.
     */
    public AbstractAmetysElement(String tagName)
    {
        _tagName = tagName;
    }
    
    /**
     * Constructor.
     * @param parent the parent {@link Element}, if any.
     */
    public AbstractAmetysElement(Element parent)
    {
        _parent = parent;
    }
    
    /**
     * Constructor.
     * @param tagName the tag name.
     * @param parent the parent {@link Element}, if any.
     */
    public AbstractAmetysElement(String tagName, Element parent)
    {
        _tagName = tagName;
        _parent = parent;
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
        return _parent;
    }
    
    @Override
    public NamedNodeMap getAttributes()
    {
        if (_attsMap == null)
        {
            _attsMap = _lookupAttributes();
        }
        
        return new AmetysNamedNodeMap(_attsMap);
    }

    @Override
    public String getAttribute(String name)
    {
        if (_attsMap == null)
        {
            _attsMap = _lookupAttributes();
        }
        
        Attr attr = _attsMap.get(name);
        
        if (attr == null || attr.getValue() == null || attr.getValue().isEmpty())
        {
            return "";
        }
        
        return attr.getValue();
    }

    @Override
    public Attr getAttributeNode(String name)
    {
        if (_attsMap == null)
        {
            _attsMap = _lookupAttributes();
        }
        
        return _attsMap.get(name);
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
        if (_attsMap == null)
        {
            _attsMap = _lookupAttributes();
        }
        
        return !_attsMap.isEmpty();
    }

    @Override
    public boolean hasAttribute(String name)
    {
        if (_attsMap == null)
        {
            _attsMap = _lookupAttributes();
        }
        
        return _attsMap.containsKey(name);
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        return hasAttribute(localName);
    }
    
    @Override
    public String getTextContent() throws DOMException
    {
        StringBuilder sb = new StringBuilder();
        
        NodeList children = getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            sb.append(children.item(i).getTextContent());
        }
        
        return sb.toString();
    }
    
    /**
     * Returns a Map&lt;name, value&gt; corresponding to the attributes.<br>
     * @return the name/value pairs
     */
    protected abstract Map<String, AmetysAttribute> _lookupAttributes();
    
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
}
