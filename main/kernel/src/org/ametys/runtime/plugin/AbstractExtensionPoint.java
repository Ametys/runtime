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
package org.ametys.runtime.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Abstract implementation of an extension point.<br>
 * Contains only helper methods. Tha actual job has to be done in the {@link #addExtension(String, String, org.apache.avalon.framework.configuration.Configuration)}
 * @param <T> the type of the managed extensions
 */
public abstract class AbstractExtensionPoint<T> extends AbstractLogEnabled implements ExtensionPoint<T>, Initializable, Component
{
    /**
     * Map containing the extensions.<br>
     * The key is the unique id of the extension.
     */
    protected Map<String, T> _extensions;
    
    public void initialize() throws Exception
    {
        _extensions = new HashMap<String, T>();
    }
    
    public T getExtension(String id)
    {
        return _extensions.get(id);
    }

    public Set<String> getExtensionsIds()
    {
        return _extensions.keySet();
    }

    public boolean hasExtension(String id)
    {
        return _extensions.containsKey(id);
    }
}
