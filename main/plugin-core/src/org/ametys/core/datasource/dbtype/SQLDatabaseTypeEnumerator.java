/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.core.datasource.dbtype;

import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;

/**
 * Enumerator for {@link SQLDatabaseType}.
 */
public class SQLDatabaseTypeEnumerator implements Enumerator, Serviceable
{
    /** The extension point handling the database types */
    private SQLDatabaseTypeExtensionPoint _sqlDatabaseTypeExtensionPoint;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _sqlDatabaseTypeExtensionPoint = (SQLDatabaseTypeExtensionPoint) manager.lookup(SQLDatabaseTypeExtensionPoint.ROLE);
    }
    
    @Override
    public I18nizableText getEntry(String value) throws Exception
    {
        SQLDatabaseType dbType = _sqlDatabaseTypeExtensionPoint.getExtension(value);
        return dbType != null ? dbType.getLabel() : null;
    }

    @Override
    public Map<Object, I18nizableText> getEntries() throws Exception
    {
        return _sqlDatabaseTypeExtensionPoint.getSQLDatabaseTypes();
    }
}
