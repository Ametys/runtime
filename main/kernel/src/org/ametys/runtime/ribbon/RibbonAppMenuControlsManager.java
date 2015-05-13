/*
 *  Copyright 2011 Anyware Services
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
package org.ametys.runtime.ribbon;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;
import org.ametys.runtime.ui.ClientSideElement;

/**
 * This extension point handle the application items of Ametys menu
 */
public class RibbonAppMenuControlsManager extends AbstractThreadSafeComponentExtensionPoint<ClientSideElement>
{
    /** Avalon role */
    public static final String ROLE = RibbonAppMenuControlsManager.class.getName();
}
