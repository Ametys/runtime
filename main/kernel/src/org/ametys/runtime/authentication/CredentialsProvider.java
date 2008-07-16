/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.authentication;

import org.apache.cocoon.environment.Redirector;

/**
 * Abstract superclass of all AuthenticationManager. <br>
 * The role of an AuthenticationManager is to provide system with user
 * credentials. <br>
 * Implementations may cover HTTP authentication, SSO, ...
 */
public interface CredentialsProvider
{
    /** Avalon role */
    public static final String ROLE = CredentialsProvider.class.getName();
    
    /**
     * Validates this AuthenticationManager.
     * It may declares itself as invalid, due to some environment status.<br>
     * In this cas, the whole authentication process is restarted.
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     * @return true if this AuthenticationManager was in a valid state, false otherwise
     * @throws Exception if something wrong occurs
     */
    public abstract boolean validate(Redirector redirector) throws Exception;

    /**
     * Mathod called by AuthenticateAction before asking for credentials. This
     * method is used to bypass authentication. If this method returns true, no
     * authentication will be require. Use it with care, as it may lead to
     * obvious security issues.
     * 
     * @return true if the Request is not authenticated
     */
    public abstract boolean accept();

    /**
     * Method called by AuthenticateAction each time a request need
     * authentication.
     * 
     * @param redirector the cocoon redirector.
     * @return the <code>Credentials</code> corresponding to the user, or null if user could not get authenticated.
     * @throws Exception if something wrong occurs
     */
    public abstract Credentials getCredentials(Redirector redirector) throws Exception;

    /**
     * Method called by AuthenticateAction each a user could not get
     * authenticated. This method implementation is responsible of redirecting
     * response to appropriate url.
     * 
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     * @throws Exception if something wrong occurs
     */
    public abstract void notAllowed(Redirector redirector) throws Exception;

    /**
     * Mathod called by AuthenticateAction after authentication process
     * succeeded
     * 
     * @param redirector the cocoon Redirector that can be used for redirecting response.
     */
    public abstract void allowed(Redirector redirector);
}
