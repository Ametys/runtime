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
    
    statics: {
        /**
         * @readonly
         * @property {RegExp} FILTER_TAGS The regular expression used to filter editor tags (to count characters).
         * @private
         */
        FILTER_TAGS: new RegExp("(<p( [^>]+)?>" + String.fromCharCode(0xA0) + "<\/p>)|(<[^>]*>)|(\r?\n)", "g")
    },
    
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
     * @cfg {Boolean} checkTitleHierarchy When true, a warning is displayed if the title hierarchy is wrong (h2 before h1...)
     */
    
    /**
     * @private
     * @property {Number} _updateEvery Time in ms between an event on the editor and the time the counter is updating (to prevent too many updates). This will vary in the life time of the editor: a big content will auto increase this value
     */
    _updateEvery: 1000,
    /**
     * @private
     * @property {String} _editorContent A cached value of the current editor content, may be null.
     */
    _editorContent: null,
    /**
     * @private
     * @property {Number} _charCount A cached value of the current character count in the editor, -1 if unknown.
     */
    _charCount: -1,
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
     * @property {Boolean} _editorInitialized=false Has the editor been initialized?
     */
    _editorInitialized: false,
    
	/**
	 * @private
	 * @property {Number} _suspended The number of times the transmission was suspended. 0 means transmission of selection events between tinymce and the ribbon are not suspended. Cannot be negative.
	 */
	_suspended: 0,
	
	/**
	 * @cfg {Boolean/String} resizable=false True to let the user resize the editor. "vertical" to only allow a vertical resize
	 */
	/**
	 * @property {Function} _bindedOnEditorResized The method _onEditorResized binded to the current object. Saved to unbind at destroy time.
	 * @private
	 */
	/**
	 * @property {Object} _editorDiffSize The differential of size between the whold field and the editor inside
	 * @property {Number} _editorDiffSize.width The width in pixel of diffence between the whole field width and the editor width
	 * @property {Number} _editorDiffSize.height The width in pixel of diffence between the whole field width and the editor height
	 * @private
	 */
	
	/**
    * @cfg {Object[]} annotations List of available semantic annotations for this RichText
    * @cfg {Object} annotations.name Unique name of the semantic annotation
    * @cfg {Object} annotations.label Display name of the semantic annotation
    * @cfg {Object} annotations.description Description of the semantic annotation
    */
	
	/**
	 * @property _annotations See #cfg-annotations
	 * @private
	 */
	_annotations: [],
	
    /**
     * @cfg {Number} maxLength
     * Maximum input field length allowed by validation.
     * Defaults to Number.MAX_VALUE.
     */
    /**
     * @private
     * @property {Number} _maxLength The maximum input field length allowed by validation. Defaults to Number.MAX_VALUE.
     * N.B: the maxLength default configuration field is forced to Number.MAX_VALUE to disable default Textarea validation.
     */
    _maxLength: Number.MAX_VALUE,
    
    constructor: function(config)
    {
    	this._checkTinyMCE();
    	this._enhanceTinyMCE();
    	
    	var resizable = config.resizable;
    	config.resizable = false;
    	
    	config = Ext.applyIf(config, {
    		height: 200,
    		minHeight: 100
    	});
    	
    	config.minHeight = Math.min(config.minHeight, config.height);
    	
    	var withBBar = config.warning || config.charCounter;
    	tinyMinHeight = config.minHeight - (withBBar ? 23 : 0);
    	
    	this.callParent(arguments);
    	
    	
    	this._settings = Ext.apply({
	    		document_base_url: Ametys.CONTEXT_PATH + "/",
	    		language: Ametys.LANGUAGE_CODE,
	    		mode: "none",
	    		height: config.height,
	    		min_height: tinyMinHeight,

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
				readonly: config.disabled,
				
				theme: 'advanced',
				theme_advanced_toolbar_location: "none",
				theme_advanced_statusbar_location : withBBar ? "bottom" : "none",
				theme_advanced_path : false,
				theme_advanced_resizing : resizable ? true: false,
				theme_advanced_resize_horizontal : resizable != "vertical" ? true: false,
				theme_advanced_resizing_use_cookie: false,
				theme_advanced_resizing_min_height: tinyMinHeight,
				
	    		// The plugins to load
	    		plugins: 'table,paste,noneditable,autolink'
	    	}, 
	    	config.settings || {}
	    );
    	this._settings.setup = Ext.bind(this._onEditorSetup, this);
    },
    
    /**
     * Initialize the maximum length.
     */
    initComponent: function()
    {
        this.callParent(arguments);
        
        // Force the default maxLength configuration to Number.MAX_VALUE to disable default validation.
        if (this.maxLength != Number.MAX_VALUE)
        {
            this._maxLength = this.maxLength;
            this.maxLength = Number.MAX_VALUE;
        }
        
        if (this.annotations)
        {
        	this._annotations = this.annotations;
        }
    },
    
    getRawValue: function()
    {
    	var editor = this.getEditor(); 
    	if (editor)
    	{
    	    // Cache the current editor content.
    	    if (this._editorContent == null)
	        {
    	        this._editorContent = editor.getContent();
    	        this.inputEl.dom.value = this._editorContent;
    	        
    	        // Compute the character count.
    	        this._computeCharCount(editor);
	        }
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
    
    initEvents: function()
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
	             * @event editorkeyup
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
     * Overridden to add custom maxLength validation.
     * @param {Object} value The current field value.
     */
    getErrors: function(value)
    {
        var me = this,
            errors = me.callParent(arguments);
        
        // me.callParent called getRawValue, _charCount should always be up-to-date.
        if (me._charCount > me._maxLength)
        {
            errors.push(Ext.String.format(me.maxLengthText, me._maxLength));
        }
        
        return errors;
    },
    
    /**
     * Get the registered semantic annotations
     * @return {Object[]} The semantic annotations
     */
    getSemanticAnnotations: function ()
    {
    	return this._annotations;
    },
    
    /**
     * Determines if this RichText has semantic annotations
     * @return true if this RichText has semantic annotations
     */
    hasSemanticAnnotations : function ()
    {
    	return this._annotations.length > 0;
    },
    
    /**
     * Get a semantic annotation by its name
     * @param {String} name The name of annotation
     * @return {Object} The annotation object or null if not found
     */
    getSemanticAnnotation: function (name)
    {
    	for (var i=0; this._annotations.length; i++)
    	{
    		if (this._annotations[i].name == name)
    		{
    			return this._annotations[i];
    		}
    	}
    	return null;
    },
    
    /**
     * @private
     * Check if tinymce is correctly loaded and in the right version. Throws an error if it is not the case.
     */
    _checkTinyMCE: function()
    {
    	var me = this;
    	function failure()
    	{
    		var msg = "tinyMCE 3 cannot be found. Please import the js file of tinymce to be able use RichText fields."
    		me.getLogger().error(msg);
    		throw new Error(msg);
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
				"<i18n:text i18n:key='KERNEL_LANGUAGE_CODE' i18n:catalogue='kernel'/>" : { 
					common: {
						charcount_chars: "<i18n:text i18n:key='KERNEL_FIELD_CARACTERS_COUNTER_1' catalogue='kernel'/>", 
						charcount_max: "<i18n:text i18n:key='KERNEL_FIELD_CARACTERS_COUNTER_2' catalogue='kernel'/>"
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
     * Creates the char counter under the editor. Use #cfg-charCounter
     * @param {tinymce.Editor} editor The editor object. If null the active editor will be used.
     */
    _createCharCounter: function(editor)
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
					 +    (this._maxLength == Number.MAX_VALUE ? '' : (' ' +  editor.getLang('charcount_max', 'on') + ' ' + this._maxLength)) 
			}
		);
    },
    
    /**
     * @private
     * Update the char counter under the editor.
     * @param {tinymce.Editor} editor The editor object.
     */
    _updateCharCounter: function(editor)
    {
        if (this._charCount > -1)
        {
            var count = this._charCount;
            
            var counter = document.getElementById(editor.editorId + "-counter-val"); 
            if (counter != null)
            {
                Ext.get(counter).removeCls("char-count-counting");
                counter.innerHTML = count;
            }
            
            // is there a maxlength ?
            if (this._maxLength != Number.MAX_VALUE)
            {
                if (count > this._maxLength)
                {
                    Ext.get(counter).addCls("char-count-maxexceed");
                }
                else
                {
                    Ext.get(counter).removeCls("char-count-maxexceed");
                }
            }
        }
    },
    
    /**
     * @private
     * This listener is called when the internal field state needs to be updated.
     * @param {tinymce.Editor} [editor] The editor object
     */
    _triggerUpdate: function(editor)
    {
        // Invalidate the editor content and current char count.
        this._editorContent = null;
        this._charCount = -1;
        
		editor = editor || this.getEditor(); 
		if (editor != null)
		{
			var counter = Ext.get(editor.editorId + "-counter-val");
			if (counter != null)
			{
			    counter.addCls("char-count-counting");
			}
			
			if (this._counting != null)
		    {
			    window.clearTimeout(this._counting);
		    }
			this._counting = window.setTimeout(Ext.bind(this._update, this), this._updateEvery);
		}
    },
    
    /**
     * @private
     * Compute the character count, validate the field and update the counter.
     * @param {tinymce.Editor} [editor] The editor object. If null the active editor will be used.
     */
    _update: function(editor)
    {
        // Cancel the running timer if necessary.
        if (this._counting != null)
        {
            window.clearTimeout(this._counting);
            this._counting = null;
        }
        
		editor = editor || this.getEditor();
		if (editor != null)
		{
			var took = new Date().getTime();
			
			// Validate the field (triggers character counting).
			this.validate();
			
			if (this.checkTitleHierarchy) 
			{
				if (this._checkTitleHierarchy())
				{
					this.clearWarning();
				}
				else
				{
					this.markWarning("<i18n:text i18n:key='KERNEL_FIELD_RICH_TEXT_HIERARCHY_ERROR'/>");
				}
			}
			
			// Update the counter.
			this._updateCharCounter(editor);
		    
			var took2 = new Date().getTime();
			editor._updateEvery = Math.max(took2 - took, 1000);
		}    	
    },
    
    _checkTitleHierarchy: function()
    {
	    var valid = true;
	    
	    var value = this.getValue();
	    
	    var titles = value.match(/<h[123456]/g);
	    if (titles != null)
	    {
	        // Check that there is no h3 after h1, etc.
	        var previousLevel = 0;
	        for (var i = 0; i < titles.length && valid; i++)
	        {
	            var level = parseInt(titles[i].charAt(2));
	            valid = (level - previousLevel) < 2;
	            previousLevel = level;
	        }
	    }
	    
	    return valid;
    },
    
    /**
     * @private
     * Update the internal char count now.
     * @param {tinymce.Editor} [editor] The editor object. If null the active editor will be used.
     */
    _computeCharCount: function(editor)
    {
        // Get the editor content from the cache or from the editor (just in case).
        var editorContent = this._editorContent || editor.getContent();
        
        // Filter the tags and compute the character count. 
        this._charCount = editorContent.replace(Ametys.form.field.RichText.FILTER_TAGS, '').length;
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
    	if (this.getLogger().isDebugEnabled())
    	{
    		this.getLogger().debug("Richtext '" + this.getInputId() + "' editor initialized")
    	}
    	
		this._createCharCounter(editor);
		
		// Cache the editor content and compute char count.
		this.getRawValue();
		this._updateCharCounter(editor);
		
		this._createWarning(editor);

		// Resize purposes
		this._bindedOnEditorResized = Ext.bind(this._onEditorResized, this);
		editor.dom.bind(editor.getWin(), 'resize', this._bindedOnEditorResized);
		
		// Setting css classes for browsers
		var iframe = Ext.get(this.getEditor().contentAreaContainer).first();
		this._setBrowserCSSClassName(iframe.dom.contentWindow.document.body);
		
		// Re-layout purposes
		Ext.get(editor.contentAreaContainer).first().on('load', this._onEditorLoaded, this);
    },
    
    /**
     * @private
     * Add the current browser informations on the body of the iframe (as it is on extjs body)
     * @param {HTMLElement} body The body to modify
     */
    _setBrowserCSSClassName: function(body)
    {
    	var body = Ext.get(body); 
		function add(cls)
		{
			body.addCls("x-" + cls);
		}
		
		
        //Let's keep this human readable!
        if (Ext.isIE && Ext.isIE9m) {
            add('ie');

            // very often CSS needs to do checks like "IE7+" or "IE6 or 7". To help
            // reduce the clutter (since CSS/SCSS cannot do these tests), we add some
            // additional classes:
            //
            //      x-ie7p      : IE7+      :  7 <= ieVer
            //      x-ie7m      : IE7-      :  ieVer <= 7
            //      x-ie8p      : IE8+      :  8 <= ieVer
            //      x-ie8m      : IE8-      :  ieVer <= 8
            //      x-ie9p      : IE9+      :  9 <= ieVer
            //      x-ie78      : IE7 or 8  :  7 <= ieVer <= 8
            //
            if (Ext.isIE6) {
                add('ie6');
            } else { // ignore pre-IE6 :)
                add('ie7p');

                if (Ext.isIE7) {
                    add('ie7');
                } else {
                    add('ie8p');

                    if (Ext.isIE8) {
                        add('ie8');
                    } else {
                        add('ie9p');

                        if (Ext.isIE9) {
                            add('ie9');
                        }
                    }
                }
            }

            if (Ext.isIE7m) {
                add('ie7m');
            }
            if (Ext.isIE8m) {
                add('ie8m');
            }
            if (Ext.isIE9m) {
                add('ie9m');
            }
            if (Ext.isIE7 || Ext.isIE8) {
                add('ie78');
            }
        }
        
        if (Ext.isIE10) {
            add('ie10');
        }
        
        if (Ext.isGecko) {
            add('gecko');
            if (Ext.isGecko3) {
                add('gecko3');
            }
            if (Ext.isGecko4) {
                add('gecko4');
            }
            if (Ext.isGecko5) {
                add('gecko5');
            }
        }
        if (Ext.isOpera) {
            add('opera');
        }
        if (Ext.isWebKit) {
            add('webkit');
        }
        if (Ext.isSafari) {
            add('safari');
            if (Ext.isSafari2) {
                add('safari2');
            }
            if (Ext.isSafari3) {
                add('safari3');
            }
            if (Ext.isSafari4) {
                add('safari4');
            }
            if (Ext.isSafari5) {
                add('safari5');
            }
            if (Ext.isSafari5_0) {
                add('safari5_0')
            }
        }
        if (Ext.isChrome) {
            add('chrome');
        }
        if (Ext.isMac) {
            add('mac');
        }
        if (Ext.isLinux) {
            add('linux');
        }
    },
    
    /**
     * @private
     * Relay iframe events to extjs
     */
    _onRelayedEvent: function (event) {
        // relay event from the iframe's document to the document that owns the iframe...

    	var iframeEl = Ext.get(this.getEditor().contentAreaContainer).first();

            // Get the left-based iframe position
            iframeXY = Ext.Element.getTrueXY(iframeEl),
            originalEventXY = event.getXY(),

            // Get the left-based XY position.
            // This is because the consumer of the injected event (Ext.EventManager) will
            // perform its own RTL normalization.
            eventXY = Ext.EventManager.getPageXY(event.browserEvent);

        // the event from the inner document has XY relative to that document's origin,
        // so adjust it to use the origin of the iframe in the outer document:
        event.xy = [iframeXY[0] + eventXY[0], iframeXY[1] + eventXY[1]];

        event.injectEvent(iframeEl); // blame the iframe for the event...

        event.xy = originalEventXY; // restore the original XY (just for safety)
    },
    
    /**
     * @private
     * Listener when the iframe is loaded to detect if the richtext is broken
     */
    _onEditorLoaded: function()
    {
    	var editor = this.getEditor();
    	
    	var iframe = Ext.get(editor.contentAreaContainer).first();
    	
        try {
        	var doc = iframe.dom.contentWindow.document;

        	Ext.EventManager.removeAll(doc);

            // These events need to be relayed from the inner document (where they stop
            // bubbling) up to the outer document. This has to be done at the DOM level so
            // the event reaches listeners on elements like the document body. The effected
            // mechanisms that depend on this bubbling behavior are listed to the right
            // of the event.
            Ext.EventManager.on(doc, {
                mousedown: this._onRelayedEvent, // menu dismisal (MenuManager) and Window onMouseDown (toFront)
                mousemove: this._onRelayedEvent, // window resize drag detection
                mouseup: this._onRelayedEvent,   // window resize termination
                click: this._onRelayedEvent,     // not sure, but just to be safe
                dblclick: this._onRelayedEvent,  // not sure again
                
                scope: this
            });
        } catch(e) {
            // cannot do this xss
        	console.info(e)
        }

    	if (iframe.dom.contentWindow.document.body.id)
    	{
    		this._onEditorResized();
    		return;
    	}

    	if (this.getLogger().isDebugEnabled())
    	{
    		this.getLogger().debug("Reseting richtext " + this.getInputId());
    	}
    	
    	this.getRawValue();
   		editor.save = function() {}
   		this._removeEditor();
   		new tinyMCE.Editor(this.getInputId(), this._settings).render();
    },
    
    /**
     * @private
     * Editor is resized, we have to resize field 
     */
    _onEditorResized: function()
    {
    	var force = false;
    	if (this._editorFrameWrapperDiffSize == null)
    	{
    		force = true,
    		this._prepareForResize();
    	}
    	
    	var editor = this.getEditor();
    	
    	var editorTab = Ext.get(editor.contentAreaContainer).parent("table") 
    	var editorSize = editorTab.getSize();

		var currentSize = this.getSize();
		var newSize = { width: editorSize.width + this._editorDiffSize.width, height: editorSize.height + this._editorDiffSize.height };
		if (force || newSize.width != currentSize.width || newSize.height != currentSize.height)
		{
			// Manual resize of the editor => impact the widget
			this.setSize(newSize.width, newSize.height);
		}
    },
    
    /**
     * @private
     * This method initialize the #_editorFrameWrapperDiffSize and should be called once only.
     */
    _prepareForResize: function()
    {
    	var editor = this.getEditor();
    	
		var editorFrame = Ext.get(editor.contentAreaContainer).first();
		var editorSize = editorFrame.getSize();
    	var editorWrapper = editorFrame.parent("table").parent("td");
    	var parentSize = editorWrapper.getSize();
		this._editorFrameWrapperDiffSize = { width: parentSize.width - editorSize.width, height: parentSize.height - editorSize.height };

		this._adaptEditorToPlace();
    },
    
    /**
     * @private
     * Adapt the size of the iframe of the editor to the widget size. Called when the widget is resized.
     */
    _adaptEditorToPlace: function()
    {
    	var editor = this.getEditor();
    	if (editor == null)
    	{
    		return;
    	}
    	
    	if (this._editorFrameWrapperDiffSize == null)
    	{
    		this._prepareForResize();
    		return;
    	}
    	
    	var editorFrame = Ext.get(editor.contentAreaContainer).first();
    	
    	var editorPlace = editorFrame.parent("table").parent("td");
    	var editorSize = editorPlace.getSize();

		var wholeSize = this.getSize();
		this._editorDiffSize = { width: wholeSize.width - editorSize.width, height: wholeSize.height - editorSize.height }; 

		var newSize = { width: editorSize.width - this._editorFrameWrapperDiffSize.width, height: editorSize.height - this._editorFrameWrapperDiffSize.height };
    	editorFrame.setSize(newSize);
    },
    
    markInvalid: function()
    {
    	this.callParent(arguments);
    	
    	if (this.isVisible(true))
	    {
    	    this._adaptEditorToPlace();
	    }
    },
    
    markWarning: function()
    {
    	this.callParent(arguments);
    	
    	if (this.isVisible(true))
	    {
    	    this._adaptEditorToPlace();
	    }
    },
    
    clearInvalid: function()
    {
    	this.callParent(arguments);

        if (this.isVisible(true))
        {
            this._adaptEditorToPlace();
        }
    },
    
    clearWarning: function()
    {
    	this.callParent(arguments);

        if (this.isVisible(true))
        {
            this._adaptEditorToPlace();
        }
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
		
		var _triggerUpdate = Ext.bind(this._triggerUpdate, this);
		editor.onChange.add(_triggerUpdate);
		editor.onSetContent.add(_triggerUpdate);
		editor.onKeyUp.add(_triggerUpdate);
		
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
			this.getLogger().error(msg);
			throw new Error(msg);
		}

		this._suspended--;
	},
	
	afterRender: function()
	{
    	this.callParent(arguments);
    	this.addCls('x-field-richtext');
	},
	
	afterComponentLayout: function()
    {
    	this.callParent(arguments);

    	// Creates the tinymce editor to replace the underlying textarea input
    	if (!this._editorInitialized)
    	{
    		this._editorInitialized = true;
    		new tinyMCE.Editor(this.getInputId(), this._settings).render();
    	}
    },
    
    /**
     * Remove the editor
     * @private
     */
    _removeEditor: function()
    {
    	var editor = this.getEditor();
    	if (editor != null)
    	{
        	var iframe = Ext.get(editor.contentAreaContainer).first();
        	Ext.EventManager.removeAll(iframe.dom.contentWindow.document);
            	
	    	editor.dom.unbind(editor.getWin(), 'resize', this._bindedOnEditorResized);
	    	
	    	// Let's destroy the tinymce component
	    	tinyMCE.execCommand("mceRemoveControl", false, this.getInputId());
	    	
	    	this._notFirstCallToOnRichTextNodeSelected = false;
    	}
    },
    
    beforeDestroy: function() 
    {
    	if (this.getLogger().isDebugEnabled())
    	{
    		this.getLogger().debug("Richtext '" + this.getInputId() + "' editor destroyed")
    	}

    	if (tinyMCE.activeEditor == this.getEditor())
    	{
    		tinyMCE.activeEditor = null;
    	}
    		
    	this._removeEditor();
    	
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
     * @param {Object} e The event
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
     * @param {Object} e The event
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
     * @param {Object} e The event
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
	}
	
});
