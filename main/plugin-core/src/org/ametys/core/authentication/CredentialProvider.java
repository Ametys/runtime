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
package org.ametys.core.authentication;

import java.util.Map;

/**
 * Defines the authentication mode of users.
 * Implementations may cover HTTP authentication, SSO, ...
 * DO NOT implement this interface, implements either {@link BlockingCredentialProvider}, 
 * either {@link NonBlockingCredentialProvider} or both.
 */
public interface CredentialProvider
{
    /**
     * Get the id of the {@link CredentialProviderModel} extension point
     * @return the id of extension point
     */
    public String getCredentialProviderModelId();
    
    /**
     * Get the values of parameters (from credential provider model)
     * @return the parameters' values
     */
    public Map<String, Object> getParameterValues();
    
    /**
     * Initialize the credential provider with given parameters' values.
     * @param cpModelId The id of credential provider extension point
     * @param paramValues The parameters' values
     * @throws Exception If an error occured
     */
    public void init(String cpModelId, Map<String, Object> paramValues) throws Exception;
}
