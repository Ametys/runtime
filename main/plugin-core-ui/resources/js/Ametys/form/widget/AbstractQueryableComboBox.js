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
 * This abstract class is used by widgets providing a combo box for single or multiple selections with querying and type-ahead support.
 * Implement the #getStore method. 
 */
Ext.define('Ametys.form.widget.AbstractQueryableComboBox', {
    
    extend: 'Ametys.form.AbstractFieldsWrapper',
  
    /**
     * @cfg {Boolean} multiple=false True to allow multiple selection.
     */
    multiple: false,
    /**
     * @cfg {Number} [minChars=2] The minimum number of characters the user must type before autocomplete activates.
     */
    minChars: 2,
    /**
     * @cfg {Number} [pageSize=0]  If greater than 0, a Ext.toolbar.Paging is displayed in the footer of the dropdown list and the filter queries will execute with page start and limit parameters.
     */
    pageSize: 0,
    /**
     * @cfg {Number} [maxResult=50]  The maximum number of records to display in the dropdown list.
     */
    maxResult: 50,
    /**
     * @cfg {String} noResultText The text when there is no result found.
     */
	noResultText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_QUERYABLE_COMBOBOX_NO_RESULT'/>",
	/**
     * @cfg {String} loadingText The text while loading results
     */
	loadingText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_QUERYABLE_COMBOBOX_LOADING'/>",
	
	/**
     * @cfg {String} emptyText The default text to place into an empty field.
     */
    
    /**
     * @cfg {Number} [valueField=id] The underlying data value name to bind to the ComboBox.
     */
    valueField: 'id',
    /**
     * @cfg {Number} [displayField=label] The underlying data field name to bind to the ComboBox.
     */
	displayField: 'label',
	
	/**
     * @cfg {String} [stacked=false] If set to `true`, the labeled items will fill to the width of the list instead of being only as wide as the displayed value.
     */
	/**
     * @cfg {String} [growMin=false] If not set to `false`, the min height in pixels of the box select
     */
	/**
     * @cfg {String} [growMax=false] If not set to `false`, the max height in pixels of the box select
     */
	
	/**
	 * @cfg {Boolean} triggerOnClick=true Set to `true` to activate the trigger when clicking in empty space in the field.
	 */
	triggerOnClick: true,
	
	/**
	 * @cfg {Boolean} hideTrigger=false Set to `true` to hide the trigger
	 */
	
    /**
     * @property {Ext.form.field.Tag} combobox The queryable combobox
     * @private
     */
	
    initComponent: function()
    {
        this.items = this.getItems();
        this.callParent();
    },
    
    /**
     * Get the items composing the fields
     * @return {Object[]} The items
     */
    getItems: function ()
    {
    	this.combobox = Ext.create('Ext.form.field.Tag', this.getComboBoxConfig());
    	return [this.combobox];
    },
    
    /**
     * Get select combo box
     * @return {Ext.form.field.Tag} The box select
     * @private
     */
    getComboBoxConfig: function ()
    {
    	var minChars = this.minChars || 3;
        if (Ext.isString(minChars))
        {
            minChars = parseInt(this.minChars);
        }
        
        return {
            queryMode: 'remote',
            minChars: minChars,
            delimiter: ',',
            
            encodeSubmitValue: true,
            editable: true,
            autoSelect: false,
            multiSelect: this.multiple,
            
            pageSize: this.pageSize,
            
            growMin: this.growMin && !isNaN(parseInt(this.growMin)) ? parseInt(this.growMin) : null, 
            growMax: this.growMax && !isNaN(parseInt(this.growMax)) ? parseInt(this.growMax) : null, 
            
            stacked: this.stacked == 'true',
            store: this.getStore(),
            valueField: this.valueField,
            displayField: this.displayField,
            
            labelTpl: this.getLabelTpl(),
            tipTpl: this.getTipTpl(),
            labelHTML: true,
            flex: 1,
            allowBlank: this.allowBlank,
            
            emptyText: this.emptyText,
            
            listConfig: {
            	cls: 'ametys-boundlist',
            	loadMask: true,
            	loadingText: this.loadingText,
            	emptyText: '<span class="x-tagfield-noresult-text">' + this.noResultText + '<span>'
            },
            
            readOnly: this.readOnly || false,
            triggerOnClick: this.triggerOnClick,
            hideTrigger: this.hideTrigger
        };
    },
    
    /**
     * Get the remote store
     * @return {Ext.data.Store} The remote store.
     * @protected
     * @template
     */
    getStore: function ()
    {
    	throw new Error("The method #getStore is not implemented in " + this.self.getName());
    },
    
    /**
     * Get the template for selected field
     * @protected
     * @template
     */
    getLabelTpl: function ()
    {
    	return null;
    },
    
    /**
     * Get the tooltip template for selected field
     * @protected
     * @template
     */
    getTipTpl: function()
    {
        return undefined; // no tip
    },
       
    markInvalid: function (msg)
    {
    	this.callParent(arguments);
    	this.combobox.markInvalid(msg);
    },
    
    /**
     * Specifically focus the box select field when an item is selected.
     * @param {Ext.form.field.Tag} field The combo box field.
     * @param {Ext.data.Model[]} records The selected records.
     * @private
     */
    _onValueSelectionChange: function(field, records)
    {
        // Focus the field when a box item is selected, so that we always get the blur event.
        if (records.length > 0)
        {
            this.focus();
        }
    },
    
    /**
     * Listeners called when the combo box value change.<br>
     * Update the internal value of the field
     */
    _onChange: function ()
    {
    	//this.value = this.combobox.getValue();
    },
    
    /**
     * @inheritdoc
     * Sets a data value into the field and update the comboxbox field
     */
    setValue: function (value)
    {
    	value = Ext.Array.from(value);
    	
    	this.combobox.value = this.combobox.multiSelect ? value : value[0];
    	this.callParent([value]);
    	this.combobox.setValue(value);
    },
    
    getValue: function ()
    {
    	return this.combobox.getValue();
    },
    
    /**
     * @inheritdoc
     */
    getSubmitData: function ()
    {
    	var data = {};    	
    	data[this.name] = this.getValue();
    	return data;
    },
    
    getReadableValue: function (separator)
    {
    	separator = separator || ',';
    	
    	var readableValues= [];
    		
    	var value = this.combobox.getValue();
    	if (value && Ext.isArray(value))
    	{
    		var readableValues= []
    		for (var i=0; i < value.length; i++)
    		{
    			var index = this.combobox.getStore().find(this.valueField, value[i]);
    			var r = this.combobox.getStore().getAt(index);
    			if (r)
    			{
    				readableValues.push(r.get(this.displayField));
    			}
    		}
    	}
    	else if (value)
    	{
    		var index = this.combobox.getStore().find(this.valueField, value);
			var r = this.combobox.getStore().getAt(index);
			if (r)
			{
				readableValues.push(r.get(this.displayField));
			}
    	}
    	
    	return readableValues.join(separator);
    }
});
