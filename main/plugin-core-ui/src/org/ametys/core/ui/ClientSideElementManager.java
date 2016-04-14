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

import java.util.ArrayList;
import java.util.List;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * Implementation of an ExtensionPoint for client side elements.
 */
public class ClientSideElementManager extends AbstractThreadSafeComponentExtensionPoint<ClientSideElement>
{
    /**
     * Find a dependency of this manager from the Client side elements it knows. 
     * @param pattern The matching pattern to find the dependency.
     * @return The dependency, or null if no Client side element matched.
     */
    public List<ClientSideElement> findDependency(String pattern)
    {
        ClientSideElement extension = getExtension(pattern);
        
        if (extension == null)
        {
            throw new IllegalArgumentException("Unable to find dependency with id : " + pattern + ".");
        }
        
        List<ClientSideElement> result = new ArrayList<>();
        result.add(extension);
        return result;
    }
}
