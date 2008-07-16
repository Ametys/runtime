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

import javax.servlet.http.HttpServletRequest;

/**
 * Simple request listener.
 */
public interface RequestListener
{
    /**
     * Called at the beginning of the HttpServlet.service() method
     * @param req the request being processed
     */
    public void requestStarted(HttpServletRequest req);
    
    /**
     * Called at the end of the HttpServlet.service() method
     * @param req the processed request
     */
    public void requestEnded(HttpServletRequest req);
}
