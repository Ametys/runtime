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
 * This class is the main class to register and retrieve widgets for edition.
 * It associates types to widgets.
 * See constants for the lists of tabs.
 */
Ext.define('Ametys.form.WidgetManager', {
	singleton: true,
	
	/**
	 * @property {String} TYPE_STRING The type 'string'. A widget of such type should get/set values using the js type "string"
	 * @readonly 
	 */
	TYPE_STRING: 'string',
    /**
     * @property {String} TYPE_PASSWORD The type 'password'. A widget of such type should get/set values using the js type "string". Note that the current real value should not be set and that getValue should return null to specify an unchanged password (while "" means a new empty password).
     * @readonly 
     */
    TYPE_PASSWORD: 'password',
	/**
	 * @property {String} TYPE_GEOCODE The type 'geocode'. A widget of such type should get/set values using the js type "object" with the following properties:
     * 
     * * {Number} latitude The latitude value of the geocode
     * * {Number} longitude The longitude value of the geocode
     * 
	 * @readonly 
	 */
	TYPE_GEOCODE: 'geocode',
	/**
	 * @property {String} TYPE_DATE The type 'date'. A widget of such type should get/set values using the js type "date"
	 * @readonly 
	 */
	TYPE_DATE: 'date',
	/**
	 * @property {String} TYPE_DATETIME The type 'datetime'. A widget of such type should get/set values using the js type "date"
	 * @readonly 
	 */
	TYPE_DATETIME: 'datetime',
	/**
	 * @property {String} TYPE_LONG The type 'long'. A widget of such type should get/set values using the js type "number"
	 * @readonly 
	 */
	TYPE_LONG: 'long',	
	/**
	 * @property {String} TYPE_DOUBLE The type 'double'. A widget of such type should get/set values using the js type "number"
	 * @readonly 
	 */
	TYPE_DOUBLE: 'double',	
	/**
	 * @property {String} TYPE_BOOLEAN The type 'boolean'. A widget of such type should get/set values using the js type "boolean"
	 * @readonly 
	 */
	TYPE_BOOLEAN: 'boolean',
	/**
	 * @property {String} TYPE_RICH_TEXT The type 'rich_text'. A widget of such type should get/set values using the js type "string" representing HTML code. See default implementation for details on how to store local files (images...) and awaited format for all tags.
	 * @readonly 
	 */
	TYPE_RICH_TEXT: 'rich_text',
	/**
	 * @property {String} TYPE_FILE The type 'file'. A widget of such type should get/set values using the js type "object" with the following properties (if no file is selected the value should be {}):
     * 
     * * {String} type The kind of file on the server side. Such as "metadata" for a local file.
     * * {String} [path] The path of the file (related to its type). Such as "attachments/1/attachment".
     * * {String} filename The file name. Such as "image.png".
     * * {String} [mimeType] The file metype. Such as "image/png".
     * * {String} size The size in byte of the file
     * * {String} [lastModified] The date of the last modification using the full ISO8601 format Ext.date#patterns.ISO8601DateTime
     * * {String} id The internal identifier of the new file or "untouched" for an existing file. For example, a just uploaded file will have the temporary value assigned by the server in the upload directory ; whereas for a jcr resource it will be the jcr UUID.
     * * {String} downloadUrl The absolute url to download the file
     * * {String} viewUrl The absolute url to preview the file
     *  
	 * @readonly 
	 */
	TYPE_FILE: 'file',
	/**
	 * @property {String} TYPE_USER The type 'user'. A widget of such type should get/set values using the js type "object" representing the login and population of the user: { 'login': 'X', 'population': 'Y' }
	 * @readonly 
	 */
	TYPE_USER: 'user',
    /**
     * @property {String} TYPE_REFERENCE The type 'reference'. A widget of such type should get/set values using the js type "object" with the following properties:
     * 
     * * {String} value The value of the reference. Can be an url or an identifier depending on the type.
     * * {String} type The type of reference.
     * 
     * @readonly 
     */
    TYPE_REFERENCE: 'reference',
    /**
     * @property {String} TYPE_URL The type 'url'. A widget of such type should get/set values using the js type "string" representing the url.
     * @readonly 
     */
    TYPE_URL: 'url',
	
    /**
     * @property {String} TYPE_CONTENT The type 'content'. A widget of such type should get/set values using the js type "string" representing the content identifier
     * @readonly 
     */
    TYPE_CONTENT: 'content',
    /**
     * @property {String} TYPE_SUB_CONTENT The type 'sub_content'. A widget of such type should get/set values using the js type "string" representing the content identifier
     * @readonly 
     */
    TYPE_SUB_CONTENT: 'sub_content',
    
    
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
			widgetsByType[type] = type == '*' ? [] : Ext.clone(widgetsByType['*'] || []);
		}

		this.getLogger().debug("Adding widget '" + xtype + (isEnumeration ? "' for enumeration" : "'") + " for type '" + type + "'");
		
		widgetsByType[type].push(xtype);
		if (type == '*')
		{
			// loop on all type to add xtype
			Ext.Object.each(widgetsByType, function(key, value) {
				if (key != '*')
				{
					widgetsByType[key].push(xtype);
				}
			})
		}
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

