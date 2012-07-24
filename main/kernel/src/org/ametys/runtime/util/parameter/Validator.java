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
package org.ametys.runtime.util.parameter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Validator for parameters values 
 */
public interface Validator
{
    /**
     * Validates a value.
     * @param value the value to validate (can be <code>null</code>).
     * @param errors the structure to populate if the validation failed.
     */
    public void validate(Object value, Errors errors);
    
    /**
     * Sax the configuration of the validator to allow the client side to prevalidate
     * @param handler The content handler where to sax parameters
     * @throws SAXException if an error occured
     */
    public void saxConfiguration(ContentHandler handler) throws SAXException;
}
