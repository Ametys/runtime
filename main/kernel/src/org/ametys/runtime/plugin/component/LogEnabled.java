package org.ametys.runtime.plugin.component;

import org.slf4j.Logger;

/**
 * Components that need to log messages.
 */
public interface LogEnabled
{
    /**
     * Returns a {@link Logger}.
     * @return a {@link Logger}.
     */
    public Logger getLogger();
    
    /**
     * Called at creation time to provide a {@link Logger}.
     * @param logger a {@link Logger} for messages.
     */
    public void setLogger(Logger logger);
}
