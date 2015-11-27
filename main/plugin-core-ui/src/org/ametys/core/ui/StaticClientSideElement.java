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
package org.ametys.core.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.right.HierarchicalRightsManager;
import org.ametys.core.right.RightsContextPrefixExtensionPoint;
import org.ametys.core.right.RightsManager;
import org.ametys.core.right.RightsManager.RightResult;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.util.ConfigurationHelper;

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
    /** The righs mode to associate rights AND or OR */
    protected String _rightsMode;
    /** The parameters configured for initial element creation */
    protected Map<String, Object> _initialParameters;
    
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
        _rightsMode = _configureRightsMode(configuration);
        
        _initialParameters = configureInitialParameters(configuration);
    }
    
    /**
     * Configure the initial parameters
     * @param configuration the global configuration
     * @return The initial parameters read
     * @throws ConfigurationException The configuration is incorrect
     */
    protected Map<String, Object> configureInitialParameters(Configuration configuration) throws ConfigurationException
    {
        Map<String, Object> initialParameters = _configureParameters (configuration.getChild("class"));
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuration of element '" + _id + "' is over");
        }
        
        return initialParameters;
    }
    
    /**
     * Configure parameters recursively 
     * @param configuration the parameters configuration
     * @return parameters in a Map
     * @throws ConfigurationException The configuration is incorrect
     */
    protected Map<String, Object> _configureParameters (Configuration configuration) throws ConfigurationException
    {
        return ConfigurationHelper.parsePluginParameters(configuration, getPluginName(), getLogger());
    }
    
    /**
     * Configure the script
     * @param configuration the global configuration
     * @return The script created
     * @throws ConfigurationException The configuration is incorrect
     */
    protected Script _configureScript(Configuration configuration) throws ConfigurationException
    {
        List<String> scriptsImports = _configureImports(configuration.getChild("scripts"));
        List<String> cssImports = _configureImports(configuration.getChild("css"));
        String jsClassName = _configureClass(configuration.getChild("class"));
        
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
        String jsClassName = configuration.getAttribute("name");
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
        return ConfigurationHelper.parsePluginResourceList(configuration, getPluginName(), getLogger());
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
        Map<String, String> rights = new HashMap<>();
        
        Configuration[] rightsConf;
        if (configuration.getChild("rights", false) != null)
        {
            rightsConf = configuration.getChild("rights").getChildren("right");
        }
        else
        {
            rightsConf = configuration.getChildren("right");
        }
        
        for (Configuration rightConf : rightsConf)
        {
            String right = rightConf.getValue("");
            if (right.length() != 0)
            {
                String prefix = rightConf.getAttribute("context-prefix", null);
                String[] rightIds = right.split("\\|");
                for (String rightId : rightIds)
                {
                    rights.put(rightId, prefix);
                }
            }
            else
            {
                String message = "The optional right element is empty.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }
        }
        
        return rights;
    }
    
    /**
     * Configure the mode to associate rights AND or OR
     * @param configuration The configuration of rights mode.
     * @return AND or OR (default value)
     * @throws ConfigurationException The configuration is incorrect
     */
    protected String _configureRightsMode(Configuration configuration) throws ConfigurationException
    {
        return "AND".equals(configuration.getChild("rights").getAttribute("mode", "OR").toUpperCase()) ? "AND" : "OR";
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
                Set<String> rightsToCheck = rights.keySet();
                for (String rightToCheck : rightsToCheck)
                {
                    if (StringUtils.isNotEmpty(rightToCheck))
                    {
                        String rightContext = rights.get(rightToCheck) != null ? rights.get(rightToCheck) : "";
                        if (_rightsManager instanceof HierarchicalRightsManager)
                        {
                            // Check right on context/*
                            if (((HierarchicalRightsManager) _rightsManager).hasRightOnContextPrefix(userLogin, rightToCheck, rightContext) == RightResult.RIGHT_OK)
                            {
                                if ("OR".equals(_rightsMode))
                                {
                                    return true;
                                }
                            }
                            else
                            {
                                if ("AND".equals(_rightsMode))
                                {
                                    return false;
                                }
                            }
                        }
                        else
                        {
                            // Check right on exact context
                            if (_rightsManager.hasRight(userLogin, rightToCheck, rightContext) == RightResult.RIGHT_OK)
                            {
                                if ("OR".equals(_rightsMode))
                                {
                                    return true;
                                }
                            }
                            else
                            {
                                if ("AND".equals(_rightsMode))
                                {
                                    return false;
                                }
                            }
                        }
                    }
                }
                
                return "AND".equals(_rightsMode);
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
    public Map<String, Object> getParameters(Map<String, Object> contextualParameters)
    {
        return _initialParameters;
    }
    
    @Override
    public String getPluginName()
    {
        return _pluginName;
    }
    
    @Override
    public String getId()
    {
        return _id;
    }
}
