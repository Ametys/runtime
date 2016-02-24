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
 * Abstract field displaying a combobox allowing to select one of the available data sources. Optionally, 
 * this field can allow the addition of other data sources. 
 * You can override #getStore and you must provide a function #creationHandler
 */
Ext.define('Ametys.form.widget.AbstractDataSource', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
	
    /**
     * @cfg {Boolean} [allowInternal=false] True to also show internal data sources
     */
    allowInternal: false,
    
    /**
     * @cfg {Boolean} [allowPrivate=false] True to also show private data sources
     */
	allowPrivate: false,
	
    /**
     * @cfg {Boolean} [allowCreation=false] True to allow the creation of new data sources
     */
	allowCreation: false,
	
	/**
	 * @cfg {String} dataSourceType the type of the data source
	 */
	/**
	 * @cfg {Function} creationFunction the function used to add a new data source
	 */
	
    /**
     * @cfg {String} createButtonText='' The text of the create button.
     */
    createButtonText: '',
    /**
     * @cfg {String} createButtonIcon The button icon path for the create button.
     */
    createButtonIcon: null,
    
    /**
     * @cfg {String} createButtonIconCls The button icon CSS for the create button.
     */
    createButtonIconCls: 'flaticon-data110 decorator-flaticon-add64',
    
    /**
     * @cfg {String} createButtonTooltip The button icon tooltip for the create button.
     */
    createButtonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_DATASOURCE_BUTTON_DEFAULT_TOOLTIP'/>",
    
	/**
	 * @private
	 * @property {Ext.data.Store} _store the combo box's store
	 */
	/**
	 * @private
	 * @property {Ext.data.Record} _selectedRecord the record that is currently selected, null if none.
	 */
	/**
	 * @private
	 * @property {Ext.data.Store} _store The combobox store
	 */
	/**
	 * @private
	 * @property {String} _dataSourceToSelect The id of the data source to select 
	 */
	
	initComponent: function()
	{
		this.items = [];
		
    	this.items[0] = Ext.create('Ext.form.field.ComboBox', 
    			        {
	    		  	        flex: 1,
	    		  	        
  	    	    		    store: this.getStore(),
  	    	    		    
  	    	    		    // FIXME set value => config tool dirty
	  	    	            autoLoadOnValue: true,
	  	    	            forceSelection: true,
	  	    	            
	  	    	            valueField: 'id',
  	    	    		    displayField: 'name',
  	    	    		    
  	    	    		    listeners: {
  	    	    		    	select: Ext.bind(this._onSelect, this),
  	    	    		    	click: {fn: Ext.bind(this._onClick, this), element: 'inputEl'},
  	    	    		    	scope: this
  	    	    		    }
      			        });
    	
    	if (this.allowCreation)
		{
    		this.items[1] = Ext.create('Ext.button.Button', 
    						{ 
				    			width: 20,
                                text: this.createButtonText,
			                    icon: this.createButtonIcon,
				    			iconCls: this.createButtonIconCls,
			                    tooltip: this.createButtonTooltip,
				    			handler: Ext.bind(this._createButtonHandler, this)
				    		}); 
		}
    	
    	this.callParent();
	},
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		config.allowPrivate = Ext.isBoolean(config.allowPrivate) ? config.allowPrivate : config.allowPrivate == 'true';
		config.allowCreation = Ext.isBoolean(config.allowCreation) ? config.allowCreation : config.allowCreation == 'true';
        config.allowInternal = Ext.isBoolean(config.allowInternal) ? config.allowInternal : config.allowInternal == 'true';
		
		this._selectedRecord = null;
		
		// Bus messages listeners
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onCreated, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onDeleted, this);
	},
	
	/**
	 * @private
	 * Get the underlying combo box
	 * @return {Ext.form.field.ComboBox} The combobox
	 */
	_getCombobox: function()
	{
		return this.items.get(0);
	},
	
    setValue: function(value) 
    {
    	this._getCombobox().setValue(value ? value : null);
    },

    getValue: function() 
    {
    	return this._getCombobox().getValue();
    },
    
	/**
	 * Get the store used by the combo box
	 * @return {Ext.data.Store} the store
	 */
	getStore: function()
	{
		if (this._store == null)
		{
			this._store = Ext.create('Ext.data.Store',
					{
				fields: [ 'id', {name: 'name', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
				
				proxy: {
					type: 'ametys',
					plugin: 'admin',
					url: 'datasources/get',
					reader: {
						type: 'json',
						rootProperty: 'datasources'
					}
				},
				
				sorters: 'name',
				
				listeners: {
					beforeload: {fn: this._onBeforeLoad, scope: this}
				}
			});
		}
		
		return this._store;
	},
    
    /**
     * Function invoked when clicking on create button
     * @private
     */
    _createButtonHandler: function ()
    {
        this.createDataSource(Ext.bind(this._selectDataSource, this));
    },
    
    /**
     * Select the created data source in the combo box
     * @param {String} id the id of the new data source
     */
    _selectDataSource: function (datasource)
    {
        if (datasource && datasource.id)
        {
            var me = this;
	        if (this._store.getById(datasource.id) != null)
	        {
	            this.setValue(datasource.id);
	        }
	        else
	        {
                // Too soon, store the id to select after reload
	            this._dataSourceToSelect = datasource.id;
	        }
        }
    },
	
	/**
     * @protected
     * @template
	 * Handler when the 'Add data source' button is clicked
     * @param {Function} the function to call after creating data source
	 */
	createDataSource: function(callback)
	{
		throw new Error("The method #createDataSource is not implemented in " + this.self.getName());
	},
	
	/**
	 * Function invoked right before the store is loaded
	 * @param {Ext.data.Store} store the store
	 * @param {Ext.data.operation.Operation} The {@link Ext.data.operation.Operation} object that will be passed to the Proxy to load the Store
	 */
	_onBeforeLoad: function(store, operation)
	{
		var me = this;

		operation.setParams(Ext.apply(operation.getParams() || {}, {
			includePrivate: me.allowPrivate,
			includeInternal: me.allowInternal,
			type: me.dataSourceType
		}));
	},
	
	/**
	 * Function invoked whenever a record is selected
	 * @param {Ext.form.field.ComboBox} comboBox the combo box
	 * @param {Ext.data.Record} record the selected record
	 */
	_onSelect: function(comboBox, record)
	{
		this._selectedRecord = record;
		this.setValue(record.get('id'));
	},
	
	/**
	 * @private
	 * Function invoked when the combo box is clicked
	 */
	_onClick: function()
	{
		var combo = this._getCombobox();
		
		// Do not collapse when the combo box is already expanded
		if (!combo.readOnly && !combo.disabled && !combo.isExpanded) 
		{
			combo.onTriggerClick();
		}
	},
	
	/**
	 * Handler function invoked whenever a {@link Ametys.message.Message.CREATED}
	 * message is sent out on the message bus. Add the corresponding record to the grid panel's store.
	 * @param {Ametys.message.Message} message the message
	 */
	_onCreated: function(message)
	{
		var me = this;
		var target = message.getTarget(Ametys.message.MessageTarget.DATASOURCE);
		if (target)
		{
			// load callback in #_selectNewDataSource is not called if the store is reloaded
			this._store.reload({
				callback: function() {
					if (this._dataSourceToSelect != null)
					{
						this.setValue(this._dataSourceToSelect);
						this._dataSourceToSelect = null;
					}
				},
				scope: this
			});
		}
	},
	
	/**
	 * Handler function invoked whenever a {@link Ametys.message.Message.MODIFIED}
	 * message is sent out on the message bus. Add the corresponding record to the grid panel's store.
	 * @param {Ametys.message.Message} message the message
	 */
	_onModified: function(message)
	{
		var target = message.getTarget(Ametys.message.MessageTarget.DATASOURCE);
		if (target)
		{
			var id = target.getParameters().id;
			if (this._selectedRecord && this._selectedRecord.get('id') == id)
			{
				this._store.load({
					callback: function() {
						var record = this._store.getById(id);
						this.setValue(record.get('name'));
					},
					scope: this
				});
			}
			else
			{
				this._store.reload();
			}
		}
	},
	
	/**
	 * Handler function invoked whenever a {@link Ametys.message.Message.CREATED}
	 * message is sent out on the message bus. Add the corresponding record to the grid panel's store.
	 * @param {Ametys.message.Message} message the message
	 */
	_onDeleted: function(message)
	{
		var target = message.getTarget(Ametys.message.MessageTarget.DATASOURCE);
		if (target)
		{
			var id = target.getParameters().id;
			if (this._selectedRecord && this._selectedRecord.get('id') == id)
			{
				this._store.load({
					callback: function() {
						this._getCombobox().setValue(null);
					},
					scope: this
				});
			}
			else
			{
				this._store.reload();
			}
		}
	}
});
