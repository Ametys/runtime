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
package org.ametys.plugins.core.user;

import java.util.Map;

import org.ametys.core.ui.StaticClientSideElement;

/**
 * This implementation creates a control only available if the current user is the super user
 */
public class ImpersonateUserClientSideElement extends StaticClientSideElement
{
    @Override
    public Script getScript(Map<String, Object> contextParameters)
    {
        if (_currentUserProvider.isSuperUser())
        {
            return super.getScript(contextParameters);
        }
        
        return null;
    }
}
