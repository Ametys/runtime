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

package org.ametys.runtime.config;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * This class represents a configuration parameter
 */
public class ConfigParameter extends Parameter<ParameterType> implements Comparable<ConfigParameter>
{
    private I18nizableText _displayCategory;
    private I18nizableText _displayGroup;
    private boolean _groupSwitch;
    private long _order;
    private DisableConditions _disableConditions;
    private final JsonFactory _jsonFactory = new JsonFactory();
    private final ObjectMapper _objectMapper = new ObjectMapper();
    
    /**
     * Retrieves the display category of the parameter.
     * @return _displayCategory the display category
     */
    public I18nizableText getDisplayCategory()
    {
        return _displayCategory;
    }
    
    /**
     * Sets the display category of the parameter.
     * @param displayCategory The category of the parameter
     */
    public void setDisplayCategory(I18nizableText displayCategory)
    {
        _displayCategory = displayCategory;
    }
    
    /**
     * Retrieves the display group of the parameter
     * @return _displayGroup the display group
     */
    public I18nizableText getDisplayGroup()
    {
        return _displayGroup;
    }
    
    /**
     * Sets the display group of the parameter
     * @param displayGroup the display group
     */
    public void setDisplayGroup(I18nizableText displayGroup)
    {
        _displayGroup = displayGroup;
    }
    
    /**
     * Retrieves the group switch of the parameter if it has one, <code>null</code> otherwise
     * @return _groupSwitch the group-switch
     */
    public boolean isGroupSwitch()
    {
        return _groupSwitch;
    }
    
    /***
     * Sets the group switch of a parameter 
     * @param groupSwitch the group switch
     */
    public void setGroupSwitch(boolean groupSwitch)
    {
        _groupSwitch = groupSwitch;
    }
    
    /**
     * Retrieves the order of a parameter
     * @return _order the order 
     */
    public long getOrder()
    {
        return _order;
    }
    
    /**
     * Sets the order of the parameter
     * @param order the order
     */
    public void setOrder(long order)
    {
        _order = order;
    }
    
    /**
     * Retrieves the disable condition.
     * @return the disable condition or <code>null</code> if none is defined.
     */
    public DisableConditions getDisableConditions()
    {
        return _disableConditions;
    }

    /**
     * Sets the disable condition.
     * @param disableConditions the disable condition.
     */
    public void setDisableConditions(DisableConditions disableConditions)
    {
        _disableConditions = disableConditions;
    }
    
    @Override
    public int compareTo(ConfigParameter o)
    {
        int cat = getDisplayCategory().toString().compareTo(o.getDisplayCategory().toString());
        if (cat != 0)
        {
            return cat;
        }
        
        int gro = getDisplayGroup().toString().compareTo(o.getDisplayGroup().toString());
        if (gro != 0)
        {
            return gro;
        }
        
        int ord = ((Long) this.getOrder()).compareTo(o.getOrder());
        if (ord != 0)
        {
            return ord;
        }
        
        return getId().compareTo(o.getId());
    }
 
    
    /**
     * Formats disable conditions into JSON. 
     * @return the Object as a JSON string.
     */
    public String disableConditionsToJSON()
    {
        try
        {
            StringWriter writer = new StringWriter();
            
            JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(writer);
            
            Map<String, Object> asJson = _disableConditionsAsMap(this.getDisableConditions());
            _objectMapper.writeValue(jsonGenerator, asJson);
            
            return writer.toString();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("The object can not be converted to json string", e);
        }
    }

    private Map<String, Object> _disableConditionsAsMap(DisableConditions disableConditions)
    {
        Map<String, Object> map = new HashMap<>();
        
        // Handle simple conditions
        List<Map<String, String>> disableConditionList = new ArrayList<>();
        map.put("condition", disableConditionList);
        for (DisableCondition disableCondition : disableConditions.getConditions())
        {
            Map<String, String> disableConditionAsMap = _disableConditionAsMap(disableCondition);
            disableConditionList.add(disableConditionAsMap);
        }

        // Handle nested conditions
        List<Map<String, Object>> disableConditionsList = new ArrayList<>();
        map.put("conditions", disableConditionsList);
        for (DisableConditions subDisableConditions : disableConditions.getSubConditions())
        {
            Map<String, Object> disableConditionsAsMap = _disableConditionsAsMap(subDisableConditions);
            disableConditionsList.add(disableConditionsAsMap);
        }
        
        // Handle type
        map.put("type", disableConditions.getAssociationType().toString().toLowerCase());
        
        return map; 
    }

    /**
     * Formats an Object into JSON. 
     * @param disableCondition The disable condition to convert
     * @return the Object as a JSON string.
     */
    private Map<String, String> _disableConditionAsMap(DisableCondition disableCondition)
    {
        Map<String, String> map = new HashMap<>();
        map.put("id", disableCondition.getId());
        map.put("operator", disableCondition.getOperator().toString().toLowerCase());
        map.put("value", disableCondition.getValue());
        return map;
    }
}
