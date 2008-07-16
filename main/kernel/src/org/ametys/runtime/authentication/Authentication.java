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

/**
 * Interface for authentication class in charge of login a user
 */
public interface Authentication
{
    /**
     * Check if a User can log in. This method is not in charge to check the
     * presence and to get the user in the base user.
     * 
     * @param credentials Contains user information (id, password and realm)
     * @return true if the user can login
     */
    public boolean login(Credentials credentials);
}
