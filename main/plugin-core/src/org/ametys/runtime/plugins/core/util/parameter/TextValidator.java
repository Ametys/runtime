/*
 *  Copyright 2010 Anyware Services
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

package org.ametys.runtime.plugins.core.util.parameter;

import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.DefaultValidator;
import org.ametys.runtime.util.parameter.Errors;

/**
 * Implements the same configuration as the DefaultValidator and also handle a &lt;maxlength&gt; parameter that allows a max count of chars
 */
public class TextValidator extends DefaultValidator
{
    /** Does the value has a max length */
    protected Integer _maxLength;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration validatorConfig = configuration.getChild("validation").getChild("custom-validator");
        
        _isMandatory = validatorConfig.getChild("mandatory", false) != null;

        String regexp = validatorConfig.getChild("regexp").getValue(null);
        if (regexp != null)
        {
            _regexp = Pattern.compile(regexp);
        }

        int maxLength = validatorConfig.getChild("maxlength").getValueAsInteger(0);
        if (maxLength > 0)
        {
            _maxLength = maxLength;
        }
    }
    
    @Override
    public void saxConfiguration(ContentHandler handler) throws SAXException
    {
        super.saxConfiguration(handler);
        
        if (_maxLength != null)
        {
            XMLUtils.createElement(handler, "maxlength", _maxLength.toString());
        }
    }
    
    @Override
    protected void validateSingleValue (Object value, Errors errors)
    {
        super.validateSingleValue(value, errors);
        
        if (_maxLength != null && value != null && getText(value).length() > _maxLength)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a value for a parameter that should be smaller (max is " + _maxLength + " and length is " + value.toString().length() + ")");
            }

            errors.addError(new I18nizableText("plugin.core", "PLUGINS_CORE_VALIDATOR_TEXT_MAXLENGTH"));
        }
    }
    
    @Override
    protected void validateArrayValues (Object[] values, Errors errors)
    {
        super.validateArrayValues(values, errors);
        
        if (_regexp != null && values != null)
        {
            for (Object value : values)
            {
                if (getText(value).length() > _maxLength)
                {
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("The validator refused a value for a parameter that should be smaller (max is " + _maxLength + " and length is " + value.toString().length() + ")");
                    }

                    errors.addError(new I18nizableText("plugin.core", "PLUGINS_CORE_VALIDATOR_TEXT_MAXLENGTH"));
                }
            }
        }
    }
    
    /**
     * Get the text
     * @param value The value to convert to text
     * @return the value converted
     */
    protected String getText(Object value)
    {
        return value.toString();
    }
}
