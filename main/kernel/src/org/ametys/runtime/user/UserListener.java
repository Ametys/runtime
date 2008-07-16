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

/**
 * Listener for users
 */
public interface UserListener
{
    /**
     * When an user is removed
     * @param login the user's login
     */
    public void userRemoved(String login);
    
    /**
     * When an user is added
     * @param login the user's login
     */
    public void userAdded(String login);
    
    /**
     * When an user is updated
     * @param login the user's login
     */
    public void userUpdated(String login);
}
