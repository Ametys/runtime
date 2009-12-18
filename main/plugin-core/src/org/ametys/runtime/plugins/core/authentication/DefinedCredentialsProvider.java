/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.authentication;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.environment.Redirector;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.authentication.CredentialsProvider;


/**
 * This implementation will always provide the same crentials
 */
public class DefinedCredentialsProvider implements CredentialsProvider, Configurable
{
    private Credentials _credentials;
 
    public void configure(Configuration configuration) throws ConfigurationException
    {
        String login = configuration.getChild("user").getValue("anonymous");
        _credentials = new Credentials(login, null);
    }
    
    public boolean accept()
    {
        return false;
    }

    public void allowed(Redirector redirector)
    {
        // nothing
    }

    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        return _credentials;
    }

    public void notAllowed(Redirector redirector) throws Exception
    {
        // nothing
    }

    public boolean validate(Redirector redirector) throws Exception
    {
        return true;
    }

}
