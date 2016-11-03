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
package org.ametys.plugins.core.authentication;

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
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.authentication.CredentialProvider;
import org.ametys.core.authentication.CredentialProviderFactory;
import org.ametys.core.authentication.CredentialProviderModel;
import org.ametys.core.cocoon.ActionResultGenerator;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.core.util.JSONUtils;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * This action checks the validity of a credential provider
 */
public class CheckCredentialProviderAction extends ServiceableAction
{
    /** Helper component gathering utility methods for the management of JSON entities */
    private JSONUtils _jsonUtils;
    
    /** The credential providers factory */
    private CredentialProviderFactory _credentialProviderFactory;

    private UserPopulationDAO _userPopulationDAO;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
        _credentialProviderFactory = (CredentialProviderFactory) smanager.lookup(CredentialProviderFactory.ROLE);
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        super.service(smanager);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, String> result = new HashMap<>();
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        // Prepare the values for the test
        String fieldCheckersInfoJSON = request.getParameter("fieldCheckersInfo");
        Map<String, Object> fieldCheckersInfo = _jsonUtils.convertJsonToMap(fieldCheckersInfoJSON);
        Map<String, Object> valuesByParameterChecker = _getValuesByParamCheckerId(fieldCheckersInfo);

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
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> _getValuesByParamCheckerId(Map<String, Object> paramCheckersInfo)
    {
        Map<String, Object> result = new HashMap<> ();
        
        String populationId = (String) paramCheckersInfo.get("_user_population_id");
        paramCheckersInfo.remove("_user_population_id");
        UserPopulation userPopulation = null;
        
        for (String paramCheckerId : paramCheckersInfo.keySet())
        {
            Map<String, Object> valuesByParamCheckerId = new HashMap<> (); 
            
            // Check the ids of the parameter checkers and build the parameter checkers' list
            ParameterCheckerDescriptor parameterCheckerDescriptor = null;
            CredentialProviderModel cpModel = null;
            for (String credentialProviderModelId : _credentialProviderFactory.getExtensionsIds())
            {
                if (parameterCheckerDescriptor != null)
                {
                    break; // param checker was found
                }
                cpModel = _credentialProviderFactory.getExtension(credentialProviderModelId);
                for (String localCheckerId : cpModel.getParameterCheckers().keySet())
                {
                    if (localCheckerId.equals(paramCheckerId))
                    {
                        parameterCheckerDescriptor = cpModel.getParameterCheckers().get(localCheckerId);
                        break;
                    }
                }
            }
            if (cpModel == null || parameterCheckerDescriptor == null)
            {
                throw new IllegalArgumentException("The parameter checker '" + paramCheckerId + "' was not found.");
            }
            
            ParameterChecker parameterChecker  = parameterCheckerDescriptor.getParameterChecker();
            
            valuesByParamCheckerId.put("checker", parameterChecker);
            
            List<String> paramNames = (List<String>) ((Map<String, Object>) paramCheckersInfo.get(paramCheckerId)).get("testParamsNames");
            List<String> paramRawValues = (List<String>) ((Map<String, Object>) paramCheckersInfo.get(paramCheckerId)).get("rawTestValues");
            
            List<String> values = new ArrayList<> ();
            
            // Compute the proper values for the test
            String cpId = paramRawValues.get(paramRawValues.size() - 1);
            for (int i = 0; i < paramNames.size() - 1; i++)
            {
                String paramName = StringUtils.substringAfter(paramNames.get(i), "$");
                String untypedValue = ParameterHelper.valueToString(paramRawValues.get(i));
                
                // Handle password field
                if (untypedValue == null && cpModel.getParameters().get(paramName).getType() == ParameterType.PASSWORD)
                {
                    // The password is null => it means we use the existing password
                    
                    if (userPopulation == null)
                    {
                        userPopulation = _userPopulationDAO.getUserPopulation(populationId);
                    }
                    CredentialProvider cp = userPopulation.getCredentialProvider(cpId);
                    values.add((String) cp.getParameterValues().get(paramName));
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
