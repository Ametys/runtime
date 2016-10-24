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
package org.ametys.runtime.plugin;

import java.util.Collection;

/**
 * Exception thrown by the {@link PluginsManager} when plugins loading fails.<br>
 * It is only the case when the safe mode itself fails to load.
 */
public class PluginException extends RuntimeException
{
    private Collection<PluginIssue> _errors;
    private Collection<PluginIssue> _safeModeErrors;
    
    /**
     * Constructor.
     * @param message the detail message.
     * @param errors the errors gathered by the PluginsManager while loading plugins
     * @param safeModeErrors the errors gathered by the PluginsManager while loading the safe mode.
     */
    public PluginException(String message, Collection<PluginIssue> errors, Collection<PluginIssue> safeModeErrors)
    {
        super(message);
        _errors = errors;
        _safeModeErrors = safeModeErrors;
    }
    
    /**
     * Constructor.
     * @param message the detail message.
     * @param cause the cause, if any.
     * @param errors the errors gathered by the PluginsManager while loading plugins
     * @param safeModeErrors the errors gathered by the PluginsManager while loading the safe mode.
     */
    public PluginException(String message, Throwable cause, Collection<PluginIssue> errors, Collection<PluginIssue> safeModeErrors)
    {
        super(message, cause);
        _errors = errors;
        _safeModeErrors = safeModeErrors;
    }
    
    /**
     * Returns errors gathered by the PluginsManager while loading plugins.
     * @return errors gathered by the PluginsManager while loading plugins.
     */
    public Collection<PluginIssue> getErrors()
    {
        return _errors;
    }
    
    /**
     * Returns errors gathered by the PluginsManager while loading the safe mode.
     * @return errors gathered by the PluginsManager while loading the safe mode.
     */
    public Collection<PluginIssue> getSafeModeErrors()
    {
        return _safeModeErrors;
    }
}
