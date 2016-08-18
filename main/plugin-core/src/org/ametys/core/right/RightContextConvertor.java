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
package org.ametys.core.right;

import java.util.Set;

/**
 * This interface is for getting, from a single object, a set of object. When calling {@link RightManager#hasRight(org.ametys.core.user.UserIdentity, String, Object)},
 * the object is converted into a set of objects and the rights are checked on all those converted objects.
 */
public interface RightContextConvertor
{
    /**
     * Converts the object
     * @param object The initial object
     * @return The converted objects
     */
    public Set<Object> convert(Object object);
}
