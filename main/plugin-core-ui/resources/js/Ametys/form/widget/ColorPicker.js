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
 * This class provides a widget to select a color.
 */
Ext.define('Ametys.form.widget.ColorPicker', {
    extend:'Ametys.form.AbstractField',
    
    /**
     * @cfg {String} buttonText The button text
     */
    buttonText : "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_COLORPICKER_SHOW_COLORS_BUTTON_TEXT'/>",
    /**
     * @cfg {String} buttonTooltipText The button tooltip text
     */ 
    buttonTooltipText : "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_COLORPICKER_SHOW_COLORS_BUTTON_TOOLTIP'/>",
    /**
     * @cfg {String} emptyText The text for empty field
     */
    emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_COLORPICKER_EMPTY_TEXT'/>",
    
    /**
     * @cfg {Object/Object[]} listColors An array of color objects, with properties :
     * {String} listColors.label The name of the picker
     * {String[]} [listColors.colors] The list of colors code, such as "FF0000".
     * If listColors is undefined, it will assume the default color configuration.
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
    
    initComponent : function() 
    {
        this.value = this.value || "";
        
        // Color field
        var displayedColorConfig = {
            cls: Ametys.form.AbstractField.READABLE_TEXT_CLS,
            html: '',
            flex: 1
        };
        this._displayedColorField = Ext.create('Ext.Component', displayedColorConfig);

        var listColors = eval(this.getInitialConfig('listColors'));
        var colorSelector = Ext.create('Ametys.form.field.ColorSelector', {
            listColors: listColors,
            allowTransparent: this.getInitialConfig('allowTransparent'),
            allowOtherColors: this.getInitialConfig('allowOtherColors'),
            allowNoColor: this.getInitialConfig('allowNoColor'),
            currentColor: this.value,
            callback: Ext.bind(this._onColorSelected, this)
        });
        
        this._colorSelectorMenu = Ext.create('Ext.button.Button', {
            text: this.buttonText,
            tooltip: this.buttonTooltipText,
            menu: {
                items: [colorSelector]
            }
        });
        
        this.items = [ this._displayedColorField, this._colorSelectorMenu ];         

        this.layout = 'hbox';
        
        this.callParent(arguments);
    },
    
    onResize: function(width, height, oldWidth, oldHeight)
    {
        this._displayedColorField.setHeight(height);
    },
    
    getReadableValue: function ()
    {
        if (this.value)
        {
            this._displayedColorField.setStyle('backgroundColor', '#' + this.value);
            return "";
        }
        else
        {
            this._displayedColorField.setStyle('backgroundColor', '');
            return this.emptyText;
        }
    },
    
    setValue: function (value) 
    {   
        this.callParent([value]);
        this._updateUI();
    },
    
    afterRender: function()
    {
        this.callParent(arguments);
        this._updateUI();
    },
    
    /**
     * Update UI
     * @private
    */
    _updateUI: function()
    {   
        var value = this.value;
        
        if (!this.rendered)
        {
            return;
        }
        
        this._updateDisplayField();
    },
    
    /**
     * Update the display field as a understanding value for the end user
     * @private
    */
    _updateDisplayField: function()
    {
        if (!this.rendered)
        {
            return;
        }
        
        this._displayedColorField.update(this.getReadableValue());
    },
    
    /**
     * @private
     * Listener when a color has been selected on the {@link Ametys.form.field.ColorSelector}.
     * Updates the value of the widget.
     * @param {String} color The 6 hexadecimal upper case color code.
     */
    _onColorSelected: function(color)
    {
        this.setValue(color);
        this.clearWarning();
        this._colorSelectorMenu.hideMenu();
    }

});