/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.test.resources;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;

/**
 * Test case for the LESS and the SASS reader
 */
public class CompiledResourceReaderTestCase extends AbstractRuntimeTestCase
{
    private SourceResolver _resolver;
        
    /**
     * This test call the LESS reader and the SASS reader, and compare the result with the expected compiled value.
     * @throws Exception If an error occurs
     */
    public void testSASSCompiledResourceReader() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        _resolver = (SourceResolver) Init.getPluginServiceManager().lookup(SourceResolver.ROLE);
        
        _assertCompiledResource("sass-file-1.scss", "sass-file-1-result.css", "Error with SASS reader");
        
        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }
    
    /**
     * This test call the LESS reader and the SASS reader, and compare the result with the expected compiled value.
     * @throws Exception If an error occurs
     */
    public void testLESSCompiledResourceReader() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        _resolver = (SourceResolver) Init.getPluginServiceManager().lookup(SourceResolver.ROLE);
        
        _assertCompiledResource("less-file-1.less", "less-file-1-result.css", "Error with LESS reader");
        
        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }
    
    private void _assertCompiledResource(String inputFilename, String outputFilename, String error) throws Exception
    {
        Source expectedSource = _resolver.resolveURI("plugin:test://resources/css/sass-less-result/" + outputFilename);
        Source resultSource = _resolver.resolveURI("cocoon://plugins/test/resources/css/sass-less/" + inputFilename);
        
        String result = IOUtils.toString(resultSource.getInputStream(), "UTF-8");
        String expected = IOUtils.toString(expectedSource.getInputStream(), "UTF-8");
        assertEquals(error + ": Output does not match expected value", expected.replace("\r", ""), result.replace("\r", ""));
    }
}
