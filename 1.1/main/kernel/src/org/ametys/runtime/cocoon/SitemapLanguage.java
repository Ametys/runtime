package org.ametys.runtime.cocoon;

import java.util.Collection;

import org.ametys.runtime.config.ConfigManager;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.MutableConfiguration;

/**
 * Entry point in the sitemap build process to dynamically insert the components brought by the SitemapConfigurationExtensionPoint
 */
public class SitemapLanguage extends org.apache.cocoon.components.treeprocessor.sitemap.SitemapLanguage
{
    @Override
    protected ComponentManager createComponentManager(Configuration tree) throws Exception
    {
        if (!ConfigManager.getInstance().isComplete())
        {
            // Pas initialisé, on n'essaie pas de charger quoi que ce soit
            return super.createComponentManager(tree);
        }
        
        if (processor.getWrappingProcessor() != processor.getRootProcessor())
        {
            // On n'est pas dans la sitemap racine, on ne fait rien
            return super.createComponentManager(tree);
        }
        
        SitemapConfigurationExtensionPoint sitemapConfigurations = (SitemapConfigurationExtensionPoint) parentManager.lookup(SitemapConfigurationExtensionPoint.ROLE);
        
        DefaultConfiguration config = new DefaultConfiguration("sitemap");
        config.addAll(tree);
        
        MutableConfiguration componentsConfig = config.getMutableChild("components");
        
        _addRuntimeComponents(componentsConfig, "actions", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "generators", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "transformers", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "serializers", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "readers", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "matchers", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "selectors", sitemapConfigurations);
        _addRuntimeComponents(componentsConfig, "pipes", sitemapConfigurations);
        
        return super.createComponentManager(config);
    }
    
    private void _addRuntimeComponents(MutableConfiguration componentsConfig, String componentName, SitemapConfigurationExtensionPoint sitemapConfigurations) throws ConfigurationException
    {
        Collection<Configuration> sitemapConfigs = sitemapConfigurations.getConfigurations(componentName);
        
        if (sitemapConfigs == null)
        {
            return;
        }
        
        MutableConfiguration config = componentsConfig.getMutableChild(componentName);
        
        for (Configuration sitemapConfig : sitemapConfigs)
        {
            config.addChild(sitemapConfig);
        }
    }
}
