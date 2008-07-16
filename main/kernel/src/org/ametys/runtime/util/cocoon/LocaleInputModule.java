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
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.i18n.I18nUtils;

/**
 * Input module for getting informations about currently used locale.<br>
 * The search algorithm is the same as {@link org.apache.cocoon.i18n.I18nUtils}.<br>
 * 3 variables are available : "locale", "country" and "language"
 */
public class LocaleInputModule implements InputModule, ThreadSafe
{
    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException
    {
        Locale locale = I18nUtils.findLocale(objectModel, "locale", null, Locale.getDefault(), true);

        if ("locale".equals(name))
        {
            return locale;
        }
        else if ("country".equals(name))
        {
            return locale.getCountry();
        }
        else if ("language".equals(name))
        {
            return locale.getLanguage();
        }

        return null;
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
