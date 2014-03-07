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
	 * @property {String} The CodeMirror mode. See #cfg-mode
	 */
	_mode: null,
	/**
	 * @property {Object} _codeMirror The CodeMirror instance.
	 * @private
	 */
	_codeMirror: null,
	
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
	 * @property {Number} border The border size
	 */
	border: 1,
	
	/**
	 * 
	 */
	anchor: '100% -20',
	
	/**
	 * @property {Object} style The style
	 */
	style: {
	    borderColor: '#ddd',
	    borderStyle: 'solid'
	},
	
	/**
	 * @inheritdoc
	 */
	initComponent: function()
	{
		this.addEvents(
			/**
			 * @event initialize
			 * Fires when the CodeMirror area is initialized.
			 */
			'initialize',
			
			/**
			 * @event change
			 * Fires when the content was changed.
			 */
			'change'
		);
	
		this.callParent(arguments);
		
		// listeners
		this.on('render', this._onRender, this),
		this.on('resize', this._onResize, this);
		this.on('move', this._onMove, this);
		this.on('focus', this._onFocus, this);
		this.on('initialize', this._init, this);
	},
	
	constructor: function (config)
	{
		this.callParent(arguments);
		this._mode = config.mode || 'htmlmixed';
	},

	/**
	 * Get the code mirror instance
	 */
	getCM: function()
	{
		return this._codeMirror;
	},
	
	/**
	 * Get the field value (the CodeMirror area content).
	 * @return {String} The full CodeMirror area content.
	 */
	getValue: function()
	{
		if (this._codeMirror) 
		{
			return this._codeMirror.getValue();
		}
		return this.initialConfig.value;
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

	/**
	 * Set the field value.
	 * @param {String} v The text to set.
	 */
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
		if (this._futureValue != '')
		{
			this.setValue(_futureValue);
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
			this._codeMirror.setSize(width, height);
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
	
	/**
	 * @private
	 * {@link #event-focus} listener
	 */
	_onFocus: function()
	{
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
		if (this._codeMirror == null)
		{
			// Get the textarea HTMLElement.
			var textarea = Ext.dom.Query.selectNode('textarea', this.getEl().dom);
			
			// Create the CodeMirror instance.
			var me = this;
			this._codeMirror = CodeMirror.fromTextArea(textarea, {
				mode: this._mode,
				value: me.initialConfig.value || '',
		
				matchBrackets: true, // add-on
				lineNumbers: true,
				
				extraKeys: {
					"Ctrl-U": "undo",
					"Ctrl-Y": "redo"
				},
				
				initCallback: function() {
					me._initialized = true;
					me.fireEvent('initialize', true);
				},
				
				onChange: function (n) { 
					me.fireEvent('change', true);
				}
			});
			
			// Styling the current cursor line
			this._codeMirror.on('cursorActivity', Ext.bind(this._onCursorActivity, this));
			this._codeMirror.on("change", Ext.bind(this._onChange, this));
		}
	},
	
	/**
	 * Listens for cursor activity to style the current line.
	 * @private
	 */
	_onCursorActivity: function()
	{
		// Styling the current cursor line
		if (this._hlLine)
		{
			this._codeMirror.removeLineClass(this._hlLine, 'text', 'activeline');
		}
		this._hlLine = this._codeMirror.addLineClass(this._codeMirror.getCursor().line, 'text', 'activeline');
	},
	
	/**
	 * Listens for change in CodeMirror editor
	 * @param {Object} cm The CodeMirror editor
	 * @param {Object} change The change
	 * @private
	 */
	_onChange: function(cm, change)
	{
		this.fireEvent('change', cm, change);
	}
});
