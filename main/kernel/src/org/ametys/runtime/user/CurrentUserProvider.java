package org.ametys.runtime.user;


/**
 * Component which:
 * <ul>
 *  <li>test if the current logged in user is the super user.
 *  <li>provides the login of the current user.
 * </ul>
 */
public interface CurrentUserProvider
{
    /** Avalon role. */
    public static final String ROLE = CurrentUserProvider.class.getName();

    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    boolean isSuperUser();
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    String getUser();
}
