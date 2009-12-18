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
package org.ametys.runtime.request;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;

/**
 * Avalon component responsible for registering <code>RequestListener</code>s
 */
public class RequestListenerManager implements Contextualizable, ThreadSafe, Initializable, Component
{
    /** Avalon Role */
    public static final String ROLE = RequestListenerManager.class.getName();
    
    /** ServletContext attribute id containing an ArrayList&lt;RequestListener> */
    public static final String CONTEXT_ATTRIBUTE_REQUEST_LISTENERS = "runtime:requestListeners";

    private Context _context;

    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        _context = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    public void initialize() throws Exception
    {
        _context.setAttribute(org.ametys.runtime.request.RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS, new ArrayList<RequestListener>());
    }
    
    /**
     * Registers a RequestListener
     * @param listener the listener being registered
     */
    @SuppressWarnings("unchecked")
    public void registerListener(RequestListener listener)
    {
        Collection<RequestListener> listeners = (Collection) _context.getAttribute(org.ametys.runtime.request.RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);
        listeners.add(listener);
    }
    
    /**
     * Unregisters a RequestListener
     * @param listener the listener being unregistered
     */
    @SuppressWarnings("unchecked")
    public void unregisterListener(RequestListener listener)
    {
        Collection<RequestListener> listeners = (Collection) _context.getAttribute(org.ametys.runtime.request.RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);
        listeners.remove(listener);
    }
}
