/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.plugins.core.right;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.right.Right;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.util.I18nUtils;

/**
 * Get rights
 *
 */
public class GetRightsAction extends ServiceableAction
{
    private RightsExtensionPoint _rights;
    private I18nUtils _i18nUtils;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rights = (RightsExtensionPoint) m.lookup(RightsExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) m.lookup(I18nUtils.ROLE);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> rights = new ArrayList<>();
        Set<String> rightIds = _rights.getExtensionsIds();
        
        String rightQuery = (String) jsParameters.get("query");
        if (StringUtils.isEmpty(rightQuery))
        {
            // Return all rights
            for (String rightId : rightIds)
            {
                rights.add(_rights.getExtension(rightId).toJSON());
            }
        }
        else
        {
            // Only return the matching rights
            rightQuery = StringUtils.stripAccents(rightQuery.toLowerCase());
            for (String rightId : rightIds)
            {
                Right right = _rights.getExtension(rightId);
                String rightLabel = StringUtils.stripAccents(_i18nUtils.translate(right.getLabel()).toLowerCase());
                String rightDescription = StringUtils.stripAccents(_i18nUtils.translate(right.getDescription()).toLowerCase());
                if (rightLabel.contains(rightQuery) || (rightDescription.contains(rightQuery)))
                {
                    rights.add(right.toJSON());
                }
            }
        }
        
        result.put("rights", rights);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);

        return EMPTY_MAP;
    }
}
