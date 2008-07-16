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
package org.ametys.runtime.cocoon;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;

import org.ametys.runtime.plugin.component.AbstractSelectorExtensionPoint;
import org.ametys.runtime.util.ConnectionHelper;

/**
 * DataSourceComponentSelector extending the Cocoon one, implemented as an ExtensionPoint
 */
public class DataSourceComponentsExtensionPoint extends AbstractSelectorExtensionPoint<DataSourceComponent>
{
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        // Initialisation du ConnectionManager
        ConnectionHelper.setSelector(this);
    }
}
