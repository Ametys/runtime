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
package org.ametys.runtime.util.cocoon;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * This input module read in parent context.<br/>
 * Use it with {parent-context-attr:paramName}.
 */
public class ParentContextInputModule implements InputModule
{

    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        Map parameters = (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        if (parameters == null)
        {
            return null;
        }

        return parameters.get(name);
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        return null;
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        return new Object[] {getAttribute(name, modeConf, objectModel)};
    }

}
