/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.plugins.core.impl.checker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.util.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * Checks if the url written is correct and if it allows to establish a connection with the given configuration. 
 */
public class HttpUrlChecker implements ParameterChecker, Configurable
{
    /** The user agent */
    protected String _userAgent;
    
    /** The method */
    protected String _method;
    
    /** The timeout */
    protected int _timeout;
    
    /** The acceptable response codes */
    protected String _okCodes;
    
    /** The values the header must have */
    protected Map<String, String> _headerValues;

    /** Something to add to the parameter */
    protected String _additionnalURL;
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration config = configuration.getChild("configuration");
        Configuration requestConfig = config.getChild("request");
        Configuration responseConfig = config.getChild("response");
        
        Configuration timeoutConfig = requestConfig.getChild("timeout", false);
        
        _timeout = timeoutConfig != null ? timeoutConfig.getValueAsInteger() : -1;
        _userAgent = requestConfig.getChild("user-agent").getValue(null);
        _method = requestConfig.getChild("method").getValue(null);
        _additionnalURL = requestConfig.getChild("additionnal-url").getValue("");
        
        _okCodes = responseConfig.getChild("code").getValue(null);
        
        _headerValues = new HashMap<> ();
        for (Configuration headerChildConfig : responseConfig.getChild("header").getChildren())
        {
            _headerValues.put(headerChildConfig.getName(), headerChildConfig.getValue());
        }
    }
    
    @Override
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        String configUrl = values.get(0);
        
        try
        {
            HttpURLConnection httpUrlConnection = _prepareConnection(configUrl + _additionnalURL);
            
            // Do the connection
            int responseCode = httpUrlConnection.getResponseCode();
            
            _testResponseCode(httpUrlConnection, responseCode);
            _testHeaders(httpUrlConnection);
        }
        catch (IOException e)
        {
            throw new ParameterCheckerTestFailureException("Unable to contact '" + configUrl + _additionnalURL + "' (" + e.getMessage() + ")", e);
        }
    }

    private HttpURLConnection _prepareConnection(String configUrl) throws IOException, MalformedURLException, ProtocolException
    {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(configUrl).openConnection();
        
        if (_userAgent != null)
        {
            httpUrlConnection.setRequestProperty("User-Agent", _userAgent);
        }
        if (_method != null)
        {
            httpUrlConnection.setRequestMethod(_method);
        }
        if (_timeout != -1)
        {
            httpUrlConnection.setReadTimeout(_timeout);
        }
        return httpUrlConnection;
    }
    
    private void _testHeaders(HttpURLConnection httpUrlConnection) throws ParameterCheckerTestFailureException
    {
        Set<String> keys = _headerValues.keySet();
        for (String key : keys)
        {
            if (httpUrlConnection.getHeaderField(key) == null)
            {
                throw new ParameterCheckerTestFailureException("The header field '" + key + "' does not exist");
            }
            
            if (!httpUrlConnection.getHeaderField(key).equals(_headerValues.get(key)))
            {
                throw new ParameterCheckerTestFailureException("The header field '" + key + "' does not have the expected value '" + _headerValues.get(key) + "', found '" + httpUrlConnection.getHeaderField(key) + "' instead.");
            }
        }
    }

    private void _testResponseCode(HttpURLConnection httpUrlConnection, int responseCode) throws ParameterCheckerTestFailureException, IOException
    {
        String[] okCodes = {Integer.toString(HttpURLConnection.HTTP_OK)};
        if (_okCodes != null)
        {
            okCodes = StringUtils.split(_okCodes, ",");
        }
        if (!ArrayUtils.contains(okCodes, Integer.toString(responseCode)))
        {
            throw new ParameterCheckerTestFailureException("Error code " + responseCode + ". Message: " + httpUrlConnection.getResponseMessage());
        }
    }
}
