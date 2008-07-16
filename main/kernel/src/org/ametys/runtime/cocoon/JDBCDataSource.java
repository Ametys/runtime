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
package org.ametys.runtime.cocoon;

import org.apache.avalon.excalibur.datasource.ResourceLimitingJdbcDataSource;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

import org.ametys.runtime.config.Config;


/**
 * DataSourceComponent which configuration may contains variables between {}.<br>
 * Such variables are resolved trough inputmodules
 */
public class JDBCDataSource extends ResourceLimitingJdbcDataSource
{
    private static final String __CONFIG_ATTRIBUTE_NAME = "runtime-config-parameter";
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        DefaultConfiguration conf = _resolve(configuration);
        
        super.configure(conf);
    }
    
    private DefaultConfiguration _resolve(Configuration configuration)
    {
        DefaultConfiguration conf = new DefaultConfiguration(configuration.getName());
        
        for (String attName : configuration.getAttributeNames())
        {
            if (!__CONFIG_ATTRIBUTE_NAME.equals(attName))
            {
                String att = configuration.getAttribute(attName, null);
                
                if (att != null)
                {
                    conf.setAttribute(attName, att);
                }
            }
        }
        
        String configName = configuration.getAttribute(__CONFIG_ATTRIBUTE_NAME, null);
        String value;
        
        if (configName == null)
        {
            // valeur du noeud courant
            value = configuration.getValue(null);
        }
        else
        {
            value = Config.getInstance().getValueAsString(configName);
        }
        
        if (value != null)
        {
            conf.setValue(value);
        }
        
        // puis les fils
        Configuration[] children = configuration.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            conf.addChild(_resolve(children[i]));
        }
        
        return conf;
    }
}
