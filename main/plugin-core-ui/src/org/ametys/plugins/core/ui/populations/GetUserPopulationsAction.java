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
package org.ametys.plugins.core.ui.populations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.user.population.PopulationContextHelper;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * Action for generating the user populations (all of it or by a given context)
 */
public class GetUserPopulationsAction extends ServiceableAction
{
    /** The user population DAO */
    private UserPopulationDAO _userPopulationDAO;
    /** The helper for the associations population/context */
    private PopulationContextHelper _populationContextHelper;

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _userPopulationDAO = (UserPopulationDAO) serviceManager.lookup(UserPopulationDAO.ROLE);
        _populationContextHelper = (PopulationContextHelper) manager.lookup(PopulationContextHelper.ROLE);
    }

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        List<Object> populations;
        
        @SuppressWarnings("unchecked")
        Map jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        String context = (String) jsParameters.get("context");
        Boolean showDisabled = (Boolean) jsParameters.get("showDisabled");
        if (context == null && showDisabled != null && showDisabled)
        {
            Boolean withAdmin = Boolean.FALSE;
            if (jsParameters.get("withAdmin") != null)
            {
                withAdmin = (Boolean) jsParameters.get("withAdmin");
            }
            populations = _userPopulationDAO.getUserPopulationsAsJson(withAdmin);
        }
        else if (context == null)
        {
            Boolean withAdmin = Boolean.FALSE;
            if (jsParameters.get("withAdmin") != null)
            {
                withAdmin = (Boolean) jsParameters.get("withAdmin");
            }
            populations = _userPopulationDAO.getEnabledUserPopulations(withAdmin).stream().map(_userPopulationDAO::getUserPopulationAsJson).collect(Collectors.toList());
        }
        else
        {
            populations = new ArrayList<>();
            
            Set<String> populationIds = _populationContextHelper.getUserPopulationsOnContext(context);
            for (String populationId : populationIds)
            {
                UserPopulation up = _userPopulationDAO.getUserPopulation(populationId);
                populations.add(_userPopulationDAO.getUserPopulationAsJson(up));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userPopulations", populations);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }

}
