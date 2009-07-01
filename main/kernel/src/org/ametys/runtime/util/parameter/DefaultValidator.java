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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.LoggerFactory;


/**
 * This default implementation validates the following configurable stuff:
 * <ul>
 *  <li>mandatory: check the parameter is set</li>
 *  <li>regexp: check the string parameter matches a regexp</li>
 * </ul> 
 */
public class DefaultValidator implements Validator
{
    private static final Logger __LOGGER = LoggerFactory.getLoggerFor(DefaultValidator.class);
    private static final String __KERNEL_CATALOG = "kernel";
    /** The validator will check if value is not null */
    private boolean _mandatory;
    /** The validator will check if the value match the following pattern */
    private Pattern _pattern;
    
    /**
     * Constructor.
     * @param regexp a regexp that the associated valeu must match. May be null.
     * @param mandatory a boolean indicating if the associated value is mandatory or not
     * @throws PatternSyntaxException if the regexp syntax is invalid
     */
    public DefaultValidator(String regexp, boolean mandatory) throws PatternSyntaxException
    {
        _mandatory = mandatory;
        
        if (regexp != null)
        {
            _pattern = Pattern.compile(regexp);
        }
    }
    
    /**
     * Tests if current validator test if the value is mandatory.
     * @return the mandatory status.
     */
    public boolean isMandatory()
    {
        return _mandatory;
    }
    
    /**
     * Retrieves the pattern to check.
     * @return the pattern to check or <code>null</code> if none.
     */
    public Pattern getPattern()
    {
        return _pattern;
    }
    
    public void validate(Object value, Errors errors)
    {
        if (_mandatory && (value == null || value.toString().length() == 0))
        {
            if (__LOGGER.isDebugEnabled())
            {
                __LOGGER.debug("The validator refused a missing or empty value for a mandatory parameter");
            }
            
            errors.addError(new I18nizableText(__KERNEL_CATALOG, "KERNEL_DEFAULT_VALIDATOR_MANDATORY"));
        }
        
        if (_pattern != null && value != null && value.toString().length() != 0 && !_pattern.matcher(value.toString()).matches())
        {
            if (__LOGGER.isDebugEnabled())
            {
                __LOGGER.debug("The validator refused a value for a parameter that should respect a regexep");
            }

            errors.addError(new I18nizableText(__KERNEL_CATALOG, "KERNEL_DEFAULT_VALIDATOR_PATTERN_FAILED"));
        }
    }
}
