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
package org.ametys.runtime.exception;

/**
 * Component responsible to return appropriate rendering XSL for a given Exception
 */
public interface ExceptionHandler
{
    /** Avalon role */
    public static final String ROLE = ExceptionHandler.class.getName();
        
    /**
     * Returns an URI corresponding to the XSL responsible to render the Exception
     * @param code the HTTP response status code corresponding to the handled exception
     * @return an URI corresponding to the XSL responsible to render the Exception
     */
    public String getExceptionXSLURI(String code);
}
