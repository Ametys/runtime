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
package org.ametys.plugins.core.right.profile;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.right.RightManager;
import org.ametys.core.script.SqlTablesInit;

/**
 * Creates the READER profile at initialization.
 */
public class CreateReaderProfileInit extends SqlTablesInit
{
    private RightManager _rightManager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);
        _rightManager = (RightManager) manager.lookup(RightManager.ROLE);
    }
    
    @Override
    public void init() throws Exception
    {
        // First create the needed tables for rights, in case the Init extension "org.ametys.core.script.profile.SqlTablesInit" is not called before this one
        super.init();
        
        // Then do the job: create the reader profile
        if (_rightManager.getProfile(RightManager.READER_PROFILE_ID) != null)
        {
            // already exist
            getLogger().info("READER profile already exists, it will not be created");
            return;
        }
        
        getLogger().info("Creating READER profile");
        String profileName = RightManager.READER_PROFILE_ID; // We give the same name as its id
        _rightManager.addProfile(profileName, RightManager.READER_PROFILE_ID, null);
    }
}
