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

import org.ametys.core.ui.ClientSideElement;

/**
 * This interface represents a context of right assignment
 */
public interface RightAssignmentContext extends ClientSideElement
{
    /**
     * From a JavaScript context object, converts it into a Java object
     * @param jsContext the JS context object
     * @return the Java object
     */
    public Object convertJSContext(Object jsContext);
    
    /**
     * Get the parent context of the object context
     * @param context The object context
     * @return The parent if exist or <code>null</code> otherwise
     */
    public Object getParentContext(Object context);
}
