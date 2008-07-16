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

import java.util.Collection;

/**
 * Enumerator for parameters values.<br>
 * Such values usually depends on environment (directory listing, ...)
 */
public interface Enumerator
{
    /**
     * Returns a Collection&lt;EnumeratorValue> corresponding to all allowed values for a parameter.
     * @return a not null Collection&lt;EnumeratorValue>
     */
    public Collection<EnumeratorValue> getValues();
}
