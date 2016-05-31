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
package org.ametys.plugins.core.ui.resources;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;

import io.bit3.jsass.annotation.Name;
import io.bit3.jsass.type.SassString;

/**
 * This helper provides function that will be directly injected into Sass file compilation. 
 * Inherited methods are NOT injected. For this reason, method that must not be accessed from Sass have to be declared in the parent class AbstractAmetysSASSHelper
 */
public class AmetysSASSHelper extends AbstractAmetysSASSHelper implements Component
{
    /** Avalon ROLE. */
    public static final String ROLE = AmetysSASSHelper.class.getName();
    
    /**
     * Resolve url path inside plugins
     * @param plugin The plugin name. If omitted, the current plugin name will be used.
     * @param path the path relative to the current plugin
     * @return the absolute path
     */
    public SassString pluginUrl(@Name("plugin") String plugin, @Name("path") String path)
    {
        Request request = ContextHelper.getRequest(_context);
        String resourcePath = "/plugins/" + plugin + path;
        SassString url = new SassString("url(\"" + request.getContextPath() + resourcePath + "\")", false);
        return url;
    }
}
