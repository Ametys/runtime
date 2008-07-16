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
package org.ametys.runtime.ui.item;

import java.util.List;

/**
 * An interaction factory is able to serve (by creating or picking) 'Interaction' 
 * to a manager possibly depending on the current environment.
 */
public interface UIItemFactory
{
    /**
     * Get the interactions that applies to the current content
     * @return A list of UIItems. Can be null if no UIItem match the current context.
     */
    public List<UIItem> getUIItems();
}
