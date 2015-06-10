/*
 *  Copyright 2014 Anyware Services
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

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.core.parameter.checker.ParameterCheckerTestFailureException;
import org.ametys.core.util.mail.SendMailHelper;
import org.ametys.runtime.parameter.ParameterChecker;

/**
 * Tests the connection to the mail box.
 */
public class MailConnectionChecker implements ParameterChecker, Configurable
{
    private String _paramHost;
    private String _paramPort;
    private String _paramSecurityProtocol;
    private String _paramUser;
    private String _paramPasswd;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration[] config = configuration.getChild("linked-params").getChildren();
        if (config.length != 5)
        {
            throw new ConfigurationException("The MailConnectionChecker should have 5 linked params in the right order: host, port, security.protocol, user, password");
        }
        
        int i = 0;
        _paramHost = config[i++].getAttribute("id");
        _paramPort = config[i++].getAttribute("id");
        _paramSecurityProtocol = config[i++].getAttribute("id");
        _paramUser = config[i++].getAttribute("id");
        _paramPasswd = config[i++].getAttribute("id");
    }
    
    @Override 
    public void check(Map<String, String> configurationParameters) throws ParameterCheckerTestFailureException
    {
        String password = configurationParameters.get(_paramPasswd);
        String host = configurationParameters.get(_paramHost);
        String portStr = configurationParameters.get(_paramPort);
        long port = Integer.parseInt(portStr);
        String user = configurationParameters.get(_paramUser);
        String protocol = configurationParameters.get(_paramSecurityProtocol);
        
        try
        {
            SendMailHelper.sendMail(null, null, null, null, null, null, null, null, false, false, host, port, protocol, user, password);
        }
        catch (Exception e)
        {
            throw new ParameterCheckerTestFailureException(e.getMessage(), e);
        }
    }
}
