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
package org.ametys.runtime.plugins.core.sqlmap.dao;

import java.sql.SQLException;
import java.util.Map;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.plugins.core.sqlmap.SqlMapClientsAware;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Interface to be implemented by any object that wishes to have
 * access to one or multiple SqlMapClient.
 */
public abstract class AbstractDAO implements SqlMapClientsAware
{
    private SqlMapClient _defaultInstance;
    private Map<String, SqlMapClient> _instances;
    
    /**
     * Retrieve default SqlMapClient instance, the one using the core pool
     * or the instance if there is only one instance.<br>
     * May return <code>null</code> if there are multiple instances and
     * none is related to the core pool.
     * @return the default SqlMapClient instance.
     */
    protected SqlMapClient _getSqlMapClient()
    {
        return _defaultInstance;
    }
    
    /**
     * Retrieve a particular SqlMapClient instance related to a pool name.
     * @param poolName the pool name.
     * @return the SqlMapClient instance or <code>null</code> if not found.
     */
    protected SqlMapClient _getSqlMapClient(String poolName)
    {
        return _instances.get(poolName);
    }

    public void setSqlMapClients(Map<String, SqlMapClient> instances)
    {
        _instances = instances;
        
        if (_instances.size() == 1)
        {
            for (SqlMapClient instance : _instances.values())
            {
                _defaultInstance = instance;
            }
        }
        else
        {
            _defaultInstance = _instances.get(ConnectionHelper.CORE_POOL_NAME);
        }
    }
    
    /**
     * Wrapper for translating SQLException to DataAccessException.
     * @param <T> the result of the process.
     */
    protected abstract class AbstractSQLExceptionWrapper<T>
    {
        private SqlMapClient _sqlMapClient;
        
        /**
         * Use default SqlMapClient instance.
         */
        public AbstractSQLExceptionWrapper()
        {
            _sqlMapClient = _getSqlMapClient();
        }
        
        /**
         * Use a given SqlMapClient instance.
         * @param sqlMapClient the sqlMapClient instance to use.
         */
        public AbstractSQLExceptionWrapper(SqlMapClient sqlMapClient)
        {
            _sqlMapClient = sqlMapClient;
        }
        
        /**
         * Provide SqlMapClient to execute one or more queries.
         * @return the result.
         * @throws DataAccessException if an error occurs.
         */
        public T process() throws DataAccessException
        {
            try
            {
                return processWithSqlMapClient(_sqlMapClient);
            }
            catch (SQLException e)
            {
                throw new DataAccessException(e);
            }
        }
        
        /**
         * Use SqlMapClient to execute one or more queries.
         * @param sqlMapClient the sqlMapClient instance to use.
         * @return the result.
         * @throws DataAccessException if a data access error occurs.
         * @throws SQLException if an error occurs.
         */
        public abstract T processWithSqlMapClient(SqlMapClient sqlMapClient) throws DataAccessException, SQLException;
    }
}
