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
package org.ametys.runtime.right;

/**
 * This right manager support an initialisation that grant all privileges to a user .<br/>
 * Extends this implementation if you want that the administrator to be able to initialize your right manager.<br/>
 * If your right manager doest not implement this interface, the administrator will have to set the initial rights another way.
 */
public interface InitializableRightsManager extends RightsManager
{
    /**
     * This method has to ensure that the user identified by its login will have all power.
     * @param login The login of the user that will obtain all privilege on the right manager.
     * @param context The context of the right (cannot be null)
     */
    public void grantAllPrivileges(String login, String context);
}
