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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Simple ExceptionHandler pointing to the default error XSL.<br>
 * In the runtime jar in <code>pages/error/error.xsl</code>
 */
public class DefaultExceptionHandler extends AbstractLogEnabled implements ExceptionHandler, ThreadSafe
{
    public String getExceptionXSLURI(String code)
    {
        return "resource://org/ametys/runtime/kernel/pages/error/error.xsl";
    }
}
