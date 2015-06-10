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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * Implementation of {@link Attr} for an AmetysObject's metadata.
 */
public class AmetysAttribute extends AbstractAmetysNode implements Attr
{
    private String _localName;
    private String _value;
    private String _qName;
    private String _namespace;
    private Element _ownerElement;
    
    /**
     * Constructor.
     * @param localName local name fir this attribute.
     * @param qName qualified name for this attribute.
     * @param namespaceURI namespace URI for this attribute.
     * @param value attribute's value.
     * @param ownerElement the owner {@link Element}.
     */
    public AmetysAttribute(String localName, String qName, String namespaceURI, String value, Element ownerElement)
    {
        _localName = localName;
        _value = value;
        _qName = qName;
        _namespace = namespaceURI;
        _ownerElement = ownerElement;
    }
    
    @Override
    public short getNodeType()
    {
        return Node.ATTRIBUTE_NODE;
    }

    @Override
    public String getName()
    {
        return _qName;
    }
    
    @Override
    public boolean getSpecified()
    {
        return true;
    }

    @Override
    public String getValue()
    {
        return _value;
    }
    
    @Override
    public String getNodeValue() throws DOMException
    {
        return getValue();
    }

    @Override
    public Element getOwnerElement()
    {
        return _ownerElement;
    }

    @Override
    public String getNodeName()
    {
        return getName();
    }

    @Override
    public String getTextContent() throws DOMException
    {
        return getValue();
    }
    
    @Override
    public String getLocalName()
    {
        return _localName;
    }
    
    @Override
    public String getNamespaceURI()
    {
        return _namespace;
    }

    @Override
    public void setValue(String value) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setValue");
    }

    @Override
    public TypeInfo getSchemaTypeInfo()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getSchemaTypeInfo");
    }

    @Override
    public boolean isId()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "isId");
    }
}
