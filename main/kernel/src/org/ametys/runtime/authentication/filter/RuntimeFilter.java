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
package org.ametys.runtime.authentication.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.http.HttpEnvironment;

import org.ametys.runtime.util.LoggerFactory;


/**
 * This class is a generic filter between Tomcat and Cocoon. It will be used
 * during authentification with CAS e.g.
 * 
 * @author flat
 */

public class RuntimeFilter
{
    private Filter _filter;

    private Logger _logger = LoggerFactory.getLoggerFor(RuntimeFilter.class);

    /**
     * Constructor
     * 
     * @param filter A filter to apply
     */
    public RuntimeFilter(Filter filter)
    {
        _filter = filter;
    }

    /**
     * Enables to init config parameters using a FilterConfig
     * 
     * @param map A map containing all filter parameters
     * @param servletContext The servlet context
     * @throws ServletException if the underlying Filter fails to initialize
     */
    public void init(Map<String, String> map, ServletContext servletContext) throws ServletException
    {
        try
        {
            _filter.init(new RuntimeFilterConfig(servletContext, map));
        }
        catch (Exception e)
        {
            _logger.error("Impossible to init the filter.", e);
            throw new ServletException("Impossible to init the filter.", e);
        }
    }

    /**
     * Applies the filter
     * 
     * @param objectModel The object model of Cocoon
     * @param redirect The object Redirector of Cocoon
     * @throws ServletException if the underlying Filter fails to process the request
     * @throws IOException if the underlying Filter fails to process the request
     */
    public void doFilter(Map objectModel, Redirector redirect) throws ServletException, IOException
    {
        HttpServletRequest request = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        HttpServletResponse response = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        FilterServletResponse filteredResponse = new FilterServletResponse(response, redirect);

        InternFilterChain chain = new InternFilterChain();
        _filter.doFilter(request, filteredResponse, chain);
    }

    private class InternFilterChain implements FilterChain
    {
        /**
         * Constructor
         */
        public InternFilterChain()
        {
            /** empty */
        }

        /**
         * applies the filter
         */
        public void doFilter(ServletRequest req, ServletResponse reponse)
        {
            /** empty */
        }
    }
}
