/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.plugins.admin.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.ActionResultGenerator;
import org.ametys.core.util.JSONUtils;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.config.ConfigParameter;
import org.ametys.runtime.config.ConfigParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Fetch the test values and dispatch the requests to the appropriate parameter checkers.
 */
public class CheckConfigAction extends ServiceableAction
{
    /** Helper component gathering utility methods for the management of JSON entities */
    private JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _jsonUtils = (JSONUtils) serviceManager.lookup(JSONUtils.ROLE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, String> result = new HashMap<> ();
        
        // Handle the 'configuration not initialized' case
        Map<String, String> oldUntypedValues = null;
        if (Config.getInstance() == null)
        {
            try
            {
                oldUntypedValues = Config.read();
            }
            catch (Exception e)
            {
                oldUntypedValues = new HashMap<>();
            }
        }
        
        // Fetch the request parameters
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        // Prepare the values for the test
        String fieldCheckersInfoJSON = request.getParameter("fieldCheckersInfo");
        Map<String, Object> fieldCheckersInfo = _jsonUtils.convertJsonToMap(fieldCheckersInfoJSON);
        Map<String, Object> valuesByParameterChecker = _getValuesByParamCheckerId(oldUntypedValues, fieldCheckersInfo);

        // Dispatch the requests
        for (String fieldCheckerId : valuesByParameterChecker.keySet())
        {
            ParameterChecker fieldChecker = (ParameterChecker) ((Map<String, Object>) valuesByParameterChecker.get(fieldCheckerId)).get("checker");
            List<String> values = (List<String>) ((Map<String, Object>) valuesByParameterChecker.get(fieldCheckerId)).get("values");
            try 
            {
                fieldChecker.check(values); 
            }
            catch (Throwable t)
            {
                getLogger().error("The test '" + fieldCheckerId + "' failed : \n" + t.getMessage(), t);
                String msg = t.getMessage() != null ? t.getMessage() : "Unknown error";
                result.put(fieldCheckerId, msg);
            }
        }
        
        request.setAttribute(ActionResultGenerator.MAP_REQUEST_ATTR, result);
        return result;
    }

    /**
     * Compute the proper values and {@link ParameterChecker} implementations to use for the test and order them by parameter checker id
     * @param oldUntypedValues the map of old untyped values of the configuration
     * @param paramCheckersInfo the information concerning the parameter checkers
     * @return the map of values lists ordered by parameter checker
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> _getValuesByParamCheckerId(Map<String, String> oldUntypedValues, Map<String, Object> paramCheckersInfo)
    {
        Map<String, Object> result = new HashMap<> ();
        
        Map<String, ConfigParameter> configParams = ConfigManager.getInstance().getParameters();
        for (String paramCheckerId : paramCheckersInfo.keySet())
        {
            Map<String, Object> valuesByParamCheckerId = new HashMap<> (); 
            
            // Check the ids of the parameter checkers and build the parameter checkers' list
            ConfigParameterCheckerDescriptor parameterCheckerDescriptor = ConfigManager.getInstance().getParameterChecker(paramCheckerId);
            if (parameterCheckerDescriptor == null)
            {
                throw new IllegalArgumentException("The parameter checker '" + paramCheckerId + "' was not found.");
            }
            
            ParameterChecker parameterChecker  = parameterCheckerDescriptor.getParameterChecker();
            
            valuesByParamCheckerId.put("checker", parameterChecker);
            
            List<String> paramNames = (List<String>) ((Map<String, Object>) paramCheckersInfo.get(paramCheckerId)).get("testParamsNames");
            List<String> paramRawValues = (List<String>) ((Map<String, Object>) paramCheckersInfo.get(paramCheckerId)).get("rawTestValues");
            
            List<String> values = new ArrayList<> ();
            
            // Compute the proper values for the test
            for (int i = 0; i < paramNames.size(); i++)
            {
                String paramName = paramNames.get(i);
                String untypedValue = ParameterHelper.valueToString(paramRawValues.get(i));
                
                // Handle password field
                if (untypedValue == null && configParams.get(paramName).getType() == ParameterType.PASSWORD)
                {
                    String typedValue = null;
                    if (Config.getInstance() != null)
                    {
                        // Fetch the value of an empty password field
                        typedValue = Config.getInstance().getValueAsString(paramName);
                    }
                    else if (oldUntypedValues != null)
                    {
                        typedValue = oldUntypedValues.get(paramName);
                    }
                    
                    values.add(typedValue);
                }
                else
                {
                    values.add(untypedValue);
                }
            }
            
            valuesByParamCheckerId.put("values", values);
            
            result.put(paramCheckerId, valuesByParamCheckerId);
        }
        
        return result;
    }
}
