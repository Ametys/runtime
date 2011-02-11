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
package org.ametys.runtime.cocoon;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.xml.ParamSaxBuffer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
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

        _configureKernel(catalogues);
        _configurePlugins(catalogues);        
        _configureWorkspaces(catalogues);        

        // Chargement de la configuration
        super.configure(newConf);
    }
    
    private void _configureKernel(DefaultConfiguration catalogues) throws ConfigurationException
    {
        SourceResolver resolver = null;
        Source source = null;
        try
        {
            resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI("context://WEB-INF/i18n/kernel.xml");
            
            if (source.exists())
            {
                // si le fichier existe dans le filesystem on prend celui-là
                DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                catalogue.setAttribute("id", "kernel");
                catalogue.setAttribute("name", "kernel");
                catalogue.setAttribute("location", "context://WEB-INF/i18n");

                catalogues.addChild(catalogue);
            }
            else
            {
                // sinon on prend celui du jar
                DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                catalogue.setAttribute("id", "kernel");
                catalogue.setAttribute("name", "messages");
                catalogue.setAttribute("location", "resource://org/ametys/runtime/kernel/i18n");

                catalogues.addChild(catalogue);
            }
        }
        catch (IOException e)
        {
            String msg = "Cannot get the catalogue for kernel";
            getLogger().error(msg);
            throw new ConfigurationException(msg, e);
        }
        catch (ServiceException e)
        {
            String msg = "Cannot get the source resolver to check i18n catalogues";
            getLogger().error(msg);
            throw new ConfigurationException(msg, e);
        }
        finally
        {
            if (resolver != null)
            {
                resolver.release(source);
                manager.release(resolver);
            }
        }
    }
    
    private void _configurePlugins(DefaultConfiguration catalogues) throws ConfigurationException
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
                
                SourceResolver resolver = null;
                Source source = null;
                try
                {
                    resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                    source = resolver.resolveURI("context://WEB-INF/i18n/plugins/" + pluginName + ".xml");
                    
                    if (source.exists())
                    {
                        // si le fichier existe dans le filesystem on prend celui-là
                        DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                        catalogue.setAttribute("id", id);
                        catalogue.setAttribute("name", pluginName);
                        catalogue.setAttribute("location", "context://WEB-INF/i18n/plugins");

                        catalogues.addChild(catalogue);
                    }
                    else
                    {
                        // sinon on prend celui du jar
                        DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                        catalogue.setAttribute("id", id);
                        catalogue.setAttribute("name", "messages");
                        catalogue.setAttribute("location", "plugin:" + pluginName + "://i18n");

                        catalogues.addChild(catalogue);
                    }
                }
                catch (IOException e)
                {
                    String msg = "Cannot get the catalogue for plugin " + pluginName;
                    getLogger().error(msg);
                    throw new ConfigurationException(msg, e);
                }
                catch (ServiceException e)
                {
                    String msg = "Cannot get the source resolver to check i18n catalogues";
                    getLogger().error(msg);
                    throw new ConfigurationException(msg, e);
                }
                finally
                {
                    if (resolver != null)
                    {
                        resolver.release(source);
                        manager.release(resolver);
                    }
                }
                
            }
        }
    }
    
    private void _configureWorkspaces(DefaultConfiguration catalogues) throws ConfigurationException
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
                
                
                SourceResolver resolver = null;
                Source source = null;
                try
                {
                    resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                    source = resolver.resolveURI("context://WEB-INF/i18n/workspaces/" + workspace + ".xml");
                    
                    if (source.exists())
                    {
                        // si le fichier existe dans le filesystem on prend celui-là
                        DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                        catalogue.setAttribute("id", id);
                        catalogue.setAttribute("name", workspace);
                        catalogue.setAttribute("location", "context://WEB-INF/i18n/workspaces");

                        catalogues.addChild(catalogue);
                    }
                    else
                    {
                        // sinon on prend celui du jar
                        DefaultConfiguration catalogue = new DefaultConfiguration("catalogue");
                        catalogue.setAttribute("id", id);
                        catalogue.setAttribute("name", "messages");
                        catalogue.setAttribute("location", "workspace:" + workspace + "://i18n");

                        catalogues.addChild(catalogue);
                    }
                }
                catch (IOException e)
                {
                    String msg = "Cannot get the catalogue for workspace " + workspace;
                    getLogger().error(msg);
                    throw new ConfigurationException(msg, e);
                }
                catch (ServiceException e)
                {
                    String msg = "Cannot get the source resolver to check i18n catalogues";
                    getLogger().error(msg);
                    throw new ConfigurationException(msg, e);
                }
                finally
                {
                    if (resolver != null)
                    {
                        resolver.release(source);
                        manager.release(resolver);
                    }
                }
            }
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

        return message;
    }
}
