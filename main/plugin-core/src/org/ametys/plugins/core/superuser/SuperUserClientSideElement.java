package org.ametys.plugins.core.superuser;

import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.right.InitializableRightsManager;
import org.ametys.core.right.RightsManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.StaticClientSideElement;

/**
 * This implementation creates a control allowing to affect a super user to a given context
 */
public class SuperUserClientSideElement extends StaticClientSideElement
{
    /** The service manager */
    private ServiceManager _sManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _sManager = smanager;
    }
    
    /**
     * Affect a user as super user on a given context
     * @param logins the list of logins of the users to affect
     * @param context the context 
     */
    @Callable
    public void affectSuperUser(List<String> logins, String context)
    {
        try
        {
            if (_rightsManager == null)
            {
                _rightsManager = (RightsManager) _sManager.lookup(RightsManager.ROLE);
            }
        }
        catch (ServiceException e)
        {
            throw new IllegalStateException(e);
        }
        
        if (logins.size() == 0)
        {
            throw new IllegalArgumentException("No login to initialize.");
        }
        
        if (!(_rightsManager instanceof InitializableRightsManager))
        {
            throw new IllegalArgumentException("Right manager is not initializable !");
        }
        
        InitializableRightsManager initRightsManager = (InitializableRightsManager) _rightsManager;
        for (String login : logins)
        {
            initRightsManager.grantAllPrivileges(login, context);
        }
    }
}
