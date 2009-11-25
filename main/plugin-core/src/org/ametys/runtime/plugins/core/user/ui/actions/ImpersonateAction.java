package org.ametys.runtime.plugins.core.user.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.authentication.AuthenticateAction;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;

/**
 * This action impersonate the current user with the given login 
 */
public class ImpersonateAction extends CurrentUserProviderServiceableAction
{
    private UsersManager _usersManager;
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, String> result = new HashMap<String, String>();
        
        if (!_isSuperUser())
        {
            throw new IllegalStateException("Current user is not logged as administrator");
        }

        String login = parameters.getParameter("login", null);
        if (StringUtils.isEmpty(login))
        {
            throw new IllegalArgumentException("'login' parameter is null or empty");
        }
        
        if (_usersManager == null)
        {
            _usersManager = (UsersManager) manager.lookup(UsersManager.ROLE);
        }
        
        User user = _usersManager.getUser(login);
        if (user == null)
        {
            result.put("error", " There is no user with login '" + login + "' in the user manager");   
        }
        else
        {
            Request request = ObjectModelHelper.getRequest(objectModel);
            request.getSession(true).setAttribute(AuthenticateAction.SESSION_USERLOGIN, login);
            
            result.put("login", login);
            result.put("name", user.getFullName());
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Impersonification of user '" + login + "' from IP " + request.getRemoteAddr());
            }
        }
        
        return result;
    }

}
