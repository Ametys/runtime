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
package org.ametys.runtime.workspaces.admin.authentication;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.authentication.AuthorizationRequiredException;


/**
 * Action setting the response Header for 401 reponse
 */
public class SetAuthorizationHeaderAction extends AbstractAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        AuthorizationRequiredException ex =  (AuthorizationRequiredException) ObjectModelHelper.getThrowable(objectModel);
        Response response = ObjectModelHelper.getResponse(objectModel);
        
        response.setHeader("WWW-Authenticate", "BASIC realm=\"" + ex.getRealm() + "\"");
        
        return EMPTY_MAP;
    }
}
