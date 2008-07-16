package org.ametys.runtime.plugins.core.sqlmap;

import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Interface to be implemented by any object that wishes to have
 * access to one or multiple SqlMapClient.
 */
public interface SqlMapClientsAware
{
    /**
     * Set multiple SqlMapClients instances.<br>
     * They are grouped by pool name.
     * @param instances SqlMapClients instances.
     */
    public void setSqlMapClients(Map<String, SqlMapClient> instances);
}
