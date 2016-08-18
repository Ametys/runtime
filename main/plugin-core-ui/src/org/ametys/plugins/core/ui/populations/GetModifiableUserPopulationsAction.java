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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.core.user.directory.UserDirectoryModel;
import org.ametys.core.user.population.PopulationContextHelper;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * Action for generating the modifiable user populations (all of it or by a given context)
 */
public class GetModifiableUserPopulationsAction extends ServiceableAction
{
    /** The user population DAO */
    private UserPopulationDAO _userPopulationDAO;
    /** The helper for the associations population/context */
    private PopulationContextHelper _populationContextHelper;
    /** The user directories factory  */
    private UserDirectoryFactory _userDirectoryFactory;

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _userPopulationDAO = (UserPopulationDAO) serviceManager.lookup(UserPopulationDAO.ROLE);
        _populationContextHelper = (PopulationContextHelper) serviceManager.lookup(PopulationContextHelper.ROLE);
        _userDirectoryFactory = (UserDirectoryFactory) serviceManager.lookup(UserDirectoryFactory.ROLE);
    }

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        List<Object> populations = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        @SuppressWarnings("unchecked")
        List<String> contexts = (List<String>) jsParameters.get("contexts");
        
        Set<String> populationIds = _populationContextHelper.getUserPopulationsOnContexts(new HashSet<>(contexts));
        for (String populationId : populationIds)
        {
            UserPopulation up = _userPopulationDAO.getUserPopulation(populationId);
            Map<String, Object> popMap = _getUserPopulation2Json(up);
            if (popMap != null)
            {
                populations.add(popMap);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userPopulations", populations);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }
    
    private Map<String, Object> _getUserPopulation2Json(UserPopulation userPopulation)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", userPopulation.getId());
        result.put("label", userPopulation.getLabel());
        List<Map<String, Object>> userDirectories = new ArrayList<>();
        for (int index = 0; index < userPopulation.getUserDirectories().size(); index++)
        {
            UserDirectory ud = userPopulation.getUserDirectories().get(index);
            if (ud instanceof ModifiableUserDirectory)
            {
                String udModelId = ud.getUserDirectoryModelId();
                UserDirectoryModel udModel = _userDirectoryFactory.getExtension(udModelId);
                
                Map<String, Object> udMap = new HashMap<>();
                udMap.put("label", udModel.getLabel());
                udMap.put("index", index);
                userDirectories.add(udMap);
            }
        }
        if (userDirectories.size() == 0)
        {
            // No modiable user directory, the population is considered unmodifiable
            return null;
        }
        result.put("userDirectories", userDirectories);
        
        return result;
    }

}
