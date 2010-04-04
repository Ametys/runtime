/*
 *  Copyright 2010 Anyware Services
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
/*
 *
 */
package org.ametys.runtime.util;

import org.apache.avalon.framework.logger.Logger;
import org.slf4j.ILoggerFactory;

/**
 * Avalon {@link Logger} wrapping a SLF4J Logger.
 */
public class SLF4JLogger implements Logger
{
    private ILoggerFactory _iLoggerFactory;
    private org.slf4j.Logger _logger;
    
    /**
     * Constructor.
     * @param logger the wrapped SLF4J logger.
     * @param iLoggerFactory the SLF4J {@link ILoggerFactory}.
     */
    public SLF4JLogger(org.slf4j.Logger logger, ILoggerFactory iLoggerFactory)
    {
        _logger = logger;
        _iLoggerFactory = iLoggerFactory;
    }

    @Override
    public void debug(String message)
    {
        _logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable)
    {
        _logger.debug(message, throwable);
    }

    @Override
    public void error(String message)
    {
        _logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable)
    {
        _logger.error(message, throwable);
    }

    @Override
    public void fatalError(String message)
    {
        _logger.error(message);
    }

    @Override
    public void fatalError(String message, Throwable throwable)
    {
        _logger.error(message, throwable);
    }

    @Override
    public Logger getChildLogger(String name)
    {
        return new SLF4JLogger(_iLoggerFactory.getLogger(_logger.getName() + "." + name), _iLoggerFactory);
    }

    @Override
    public void info(String message)
    {
        _logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable)
    {
        _logger.info(message, throwable);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return _logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled()
    {
        return _logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalErrorEnabled()
    {
        return _logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled()
    {
        return _logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled()
    {
        return _logger.isWarnEnabled();
    }

    @Override
    public void warn(String message)
    {
        _logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable)
    {
        _logger.warn(message, throwable);
    }
}
