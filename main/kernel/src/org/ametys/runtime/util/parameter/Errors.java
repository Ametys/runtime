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
    private List<I18nizableText> _errors = new ArrayList<I18nizableText>();
    
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
