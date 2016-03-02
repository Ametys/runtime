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
package org.ametys.core;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.PluginsManager;

/**
 * Static class for retrieving and setting the development mode of the application.
 */
public final class DevMode
{
    /** The session attribute name for storing the dev mode forced by the connected user. */
    public static final String SESSION_ATTRIBUTE_DEVMODE = DevMode.class.toString() + "$DevMode";

    /** The request parameter name for forcing the dev mode for the connected user. */
    public static final String REQUEST_PARAM_FORCE_DEVMODE = "debug.mode";

    private DevMode()
    {
        // static class
    }
    
    /**
     * Check if the current config is in developer mode.
     * @return True if the developer mode is activated.
     */
    public static boolean isDeveloperMode()
    {
        return isDeveloperMode(null);
    }
    
    /**
     * Check if the current config is in developer mode for the current request.
     * @param request The current request. Can be null.
     * @return True if the developer mode is activated.
     */
    public static boolean isDeveloperMode(Request request)
    {
        if (request != null)
        {
            Session session = request.getSession(false);

            String forceDevMode = request.getParameter(REQUEST_PARAM_FORCE_DEVMODE);
            if (forceDevMode != null)
            {
                // There is a request parameter, set the dev mode for the current session.
                boolean devMode = Boolean.valueOf(forceDevMode);
                if (session != null)
                {
                    session.setAttribute(SESSION_ATTRIBUTE_DEVMODE, devMode);
                }
                
                return devMode;
            }
            
            if (session != null)
            {
                // Check if there is already a dev mode set in the session.
                Boolean devMode = (Boolean) session.getAttribute(SESSION_ATTRIBUTE_DEVMODE);
                if (devMode != null)
                {
                    return devMode;
                }
            }
        }
        
        if (PluginsManager.getInstance().isSafeMode())
        {
            return true;
        }
        
        Boolean devMode = Config.getInstance().getValueAsBoolean("runtime.mode.dev");
        return devMode != null ? devMode : false;
    }
}
