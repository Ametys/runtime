package org.ametys.runtime.plugins.core.sqlmap;


/**
 * Provide SqlMap to a component.<br>
 * This component must implements <code>SqlMapClientSupport</code>.
 */
public interface SqlMapClientComponentProvider
{
    /**
     * Retrieve the role to use for accessing the component which
     * will be injected with SqlMap.
     * @return the avalon role of the component.
     */
    public String getComponentRole();
}
