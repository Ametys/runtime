/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.ui;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import org.ametys.runtime.cocoon.JSonReader;
import org.ametys.runtime.plugin.ExtensionPoint;

/**
 * Action executing remote method calls coming from client-side elements.<br>
 * Called methods should be annotated with {@link Callable}.<br>
 */
public class ExecuteClientCallsAction extends ServiceableAction implements ThreadSafe
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);

        // Find the corresponding object, either a component or an extension
        String role = (String) jsParameters.get("role");
        
        if (role == null)
        {
            throw new IllegalArgumentException("Component role should be present.");
        }
        
        Object object;
        
        if (!manager.hasService(role))
        {
            throw new IllegalArgumentException("The role '" + role + "' does not correspond to a valid component.");
        }
        
        Object component = manager.lookup(role);
        
        if (component instanceof ExtensionPoint)
        {
            ExtensionPoint extPoint = (ExtensionPoint) component;
            
            String id = (String) jsParameters.get("id");
            
            if (id == null)
            {
                object = component;
            }
            else
            {
                object = extPoint.getExtension(id);
                
                if (object == null)
                {
                    throw new IllegalArgumentException("The id '" + role + "' does not correspond to a valid extension for point " + role);
                }
            }
        }
        else
        {
            object = component;
        }
        
        // Find the corresponding method
        String methodName = (String) jsParameters.get("methodName");
        List<Object> params = (List<Object>) jsParameters.get("parameters");
        
        if (methodName == null)
        {
            throw new IllegalArgumentException("No method name present, cannot execute server side code.");
        }
        
        Class[] paramClass;
        Object[] paramValues;
        if (params == null)
        {
            paramClass = new Class[0];
            paramValues = new Object[0];
        }
        else
        {
            paramValues = params.toArray();
            paramClass = ClassUtils.toClass(paramValues);
        }
        
        Class<? extends Object> clazz = object.getClass();
        Method method = MethodUtils.getMatchingAccessibleMethod(clazz, methodName, paramClass);
        
        if (method == null)
        {
            throw new IllegalArgumentException("No method named '" + methodName + "' present in class " + clazz.getName() + ".");
        }
        
        Map<String, Object> result = _executeMethod(method, object, paramValues);

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.MAP_TO_READ, result);
        
        return EMPTY_MAP;
    }
    
    private Map<String, Object> _executeMethod(Method method, Object object, Object[] paramValues) throws Exception
    {
        Map<String, Object> result = null;
        if (method.isAnnotationPresent(Callable.class))
        {            
            result = (Map<String, Object>) method.invoke(object, paramValues);
            
            if (result == null)
            {
                result = Collections.EMPTY_MAP;
            }
        }
        else
        {
            throw new IllegalArgumentException("Trying to call a non-callable method : " + method.toGenericString());
        }
        
        return result;
    }
}
