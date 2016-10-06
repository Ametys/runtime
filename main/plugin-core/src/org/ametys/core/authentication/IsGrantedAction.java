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
package org.ametys.core.authentication;

import java.util.Collections;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This action return EMPTY_MAP or null depending if the request was granted or not.
 * The effect can be revert by a configuration parameter "is-granted"
 */
public class IsGrantedAction implements Configurable, Action
{
    private boolean _isGranted;

    public void configure(Configuration configuration) throws ConfigurationException
    {
        _isGranted = "true".equals(configuration.getChild("is-granted").getValue("true"));   
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Boolean granted = (Boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_GRANTED);
        if (Boolean.TRUE.equals(granted) && _isGranted
                || !(Boolean.TRUE.equals(granted)) && !_isGranted)
        {
            return Collections.EMPTY_MAP;
        }
        else
        {
            return null;
        }
    }
}
