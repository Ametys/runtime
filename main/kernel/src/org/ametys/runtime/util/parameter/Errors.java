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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ametys.runtime.util.I18nizableText;

/**
 * Errors structure to populate on validation.
 * @see Validator
 */
public class Errors
{
    private List<I18nizableText> _errors = new ArrayList<>();
    
    /**
     * Tests if there were any errors.
     * @return <code>true</code> if there is at least one error,
     *         <code>false</code> if there is no error.
     */
    public boolean hasErrors()
    {
        return !_errors.isEmpty();
    }
    
    /**
     * Retrieves the errors.
     * @return the errors.
     */
    public List<I18nizableText> getErrors()
    {
        return Collections.unmodifiableList(_errors);
    }
    
    /**
     * Add an error.
     * @param errorLabel the error label.
     */
    public void addError(I18nizableText errorLabel)
    {
        _errors.add(errorLabel);
    }
}
