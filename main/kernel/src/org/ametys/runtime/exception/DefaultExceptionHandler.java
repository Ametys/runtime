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

import java.io.IOException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.excalibur.source.impl.FileSource;

/**
 * Simple ExceptionHandler pointing to the default error XSL.<br>
 * In <code>WEB-INF/stylesheets/error</code> or in the runtime jar in <code>pages/error/error.xsl</code>
 */
public class DefaultExceptionHandler extends AbstractLogEnabled implements ExceptionHandler, ThreadSafe, Contextualizable
{
    private org.apache.cocoon.environment.Context _cocoonContext;
    
    /**
     * Constructor as component
     */
    public DefaultExceptionHandler()
    {
        // empty
    }
    
    /**
     * Constructor for default behavior when application is not starting
     * @param context The cocoon context
     */
    DefaultExceptionHandler(org.apache.cocoon.environment.Context context)
    {
        _cocoonContext = context;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    public String getExceptionXSLURI(String code)
    {
        String uri = null;
        try
        {
            uri = "file://" + _cocoonContext.getRealPath("WEB-INF/stylesheets/error/error_" + code + ".xsl");
            if (new FileSource(uri).exists())
            {
                return uri;
            }
        }
        catch (IOException e)
        {
            getLogger().warn("Unable to find XSL error '" + uri + "'", e);
        }
        
        return "kernel://pages/error/error.xsl";
    }
}
