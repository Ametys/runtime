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
package org.ametys.core.cocoon;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.xml.ParamSaxBuffer;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.workspace.WorkspaceManager;


/**
 * This class extends the classic I18nTransormer by automatically filling it with plugins catalogues.
 * It also handles special sitemap parameters :<br>
 * <ul>
 * <li><code>plugin</code> : when specified and when the given plugin exists, the default catalogue id is set to plugin.<code>plugin</code>
 * <li><code>workspace</code> : when specified and when the given workspace exists, the default catalogue id is set to workspace.<code>workspace</code>
 * </ul>
 * If the specified pugin or workspace does not exist, the specified default catalogue id is used, if any.
 */
public class I18nTransformer extends org.apache.cocoon.transformation.I18nTransformer implements Contextualizable
{
    /** Cocoon context */
    protected Context _context;
    
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        _context = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void configure(Configuration conf) throws ConfigurationException
    {
        // Add plugins catalogues to the configuration
        DefaultConfiguration newConf = new DefaultConfiguration("i18n");
        newConf.addChild(conf.getChild("untranslated-text"));
        newConf.addChild(conf.getChild("cache-at-startup"));

        Configuration cataloguesConf = conf.getChild("catalogues", false);
        DefaultConfiguration catalogues = new DefaultConfiguration("catalogues");
        newConf.addChild(catalogues);

        if (cataloguesConf != null)
        {
            catalogues.addAll(cataloguesConf);
        }

        _configurePlugins(catalogues);        
        _configureWorkspaces(catalogues);        

        // Load the configuration
        super.configure(newConf);
    }
    
    private void _configurePlugins(DefaultConfiguration catalogues)
    {
        PluginsManager pm = PluginsManager.getInstance();
        
        for (String pluginName : pm.getPluginNames())
        {
            String id = "plugin." + pluginName;
            
            DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
            catalogue.setAttribute("id", id);
            catalogue.setAttribute("name", "messages");
            
            DefaultConfiguration location1 = new DefaultConfiguration("location");
            location1.setValue("context://WEB-INF/i18n/plugins/" + pluginName);
            catalogue.addChild(location1);
            
            DefaultConfiguration location2 = new DefaultConfiguration("location");
            location2.setValue("plugin:" + pluginName + "://i18n");
            catalogue.addChild(location2);

            catalogues.addChild(catalogue);
        }
    }
    
    private void _configureWorkspaces(DefaultConfiguration catalogues)
    {
        WorkspaceManager wm = WorkspaceManager.getInstance();
        
        for (String workspace : wm.getWorkspaceNames())
        {
            String id = "workspace." + workspace;

            DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
            catalogue.setAttribute("id", id);
            catalogue.setAttribute("name", "messages");

            DefaultConfiguration location1 = new DefaultConfiguration("location");
            location1.setValue("context://WEB-INF/i18n/workspaces/" + workspace);
            catalogue.addChild(location1);
            
            DefaultConfiguration location2 = new DefaultConfiguration("location");
            location2.setValue("workspace:" + workspace + "://i18n");
            catalogue.addChild(location2);
           
            catalogues.addChild(catalogue);
        }
    }
    
    @Override
    public void setup(org.apache.cocoon.environment.SourceResolver resolver, Map objModel, String source, Parameters parameters) throws ProcessingException, SAXException, IOException
    {
        Parameters newParam = new Parameters();
        
        // Copie de tous les paramètres qui ne sont pas directement utilisés ici
        String[] names = parameters.getNames();

        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            
            if (!"plugin".equals(name) && !"workspace".equals(name) && !I18N_DEFAULT_CATALOGUE_ID.equals(name))
            {
                String value = parameters.getParameter(name, null);
                newParam.setParameter(name, value);
            }
        }
        
        String defaultCatalogueId = parameters.getParameter(I18N_DEFAULT_CATALOGUE_ID, null);
        String pluginName = parameters.getParameter("plugin", null);
        String workspaceName = parameters.getParameter("workspace", null);
        boolean useDefault = true;
        
        if (pluginName != null)
        {
            if (PluginsManager.getInstance().getPluginNames().contains(pluginName))
            {
                newParam.setParameter(I18N_DEFAULT_CATALOGUE_ID, "plugin." + pluginName);
                useDefault = false;
            }
        }
        else if (workspaceName != null)
        {
            if (WorkspaceManager.getInstance().getWorkspaceNames().contains(workspaceName))
            {
                newParam.setParameter(I18N_DEFAULT_CATALOGUE_ID, "workspace." + workspaceName);
                useDefault = false;
            }
        }
        
        if (useDefault)
        {
            newParam.setParameter(I18N_DEFAULT_CATALOGUE_ID, defaultCatalogueId);
        }
        
        super.setup(resolver, objModel, source, newParam);
    }
    
    @Override
    protected ParamSaxBuffer getMessage(String catalogueID, String key)
    {
        ParamSaxBuffer message = super.getMessage(catalogueID, key);
        if (message == null)
        {
            return getUntranslatedMessage (catalogueID, key);
        }

        return message;
    }
    
    /**
     * Retrieve the message when the key is not found
     *
     * @param catalogueID The catalogue id
     * @param key The i18n key
     * @return SaxBuffer containing the message for untranslated key
     */
    protected ParamSaxBuffer getUntranslatedMessage(String catalogueID, String key)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn("Translation not found for key " + key + " in catalogue " + catalogueID);
        }
        
        try
        {
            String value = catalogueID + ':' + key;
            ParamSaxBuffer paramSaxBuffer = new ParamSaxBuffer();
            paramSaxBuffer.characters(value.toCharArray(), 0, value.length());

            return paramSaxBuffer;
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }
}
