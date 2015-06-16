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

import org.apache.avalon.excalibur.logger.AbstractLoggerManager;
import org.apache.avalon.framework.logger.Logger;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * Interface between LoggerManager and SLF4J.
 */
public class SLF4JLoggerManager extends AbstractLoggerManager
{
    private ILoggerFactory _iloggerFactory;
    
    /**
     * Constructor.
     */
    public SLF4JLoggerManager()
    {
        this(null, null, null);
    }
    
    /**
     * Constructor.
     * @param prefix the prefix to prepended to the category name
     *         on each invocation of getLoggerForCategory before
     *         passing the category name on to the underlying logging
     *         system (currently LogKit or Log4J).
     * @param switchTo fuel for the <code>start()</code> method; 
     *         if null <code>start()</code> will do nothing; 
     *         if empty <code>start()</code> will switch to
     *         <code>getLoggerForCategory("")</code>.
     * @param defaultLoggerOverride the SLF4J {@link ILoggerFactory} to use as default.
     */
    public SLF4JLoggerManager(final String prefix, final String switchTo, Logger defaultLoggerOverride)
    {
        super(prefix, switchTo, defaultLoggerOverride);
        _iloggerFactory = LoggerFactory.getILoggerFactory();
    }
    
    @Override
    protected Logger doGetLoggerForCategory(String fullCategoryName)
    {
        return new SLF4JLogger(_iloggerFactory.getLogger(fullCategoryName), _iloggerFactory);
    }
}
