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

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * This extension point is the pool of interactions 
 * @deprecated
 */
public class UIItemManager extends AbstractThreadSafeComponentExtensionPoint<UIItemFactory>
{
    /** Avalon role */
    public static final String ROLE = UIItemManager.class.getName();
}
