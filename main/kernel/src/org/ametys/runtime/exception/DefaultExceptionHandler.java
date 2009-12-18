/*
 *  Copyright 2009 Anyware Services
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
