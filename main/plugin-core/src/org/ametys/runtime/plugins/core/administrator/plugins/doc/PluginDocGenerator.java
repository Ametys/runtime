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

package org.ametys.runtime.plugins.core.administrator.plugins.doc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.util.IgnoreRootHandler;

/**
 * Generates all the plugins.xml
 */
public class PluginDocGenerator extends ServiceableGenerator
{
    private SourceResolver _sourceResolver;
    private SAXParser _saxParser;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        
        _sourceResolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
        _saxParser = (SAXParser) smanager.lookup(SAXParser.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "plugins");

        Set<String> pluginNames = PluginsManager.getInstance().getPluginNames();
        for (String pluginName : pluginNames)
        {
            _saxPlugin(pluginName);
        }

        XMLUtils.endElement(contentHandler, "plugins");
        contentHandler.endDocument();
    }

    private void _saxPlugin(String pluginName) throws IOException, SAXException
    {
        Source pluginSource = _sourceResolver.resolveURI("plugin:" + pluginName + "://plugin.xml");
        try (InputStream is = pluginSource.getInputStream())
        {
            _saxParser.parse(new InputSource(is), new SpecialHandler(contentHandler, pluginName));
        }
        finally
        {
            _sourceResolver.release(pluginSource);
        }
    }

    /**
     * Ignore root handler that set the plugin name on the root tag 
     */
    public class SpecialHandler extends IgnoreRootHandler
    {
        private String _pluginName;
        private int _level;
        
        /**
         * Create a handler
         * @param handler The handler to wrap
         * @param pluginName The plugin name to set on root tag
         */
        public SpecialHandler(ContentHandler handler, String pluginName)
        {
            super(handler);
            _pluginName = pluginName;
        }
        
        /* (non-Javadoc)
         * @see org.apache.excalibur.xml.sax.ContentHandlerProxy#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException
        {
            _level++;
            
            if (_level == 1)
            {
                AttributesImpl attr = new AttributesImpl(a);
                attr.addCDATAAttribute("name", _pluginName);

                super.startElement(uri, loc, raw, attr);
            }
            else
            {
                super.startElement(uri, loc, raw, a);
            }
        }
        
        @Override
        public void endElement(String uri, String loc, String raw) throws SAXException
        {
            _level--;
            
            super.endElement(uri, loc, raw);
        }
    }
}
