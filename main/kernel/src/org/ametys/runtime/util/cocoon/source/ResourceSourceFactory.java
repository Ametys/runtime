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
package org.ametys.runtime.util.cocoon.source;

import java.io.IOException;
import java.util.Map;

import org.apache.excalibur.source.Source;

/**
 * Extension of Excalibur ResourceSourceFactory to use own ResourceSource
 */
public final class ResourceSourceFactory extends org.apache.excalibur.source.impl.ResourceSourceFactory
{
    @Override
    public Source getSource(String location, Map parameters) throws IOException
    {
        return new ResourceSource(location);
    }
}
