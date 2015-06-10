/*
 *  Copyright 2012 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ametys.core.util;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;

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
