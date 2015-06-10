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
package org.ametys.core.userpref;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.ametys.core.util.I18nizableText;
import org.ametys.runtime.parameter.Errors;

/**
 * User preferences errors.
 */
public class UserPreferencesErrors
{
    /** The errors as a Map of field ID -&gt; error messages. */
    protected Map<String, Errors> _errors;
    
    /**
     * Default constructor.
     */
    public UserPreferencesErrors()
    {
        this(new LinkedHashMap<String, Errors>());
    }
    
    /**
     * Constructor with parameters.
     * @param errors the errors.
     */
    public UserPreferencesErrors(Map<String, Errors> errors)
    {
        this._errors = errors;
    }
    
    /**
     * Get the errors.
     * @return the errors.
     */
    public Map<String, Errors> getErrors()
    {
        return Collections.unmodifiableMap(_errors);
    }
    
    /**
     * Get the errors for a single field.
     * @param fieldId the field ID.
     * @return the field errors.
     */
    public Errors getErrors(String fieldId)
    {
        Errors fieldErrors;
        if (_errors.containsKey(fieldId))
        {
            fieldErrors = _errors.get(fieldId);
        }
        else
        {
            fieldErrors = new Errors();
            _errors.put(fieldId, fieldErrors);
        }
        return fieldErrors;
    }
    
    /**
     * Set the errors.
     * @param errors the errors to set
     */
    public void setErrors(Map<String, Errors> errors)
    {
        this._errors = errors;
    }
    
    /**
     * Add an error.
     * @param fieldId the field ID.
     * @param error the error message.
     */
    public void addError(String fieldId, I18nizableText error)
    {
        if (StringUtils.isNotEmpty(fieldId) && error != null)
        {
            getErrors(fieldId).addError(error);
        }
    }
    
    /**
     * Add an error list.
     * @param fieldId the field ID.
     * @param errors the error messages.
     */
    public void addErrors(String fieldId, List<I18nizableText> errors)
    {
        if (StringUtils.isNotEmpty(fieldId) && !errors.isEmpty())
        {
            Errors fieldErrors = getErrors(fieldId);
            
            for (I18nizableText errorLabel : errors)
            {
                fieldErrors.addError(errorLabel);
            }
        }
    }
    
    /**
     * Tests if the form has errors.
     * @return true if there are errors, false otherwise.
     */
    public boolean hasErrors()
    {
        return !_errors.isEmpty();
    }
    
}
