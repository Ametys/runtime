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
package org.ametys.core.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * This element creates a button for logging out the currently logged user.
 */
public class LogoutClientSideElement extends StaticClientSideElement
{
    /**
     * Logout the current logged user
     * @return True if the logging out succeeded
     */
    @Callable
    public boolean logout()
    {
        return _currentUserProvider.logout();
    }
    
    /**
     * Check if the current logged user can logout
     * @return True if the current logged user can logout
     */
    public boolean canLogout()
    {
        return _currentUserProvider.canLogout();
    }
    
    @Override
    public List<Script> getScripts(boolean ignoreRights, Map<String, Object> contextParameters)
    {
        if (canLogout())
        {
            return super.getScripts(ignoreRights, contextParameters);
        }
        else
        {
            return new ArrayList<>();
        }
        
    }

}
