/*
 *  Copyright 2009 Anyware Services
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

import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.LoggerFactory;


/**
 * This default implementation validates the following configurable stuff:
 * <ul>
 *  <li>mandatory: check the parameter is set</li>
 *  <li>regexp: check the string parameter matches a regexp</li>
 * </ul> 
 */
public class DefaultValidator extends AbstractLogEnabled implements Validator, Configurable
{
    /** Is the value mandatory ? */
    protected boolean _isMandatory;
    /** Does the value need to match a regexp */
    protected Pattern _regexp;

    /**
     * Default constructor for avalon
     */
    public DefaultValidator()
    {
        // empty
    }
    
    /**
     * Manual constructor
     * @param regexp The regexp to check or null
     * @param mandatory Is the value mandatory 
     */
    public DefaultValidator(String regexp, boolean mandatory)
    {
        _isMandatory = mandatory;
        _regexp = Pattern.compile(regexp);
        enableLogging(LoggerFactory.getLoggerFor(this.getClass()));
    }
    
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration validatorConfig = configuration.getChild("validation");
        
        _isMandatory = validatorConfig.getChild("mandatory", false) != null;

        String regexp = validatorConfig.getChild("regexp").getValue(null);
        if (regexp != null)
        {
            _regexp = Pattern.compile(regexp);
        }
    }
    
    @Override
    public void saxConfiguration(ContentHandler handler) throws SAXException
    {
        XMLUtils.createElement(handler, "mandatory", Boolean.toString(_isMandatory));
        
        if (_regexp != null)
        {
            XMLUtils.createElement(handler, "regexp", _regexp.toString());
        }
    }
    
    @Override
    public void validate(Object value, Errors errors)
    {
        if (_isMandatory && (value == null || value.toString().length() == 0))
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a missing or empty value for a mandatory parameter");
            }
            
            errors.addError(new I18nizableText("kernel", "KERNEL_DEFAULT_VALIDATOR_MANDATORY"));
        }
        
        if (_regexp != null && value != null && value.toString().length() != 0 && !_regexp.matcher(value.toString()).matches())
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a value for a parameter that should respect a regexep");
            }

            errors.addError(new I18nizableText("kernel", "KERNEL_DEFAULT_VALIDATOR_PATTERN_FAILED"));
        }
    }
}
