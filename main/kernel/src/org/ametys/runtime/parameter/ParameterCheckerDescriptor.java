/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.runtime.parameter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.util.I18nizableText;

/**
 *  Default implementation for the parameter checker 
 */
public class ParameterCheckerDescriptor extends AbstractLogEnabled
{
    /** The parameter checker's id */
    protected String _id;
    
    /** The parameters checker's description */
    protected I18nizableText _description;
    
    /** The parameter checker's label */
    protected I18nizableText _label;
    
    /** The path of the small icon*/
    protected String _smallIconPath;
    
    /** The path of the medium icon*/
    protected String _mediumIconPath;
    
    /** The path of the large icon*/
    protected String _largeIconPath;
    
    /** The concrete class of the parameter checker */
    protected String _concreteClass;
    
    /** The order of the ui parameter */
    protected int _uiRefOrder;
    
    /** The id of the ui parameter */
    protected String _uiRefParamId;
    
    /** The group of the checked parameter */
    protected I18nizableText _uiRefGroup;
    
    /** The category of the checked parameter */
    protected I18nizableText _uiRefCategory;
    
    /** The configuration of the linked parameters */
    protected Set<String> _linkedParamsIds;
    
    /** The Map representation of the parameter checker */
    protected ParameterChecker _parameterChecker;

    /** The Json factory */
    private JsonFactory _jsonFactory = new JsonFactory();
    
    /** The object mapper */
    private ObjectMapper _objectMapper = new ObjectMapper();
    
   /**
    * Returns a json representation of the ParameterCheckerDescriptor
    * @return the json representation
    */
    public String toJsonOLD() // FIXME to remove
    {
        StringWriter writer = new StringWriter();

        Map<String, Object> result = new HashMap<>();
        List<String> linkedParamsList = new ArrayList<>();

        Iterator<String> it = _linkedParamsIds.iterator();
        while (it.hasNext())
        {
            linkedParamsList.add(it.next());
        }

        if (_uiRefParamId != null)
        {
            result.put("param-ref", _uiRefParamId);
        }

        result.put("linked-params", linkedParamsList);
        result.put("small-icon-path", _smallIconPath);
        result.put("medium-icon-path", _mediumIconPath);
        result.put("large-icon-path", _largeIconPath);
        result.put("id", _id);
        result.put("class", _concreteClass);

        try
        {
            JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(writer);
            _objectMapper.writeValue(jsonGenerator, result);

            return writer.toString();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The object can not be converted to json string", e);
        }
    }
    
    /**
     * SAX the description informations
     * @param handler The handler where to sax
     * @throws SAXException if an error occurred
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        XMLUtils.startElement(handler, "param-checker");

        XMLUtils.createElement(handler, "id", getId());
        XMLUtils.createElement(handler, "small-icon-path", getSmallIconPath());
        XMLUtils.createElement(handler, "medium-icon-path", getMediumIconPath());
        XMLUtils.createElement(handler, "large-icon-path", getLargeIconPath());
        
        if (getLinkedParamsIds() != null)
        {
            String linkedParamsAsJSON = "[" + StringUtils.join(getLinkedParamsIds().parallelStream().map(s -> "\"" + s + "\"").collect(Collectors.toList()), ", ") + "]"; 
            XMLUtils.createElement(handler, "linked-params", linkedParamsAsJSON);
        }
        
        if (getUiRefParamId() != null)
        {
            XMLUtils.createElement(handler, "param-ref", getUiRefParamId());
        }
        if (getUiRefCategory() != null)
        {
            getUiRefCategory().toSAX(handler, "category");
        }
        if (getUiRefGroup() != null)
        {
            getUiRefGroup().toSAX(handler, "group");
        }
        getLabel().toSAX(handler, "label");
        getDescription().toSAX(handler, "description");
        XMLUtils.createElement(handler, "order", Integer.toString(getUiRefOrder()));
        
        XMLUtils.endElement(handler, "param-checker");
    }
    
    /**
     * Retrieves the parameter checker's id
     * @return _id the id of the parameter checker 
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Sets the parameter checker's id
     * @param id the id of the parameter checker
     */
    public void setId(String id)
    {
        _id = id;
    }
    
    /**
     * Retrieves the parameter's checker label.
     * @return _label the label of the parameter checker
     */
    public I18nizableText getLabel()
    {
        return _label;
    }

