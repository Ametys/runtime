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

import org.apache.cocoon.environment.Redirector;

/**
 * Component executed on the beginning of each request.<br>
 * It can be used to :<br>
 *  - Initializes some components<br>
 *  - Initializes some values in the request<br>
 *  - Redirects to another URL
 */
public interface InitRequestHandler
{
    /** Avalon Role */
    public static final String ROLE = InitRequestHandler.class.getName();
    
    /**
     * Implement this method to perform any operation before actual processing of the request.
     * @param redirector the Cocoon redirector
     * @throws Exception if an error occurred
     */
    public void initRequest(Redirector redirector) throws Exception;
}
