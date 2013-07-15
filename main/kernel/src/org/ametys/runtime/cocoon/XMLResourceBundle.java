/*
 *  Copyright 2013 Anyware Services
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

import java.util.Locale;

import org.apache.cocoon.i18n.Bundle;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.runtime.util.cocoon.InvalidSourceValidity;

/**
 * This implementation of <code>Bundle</code> interface for XML resources allows to be invalidated. 
 *
 */
public class XMLResourceBundle extends org.apache.cocoon.i18n.XMLResourceBundle
{
    /**
     * Construct a bundle.
     * @param sourceURI source URI of the XML bundle
     * @param locale locale
     * @param parentBundle parent bundle of this bundle
     */
    public XMLResourceBundle(String sourceURI, Locale locale, Bundle parentBundle)
    {
        super(sourceURI, locale, parentBundle);
    }

    @Override
    public boolean reload(SourceResolver resolver, long interval)
    {
        return super.reload(resolver, interval);
    }
    
    /**
     * Invalidate bundle
     */
    public void invalidate ()
    {
        this.validity = new InvalidSourceValidity();
    }
}
