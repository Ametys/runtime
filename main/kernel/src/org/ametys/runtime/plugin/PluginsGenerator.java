/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager.ActiveFeature;
import org.ametys.runtime.plugin.PluginsManager.InactiveFeature;


/**
 * SAX plugins' informations in order to be able to generate a plugin by file tree
 */
public class PluginsGenerator extends AbstractGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "list");
        
        // Get active features
        Map<String, Collection<ActiveFeature>> activeFeatures = _getActiveFeatures();
        
        // Get inactive features
        Map<String, Collection<InactiveFeature>> inactiveFeatures = _getInactiveFeatures();

        // Loop on the list of plugins' groups (by name)
        _saxPlugins(activeFeatures, inactiveFeatures);
        
        // Extension points
        _saxExtensionPoints();
        
        XMLUtils.endElement(contentHandler, "list");
        contentHandler.endDocument();
    }
    
    private void _saxPlugins(Map<String, Collection<ActiveFeature>> activeFeatures, Map<String, Collection<InactiveFeature>> inactiveFeatures) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "plugins");

        Set<String> pluginNames = new HashSet<>();
        pluginNames.addAll(inactiveFeatures.keySet());
        pluginNames.addAll(activeFeatures.keySet());

        for (String pluginName : pluginNames)
        {
            AttributesImpl psAttrs = new AttributesImpl();
            psAttrs.addCDATAAttribute("name", pluginName);
            XMLUtils.startElement(contentHandler, "plugin", psAttrs);
            
            Collection<ActiveFeature> active = activeFeatures.get(pluginName);
            if (active != null)
            {
                for (ActiveFeature feature : active)
                {
                    AttributesImpl pAttrs = new AttributesImpl();
                    pAttrs.addCDATAAttribute("name", feature.getFeatureName());
                    XMLUtils.startElement(contentHandler, "feature", pAttrs);
                    
                    Map<String, Collection<String>> exts = feature.getExtensions();
                    
                    // Récupérer les extensions du plugin courant (par point d'extension)
                    for (String extensionPoint : exts.keySet())
                    {
                        AttributesImpl epAttrs = new AttributesImpl();
                        epAttrs.addCDATAAttribute("name", extensionPoint);
                        XMLUtils.startElement(contentHandler, "extensionPoint", epAttrs);
    
                        // Récupérer les extensions du plugin courant (par id)
                        for (String extension : exts.get(extensionPoint))
                        {
                            XMLUtils.createElement(contentHandler, "extension", extension);
                        }
    
                        XMLUtils.endElement(contentHandler, "extensionPoint");
                    }
    
                    // Récupérer les composants du plugin courant
                    for (String component : feature.getComponents())
                    {
                        XMLUtils.createElement(contentHandler, "component", component);
                    }
    
                    XMLUtils.endElement(contentHandler, "feature");
                }
            }            
                
            Collection<InactiveFeature> inactive = inactiveFeatures.get(pluginName);
            if (inactive != null)
            {
                for (InactiveFeature plugin : inactive)
                {
                    AttributesImpl pAttrs = new AttributesImpl();
                    pAttrs.addCDATAAttribute("name", plugin.getFeatureName());
                    pAttrs.addCDATAAttribute("inactive", "true");
                    pAttrs.addCDATAAttribute("cause", plugin.getCause().toString());
                    XMLUtils.createElement(contentHandler, "feature", pAttrs);
                }
            }
            
            XMLUtils.endElement(contentHandler, "plugin");
        }
        
        XMLUtils.endElement(contentHandler, "plugins");
    }
    
    private void _saxExtensionPoints() throws SAXException
    {
        XMLUtils.startElement(contentHandler, "extension-points");
        
        for (String extPoint : PluginsManager.getInstance().getExtensionPoints())
        {
            AttributesImpl ePAttrs = new AttributesImpl();
            ePAttrs.addCDATAAttribute("id", extPoint);
            XMLUtils.createElement(contentHandler, "extension-point", ePAttrs);
        }
        
        for (String extPoint : PluginsManager.getInstance().getSingleExtensionPoints())
        {
            AttributesImpl ePAttrs = new AttributesImpl();
            ePAttrs.addCDATAAttribute("id", extPoint);
            XMLUtils.createElement(contentHandler, "single-extension-point", ePAttrs);
        }
        
        XMLUtils.endElement(contentHandler, "extension-points");
    }
    
    private Map<String, Collection<ActiveFeature>> _getActiveFeatures()
    {
        Map<String, Collection<ActiveFeature>> activeFeatures = new HashMap<>();
        
        for (ActiveFeature feature : PluginsManager.getInstance().getActiveFeatures().values())
        {
            String pluginName = feature.getPluginName();
            
            Collection<ActiveFeature> features = activeFeatures.get(pluginName);
            
            if (features == null)
            {
                features = new ArrayList<>();
                activeFeatures.put(pluginName, features);
            }
            
            features.add(feature);
        }
        
        return activeFeatures;
    }
    
    private Map<String, Collection<InactiveFeature>> _getInactiveFeatures()
    {
        Map<String, Collection<InactiveFeature>> inactiveFeatures = new HashMap<>();
        Map<String, InactiveFeature> inactive = PluginsManager.getInstance().getInactiveFeatures();
        
        for (String featureId : inactive.keySet())
        {
            InactiveFeature feature = inactive.get(featureId);
            String pluginName = feature.getPluginName();
            
            Collection<InactiveFeature> features = inactiveFeatures.get(pluginName);
            
            if (features == null)
            {
                features = new ArrayList<>();
                inactiveFeatures.put(pluginName, features);
            }
            
            features.add(feature);
        }
        
        return inactiveFeatures;
    }
}
