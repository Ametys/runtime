package org.ametys.runtime.util.cocoon;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.acting.ServiceableAction;

import org.ametys.runtime.user.CurrentUserProvider;

/**
 * {@link ServiceableAction} which provides the current user if necessary.
 */
public abstract class CurrentUserProviderServiceableAction extends ServiceableAction
{
    /** The current user provider. */
    protected CurrentUserProvider _currentUserProvider;

    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    protected boolean _isSuperUser()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) manager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new IllegalStateException(e);
            }
        }
        
        return _currentUserProvider.isSuperUser();
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected String _getCurrentUser()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) manager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new IllegalStateException(e);
            }
        }
        
        return _currentUserProvider.getUser();
    }
}
