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

package org.ametys.runtime.plugins.core.dispatcher;

import org.apache.cocoon.environment.Request;

/**
 * This is a process after a request in the dispatch generator
 */
public interface DispatchRequestPostProcess
{
    /**
     * Process the request
     * @param request The request to process
     */
    public void process(Request request);
}
