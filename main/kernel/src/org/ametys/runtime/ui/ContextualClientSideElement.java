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
package org.ametys.runtime.ui;

import java.util.Map;

import org.ametys.runtime.util.I18nizableText;

/**
 * A client side element that may change its state upon the context
 */
public interface ContextualClientSideElement extends ClientSideElement
{
    /**
     * This method returns the parameters given to the element script class at a given time.
     * The parameters returns may depend on the current environment.
     * @param parameters The parameters transmitted by the client side script
     * @return a map of parameters. Key represents ids of the parameters and values represents its values. Can not be null.
     */
    public Map<String, I18nizableText> getCurrentParameters(Map<String, Object> parameters);
}
