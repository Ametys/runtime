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
package org.ametys.runtime.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

/**
 * Action which gets and returns a parent context attribute value.
 */
public class GetParentContextAttributeAction extends AbstractAction
{

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map parentContextAttr = (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        Map<String, String> result = new HashMap<String, String>();
        
        String attributes = parameters.getParameter("attributes", "");
        
        String[] attributeArr = StringUtils.split(attributes, ", ");
        for (String attribute : attributeArr)
        {
            String value = StringUtils.defaultString((String) parentContextAttr.get(attribute));
            result.put(attribute, value);
        }
        
        return result;
    }

}
