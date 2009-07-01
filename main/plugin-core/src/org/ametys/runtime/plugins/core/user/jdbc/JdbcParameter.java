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
package org.ametys.runtime.plugins.core.user.jdbc;

import org.ametys.runtime.util.parameter.Parameter;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;


/**
 * Handle a typed parameter for the config extension point.
 */
public class JdbcParameter extends Parameter<ParameterType>
{
    private String _id;
    private String _column;
    
    /**
     * Get the id.
     * @return the id.
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(String id)
    {
        _id = id;
    }

    /**
     * Get the JDBC column.
     * @return the JDBC column.
     */
    public String getColumn()
    {
        return _column;
    }
    
    /**
     * Set the JDBC column.
     * @param column the JDBC column.
     */
    public void setColumn(String column)
    {
        _column = column;
    }

    @Override
    public String toString()
    {
        StringBuilder parameter = new StringBuilder();
        
        parameter.append(getId());
        parameter.append("[type: ");
        parameter.append(ParameterHelper.typeToString(getType()));
        parameter.append(", labelKey:");
        parameter.append(getLabel().toString());
        parameter.append("]");
        
        return parameter.toString();
    }
}
