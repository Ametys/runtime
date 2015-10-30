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
package org.ametys.core.sqlmap;


/**
 * Provide SqlMap to a component.<br>
 * This component must implements <code>SqlMapClientSupport</code>.
 */
public interface SqlMapClientComponentProvider
{
    /**
     * Retrieve the role to use for accessing the component which
     * will be injected with SqlMap.
     * @return the avalon role of the component.
     */
    public String getComponentRole();
}