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
package org.ametys.runtime.plugins.core.authentication;

import java.util.Set;

import org.ametys.runtime.authentication.Authentication;
import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.right.RightsManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;


/**
 * Authenticate http request with the current base users extension (which must
 * implements AuthenticatingBaseUsers).
 */
public class HasRightManager extends AbstractLogEnabled implements Authentication, Serviceable
{
    private RightsManager _rightMgr;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _rightMgr = (RightsManager) manager.lookup(RightsManager.ROLE);
    }
    
    /**
     * Check if a User can log in. 
     * @param credentials Contains user information with an unencrypted password
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean login(Credentials credentials)
    {
        String login = credentials.getLogin();
        Set<String> userRightsSet = _rightMgr.getUserRights(login, null);
        return !(userRightsSet == null || userRightsSet.size() == 0);
    }
}
