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

/**
 * @private
 * The model used in the {@link Ametys.plugins.admin.datasource.DataSourceTool} grid
 */
Ext.define('Ametys.plugins.admin.datasource.DataSourceTool.DataSource', {
    extend: 'Ext.data.Model',
    
    fields: [
       {name: 'id', mapping: 'id'},
       {name: 'name', mapping: 'name'},
       {name: 'isInUse', mapping: 'isInUse', type: 'boolean'},
       {name: 'private', mapping: 'private', type: 'boolean'},
       {name: 'isValid', mapping: 'isValid', type: 'boolean'},
       {name: 'type', mapping: 'type'}
    ]
});