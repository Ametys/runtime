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
package org.ametys.core.user.directory;

import java.util.Collection;
import java.util.Map;

import org.ametys.core.user.InvalidModificationException;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Abstraction for getting users list and verify the presence of a particular
 * user and finally modifying this list.
 */
public interface ModifiableUserDirectory extends UserDirectory
{
    /**
     * Add a new user to the list.
     * @param userInformation Informations about the user, see implementation. Cannot be null.
     * @throws InvalidModificationException if the login exists yet or
     *         if at least one of the parameter is invalid. 
     */
    public void add(Map<String, String> userInformation) throws InvalidModificationException;

    /**
     * Modify informations about an user of the list.
     * @param userInformation New informations about the user, see implementation. Cannot be null.
     * @throws InvalidModificationException if the login does not match
     *         in the list or if at least one of the parameter is invalid. 
     */
    public void update(Map<String, String> userInformation) throws InvalidModificationException;

    /**
     * Remove an user from the list.
     * @param login The user's login. Cannot be null.
     * @throws InvalidModificationException if the user cannot be removed
     */
    public void remove(String login) throws InvalidModificationException;
    
    /**
     * Validate user information.
     * @param userInformation Informations about the user, see implementation. Cannot be null.
     * @return validation errors.
     */
    public Map<String, Errors> validate(Map<String, String> userInformation);
    
    /**
     * Get the user's edition model as a Collection of parameters.
     * @return the user's edition model as a Collection of parameters.
     */
    public Collection<? extends Parameter<ParameterType>> getModel();
}
