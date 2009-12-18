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
