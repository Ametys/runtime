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
package org.ametys.runtime.plugin.component;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;

/**
 * Bridge between the plugin stuff and Cocoon ComponentSelectors.<br>
 * The aim is to provide a way to declare InputModules, DataSources, SourceFactories, ... at the plugin level.<br>
 * Selectable components are declared as extensions to this extension point and may be used as normal Cocoon components.
 * @param <T> the type of the managed extensions. Must extends Component to be compatible with the ComponentSelector contract
 */
public class AbstractSelectorExtensionPoint<T extends Component> extends AbstractThreadSafeComponentExtensionPoint<T> implements ParentAware, ComponentSelector
{
    private ComponentSelector _parentSelector;
    
    public Component select(Object hint) throws ComponentException
    {
        if (!(hint instanceof String))
        {
            throw new IllegalArgumentException("This kind of selector only accepts Strings as hint values.");
        }
        
        String id = (String) hint;
        
        T extension = getExtension(id);
        
        if (extension != null)
        {
            return extension;
        }
        else if (_parentSelector != null)
        {
            return _parentSelector.select(id);
        }
        
        throw new ComponentException(id, "Could not find component");
    }
    
    public boolean hasComponent(Object hint)
    {
        boolean exists = hasExtension((String) hint);
       
        if (!exists && _parentSelector != null) 
        {
            exists = _parentSelector.hasComponent(hint);
        }
        
        return exists;
    }

    @SuppressWarnings("unchecked")
    public void release(Component component)
    {
        if (!_manager.hasComponent((T) component) && _parentSelector != null)
        {
            _parentSelector.release(component);
        } 
    }

    public void setParent(Object parentComponent)
    {
        _parentSelector = (ComponentSelector) parentComponent;
    }
}
