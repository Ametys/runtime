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
package org.ametys.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Default implementation of the ClientSideRelation that expects the configuration to define the supported relations.
 */
public class StaticClientSideRelation extends StaticClientSideElement implements ClientSideRelation
{
    List<String> _sourceRelationType;
    List<String> _targetRelationType;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _sourceRelationType = new ArrayList<>();
        _targetRelationType = new ArrayList<>();
        List<String> messageTargetDependencies = new ArrayList<>();
        
        if (configuration.getChild("relations", false) != null)
        {
            for (Configuration relationConfiguration : configuration.getChild("relations").getChildren())
            {
                String relationType = relationConfiguration.getName();
                String relation = relationConfiguration.getValue();
                
                if ("source".equals(relationType))
                {
                    _sourceRelationType.add(relation);
                    messageTargetDependencies.add(relation);
                }
                else if ("target".equals(relationType))
                {
                    _targetRelationType.add(relation);
                    messageTargetDependencies.add(relation);
                }
                else
                {
                    throw new ConfigurationException("Unsupported relation type for StaticClientSideRelation : " + relationType, configuration);
                }
            }
        }
        
        super.configure(configuration);
        
        for (String messageTargetFactory : messageTargetDependencies)
        {
            _addMessageTargetDependency(messageTargetFactory);
        }
    }
    
    private void _addMessageTargetDependency(String messageTargetFactory)
    {
        if (_dependencies.containsKey(MessageTargetFactoriesManager.ROLE))
        {
            List<String> extensions = _dependencies.get(MessageTargetFactoriesManager.ROLE);
            extensions.add(messageTargetFactory);
        }
        else
        {
            List<String> extensions = new ArrayList<>();
            extensions.add(messageTargetFactory);
            _dependencies.put(MessageTargetFactoriesManager.ROLE, extensions);
        }
    }

    @Override
    public List<String> getSourceRelationType()
    {
        return _sourceRelationType;
    }

    @Override
    public List<String> getTargetRelationType()
    {
        return _targetRelationType;
    }
}
