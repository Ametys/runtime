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
package org.ametys.runtime.authentication;

/**
 * Exception representing a 401 response
 */
public class AuthorizationRequiredException extends Exception
{
    private String _realm;
    
    /**
     * Constructor
     * @param realm the Realm associated with the credentials to provide
     */
    public AuthorizationRequiredException(String realm)
    {
        _realm = realm;
    }
    
    /**
     * Returns the Realm associated with the credentials to provide
     * @return the Realm associated with the credentials to provide
     */
    public String getRealm()
    {
        return _realm;
    }
}
