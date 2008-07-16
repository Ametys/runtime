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
package org.ametys.runtime.authentication.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * The runtime filter configuration based on configuration parameters
 */
public class RuntimeFilterConfig implements FilterConfig
{
    private HashMap _parameters;
    
    private ServletContext _servletContext;
    
    /**
     * Creates a generic filter 
     * @param map A hashmap of parameters 
     * @param servlet A reference to the servlet context
     * */
    public RuntimeFilterConfig(ServletContext servlet, Map map)
    {
        _parameters = (HashMap) map; 
        _servletContext = servlet;
    }
    
    /**
     * Gets the servlet context 
     */
    public ServletContext getServletContext()
    {
        return _servletContext;
    }
    
    /**
     * Gets the filter name
     */
    public String getFilterName()
    {
        return "RuntimeFilter";
    }

    public String getInitParameter(String name)
    {
        return (String) _parameters.get(name);
    }

    public Enumeration getInitParameterNames()
    {
        return new IteratorEnumeration(_parameters.keySet().iterator());
    }

    /**
     * An enumeration based on an iterator
     */
    public class IteratorEnumeration implements Enumeration
    {
        private Iterator _it;
        
        /**
         * Create an enumeration from an iterator
         * @param it The iterator to enumerate
         */
        public IteratorEnumeration (Iterator it)
        {
            _it = it;
        }

        public boolean hasMoreElements()
        {
            return _it.hasNext();
        }

        public Object nextElement()
        {
            return _it.next();
        }
    }
}
