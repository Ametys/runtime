package org.ametys.runtime.plugins.core.sqlmap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Implementation which use the extension id as the avalon role
 * of the component to retrieve.<br>
 * This target component must implements <code>SqlMapClientsAware</code> in order
 * to be injected with one or more <code>SqlMapClient</code> instances.
 */
public class SelfSqlMapClientComponentProvider implements SqlMapClientComponentProvider, Configurable
{
    private String _role;

    public void configure(Configuration configuration) throws ConfigurationException
    {
        // Use the extension id for selecting the component to inject SqlMap with
        _role = configuration.getAttribute("id");
    }
    
    public String getComponentRole()
    {
        return _role;
    }
}
