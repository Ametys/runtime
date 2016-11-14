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
 * This class controls a ribbon input field.
 * 
 * - It supports handlers on key events
 * - It supports enabling/disabling upon the current selection (see {@link #cfg-selection-target-id}) and associated rights (see {@link #cfg-rights}).
 * - It supports enabling/disabling upon a focused tool (see {@link #cfg-tool-id})
 * 
 * Note that a property "controlId" is available on the created input. This string references this controller id, that can be retrieve with {@link Ametys.ribbon.RibbonManager#getUI}
 */
Ext.define(
	"Ametys.ribbon.element.ui.FieldController",
	{
		extend: "Ametys.ribbon.element.RibbonUIController",
		
		mixins: ['Ametys.ribbon.element.ui.CommonController'],
		
		statics: {
			/**
			 * @readonly
			 * @property {Number} DEFAULT_LABEL_WIDTH The default width for input label.
			 */
			DEFAULT_LABEL_WIDTH: 80,
			
			/**
			 * @readonly
			 * @property {Number} DEFAULT_TEXTAREA_HEIGHT The default height for textarea label.
			 */
			DEFAULT_TEXTAREA_HEIGHT: 60,
			
			/**
			 * @readonly
			 * @property {Number} DEFAULT_INPUT_WIDTH The default width for input.
			 */
			DEFAULT_INPUT_WIDTH: 190
		},
		
		/**
		 * @property {String} _value The current input value
		 * @private
		 */
		/**
		 * @cfg {String} value='' The default value to set to input field
		 */
		/**
		 * @cfg {String} input-xtype=textfield The xtype for input field
		 */
		/**
		 * @cfg {String} input-type=text The type attribute for input field -- e.g. radio, text, password, file, url, mail, ...
		 */
		/**
		 * @cfg {Boolean} readOnly=false true to prevent the user from changing the field
		 */
		/**
		 * @cfg {Boolean} allowDecimals=false false to allow decimal values
		 */
		/**
		 * @cfg {Number} label-width=80 The width of label field in pixels
		 */
		/**
		 * @cfg {Number} width=190 The width of input field in pixels
		 */
		/**
		 * @cfg {Number} height The height of input field in pixels
		 */
		/**
		 * @cfg {Number} [width-small] The width of input field in pixels in "small" layout size
		 */
		/**
		 * @cfg {Number} [width-medium] The width of input field in pixels in "medium" layout size
		 */
		/**
		 * @cfg {Number} [width-large] The width of input field in pixels in "large" layout size
		 */
		/**
		 * @cfg {String} empty-text The default text to place if the field is empty
		 */
		/**
		 * @cfg {Object[]/Object} data 
		 * Valid only when used with a ComboxBox field. See #cfg-input-xtype.
		 * The data of local store used in conjunction with the #cfg-model or #cfg-value-field and #cfg-value-field.
		 * If the format is not an array of model configuration, you will have to use #cfg-data-convert to convert it to such a format.
		 */
		/**
		 * @cfg {String} data-convert A function name that will be called when initializing #cfg-data.
		 * Parameters are the current controller instance and the #cfg-data object, and return value is the transformed array.
		 */
		/**
		 * @cfg {String} value-field=value
		 * 		Valid only when used with a ComboxBox field. See #cfg-input-xtype.
		 * The underlying data value name to bind to the ComboBox. If #cfg-model is not null, this will be ignored.
		 */
		/**
		 * @cfg {String} display-field=label 
		 * 		Valid only when used with a ComboxBox field. See #cfg-input-xtype.
		 * The underlying data field name to bind to the ComboBox. If #cfg-model is not null, this will be ignored.
		 */
		/**
		 * @cfg {String} [model] 
		 * 		Valid only when used with a ComboxBox field. See #cfg-input-xtype.
		 * Name of the Model associated with the store of the ComboBox. 
		 * If null a simple stores with the two-field store #cfg-value-field and #cfg-value-field will be used.
		 */
		/**
		 * @cfg {String/String[]} [template]
		 * 		Valid only when used with a ComboxBox field. See #cfg-input-xtype.
		 * A string or an array of strings to form an Ext.XTemplate for the dropdown menu of ComboBox.
		 */
		
		/**
		 * @cfg {Boolean} html-encode=false
		 * 		Valid only when used with a Display field. See #cfg-input-xtype. 
		 *  True to escape HTML in text value when rendering it. Defaults to true.
		 */
		constructor: function(config)
		{
			this.callParent(arguments);

            // Has to be done before initialize, so "initialize" function has access to it
            this.createDataStore();
            
			// Initialize input properties
			this._initialize();
		},
		
		createUI: function(size, colspan)
		{
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Creating new UI field for controller " + this.getId() + " in size " + size + " (with colspan " + colspan + ")");
			}

			var labelWidth = this.getInitialConfig('label') == null || this.getInitialConfig('hideLabel') == 'true' ? 0 : (this.getInitialConfig('label-width') ? Number(this.getInitialConfig('label-width')) : Ametys.ribbon.element.ui.FieldController.DEFAULT_LABEL_WIDTH);
			var width = labelWidth + this._getInputWidth (size) ;
			
			// Is the label going to be on top ?
			var labelOnTop = size == 'large' && this.getInitialConfig('label') != null;
			
			var element = Ext.ComponentManager.create(Ext.apply(
			{
				cls: this.getInitialConfig()['cls'],
				readOnly: this.getInitialConfig()['readOnly'] == "true",
				
				colspan: colspan,
				
				inputType: this.getInitialConfig('input-type') || 'text', 
		    	xtype: this.getInitialConfig('input-xtype') || 'textfield', 

		    	labelWidth: labelWidth,
		    	labelAlign: labelOnTop ? 'top' : 'left',
		    	hideLabel: this.getInitialConfig('label') == null || this.getInitialConfig('hideLabel') == 'true',
		    	
		    	fieldLabel: this.getInitialConfig('hideLabel') == 'true' ? null : this.getInitialConfig('label'), // hideLabel above does not seems to work
		    	name: this.getInitialConfig('name'),
		    	value: this.getInitialConfig('value') || '',
		    	
		    	height: this._getInputHeight(size, labelOnTop), 
		    	width: width,
		    	
		    	emptyText: this.getInitialConfig('empty-text'),
		    	controlId: this.getId(),
		    	
		    	enableKeyEvents:  true,
		    	disabled: this.getInitialConfig('disabled') == "true",

		    	listeners: this._getListeners()
			}, 
			this._getTypeConfig(this.getInitialConfig('input-xtype') || 'textfield'))
		);
			
			this._value = this.getInitialConfig('value') || '';
			
			return element;
		},
		
		createMenuItemUI: function ()
		{
			throw new Error("The method #createMenuItemUI is not supported for " + this.self.getName());
		},
		
		createGalleryItemUI: function ()
		{
			throw new Error("The method #createGalleryItemUI is not supported for " + this.self.getName());
		},
		
		/**
		 * Get the specific configuration object according the input xtype
		 * @param {String} xtype The input xtype
		 * @return {Object} The configuration object to add to the main configuration. Can not be null.
		 * @private
		 */
		_getTypeConfig: function (xtype)
		{
			switch (xtype) 
			{
				case 'numberfield':	
					return this.getNumberConfig();
				case 'checkboxfield':	
				case 'checkbox':	
					return this.getCheckboxConfig();
				case 'combobox':
				case 'combo':
					return this.getComboBoxConfig();
				case 'displayfield':	
					return this.getDisplayConfig();
				default:
					return this.getTextConfig();
			}
		},
		
		/**
		 * This function builds the specific configuration for a combobox field to be added to the main configuration of the UI control
		 * Override this function is you need to specific.
		 * @return {Object} The configuration object to add to the main configuration. Can not be null.
		 * @protected
		 * @template
		 */
		getComboBoxConfig: function ()
		{
			var tpl = null;
			if (this.getInitialConfig('template'))
			{
				tpl = Ext.create('Ext.XTemplate',
						'<tpl for=".">',
							this.getInitialConfig('template').replace(/'/g, '"'),
						'</tpl>'
				);
			}
			
			return Ext.apply({
				store: this.getStore(),
		    	queryMode: 'local',

				forceSelection : true,
				triggerAction: 'all',
				
				editable: false,
				valueField: this.getInitialConfig('value-field') || 'value',
				displayField: this.getInitialConfig('display-field') || 'label',
				
				tpl: tpl
			}, this.getInitialConfig('ui-config'));
		},
		
		/**
		 * This function builds the specific configuration for a number field to be added to the main configuration of the UI control
		 * Override this function is you need to specific.
		 * @return {Object} The configuration object to add to the main configuration. Can not be null.
		 * @protected
		 * @template
		 */
		getNumberConfig: function ()
		{
			return Ext.apply({
				allowDecimals: this.getInitialConfig('allowDecimals') != null ? this.getInitialConfig('allowDecimals') == "true" : false
			}, this.getInitialConfig('ui-config'));
		},
		
		/**
		 * This function builds the specific configuration for text fields to be added to the main configuration of the UI control.
		 * Override this function is you need to specific.
		 * @return {Object} The configuration object to add to the main configuration. Can not be null.
		 * @protected
		 * @template
		 */
		getTextConfig: function ()
		{
			return Ext.apply({
				// TODO regexp
			}, this.getInitialConfig('ui-config'));
		},
		
		/**
		 * This function builds the specific configuration for a display field to be added to the main configuration of the UI control
		 * Override this function is you need to specific.
		 * @return {Object} The configuration object to add to the main configuration. Can not be null.
		 * @protected
		 * @template
		 */
		getDisplayConfig: function ()
		{
			return Ext.apply({
				htmlEncode: this.getInitialConfig('html-encode') ? Boolean(this.getInitialConfig('html-encode')) : false
			}, this.getInitialConfig('ui-config'));
		},
		
		/**
		 * This function builds the specific configuration for a checbox field to be added to the main configuration of the UI control
		 * Override this function is you need to specific.
		 * @return {Object} The configuration object to add to the main configuration. Can not be null.
		 * @protected
		 * @template
		 */
		getCheckboxConfig: function ()
		{
			return Ext.apply({
				hideLabel: true,
				boxLabel: this.getInitialConfig('label')
			}, this.getInitialConfig('ui-config'));
		},
		
		/**
		 * This function creates and returns the data store to be used by combobox field.
		 * This implementation fills the data store upon #cfg-data.
		 * Override this function is you need to specific.
		 * @protected
		 * @template
		 */
		createDataStore: function ()
		{
			var initialData = this.getInitialConfig('data') || [];
			var transformFunction = this.getInitialConfig('data-convert');
			var transformedData = (transformFunction && Ametys.executeFunctionByName(transformFunction, null, null, this, initialData)) || initialData;
			
			if (this.getInitialConfig('model'))
			{
				this._store = Ext.create('Ext.data.Store', {
					model: this.getInitialConfig('model'),
					data: transformedData,
					
					listeners: this._getStoreListeners()
				});
			}
			else
			{
				this._store =  Ext.create('Ext.data.Store', {
					fields: [
					         this.getInitialConfig('value-field') || 'value',
					         this.getInitialConfig('display-field') || 'label'
					],
					data: transformedData,
					
					listeners: this._getStoreListeners()
				});
			}
			
			return this._store;
		},
		
		/**
		 * Get the store used in ComboBox
		 * @return {Ext.data.Store} The store
		 */
		getStore: function ()
		{
			return this._store;
		},
		
		_updateUI: function ()
		{
			var me = this;
			this.getUIControls().each(function (elmt) {
				me._setTooltip(elmt);
			});			
		},
		
		/**
		 * Set the tooltip for component. See Ametys.ui.fluent.Tooltip.
		 * @param {Ext.Component} elmt The component
		 * @private
		 */
		_setTooltip: function (elmt)
		{
	        Ext.QuickTips.register(Ext.apply({target: elmt.getId()}, this._getTooltip()));
		},
		
		/**
		 * Sets a raw data value into the all controller fields ui
		 * @param {String} value The value to set
		 */
		setValue: function (value)
		{
			this._value = value;
			this.getUIControls().each(function (input) {
				// Prevent onchange event
				input.suspendEvent('change');
				input.setValue(value);
				input.resumeEvent('change');
			});
		},
		
		/**
		 * Get the input width from initial configuration and control's size
		 * @param {String} size The size required for the control. Can be 'small', 'very-small' or 'large'.
		 * @return {Number/String} the input width
		 * @private
		 */
		_getInputWidth: function (size)
		{
			var width = this.getInitialConfig()['width'] != null ? Number(this.getInitialConfig()['width']) : Ametys.ribbon.element.ui.FieldController.DEFAULT_INPUT_WIDTH;
			width = this.getInitialConfig()['width-' + size] != null ? Number(this.getInitialConfig()['width-' + size]) : width;
			return width;
		},
		
		/**
		 * Get the input height from initial configuration and control's size
		 * @param {String} size The size required for the control. Can be 'small', 'very-small' or 'large'.
		 * @param {Boolean} labelOnTop is the label going to be on top ?
		 * @return {Number/String} the input height
		 * @private
		 */
		_getInputHeight: function (size, labelOnTop)
		{
			var height = this.getInitialConfig('height') ? Number(this.getInitialConfig('height')) : null;
			height = this.getInitialConfig('height-' + size) != null ? Number(this.getInitialConfig('height-' + size)) : height;
			return height;
		},
		
		/**
		 * Get the handlers to be attached to the field.
		 * @return {Object} The listeners configuration object
		 * @private
		 */
		_getListeners: function ()
		{
			var listeners = {};
			
			listeners['afterrender'] = Ext.bind(this._setTooltip, this);
			
			for (var key in this.getInitialConfig())
			{
				if (/^on(.*)$/.test(key))
				{
					var fn = this.getInitialConfig(key);
					listeners[RegExp.$1] = Ametys.getFunctionByName(fn)
				}
			}
			
			return listeners;
		},
		
		/**
		 * Get the handlers to be attached to the store on the field.
		 * @return {Object} The listeners configuration object
		 * @private
		 */
		_getStoreListeners: function ()
		{
			var listeners = {};
			
			for (var key in this.getInitialConfig())
			{
				if (/^store-on(.*)$/.test(key))
				{
					var fn = this.getInitialConfig(key);
					listeners[RegExp.$1] = Ametys.getFunctionByName(fn)
				}
			}
			
			return listeners;
		}
	}
);
