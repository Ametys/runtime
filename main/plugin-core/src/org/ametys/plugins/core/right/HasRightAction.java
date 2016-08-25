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
package org.ametys.plugins.core.right;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.right.RightManager;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.util.cocoon.AbstractCurrentUserProviderServiceableAction;
import org.ametys.runtime.authentication.AccessDeniedException;


/**
 * This action determines if the user has a right given in src.<br>
 * return EMPTY_MAP if the user has right and null otherwise<br>
 * You can use the 'context' parameter to specify the right context. / is the default value.
 */
public class HasRightAction extends AbstractCurrentUserProviderServiceableAction implements Configurable
{
    /** The runtime rights manager */
    protected RightManager _rightManager;
    /** Is the action in has right mode? or has not right mode? */
    protected boolean _hasRight;
    /** The right context to use */
    protected String _baseContext;
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _hasRight = "true".equals(configuration.getChild("has-right").getValue("true"));
        _baseContext = configuration.getChild("base-context").getValue("");
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        
        _rightManager = (RightManager) manager.lookup(RightManager.ROLE);
    }
    
    /**
     * Return the base context when not specified
     * @param parameters The sitemap parameters
     * @param objectModel the objectModel of the calling environment 
     * @return the base context when not specified
     */
    protected String getBaseContext(Parameters parameters, Map objectModel)
    {
        return _baseContext;
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        boolean hasRight = false;
        String context = parameters.getParameter("context", null);
        if (context == null || "".equals(context))
        {
            context = getBaseContext(parameters, objectModel);
        }
        
        UserIdentity user = _getCurrentUser();
        if (user == null)
        {
            throw new AccessDeniedException("Anonymous user tried to access a privileged feature without convenient right. Should have in right between those : '" + source + "' on context '" + context + "'");
        }
        else
        {
            String[] rigths = source.split("\\|");
            for (int i = 0; i < rigths.length; i++) 
            {
                String right = rigths[i].trim();

                if (_rightManager.hasRight(user, right, context) == RightManager.RightResult.RIGHT_ALLOW)
                {
                    hasRight = true;
                }
            }
        }
        
        if (hasRight)
        {
            if (_hasRight)
            {
                return EMPTY_MAP;
            }
            else
            {
                return null;
            }
        }
        else
        {
            if (_hasRight)
            {
                return null;
            }
            else
            {
                throw new AccessDeniedException("User '" + user + "' tried to access a privileged feature without convenient right. Should have in right between those : '" + source + "' on context '" + context + "'");
            }
        }
    }

}
