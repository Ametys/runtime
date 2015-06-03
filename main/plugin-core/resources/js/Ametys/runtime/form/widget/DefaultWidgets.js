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
 * Provides a widget for text field<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_STRING.<br>
 * It does handle multiple values (see {@link #cfg-multiple}) using separated by commas text. 
 */
Ext.define('Ametys.runtime.form.widget.Text', {
	extend: "Ext.form.field.Text",
	alias: ['widget.edition.textfield', 'widget.text'], //TODO: remove all aliases in this file (alternative to addXTypes() ???)
	
	/**
	 * @cfg {Boolean} multiple=false True to handle multiple values 
	 */

	constructor: function (config)
	{
		config = Ext.apply(config, {
			ametysShowMultipleHint: config.multiple
		});
		
		this.callParent(arguments);
	},
	
	getValue: function ()
	{
		var me = this,
		val = me.callParent(arguments);
		
		if (val && this.multiple)
        {
            var values = val.split(',');
            for (var i = 0; i < values.length; i++)
            {
                values[i] = values[i].trim();
            }
            
            return values;
        }
		
		return val;
	},
	
	/**
	 * @inheritdoc
	 * Overridden to send a JSON-encoded string array to the server in case of a multiple value.
	 */
	getSubmitData: function()
	{
	    var me = this,
        val = me.callParent(arguments);
        
        if (val && val[me.name] && this.multiple)
        {
            var values = val[me.name].split(',');
            for (var i = 0; i < values.length; i++)
            {
                values[i] = values[i].trim();
            }
            
            val[me.name] = values;
        }
        
        return val;
	}
});

/**
 * Provides a widget for string enumeration, ordered by alphabetical order.<br>
 * This widget is the default widget registered for enumerated field of type Ametys.runtime.form.WidgetManager#TYPE_STRING.<br>
 * It does NOT handle multiple values.
 */
Ext.define('Ametys.runtime.form.widget.ComboBox', {
	extend: "Ext.form.field.ComboBox",
	alias: ['widget.edition.combobox'],
	
	/**
	 * @cfg {Boolean} naturalOrder=false True to sort drop down list by natural order. By default alphabetical order is applied to the store.
	 */
	
	constructor: function (config)
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
			typeAhead: true,
			editable: true,
			forceSelection: true,
			triggerAction: 'all',
			
			queryMode: 'local',
			store: new Ext.data.SimpleStore(storeCfg),
	        valueField: 'value',
	        displayField: 'text',
	        
	        multiSelect: config.multiple,
	        
	        listConfig: {
	    		cls: 'ametys-boundlist'
	    	}
		});
		
		this.callParent(arguments);
	}
});

/**
 * Provides a widget for long input field.<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_LONG.<br> 
 * It does NOT handle multiple values.
 */
Ext.define('Ametys.runtime.form.widget.Long', {
	extend: "Ext.form.field.Number",
	alias: ['widget.edition.long'],
	
	constructor: function (config)
	{
		config = Ext.apply(config, {
			allowDecimals: false
		});
		
		this.callParent(arguments);
	}
});

/**
 * Provides a widget for decimal input field.<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_DOUBLE.<br>
 * It does NOT handle multiple values.
 */
Ext.define('Ametys.runtime.form.widget.Double', {
	extend: "Ext.form.field.Number",
	alias: ['widget.edition.double'],

	constructor: function (config)
	{
		config = Ext.apply(config, {
			allowDecimals: true
		});
		
		this.callParent(arguments);
	}
});

/**
 * Provides a widget for checkbox input field.<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_BOOLEAN.<br> 
 * It does NOT handle multiple values.
 */
Ext.define('Ametys.runtime.form.widget.Checkbox', {
	extend: "Ext.form.field.Checkbox",
	alias: ['widget.edition.checkbox'],
	
	constructor: function (config)
	{
		config = Ext.apply(config, {
			checked: Ext.isBoolean(config.value) ? config.value : config.value == "true",
			hideEmptyLabel: Ext.isBoolean(config.hideEmptyLabel) ? config.hideEmptyLabel : config.hideEmptyLabel !== "false",	
			hideLabel: Ext.isBoolean(config.hideLabel) ? config.hideLabel : config.hideLabel == "true",		
			inputValue: 'true', 
			uncheckedValue: 'false'
		});
		
		this.callParent(arguments);
	}
});

/**
 * Provides a widget for date input field.<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_DATE.<br> 
 * It does NOT handle multiple values.<br>
 * 
 * The format used for date is ISO 8601
 */
Ext.define('Ametys.runtime.form.widget.Date', {
	extend: "Ext.form.field.Date",
	alias: ['widget.edition.date'],
	
	statics:
	{
		/**
		 * @readonly
		 * @property {String} CURRENT This constant can be use to #setValue to the current day.
		 * @private
		 */
		CURRENT: 'current'
	},
	
	/**
	 * @cfg {Object} value
	 * @inheritdoc
	 * You can use the string "current" to initialize the field with the current date.
	 */
		
	constructor: function (config)
	{
		config = Ext.apply(config, {
			value: config.value == Ametys.runtime.form.widget.Date.CURRENT ? new Date() : config.value,
	        format: Ext.Date.patterns.LongDate,
			altFormats: 'c',
			submitFormat: Ext.Date.patterns.ISO8601DateTime
		});

		this.callParent(arguments);
	}
});

/**
 * Provides a widget for datetime input field.<br>
 * This widget is available for fields of type Ametys.runtime.form.WidgetManager#TYPE_DATE.<br> 
 * It does NOT handle multiple values.<br>
 * 
 * The format used for date is ISO 8601
 */
