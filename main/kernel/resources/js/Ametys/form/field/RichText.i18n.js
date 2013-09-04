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
 * Field that displays a richtext.
 * Requires tinymce (version 3) to be loaded.
 * See http://www.tinymce.com/wiki.php/TinyMCE3x:TinyMCE_3.x to have documentation on it.
 */
Ext.define('Ametys.form.field.RichText', {
    extend: 'Ext.form.field.TextArea',
    alias: ['widget.richtextfield', 'widget.richtext'],
    alternateClassName: ['Ext.form.RichTextField', 'Ext.form.RichText', 'Ext.form.field.RichText'],
    
    /**
     * @cfg {String[]} settings Settings that will be transmitted to tinymce to create the editor. See http://www.tinymce.com/wiki.php/Configuration3x.
     * They are many default settings.
     * Example:
     * 
     *      settings: {
     *      	content_css: "style1.css,style2.css",
     *      }
     *      
     * You cannot bind the "setup" setting: use a listener on initialization.
     */
    /**
     * @property {Object} _settings The settings to init the editor. See #cfg-settings.
     * @private
     */
    
    /**
     * @cfg {Boolean} charCounter=false Show the char counter.
     */
    /**
     * @cfg {String} warning Display a warning in the statusbar. Can be used when the richtext width is generally fixed (to the rendered size) but is not in some cases.
     */
    
    /**
     * @private
     * @property {Number} _checkCounterEvery Time in ms between an event on the editor and the time the counter is updating (to prevent too many updates). This will vary in the life time of the editor: a big content will auto increase this value
     */
    _checkCounterEvery: 1000,
    /**
     * @private
     * @property {Date} _lastCharCountUpdate The time the char count was lastly updated
     */
    /**
     * @private
     * @property {Object} _counting The timeout object of a pending recount of the characters
     */
    /**
     * @private
     * @property {Boolean} _notFirstCallToOnRichTextNodeSelected=false This value is false until the editor is selected once. Stays true after.
     */
    _notFirstCallToOnRichTextNodeSelected: false,
    
	/**
	 * @private
	 * @property {Number} _suspended The number of times the transmission was suspended. 0 means transmission of selection events between tinymce and the ribbon are not suspended. Cannot be negative.
	 */
	_suspended: 0,
    
    constructor: function(config)
    {
    	this._checkTinyMCE();
    	this._enhanceTinyMCE();
    	
    	this.callParent(arguments);
    	
    	this._settings = Ext.apply({
	    		document_base_url: Ametys.CONTEXT_PATH + "/",
	    		language: Ametys.LANGUAGE_CODE,
	    		mode: "none",

				// Settings
	    		doctype: "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
				entity_encoding : 'raw',
				fix_list_elements : true,
				fix_table_elements : true,
				fix_nesting : true,
				verify_css_classes : false,
				gecko_spellcheck : true,	    		
				paste_strip_class_attributes: "mso",
				paste_remove_styles: true,
				relative_urls : false,
				remove_script_host: false,
				strict_loading_mode : true,
				
				theme: 'advanced',
				theme_advanced_toolbar_location: "none",
				theme_advanced_statusbar_location : config.warning || config.charCounter ? "bottom" : "none",
				theme_advanced_path : false,
				theme_advanced_resizing : false,
				theme_advanced_resizing_use_cookie: false,

	    		
	    		// The plugins to load
	    		plugins: 'table,paste,tabfocus,noneditable,autolink',
	    	}, 
	    	config.settings || {}
	    );
    	this._settings.setup = Ext.bind(this._onEditorSetup, this);
    },
    
    getRawValue: function()
    {
    	var editor = this.getEditor(); 
    	if (editor)
    	{
	    	var html = editor.getContent(); 
	    	this.inputEl.dom.value = html;
    	}
    	
    	return this.callParent(arguments);
    },
    
    setRawValue: function(value)
    {
    	this.callParent(arguments);
    	
    	var editor = this.getEditor(); 
    	if (editor)
    	{
    		editor.setContent(value);
    	}
    },
    
    initEvent: function()
    {
    	this.callParent(arguments);
    	
		this.addEvents (
				/**
	             * @event editorsetcontent
	             * Fires when the editor received new content. This allows to convert storing tags to internal tags.
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} object A tinymce object
	             * @param {String} object.content The html content
	             */
	            'editorsetcontent',
	            /**
	             * @event editorgetcontent
	             * Fires when the editor received content. This allows to convert internal tags to storing tags.
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} object A tinymce object
	             * @param {String} object.content The html content
	             */
	            'editorgetcontent',
	            /**
	             * @event editorkeypress
	             * Fires when the editor has a key press.
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} e The event 
	             */
	            'editorkeypress',
	            /**
	             * @event editorkeydown
	             * Fires when the editor has a key down.
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} e The event 
	             */
	            'editorkeydown',
	            /**
	             * @event editorkeydown
	             * Fires when the editor has a key up.
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} e The event 
	             */
	            'editorkeyup',
	            /**
	             * @event editorvisualaid
	             * Fires when the editor pre process the serialization
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} object The object 
	             */
	            'editorvisualaid',
	            /**
	             * @event editorpreprocess
	             * Fires when the editor pre process the serialization
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {Object} object The object 
	             */
	            'editorpreprocess',
	            /**
	             * @event editorhtmlnodeselected
	             * Fires when a HTML node is selected in editor
	             * @param {Ext.form.Field} field The editor field
	             * @param {tinymce.Editor} editor The tinyMCE editor
	             * @param {HTMLElement} node The HTML element selected
	             */
	            'editorhtmlnodeselected'
		);
    },
    
    /**
     * @private
     * Check if tinymce is correctly loaded and in the right version. Throws an error if it is not the case.
     */
    _checkTinyMCE: function()
    {
    	function failure()
    	{
    		var msg = "tinyMCE 3 cannot be found. Please import the js file of tinymce to be able use RichText fields."
    		Ametys.log.Logger.error();
    		throw e;
    	}
    	
    	try
    	{
    		if (tinyMCE.majorVersion != "3")
    		{
    			failure();
    		}
    	}
    	catch (e)
    	{
    		failure();
    	}
    },
    
    /**
     * @private
     * Add methods, and position some path on tinymce
     */
    _enhanceTinyMCE: function()
    {
		if (!tinyMCE.save)
		{
			tinyMCE.baseURL = Ametys.CONTEXT_PATH + '/plugins/tiny_mce/resources/js';

			/*
			 * Bookmark the cursor position for IE
			 */
			tinyMCE.save = function(editor)
			{
				editor = editor || tinyMCE.activeEditor;
				if (Ext.isIE)
				{
					editor._bookmark = editor.selection.getBookmark(1);
				}
			};
			
			/*
			 * Focus and on IE restore the last bookmarked position
			 */ 
			tinyMCE.focus = function ()
			{
				tinyMCE.activeEditor.focus();
				if (tinyMCE.activeEditor._bookmark)
				{
					tinyMCE.activeEditor.selection.moveToBookmark(tinyMCE.activeEditor._bookmark);
				}
			};
		
			// Add i18n for the char counter
			tinyMCE.addI18n({ 
				<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/> : { 
					common: {
						charcount_chars: "<i18n:text i18n:key="KERNEL_FIELD_CARACTERS_COUNTER_1" catalogue="kernel"/>", 
						charcount_max: "<i18n:text i18n:key="KERNEL_FIELD_CARACTERS_COUNTER_2" catalogue="kernel"/>"
					}
				}
			});
			
			/*
			 * Insert the given html code at the root of the current selection of the current editor
			 * @param {String} html The html code to insert
			 * @param {String} [mode=split] The mode 'split' : to split the current place of the cursor to insert the html ; 'after' to insert after the root element of the current selection ; 'before' idem but before.
			 */
			tinyMCE.insertHTMLAtRoot = function(html, mode)
			{
				mode = mode || 'split';
				var editor = tinyMCE.activeEditor;
				
				var rootelements = 'h1,h2,h3,h4,h5,h6,div,p';
				
				var root = editor.dom.getParent(editor.selection.getNode(), rootelements);

				if (mode == 'split' || root == null)
				{
					// UPGRADE tinyMCE 3.3.3 plugins/table/js/table.js insertTable
					editor.selection.setContent('<br class="_mce_marker" />');
				
					var patt = '';
					tinymce.each(rootelements.split(','), function(n) {
						if (patt)
							patt += ',';
				
						patt += n + ' ._mce_marker';
					});
				
					tinymce.each(editor.dom.select(patt), function(n) {
						editor.dom.split(editor.dom.getParent(n, rootelements), n);
					});
				
					editor.dom.setOuterHTML(editor.dom.select('br._mce_marker')[0], html);
				}
				else
				{
					var br = editor.dom.doc.createElement("br")
					root.parentNode.insertBefore(br, mode == 'after' ? root.nextSibling : root);
					editor.dom.setOuterHTML(br, html);
				}
			};			
		}
    },
    
    /**
     * @private
     * Creates the char count under the editor. Use #cfg-charCounter
     * @param {tinymce.Editor} editor The editor object. If null the active editor will be used.
     */
    _createCharCount: function(editor)
    {
    	if (this.initialConfig.charCounter !== true)
    	{
    		return;
    	}
    	
		// Char counter
		Ext.get(editor.id + '_path_row').createChild({
				tag: 'span',
				cls: 'char-counter',
				html: editor.getLang('charcount_chars', 'Characters:') + ' '
					 +    '<span id="' + editor.id + '-counter-val' + '">?</span>'
					 +    (this.maxLength == Number.MAX_VALUE ? '' : (' ' +  editor.getLang('charcount_max', 'on') + ' ' + this.maxLength)) 
			}
		);
		
		this._updateCharCount(editor);
    },
    
    /**
     * @private
     * This listener is called when the counter needs to be updated
     * @param {tinymce.Editor} [editor] The editor object
     */
    _updateCharCount: function(editor)
    {
		editor = editor || this.getEditor(); 
		if (editor != null)
		{
			var counter = document.getElementById(editor.editorId + "-counter-val"); 
			if (counter != null)
			{
				var time = new Date().getTime();
				var lastTime = this._lastCharCountUpdate;
				this._lastCharCountUpdate = time;

				if (lastTime == null || (time - lastTime >= this._checkCounterEvery && this._counting != null))
				{
					this._setCharCount(editor);
				}
				else
				{
					Ext.get(counter).addCls("char-count-counting");
					window.clearTimeout(this._counting);
					this._counting = window.setTimeout(Ext.bind(this._setCharCount, this), this._checkCounterEvery);
				}
			}
		}
    },
    
    /**
     * @private
     * Update the char counter now.
     * @param {tinymce.Editor} [editor] The editor object. If null the active editor will be used.
     */
    _setCharCount: function(editor)
    {
		editor = editor || tinyMCE.activeEditor; 
		if (editor != null)
		{
			var took = new Date().getTime();
			
			var r = new RegExp("<p( [^>]+)?>" + String.fromCharCode(0xA0) + "<\/p>", "g");
			var a = editor.getContent({from: 'char-counter'}).replace(/\r?\n/g, '').replace(r, '').replace(/<[^>]*>/g, '').length;
			var counter = document.getElementById(editor.editorId + "-counter-val"); 
			if (counter != null)
			{
				Ext.get(counter).removeCls("char-count-counting");
				this._counting = null;
				counter.innerHTML = a;
			}
			
			// is there a maxlength ?
			if (this.maxLength != Number.MAX_VALUE)
			{
				if (a > this.maxLength)
				{
					Ext.get(count).addCls("char-count-maxexceed");
				}
				else
				{
					Ext.get(count).removeCls("char-count-maxexceed");
				}
			}

			var took2 = new Date().getTime();
			editor._checkCounterEvery = Math.max(took2 - took, 1000);
		}    	
    },
    
    /**
     * @private
     * Displays the #cfg-warning if necessary
     * @param {tinymce.Editor} editor The editor object
     */
    _createWarning: function(editor)
    {
    	if (this.initialConfig.warning)
    	{
			Ext.get(editor.id + '_path_row').insertSibling({
					tag: 'img',
					'data-qtip': this.initialConfig.warning,
					src: Ametys.CONTEXT_PATH +  '/plugins/cms/resources/img/content/warning.png',
					style: 'float: right; margin: 3px;'
				},
				'after'
			);
    	}
    },
    
    /**
     * @private
     * Listener when the editor is initialized
     * @param {tinymce.Editor} editor The editor object
     */
    _onEditorInit: function(editor)
    {
		this._createCharCount(editor);

		this._createWarning(editor);
    },
    
    /**
     * @private
     * Listener when the editor is being setup
     * @param {tinymce.Editor} editor The editor object
     */
    _onEditorSetup: function(editor)
    {
		if (Ext.isIE)
		{
			editor.onEvent.add(tinyMCE.save);
		}
		
		editor.onInit.add(Ext.bind(this._onEditorInit, this));
		
		var _updateCharCount = Ext.bind(this._updateCharCount, this);
		editor.onChange.add(_updateCharCount);
		editor.onSetContent.add(_updateCharCount);
		editor.onKeyUp.add(_updateCharCount);
		
		editor.onNodeChange.add (Ext.bind(this._onEditorRichTextNodeSelected, this));
		editor.onBeforeSetContent.add(Ext.bind(this._onEditorSetContent, this));
		editor.onGetContent.add(Ext.bind(this._onEditorGetContent, this));
		editor.onKeyPress.add(Ext.bind(this._onEditorKeyPress, this));
		editor.onKeyDown.add(Ext.bind(this._onEditorKeyDown, this));
		editor.onKeyUp.add(Ext.bind(this._onEditorKeyUp, this));
		editor.onVisualAid.add(Ext.bind(this._onEditorVisualAid, this));
		editor.onPreProcess.add(Ext.bind(this._onEditorPreProcess, this));		
    },
    
	/**
	 * Suspend the event fired to the messagebus when a node is selected.
	 * Use it when you know that several events could be sent at once : suspend before the first, and restart before the last.
	 * Do not forget to call the restart method! (each suspend should have a restart - think about try/catch to ensure this)
	 */
	suspendRichTextNodeSelectionEvent: function()
	{
		this._suspended++;
	},
	/**
	 * Restart the transmission of selection event to the messagebug.
	 * Do not call this method if you do not have call the suspend one before.
	 */
	restartRichTextNodeSelectionEvent: function()
	{
		if (this._suspended == 0)
		{
			var msg = "Ametys.form.field.RichText#restartRichTextNodeSelectionEvent method has been called but transmissions where not suspended";
			Ametys.log.Logger.error({category: this.self.getName(), message: msg});
			throw new Error(msg);
		}

		this._suspended--;
	},
	
	afterComponentLayout: function()
    {
    	this.callParent(arguments);
    	
    	this.addCls('x-field-richtext');

		// Creates the tinymce editor above the textarea input
		new tinyMCE.Editor(this.getInputId(), this._settings).render();
    },
    
    beforeDestroy: function() 
    {
    	// Let's destroy the tinymce component
    	tinyMCE.execCommand("mceRemoveControl", false, this.getInputId());

    	this.callParent(arguments);
    },
    
    /**
     * Get the underlying tinymce editor (see http://www.tinymce.com/wiki.php/API3:class.tinymce.Editor).
     * @return {tinymce.Editor} The wrapper tinymce editor object. Can be null before the end of the render process or after the destroy process. 
     */
    getEditor: function()
    {
    	return tinyMCE.get(this.getInputId());
    },
    
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorSetContent: function (editor, object)
	{
		this.fireEvent ('editorsetcontent', this, editor, object);
	},
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorGetContent: function (editor, object)
	{
		this.fireEvent ('editorgetcontent', this, editor, object);
	},
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorKeyPress: function (editor, e)
	{
		if (tinymce.isGecko)
		{
			if (e.keyCode == 13 || e.charCode == 13) 
			{
				var rng = editor.selection.getRng();
	
				if (rng.startContainer && editor.getBody() == rng.startContainer && /^table$/i.test(editor.selection.getStart().nodeName)) 
				{
					editor.execCommand('mceInsertContent', false, '<p><br mce_bogus="1"/></p>');
	
					return tinymce.dom.Event.cancel(e);
				}
			}
		}
		
		this.fireEvent ('editorkeypress', this, editor, e);
	},
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorKeyDown: function (editor, e)
	{
		// BUG FIX CMS 2499
		if (tinymce.isGecko)
		{
			// UPGRADE (tinyMCE 3.3.9.1) plugins/table/editor_plugin_src.js line 968  but handling div form insteadof table
			var rng, table, dom = editor.dom;
	
			// On gecko it's not possible to place the caret before a table
			if (e.keyCode == 37 || e.keyCode == 38) 
			{
				rng = editor.selection.getRng();
				table = dom.getParent(rng.startContainer, 'table');
	
				if (table && editor.getBody().firstChild != table) // DOING THE REVERSE CONDITION BECAUSE WE WANT TO DO SO ALL THE TIME 
				{
					if (org.ametys.forms._isAtStart(rng, table)) 
					{
						rng = dom.createRng();
	
						rng.setStartBefore(table);
						rng.setEndBefore(table);
	
						editor.selection.setRng(rng);
						
						e.preventDefault();
					}
				}
			}
		}	
		
		this.fireEvent ('editorkeydown', this, editor, e);
	},
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorKeyUp: function (editor, e)
	{
		this.fireEvent ('editorkeyup', this, editor, e);
	},
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorVisualAid: function (editor, object)
	{
		this.fireEvent ('editorvisualaid', this, editor, object);
	},
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
	_onEditorPreProcess: function (editor, object)
	{
		this.fireEvent ('editorpreprocess', this, editor, object);
	},    
	/**
	 * This function is called when a node is selected in the rich text
     * @param {tinymce.Editor} editor The tinymce editor involved
     * @param {Object} controlManager See tinymce doc.
     * @param {HTMLElement} node The currently selected node
     * @param {Boolean} isCollapsed Is the current selection collaspsed or not
	 * @private
	 */
	_onEditorRichTextNodeSelected: function(editor, controlManager, node, isCollapsed)
	{
		if (editor.contentDocument == node || this._suspended > 0)
		{
			return;
		}
		
		if (this._notFirstCallToOnRichTextNodeSelected)
		{
			Ext.defer(this.fireEvent, 1, this , ['editorhtmlnodeselected', Ext.getCmp(Ext.get(editor.id).parent(".x-form-item").id), editor.selection.getNode()]);
		}
		else
		{
			editor.onClick.remove(editor.nodeChanged);
		}
		this._notFirstCallToOnRichTextNodeSelected = true;
	},
});
