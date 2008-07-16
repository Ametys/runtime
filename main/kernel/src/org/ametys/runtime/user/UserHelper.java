/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.user;

import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

/**
 * This utility component allow to know is user is a system administrator or help to get the User if not.
 */
public final class UserHelper
{
    /** The session attribute name storing the login of the connected user */
    public static final String SESSION_USERLOGIN = "Runtime:UserLogin"; 

    /** The request attribute name telling that administrator is logged in */
    public static final String REQUEST_ATTRIBUTE_ADMINISTRATOR = "Runtime:Administrator";

    private UserHelper ()
    {
        // empty
    }
    
    /**
     * Determine if current user is the application administrator
     * @param objectModel the cocoon object model
     * @return true if administrator is logged in
     */
    public static boolean isAdministrator(Map objectModel)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return request.getAttribute(REQUEST_ATTRIBUTE_ADMINISTRATOR) != null;
    }
    
    /**
     * Retrieve the current user
     * @param objectModel the cocoon object model
     * @return the current user's login or null if user is anonymous
     * @throws IllegalStateException if the user is the administrator
     */
    public static String getCurrentUser(Map objectModel)
    {
        if (isAdministrator(objectModel))
        {
            throw new IllegalStateException("The current user is the application administrator.");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (String) request.getSession().getAttribute(SESSION_USERLOGIN);
    }
}
