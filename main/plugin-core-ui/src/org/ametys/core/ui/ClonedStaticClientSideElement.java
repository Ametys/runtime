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
package org.ametys.core.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.lang3.StringUtils;

/**
 * The StaticClientSideElement can clone another ClientSideElement to overwritten partially its configuration.
 * The cloned element is referenced with the property ref-id, and the clone must use the same Extension Point as the cloned element.
 */
public final class ClonedStaticClientSideElement extends StaticClientSideElement
{
    /** The reference id of another ClientSideElement to inherit */
    protected String _refId;
    
    /** The manager for this extension point */
    protected ClientSideElementManager _clientSideElementManager;
    
    private ServiceManager _serviceManager;
    
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
         
        _serviceManager = smanager;
    }

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _refId = configuration.getAttribute("ref-id", null);
        if (_refId != null)
        {
            String extensionPoint = configuration.getAttribute("point");
            try
            {
                _clientSideElementManager = (ClientSideElementManager) _serviceManager.lookup(extensionPoint);
            }
            catch (ServiceException e)
            {
                throw new ConfigurationException("Unable to lookup the manager for the extension point '" + extensionPoint + "'", e);
            }
        }
        
        super.configure(configuration);
    }
    
    @Override
    protected String _configureClass(Configuration configuration) throws ConfigurationException
    {
        if (_refId != null)
        {
            // If empty, class name from _refId will be use in #getScripts
            return configuration.getAttribute("name", "");
        }
        
        return super._configureClass(configuration);        
    }
    
    @Override
    public List<Script> getScripts(boolean ignoreRights, Map<String, Object> contextParameters)
    {
        if (_refId == null)
        {
            return super.getScripts(ignoreRights, contextParameters);
        }
        
        // FIXME handle rights for workspace admin, here is a temporary workaround
        if (ignoreRights || (contextParameters != null && "admin".equals(contextParameters.get("workspace"))) || hasRight(getRights(contextParameters)))
        {
            ClientSideElement parent = _clientSideElementManager.getExtension(_refId);
            List<Script> scripts = parent.getScripts(true, contextParameters);
            
            List<ScriptFile> scriptFiles = _script.getScriptFiles();
            List<ScriptFile> cssFiles = _script.getCSSFiles();
            Map<String, Object> parameters = _script.getParameters();
            String scriptClassname = _script.getScriptClassname();
            
            List<Script> inheritedScrits = new ArrayList<>();
            for (Script parentScript : scripts)
            {
                List<ScriptFile> inheritedCssFiles = new ArrayList<>(parentScript.getCSSFiles());
                inheritedCssFiles.addAll(cssFiles);
                List<ScriptFile> inheritedScriptFiles = new ArrayList<>(parentScript.getScriptFiles());
                inheritedScriptFiles.addAll(scriptFiles);
                Map<String, Object> inheritedParameters = new HashMap<>(parentScript.getParameters());
                inheritedParameters.putAll(parameters);
                
                String classname = StringUtils.isNotEmpty(scriptClassname) ? scriptClassname : parentScript.getScriptClassname();
                inheritedScrits.add(new Script(_script.getId(), parentScript.getServerId(), classname, inheritedScriptFiles, inheritedCssFiles, inheritedParameters));
            }
            
            return inheritedScrits;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, String> getRights(Map<String, Object> contextParameters)
    {
        if (_refId != null && _rights.isEmpty())
        {
            ClientSideElement parent = _clientSideElementManager.getExtension(_refId);
            return parent.getRights(contextParameters);
        }
        
        return super.getRights(contextParameters);
    }
    
    @Override
    public Map<String, List<String>> getDependencies()
    {
        if (_refId != null && _dependencies.isEmpty())
        {
            ClientSideElement parent = _clientSideElementManager.getExtension(_refId);
            return parent.getDependencies();
        }
        
        return super.getDependencies();
    }
}
