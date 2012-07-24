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

package org.ametys.runtime.util.cocoon;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.impl.SitemapSource;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.Errors;

/**
 * This generator generates a ActionResult tag surounding parameters.<br>
 * Usefull for pipeline that needs no generator.
 */
public class ActionResultGenerator extends AbstractGenerator
{
    /** Request attribute name containing the map to use. */
    public static final String MAP_REQUEST_ATTR = ActionResultGenerator.class.getName() + ";map";

    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "ActionResult");
        
        saxParams();
        saxMap();
        saxSource();
        
        XMLUtils.endElement(contentHandler, "ActionResult");
        contentHandler.endDocument();
    }
    
    /**
     * Take the url in the source and sax it
     * the request parameters are converted into jsParameters
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ProcessingException on error
     */
    protected void saxSource() throws IOException, SAXException, ProcessingException
    {
        if (StringUtils.isEmpty(source))
        {
            return;
        }
        
        SitemapSource sitemapSource = null;
        try
        {
            Map<String, String> jsParameters = new HashMap<String, String>();
            int argsIndex = source.indexOf('?');
            if (argsIndex >= 0)
            {
                String argsString = source.substring(argsIndex + 1);
                String[] argsArrayString = argsString.split("&");
                for (String argString : argsArrayString)
                {
                    if (StringUtils.isEmpty(argString))
                    {
                        continue;
                    }

                    String argsName = argString;
                    String argsValue = "";
                    
                    int equalsIndex = argString.indexOf("=");
                    if (equalsIndex >= 0)
                    {
                        argsName = argString.substring(0, equalsIndex);
                        argsValue = argString.substring(equalsIndex + 1);
                    }
                    
                    jsParameters.put(argsName, argsValue);
                }
            }
            
            sitemapSource = (SitemapSource) resolver.resolveURI("cocoon:/" + source, null, jsParameters);
            sitemapSource.toSAX(contentHandler);
        }
        finally
        {
            resolver.release(sitemapSource);
        }
    }
    
    /**
     * Sax the sitemap parameters
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ProcessingException on error
     */
    protected void saxParams() throws IOException, SAXException, ProcessingException
    {
        for (String parameterName : parameters.getNames())
        {
            XMLUtils.createElement(contentHandler, parameterName, parameters.getParameter(parameterName, ""));
        }
    }
    
    /**
     * Used by saxMap to sax one item
     * @param key The key of the item
     * @param value The item to sax
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ProcessingException on error
     */
    protected void saxMapItem(String key, Object value) throws IOException, SAXException, ProcessingException
    {
        if (value instanceof Errors)
        {
            XMLUtils.startElement(contentHandler, key);
            
            for (I18nizableText errorLabel : ((Errors) value).getErrors())
            {
                errorLabel.toSAX(contentHandler, "error");
            }
            
            XMLUtils.endElement(contentHandler, key);
        }
        else if (value instanceof I18nizableText)
        {
            ((I18nizableText) value).toSAX(contentHandler, key);
        }
        else if (value instanceof Collection)
        {
            for (Object item : (Collection) value)
            {
                if (item != null)
                {
                    saxMapItem(key, item);
                }
            }
        }
        else
        {
            String stringValue = "";
            
            if (value != null)
            {
                stringValue = value.toString();
            }
            
            XMLUtils.createElement(contentHandler, key, stringValue);
        }
    }
    
    /**
     * Sax the map in request attribute MAP_REQUEST_ATTR. Should be a Map&gt;String, Object&gt; where values are saxed depending on their type : 
     * &gt;ul&gt;
     * as string using toString except for
     *    &lt;li&gt;Errors : each error is saxed &lt;/li&gt;
     *    &lt;li&gt;I18nizableText : saxed using toSAX method &lt;/li&gt;
     *    &lt;li&gt;Collection : each item is saxed used the same key. Note that Collection with inside Collection will have all its item with the same key at root &lt;/li&gt;
     *    &lt;li&gt;Object : a simple &lt;key>value.toString()&lt;/key> &lt;/li&gt;
     * &gt;/ul&gt;
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ProcessingException on error
     */
    protected void saxMap() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> map = (Map<String, Object>) request.getAttribute(MAP_REQUEST_ATTR);
        
        if (map != null)
        {
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();
    
                saxMapItem(key, value);
            }
        }
    }
}
