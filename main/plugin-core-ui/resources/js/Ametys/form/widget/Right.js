/*
 *  Copyright 2016 Anyware Services
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
 * This class provides a widget to query and select one or more rights.
 * 
 * This widget is registered for fields of type Ametys.form.WidgetManager#TYPE_STRING.<br>
 */
Ext.define('Ametys.form.widget.Right', {
    extend: 'Ametys.form.AbstractQueryableComboBox',
    
    /**
     * @private
     * @property {Ext.data.Store} _store The store of the combobox
     */
    
    getStore: function()
    {
        this._store = Ext.create('Ext.data.Store', {
            autoDestroy: true,
            proxy: {
                type: 'ametys',
                plugin: 'core',
                url: 'rights/rights.json',
                reader: {
                    type: 'json',
                    rootProperty: 'rights'
                }
             },
             sorters: [{property: 'label', direction: 'ASC'}],
             fields: [
	             {name: 'id'},
	             {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}
             ]
        });
        
        return this._store;
    }
    
    /*getLabelTpl: function ()
    {
        var tpl = [];
        tpl.push("<tpl if='label.length &gt; 30'>{[values.label.substr(0, 26) + '...']}</tpl>");
        tpl.push("<tpl if='label.length &lt; 31'>{label}</tpl>");
        return tpl;
    }*/
});