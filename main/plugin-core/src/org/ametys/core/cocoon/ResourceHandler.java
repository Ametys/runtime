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
package org.ametys.core.cocoon;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * Interface used to handle resources
 */
public interface ResourceHandler
{
    /**
     * Initialize the resource handler with a resource.
     * @param resolver The source resolver
     * @param objectModel The object model
     * @param source The source
     * @param par The parameters
     * @throws IOException If an error occurs
     * @throws ProcessingException If an error occurs
     * @throws SAXException If an error occurs
     */
    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters par) throws IOException, ProcessingException, SAXException;
    
    /**
     * Generate the resource configured during setup, and output it
     * @param out The output stream to write to
     * @throws IOException If an error occurs
     * @throws ProcessingException If an error occurs
     */
    public void generateResource(OutputStream out) throws IOException, ProcessingException;
    
    /**
     * Return the mime type of the configured resource.
     * @return The mime type.
     */
    public String getMimeType();
    
    /**
     * Get the unique key for this resource, for cache purpose.
     * @return The cache key.
     */
    public Serializable getKey();
    
    /**
     * Get the resource validity, for cache purpose.
     * @return The resource validity.
     */
    public SourceValidity getValidity();
    
    /**
     * Get the resource size, if available.
     * @return The resource size.
     */
    public long getSize();
    
    /**
     * Get the resource last modified time
     * @return The last modified
     */
    public long getLastModified();
}
