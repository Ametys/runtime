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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.plugins.core.ui.minimize.MinimizeTransformer;
import org.ametys.plugins.core.ui.minimize.MinimizeTransformer.FileData;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;

/**
 * Test case for the minimize transformer
 */
public class MinimizeTransformerTestCase extends AbstractRuntimeTestCase
{
    private static Pattern MINIMIZED_URLS_PATTERN = Pattern.compile("['\"]/plugins/core-ui/resources-minimized/([^/.'\"]+)\\.([^/.'\"]+)['\"]");

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
        String filteredResult = result.replaceAll("/plugins/core-ui/resources-minimized/[^/.]+\\.(js|css)", "/plugins/core-ui/resources-minimized/__HASH__.$1");
        String expected = IOUtils.toString(expectedSource.getInputStream(), "UTF-8");
        assertEquals("XML output differs", expected.replace("\r", ""), filteredResult.replace("\r", ""));

        Matcher matcher = MINIMIZED_URLS_PATTERN.matcher(result);
        
        _assertFilesMinimized(matcher, "css", "complete-test-css-1.css");
        _assertFilesMinimized(matcher, "css", "complete-test-css-2.css");
        _assertFilesMinimized(matcher, "js", "complete-test-js-1.js"); // File a.js does not exist, but hashed file should still generate
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
        
        Source source = _resolver.resolveURI("cocoon://_plugins/test/resources-minimized/" + file + "." + fileExtension);
        String actual = IOUtils.toString(source.getInputStream(), "UTF-8");
        assertEquals("Minimized file content did not match expected value", expected.replace("\r", ""), actual.replace("\r", ""));
    }
    
    /**
     * This test runs all the possible situations that the minimize transformer may encounter, though a single html file and all the minified results.
     * @throws Exception If an error occurs
     */
    public void testMinimizeTransformerLastModify() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        _resolver = (SourceResolver) Init.getPluginServiceManager().lookup(SourceResolver.ROLE);
        _assertLastModify("last-modify-test.html", "lastModify-create.js", "lastModify-edit.js", "js");
        
        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }

    private void _assertLastModify(String htmlFile, String fileToCreate, String fileToModify, String extension) throws Exception
    {
        // Delete temporary file to create if exists
        File file = new File("test/environments/webapp2/plugins/test/resources/js/minimize/" + fileToCreate);
        if (file.exists())
        {
            assertTrue("Unable to delete file for last modify tests", file.delete());
        }
        
        
        // Generate a hash with a non existing file
        String hashNotFound = _getHashFromSourceFile(htmlFile);
        List<FileData> filesForHash = MinimizeTransformer.getFilesForHash(hashNotFound);
        assertTrue("The hash cache is invalid", filesForHash.size() == 2 
                && filesForHash.get(0).getUri().equals("/plugins/test/resources/js/minimize/" + fileToCreate)
                && filesForHash.get(1).getUri().equals("/plugins/test/resources/js/minimize/" + fileToModify));
        
        _assertMinimizedEquals(hashNotFound + "." + extension, "/** ERROR Cannot get input stream for cocoon://plugins/test/resources/js/minimize/" + fileToCreate + "*/" 
                + "/** File : /plugins/test/resources/js/minimize/" + fileToModify + " */\nconsole.log(\"test\");\n");
        
        
        // Create the file and regenerate the hash
        assertTrue("Unable to create file to test last modify", file.createNewFile());
        String hashFound = _getHashFromSourceFile(htmlFile);
        assertFalse("Same hash after creating the file", hashNotFound.equals(hashFound));

        _assertMinimizedEquals(hashFound + "." + extension, "/** ERROR Index: 0, Size: 0*/"
                + "/** File : /plugins/test/resources/js/minimize/" + fileToModify + " */\nconsole.log(\"test\");\n");
        
        // Edit the file to generate a new hash
        Files.write(Paths.get("test/environments/webapp2/plugins/test/resources/js/minimize/" + fileToModify), Arrays.asList("/* Temporary test file */", "console.log('test')"), Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        
        String hashModified = _getHashFromSourceFile(htmlFile);
        assertFalse("Hash must change when file is modified", hashNotFound.equals(hashModified) || hashFound.equals(hashModified));

        _assertMinimizedEquals(hashModified + "." + extension, "/** ERROR Index: 0, Size: 0*/"
                + "/** File : /plugins/test/resources/js/minimize/" + fileToModify + " */\nconsole.log(\"test\");\n");

        String hashNoChange = _getHashFromSourceFile(htmlFile);
        assertEquals("Hash must not change when there is no modifications", hashModified, hashNoChange);
        
        // Delete temporary file created at the end
        if (file.exists())
        {
            assertTrue("Unable to delete file after last modify tests", file.delete());
        }
    }

    private void _assertMinimizedEquals(String hashedFile, String expected) throws MalformedURLException, IOException, SourceNotFoundException
    {
        Source source = _resolver.resolveURI("cocoon://_plugins/test/resources-minimized/" + hashedFile);
        String contentFound = IOUtils.toString(source.getInputStream(), "UTF-8");

        assertEquals("Minimized file content is not correct", expected, contentFound);
    }

    private String _getHashFromSourceFile(String fileSource) throws MalformedURLException, IOException, SourceNotFoundException
    {
        Source source = _resolver.resolveURI("cocoon://_plugins/test/minimize/" + fileSource);
        String sourceContent = IOUtils.toString(source.getInputStream(), "UTF-8");
        Matcher matcher = MINIMIZED_URLS_PATTERN.matcher(sourceContent);
        assertTrue("File was not successfully minimized", matcher.find());
        return matcher.group(1);
    }
}
