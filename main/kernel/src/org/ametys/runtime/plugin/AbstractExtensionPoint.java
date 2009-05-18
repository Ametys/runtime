package org.ametys.runtime.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;

public abstract class AbstractExtensionPoint<T> implements ExtensionPoint<T>, Initializable
{
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
