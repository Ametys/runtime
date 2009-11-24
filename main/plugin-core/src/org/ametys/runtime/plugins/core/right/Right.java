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
package org.ametys.runtime.plugins.core.right;

import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nizableText;

/**
 * A right in a runtime application.<br>
 * A right can also be considered as a boolean permission : a given user has or does not have the right to do something.
 */
public class Right
{
    private String _id;
    private I18nizableText _label;
    private I18nizableText _description;
    private I18nizableText _category;
    private String _declaration;
    
    /**
     * Constructor.
     * @param id the unique Id of this right
     * @param label the i18n label of this Right
     * @param description the i18n description of the usage of this right
     * @param category the i18n cateogry of the usage of this right
     * @param catalogue the full catalogue identifier
     * @param declaration the declaration source (for debug purposes)
     */
    Right(String id, I18nizableText label, I18nizableText description, I18nizableText category, String declaration)
    {
        _id = id;
        _label = label;
        _description = description;
        _category = category;
        _declaration = declaration;
    }

    /**
     * Returns the unique Id of this Right
     * @return the unique Id of this Right
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Returns the i18n label of this Right
     * @return the i18n label of this Right
     */
    public I18nizableText getLabel()
    {
        return _label;
    }
    
    /**
     * Returns the i18n description of this Right
     * @return the i18n description of this Right
     */
    public I18nizableText getDescription()
    {
        return _description;
    }
    
    /**
     * Returns the i18n category of this Right
     * @return the i18n category of this Right
     */
    public I18nizableText getCategory()
    {
        return _category;
    }
    
    /**
     * Returns the declaration of this Right
     * @return the declaration
     */
    public String getDeclaration()
    {
        return _declaration;
    }
    
    /**
     * Represents this Right as SAX events
     * @param ch the ContentHandler to process SAX events
     * @throws SAXException if an error occurs
     */
    public void toSAX(ContentHandler ch) throws SAXException
    {
        AttributesImpl atts = new AttributesImpl();
        atts.addCDATAAttribute("id", _id);
        XMLUtils.startElement(ch, "right", atts);
        _label.toSAX(ch, "label");
        _description.toSAX(ch, "description");
        
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("id", _category.toString().replaceAll("[^a-zA-Z0-9]", "_"));
        XMLUtils.startElement(ch, "category", attrs);
        _category.toSAX(ch);
        XMLUtils.endElement(ch, "category");

        XMLUtils.endElement(ch, "right");
    }
    
    @Override
    public int hashCode()
    {
        return _id.hashCode();
    }
    
    @Override
    public boolean equals(Object object)
    {
        if (object == null || !(object instanceof Right))
        {
            return false;
        }
        
        return _id.equals(((Right) object).getId());
    }
}
