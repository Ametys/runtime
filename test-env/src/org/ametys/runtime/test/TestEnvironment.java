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
package org.ametys.runtime.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.commandline.CommandLineRequest;
import org.apache.cocoon.environment.commandline.CommandLineResponse;

class TestEnvironment extends AbstractEnvironment
{
    TestEnvironment(String uri, Map requestParameters, Map requestAttributes, Map requestHeaders, Context ctx, Logger logger) throws MalformedURLException
    {
        super(uri, null, ctx.getRealPath("/"), null);
        enableLogging(logger);
        
        objectModel.put(ObjectModelHelper.REQUEST_OBJECT, new CommandLineRequest(this, ctx.getRealPath("/"), "", "", requestAttributes, requestParameters, requestHeaders));
        objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, new CommandLineResponse());
        objectModel.put(ObjectModelHelper.CONTEXT_OBJECT, ctx);
    }
    
    public String getContentType()
    {
        return null;
    }

    public boolean isExternal()
    {
        return false;
    }

    public void redirect(boolean sessionmode, String url) throws IOException
    {
        // empty
    }

    public void setContentLength(int length)
    {
        // empty
    }

    public void setContentType(String mimeType)
    {
        // empty
    }
}
