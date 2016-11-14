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
 * Field for picking a color value from a list of available colors, or choosing a custom color value from a {Ext.ux.colorpick.Selector}.
 */
Ext.define('Ametys.form.field.ColorSelector', {
	extend: 'Ametys.form.AbstractFieldsWrapper',
	
	layout: { 
        type: 'vbox',
        align: 'stretch'
    },
	
	/**
	 * @cfg {Object/Object[]} listColors An array of color objects, with properties :
	 * {String} listColors.label The name of the picker
	 * {String[]} [listColors.colors] The list of colors code, such as "FF0000".
	 * If listColors is undefined, it will assume the default color configuration.
	 */
	
	/**
	 * @cfg {String} [currentColor] The initial selected color
	 */
    
	/**
	 * @cfg {Boolean} [allowTransparent=false] True to allow selection of a fully transparent color
	 */
    
	/**
	 * @cfg {Boolean} [allowOtherColors=true] True to allow selection of custom color
	 */
    
	/**
	 * @cfg {Boolean} [allowNoColor=false] True to allow selection of no color (a button will be drawn)
	 */
	
	/**
     * @inheritdoc
	 * @cfg {String} value Format is a 6 hex characters color value, or 'transparent'.
	 */
	
	/**
	 * @cfg {Function} callback The function called when a color is selected. Arguments is :
	 * {String} callback.color The 6 hex characters color value, or 'transparent'.
	 */
    
    width: 155,
	
    constructor: function(config)
    {
        config.allowTransparent = config.allowTransparent == "true";
        config.allowOtherColors = config.allowOtherColors != "false";
        config.allowNoColor = config.allowNoColor == "true";
        this.callParent(arguments);
    },
    
	initComponent: function() 
	{
		this.items = [];
		
		this.listColors = Ext.Array.from(this.listColors);
		
		if (this.listColors.length > 0)
		{
			Ext.Array.each(this.listColors, this._initColorPicker, this);
		}
		else
		{
			// Default colors
			this._initColorPicker({
				label: "{{i18n PLUGINS_CORE_UI_COLORSELECTOR_DEFAULT_COLORS}}"
			});
		}
		
		if (this.allowTransparent)
		{
			// Button for choosing transparent color
			this.items.push({
				xtype: 'button',
				padding: 5,
				itemId: 'transparent-btn',
				icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/field/transparent_22.png',
				text: "{{i18n PLUGINS_CORE_UI_COLORSELECTOR_TRANSPARENT}}",
				handler: this._setTransparent,
				scope: this
			});
		}
		
		// Custom colors
		this.items.push({
			xtype: "panel",
			bodyPadding: 5,
			itemId: "custom-color-panel",
			title: "{{i18n PLUGINS_CORE_UI_COLORSELECTOR_CUSTOM_COLORS}}",
			hidden: true,
			items: []
		});
		
		// Other colors
        if (this.allowOtherColors)
        {
			this.items.push({
				xtype: 'button',
				padding: 5,
				itemId: 'other-colors-btn',
				text: "{{i18n PLUGINS_CORE_UI_COLORSELECTOR_OTHERS_COLORS}}",
				handler: this._openOtherColors,
				scope: this
			});
        }
        
		// No color
        if (this.allowNoColor)
        {
			this.items.push({
				xtype: 'button',
				padding: 5,
				itemId: 'no-color-btn',
				text: "{{i18n PLUGINS_CORE_UI_COLORSELECTOR_NO_COLOR}}",
				handler: this._setNoColor,
				scope: this
			});
        }
		
		this.callParent(arguments);
	},
	
	onRender: function ()
	{
		var currentColor = this.getInitialConfig("currentColor");
		if (currentColor)
		{
			this.setColor(currentColor);
		}
		
		this.callParent(arguments);
	},
	
	/**
	 * Initialize a new color picker and it header
	 * @param {Object} colorInfos The color picker infos
	 * @param {String} [colorInfos.label] The header text. If ommited, the header is not created.
	 * @private
	 */
	_initColorPicker: function (colorInfos) 
	{
		this.items.push({
			xtype: "panel",
			bodyPadding: 5,
			title: colorInfos.label,
			items: [
				Ext.applyIf({
					xtype: 'colorpicker',
					handler: this._onColorPickerSelect,
					scope: this,
					height: "auto",
                    width: "auto"
				}, colorInfos)
			]
		});
	},
	
	/**
	 * Handler for the transparent button. Set the color "transparent"
	 * @private
	 */
	_setTransparent: function ()
	{
		this.setColor("transparent");
		
		var cbFn = this.getInitialConfig("callback");
		if (Ext.isFunction(cbFn))
		{
			cbFn("transparent");
		}
	},
	
	/**
	 * Handler for the "other colors" button. Open the helper to pick other colors
	 * @private
	 */
	_openOtherColors: function ()
	{
		var color = this._currentColor == "transparent" ? "000000" : this._currentColor;
		Ametys.helper.ChooseColor.open(this._currentColor, Ext.bind(this._onColorSelect, this));
	},
    
    /**
     * Handler for the "no color" button. Set an empty value
     * @private
     */
    _setNoColor: function ()
    {
        this.setColor("");
        
        var cbFn = this.getInitialConfig("callback");
        if (Ext.isFunction(cbFn))
        {
            cbFn("");
        }
    },
	
	/**
	 * Set the current value of the field.
	 * @param {String} color The color
	 */
	setColor: function (color)
	{
		var colorPickers = this.query("colorpicker");
		
		var valueExists = false;
		// update the color pickers
		Ext.Array.each(colorPickers, function (picker) {
			if (Ext.Array.contains(picker.colors, color))
			{
				// select the color
				if (picker.itemId != "custom-colors")
				{
					picker.select(color);
					valueExists = true;
				}
				else
				{
					picker.colors = Ext.Array.remove(picker.colors, color);
					picker.clear();
				}
			}
			else
			{
				picker.clear();
			}
		});
		
		if (this.allowTransparent)
	    {
    		// update the transparency button
    		var transparentBtn = this.down("#transparent-btn");
    		transparentBtn.toggle(color == "transparent", true);
    		if (color == "transparent")
    		{
    			valueExists = true;
    			transparentBtn.setIcon(Ametys.getPluginResourcesPrefix('core-ui') + '/img/field/transparent_selected_22.png');
    		}
    		else
    		{
    			transparentBtn.setIcon(Ametys.getPluginResourcesPrefix('core-ui') + '/img/field/transparent_22.png');
    		}
	    }
        
        if (this.allowNoColor && color == "")
        {
            valueExists = true;
            this._currentColor = color;
        }
		
		if (!valueExists)
		{
			// update the custom colors
			var pickerPanel = this.getComponent("custom-color-panel");
			pickerPanel.setHidden(false);
			
			var picker = pickerPanel.getComponent("custom-colors");
			var colors;
			if (picker)
			{
				colors = Ext.Array.insert(picker.colors, 0, [color]);
				// limit to 8 elements (1 row)
				if (colors.length > 8)
				{
					colors = Ext.Array.slice(colors, 0, 8);
				}
			
				pickerPanel.remove(picker);
			}
			else
			{
				colors = [color];
			}
			
			picker = Ext.create('Ext.picker.Color', {
				colors: colors,
				height: "auto",
				itemId: "custom-colors",
				value: color,
				handler: this._onColorPickerSelect,
				scope: this
			});
			
			pickerPanel.add(picker);
		}
		
		this._currentColor = color;
	},
	
	/**
	 * Handler for the color pickers. 
	 * @param {Ext.picker.Color} object The color picker that initiated the action.
	 * @param {String} color The color value
	 * @private
	 */
	_onColorPickerSelect: function (object, color)
	{
		this._onColorSelect(color);
	},
	
	/**
	 * Handler for the color selection
	 * @param {String} color The color value
	 * @private
	 */
	_onColorSelect: function (color)
	{
		this.setColor(color);
		
		var cbFn = this.getInitialConfig("callback");
		if (Ext.isFunction(cbFn))
		{
			cbFn(color);
		}
	}
	
});