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

/**
 * Validator for parameters values 
 */
public interface Validator
{
    /**
     * Validate the parameter value
     * @param value The value to validate (may be null)
     * @return true if the parameter is valid or false otherwise
     */
    public boolean validate(Object value); 
}
