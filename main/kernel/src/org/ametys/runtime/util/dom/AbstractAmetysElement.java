package org.ametys.runtime.util.dom;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * DOM layer on top if an object hierarchy.
 * @param <T> the actual type of the wrapped object.
 */
public abstract class AbstractAmetysElement<T> extends AmetysNode implements Element
{
    /** The wrapper object. */
    protected T _object;
    /** The parent Element or null if none. */
    protected AbstractAmetysElement _parent;
    
    private Map<String, AmetysAttribute> _attsMap;
    
    /**
     * Constructor.
     * @param object the underlying object.
     */
    public AbstractAmetysElement(T object)
    {
        this(object, null);
    }

    /**
     * Constructor.
     * @param object the underlying object.
     * @param parent the parent {@link Element}.
     */
    public AbstractAmetysElement(T object, AbstractAmetysElement parent)
    {
        _object = object;
        _parent = parent;
    }
    
    /**
     * Returns the wrapped object.
     * @return the wrapped object.
     */
    public T getWrappedObject()
    {
        return _object;
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
        
        if (attr == null || StringUtils.isEmpty(attr.getValue()))
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

    @Override
    public String getTextContent() throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getTextContent");
    }
}
