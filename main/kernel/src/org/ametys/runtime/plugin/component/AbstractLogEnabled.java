package org.ametys.runtime.plugin.component;

import org.slf4j.Logger;

/**
 * Abstract implementation of LogEnabled for Ametys components.
 */
public abstract class AbstractLogEnabled implements LogEnabled
{
    private Logger _logger;

    public Logger getLogger()
    {
        return _logger;
    }

    public void setLogger(Logger logger)
    {
        _logger = logger;
    }
}
