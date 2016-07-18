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
 * Field displaying a combobox allowing to select one of the available LDAP data sources. Optionally, 
 * this field can allow the addition of other LDAP data sources. 
 */
Ext.define('Ametys.form.widget.LDAPDataSource', {
	extend: 'Ametys.form.widget.AbstractDataSource',
    alias: ['widget.datasource-ldap'],
	
    dataSourceType: 'LDAP',
    createButtonIconCls: 'ametysicon-agenda3 decorator-ametysicon-add64',
    createButtonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_LDAP_DATASOURCE_BUTTON_TOOLTIP}}",
    
    createDataSource: function (callback)
    {
        Ametys.plugins.admin.datasource.EditLDAPDataSourceHelper.add(callback);
    }
});