    /**
     * Sets the parameter's checker label.
     * @param label the label of the parameter checker
     */
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }
    
    /**
     * Retrieves the parameter's checker description.
     * @return _description the description of the parameter checker
     */
    public I18nizableText getDescription()
    {
        return _description;
    }
    
    /**
     * Sets the parameter's checker description.
     * @param description the description of the parameter checker
     */
    public void setDescription(I18nizableText description)
    {
        _description = description;
    }
    
    /** 
     * Retrieves the parameter checker's icon 
     * @return _iconPath the path to the icon representing the parameter checker
     */
    public String getSmallIconPath()
    {
        return _smallIconPath;
    }
    
    /**
     * Sets the icon path of the parameter checker
     * @param path the path of the small icon
     */
    public void setSmallIconPath(String path)
    {
        _smallIconPath = path;
    }
    
    /** 
     * Retrieves the parameter checker's icon 
     * @return _iconPath the path to the icon representing the parameter checker
     */
    public String getMediumIconPath()
    {
        return _mediumIconPath;
    }
    
    /**
     * Sets the icon path of the parameter checker
     * @param path the path of the medium icon
     */
    public void setMediumIconPath(String path)
    {
        _mediumIconPath = path;
    }
    
    /** 
     * Retrieves the parameter checker's icon 
     * @return _iconPath the path to the icon representing the parameter checker
     */
    public String getLargeIconPath()
    {
        return _largeIconPath;
    }
    
    /**
     * Sets the icon path of the parameter checker
     * @param path the path of the large icon
     */
    public void setLargeIconPath(String path)
    {
        _largeIconPath = path;
    }
  
    /**
     * Retrieves the class of the parameter checker
     * @return _concreteClass the class of the parameter checker.
     */
    public String getConcreteClass()
    {
        return _concreteClass;
    }

    /**
     * Sets the class of the parameter checker
     * @param concreteClass the class of the parameter checker
     */
    public void setClass(String concreteClass)
    {
        this._concreteClass = concreteClass;
    }

    /**
     * Gets the ui order of the parameter checker
     * @return _uiRefOrder the ui order
     */
    public int getUiRefOrder()
    {
        return _uiRefOrder;
    }
    
    /**
     * Sets the ui order
     * @param uiRefOrder the ui order
     */
    public void setUiRefOrder(int uiRefOrder)
    {
        _uiRefOrder = uiRefOrder;
    }
    
    /**
     * Retrieves the configuration of the parameter the parameter checker is attached to
     * @return _uiRefParamId the configuration of the parameter
     */
    public String getUiRefParamId()
    {
        return _uiRefParamId;
    }
    
    /**
     * Sets the configuration of the parameter the parameter checker is attached to
     * @param uiRefParamId the configuration of the parameter
     */
    public void setUiRefParamId(String uiRefParamId)
    {
        _uiRefParamId = uiRefParamId;
    }
    
    /**
     * Retrieves the text of the configuration group the parameter checker is attached to
     * @return _uiRefGroup the text of the configuration group
     */
    public I18nizableText getUiRefGroup()
    {
        return _uiRefGroup;
    }
    
    /**
     * Sets the text of the configuration group the parameter checker is attached to
     * @param uiRefGroup the text of the configuration group
     */
    public void setUiRefGroup(I18nizableText uiRefGroup)
    {
        _uiRefGroup = uiRefGroup;
    }
    
    /**
     * Retrieves the text of the configuration category the parameter checker is attached to
     * @return _uiRefCategory the text of the configuration group
     */
    public I18nizableText getUiRefCategory()
    {
        return _uiRefCategory;
    }
    
    /**
     * Sets the text of the configuration category the parameter checker is attached to
     * @param uiRefCategory the text of the configuration group
     */
    public void setUiRefCategory(I18nizableText uiRefCategory)
    {
        _uiRefCategory = uiRefCategory;
    }
    
    /**
     * Retrieves the parameters' ids used by the parameter checker
     * @return _linkedParams the parameters' ids used by the parameter checker
     **/
    public Set<String> getLinkedParamsIds()
    {
        return _linkedParamsIds;
    }
    
    /**
     * Sets the parameters' ids used by the parameter checker
     * @param linkedParamsIds the parameters' ids used by the parameter checker
     */
    public void setLinkedParamsIds(Set<String> linkedParamsIds)
    {
        _linkedParamsIds = linkedParamsIds;    
    }
    
    /**
     * Retrieves the parameter checker.
     * @return _parameterChecker the parameter checker
     */
    public ParameterChecker getParameterChecker()
    {
        return _parameterChecker;
    }
    
    /**
     * Sets the parameter checker
     * @param parameterChecker the parameter checker 
     */
    public void setParameterChecker(ParameterChecker parameterChecker)
    {
        _parameterChecker = parameterChecker;
    }
}
