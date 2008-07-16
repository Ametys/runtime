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
 * Used by components needing to know their declaring plugin.
 */
public interface PluginAware
{
    /**
     * Sets the plugin info relative to the current component.<br>
     * <i>Note : </i>The feature name may be null if the targeted component in declared at plugin level.
     * @param pluginName Unique identifier for the plugin hosting the extension
     * @param featureName Unique feature identifier (unique for a given pluginName)
     */
    public void setPluginInfo(String pluginName, String featureName);
}
