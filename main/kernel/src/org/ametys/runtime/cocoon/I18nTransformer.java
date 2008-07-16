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
import org.apache.cocoon.environment.SourceResolver;
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
        // Modification de la configuration pour faire apparaitre les plugins
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

        // Chargement de la configuration
        super.configure(newConf);
    }
    
    private void _configurePlugins(DefaultConfiguration catalogues)
    {
        // Les plugins dans des jar
        PluginsManager pm = PluginsManager.getInstance();
        
        for (String pluginName : pm.getPluginNames())
        {
            String pluginURI = pm.getBaseURI(pluginName);
            
            if (pluginURI == null)
            {
                // Le plugin est dans le filesystem
                String pluginFamily = pm.getPluginLocation(pluginName);
                
                if (!pluginFamily.endsWith("/"))
                {
                    pluginFamily += '/';
                }
                
                String id = "plugin." + pluginName;

                DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                catalogue.setAttribute("id", id);
                catalogue.setAttribute("name", "messages");
                catalogue.setAttribute("location", "context://" + pluginFamily + pluginName + "/i18n");

                catalogues.addChild(catalogue);
            }
            else
            {
                // Le plugin est dans le classpath
                String id = "plugin." + pluginName;
                
                DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                catalogue.setAttribute("id", id);
                catalogue.setAttribute("name", pluginName);
                catalogue.setAttribute("location", "context://WEB-INF/i18n/plugins");

                catalogues.addChild(catalogue);
            }
        }
    }
    
    private void _configureWorkspaces(DefaultConfiguration catalogues)
    {
        WorkspaceManager wm = WorkspaceManager.getInstance();
        
        for (String workspace : wm.getWorkspaceNames())
        {
            String workspaceURI = wm.getBaseURI(workspace);
            
            if (workspaceURI == null)
            {
                // Le workspace est dans le filesystem
                String id = "workspace." + workspace;

                DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                catalogue.setAttribute("id", id);
                catalogue.setAttribute("name", "messages");
                catalogue.setAttribute("location", "context://workspaces/" + workspace + "/i18n");

                catalogues.addChild(catalogue);
            }
            else
            {
                // Le workspace est dans le classpath
                String id = "workspace." + workspace;
                
                DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                catalogue.setAttribute("id", id);
                catalogue.setAttribute("name", workspace);
                catalogue.setAttribute("location", "context://WEB-INF/i18n/workspaces");

                catalogues.addChild(catalogue);
            }
        }
    }
    
    @Override
    public void setup(SourceResolver resolver, Map objModel, String source, Parameters parameters) throws ProcessingException, SAXException, IOException
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
}
