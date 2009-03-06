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
package org.ametys.runtime.util;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.log.Hierarchy;

/**
 * Factory for LogKit <code>Loggers</code> that provides automatic setup of
 * log targets and formatters.
 */
public final class LoggerFactory
{
    private static LoggerManager __loggerManager;

    private LoggerFactory()
    {
        // empty private constructor
    }

    /**
     * Get a <code>Logger</code> for a given category.
     * @param category the category to use for traces
     * @return a Logger for traces
     */
    public static final Logger getLoggerFor(String category)
    {
        return __loggerManager.getLoggerForCategory(category);
    }

    /**
     * Returns the logger manager.
     * @return the logger manager.
     */
    public static final LoggerManager getLoggerManager()
    {
        return __loggerManager;
    }

    /**
     * Get a <code>Logger</code> for a given class. The logger category is the fully qualified name of the class. 
     * @param clazz the class used for category
     * @return a Logger for traces
     */
    public static final Logger getLoggerFor(Class clazz)
    {
        return getLoggerFor(clazz.getName());
    }

    /**
     * Set the <code>LoggerManager</code> used for creating Loggers.
     * @param loggerManager the logger manager to use
     */
    public static void setup(LoggerManager loggerManager)
    {
        __loggerManager = loggerManager;
    }
}
