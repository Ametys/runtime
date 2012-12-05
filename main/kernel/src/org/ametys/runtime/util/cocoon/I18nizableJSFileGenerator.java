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
package org.ametys.runtime.util.cocoon;

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
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This generator reads a text file that contains xml tags, and add xml
 * declaration and a firstlevel tag.<br>
 * The following file:<br>
 * I am a <i18n:text i18n:key="test"/>.<br>
 * Will be read as:<br>
 * <?xml version="1.0" encoding="UTF-8">
 * &lt;xml xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
 * I am a &lt;i18n:text i18n:key="test"/>.
 * &lt;/xml><br>
 * And so will sax events correctly.
 */
public class I18nizableJSFileGenerator extends FileGenerator
{
    private String _tagName = "xml";
    private String _encoding = "UTF-8";
    private String _nameSpaces = "xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\"";
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
     * Get the source contne
     * @throws MalformedURLException
     * @throws IOException
     * @throws ProcessingException
     */
    private String _getFileContent() throws MalformedURLException, IOException, ProcessingException
    {
        InputStream is = null;
        try 
        {
            is = this.inputSource.getInputStream();
            return IOUtils.toString(is, _encoding);
        } 
        catch (SourceException se) 
        {
            throw SourceUtil.handle("Error during resolving of '" + this.source + "'.", se);
        }
        finally
        {
            IOUtils.closeQuietly(is);
            super.resolver.release(this.inputSource);
        }
    }
    
    private String _saveXMLChars(String s)
    {
        return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("&lt;i18n:", "<i18n:").replaceAll("&lt;/i18n:", "</i18n:");
    }
}
