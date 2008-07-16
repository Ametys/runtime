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
 * Value for an <code>Enumerator</code> containing both value and i18nizable label.<br>
 * The value is a java.lang.Object which actual type is one of {@link ParameterHelper.TYPE}
 */
public final class EnumeratorValue
{
    private Object _value;
    private I18nizableText _label;
    
    /**
     * Constructor
     * @param value the typed value. May be null.
     * @param label the associated label. Cannot be null.
     */
    public EnumeratorValue(Object value, I18nizableText label)
    {
        _value = value;
        _label = label;
    }
    
    /**
     * Returns the value
     * @return the value
     */
    public Object getValue()
    {
        return _value;
    }
    
    /**
     * Returns the i18nizable label
     * @return the i18nizable label
     */
    public I18nizableText getLabel()
    {
        return _label;
    }
}
