/*
 *  Copyright 2012 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ametys.runtime.authentication;

/**
 * Exception representing a 401 response
 */
public class AuthorizationRequiredException extends Exception
{
    private String _realm;
    private String _token;
    private boolean _isNegotiate;
    
    /**
     * Constructor
     * @param realm the Realm associated with the credentials to provide
     */
    public AuthorizationRequiredException(String realm)
    {
        this(false, realm);
    }
    
    /**
     * Constructor
     * @param isNegotiate True if the authorization can be negotiated
     * @param data The data associated with the exception, either the token or the realm
     */
    public AuthorizationRequiredException(boolean isNegotiate, String data)
    {
        _isNegotiate = isNegotiate;
        if (isNegotiate)
        {
            _realm = data;
        }
        else
        {
            _token = data;
        }
    }
    
    /**
     * Returns true if the authorization can be negotiated
     * @return Ture if the authorization can be negotiated
     */
    public boolean isNegotiate()
    {
        return _isNegotiate;
    }
    
    /**
     * Returns the Realm associated with the credentials to provide
     * @return the Realm associated with the credentials to provide
     */
    public String getRealm()
    {
        return _realm;
    }
    
    /**
     * Returns the token associated with the negotiation
     * @return The token. Can be null
     */
    public String getToken()
    {
        return _token;
    }
}
