/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.user.jdbc;

import org.ametys.runtime.util.parameter.Parameter;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;


/**
 * Handle a typed parameter for the config extension point.
 */
public class JdbcParameter extends Parameter<ParameterType>
{
    private String _column;
    
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
