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
package org.ametys.runtime.plugins.admin;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.plugins.core.right.HasRightAction;
import org.ametys.runtime.workspaces.admin.authentication.AdminAuthenticateAction;

/**
 * This action determines if the user is in admin.<br>
 * Throw an IllegalStateException if not
 */
public class HasNotAdminRightAction extends HasRightAction
{
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _hasRight = "true".equals(configuration.getChild("has-right").getValue("false"));
        _baseContext = configuration.getChild("base-context").getValue(AdminAuthenticateAction.ADMIN_RIGHT_CONTEXT);
    }
}
