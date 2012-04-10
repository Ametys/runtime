package org.ametys.runtime.authentication;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.authentication.AccessDeniedException;

/**
 * Checks that the current user is authenticated or throw an {@link AccessDeniedException}.
 */
public class CheckAuthenticationAction extends AbstractAction
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        
        if (session == null || session.getAttribute(AuthenticateAction.SESSION_USERLOGIN) == null)
        {
            // user is not authenticated
            throw new AccessDeniedException("The requested URL '" + request.getSitemapURI() + "' could only be issued by an authenticated user.");
        }
        
        return null;
    }
}
