/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.plugins.admin.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.datasource.DataSourceDAO;

/**
 * Retrieve the data sources and format them in JSON
 */
public class GetDataSourcesAction extends ServiceableAction
{
    /** The manager handling the SQL data sources */
    private DataSourceDAO _dataSourceDAO; 
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _dataSourceDAO = (DataSourceDAO) smanager.lookup(DataSourceDAO.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, Object> result = new HashMap<> ();

        Request request = ObjectModelHelper.getRequest(objectModel);
        
        boolean includePrivate = parameters.getParameterAsBoolean("includePrivate", false);
        boolean includeInternal = parameters.getParameterAsBoolean("includeInternal", false);
        String dstype = parameters.getParameter("type", null);
        
        List<Map<String, Object>> dataSources = _dataSourceDAO.getDataSources(dstype, includePrivate, includeInternal);
        result.put("datasources", dataSources);
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        return EMPTY_MAP;
    }
}
