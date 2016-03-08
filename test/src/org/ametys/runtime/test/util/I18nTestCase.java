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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

import junit.framework.TestCase;

/**
 * {@link TestCase} for programmatically translating i18n keys.
 */
public class I18nTestCase extends AbstractRuntimeTestCase
{
    I18nUtils _i18nUtils;
    SourceResolver _srcResolver;
    
    @Override
    protected void setUp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");
        super.setUp();
        
        ServiceManager manager = Init.getPluginServiceManager();
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
        _srcResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Tests simple i18n keys, without params.
     */
    public void testI18n()
    {
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
                
        String translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "en");
        assertEquals("english text", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "fr");
        assertEquals("texte français", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "fr_FR");
        assertEquals("texte français", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY"), "zh");
        assertEquals("english text", translatedText);
        
        _cocoon._leaveEnvironment(environmentInformation);
    }
    
    /**
     * Tests i18n keys with params.
     */
    public void testI18nWithParams()
    {
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        ArrayList<String> params = new ArrayList<>(1);
        params.add("2");
        
        String translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "en");
        assertEquals("english text 2", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "fr");
        assertEquals("texte français 2", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "fr_FR");
        assertEquals("texte français 2", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_PARAMS", params), "zh");
        assertEquals("english text 2", translatedText);
        
        _cocoon._leaveEnvironment(environmentInformation);
    }
    
    /**
     * Tests i18n keys with i18n params.
     */
    public void testI18nWithI18nParams()
    {
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Map<String, I18nizableText> params = new HashMap<>(1);
        params.put("language", new I18nizableText("plugin.test", "TEST"));
        
        String translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "en");
        assertEquals("english text", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "fr");
        assertEquals("texte français", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "fr_FR");
        assertEquals("texte français", translatedText);
        
        translatedText = _i18nUtils.translate(new I18nizableText("plugin.test", "TEST_KEY_I18N_PARAMS", params), "zh");
        assertEquals("chinese text", translatedText);
        
        _cocoon._leaveEnvironment(environmentInformation);
    }
    
    /**
     * Test the i18n text reader
     */
    @SuppressWarnings("resource")
    public void testI18nTextReader()
    {
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        Source pluginSource = null;
        Source expectation = null;
        
        InputStream is1 = null;
        InputStream is2 = null;
        
        try 
        {
            pluginSource = _srcResolver.resolveURI("cocoon://plugins/test/resources/js/input.en.js");
            expectation = _srcResolver.resolveURI("plugin:test://resources/js/expectation.js");
            
            is1 = pluginSource.getInputStream();
            is2 = expectation.getInputStream();
                    
            assertEquals(IOUtils.toString(is1), IOUtils.toString(is2));
        }
        catch (IOException e)
        {
            throw new RuntimeException("I/O exception", e);
        }
        finally
        {
            IOUtils.closeQuietly(is1);
            IOUtils.closeQuietly(is2);
            
            _srcResolver.release(expectation);
            _srcResolver.release(pluginSource);
        }
        
        
        _cocoon._leaveEnvironment(environmentInformation);
    }
}
