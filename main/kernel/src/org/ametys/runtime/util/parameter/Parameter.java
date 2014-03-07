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

package org.ametys.runtime.util.parameter;

import java.util.Map;

import org.ametys.runtime.util.I18nizableText;

/**
 * A parameter is defined with the following properties:
 * <dl>
 *  <dd>id
 *  <dt>id of the parameter, must be unique
 *  <dd>pluginName
 *  <dt>the plugin name defining this parameter
 *  <dd>label
 *  <dt>the label (can be i18nized)
 *  <dd>description
 *  <dt>the description (can be i18nized)
 *  <dd>type
 *  <dt>the type
 *  <dd>widget
 *  <dt>the optional widget to use for rendering
 *  <dd>enumerator
 *  <dt>the optional enumerator
 *  <dd>validator
 *  <dt>the optional validator
 *  <dd>defaultValue
 *  <dt>the default value
 * </dl>
 * @param <T> the actual parameter type.
 */
public class Parameter<T>
{
    private String _id;
    private String _pluginName;
    private I18nizableText _label;
    private I18nizableText _description;
    private T _type;
    private String _widget;
    private Map<String, I18nizableText> _widgetParams;
    private Enumerator _enumerator;
    private Validator _validator;
    private Object _defaultValue;

    /**
     * Get the id.
     * @return Returns the id.
     */
    public String getId()
    {
        return _id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(String id)
    {
        _id = id;
    }

    
    /**
     * Retrieves the name of the plugin declaring this parameter.
     * @return the plugin name.
     */
    public String getPluginName()
    {
        return _pluginName;
    }

    /**
     * Set the name of the plugin declaring this parameter.
     * @param pluginName the plugin name.
     */
    public void setPluginName(String pluginName)
    {
        _pluginName = pluginName;
    }

    /**
     * Retrieves the label.
     * @return the label.
     */
    public I18nizableText getLabel()
    {
        return _label;
    }

    /**
     * Set the label.
     * @param label the label.
     */
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }

    /**
     * Retrieves the description.
     * @return the description.
     */
    public I18nizableText getDescription()
    {
        return _description;
    }

    /**
     * Set the description.
     * @param description the description.
     */
    public void setDescription(I18nizableText description)
    {
        _description = description;
    }

    /**
     * Retrieves the type.
     * @return the type.
     */
    public T getType()
    {
        return _type;
    }

    /**
     * Set the type.
     * @param type the type.
     */
    public void setType(T type)
    {
        _type = type;
    }

    /**
     * Retrieves the widget to use for rendering.
     * @return the widget or <code>null</code> if none is defined.
     */
    public String getWidget()
    {
        return _widget;
    }

    /**
     * Set the widget.
     * @param widget the widget.
     */
    public void setWidget(String widget)
    {
        _widget = widget;
    }
    
    /**
     * Get the widget's parameters
     * @return the widget's parameters
     */
    public Map<String, I18nizableText> getWidgetParameters()
    {
        return _widgetParams;
    }
    
    /**
     * Set the widget's parameters
     * @param params the parameters to set
     */
    public void setWidgetParameters (Map<String, I18nizableText> params)
    {
        _widgetParams = params;
    }
    
    /**
     * Retrieves the enumerator.
     * @return the enumerator or <code>null</code> if none is defined.
     */
    public Enumerator getEnumerator()
    {
        return _enumerator;
    }

    /**
     * Set the enumerator.
     * @param enumerator the enumerator.
     */
    public void setEnumerator(Enumerator enumerator)
    {
        _enumerator = enumerator;
    }

    /**
     * Retrieves the validator.
     * @return the validator or <code>null</code> if none is defined.
     */
    public Validator getValidator()
    {
        return _validator;
    }

    /**
     * Set the validator.
     * @param validator the validator.
     */
    public void setValidator(Validator validator)
    {
        _validator = validator;
    }

    /**
     * Retrieves the default value.
     * @return the default value or <code>null</code> if none is defined.
     */
    public Object getDefaultValue()
    {
        return _defaultValue;
    }

    /**
     * Set the default value.
     * @param defaultValue the default value.
     */
    public void setDefaultValue(Object defaultValue)
    {
        _defaultValue = defaultValue;
    }
}
