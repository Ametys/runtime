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
 * This widget is a tri-state widget used for all types of externalizable field.
 * A externalizable field is a field with a editable local value and a non-editable external value.
 */
Ext.define('Ametys.form.widget.Externalizable', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
    alias: ['widget.externalizable'],
    
    statics: {
    	/** 
         * @property {String} _LOCAL_SUFFIX The suffix for local widget
         * @readonly
         * @private
         */ 
    	_LOCAL_SUFFIX: "_local",
        
    	/** 
         * @property {String} _LOCAL_SUFFIX The suffix for local widget
         * @readonly
         * @private
         */ 
    	_EXTERNAL_SUFFIX: "_external",
    	
    	/** 
         * @property {String} _STATUS_SUFFIX The suffix for status field
         * @readonly
         * @private
         */ 
    	_STATUS_SUFFIX: "_status"
    },
    	
    /**
     * @property {String} externalizableCls The base class for this field
     * @private
     */
    externalizableCls: "x-field-externalizable",
    
    /**
     * @property {String} localStatusBtnIconCls The separated CSS classes to be applied to the icon of status button when local value is active.
     * @private
     */
    localStatusBtnIconCls: 'ametysicon-v-switch-off',
    
    /**
     * @property {String} localStatusBtnCls The CSS class to apply to the status button when local value is active.
     * @private
     */
    localStatusBtnCls: 'x-field-externalizable-btn-local',
    
    /**
     * @property {String} localStatusBtnTooltip The tooltip for synchronization button when local value is active
     * @private
     */
    localStatusBtnTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_EXTERNALIZABLE_SYNCHRO_OFF}}",
    
    /**
     * @property {String} extStatusBtnIconCls The separated CSS classes to be applied to the icon of status button when external value is active.
     * @private
     */
    extStatusBtnIconCls: 'ametysicon-v-switch-on',
    
    /**
     * @property {String} extStatusBtnCls The CSS class to apply to the status button when external value is active.
     * @private
     */
    extStatusBtnCls: 'x-field-externalizable-btn-external',
    
    /**
     * @property {String} extStatusBtnTooltip The tooltip for synchronization button when external value is active
     * @private
     */
    extStatusBtnTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_EXTERNALIZABLE_SYNCHRO_ON}}",
    
    /** 
     * @cfg {String/Object} layout
     * @private 
     */
    layout: { 
        type: 'hbox',
        align: 'middle'
    },
    
    config: {
        /**
         * @cfg {Boolean} readOnly true to prevent the user from changing the field
         */
        readOnly: false
    },

    /** 
     * @cfg {Object/Object[]} items
     * @private 
     */
     
    /**
     * @readonly
     * @property {Boolean} isExternalizableField True means the field is a tri-state field for externalizable values
     */
    isExternalizableField: true, 
    
    
    /**
     * @private
     * @property {Ext.form.Field} _localField The field holding the local value
     */
    
    /**
     * @private
     * @property {Ext.form.Field} _externalField The field holding the external value
     */
    
    /**
     * @private
     * @property {Ext.Button} _statusBtn The button to switch on/off the synchronization
     */
     
    constructor: function(config)
    {
        config.cls = Ext.Array.from(config.cls);
        config.cls.push(this.externalizableCls);
        config.id = config.id || Ext.id();
        
        this._localField = this._createWrappedField(config.name + Ametys.form.widget.Externalizable._LOCAL_SUFFIX, config, false);
        this._externalField = this._createWrappedField(config.name + Ametys.form.widget.Externalizable._EXTERNAL_SUFFIX, config, true);
        
        if (this._localField.isRichText)
        {
        	this.isRichText = this._localField.isRichText;
        	this.getNode = Ext.bind(this._localField.getNode, this._localField);
        	this.getEditor = Ext.bind(this._localField.getEditor, this._localField);
        }
        
        this._statusBtn = Ext.create ('Ext.Button', {
        	cls: 'a-btn-lighter ' + this.localStatusBtnCls,
        	iconCls: this.localStatusBtnIconCls,
        	tooltip: this.localStatusBtnTooltip,
        	border: false,
        	enableToggle: true,
        	toggleHandler: this._switchStatus,
        	scope: this,
        	scale: 'large',
        	width: 40,
        	height: 40
        });
        
        config.items = [this._statusBtn, {
        	xtype: 'container',
        	flex: 1,
        	layout: { 
                type: 'vbox',
                align: 'stretch'
            },
        	items:[
        	   this._localField, 
        	   this._externalField
        	]
        }]
        
        this.callParent(arguments);
    },
    
    /**
     * @private
     * Create the wrapped field (local or external)
     * @param {String} fieldName The field name
     * @param {Object} config The initial configuration
     * @param {Boolean} external True if the field is the created field
     */
    _createWrappedField: function (fieldName, config, external)
    {
    	var clonedConfig = Ext.clone(config);
    	
    	var wrappedWidgetCfg = Ext.applyIf({
            name: fieldName,
            hideLabel: true,
            width: '100%',
            readOnly: config.readOnly || external,
            disabled: external,
            id: Ext.id(),
            msgTarget: 'none',
            preventMark: true
        }, clonedConfig);
    	
    	delete wrappedWidgetCfg.ametysDescription;
    	delete wrappedWidgetCfg.showAmetysComments;
    	delete wrappedWidgetCfg.fieldLabel;
    	delete wrappedWidgetCfg.anchor;
    	delete wrappedWidgetCfg.style;
    	delete wrappedWidgetCfg['wrapped-widget'];
    	
    	if (external)
    	{
    		wrappedWidgetCfg.style = {
    			marginBottom: 0
    		}
    	}
        
        if (clonedConfig.validation)
        {
            var validation = clonedConfig.validation;
            
            wrappedWidgetCfg.validationConfig = validation;
            wrappedWidgetCfg.regexp = validation.regexp || null;
            
            if (validation.invalidText)
            {
            	wrappedWidgetCfg.invalidText = validation.invalidText;
            }
            if (validation.regexText)
            {
            	wrappedWidgetCfg.regexText = validation.regexText;
            }
        }
        
        if (clonedConfig.enumeration)
        {
            var enumeration = [];
            
            var entries = clonedConfig.enumeration;
            for (var j=0; j < entries.length; j++)
            {
                enumeration.push([entries[j].value, entries[j].label]);
            }
            wrappedWidgetCfg.enumeration = enumeration;
        }
        
        var field = Ametys.form.WidgetManager.getWidget (clonedConfig['wrapped-widget'], config.type.toLowerCase(), wrappedWidgetCfg);
        
        if (field.isRichText)
        {
            /**
             * @event htmlnodeselected
             * Fires when a HTML node is selected in the editor of a richtext field
             * @param {Ext.form.Field} field The editor field
             * @param {tinymce.Editor} editor The tinyMCE editor
             * @param {HTMLElement} node The HTML element selected
             */
            field.on({
                'editorhtmlnodeselected': { fn: function (field, node) { this.fireEvent ('htmlnodeselected', this, node)}, scope: this }
            });
        }
        
        // if field is disabled or not visible we return no errors
        field.getErrors = Ext.Function.createInterceptor(field.getErrors, function() { return this.isVisible() && !this.isDisabled(); }, null, []);
        return field;
    },
    
    /**
     * @private
     * Handle when the status button is pressed
     * @param {Ext.Button} btn The toggle button
     * @param {Boolean} state The state of the button
     */
    _switchStatus: function (btn, state)
    {
    	this._switchToExternalStatus(state);
    	this.renderActiveWarning();
    },
    
    /**
     * @private
     * Switch the current status of synchronization
     * @param {Boolean} state true if to switch to the external status, false otherwise
     */
    _switchToExternalStatus: function (state)
    {
    	this._status = state ? 'external' : 'local';
    	if (state)
    	{
    		this._statusBtn.setTooltip(this.extStatusBtnTooltip);
    		this._statusBtn.setIconCls(this.extStatusBtnIconCls);
    		this._statusBtn.removeCls(this.localStatusBtnCls);
    		this._statusBtn.addCls(this.extStatusBtnCls);
    	}
    	else
    	{
    		this._statusBtn.setTooltip(this.localStatusBtnTooltip);
    		this._statusBtn.setIconCls(this.localStatusBtnIconCls);
    		this._statusBtn.removeCls(this.extStatusBtnCls);
    		this._statusBtn.addCls(this.localStatusBtnCls);
    	}
    	
    	this._localField.setDisabled(state);
		this._externalField.setDisabled(!state);
    },
    
    getValue: function()
    {
        return {
        	local: this._localField.getValue(),
        	external: this._externalField.getValue(),
        	status: this._status
        }
    },
    
    getSubmitValue: function ()
    {
    	var value = {
        	local: this._localField.getSubmitValue(),
        	external: this._externalField.getSubmitValue(),
        	status: this._status
    	}
    	
        return Ext.encode(value);
    },
    
    setValue: function(value)
    {
    	this.callParent(arguments);
    	
    	var localValue = value,
    		extValue = null,
    		external = false;
    	
        if (Ext.isObject(value) && (value.status || value.local || value.external))
        {
        	localValue = value.local;
        	extValue = value.external;
        	external = value.status == 'external';
        }
        
        this._localField.setValue(localValue);
    	this._externalField.setValue(extValue);
    	this._statusBtn.toggle(external, false);
    	this._switchToExternalStatus(external);
    },  
    
    getErrors: function(value)
    {
        if (this._status == 'external')
        {
        	return this._externalField.getErrors(value ? value.external : null);
        }
        else
        {
        	return this._localField.getErrors(value ? value.local : null);
        }
    },
    
    getActiveWarning: function ()
    {
    	if (this._status == 'external')
        {
        	return this._externalField.getActiveWarning();
        }
        else
        {
        	return this._localField.getActiveWarning();
        }
    },
    
    getActiveWarnings: function ()
    {
    	if (this._status == 'external')
        {
        	return this._externalField.getActiveWarnings();
        }
        else
        {
        	return this._localField.getActiveWarnings();
        }
    }
});
