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

import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A right in a runtime application.<br>
 * A right can also be considered as a boolean permission : a given user has or does not have the right to do something.
 */
public class Right
{
    private String _id;
    private String _label;
    private String _description;
    private String _category;
    private String _catalogue;
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
    Right(String id, String label, String description, String category, String catalogue, String declaration)
    {
        _id = id;
        _label = label;
        _description = description;
        _category = category;
        _catalogue = catalogue;
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
    public String getLabel()
    {
        return _label;
    }
    
    /**
     * Returns the i18n description of this Right
     * @return the i18n description of this Right
     */
    public String getDescription()
    {
        return _description;
    }
    
    /**
     * Returns the i18n category of this Right
     * @return the i18n category of this Right
     */
    public String getCategory()
    {
        return _category;
    }
    
    /**
     * Returns the i18n label of this Right
     * @return the i18n label of this Right
     */
    public String getCatalogue()
    {
        return _catalogue;
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
     * @throws SAXException 
     */
    public void toSAX(ContentHandler ch) throws SAXException
    {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "id", "id", "CDATA", _id);
        atts.addAttribute("", "catalogue", "catalogue", "CDATA", _catalogue);
        
        XMLUtils.startElement(ch, "right", atts);
        XMLUtils.createElement(ch, "label", _label);
        XMLUtils.createElement(ch, "description", _description);
        XMLUtils.createElement(ch, "category", _category);
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
