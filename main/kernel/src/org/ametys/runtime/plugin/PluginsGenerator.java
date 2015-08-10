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
import java.util.Collection;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager.InactivityCause;

/**
 * SAX plugins' informations in order to be able to generate a plugin by file tree
 */
public class PluginsGenerator extends AbstractGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "list");
        
        Map<String, Plugin> plugins = PluginsManager.getInstance().getPlugins();
        Map<String, Feature> activeFeatures = PluginsManager.getInstance().getFeatures();
        Map<String, InactivityCause> inactiveFeatures = PluginsManager.getInstance().getInactiveFeatures();

        _saxPlugins(plugins, activeFeatures, inactiveFeatures);
        
        // Extension points
        _saxExtensionPoints();
        
        XMLUtils.endElement(contentHandler, "list");
        contentHandler.endDocument();
    }
    
    private void _saxPlugins(Map<String, Plugin> plugins, Map<String, Feature> activeFeatures, Map<String, InactivityCause> inactiveFeatures) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "plugins");

        for (String pluginName : plugins.keySet())
        {
            AttributesImpl psAttrs = new AttributesImpl();
            psAttrs.addCDATAAttribute("name", pluginName);
            XMLUtils.startElement(contentHandler, "plugin", psAttrs);
            
            Plugin plugin = plugins.get(pluginName);
            Map<String, Feature> features = plugin.getFeatures();
            
            for (String featureId : features.keySet())
            {
                Feature feature = features.get(featureId);
                
                if (activeFeatures.containsKey(featureId))
                {
                    AttributesImpl pAttrs = new AttributesImpl();
                    pAttrs.addCDATAAttribute("name", feature.getFeatureName());
                    XMLUtils.startElement(contentHandler, "feature", pAttrs);
                    
                    // Get extension by extension point
                    Map<String, Collection<String>> exts = feature.getExtensionsIds();
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
    
                    // Get components
                    for (String component : feature.getComponentsIds().keySet())
                    {
                        XMLUtils.createElement(contentHandler, "component", component);
                    }
    
                    XMLUtils.endElement(contentHandler, "feature");
                }
                else
                {
                    InactivityCause cause = inactiveFeatures.get(featureId);
                    
                    AttributesImpl pAttrs = new AttributesImpl();
                    pAttrs.addCDATAAttribute("name", feature.getFeatureName());
                    pAttrs.addCDATAAttribute("inactive", "true");
                    pAttrs.addCDATAAttribute("cause", cause.toString());
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
        
        for (String extPoint : PluginsManager.getInstance().getExtensionPoints().keySet())
        {
            AttributesImpl ePAttrs = new AttributesImpl();
            ePAttrs.addCDATAAttribute("id", extPoint);
            XMLUtils.createElement(contentHandler, "extension-point", ePAttrs);
        }
        
        XMLUtils.endElement(contentHandler, "extension-points");
    }
}
