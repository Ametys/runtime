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
package org.ametys.runtime.group;

/**
 * Listener for group
 */
public interface GroupListener
{
    /**
     * When a group is removed
     * @param groupID the group id
     */
    public void groupRemoved(String groupID);
    
    /**
     * When a group is added
     * @param groupID the group id
     */
    public void groupAdded(String groupID);
    
    /**
     * When a group is updated
     * @param groupID the group id
     */
    public void groupUpdated(String groupID);
}
