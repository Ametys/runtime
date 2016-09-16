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
package org.ametys.core.right;

import org.ametys.core.ui.AbstractClientSideExtensionPoint;
import org.ametys.core.ui.Callable;
import org.ametys.runtime.plugin.ExtensionPoint;

/**
 * {@link ExtensionPoint} handling {@link RightAssignmentContext}s
 */
public class RightAssignmentContextExtensionPoint extends AbstractClientSideExtensionPoint<RightAssignmentContext>
{
    /** Avalon Role */
    public static final String ROLE = RightAssignmentContextExtensionPoint.class.getName();
    
    /**
     * From a JavaScript context object, converts it into a Java object
     * @param context the JS context object
     * @param extensionId The id of the {@link RightAssignmentContext} extension
     * @return the Java object
     */
    @Callable
    public Object convertJSContext(Object context, String extensionId)
    {
        return getExtension(extensionId).convertJSContext(context);
    }
    
}
