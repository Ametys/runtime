/*
 * Copyright 2012 Anyware Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ametys.runtime.plugins.core.right;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nUtils;
import org.ametys.runtime.util.I18nizableText;

/**
 * A right in a runtime application.<br>
 * A right can also be considered as a boolean permission : a given user has or
 * does not have the right to do something.
 */
public class Right
{
    private final String _id;

    private final I18nizableText _label;

    private final I18nizableText _description;

    private final I18nizableText _category;

    private final String _declaration;

    /**
     * Constructor.
     * 
     * @param id the unique Id of this right
     * @param label the i18n label of this Right
     * @param description the i18n description of the usage of this right
     * @param category the i18n cateogry of the usage of this right
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
     * 
     * @return the unique Id of this Right
     */
    public String getId()
    {
        return _id;
    }

    /**
     * Returns the i18n label of this Right
     * 
     * @return the i18n label of this Right
     */
    public I18nizableText getLabel()
    {
        return _label;
    }

    /**
     * Returns the i18n description of this Right
     * 
     * @return the i18n description of this Right
     */
    public I18nizableText getDescription()
    {
        return _description;
    }

    /**
     * Returns the i18n category of this Right
     * 
     * @return the i18n category of this Right
     */
    public I18nizableText getCategory()
    {
        return _category;
    }

    /**
     * Returns the declaration of this Right
     * 
     * @return the declaration
     */
    public String getDeclaration()
    {
        return _declaration;
    }

    /**
     * Represents this Right as SAX events
     * 
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

    /**
     * Represents this Right as JSON
     * @return the right in JSON format
     */
    public Map<String, Object> toJSON()
    {
        Map<String, Object> right = new HashMap<String, Object>();

        right.put("id", _id);
        right.put("label", I18nUtils.getInstance().translate(_label));
        right.put("description", I18nUtils.getInstance().translate(_description));

        // Category
        Map<String, Object> category = new HashMap<String, Object>();
        category.put("text", I18nUtils.getInstance().translate(_category));
        category.put("id", _category.toString().replaceAll("[^a-zA-Z0-9]", "_"));
        right.put("category", category);

        return right;
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
