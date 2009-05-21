package org.ametys.runtime.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;

/**
 * Abstract implementation of an extension point.<br>
 * Contains only helper methods. Tha actual job has to be done in the {@link #addExtension(String, String, org.apache.avalon.framework.configuration.Configuration)}
 * @param <T> the type of the managed extensions
 */
public abstract class AbstractExtensionPoint<T> implements ExtensionPoint<T>, Initializable
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
