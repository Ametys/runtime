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
package org.ametys.runtime.util.cocoon;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.generation.ServiceableGenerator;

import org.ametys.runtime.user.CurrentUserProvider;

/**
 * {@link ServiceableGenerator} which provides the current user if necessary.
 */
public abstract class AbstractCurrentUserProviderServiceableGenerator extends ServiceableGenerator
{
    /** The current user provider. */
    protected CurrentUserProvider _currentUserProvider;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
    }

    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    protected boolean _isSuperUser()
    {
        return _currentUserProvider.isSuperUser();
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected String _getCurrentUser()
    {
        if (!_currentUserProvider.isSuperUser())
        {
            return _currentUserProvider.getUser();
        }

        return "admin";
    }
}
