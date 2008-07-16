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
package org.ametys.runtime.test.plugins;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

/**
 * Dumb Object used as test extension.
 * It implements Contextualizable for testing differences between ComponentBasedEP and "normal" EP
 */
public class TestExtension implements Contextualizable
{
    private Context _context;
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    /**
     * Returns the Avalon context
     * @return the Avalon context
     */
    public Context getContext()
    {
        return _context;
    }
}
