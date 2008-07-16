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
package org.ametys.runtime.request;

import java.util.Map;

import org.apache.cocoon.environment.Redirector;

/**
 * Default implementation of a InitRequestHandler, simply doing nothing...
 */
public class DefaultInitRequestHandler implements InitRequestHandler
{
    public void initRequest(Redirector redirector, Map objectModel)
    {
        // this implementation does nothing ...
    }
}
