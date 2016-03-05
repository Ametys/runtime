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
package org.ametys.core.cocoon;

import java.util.Locale;

import org.apache.avalon.framework.component.ComponentException;

/**
 * This XMLResourceBundleFactory creates a <code>Bundle</code> for XML resources which can be invalidated
 *
 */
public class XMLResourceBundleFactory extends org.apache.cocoon.i18n.XMLResourceBundleFactory
{
    @Override
    protected org.apache.cocoon.i18n.XMLResourceBundle create(String sourceURI, Locale locale, org.apache.cocoon.i18n.XMLResourceBundle parent)
    {
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("Creating bundle <" + sourceURI + ">");
        }

        XMLResourceBundle bundle = new XMLResourceBundle(sourceURI, locale, parent);
        bundle.enableLogging(getLogger());
        bundle.reload(manager, resolver, interval);
        return bundle;
    }
    
    /**
     * Invalidate catalogue 
     * @param location catalogue base location (URI)
     * @param name bundle name
     * @param localeName  locale name
     * @throws ComponentException If an error occurred
     */
    public void invalidateCatalogue (String location, String name, String localeName) throws ComponentException
    {
        XMLResourceBundle b = (XMLResourceBundle) select(location, name, new Locale(localeName));
        b.invalidate();
    }
     
}
