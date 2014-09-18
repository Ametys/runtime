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
package org.ametys.runtime.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.StringUtils;

import org.ametys.runtime.util.cocoon.ActionResultGenerator;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * Dispatches the requests to the appropriate parameter checkers.
 */
public class ParameterCheckerAction extends AbstractAction 
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting ParameterCheckerAction");
        }
        
        Map<String, String> result = new HashMap<String, String> ();
        Request request = ObjectModelHelper.getRequest(objectModel);
        

        // Check the ids of the parameter checkers and build the parameter checkers' list
        List<ParameterChecker> parameterCheckers = new ArrayList<ParameterChecker>(); 
        String paramCheckersIdsStr = request.getParameter("paramCheckersIds");
        String[] paramCheckersIds = StringUtils.split(paramCheckersIdsStr, ",");
        for (String paramCheckerId : paramCheckersIds)
        {
            ParameterChecker parameterChecker = ConfigManager.getInstance().getParameterChecker(paramCheckerId).getParameterChecker();
            if (parameterChecker == null)
            {
                throw new IllegalArgumentException("The parameter checker '" + paramCheckerId + "' does not exist");
            }
            
            parameterCheckers.add(parameterChecker);
        }
        
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
                oldUntypedValues = new HashMap<String, String>();
            }
        }
        
        // Configuration
        Map<String, String> untypedValues = new HashMap<String, String>();
        String[] ids = ConfigManager.getInstance().getParametersIds();
        for (String id : ids)
        {
            String untypedValue = request.getParameter(id);
            untypedValues.put(id, untypedValue);
        }
        
        // Put request parameters in a map 
        Map<String, String> requestParameters = new HashMap<String, String> ();
        Map<String, ConfigParameter> configParams = ConfigManager.getInstance().getParameters();
        for (String id: ids)
        {
            String untypedValue = untypedValues.get(id);
            
            Object typedValue = ParameterHelper.castValue(untypedValue, configParams.get(id).getType());
            
            // Handle password field
            if (typedValue == null && configParams.get(id).getType() == ParameterType.PASSWORD)
            {
                if (Config.getInstance() != null)
                {
                    // Keeps the value of an empty password field
                    typedValue = Config.getInstance().getValueAsString(id);
                }
                else if (oldUntypedValues != null)
                {
                    typedValue = oldUntypedValues.get(id);
                }
                
                requestParameters.put(id, (String) typedValue);
            }
            else
            {
                requestParameters.put(id, request.getParameter(id));
            }
        }

        // Dispatch the requests
        ParameterChecker parameterChecker = null;
        for (int i = 0; i < parameterCheckers.size(); i++)
        {
            try 
            {
                parameterChecker = parameterCheckers.get(i);
                parameterChecker.check(requestParameters); 
            }
            catch (Throwable t)
            {
                getLogger().error("The test '" + paramCheckersIds[i] + "' failed : \n" + t.getMessage(), t);
                String msg = t.getMessage() != null ? t.getMessage() : "Unknown error";
                result.put(paramCheckersIds[i], msg);
            }
        }
        request.setAttribute(ActionResultGenerator.MAP_REQUEST_ATTR, result);
        return result;
    }
}
