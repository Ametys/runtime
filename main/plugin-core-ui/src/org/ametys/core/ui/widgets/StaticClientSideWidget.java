/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.core.ui.widgets;

import java.util.Map;

import org.apache.cocoon.util.StringUtils;

import org.ametys.core.ui.StaticClientSideElement;

/**
 * This implementation creates a widget from a static configuration.
 * Classes should have the parameters defined as constants in their class.
 */
public class StaticClientSideWidget extends StaticClientSideElement implements ClientSideWidget
{
    /** The parameter in the configuration for ftype. Comma-separated list. Defaut value is 'string'. */
    public static final String PARAMETER_FTYPES = "ftypes";
    /** The parameter in the configuration for supporting enumarated. Default value is false. */
    public static final String PARAMETER_SUPPORTS_ENUMERATED = "supports-enumerated";
    /** The parameter in the configuration for supporting non-enumarated. Default value is true. */
    public static final String PARAMETER_SUPPORTS_NONENUMERATED = "supports-non-enumerated";
    /** The parameter in the configuration for supporting multiple. Default value is false. */
    public static final String PARAMETER_SUPPORTS_MULTIPLE = "supports-multiple";
    /** The parameter in the configuration for supporting non-multiple. Default value is true. */
    public static final String PARAMETER_SUPPORTS_NONMULTIPLE = "supports-non-multiple";
    
    @Override
    public String[] getFormTypes(Map<String, Object> contextParameters)
    {
        Map<String, Object> initialParameters = _script.getParameters();
        if (initialParameters.containsKey(PARAMETER_FTYPES))
        {
            return StringUtils.split((String) initialParameters.get(PARAMETER_FTYPES), ",");
        }
        else
        {
            return new String[] {"string"}; 
        }
    }
    
    @Override
    public boolean supportsEnumerated(Map<String, Object> contextParameters)
    {
        Map<String, Object> initialParameters = _script.getParameters();
        if (initialParameters.containsKey(PARAMETER_SUPPORTS_ENUMERATED))
        {
            return Boolean.parseBoolean((String) initialParameters.get(PARAMETER_SUPPORTS_ENUMERATED));
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean supportsNonEnumerated(Map<String, Object> contextParameters)
    {
        Map<String, Object> initialParameters = _script.getParameters();
        if (initialParameters.containsKey(PARAMETER_SUPPORTS_NONENUMERATED))
        {
            return Boolean.parseBoolean((String) initialParameters.get(PARAMETER_SUPPORTS_NONENUMERATED));
        }
        else
        {
            return true;
        }
    }
    
    @Override
    public boolean supportsMultiple(Map<String, Object> contextParameters)
    {
        Map<String, Object> initialParameters = _script.getParameters();
        if (initialParameters.containsKey(PARAMETER_SUPPORTS_MULTIPLE))
        {
            return Boolean.parseBoolean((String) initialParameters.get(PARAMETER_SUPPORTS_MULTIPLE));
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean supportsNonMultiple(Map<String, Object> contextParameters)
    {
        Map<String, Object> initialParameters = _script.getParameters();
        if (initialParameters.containsKey(PARAMETER_SUPPORTS_NONMULTIPLE))
        {
            return Boolean.parseBoolean((String) initialParameters.get(PARAMETER_SUPPORTS_NONMULTIPLE));
        }
        else
        {
            return true;
        }
    }
}
