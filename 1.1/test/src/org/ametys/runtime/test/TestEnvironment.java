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

@SuppressWarnings("unchecked")
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
