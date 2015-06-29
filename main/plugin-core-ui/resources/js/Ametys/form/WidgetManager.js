/*
 *  Copyright 2012 Anyware Services
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
 * This class is the main class to register and retrieve widgets for edition
 * 
 * 		Ametys.form.WidgetManager.registerWidget(MyWidgetXType, MyWidgetXType, IsEnumeration, IsMultiple)
 * 		Ametys.form.WidgetManager.getWidget(MyWidgetXType, MyWidgetXType, MyWidgetConfig)
 */
Ext.define('Ametys.form.WidgetManager', {
	singleton: true,
	
	/**
	 * @property {String} TYPE_STRING The type 'string'
	 * @private
	 * @readonly 
	 */
	TYPE_STRING: 'string',
	/**
	 * @property {String} TYPE_GEOCODE The type 'geocode'
	 * @private
	 * @readonly 
	 */
	TYPE_GEOCODE: 'geocode',
	/**
	 * @property {String} TYPE_DATE The type 'date'
	 * @private
	 * @readonly 
	 */
	TYPE_DATE: 'date',
	/**
	 * @property {String} TYPE_DATETIME The type 'datetime'
	 * @private
	 * @readonly 
	 */
	TYPE_DATETIME: 'datetime',
	/**
	 * @property {String} TYPE_LONG The type 'long'
	 * @private
	 * @readonly 
	 */
	TYPE_LONG: 'long',	
	/**
	 * @property {String} TYPE_DOUBLE The type 'double'
	 * @private
	 * @readonly 
	 */
	TYPE_DOUBLE: 'double',	
	/**
	 * @property {String} TYPE_BOOLEAN The type 'boolean'
	 * @private
	 * @readonly 
	 */
	TYPE_BOOLEAN: 'boolean',
	/**
	 * @property {String} TYPE_RICH_TEXT The type 'rich_text'
	 * @private
	 * @readonly 
	 */
	TYPE_RICH_TEXT: 'rich_text',
	/**
	 * @property {String} TYPE_BINARY The type 'binary'
	 * @private
	 * @readonly 
	 */
	TYPE_BINARY: 'binary',
	/**
	 * @property {String} TYPE_FILE The type 'file'
	 * @private
	 * @readonly 
	 */
	TYPE_FILE: 'file',
	/**
	 * @property {String} TYPE_CONTENT The type 'content'
	 * @private
	 * @readonly 
	 */
	TYPE_CONTENT: 'content',
	/**
	 * @property {String} TYPE_SUB_CONTENT The type 'sub_content'
	 * @private
	 * @readonly 
	 */
	TYPE_SUB_CONTENT: 'sub_content',
	/**
	 * @property {String} TYPE_USER The type 'user'
	 * @private
	 * @readonly 
	 */
	TYPE_USER: 'user',
	
	/**
	 * @property {Object} _widgets The registered widgets classified by single/multiple property and type
	 * @private
	 */
	_widgets: {
		'single': {},
		'multiple': {}
	},

	/**
	 * @property {Object} _widgetsForEnumeration The registered widgets for enumeration classified by single/multiple property and type
	 * @private
	 */
	_widgetsForEnumeration: {
		'single': {},
		'multiple': {}
	},
	
	/**
	 * @property {Object} _defaultWidgets The default widgets classified by single/multiple property and type
	 * @readonly
	 * @private
	 */
	_defaultWidgets : {
		'single' : {},
		'multiple' : {}
	},
	
	/**
	 * @property {Object} _defaultWidgetsForEnumeration The default widgets for enumeration classified by single/multiple property and type
	 * @readonly
	 * @private
	 */
	_defaultWidgetsForEnumeration : {
		'single' : {},
		'multiple' : {}
	},
	
	/**
	 * Register a widget on the manager.
	 * @param {String} xtype (required) The widget xtype. Cannot be null.
	 * @param {String} type (required) The type of widget such as Ametys.form.WidgetManager.TYPE_STRING, Ametys.form.WidgetManager.TYPE_DATE, ... Can not be null.
	 * @param {Boolean} isEnumeration True if the widget is registered for enumeration
	 * @param {Boolean} handleMultiple True if the widget handles multiple values
	 */
	register: function(xtype, type, isEnumeration, handleMultiple) 
	{
		var widgets = isEnumeration ? this._widgetsForEnumeration : this._widgets;
		var widgetsByType = handleMultiple ? widgets['multiple'] : widgets['single'];
		
		if (!widgetsByType[type])
		{
			widgetsByType[type] = [];
		}

		this.getLogger().debug("Adding widget '" + xtype + (isEnumeration ? "' for enumeration" : "'") + " for type '" + type + "'");
		
		widgetsByType[type].push (xtype);
	},
	
	/**
	 * Creates and returns the registered widget by its xtype and its type
	 * @param {String} xtype The widget xtype
	 * @param {String} type (required) The type of widget such as Ametys.form.WidgetManager.TYPE_STRING, Ametys.form.WidgetManager.TYPE_DATE, ...
	 * @param {Object} config The object configuration to be passed to the widget constructor
	 * @returns {Ext.form.Field} The created field
	 */
	getWidget: function (xtype, type, config)
	{
		var xtype = this.getWidgetXType (xtype, type, config.enumeration != null, config.multiple || false);
		return Ext.ComponentManager.create(Ext.apply (config, {xtype: xtype}));
	},
	
	/**
	 * Get the default widget xtype by type, enumerated and multiple properties
	 * @return {String} The default xtype
	 * @private
	 */
	_getDefaultWidgetXType: function (type, isEnumeration, isMultiple)
	{
		var defaultWidgets = isEnumeration ? this._defaultWidgetsForEnumeration : this._defaultWidgets;
		var defaultWidgetsByType = isMultiple ? defaultWidgets['multiple'] : defaultWidgets['single'];
		
		if (!defaultWidgetsByType[type])
		{
			var widgets = isEnumeration ? this._widgetsForEnumeration : this._widgets;
			var widgetsByType = isMultiple ? widgets['multiple'] : widgets['single'];
			
			if (!widgetsByType[type] || widgetsByType[type].length == 0)
			{
				this.getLogger().warn("There is no default widget nor registered widget for type '" + type + "' (" + (isEnumeration ? 'enumerated': 'non enumerated') + "/" + (isMultiple ? 'multiple': 'single') + "), default widget for type Ametys.form.WidgetManager#TYPE_STRING will be used");
				return defaultWidgetsByType[this.self.TYPE_STRING];
			}
			else
			{
				this.getLogger().warn("There is no default widget for type '" + type + "' (" + (isEnumeration ? 'enumerated': 'non enumerated') + "/" + (isMultiple ? 'multiple': 'single') + "), first widget of this type will be used.");
				return widgetsByType[type][0];
			}
		}
		else
		{
			return defaultWidgetsByType[type];
		}
	},
	
	/**
	 * Get the registered widget xtype by its asked xtype, type, enumerated and multiple properties
	 * @param {String} xtype The asked xtype. Can be null to use the default widget
	 * @param {String} type (required) The type of widget such as Ametys.form.WidgetManager.TYPE_STRING, Ametys.form.WidgetManager.TYPE_DATE, ...
	 * @param {Boolean} isEnumeration True if the handled field is a enumeration
	 * @param {Boolean} isMultiple True if the handled field has multiple values
	 * @returns {String} The matching widget xtype among registered xtypes
	 */
	getWidgetXType: function (xtype, type, isEnumeration, isMultiple)
	{
		var widgets = isEnumeration ? this._widgetsForEnumeration : this._widgets;
		var widgetsByType = isMultiple ? widgets['multiple'] : widgets['single'];
		
		if (!widgetsByType[type])
		{
			this.getLogger().error("Unknown type '" + type + "' for widgets (" + (isEnumeration ? 'enumerated': 'non enumerated') + "/" + (isMultiple ? 'multiple': 'single') + "), Ametys.form.WidgetManager#TYPE_STRING will be used");
			type = Ametys.form.WidgetManager.TYPE_STRING;
		}
		
		if (xtype == null)
		{
			// Use default widget for this type
			return this._getDefaultWidgetXType (type, isEnumeration, isMultiple);
		}
		else
		{
			if (Ext.Array.contains(widgetsByType[type], xtype))
			{
				return xtype;
			}
			else if (!isMultiple && widgets['multiple'][type] && Ext.Array.contains(widgets['multiple'][type], xtype))
			{
				// Find widget among multiple widgets
				return xtype;
			}
			else
			{
				this.getLogger().error("There is no registered widget of xtype '" + xtype + "' for type '" + type + "'. Default widget will be used.");
				return this._getDefaultWidgetXType (type, isEnumeration, isMultiple);
			}
		}
	}
});

