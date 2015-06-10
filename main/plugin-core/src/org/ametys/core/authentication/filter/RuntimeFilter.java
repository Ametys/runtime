/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.authentication.filter;

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

import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.http.HttpEnvironment;


/**
 * This class is a generic filter wrapping an actual {@link Filter}. 
 * {@link RuntimeFilter} are used to filter requests from inside the application, instead of inside the servlet engine.
 */
public class RuntimeFilter
{
    private Filter _filter;

    /**
     * Constructor
     * @param filter A filter to apply
     */
    public RuntimeFilter(Filter filter)
    {
        _filter = filter;
    }

    /**
     * Enables to init config parameters using a FilterConfig.
     * @param map a map containing all filter parameters.
     * @param servletContext the servlet context.
     * @throws ServletException if the underlying Filter fails to initialize.
     */
    public void init(Map<String, String> map, ServletContext servletContext) throws ServletException
    {
        try
        {
            _filter.init(new RuntimeFilterConfig(servletContext, map));
        }
        catch (Exception e)
        {
            throw new ServletException("Impossible to initialize the filter.", e);
        }
    }

    /**
     * Applies the filter.
     * @param objectModel the Cocoon's object model.
     * @param redirect the Cocoon's redirector.
     * @throws ServletException if the underlying Filter fails to process the request.
     * @throws IOException if the underlying Filter fails to process the request.
     */
    public void doFilter(Map objectModel, Redirector redirect) throws ServletException, IOException
    {
        HttpServletRequest request = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        HttpServletResponse response = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        FilterServletResponse filteredResponse = new FilterServletResponse(response, redirect);

        FilterChain chain = new FilterChain()
        {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException
            {
             // does nothing
            }
        };
        
        _filter.doFilter(request, filteredResponse, chain);
    }
}
