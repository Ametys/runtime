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
package org.ametys.runtime.util.parameter;

import org.ametys.runtime.util.I18nizableText;

/**
 * A parameter is defined with the following properties:
 * <dl>
 *  <dd>id</dd>
 *  <dt>id of the parameter, must be unique</dt>
 *  <dd>pluginName</dd>
 *  <dt>the plugin name defining this parameter</dt>
 *  <dd>label</dd>
 *  <dt>the label (can be i18nized)</dt>
 *  <dd>description</dd>
 *  <dt>the description (can be i18nized)</dt>
 *  <dd>type</dd>
 *  <dt>the type</dt>
 *  <dd>widget</dd>
 *  <dt>the optional widget to use for rendering</dt>
 *  <dd>enumerator</dd>
 *  <dt>the optional enumerator</dt>
 *  <dd>validator</dd>
 *  <dt>the optional validator</dt>
 *  <dd>defaultValue</dd>
 *  <dt>the default value</dt>
 * </dl>
 * @param <T> the actual parameter type.
 */
public class Parameter<T>
{
    private String _pluginName;
    private I18nizableText _label;
    private I18nizableText _description;
    private T _type;
    private String _widget;
    private Enumerator _enumerator;
    private Validator _validator;
    private Object _defaultValue;

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
