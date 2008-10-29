package org.ametys.runtime.test.cocoon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.ametys.runtime.cocoon.XHTMLSerializer;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Test {@link XHTMLSerializer} with different input.
 */
public class XHTMLSerializerTestCase extends TestCase
{
    /**
     * Create the test case.
     * @param name the test case name.
     */
    public XHTMLSerializerTestCase(String name)
    {
        super(name);
    }
    
    /**
     * Test XHTMLSerializer.
     * @throws Exception if an error occurs.
     */
    public void testXHTMLSerializer() throws Exception
    {
        XHTMLSerializer serializer = new XHTMLSerializer();

        serializer.configure(_getConfiguration());
        
        // XML content to validate
        for (String content : Arrays.asList(new String[] {"simple", "complex", "namespace"}))
        {
            _assertSerialization(serializer, content + ".xml", content + "-result.xhtml");
            serializer.recycle();
        }
    }

    private void _assertSerialization(XHTMLSerializer serializer, String inputFilename, String outputFilename) throws Exception
    {
        InputStream inputIs = getClass().getResourceAsStream(inputFilename);
        InputStream outputIs = getClass().getResourceAsStream(outputFilename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try
        {
            assertNotNull("Missing input " + inputFilename, inputIs);
            assertNotNull("Missing output " + outputFilename, outputIs);
            
            
            serializer.setOutputStream(baos);
            
            PasstroughDefaultHandler handler = new PasstroughDefaultHandler(serializer);
            
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", handler); 
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", true); 
            saxParser.parse(inputIs, handler);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            
            try
            {
                _assertEquals(outputIs, bais);
            }
            finally
            {
                IOUtils.closeQuietly(inputIs);
            }
        }
        finally
        {
            IOUtils.closeQuietly(inputIs);
            IOUtils.closeQuietly(outputIs);
            IOUtils.closeQuietly(baos);
        }
    }

    private Configuration _getConfiguration()
    {
        DefaultConfiguration configuration = new DefaultConfiguration("serializer");
        
        configuration.setAttribute("name", "xhtml");
        configuration.setAttribute("mime-type", "text/html");

        DefaultConfiguration encodingConfig = new DefaultConfiguration("encoding");
        encodingConfig.setValue("UTF-8");
        DefaultConfiguration methodConfig = new DefaultConfiguration("method");
        methodConfig.setValue("xhtml");
        DefaultConfiguration doctypePublicConfig = new DefaultConfiguration("doctype-public");
        methodConfig.setValue("-//W3C//DTD XHTML 1.0 Transitional//EN");
        DefaultConfiguration doctypeSystemConfig = new DefaultConfiguration("doctype-system");
        methodConfig.setValue("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
        DefaultConfiguration omitXmlDeclarationConfig = new DefaultConfiguration("omit-xml-declaration");
        methodConfig.setValue("no");

        configuration.addChild(encodingConfig);
        configuration.addChild(methodConfig);
        configuration.addChild(doctypePublicConfig);
        configuration.addChild(doctypeSystemConfig);
        configuration.addChild(omitXmlDeclarationConfig);
        
        return configuration;
    }
    
    private void _assertEquals(InputStream expectedIs, ByteArrayInputStream resultIs) throws IOException
    {
        String expected = IOUtils.toString(expectedIs, "UTF-8");
        String result = IOUtils.toString(resultIs, "UTF-8");

        assertEquals("XML output differs", expected, result);
    }

    private static class PasstroughDefaultHandler extends DefaultHandler implements LexicalHandler
    {
        private XMLConsumer _xmlConsumer;
        
        public PasstroughDefaultHandler(XMLConsumer xmlConsumer)
        {
            _xmlConsumer = xmlConsumer;
        }
        
        @Override
        public void setDocumentLocator(Locator locator)
        {
            _xmlConsumer.setDocumentLocator(locator);
        }

        @Override
        public void startDocument() throws SAXException
        {
            _xmlConsumer.startDocument();
        }
        
        public void startDTD(String name, String publicId, String systemId) throws SAXException
        {
            _xmlConsumer.startDTD(name, publicId, systemId);
        }
        
        public void startEntity(String name) throws SAXException
        {
            _xmlConsumer.startEntity(name);
        }
        
        public void endEntity(String name) throws SAXException
        {
            _xmlConsumer.endEntity(name);
        }
        
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException
        {
            _xmlConsumer.startPrefixMapping(prefix, uri);
        }
        
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
        {
            _xmlConsumer.startElement(uri, localName, name, attributes);
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            _xmlConsumer.characters(ch, start, length);
        }

        public void comment(char[] ch, int start, int length) throws SAXException
        {
            _xmlConsumer.comment(ch, start, length);
        }
        
        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
        {
            _xmlConsumer.ignorableWhitespace(ch, start, length);
        }
        
        public void startCDATA() throws SAXException
        {
            _xmlConsumer.startCDATA();
        }
        
        public void endCDATA() throws SAXException
        {
            _xmlConsumer.endCDATA();
        }
        
        @Override
        public void processingInstruction(String target, String data) throws SAXException
        {
            _xmlConsumer.processingInstruction(target, data);
        }

        @Override
        public void skippedEntity(String name) throws SAXException
        {
            _xmlConsumer.skippedEntity(name);
        }
        
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException
        {
            _xmlConsumer.endElement(uri, localName, name);
        }
        
        @Override
        public void endPrefixMapping(String prefix) throws SAXException
        {
            _xmlConsumer.endPrefixMapping(prefix);
        }
        
        public void endDTD() throws SAXException
        {
            _xmlConsumer.endDTD();
        }
        
        @Override
        public void endDocument() throws SAXException
        {
            _xmlConsumer.endDocument();
        }
    }
}
