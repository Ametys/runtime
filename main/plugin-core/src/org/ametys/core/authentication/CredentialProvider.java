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

import org.apache.cocoon.environment.Redirector;

import org.ametys.core.user.UserIdentity;

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
    
    /**
     * Method called by AuthenticateAction before asking for credentials. This
     * method is used to bypass authentication. If this method returns true, no
     * authentication will be required. Use it with care, as it may lead to
     * obvious security issues.
     * @param blockingkMode true to use the blocking mode of the credential provider if available, false to use the non blocking mode if available 
     * @return true if the Request does not need to be authenticated
     */
    public default boolean grantAnonymousRequest(boolean blockingkMode)
    {
        if (!blockingkMode && this instanceof NonBlockingCredentialProvider)
        {
            return ((NonBlockingCredentialProvider) this).nonBlockingGrantAnonymousRequest();
        }
        else if (blockingkMode && this instanceof BlockingCredentialProvider)
        {
            return ((BlockingCredentialProvider) this).blockingGrantAnonymousRequest();
        }
        else
        {
            return false;
        }
    }

    /**
     * Validates that the user specify is still connected
     * @param userCurrentlyConnected the user previously correctly identified with this credential provider
     * @param blockingkMode true to use the blocking mode of the credential provider if available, false to use the non blocking mode if available 
     * @param redirector The cocoon redirector
     * @return true if this CredentialProvider was in a valid state, false to restart authentication process
     * @throws Exception If an error occurred
     */
    public default boolean isStillConnected(boolean blockingkMode, UserIdentity userCurrentlyConnected, Redirector redirector) throws Exception
    {
        if (!blockingkMode && this instanceof NonBlockingCredentialProvider)
        {
            return ((NonBlockingCredentialProvider) this).nonBlockingIsStillConnected(userCurrentlyConnected, redirector);
        }
        else if (blockingkMode && this instanceof BlockingCredentialProvider)
        {
            return ((BlockingCredentialProvider) this).blockingIsStillConnected(userCurrentlyConnected, redirector);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Method called by AuthenticateAction each time a request need
     * authentication.
     * @param blockingkMode true to use the blocking mode of the credential provider if available, false to use the non blocking mode if available 
     * @param redirector the cocoon redirector.
     * @return the <code>UserIdentity</code> corresponding to the user (with or without population specified), or null if user could not get authenticated.
     * @throws Exception If an error occurred
     */
    public default UserIdentity getUserIdentity(boolean blockingkMode, Redirector redirector) throws Exception
    {
        if (!blockingkMode && this instanceof NonBlockingCredentialProvider)
        {
            return ((NonBlockingCredentialProvider) this).nonBlockingGetUserIdentity(redirector);
        }
        else if (blockingkMode && this instanceof BlockingCredentialProvider)
        {
            return ((BlockingCredentialProvider) this).blockingGetUserIdentity(redirector);
        }
        else
        {
            return null;
        } 
    }

    /**
     * Method called by AuthenticateAction each a user could not get
     * authenticated. This method implementation is responsible of redirecting
     * response to appropriate url.
     * @param blockingkMode true to use the blocking mode of the credential provider if available, false to use the non blocking mode if available 
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     * @throws Exception if something wrong occurs
     */
    public default void userNotAllowed(boolean blockingkMode, Redirector redirector) throws Exception
    {
        if (!blockingkMode && this instanceof NonBlockingCredentialProvider)
        {
            ((NonBlockingCredentialProvider) this).nonBlockingUserNotAllowed(redirector);
        }
        else if (blockingkMode && this instanceof BlockingCredentialProvider)
        {
            ((BlockingCredentialProvider) this).blockingUserNotAllowed(redirector);
        }
    }

    /**
     * Method called by AuthenticateAction after authentication process succeeded
     * @param blockingkMode true to use the blocking mode of the credential provider if available, false to use the non blocking mode if available 
     * @param userIdentity The user correctly connected
     */
    public default void userAllowed(boolean blockingkMode, UserIdentity userIdentity)
    {
        if (!blockingkMode && this instanceof NonBlockingCredentialProvider)
        {
            ((NonBlockingCredentialProvider) this).nonBlockingUserAllowed(userIdentity);
        }
        else if (blockingkMode && this instanceof BlockingCredentialProvider)
        {
            ((BlockingCredentialProvider) this).blockingUserAllowed(userIdentity);
        }

    }
}
