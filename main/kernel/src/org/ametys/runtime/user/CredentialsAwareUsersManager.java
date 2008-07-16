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
package org.ametys.runtime.user;

import org.ametys.runtime.authentication.Credentials;

/**
 * Abstraction for getting users list and verify the presence of a particular
 * user and authenticating users.
 */
public interface CredentialsAwareUsersManager extends UsersManager
{
    /**
     * Authenticate a user with its credentials
     * @param credentials the credentials of the user. Cannot be null.
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean checkCredentials(Credentials credentials);
}
