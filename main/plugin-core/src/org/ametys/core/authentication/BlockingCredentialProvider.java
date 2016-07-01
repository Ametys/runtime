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

import org.apache.cocoon.environment.Redirector;

/**
 * Defines a {@link CredentialProvider} that can be blocking.
 */
public interface BlockingCredentialProvider extends CredentialProvider
{
    /**
     * Validates this CredentialProvider.
     * It may declare as invalid an already connected user
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     * @return true if this CredentialProvider was in a valid state, false to restart authentication process
     * @throws Exception if something wrong occurs
     */
    public abstract boolean validateBlocking(Redirector redirector) throws Exception;

    /**
     * Method called by AuthenticateAction before asking for credentials. This
     * method is used to bypass authentication. If this method returns true, no
     * authentication will be required. Use it with care, as it may lead to
     * obvious security issues.
     * 
     * @return true if the Request is not authenticated
     */
    public abstract boolean acceptBlocking();

    /**
     * Method called by AuthenticateAction each time a request need
     * authentication.
     * 
     * @param redirector the cocoon redirector.
     * @return the <code>Credentials</code> corresponding to the user, or null if user could not get authenticated.
     * @throws Exception if something wrong occurs
     */
    public abstract Credentials getCredentialsBlocking(Redirector redirector) throws Exception;

    /**
     * Method called by AuthenticateAction each a user could not get
     * authenticated. This method implementation is responsible of redirecting
     * response to appropriate url.
     * 
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     * @throws Exception if something wrong occurs
     */
    public abstract void notAllowedBlocking(Redirector redirector) throws Exception;

    /**
     * Method called by AuthenticateAction after authentication process
     * succeeded
     * 
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     */
    public abstract void allowedBlocking(Redirector redirector);

}