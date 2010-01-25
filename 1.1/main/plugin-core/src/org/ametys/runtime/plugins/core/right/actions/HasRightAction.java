/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.right.actions;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.UserHelper;


/**
 * This action determines if the user has a right given in src.<br/>
 * return EMPTY_MAP if the user has right and null otherwise<br/>
 * You can use the 'context' parameter to specify the right context. / is the default value.
 */
public class HasRightAction extends ServiceableAction implements Configurable, ThreadSafe
{
    /** The runtime rights manager */
    protected RightsManager _rightsManager;
    
    private boolean _hasRight;
    
    private String _baseContext;
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _hasRight = "true".equals(configuration.getChild("has-right").getValue("true"));
        _baseContext = configuration.getChild("base-context").getValue("/application");
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
        if (_rightsManager == null)
        {
            _rightsManager = (RightsManager) manager.lookup(RightsManager.ROLE);
        }

        if (UserHelper.isAdministrator(objectModel))
        {
            return _hasRight ? EMPTY_MAP : null;
        }

        boolean hasRight = false;
        String context = parameters.getParameter("context", null);
        if (context == null)
        {
            context = getBaseContext(parameters, objectModel);
        }
        
        String userLogin = UserHelper.getCurrentUser(objectModel);
        if (userLogin == null)
        {
            getLogger().error("Annonymous user tried to access a privileged feature without convenient right. Should have in right between those : '" + source + "' on context '" + context + "'");
            throw new IllegalStateException("You have no right to access this feature.");
        }
        else
        {
            String[] rigths = source.split("\\|");
            for (int i = 0; i < rigths.length; i++) 
            {
                String right = rigths[i].trim();

                if (_rightsManager.hasRight(userLogin, right, context) == RightsManager.RightResult.RIGHT_OK)
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
                getLogger().error("User '" + userLogin + "' tried to access a privileged feature without convenient right. Should have in right between those : '" + source + "' on context '" + context + "'");
                throw new IllegalStateException("You have no right to access this feature.");
            }
        }
    }

}
