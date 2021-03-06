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
package org.ametys.plugins.core.impl.right;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ametys.core.right.AbstractStaticRightAssignmentContext;
import org.ametys.core.right.RightAssignmentContext;

/**
 * {@link RightAssignmentContext} for assign rights to a configured context
 */
public class StringRightAssignmentContext extends AbstractStaticRightAssignmentContext
{
    @Override
    public Object convertJSContext(Object context)
    {
        if (context instanceof String)
        {
            return context;
        }
        return null;
    }
    
    @Override
    public Object getParentContext(Object context)
    {
        return null;
    }
    
    @Override
    public List<Object> getRootContexts(Map<String, Object> contextParameters)
    {
        List<Object> rootContexts = new ArrayList<>();
        
        if (matchWorkspace(contextParameters))
        {
            rootContexts.add(_script.getParameters().get("context"));
        }
        return rootContexts;
    }
}
