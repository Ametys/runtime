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
package org.ametys.runtime.plugins.core.administrator.version;

import java.util.Collection;

/**
 * Administrator component able to determine the content of the "Versions" panel in the administrator area.<br>
 * It may includes plugins versions, Runtime version, application version, ...
 */
public interface VersionsHandler
{
    /** Avalon Role */
    public static final String ROLE = VersionsHandler.class.getName();
    
    /**
     * Returns all Versions to be displayed in the administrator area
     * @return all Versions to be displayed in the administrator area
     */
    public Collection<Version> getVersions();
}
