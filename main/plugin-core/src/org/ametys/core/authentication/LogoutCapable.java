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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Redirector;

/**
 * Defines a logout capable authentication mode of users.
 */
public interface LogoutCapable
{
    /**
     * Logout a particular user.
     * @param redirector The cocoon redirector if a redirection is required. Only external redirection are correctly supported.
     * @throws ProcessingException If an error occurred (e.g. during redirection)
     */
    public void logout(Redirector redirector) throws ProcessingException;
}
