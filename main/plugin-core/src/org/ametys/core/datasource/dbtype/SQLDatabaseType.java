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

import org.ametys.runtime.i18n.I18nizableText;

/**
 * Interface for the SQL database types
 */
public interface SQLDatabaseType
{
    /**
     * Get the id of the database type.
     * @return the id. Can not be null.
     */
    public String getId();
    
    /**
     * Get the label of the database type.
     * @return the label
     */
    public I18nizableText getLabel();
    
    /**
     * Get the driver of the database type.
     * @return the driver. Can not be null.
     */
    public String getDriver();
    
    /**
     * Get the url template to use for this database type
     * @return the url template
     */
    public String getTemplate();
    
    /**
     * Get the error message key when the specified driver was not found
     * @return the error message 
     */
    public I18nizableText getDriverNotFoundMessage();
}

