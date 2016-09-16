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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.ametys.core.user.directory.ModifiableUserDirectory;
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
        List<UserPopulation> populations;
        
        @SuppressWarnings("unchecked")
        Map jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        @SuppressWarnings("unchecked")
        List<String> contexts = (List<String>) jsParameters.get("contexts");
        
        
        // GET POPULATIONS
        Boolean showDisabled = (Boolean) jsParameters.get("showDisabled");
        
        if (contexts == null && showDisabled == Boolean.TRUE)
        {
            Boolean withAdmin = Boolean.FALSE;
            if (jsParameters.get("withAdmin") != null)
            {
                withAdmin = (Boolean) jsParameters.get("withAdmin");
            }
            populations = _userPopulationDAO.getUserPopulations(withAdmin);
        }
        else if (contexts == null)
        {
            Boolean withAdmin = Boolean.FALSE;
            if (jsParameters.get("withAdmin") != null)
            {
                withAdmin = (Boolean) jsParameters.get("withAdmin");
            }
            populations = _userPopulationDAO.getEnabledUserPopulations(withAdmin);
        }
        else
        {
            populations = new ArrayList<>();
            
            List<String> populationIds = _populationContextHelper.getUserPopulationsOnContexts(new HashSet<>(contexts));
            for (String populationId : populationIds)
            {
                populations.add(_userPopulationDAO.getUserPopulation(populationId));
            }
        }
        
        // FILTER MODIFIABLE ONLY
        Boolean modifiable = (Boolean) jsParameters.get("modifiable");
        if (modifiable == Boolean.TRUE)
        {
            populations = populations.stream().filter(up -> containsModifiableUserDirectory(up)).collect(Collectors.toList());
        }
        
        // CONVERT TO JSON
        Map<String, Object> result = new HashMap<>();
        List<Object> populationsAsJson = populations.stream().map(_userPopulationDAO::getUserPopulationAsJson).collect(Collectors.toList());
        result.put("userPopulations", populationsAsJson);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }

    private boolean containsModifiableUserDirectory(UserPopulation up)
    {
        return up.getUserDirectories().stream().anyMatch(ud -> ud instanceof ModifiableUserDirectory);
    }
}
