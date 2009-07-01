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
package org.ametys.runtime.util.parameter;

import java.util.Map;

import org.ametys.runtime.util.I18nizableText;

/**
 * Enumerator for listing values.<p>
 * Such values usually depends on environment (directory listing, DB table, ...).
 */
public interface Enumerator
{
    /**
     * Retrieves a single label from a value.
     * @param value the value.
     * @return the label or <code>null</code> if not found.
     * @throws Exception if an error occurs.
     */
    public I18nizableText getEntry(String value) throws Exception;
    
    /**
     * Provides the enumerated values with their optional label.
     * @return the enumerated values and their label.
     * @throws Exception if an error occurs.
     */
    public Map<Object, I18nizableText> getEntries() throws Exception;
}
