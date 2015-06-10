/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.core.cocoon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.serializers.XMLSerializer;
import org.xml.sax.SAXException;

/**
 * Inherits from cocoon's serializers block XMLSerializer.<p>
 * Empty tags are not collapsed except the ones configured with
 * <code>tags-to-collapse</code>.<br>
 * If there is no such configuration, default tags to collaspe are:
 * <ul>
 *   <li>input</li>
 *   <li>img</li>
 *   <li>meta</li>
 *   <li>link</li>
 *   <li>hr</li>
 *   <li>br</li>
 * </ul>
 */
public class XHTMLFragmentSerializer extends XMLSerializer
{
    /** List of the tags to collapse. */
    private static final Set<String> __COLLAPSE_TAGS = new HashSet<>(Arrays.asList(
        new String[] {"input", "img", "meta", "link", "hr", "br"}));
    
    
    /** Buffer to store tag to collapse. */
    private Set<String> _tagsToCollapse;
    
    @Override
    public void configure(Configuration conf) throws ConfigurationException
    {
        super.configure(conf);
        
        // Tags to collapse
        String tagsToCollapse = conf.getChild("tags-to-collapse").getValue(null);
        
        if (tagsToCollapse != null)
        {
            _tagsToCollapse = new HashSet<>();
            for (String tag : tagsToCollapse.split(","))
            {
                _tagsToCollapse.add(tag.trim());
            }
        }
        else
        {
            _tagsToCollapse = __COLLAPSE_TAGS;
        }
    }
    
    @Override
    public void endElementImpl(String uri, String local, String qual) throws SAXException
    {
        // If the element is not in the list of the tags to collapse, close it without collapsing
        if (!_tagsToCollapse.contains(local))
        {
            this.closeElement(false);
        }
        
        super.endElementImpl(uri, local, qual);
    }
    
}
