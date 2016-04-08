/*
 *  Copyright 2016 Anyware Services
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * Descriptor of a parameter checker 
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
    
    /** The order of the parameter checker. When several parameter checkers have the same location, 
     *  the order allows to order graphically the parameter checkers: the parameter checker with the lowest
     *  order will be at the top. */
    protected int _uiRefOrder;
    
    /** The location of the parameter checker */
    protected String _uiRefLocation;
    
    /** The configuration of the linked parameters */
    protected Set<String> _linkedParamsPaths;
    
    /** The concrete class of the parameter checker implementing the check */
    protected ParameterChecker _parameterChecker;

    /**
     * SAX the description informations
     * @param handler The handler where to sax
     * @throws SAXException if an error occurred
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        XMLUtils.createElement(handler, "id", getId());
        getLabel().toSAX(handler, "label");
        getDescription().toSAX(handler, "description");

        XMLUtils.createElement(handler, "small-icon-path", getSmallIconPath());
        XMLUtils.createElement(handler, "medium-icon-path", getMediumIconPath());
        XMLUtils.createElement(handler, "large-icon-path", getLargeIconPath());
        
        if (getLinkedParamsPaths() != null)
        {
            String linkedParamsAsJSON = "[" + StringUtils.join(getLinkedParamsPaths().parallelStream().map(s -> "\"" + s + "\"").collect(Collectors.toList()), ", ") + "]"; 
            XMLUtils.createElement(handler, "linked-fields", linkedParamsAsJSON);
        }

        XMLUtils.createElement(handler, "order", Integer.toString(getUiRefOrder()));
    }
    
    /**
     * Get the description information to JSON format
     * @return The information as a map
     */
    public Map<String, Object> toJSON()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("id", getId());
        result.put("label", getLabel());
        result.put("description", getDescription());
        result.put("small-icon-path", getSmallIconPath());
        result.put("medium-icon-path", getMediumIconPath());
        result.put("large-icon-path", getLargeIconPath());
        
        if (getLinkedParamsPaths() != null)
        {
            result.put("linked-fields", getLinkedParamsPaths());
        }
        
        result.put("order", getUiRefOrder());
        
        return result;
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
     * Get the location of the parameter checker
     * @return _uiRefOrder the ui order
     */
    public String getUiRefLocation()
    {
        return _uiRefLocation;
    }
    
    /**
     * Set the location of the parameter checker
     * @param uiRefLocation the location of the parameter checker
     */
    public void setUiRefLocation(String uiRefLocation)
    {
        _uiRefLocation = uiRefLocation;
    }
    
    /**
     * Retrieve the path of the parameters used by the parameter checker
     * @return _linkedParamsPaths the paths of the parameters used by the parameter checker
     **/
    public Set<String> getLinkedParamsPaths()
    {
        return _linkedParamsPaths;
    }
    
    /**
     * Sets the parameters' ids used by the parameter checker
     * @param linkedParamsPaths the parameters' ids used by the parameter checker
     */
    public void setLinkedParamsPaths(Set<String> linkedParamsPaths)
    {
        _linkedParamsPaths = linkedParamsPaths;    
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
