/*
 *  Copyright 2015 Anyware Services
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

import org.apache.avalon.framework.logger.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * SLF4J Logger wrapping an Avalon {@link Logger}.
 */
public class AvalonLoggerAdapter extends MarkerIgnoringBase
{
    private org.apache.avalon.framework.logger.Logger _logger;
    
    /**
     * Constructor
     * @param logger the Avalon {@link Logger}.
     */
    public AvalonLoggerAdapter(org.apache.avalon.framework.logger.Logger logger)
    {
        _logger = logger;
    }

    public boolean isTraceEnabled()
    {
        return _logger.isDebugEnabled();
    }

    public void trace(String msg)
    {
        _logger.debug(msg);
    }

    public void trace(String format, Object arg)
    {
        if (_logger.isDebugEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            _logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void trace(String format, Object arg1, Object arg2)
    {
        if (_logger.isDebugEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            _logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void trace(String format, Object... arguments)
    {
        if (_logger.isDebugEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            _logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void trace(String msg, Throwable t)
    {
        _logger.debug(msg, t);
    }

    public boolean isDebugEnabled()
    {
        return _logger.isDebugEnabled();
    }

    public void debug(String msg)
    {
        _logger.debug(msg);
    }

    public void debug(String format, Object arg)
    {
        if (_logger.isDebugEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            _logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void debug(String format, Object arg1, Object arg2)
    {
        if (_logger.isDebugEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            _logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void debug(String format, Object... arguments)
    {
        if (_logger.isDebugEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            _logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void debug(String msg, Throwable t)
    {
        _logger.debug(msg, t);
    }

    public boolean isInfoEnabled()
    {
        return _logger.isInfoEnabled();
    }

    public void info(String msg)
    {
        _logger.info(msg);
    }

    public void info(String format, Object arg)
    {
        if (_logger.isInfoEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            _logger.info(ft.getMessage(), ft.getThrowable());
        }
    }

    public void info(String format, Object arg1, Object arg2)
    {
        if (_logger.isInfoEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            _logger.info(ft.getMessage(), ft.getThrowable());
        }
    }

    public void info(String format, Object... arguments)
    {
        if (_logger.isInfoEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            _logger.info(ft.getMessage(), ft.getThrowable());
        }
    }

    public void info(String msg, Throwable t)
    {
        _logger.info(msg, t);
    }

    public boolean isWarnEnabled()
    {
        return _logger.isWarnEnabled();
    }

    public void warn(String msg)
    {
        _logger.warn(msg);
    }

    public void warn(String format, Object arg)
    {
        if (_logger.isWarnEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            _logger.warn(ft.getMessage(), ft.getThrowable());
        }
    }

    public void warn(String format, Object... arguments)
    {
        if (_logger.isWarnEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            _logger.warn(ft.getMessage(), ft.getThrowable());
        }
    }

    public void warn(String format, Object arg1, Object arg2)
    {
        if (_logger.isWarnEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            _logger.warn(ft.getMessage(), ft.getThrowable());
        }
    }

    public void warn(String msg, Throwable t)
    {
        _logger.warn(msg, t);
    }

    public boolean isErrorEnabled()
    {
        return _logger.isErrorEnabled();
    }

    public void error(String msg)
    {
        _logger.error(msg);
    }

    public void error(String format, Object arg)
    {
        if (_logger.isErrorEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            _logger.error(ft.getMessage(), ft.getThrowable());
        }
    }

    public void error(String format, Object arg1, Object arg2)
    {
        if (_logger.isErrorEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            _logger.error(ft.getMessage(), ft.getThrowable());
        }
    }

    public void error(String format, Object... arguments)
    {
        if (_logger.isErrorEnabled()) 
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            _logger.error(ft.getMessage(), ft.getThrowable());
        }
    }

    public void error(String msg, Throwable t)
    {
        _logger.error(msg, t);
    }
}
