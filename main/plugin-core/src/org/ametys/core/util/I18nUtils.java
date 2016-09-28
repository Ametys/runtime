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

package org.ametys.core.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.i18n.Bundle;
import org.apache.cocoon.i18n.BundleFactory;
import org.apache.cocoon.xml.ParamSaxBuffer;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.SaxBuffer.Characters;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.workspace.WorkspaceManager;

/**
 * Utils for i18n
 */
public class I18nUtils extends AbstractLogEnabled implements Component, Serviceable, Contextualizable, Initializable, Disposable
{
    /** The avalon role */
    public static final String ROLE = I18nUtils.class.getName();
    
    private static I18nUtils _instance;
    
    /** I18n catalogues */
    protected Map<String, Location> _locations;
    
    /** The avalon context */
    protected Context _context;
    
    private BundleFactory _bundleFactory;
    
    // Map<language, Map<text, translatedValue>>
    private Map<String, Map<I18nizableText, String>> _cache;

    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _bundleFactory = (BundleFactory) manager.lookup(BundleFactory.ROLE);
    }
    
    @Override
    public void initialize() throws Exception
    {
        _instance = this;
        _cache = new HashMap<>();
        
        _configure();
    }
    
    /**
     * Configure the i18n catalogue
     */
    protected void _configure ()
    {
        _locations = new HashMap<>();
        
        // initializes locations
        
        _locations.put("application", new Location("application", new String[]{"context://WEB-INF/i18n"}));
        
        PluginsManager pm = PluginsManager.getInstance();
        
        for (String pluginName : pm.getPluginNames())
        {
            String id = "plugin." + pluginName;
            
            String location2 = "plugin:" + pluginName + "://i18n";

            _locations.put(id, new Location("messages", new String[]{"context://WEB-INF/i18n/plugins/" + pluginName, location2}));
        }

        WorkspaceManager wm = WorkspaceManager.getInstance();
        
        for (String workspace : wm.getWorkspaceNames())
        {
            String id = "workspace." + workspace;
            String location2 = "workspace:" + workspace + "://i18n";
           
            _locations.put(id, new Location("messages", new String[]{"context://WEB-INF/i18n/workspaces/" + workspace, location2}));
        }
    }
    
    /**
     * Reload the i18n catalogues and clear cache.
     * This method should be called as soon as the list of i18n catalogue was changed, when adding a new catalogue for example.
     */
    public void reloadCatalogues ()
    {
        clearCache();
        _configure();
    }
    
    /**
     * Get the unique instance
     * @return the unique instance
     */
    public static I18nUtils getInstance()
    {
        return _instance;
    }
    
    /**
     * Get the translation of the key.
     * This method is slow.
     * Only use in very specific cases (send mail for example)
     * @param text The i18n key to translate
     * @return The translation or null if there's no available translation
     * @throws IllegalStateException if an error occured
     */
    public String translate(I18nizableText text)
    {
        return translate(text, null);
    }
    
    /**
     * Get the translation of the key.
     * This method is slow.
     * Only use in very specific cases (send mail for example)
     * @param text The i18n key to translate
     * @param language The language code to use for translation. Can be null.
     * @return The translation or null if there's no available translation
     * @throws IllegalStateException if an error occurred
     */
    public String translate(I18nizableText text, String language) throws IllegalStateException
    {
        // Check language
        String langCode = language;
        if (langCode == null)
        {
            Map objectModel = ContextHelper.getObjectModel(_context);
            Locale locale = org.apache.cocoon.i18n.I18nUtils.findLocale(objectModel, "locale", null, Locale.getDefault(), true);
            langCode = locale.toString();
        }
        
        Map<I18nizableText, String> values = getLangCache(langCode);
        
        String value = null;
        
        if (values.containsKey(text))
        {
            value = values.get(text);
        }
        else
        {
            value = _translate(text, langCode);
            
            if (value != null)
            {
                values.put(text, value);
            }
        }

        return value;
    }
    
    /**
     * Clear the i18n cache.
     */
    public void clearCache()
    {
        _cache.clear();
    }
    
    /**
     * Get the translation cache for a language.
     * @param language the language.
     * @return the translation cache for the given language.
     */
    protected Map<I18nizableText, String> getLangCache(String language)
    {
        Map<I18nizableText, String> langCache;
        
        if (_cache.containsKey(language))
        {
            langCache = _cache.get(language);
        }
        else
        {
            langCache = new HashMap<>();
            _cache.put(language, langCache);
        }
        
        return langCache;
    }
    
    /**
     * Get the translation of the key.
     * This method is slow.
     * Only use in very specific cases (send mail for example)
     * @param text The i18n key to translate
     * @param language The language code to use for translation. Can be null.
     * @return The translation or null if there's no available translation
     * @throws IllegalStateException if an error occured
     */
    protected String _translate(I18nizableText text, String language) throws IllegalStateException
    {
        if (!text.isI18n())
        {            
            return text.getLabel();
        }
        
        Location location = null;
        if (text.getLocation() != null)
        {
            location = new Location(text.getBundleName(), new String[]{text.getLocation()});
        }
        else
        {
            String catalogue = text.getCatalogue();
            location = _locations.get(catalogue);
        }
        
        if (location == null)
        {
            return null;
        }
        
        try
        {
            Bundle bundle = _bundleFactory.select(location.getLocations(), location.getName(), org.apache.cocoon.i18n.I18nUtils.parseLocale(language));
            
            // translated message
            ParamSaxBuffer buffer = (ParamSaxBuffer) bundle.getObject(text.getKey());
            
            if (buffer == null)
            {
                return null;
            }
            
            // message parameters
            Map<String, SaxBuffer> params = new HashMap<>();
            
            if (text.getParameters() != null)
            {
                int p = 0;
                for (String param : text.getParameters())
                {
                    Characters characters = new Characters(param.toCharArray(), 0, param.length());
                    params.put(String.valueOf(p++), new SaxBuffer(Arrays.asList(characters)));
                }
            }
            
            if (text.getParameterMap() != null)
            {
                for (String name : text.getParameterMap().keySet())
                {
                    // named parameters are themselves I18nizableText, so translate them recursively
                    String param = translate(text.getParameterMap().get(name), language);
                    Characters characters = new Characters(param.toCharArray(), 0, param.length());
                    params.put(name, new SaxBuffer(Arrays.asList(characters)));
                }
            }
            
            StringBuilder result = new StringBuilder();
            buffer.toSAX(new BufferHandler(result), params);
            
            return result.toString();
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Unable to get i18n translation", e);
        }
        catch (ComponentException e)
        {
            throw new RuntimeException("Unable to get i18n catalogue", e);
        }
    }
    
    private class BufferHandler extends DefaultHandler
    {
        StringBuilder _builder;
        
        public BufferHandler(StringBuilder builder)
        {
            _builder = builder;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            _builder.append(ch, start, length);
        }
    }
    
    public void dispose()
    {
        _instance = null;
    }
    
    /**
     * Class representing an i18n location
     */
    protected class Location 
    {
        String[] _loc;
        String _name;
        
        /**
         * Constructor.
         * @param name the catalogue name
         * @param locations the files locations.
         */
        public Location (String name, String[] locations)
        {
            _name = name;
            _loc = locations;
        }
        
        /**
         * Get the name
         * @return the name
         */
        public String getName ()
        {
            return _name;
        }
        
        /**
         * Get the files location
         * @return the files location
         */
        public String[] getLocations()
        {
            return _loc;
        }
    }
}
