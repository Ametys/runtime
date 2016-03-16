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
package org.ametys.runtime.test.minimize;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;

/**
 * Test case for the minimize transformer
 */
public class MinimizeTransformerTestCase extends AbstractRuntimeTestCase
{
    private SourceResolver _resolver;

    /**
     * Create the test case.
     * @param name the test case name.
     */
    public MinimizeTransformerTestCase(String name)
    {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }
        
    /**
     * This test runs all the possible situations that the minimize transformer may encounter, though a single html file and all the minified results.
     * @throws Exception If an error occurs
     */
    public void testMinimizeTransformerFull() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        _resolver = (SourceResolver) Init.getPluginServiceManager().lookup(SourceResolver.ROLE);
        _assertTransformation("complete-test.html", "complete-test-result.html");
        
        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }
    
    private void _assertTransformation(String inputFilename, String outputFilename) throws Exception
    {
        Source expectedSource = _resolver.resolveURI("plugin:test://pages/minimize/" + outputFilename);
        Source source = _resolver.resolveURI("cocoon://_plugins/test/minimize/" + inputFilename);
        
        String result = IOUtils.toString(source.getInputStream(), "UTF-8");
        String expected = IOUtils.toString(expectedSource.getInputStream(), "UTF-8");
        assertEquals("XML output differs", expected, result);

        Pattern urls = Pattern.compile("['\"]/plugins/core-ui/resources-minimized/([^/.'\"]+\\.([^/.'\"]+))['\"]");
        Matcher matcher = urls.matcher(result);
        
        _assertFilesMinimized(matcher, "css", "complete-test-css-1.css");
        _assertFilesMinimized(matcher, "css", "complete-test-css-2.css");
        _assertFilesMinimized(matcher, "js", "complete-test-js-1.js"); // File a.js does not exist, but file should still generate
        _assertFilesMinimized(matcher, "css", "complete-test-css-3.css");
        _assertFilesMinimized(matcher, "css", "complete-test-css-4.css");
        _assertFilesMinimized(matcher, "js", "complete-test-js-2.js");
        _assertFilesMinimized(matcher, "js", "complete-test-js-3.js");
        _assertFilesMinimized(matcher, "js", "complete-test-js-3.js");
    }

    private void _assertFilesMinimized(Matcher matcher, String type, String expectedFile) throws Exception
    {
        Source expectedSource = _resolver.resolveURI("plugin:test://resources/" + type + "/minimize-result/" + expectedFile);
        String expected = IOUtils.toString(expectedSource.getInputStream(), "UTF-8");
        
        assertTrue("Url not minimized", matcher.find());
        String file = matcher.group(1);
        String fileExtension = matcher.group(2);
        assertEquals("Wrong minimized type", type, fileExtension);
        
        Source source = _resolver.resolveURI("cocoon://_plugins/test/resources-minimized/" + file);
        String actual = IOUtils.toString(source.getInputStream(), "UTF-8");
        assertEquals("Minimized file content did not match expected value", expected, actual);
    }
}
