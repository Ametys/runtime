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
package org.ametys.runtime.plugin.component;

/**
 * Components acception this marker interface indicate that they want
 * to have a reference to their parent.
 * This is for example used for selectors.
 * Note: For the current implementation to work, the parent aware 
 * component and the parent have to be both ThreadSafe!
 */
public interface ParentAware
{
    /**
     * Set the parent component
     * @param parentComponent the parent component
     */
    void setParent(Object parentComponent);
}
