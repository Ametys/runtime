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
 * Field that displays a code editor.
 * Requires CodeMirror (version 3.14) to be loaded.
 * See http://codemirror.net/ to have documentation on it.
 */
Ext.define('Ametys.form.field.Code', {
    extend: 'Ext.form.field.TextArea',
    alias: ['widget.code'],
    
    /** 
     * @cfg {String} mode=htmlmixed The CodeMirror mode
     */
    /** 
     * @cfg {Object} cmParams The CodeMirror parameters.
     * The `mode` and `value` standard parameters will be ignored.
     */
    /** 
     * @cfg {Boolean} singleLine=false Set to `true` to get a single-line editor.
     */
    
    /**
     * @property {String} The CodeMirror mode. See #cfg-mode
     */
    _mode: null,
    /**
     * @property {Object} _codeMirror The CodeMirror instance.
     * @private
     */
    _codeMirror: null,
    /**
     * @property {Boolean} _singleLine `true` when the editor is single-line.
     * @private
     */
    _singleLine: false,
    
    /**
     * @property {Boolean} _initialized True when the CodeMirror area is initialized. 
     * @private
     */
    _initialized: false,
    
    /**
     * @property {String} _futureValue The value to set when after code mirror initialization.
     * @private 
     */
    _futureValue: '',
    
    /**
     * @property {Boolean} [_readOnly=false] Set to 'true' to open tool in read-only mode
     */
    _readOnly: false,
    
    focusable: true,
    liquidLayout: false,
    
    /**
     * @inheritdoc
     */
    initComponent: function()
    {
            /**
             * @event initialize
             * Fires when the CodeMirror area is initialized.
             */
            
            /**
             * @event change
             * Fires when the content was changed.
             */
    
        this.callParent(arguments);
        
        // listeners
        this.on('render', this._onRender, this),
        this.on('resize', this._onResize, this);
        this.on('move', this._onMove, this);
        this.on('initialize', this._init, this);
    },
    
    constructor: function (config)
    {
        this.callParent(arguments);
        this._mode = config.mode || 'htmlmixed';
        this._singleLine = config.singleLine || false;
        this._readOnly = config.readOnly === true;
    },
    
    /**
     * Get the code mirror instance
     */
    getCM: function()
    {
        return this._codeMirror;
    },
    
    setReadOnly: function (readOnly)
    {
        this._readOnly = readOnly;
        if (this._codeMirror) 
        {
            this._codeMirror.setOption ('readOnly', readOnly);
        }
    },
    
    /**
     * Returns the parameter(s) that would be included in a standard form submit for this field. Typically this will be
     * an object with a single name-value pair, the name being this field's {@link #getName name} and the value being
     * its current stringified value. More advanced field implementations may return more than one name-value pair.
     *
     * Note that the values returned from this method are not guaranteed to have been successfully {@link #validate
     * validated}.
     *
     * @return {Object} A mapping of submit parameter names to values; each value should be a string, or an array of
     * strings if that particular name has multiple values. It can also return null if there are no parameters to be
     * submitted.
     */
    getSubmitData: function() 
    {
        var me = this,
            data = null;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) 
        {
            data = {};
            data[me.getName()] = '' + me._codeMirror.getValue();
        }
        return data;
    },
    
    reset: function()
 	{
 		this.originalValue = this.originalValue || '';
 		this.callParent(arguments);
 	},
    
    getValue: function()
    {
        var value;
        if (this._codeMirror) 
        {
            value = this._codeMirror.getValue();
        }
        else if (this._futureValue != null)
        {
            value = this._futureValue;
        }
        else
        {
            value = this.initialConfig.value;
        }
        
        if (this.rendered)
        {
            this.inputEl.dom.value = value;
            return this.callParent(arguments);
        }
        
        return value;
    },

    setValue: function (v)
    {
        this.callParent([v]);
        
        if (this._codeMirror) 
        {
            this._codeMirror.setValue(v);
        }
        else
        {
            this._futureValue = v;
        }
    },
    
    /**
     * @private
     * {@link #event-initialize} listener
     */
    _init: function()
    {   
    	if (!Ext.isEmpty(this._futureValue))
        {
            this.setValue(this._futureValue);
            this._futureValue = null;
        }
    },
    
    /**
     * Fires when the base textarea is rendered: initializes the CodeMirror area from the textarea.
     * @private
     */
    _onRender: function()
    {
        this._initializeCodeMirror();
    },

    /**
     * @private
     * {@link #event-resize} listener
     */
    _onResize: function(editor, width, height)
    {
        if (this._codeMirror)
        {
            var s = this.bodyEl.getSize(true);
            this._codeMirror.setSize(s.width - 2, s.height - 2); // This - 2 is a hack due to border, but will soon or later fail
            // FIXME this should fail with errorEl visible or ametysDescription?
        }
    },
    
    /**
     * @private
     * {@link #event-move} listener
     */
    _onMove: function(editor, x, y)
    {
        if (this._codeMirror)
        {
            this._codeMirror.scrollTo(x, y);
        }
    },
    
    focus: function()
    {
        this.callParent(arguments);
        
        if (this._codeMirror) 
        {
            this._codeMirror.focus();
        }
    },
    
    /**
     * Initialize the CodeMirror
     * @private
     */
    _initializeCodeMirror: function()
    {
        var me = this;
        
        if (this._codeMirror == null)
        {
            // Get the textarea HTMLElement.
            var textarea = Ext.dom.Query.selectNode('textarea', this.getEl().dom);
            
            // Default parameters.
            var defaultParams = {
                matchBrackets: true, // add-on
                lineNumbers: true,
                styleActiveLine: true,
                
                extraKeys: {
                    "Ctrl-U": "undo",
                    "Ctrl-Y": "redo"
                }
            };
            
            // Non-overridable parameters.
            var forcedParams = {
                mode: this._mode,
                value: me.initialConfig.value || '',
                readOnly: this._readOnly
            };
            
            // Merge default params, then user params, then forced params.
            var params = Ext.Object.merge(defaultParams, me.initialConfig.cmParams || {}, forcedParams);
            
            // Create the CodeMirror instance.
            this._codeMirror = CodeMirror.fromTextArea(textarea, params);
            
            // Used to be able to modify the change (for instance, filter newline chars).
            this._codeMirror.on('beforeChange', Ext.bind(this._onBeforeChange, this));
            
            // Styling the current cursor line
            this._codeMirror.on("change", Ext.bind(this._onChange, this));
            
            /**
             * @event beforechange
             * Fires before the content is changed.
             * @param {Object} codeMirror the current codeMirror instance
             * @param {Object} changes The object with the current changes, with properties from, to and text, a cancel() and a update() method.
             */            
            this._codeMirror.on("beforeChange", function(cm, change) { 
                me.fireEvent('beforechange', cm, change); 
            });
            
            // Relay on change event
            this._codeMirror.on("change", function() { me.fireEvent('change', true); });
            
            me.fireEvent('initialize', true);
            me._initialized = true;
        }
    },
    
    /**
     * Fired before a change is applied in the CodeMirror editor.
     * @param {Object} cm The CodeMirror editor
     * @param {Object} change The change
     * @private
     */
    _onBeforeChange: function(cm, change)
    {
        // If single-line, join the different lines and filter out newline characters.
        if (this._singleLine && change.update)
        {
            var newtext = change.text.join('').replace(/\n/g, '');
            change.update(change.from, change.to, [newtext]);
        }
    },
    
    /**
     * Listens for change in CodeMirror editor
     * @param {Object} cm The CodeMirror editor
     * @param {Object} change The change
     * @private
     */
    _onChange: function(cm, change)
    {
        var oldValue = this.value, // value is updated when calling this#getValue
            newValue = this.getValue();
        this.fireEvent('change', this, newValue, oldValue);
    },
    
    onFocusEnter: function(e)
    {
        this.callParent(arguments);
        this.onFocus(e);
    },

    onFocusLeave: function(e)
    {
        this.callParent(arguments);
        this.onBlur(e);
    },
    
    getState: function()
    {
        var state = this.callParent(arguments);
        if (state && state.value)
        {
            state.value = Ext.JSON.encode(state.value);
        }
        return state;
    },
    
    applyState: function(state)
    {
        if (state && state.value)
        {
            state.value = Ext.JSON.decode(state.value);
        }
        this.callParent(arguments);
    }
});
