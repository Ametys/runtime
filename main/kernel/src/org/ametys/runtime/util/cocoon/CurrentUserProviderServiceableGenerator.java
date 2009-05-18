package org.ametys.runtime.util.cocoon;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.generation.ServiceableGenerator;

import org.ametys.runtime.user.CurrentUserProvider;

/**
 * {@link ServiceableGenerator} which provides the current user if necessary.
 */
public abstract class CurrentUserProviderServiceableGenerator extends ServiceableGenerator
{
    /** The current user provider. */
    protected CurrentUserProvider _currentUserProvider;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
    }

    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    protected boolean _isSuperUser()
    {
        return _currentUserProvider.isSuperUser();
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected String _getCurrentUser()
    {
        return _currentUserProvider.getUser();
    }
}
