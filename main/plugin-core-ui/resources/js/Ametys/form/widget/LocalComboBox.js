/*
 *  Copyright 2013 Anyware Services
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
 * This class provides a combo box with local store for single or multiple selections with type-ahead support.<br>
 * The items of the list are not queryable.
 * 
 * This widget is the default widget registered for enumerated and multiple fields of type Ametys.form.WidgetManager#TYPE_STRING.<br>
 */
Ext.define('Ametys.form.widget.LocalComboBox', {
    extend: 'Ext.form.field.Tag',
    
    /**
	 * @cfg {Boolean} naturalOrder=false True to sort drop down list by natural order. By default alphabetical order is applied to the store.
	 */
    
    constructor: function(config)
    {
    	var storeCfg = {
			id: 0,
            fields: [ 'value', {name: 'text', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
            data: config.enumeration
        };
    		
		config.naturalOrder = Ext.isBoolean(config.naturalOrder) ? config.naturalOrder : config.naturalOrder == 'true';
		if (!config.naturalOrder)
		{
			storeCfg.sorters = [{property: 'text', direction:'ASC'}]; // default order
		}
    		
        config = Ext.apply(config, {
        	cls: 'ametys',
        	
            mode: 'local',
            queryMode: 'local',
            
            encodeSubmitValue: false,
            typeAhead: true,
            triggerAction: 'all',
            enableKeyEvents: true,
            
            store: new Ext.data.ArrayStore(storeCfg),
            
            valueField: 'value',
            displayField: 'text',
            
            allowBlank: this.allowBlank,
            multiSelect: config.multiple,
            
            listConfig: {
            	loadingText: this.loadingText,
            	emptyText: '<span class="x-tagfield-noresult-text">' + this.noResultText + '<span>'
            }
        });
        
        this.callParent(arguments);
    }
    
});
