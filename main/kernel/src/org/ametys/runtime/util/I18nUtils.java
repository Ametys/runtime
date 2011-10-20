/*
 *  Copyright 2010 Anyware Services
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

package org.ametys.runtime.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;

/**
 * Utils for i18n
 */
public class I18nUtils extends AbstractLogEnabled implements Component, Serviceable, Contextualizable, Initializable
{
    /** The avalon role */
    public static final String ROLE = I18nUtils.class.getName();
    
    private static I18nUtils _instance;
    
    /** The excalibur source resolver */
    protected SourceResolver _sourceResolver;
    /** The avalon context */
    protected Context _context;
    
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
        _sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void initialize() throws Exception
    {
        _cache = new HashMap<String, Map<I18nizableText, String>>();
        _instance = this;
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
     * @return The translation
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
     * @return The translation
     * @throws IllegalStateException if an error occured
     */
    public String translate(I18nizableText text, String language) throws IllegalStateException
    {
        Map<I18nizableText, String> values = getLangCache(language);
        
        String value = null;
        
        if (values.containsKey(text))
        {
            value = values.get(text);
        }
        else
        {
            value = _translate(text, language);
            
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
            langCache = new HashMap<I18nizableText, String>();
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
     * @return The translation
     * @throws IllegalStateException if an error occured
     */
    protected String _translate(I18nizableText text, String language) throws IllegalStateException
    {
        if (!text.isI18n())
        {            
            return text.getLabel();
        }
        
        // Check language
        String langCode = language;
        if (langCode == null)
        {
            Map objectModel = ContextHelper.getObjectModel(_context);
            Locale locale = org.apache.cocoon.i18n.I18nUtils.findLocale(objectModel, "locale", null, Locale.getDefault(), true);
            langCode = locale.toString();
        }
            
        Source source = null;
        InputStream is = null;
        try
        {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("i18n", text);
            parameters.put("locale", langCode);

            String uri = "cocoon://_plugins/core/i18n";
            source = _sourceResolver.resolveURI(uri, null, parameters);
            is = source.getInputStream();
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            SourceUtil.copy(is, bos);
            
            return bos.toString("UTF-8");
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot translate '" + text.toString() + "' into '" + langCode + "'", e);
        }
        finally
        {
            IOUtils.closeQuietly(is);
            _sourceResolver.release(source);
        }
    }

}
