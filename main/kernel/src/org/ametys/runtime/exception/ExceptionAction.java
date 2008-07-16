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
package org.ametys.runtime.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;


/**
 * This action determines which xsl will display the exception.
 */
public class ExceptionAction extends ServiceableAction implements ThreadSafe
{
    public Map<String, String> act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        // Le composant peut ne pas exister si le pluginManager n'est pas chargé
        // Il faut justement afficher les erreurs correctement.
        ExceptionHandler exceptionHandler;
        if (manager.hasService(ExceptionHandler.ROLE))
        {
            exceptionHandler = (ExceptionHandler) manager.lookup(ExceptionHandler.ROLE);
        }
        else
        {
            exceptionHandler = new DefaultExceptionHandler();
        }
        
        String uri = exceptionHandler.getExceptionXSLURI(source);
        
        Map<String, String> result = new HashMap<String, String>();
        result.put("xsl", uri);
        return result;
    }
}
