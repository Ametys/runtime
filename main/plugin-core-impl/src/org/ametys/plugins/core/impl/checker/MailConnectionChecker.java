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

import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.core.util.mail.SendMailHelper;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * Tests the connection to the mail box.
 */
public class MailConnectionChecker implements ParameterChecker, Configurable
{
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration[] config = configuration.getChild("linked-params").getChildren();
        if (config.length != 5)
        {
            throw new ConfigurationException("The MailConnectionChecker should have 5 linked params in the right order: host, port, security.protocol, user, password");
        }
    }
    
    @Override 
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        String host = values.get(0);
        String portAsString = values.get(1);
        String protocol = values.get(2);
        String user = values.get(3);
        String password = values.get(4);
        
        try
        {
            SendMailHelper.sendMail(null, null, null, null, null, null, null, null, false, false, host, Integer.parseInt(portAsString), protocol, user, password);
        }
        catch (Exception e)
        {
            throw new ParameterCheckerTestFailureException(e.getMessage(), e);
        }
    }
}
