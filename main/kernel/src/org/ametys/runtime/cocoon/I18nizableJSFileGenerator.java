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
package org.ametys.runtime.cocoon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.generation.FileGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This generator reads a text file that contains xml tags, and add xml
 * declaration and a firstlevel tag.<br>
 * The following file:<br>
 * I am a &lt;i18n:text i18n:key="test"/&gt;.<br>
 * Will be read as:<br>
 * &lt;?xml version="1.0" encoding="UTF-8"&gt;
 * &lt;xml xmlns:i18n="http://apache.org/cocoon/i18n/2.1"&lt;
 * I am a &lt;i18n:text i18n:key="test"/&lt;.
 * &lt;/xml&lt;<br>
 * And so will sax events correctly.
 */
public class I18nizableJSFileGenerator extends FileGenerator
{
    private static final String[] __MATCH_XML = new String[] {"&", "<"}; 
    private static final String[] __REPLACE_XML = new String[] {"&amp;", "&lt;"};
    private static final String[] __MATCH_I18N = new String[] {"&lt;i18n:", "&lt;/i18n:"};
    private static final String[] __REPLACE_I18N = new String[] {"<i18n:", "</i18n:"};
    
    private final String _tagName = "xml";
    private final String _encoding = "UTF-8";
    private final String _nameSpaces = "xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\"";
    
    private SAXParser _saxParser;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String fileContent = _getFileContent();
        String modifiedFile = _saveXMLChars(fileContent);
        String finalFile = "<?xml version=\"1.0\" encoding=\"" + _encoding + "\"?><" + _tagName + " " + _nameSpaces + ">" + modifiedFile + "</" + _tagName + ">";
        
        InputStream is = new ByteArrayInputStream(finalFile.getBytes(_encoding)); 
        InputSource isource = new InputSource(is);
        isource.setSystemId(this.inputSource.getURI());
        
        _saxParser.parse(isource, contentHandler);
    }

    /**
     * Get the source content
     * @return The content of the file
     * @throws MalformedURLException if an error occurred
     * @throws IOException if an error occurred
     * @throws ProcessingException if an error occurred
     */
    private String _getFileContent() throws MalformedURLException, IOException, ProcessingException
    {
        try (InputStream is = inputSource.getInputStream())
        {
            return IOUtils.toString(is, _encoding);
        } 
        catch (SourceException se) 
        {
            throw SourceUtil.handle("Error during resolving of '" + this.source + "'.", se);
        }
    }
    
    private String _saveXMLChars(String s)
    {
        // First escape & and <, then unescape &lt;i18n: and &lt;/i18n:
        return StringUtils.replaceEach(StringUtils.replaceEach(s, __MATCH_XML, __REPLACE_XML), __MATCH_I18N, __REPLACE_I18N);
    }
}
