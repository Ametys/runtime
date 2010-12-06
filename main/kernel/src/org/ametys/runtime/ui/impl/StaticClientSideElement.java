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
package org.ametys.runtime.ui.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.right.RightsContextPrefixExtensionPoint;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.ui.ClientSideElement;
import org.ametys.runtime.user.CurrentUserProvider;
import org.ametys.runtime.util.I18nizableText;

/**
 * This implementation creates an element from a static configuration
 */
public class StaticClientSideElement extends AbstractLogEnabled implements ClientSideElement, Configurable, PluginAware, Serviceable
{
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;
    /** The rights manager */
    protected RightsManager _rightsManager;
    /** The rights context prefix */
    protected RightsContextPrefixExtensionPoint _rightsContextPrefixEP;
    /** The element id */
    protected String _id;
    /** The script configured */
    protected Script _script;
    /** The right configured. Can be null */
    protected Map<String, String> _rights;
    /** The parameters configured for initial element creation */
    protected Map<String, I18nizableText> _initialParameters;
    
    /** The name of the plugin that declared the element */
    protected String _pluginName;
    /** The name of the feature that declared the element */
    protected String _featureName;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _currentUserProvider = (CurrentUserProvider) smanager.lookup(CurrentUserProvider.ROLE);
        _rightsManager = (RightsManager) smanager.lookup(RightsManager.ROLE);
        _rightsContextPrefixEP = (RightsContextPrefixExtensionPoint) smanager.lookup(RightsContextPrefixExtensionPoint.ROLE);
    }
    
    @Override
    public void setPluginInfo(String pluginName, String featureName)
    {
        _pluginName = pluginName;
        _featureName = featureName;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _id = configuration.getAttribute("id");
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuring element '" + _id + "'");
        }
        
        _script = _configureScript(configuration);
        _rights = _configureRights(configuration);
        
        _initialParameters = configureInitialParameters(configuration);
    }
    
    /**
     * Configure the initial parameters
     * @param configuration the global configuration
     * @return The initial parameters read
     * @throws ConfigurationException
     */
    protected Map<String, I18nizableText> configureInitialParameters(Configuration configuration) throws ConfigurationException
    {
        Map<String, I18nizableText> initialParameters = new HashMap<String, I18nizableText>();
        
        for (Configuration paramConfiguration : configuration.getChild("action").getChildren("param"))
        {
            String name = paramConfiguration.getAttribute("name");
            String value = paramConfiguration.getValue("");

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Configured with parameter '" + name + "' : '" + value + "'");
            }

            if (paramConfiguration.getAttributeAsBoolean("i18n", false))
            {
                initialParameters.put(name, new I18nizableText("plugin." + _pluginName, value));
            }
            else if (paramConfiguration.getAttributeAsBoolean("file", false))
            {
                String pluginName = paramConfiguration.getAttribute("plugin", getPluginName());
                initialParameters.put(name, new I18nizableText("/plugins/" + pluginName + "/resources/" + value));
            }
            else 
            {
                initialParameters.put(name, new I18nizableText(value));
            }
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Configuration of element '" + _id + "' is over");
            }
        }
        
        return initialParameters;
    }
    
    /**
     * Configure the script
     * @param configuration the global configuration
     * @return The script created
     * @throws ConfigurationException
     */
    protected Script _configureScript(Configuration configuration) throws ConfigurationException
    {
        List<String> scriptsImports = _configureImports(configuration.getChild("scripts"));
        List<String> cssImports = _configureImports(configuration.getChild("css"));
        String jsClassName = _configureClass(configuration.getChild("action"));
        
        return new Script(jsClassName, scriptsImports, cssImports);
    }
    
    /**
     * Configure the js class name
     * @param configuration The configuration on action tag
     * @return The js class name
     * @throws ConfigurationException If an error occurs
     */
    protected String _configureClass(Configuration configuration) throws ConfigurationException
    {
        String jsClassName = configuration.getAttribute("class");
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Js class configured is '" + jsClassName + "'");
        }
        return jsClassName;        
    }
    
    /**
     * Configure the import part
     * @param configuration The imports configuration
     * @return The set of the complete url of imported file
     * @throws ConfigurationException If an error occurs
     */
    protected List<String> _configureImports(Configuration configuration) throws ConfigurationException
    {
        List<String> scriptsImports = new ArrayList<String>();
        String scriptsDefaultPlugin = configuration.getAttribute("plugin", _pluginName);
        for (Configuration scriptsConfiguration : configuration.getChildren("file"))
        {
            String pluginName = scriptsConfiguration.getAttribute("plugin", scriptsDefaultPlugin);
            String url = scriptsConfiguration.getValue();
            
            String completeUrl = "/plugins/" + pluginName + "/resources/" + url; 
            scriptsImports.add(completeUrl);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Importing file '" + completeUrl + "'");
            }
        }
        return scriptsImports;
    }
    
    /**
     * Configure the right following the configuration
     * 
     * @param configuration The root configuration
     * @return The right name or null.
     * @throws ConfigurationException if a right element is present but empty
     */
    protected Map<String, String> _configureRights(Configuration configuration) throws ConfigurationException
    {
        _rights = new HashMap<String, String>();
        
        Configuration[] rightsConf = configuration.getChildren("right");
        for (Configuration rightConf : rightsConf)
        {
            String right = rightConf.getValue("");
            if (right.length() != 0)
            {
                String prefix = rightConf.getAttribute("context-prefix", null);
                String[] rightIds = right.split("|");
                for (String rightId : rightIds)
                {
                    _rights.put(rightId, prefix);
                }
            }
            else
            {
                String message = "The optional right element is empty.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }
        }
        
        return _rights;
    }
    
    /**
     * Configure the right following the configuration
     * 
     * @param configuration The root configuration
     * @return The right name or null.
     * @throws ConfigurationException if a right element is present but empty
     */
    protected String _configureRightContextPrefix(Configuration configuration) throws ConfigurationException
    {
        Configuration rightConf = configuration.getChild("right", false);
        if (rightConf != null)
        {
            String prefix = rightConf.getAttribute("context-prefix", "");
            if (prefix.length() != 0)
            {
                return prefix;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Determine following the right parameter if the user has right to access this feature
     * 
     * @param rights The rights (name, context) to check. Can be empty.
     * @return true if the user has the right or if there is not right and false otherwise
     */
    protected boolean hasRight(Map<String, String> rights)
    {
        if (rights.isEmpty())
        {
            return true;
        }
        else
        {
            
            if (_currentUserProvider.isSuperUser())
            {
                return true;
            }
            else
            {
                String userLogin = _currentUserProvider.getUser();
                if (userLogin == null)
                {
                    return false;
                }
                else
                {
                    Set<String> rightsToCheck = rights.keySet();
                    for (String rightToCheck : rightsToCheck)
                    {
                        if (StringUtils.isNotEmpty(rightToCheck))
                        {
                            // Check the user has at least the right on a current context
                            Set<String> contexts = _rightsManager.getUserRightContexts(userLogin, rightToCheck);
                            for (String context : contexts)
                            {
                                if (context.startsWith(_rightsContextPrefixEP.getContextPrefix() + (rights.get(rightToCheck) != null ? rights.get(rightToCheck) : "")))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                    
                    return false;
                }
            }
        }
    }
    
    @Override
    public Script getScript(Map<String, Object> contextParameters)
    {
        if (!hasRight(getRights(contextParameters)))
        {
            return null;
        }
        return _script;
    }

    @Override
    public Map<String, String> getRights(Map<String, Object> contextParameters)
    {
        return _rights;
    }
    
    @Override
    public Map<String, I18nizableText> getParameters(Map<String, Object> contextualParameters)
    {
        return _initialParameters;
    }
    
    @Override
    public String getPluginName()
    {
        return _pluginName;
    }
}