Ext.define('Ametys.runtime.form.widget.DateTime', {
	extend: "Ametys.form.field.DateTime",
	alias: ['widget.edition.datetime'],
	
	statics:
	{
		/**
		 * @readonly
		 * @property {String} CURRENT This constant can be use to #setValue to the current day.
		 * @private
		 */
		CURRENT: 'current'
	},
	
	/**
	 * @cfg {Object} value
	 * @inheritdoc
	 * You can use the string "current" to initialize the field with the current date.
	 */
		
	constructor: function (config)
	{
		config.dateConfig = Ext.apply(config.dateConfig || {}, {
			value: config.value == Ametys.runtime.form.widget.Date.CURRENT ? new Date() : config.value,
	        format: Ext.Date.patterns.LongDate,
			altFormats: 'c',
			submitFormat: Ext.Date.patterns.ISO8601Date
		});
		
		config.timeConfig = Ext.apply(config.timeConfig || {}, {
			value: config.value == Ametys.runtime.form.widget.Date.CURRENT ? new Date() : config.value,
	        format: "H:i"
		});
		
		this.callParent(arguments);
	}
});

/**
 * Provides a widget for rich text field.<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_RICH_TEXT.<br>
 * It does NOT handle multiple values.<br>
 * It can be "remotely" configured by using {@link Ametys.runtime.form.widget.RichText.RichTextConfiguration}
 */
Ext.define('Ametys.cms.form.widget.RichText', {
	extend: "Ext.form.field.RichText",
	alias: ['widget.edition.richtext'],
	
	/**
	 * @cfg {Object} widgetParams The widget configuration
	 * @cfg {Number} widgetParams.height=#FIELD_HEIGHT The height of HTML area
	 */
	
	statics:
	{
		/**
		 * @property {Number} FIELD_HEIGHT The default height for textarea field
		 * @private
		 * @readonly 
		 */
		FIELD_HEIGHT: 400
	},
	
	constructor: function (config)
	{
		config = Ext.apply(config, {
			height: config.height ? Number(config.height) : Ametys.cms.form.widget.RichText.FIELD_HEIGHT,
			resizable: true,
			charCounter: true,
			checkTitleHierarchy: true,
			
			settings: {
				// Theme options
				theme_advanced_buttons1 : "",
				theme_advanced_buttons2 : "",
				theme_advanced_buttons3 : "",
			
				content_css: Ametys.runtime.form.widget.RichText.RichTextConfiguration.getCSSFiles(),
				valid_elements: Ametys.runtime.form.widget.RichText.RichTextConfiguration.getTags()
			},
			
			validator: function(value)
			{
				return Ametys.runtime.form.widget.RichText.RichTextConfiguration.validates(value);
			}
		});
		
		this.callParent(arguments);
		
		this.addListener('editorsetcontent', function(field, editor, object) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('setcontent', field, editor, object); });
		this.addListener('editorgetcontent', function(field, editor, object) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('getcontent', field, editor, object); });
		this.addListener('editorkeypress', function(field, editor, e) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('keypress', field, editor, e); });
		this.addListener('editorkeydown', function(field, editor, e) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('keydown', field, editor, e); });
		this.addListener('editorkeyup', function(field, editor, e) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('keyup', field, editor, e); });
		this.addListener('editorvisualaid', function(field, editor, object) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('visualaid', field, editor, object); });
		this.addListener('editorpreprocess', function(field, editor, object) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('preprocess', field, editor, object); });
		this.addListener('editorhtmlnodeselected', function(field, editor, node) { Ametys.runtime.form.widget.RichText.RichTextConfiguration.fireEvent('htmlnodeselected', field, editor, node); });
	}
});

/**
 * Provides a widget for textarea field (multiline text).<br>
 * This widget is registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_STRING.<br>
 * It does NOT handle multiple values. 
 */
Ext.define('Ametys.runtime.form.widget.TextArea', {
	extend: "Ext.form.field.TextArea",
	alias: ['widget.edition.textarea'],
	
	statics:
	{
		/**
		 * @property {Number} FIELD_HEIGHT The default height for textarea field
		 * @private
		 * @readonly 
		 */
		FIELD_HEIGHT: 80
	},
		
	constructor: function (config)
	{
		config = Ext.apply(config, {
			height: config.widgetParams && config.widgetParams.height ? config.widgetParams.height : Ametys.runtime.form.widget.TextArea.FIELD_HEIGHT
		});
		
		this.callParent(arguments);
	}
	
});

/**
 * Provides a widget for reference of type external<br>
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_REFERENCE.<br>
 * It does NOT handle multiple values.
 */
Ext.define('Ametys.runtime.form.widget.UrlReference', {
	extend: 'Ametys.runtime.form.widget.Text',
	alias: ['widget.edition.urlreference'],
	
	/**
	 * @property {String} referenceType=__external The type of the reference.
	 * @readonly 
	 */
	referenceType: '__external',
	
	/**
	 * @inheritdoc
	 */
	setValue: function(value) 
	{
		if (!value)
		{
			this.callParent(arguments);
		}
		else
		{
			if (value.type == this.referenceType)
			{
				this.callParent([value.value]);
			}
		}
	},
	
	/**
	 * @inheritdoc
	 */
	getValue: function()
	{
		var value = this.callParent(arguments) || '';
		
		if (value)
		{
			return {
				type: this.referenceType,
				value: value
			}
		}
		
		return value;
	},
	
	/**
	 * @inheritdoc
	 */
	getSubmitData: function()
	{
		var data = this.callParent(arguments),
			dataName;
		
		if (data && data[this.name])
		{
			dataName = data[this.name];
			
			data[this.name] = Ext.encode({
				type: this.referenceType,
				value: dataName
			});
		}
		
		return data;
	}
});
