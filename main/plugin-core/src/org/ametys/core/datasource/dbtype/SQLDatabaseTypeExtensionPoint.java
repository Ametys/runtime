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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * This class is in charge to load and initialize the {@link SQLDatabaseType} 
 */
public class SQLDatabaseTypeExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<SQLDatabaseType>
{
    /** Avalon Role */
    public static final String ROLE = SQLDatabaseTypeExtensionPoint.class.getName();
    
    @Override
    public void initializeExtensions() throws Exception
    {
        super.initializeExtensions();
        
        for (String databaseTypeId : getExtensionsIds())
        {
            SQLDatabaseType databaseType = getExtension(databaseTypeId);
            
            try
            {
                Class.forName(databaseType.getDriver());
            }
            catch (ClassNotFoundException e)
            {
                getLogger().warn("JDBC Driver cannot be found for extension '" + databaseTypeId + "' with classname '" + databaseType.getDriver() + "'");
            }
        }
    }
    
    /**
     * Get the SQL database types with their label
     * @return the SQL database types with their label
     */
    public Map<Object, I18nizableText> getSQLDatabaseTypes()
    {
        Map<Object, I18nizableText> db = new HashMap<>();
        
        Set<String> extensionsIds = getExtensionsIds();
        for (String extensionId : extensionsIds)
        {
            SQLDatabaseType dbType = getExtension(extensionId);
            db.put(extensionId, dbType.getLabel());
        }
        
        return db;
    }
}
