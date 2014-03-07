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
package org.ametys.runtime.request;

import org.apache.cocoon.environment.Redirector;

/**
 * Component executed on the beginning of each request.<br>
 * It can be used to :<br>
 *  - Initializes some components<br>
 *  - Initializes some values in the request<br>
 *  - Redirects to another URL
 */
public interface InitRequestHandler
{
    /** Avalon Role */
    public static final String ROLE = InitRequestHandler.class.getName();
    
    /**
     * Implement this method to perform any operation before actual processing of the request.
     * @param redirector the Cocoon redirector
     * @throws Exception if an error occurred
     */
    public void initRequest(Redirector redirector) throws Exception;
}
