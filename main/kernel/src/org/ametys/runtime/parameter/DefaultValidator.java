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
package org.ametys.runtime.parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.apache.cocoon.xml.XMLUtils;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.util.JSONUtils;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.PluginAware;


/**
 * This default implementation validates the following configurable stuff:
 * <ul>
 *  <li>mandatory: check the parameter is set</li>
 *  <li>regexp: check the string parameter matches a regexp</li>
 * </ul> 
 */
public class DefaultValidator extends AbstractLogEnabled implements Validator, Configurable, PluginAware, Serviceable
{
    /** Is the value mandatory ? */
    protected boolean _isMandatory;
    /** Does the value need to match a regexp */
    protected Pattern _regexp;
    /** The error text to display if regexp fails */
    protected I18nizableText _invalidText;
    /** The plugin name */
    protected String _pluginName;
    
    /** The service manager */
    protected ServiceManager _smanager;
    
    private JSONUtils _jsonUtils;
    
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
        if (regexp != null)
        {
            _regexp = Pattern.compile(regexp);
        }
        enableLogging(new SLF4JLoggerAdapter(LoggerFactory.getLogger(this.getClass())));
    }
    
    /**
     * Manual constructor
     * @param regexp The regexp to check or null
     * @param invalidText The error text to display
     * @param mandatory Is the value mandatory 
     */
    public DefaultValidator(String regexp, I18nizableText invalidText, boolean mandatory)
    {
        _isMandatory = mandatory;
        if (regexp != null)
        {
            _regexp = Pattern.compile(regexp);
        }
        _invalidText = invalidText;
        
        enableLogging(new SLF4JLoggerAdapter(LoggerFactory.getLogger(this.getClass())));
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
    }
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
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
        
        Configuration textConfig = validatorConfig.getChild("invalidText", false);
        if (textConfig != null)
        {
            _invalidText = I18nizableText.parseI18nizableText(textConfig, "plugin." + _pluginName);
        }
    }
    
    @Override
    public Map<String, Object> toJson()
    {
        Map<String, Object> jsonObject = new HashMap<>();
        
        jsonObject.put("mandatory", _isMandatory);
        
        if (_regexp != null)
        {
            jsonObject.put("regexp", _regexp.toString());
        }
        
        if (_invalidText != null)
        {
            jsonObject.put("invalidText", _invalidText);
        }
        return jsonObject;
    }
    
    /**
     * SAX the JSON configuration of this validator 
     * @param handler The content handlet to sax into
     * @throws SAXException if an error occurred
     */
    protected void saxJsonConfig (ContentHandler handler) throws SAXException
    {
        if (_jsonUtils == null)
        {
            try
            {
                // lazy lookup the json utils
                _jsonUtils = (JSONUtils) _smanager.lookup(JSONUtils.ROLE);
                XMLUtils.createElement(handler, "config", _jsonUtils.convertObjectToJson(toJson()));
            }
            catch (ServiceException e)
            {
                // FIXME  RUNTIME-2013 JSONUtils can not be used in safe-mode 
                getLogger().warn("Unable to retrieve the component JSONUtils", e);
            }
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
        
        if (_invalidText != null)
        {
            _invalidText.toSAX(handler, "invalidText");
        }
        
        // FIXME  RUNTIME-2013 JSONUtils can not be used in safe-mode 
        // saxJsonConfig(handler);
    }
    
    @Override
    public Map<String, Object> getConfiguration()
    {
        Map<String, Object> configuration = new HashMap<>();
        
        configuration.put("mandatory", Boolean.valueOf(_isMandatory));
        
        if (_regexp != null)
        {
            configuration.put("regexp", _regexp);
        }
    
        if (_invalidText != null)
        {
            configuration.put("invalidText", _invalidText);
        }
        
        return configuration;
    }
    
    @Override
    public void validate(Object value, Errors errors)
    {
        boolean isArray = value != null && value.getClass().isArray();
        if (isArray)
        {
            validateArrayValues((Object[]) value, errors); 
        }
        else
        {
            validateSingleValue (value, errors);
        }
    }
    
    /**
     * Validates a single value.
     * @param value the value to validate (can be <code>null</code>).
     * @param errors the structure to populate if the validation failed.
     */
    protected void validateSingleValue (Object value, Errors errors)
    {
        if (_isMandatory && (value == null || value.toString().length() == 0))
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a missing or empty value for a mandatory parameter");
            }
            
            errors.addError(new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_DEFAULT_VALIDATOR_MANDATORY"));
        }
        
        if (_regexp != null && value != null && value.toString().length() != 0 && !_regexp.matcher(value.toString()).matches())
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a value for a parameter that should respect a regexep");
            }

            errors.addError(new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_DEFAULT_VALIDATOR_PATTERN_FAILED"));
        }
    }
    
    /**
     * Validates a array of values.
     * @param values the values to validate
     * @param errors the structure to populate if the validation failed.
     */
    protected void validateArrayValues (Object[] values, Errors errors)
    {
        if (_isMandatory && (values == null || values.length == 0))
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a missing or empty value for a mandatory parameter");
            }
            
            errors.addError(new I18nizableText("kernel", "PLUGINS_CORE_UI_DEFAULT_VALIDATOR_MANDATORY"));
        }
        
        if (_regexp != null && values != null && !_matchRegexp(values))
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("The validator refused a value for a parameter that should respect a regexep");
            }

            errors.addError(new I18nizableText("kernel", "PLUGINS_CORE_UI_DEFAULT_VALIDATOR_PATTERN_FAILED"));
        }
    }
    
    private boolean _matchRegexp (Object[] values)
    {
        for (Object value : values)
        {
            if (!_regexp.matcher(value.toString()).matches())
            {
                return false;
            }
        }
        return true;
    }
}
