/*
 *  Copyright 2010 Anyware Services
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

import java.util.ArrayList;
import java.util.List;

/**
 * This extension point handle the existing ribbon controls.
 */
public class MessageTargetFactoriesManager extends AbstractClientSideExtensionPoint
{
    /** Avalon role */
    public static final String ROLE = MessageTargetFactoriesManager.class.getName();
    
    @Override
    public List<ClientSideElement> findDependency(String pattern)
    {
        ClientSideElement extension = getExtension(pattern);
        
        if (extension == null)
        {
            getLogger().info("Invalid message target factory id : " + pattern + ". Unable to find dependency.");
        }
        
        List<ClientSideElement> result = new ArrayList<>();
        result.add(extension);
        return result;
    }
}
