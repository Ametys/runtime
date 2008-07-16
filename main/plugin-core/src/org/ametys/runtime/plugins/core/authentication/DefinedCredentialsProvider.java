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
