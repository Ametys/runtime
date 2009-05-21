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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Redirector;

import org.ametys.runtime.util.LoggerFactory;


/**
 * This class is a filter for the communication between Tomcat and Cocoon. This one concerns the response request.
 * All the methods of the HttpServletResponse interface are implemented.
 * <br>
 * Only one of them will be usefull : sendRedirect(). This method uses the cocoon redirector to send the request. 
 */

public class FilterServletResponse implements HttpServletResponse
{
    private HttpServletResponse _response;
    
    private Redirector _redirect;
   
    private Logger _logger = LoggerFactory.getLoggerFor(FilterServletResponse.class);
    
    /**
     * Constructor of this filter.
     * @param req the response Httpservlet
     * @param redirect the Cocoon redirector.
     */
    public FilterServletResponse(HttpServletResponse req, Redirector redirect)
    {
        _response = req;
        
        _redirect = redirect;
             
        if (_response == null)
        {
            throw new IllegalArgumentException("Response can not be null");
        }
    }

    public void addHeader(String name, String value)
    {
        _response.addHeader(name, value);
    }
    
    public void addDateHeader(String name, long date)
    {
        _response.addDateHeader(name, date);
    }

    public void setIntHeader(String name, int value)
    {
        _response.setIntHeader(name, value);
    }

    public void setDateHeader(String name, long date)
    {
        _response.setDateHeader(name, date);
    }

    public void setContentType(String type)
    {
        _response.setContentType(type);
    }

    public String encodeURL(String url)
    {
        return _response.encodeURL(url);
    }
    
    @Deprecated
    public String encodeUrl(String url)
    {
        return _response.encodeUrl(url);
    }
    
    public String encodeRedirectURL(String url)
    {
        return _response.encodeRedirectURL(url);
    }
    
    @Deprecated
    public String encodeRedirectUrl(String url)
    {
        return _response.encodeRedirectUrl(url);
    }
    
    public String getCharacterEncoding()
    {
        return _response.getCharacterEncoding();
    }
    
    public void setContentLength(int length)
    {
        _response.setContentLength(length);
    }
    
    public boolean isCommitted()
    {
        return false;
    }
    
    public ServletOutputStream getOutputStream() throws IOException
    {
        return _response.getOutputStream();
    }
    
    public void setStatus(int sc)
    {
        _response.setStatus(sc);
    }

    @Deprecated
    public void setStatus(int sc, String sm)
    {
        _response.setStatus(sc, sm);
    }
    
    public void reset()
    {
        _response.reset();
    }
    
    public int getBufferSize()
    {
        return _response.getBufferSize();
    }
    
    public void flushBuffer() throws IOException
    {
        _response.flushBuffer();
    }
    
    public void setBufferSize(int size)
    {
        _response.setBufferSize(size);
    }

    public void sendRedirect(String location) throws IOException
    {
        try
        {
            _redirect.redirect(false, location);
        }
        catch (ProcessingException pe)
        {
            _logger.error(pe.getMessage(), pe);
            throw new IOException(pe.getMessage());
        }
    }
    
    public void setHeader(String name, String value)
    {
        _response.setHeader(name, value);
    }
    
    public Locale getLocale()
    {
        return _response.getLocale();
    }
    
    public void setLocale(Locale loc)
    {
        _response.setLocale(loc);
    }
    
    public void sendError(int sc) throws IOException
    {
        _response.sendError(sc);
    }
    
    public void sendError(int sc, String sm) throws IOException
    {
        _response.sendError(sc, sm);
    }

    public boolean containsHeader(String name)
    {
        return _response.containsHeader(name);
    }
    
    public void addIntHeader(String name, int value)
    {
        _response.addIntHeader(name, value);
    }
    
    public PrintWriter getWriter() throws IOException
    {
        return _response.getWriter();
    }
    
    public void addCookie(Cookie cookie)
    {
        _response.addCookie(cookie);
    }

    public String getContentType()
    {
        throw new UnsupportedOperationException("This method is not supported.");
    }

    public void resetBuffer()
    {
        throw new UnsupportedOperationException("This method is not supported.");
    }

    public void setCharacterEncoding(String arg0)
    {
        throw new UnsupportedOperationException("This method is not supported.");
    }
}
