/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.right.actions;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;


/**
 * This action determines if the user is in admin.<br/>
 * Throw an IllegalStateException if not
 */
public class IsAdminAction extends CurrentUserProviderServiceableAction 
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (_isSuperUser())
        {
            return EMPTY_MAP;
        }
        else
        {
            throw new IllegalStateException("This action can be used only by system administrator.");
        }
    }
}
