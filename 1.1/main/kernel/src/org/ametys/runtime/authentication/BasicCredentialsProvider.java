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

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.commons.codec.binary.Base64;

import org.ametys.runtime.config.Config;


/**
 * Basic http authentication based AuthenticationManager.
 */
public class BasicCredentialsProvider implements CredentialsProvider, Initializable, Contextualizable
{
    private static final String BASIC_AUTHENTICATION_KEY = "BASIC ";

    /** The authenticaiton realm */
    protected String _realm;
    
    /** the avalon context */
    protected Context _context;

    /**
     * Create a basic credential. This constructor is to be used by avalon component manager since a cll to configure is needed.
     */
    public BasicCredentialsProvider()
    {
        // default constructor for avalon
    }
    
    /**
     * Create a basic credential without avalon (for admin workspace purpose : avalon may not have been started)
     * @param realm The authentication realm
     * @param context The avalon context
     */
    public BasicCredentialsProvider(String realm, Context context)
    {
        _realm = realm;
        if (_realm == null)
        {
            throw new IllegalArgumentException("Realm must be provided");
        }
        
        _context = context;
        if (_context == null)
        {
            throw new IllegalArgumentException("Avalon context must no be null");
        }
    }

    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void initialize() throws Exception
    {
        _realm = Config.getInstance().getValueAsString("runtime.authentication.basic.realm");
    }
    
    public boolean validate(Redirector redirector) throws Exception
    {
        // ce Manager est toujours valide
        return true;
    }

    public boolean accept()
    {
        // cette implémentation n'a pas de requête particulière à prendre en
        // compte
        return false;
    }

    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        
        // Check authentication header
        String auth = ObjectModelHelper.getRequest(objectModel).getHeader("Authorization");

        if (auth == null)
        {
            // no auth
            return null;
        }

        if (!auth.toUpperCase().startsWith(BASIC_AUTHENTICATION_KEY))
        {
            // we only do BASIC
            return null;
        }

        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = auth.substring(BASIC_AUTHENTICATION_KEY.length());

        // Decode it, using any base 64 decoder
        String userpassDecoded = new String(Base64.decodeBase64(userpassEncoded.getBytes()));

        // Login and password are separated with a :
        StringTokenizer stk = new StringTokenizer(userpassDecoded, ":");
        String log = stk.hasMoreTokens() ? stk.nextToken() : "";
        String pass = stk.hasMoreTokens() ? stk.nextToken() : "";

        return new Credentials(log, pass);
    }

    public void notAllowed(Redirector redirector) throws Exception
    {
        throw new AuthorizationRequiredException(_realm);
    }

    public void allowed(Redirector redirector)
    {
        // empty method, nothing more to do
    }
}
