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

import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Superclass of all known extension points. <br>
 * <br>
 * This class contains all required methods to build an extension point :<br>
 * <ul>
 * <li>configure(Configuration) to configure the extension point itself
 * <li>configureExtension(Configure) to add an extension to this point
 * </ul>
 * @param <T> the type of the managed extensions
 */
public interface ExtensionPoint<T>
{
    /**
     * Add an extension to this point. Each implementation knows the meaning of
     * the given configuration.
     * @param pluginName Unique identifier for the plugin hosting the extension
     * @param featureName Unique feature identifier (unique for a given pluginName)
     * @param configuration the information about the extension to be added
     * @throws ConfigurationException when a configuration problem occurs
     */
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException;
    
    /**
     * Finalize the initialization of the extensions.<br>
     * This method is called after all <code>addExtension()</code> calls.<br>
     * This is the last step before the actual startup of the application.
     * @throws Exception if something wrong occurs
     */
    public void initializeExtensions() throws Exception;
    
    /**
     * Returns true if the named extension exists
     * @param id the unique id of the extension
     * @return true if the named extension exists
     */
    public boolean hasExtension(String id);
    
    /**
     * Returns the named extension
     * @param id the unique id of the extension
     * @return the named extension
     */
    public T getExtension(String id);
    
    /**
     * Returns a Set containing the ids of all known extensions
     * @return a Set containing the ids of all known extensions
     */
    public Set<String> getExtensionsIds();
}
