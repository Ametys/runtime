/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.util.I18nUtils;
import org.ametys.runtime.util.I18nizableText;

/**
 * {@link TestCase} for programmatically translating i18n keys.
 */
public class I18nTestCase extends AbstractRuntimeTestCase
{
    I18nUtils _i18nUtils;
    
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp2");
        
        ServiceManager manager = Init.getPluginServiceManager();
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
    }
    
    /**
     * Tests simple i18n keys, without params.
     */
    public void testI18n()
    {
        String translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "en");
        assertEquals("english text", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "fr");
        assertEquals("texte français", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "zh");
        assertEquals("english text", translatedText);
    }
    
    /**
     * Tests i18n keys with params.
     */
    public void testI18nWithParams()
    {
        ArrayList<String> params = new ArrayList<String>(1);
        params.add("2");
        
        String translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "en");
        assertEquals("english text 2", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "fr");
        assertEquals("texte français 2", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "zh");
        assertEquals("english text 2", translatedText);
    }
    
    /**
     * Tests i18n keys with i18n params.
     */
    public void testI18nWithI18nParams()
    {
        Map<String, I18nizableText> params = new HashMap<String, I18nizableText>(1);
        params.put("language", new I18nizableText("plugin.help", "TEST"));
        
        String translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "en");
        assertEquals("english text", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "fr");
        assertEquals("texte français", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "zh");
        assertEquals("chinese text", translatedText);
    }
}
