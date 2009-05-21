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
package org.ametys.runtime.plugin;

/**
 * Interface for runtime initialization classes
 */
public interface Init
{
    /** Avalon Role */
    public static final String ROLE = Init.class.getName();

    /**
     * Method to be implemented by user Init class. Here you should manage the
     * business part of your application init.
     * As an Avalon component, class extending this interface have access to all Cocoon-managed components, all plugins, ...
     * @throws Exception if an error occurs
     */
    public void init() throws Exception;
}
