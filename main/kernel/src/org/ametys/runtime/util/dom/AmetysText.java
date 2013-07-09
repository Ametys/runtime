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

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Partial implementation of a read-only, non-namespace aware {@link Text} on top of an objects hierarchy.<br>
 * It is NOT intended to be used as a full-featured DOM implementation, but it aims to provide a thin DOM layer over objects usable e.g. in XPath expressions and XSL stylesheets.<br>
 */
public class AmetysText extends AbstractAmetysNode implements Text
{
    private String _data;
    private AbstractAmetysElement _parent;
    
    /**
     * Constructor
     * @param data The data value
     * @param parent the parent {@link Element}.
     */
    public AmetysText (String data, AbstractAmetysElement parent)
    {
        super();
        _data = data;
        _parent = parent;
    }
    
    @Override
    public Node getParentNode()
    {
        return _parent;
    }
    
    @Override
    public String getData() throws DOMException
    {
        return getNodeValue();
    }

    @Override
    public int getLength()
    {
        return _data != null ? _data.length() : 0;
    }

    @Override
    public String substringData(int offset, int count) throws DOMException
    {
        if (_data == null)
        {
            return null;
        }
        
        if (offset + count >= _data.length()) 
        {
            return _data.substring(offset);
        }
        else
        {
            return _data.substring(offset, offset + count);
        }
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        return _data;
    }

    @Override
    public String getNodeName()
    {
        return "#text";
    }

    @Override
    public short getNodeType()
    {
        return Node.TEXT_NODE;
    }
    
    @Override
    public Node getNextSibling()
    {
        return null;
    }

    @Override
    public String getTextContent() throws DOMException
    {
        return getNodeValue();
    }

    @Override
    public void setData(String data) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setData");
    }
    
    @Override
    public boolean isElementContentWhitespace()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "isElementContentWhitespace");
    }

    @Override
    public String getWholeText()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "getWholeText");
    }

    @Override
    public void appendData(String arg) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "appendData");
    }

    @Override
    public void insertData(int offset, String arg) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "insertData");
    }

    @Override
    public void deleteData(int offset, int count) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "deleteData");
    }

    @Override
    public void replaceData(int offset, int count, String arg) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "replaceData");
    }
    
    @Override
    public Text splitText(int offset) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "splitText");
    }
    
    @Override
    public Text replaceWholeText(String content) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "replaceWholeText");
    }

    

}
