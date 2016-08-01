/*
 *  Copyright 2014 Anyware Services
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
 * This class provides a combo box with local store for handling a boolean field. List options are: empty / yes / no.<br>
 * Labels of options are configurable. See {@link #cfg-emptyLabel}, {@link #cfg-trueLabel} and {@link #cfg-falseLabel}.
 * 
 * This widget is registered for fields of type Ametys.form.WidgetManager#TYPE_BOOLEAN.<br>
 */
Ext.define('Ametys.form.widget.BooleanCombobox', {
    extend: "Ext.form.field.ComboBox",
    
    /**
     * @cfg {String} emptyLabel='-' Label used for empty option 
     */
    emptyLabel: "{{i18n PLUGINS_CORE_UI_WIDGET_BOOLEAN_COMBOBOX_DEFAULT_EMPTY_LABEL}}",
    
    /**
     * @cfg {String} trueLabel='yes' Label used for 'true' option
     */
    trueLabel: "{{i18n PLUGINS_CORE_UI_WIDGET_BOOLEAN_COMBOBOX_DEFAULT_TRUE_LABEL}}",
    
    /**
     * @cfg {String} falseLabel='no' Label used for 'false' option
     */
    falseLabel: "{{i18n PLUGINS_CORE_UI_WIDGET_BOOLEAN_COMBOBOX_DEFAULT_FALSE_LABEL}}",
    
    constructor: function (config)
    {
        config = Ext.apply(config, {
            editable: false,
            forceSelection: true,
            triggerAction: 'all',
            
            queryMode: 'local',
            store: [
               ['', config.emptyLabel || "{{i18n PLUGINS_CORE_UI_WIDGET_BOOLEAN_COMBOBOX_DEFAULT_EMPTY_LABEL}}"],
               [true, config.trueLabel || "{{i18n PLUGINS_CORE_UI_WIDGET_BOOLEAN_COMBOBOX_DEFAULT_TRUE_LABEL}}"],
               [false, config.falseLabel || "{{i18n PLUGINS_CORE_UI_WIDGET_BOOLEAN_COMBOBOX_DEFAULT_FALSE_LABEL}}"]
            ]
        });
        
        config.value = config.value === undefined ? '' : config.value;  
        
        this.callParent(arguments);
    },
    
    getSubmitValue: function()
    {
        var value = this.getValue();
        if (Ext.isEmpty(value))
        {
            value = null;
        }
        else
        {
            value = value === true ? "true" : "false";
        }
        
        return value;
    }
});
