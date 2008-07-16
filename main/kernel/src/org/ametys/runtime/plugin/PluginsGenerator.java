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
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.plugin.PluginsManager.ActiveFeature;
import org.ametys.runtime.plugin.PluginsManager.InactiveFeature;


/**
 * SAX the plugins' informations
 */
public class PluginsGenerator extends AbstractGenerator
{
    private Map<String, Collection<ActiveFeature>> _getActiveFeatures()
    {
        Map<String, Collection<ActiveFeature>> activeFeatures = new HashMap<String, Collection<ActiveFeature>>();
        
        for (ActiveFeature feature : PluginsManager.getInstance().getActiveFeatures().values())
        {
            String pluginName = feature.getPluginName();
            
            Collection<ActiveFeature> features = activeFeatures.get(pluginName);
            
            if (features == null)
            {
                features = new ArrayList<ActiveFeature>();
                activeFeatures.put(pluginName, features);
            }
            
            features.add(feature);
        }
        
        return activeFeatures;
    }
    
    private Map<String, Collection<InactiveFeature>> _getInactiveFeatures()
    {
        Map<String, Collection<InactiveFeature>> inactiveFeatures = new HashMap<String, Collection<InactiveFeature>>();
        Map<String, InactiveFeature> inactive = PluginsManager.getInstance().getInactiveFeatures();
        
        for (String featureId : inactive.keySet())
        {
            InactiveFeature feature = inactive.get(featureId);
            String pluginName = feature.getPluginName();
            
            Collection<InactiveFeature> features = inactiveFeatures.get(pluginName);
            
            if (features == null)
            {
                features = new ArrayList<InactiveFeature>();
                inactiveFeatures.put(pluginName, features);
            }
            
            features.add(feature);
        }
        
        return inactiveFeatures;
    }
    
    private void _saxExtensionPoints() throws SAXException
    {
        contentHandler.startElement("", "extension-points", "extension-points", new AttributesImpl());
        
        for (String extPoint : PluginsManager.getInstance().getExtensionPoints())
        {
            AttributesImpl ePAttrs = new AttributesImpl();
            ePAttrs.addAttribute("", "id", "id", "CDATA", extPoint);
            contentHandler.startElement("", "extension-point", "extension-point", ePAttrs);
            contentHandler.endElement("", "extension-point", "extension-point");
        }
        
        for (String extPoint : PluginsManager.getInstance().getSingleExtensionPoints())
        {
            AttributesImpl ePAttrs = new AttributesImpl();
            ePAttrs.addAttribute("", "id", "id", "CDATA", extPoint);
            contentHandler.startElement("", "single-extension-point", "single-extension-point", ePAttrs);
            contentHandler.endElement("", "single-extension-point", "single-extension-point");
        }
        
        contentHandler.endElement("", "extension-points", "extension-points");
    }
    
    private void _saxPlugins(Map<String, Collection<ActiveFeature>> activeFeatures, Map<String, Collection<InactiveFeature>> inactiveFeatures) throws SAXException
    {
        Set<String> pluginNames = new HashSet<String>();
        pluginNames.addAll(inactiveFeatures.keySet());
        pluginNames.addAll(activeFeatures.keySet());
        
        for (String pluginName : pluginNames)
        {
            AttributesImpl psAttrs = new AttributesImpl();
            psAttrs.addAttribute("", "name", "name", "CDATA", pluginName);
            XMLUtils.startElement(contentHandler, "plugin", psAttrs);
            
            Collection<ActiveFeature> active = activeFeatures.get(pluginName);
            if (active != null)
            {
                for (ActiveFeature feature : active)
                {
                    AttributesImpl pAttrs = new AttributesImpl();
                    pAttrs.addAttribute("", "name", "name", "CDATA", feature.getFeatureName());
                    XMLUtils.startElement(contentHandler, "feature", pAttrs);
                    
                    Map<String, Collection<String>> exts = feature.getExtensions();
                    
                    // Récupérer les extensions du plugin courant (par point d'extension)
                    for (String extensionPoint : exts.keySet())
                    {
                        AttributesImpl epAttrs = new AttributesImpl();
                        epAttrs.addAttribute("", "name", "name", "CDATA", extensionPoint);
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
                    pAttrs.addAttribute("", "name", "name", "CDATA", plugin.getFeatureName());
                    pAttrs.addAttribute("", "inactive", "inactive", "CDATA", "true");
                    pAttrs.addAttribute("", "cause", "cause", "CDATA", plugin.getCause().toString());
                    XMLUtils.createElement(contentHandler, "feature", pAttrs);
                }
            }
            
            XMLUtils.endElement(contentHandler, "plugin");
        }
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "list");

        // Parcourir la liste des plugins actifs
        Map<String, Collection<ActiveFeature>> activeFeatures = _getActiveFeatures();
        
        // Parcourir la liste des plugins inactifs
        Map<String, Collection<InactiveFeature>> inactiveFeatures = _getInactiveFeatures();

        // Parcourir la liste des groupe de plugins (par nom)
        _saxPlugins(activeFeatures, inactiveFeatures);

        // Parcourir la liste des points d'extensions
        _saxExtensionPoints();

        XMLUtils.endElement(contentHandler, "list");
        contentHandler.endDocument();
    }
}
