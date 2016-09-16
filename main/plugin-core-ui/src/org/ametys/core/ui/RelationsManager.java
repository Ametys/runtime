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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.commons.lang3.StringUtils;

/**
 * This extension point handle the existing relations on the client side (relation system allow for example drag and drop).
 */
public class RelationsManager extends AbstractClientSideExtensionPoint<ClientSideRelation>
{
    /** Avalon role */
    public static final String ROLE = RelationsManager.class.getName();
    
    @Override
    public List<ClientSideElement> findDependency(String pattern)
    {
        if (!StringUtils.startsWith(pattern, "source:") && !StringUtils.startsWith(pattern, "target:"))
        {
            throw new IllegalArgumentException("Invalid dependency : " + pattern + ", the prefix 'source:' or 'target:' is mandatory.");
        }
        
        if (StringUtils.startsWith(pattern, "source:"))
        {
            return _findSourceDependencies(StringUtils.substring(pattern, "source:".length()));
        }
        else if (StringUtils.startsWith(pattern, "target:"))
        {
            return _findTargetDependencies(StringUtils.substring(pattern, "target:".length()));
        }
        
        return null;
    }

    private List<ClientSideElement> _findSourceDependencies(String pattern)
    {
        List<ClientSideElement> result = new ArrayList<>();
        
        for (String extensionId : getExtensionsIds())
        {
            ClientSideRelation extension = getExtension(extensionId);
            if (extension.getSourceRelationType().contains(pattern))
            {
                result.add(extension);
            }
        }
        
        result.add(_getMessageTargetFactory(pattern));
        
        return result;
    }

    private List<ClientSideElement> _findTargetDependencies(String pattern)
    {
        List<ClientSideElement> result = new ArrayList<>();
        for (String extensionId : getExtensionsIds())
        {
            ClientSideRelation extension = getExtension(extensionId);
            if (extension.getTargetRelationType().contains(pattern))
            {
                result.add(extension);
            }
        }
        
        ClientSideElement messageTargetFactory = _getMessageTargetFactory(pattern);
        if (messageTargetFactory != null)
        {
            result.add(messageTargetFactory);
        }
        
        return result;
    }

    private ClientSideElement _getMessageTargetFactory(String pattern)
    {
        try
        {
            MessageTargetFactoriesManager messageTargetFactoriesManager = (MessageTargetFactoriesManager) _cocoonManager.lookup(MessageTargetFactoriesManager.ROLE);
            
            return messageTargetFactoriesManager.hasExtension(pattern) ? messageTargetFactoriesManager.getExtension(pattern) : null;
        }
        catch (ServiceException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Unable to retrieve the MessageTargetFactoriesManager", e);
            }
            
            return null;   
        }
    }
}
