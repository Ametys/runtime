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
package org.ametys.core.authentication;

import java.util.Map;

import org.apache.avalon.framework.component.Component;

import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Abstract implementation of {@link CredentialProvider}, which is configurable.
 * Extends this class for implementing a CredentialProvider (and implement {@link NonBlockingCredentialProvider},
 * {@link BlockingCredentialProvider} or both)
 */
public abstract class AbstractCredentialProvider extends AbstractLogEnabled implements CredentialProvider, Component
{
    private String _cpModelId;
    private Map<String, Object> _paramValues;
    private String _label;
    private String _id;
    
    public String getId()
    {
        return _id;
    }
    
    @Override
    public String getLabel()
    {
        return _label;
    }
    
    @Override
    public String getCredentialProviderModelId()
    {
        return _cpModelId;
    }

    @Override
    public Map<String, Object> getParameterValues()
    {
        return _paramValues;
    }

    @Override
    public void init(String id, String cpModelId, Map<String, Object> paramValues, String label)
    {
        _id = id;
        _cpModelId = cpModelId;
        _paramValues = paramValues;
        _label = label;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof CredentialProvider))
        {
            return false;
        }
        
        CredentialProvider secondCp = (CredentialProvider) obj;
        // Two credential providers are said equals if and only if their model id is equal and their parameter values are equals
        return _cpModelId.equals(secondCp.getCredentialProviderModelId()) && _paramValues.equals(secondCp.getParameterValues());
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_cpModelId == null) ? 0 : _cpModelId.hashCode());
        result = prime * result + ((_paramValues == null) ? 0 : _paramValues.hashCode());
        return result;
    }
    
}
