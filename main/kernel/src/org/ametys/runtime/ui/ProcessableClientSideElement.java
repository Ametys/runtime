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
package org.ametys.runtime.ui;

import java.util.Map;

/**
 * A client side element that is interactive with the client side
 */
public interface ProcessableClientSideElement extends ClientSideElement
{
    /**
     * This method process a request from the client side element and returns the results to the element script class.
     * The parameters returns may depend on the current environment.
     * @param parameters The parameters transmitted by the client side script
     * @return a map of parameters. Key represents ids of the parameters and values represents its values. Can not be null.
     */
    public Map<String, Object> process(Map<String, Object> parameters);
}
