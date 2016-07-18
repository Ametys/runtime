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
 * Field that displays a richtext.
 * Requires tinymce (version 4) to be loaded.
 * See http://www.tinymce.com to have documentation on it.
 */
Ext.define('Ametys.form.field.RichText', {
    extend: 'Ametys.form.AbstractField',
    alias: ['widget.richtextfield', 'widget.richtext'],
    alternateClassName: ['Ext.form.RichTextField', 'Ext.form.RichText', 'Ext.form.field.RichText'],
    
    statics: {
        /** 
         * @property {Number} _MIN_HEIGHT The min value for the minHeight configuration
         * @readonly
         * @private
         */ 
        _MIN_HEIGHT: 100,
        
        /**
         * @property {Number} __DEFAULT_HEIGHT The default height value
         * @readonly
         * @private
         */
        _DEFAULT_HEIGHT: 200,
        
        /**
         * @readonly
         * @property {RegExp} FILTER_TAGS The regular expression used to filter editor tags (to count characters).
         * @private
         */
        FILTER_TAGS: new RegExp("(<p( [^>]+)?>" + String.fromCharCode(0xA0) + "<\/p>)|(<[^>]*>)|(\r?\n)", "g")        
    },
    
    /**
     * @property {String} richtextCls The base class for richtext
     * @private
     */
    richtextCls: "x-field-richtext",
    
    /**
     * @property {Boolean} _editorInitialized=false Has the editor been initialized?
     * @private
     */
    _editorInitialized: false,

    /**
     * @property {String} _editorId The editor identifier
     * @private
     */
    
    /**
     * @private
     * @property {Object} _availableModes A list of available editor modes.
     * @property {Boolean} _availableModes.preview When true the preview mode is available.
     * @property {Boolean} _availableModes.full When true the full mode is available.
     */
    /**
     * @private
     * @property {String} _currentMode One of the #_availableModes keys.
     */
    /**
     * @private
     * @property {Number} _maxLength See #cfg-maxLength
     */
    /**
     * @property _annotations See #cfg-annotations
     * @private
     */
    /**
     * @private
     * @property {Number} _updateEvery Time in ms between an event on the editor and the time the counter is updating (to prevent too many updates). This will vary in the life time of the editor: a big content will auto increase this value
     */
    _updateEvery: 100,
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
     * @property {Number} _suspended The number of times the transmission was suspended. 0 means transmission of selection events between tinymce and the ribbon are not suspended. Cannot be negative.
     */
    _suspended: 0,
    /**
     * @private
     * @property {String} charCounterCls The css classname for the counter
     */
    charCounterCls: 'char-counter',
    /**
     * @private
     * @property {String} charCounterValueCls The css classname for the counter value
     */
    charCounterValueCls: 'char-counter-value',
    /**
     * @private
     * @property {String} charCounterCountingCls The css classname for the counter when counting operation is proceeding
     */
    charCounterCountingCls: 'char-count-counting',
    /**
     * @private
     * @property {String} charCounterMaxExceededCls The css classname when the max number of characters was exceeded
     */
    charCounterMaxExceededCls: 'char-count-maxexceed',
    
    /** 
     * @cfg {String/Object} layout
     * @private 
     */
    layout: { 
        type: 'vbox',
        align: 'stretch'
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
     * @property {Boolean} isRichText True means the field is a richtext and have a #getNode method
     */
    isRichText: true, 
     
     
    /**
     * @cfg {Object[]} annotations List of available semantic annotations for this RichText
     * @cfg {String} annotations.name Unique name of the semantic annotation
     * @cfg {String} annotations.label Display name of the semantic annotation
     * @cfg {String} annotations.description Description of the semantic annotation
     */
     
    /**
     * @cfg {String/String[]} editorCSSFile Can be a simple file URL, a comma separated list of file URLs or an array of file URLs. Theses files are CSS files that will be loaded into the editor
     * @cfg {String} editorBodyClass=mceConentBody The class set on the editor body tag.
     */
    
    /**
     * @cfg {String} validElements See tinyMCE valid_elements configuration
     */
    /**
     * @cfg {Object} validStyles See tinyMCE valid_styles configuration
     */
    /**
     * @cfg {Object} validClasses See tinyMCE valid_classes configuration
     */

    /**
     * @cfg {Number} minHeight @inheritdoc
     * The minimum valeur for this argument is Ametys.form.field.RichText#_MIN_HEIGHT. So you cannot have a very small richtext even if you set a small minHeight value.
     */
     
    /**
     * @cfg {Number} wysiwygWidth=0 if greater than 0, the width of the editor will be fixed to this value. And the "preview" mode will be available and set by default.
     */
     
    /**
     * @cfg {Number} maxLength=Number.MAX_VALUE Maximum input field length allowed by validation.
     */
    /**
     * @cfg {Boolean} charCounter=false Show the char counter
     */
    /**
     * @cfg {Boolean} checkTitleHierarchy=false When true, a warning is displayed if the title hierarchy is wrong (h2 before h1...)
     */
    /**
     * @cfg {Boolean/String} editableSource=false When true or "true", the source code become editable in the editor
     */
     
    constructor: function(config)
    {
        this._checkTinyMCE();
        this._enhanceTinyMCE();
        
        this._availableModes = { preview: false, full: true, source: false };
        this._currentMode = "full";
        this._editorId = Ext.id(null, "ametysicon-");
        
        config.cls = Ext.Array.from(config.cls);
        config.cls.push(this.richtextCls);
        config.minHeight = Math.max(config.minHeight || 0, Ametys.form.field.RichText._MIN_HEIGHT);
        config.height = Ext.Number.constrain(config.height || Ametys.form.field.RichText._DEFAULT_HEIGHT, config.minHeight, config.maxHeight);
        config.id = config.id || Ext.id();
        
        this._maxLength = Ext.isNumber(config.maxLength) ? config.maxLength : Number.MAX_VALUE;
        this._annotations = Ext.Array.from(config.annotations);
        
        config.items = [
            { 
                xtype: 'container', 
                itemId: 'card', 
                layout: 'card', 
                flex: 1,
                items: [
                    { xtype: 'component', itemId: 'wrapper', cls: this.richtextCls + '-wrapper', scrollable: false, border: true, html: "<div id=\"" + this._editorId + "\"></div>" },
                    { xtype: 'code', itemId: 'source', isField: false, listeners: { change: Ext.bind(this._onSourceChange, this), focus: Ext.bind(this._onCodeFocus, this), blur: Ext.bind(this._onCodeBlur, this)} }
                ]
            }
        ];
        
        var toolbarItems = [];
        
        var align = 'left';
        
        // char counter
        if (config.charCounter === true)
        {
            toolbarItems.push({ 
                xtype: 'component', 
                cls: this.charCounterCls,
                html: "{{i18n PLUGINS_CORE_UI_FIELD_CARACTERS_COUNTER_1}} "
                     +    '<span id="' + config.id + '-counter-val' + '" class="' + this.charCounterValueCls + '">?</span>'
                     +    (this._maxLength == Number.MAX_VALUE ? '' : (" {{i18n PLUGINS_CORE_UI_FIELD_CARACTERS_COUNTER_2}} " + this._maxLength)) 
            });
        }
        
        // Resize handling
        if (config.resizable)
        {
            toolbarItems.push({ xtype: 'component', flex: 1 });
            toolbarItems.push({ 
                xtype: 'splitter',
                cls: this.richtextCls + "-splitter",
                border: true,
                performCollapse: false, 
                collapseDirection: 'top', 
                collapseTarget: 'prev', 
                width: 40, 
                size: '100%', 
                tracker: { xclass: 'Ametys.form.field.RichText.SplitterTracker', componentToResize: this } 
            });
            toolbarItems.push({ xtype: 'component', flex: 1 });
            align = 'right';
        }
        config.resizable = false; // the wrapping component is not rezisable by it self
        
        // Resize modes
        if (Ext.isNumber(config.wysiwygWidth) && config.wysiwygWidth > 0)
        {
            if (align != 'right')
            {
                toolbarItems.push({ xtype: 'component', flex: 1 });
                align = 'right'
            }
            
            toolbarItems.push({ 
                xtype: 'button', 
                cls: 'a-btn-light',
                iconCls: 'ametysicon-document209 decorator-ametysicon-world91',
                tooltip: {
                    glyphIcon: 'ametysicon-document209',
                    iconDecorator: 'decorator-ametysicon-world91',
                    title: "{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_MODE_PREVIEW_TOOLTIP_TITLE}}",
                    text: "{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_MODE_PREVIEW_TOOLTIP_TEXT}}",
                    anchor: "br-tr",
                    inribbon: false
                },
                enableToggle: true,
                allowDepress: false,
                pressed: true,
                toggleGroup: this._editorId + "-mode",
                handler: Ext.bind(this._setMode, this, ["preview", true], false) 
            });
            this._availableModes.preview = true;
            this._currentMode = "preview"
        }
        
        // Creating status bar if required
        if (toolbarItems.length > 0)
        {
            if (align != 'right')
            {
                toolbarItems.push({ xtype: 'component', flex: 1 });
                align = 'right'
            }

            toolbarItems.push({ 
                xtype: 'button', 
                cls: 'a-btn-light',
                iconCls: 'ametysicon-document209',
                tooltip: {
                    glyphIcon: 'ametysicon-document209',
                    title: "{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_MODE_FULLPAGE_TOOLTIP_TITLE}}",
                    text: "{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_MODE_FULLPAGE_TOOLTIP_TEXT}}",
                    anchor: "br-tr",
                    inribbon: false
                },
                enableToggle: true,
                allowDepress: false,
                pressed: this._currentMode == "full",
                toggleGroup: this._editorId + "-mode",
                handler: Ext.bind(this._setMode, this, ["full", true], false) 
            });
            
            if (config.editableSource === true || config.editableSource === "true")
            {
                toolbarItems.push({ 
                    xtype: 'button', 
                    cls: 'a-btn-light',
                    iconCls: 'ametysicon-html25',
                    tooltip: {
                        glyphIcon: 'ametysicon-html25',
                        title: "{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_MODE_SOURCE_TOOLTIP_TITLE}}",
                        text: "{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_MODE_SOURCE_TOOLTIP_TEXT}}",
                        anchor: "br-tr",
                        inribbon: false
                    },
                    enableToggle: true,
                    allowDepress: false,
                    toggleGroup: this._editorId + "-mode",
                    handler: Ext.bind(this._setMode, this, ["source", true], false) 
                });
                this._availableModes.source = true;
            }

            config.items.push({
                xtype: 'container',
                cls: this.richtextCls + "-toolbar",
                border: true,
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                items: toolbarItems
            });
        }
        
        this.callParent(arguments);
        
        this._sendDelayedSelection = Ext.Function.createBuffered(this._sendDelayedSelection, Ext.isIE ? 80 : 10, this); // We buffer the sendSelection to avoid multiple events, but IE seems to need more time to eat them.
    },
    
    _onCodeFocus: function() {
        if (this._currentMode == "source")
        {
            this.onFocus();
        }
    },

    _onCodeBlur: function() {
        if (this._currentMode == "source")
        {
            this.onBlur();
        }
    },

    _onEditorFocus: function() {
        if (this._currentMode != "source")
        {
            this.onFocus();
        }
    },

    _onEditorBlur: function() {
        if (this._currentMode != "source")
        {
            this.onBlur();
        }
    },

    getValue: function()
    {
        var editor = this.getEditor();

        if (this._charCount == -1 && editor)
        {
            if (this._currentMode == "source")
            {
                // Cache the current editor content.
                {
                    var rawValue = this.getSourceEditor().getValue(); 
                    editor.setContent(rawValue);
                }
            }

            // Cache the current editor content.
            this.value = editor.getContent();
            
            // Filter the tags and compute the character count. 
            this._charCount = this.value.replace(Ametys.form.field.RichText.FILTER_TAGS, '').length;

            this.checkChange();
        }
    
        return this.callParent(arguments);
    },
    
    reset: function()
 	{
 		this.originalValue = this.originalValue || '';
 		this.callParent(arguments);
 	},
    
    setValue: function(value)
    {
        this.callParent(arguments);
        
        var editor;
        if (this._currentMode == "source")
        {
            this.getSourceEditor().setValue(value);
        }
        else if (editor = this.getEditor())
        {
            editor.setContent(value);
        }
    },  
    
    /**
     * Get the currently selection node in the richtext
     * @return {HTMLElement} The node. Can be null.
     */
    getNode: function()
    {
        if (this._currentMode != "source")
        {
            var editor = this.getEditor();
            if (editor)
            {
                var node = editor.selection.getNode();
                
                if (editor.contentDocument != node               // Do not send for #document
                    && this._suspended == 0                         // Do not send if suspended
                    )
                {
                    return node;
                }
            }
        }
        
        return null;
    },
    
    /**
     * @private
     * Get the underlying tinymce editor (see http://www.tinymce.com/wiki.php/API3:class.tinymce.Editor).
     * @return {tinymce.Editor} The wrapper tinymce editor object. Can be null before the end of the render process or after the destroy process. 
     */
    getEditor: function()
    {
        return tinyMCE.get(this._editorId);
    },
    
    /**
     * @private
     * Get the frame of the editor. Can be null if the editor is not ready.
     * @return {Element} The iframe element or null.
     */
    getFrameEl: function()
    {
        return this.getEditor() ? Ext.get(this.getEditor().contentAreaContainer).first() : null;
    },
    
    /**
     * @private
     * Get the window of the editor. Can be null if the editor is not ready.
     */
    getWindow: function()
    {
        var frame = this.getFrameEl()
        return frame ? frame.dom.contentWindow : null;
    },
    
    /**
     * @private
     * Get the document of the editor. Can be null if the editor is not ready.
     */
    getDocument: function()
    {
        var window = this.getWindow();
        return window ? window.document : null;
    },
    
    /**
     * Get the field for editing source mode
     * @return {Ext.form.field.Field} The field for source code editing or null if there is no right to edit source
     * @private 
     */
    getSourceEditor: function()
    {
        return this.getComponent("card").items.get(1);
    },
    
    getFocusEl: function()
    {
        if (this._currentMode == "source")
        {
            return this.getSourceEditor();
        }
        else
        {
            return this.getFrameEl();
        }
    },
    
    focus: function(selectText, delay)
    {
        var me = this,
            value, focusEl;

        if (delay) 
        {
            if (!me.focusTask) 
            {
                me.focusTask = new Ext.util.DelayedTask(me.focus);
            }
            me.focusTask.delay(Ext.isNumber(delay) ? delay : 10, null, me, [selectText, false]);
        }
        else 
        {
            if (this._currentMode == "source")
            {
                this.getSourceEditor().focus();
            }
            else
            {
                window.setTimeout(Ext.bind(this.getEditor().focus, this.getEditor()), 1);
            }
        }
    },

    /**
     * @private
     * Change the editor mode or reapply the current and disable state
     * @param {String} mode A constant of #_availableModes. Can be null to (re)apply existing mode
     * @param {Boolean} forceFocus Should focus goes back to the editor
     */
    _setMode: function(mode, forceFocus)
    {
        var newMode = mode || this._currentMode;
        
        if (this._availableModes[newMode] === true)
        {
            var value = this.getValue(); // We have to read this BEFORE changing the mode, to have the correct value
            
            this._currentMode = newMode;

            if (this._editorInitialized == false)
            {
                // Too soon... this method will be called again automatically after initialization
                return;
            }
            
            if (newMode == "source")
            {
                if (this.getComponent("card").getLayout().getActiveItem() != this.getComponent("card").items.get(1))
                {
                    this.getComponent("card").setActiveItem(1);
                    this.setValue(value); // Transmit the current value to the new edit component
                }
                
                this.getSourceEditor().setReadOnly(this.getReadOnly());
            }
            else
            {
                if (this.getComponent("card").getLayout().getActiveItem() != this.getComponent("card").items.get(0))
                {
                    this.getComponent("card").setActiveItem(0);
                    this.setValue(value); // Transmit the current value to the new edit component
                }
                
                var domUtils = this.getEditor().dom;
                
                var bodyTag = this.getDocument().body;
                
                var htmlTag = bodyTag.parentNode;
                Ext.Object.each(this._availableModes, function(mode) { domUtils.removeClass(htmlTag, mode); });
                domUtils.addClass(htmlTag, newMode);
                
                switch (newMode)
                {
                    case "preview":
                        // Apply #cfg-wysiwygWidth
                        domUtils.setStyle(bodyTag, "width", this.getInitialConfig("wysiwygWidth") + "px");
                        break;
                    case "full":
                    default:
                        domUtils.setStyle(bodyTag, "width", null);
                }
                
                // Adapt readonly mode
                bodyTag.setAttribute('contenteditable', !this.getReadOnly());
            }

            if (forceFocus)
            {
                this.focus(null, 1);
            }
        }

    },
    
    updateReadOnly: function(value)
    {
        this._setMode();
    },
    
    getErrors: function(value)
    {
        var errors = this.callParent(arguments);
        
        // me.callParent called getRawValue, _charCount should always be up-to-date.
        if (this._charCount > this._maxLength)
        {
            errors.push(Ext.String.format(this.maxLengthText, this._maxLength));
        }
        
        return errors;
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
            var msg = "tinyMCE 4 cannot be found. Please import the js file of tinymce to be able use RichText fields."
            me.getLogger().error(msg);
            throw new Error(msg);
        }
        
        try
        {
            if (tinyMCE.majorVersion != "4")
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
     * This method is called for IE only to bookmark the cursor position as IE may forgot it and restore the cursor to the 1st character.
     * It used in conjonction with #_restorePostion
     * @param {Obejct} o A tinymce object for command 
     */
    _savePosition: function(o)
    {
        if (o.command == "AutoUrlDetect" /* The autolink plugin send this command on focus to test if IE has this feature */
            || o.command == "mceCleanup" /* Do not change bookmark after a cleanup */
            || o.command == "mceAddUndoLevel" /* Do not change bookmark after a addundolevel */)
        {
            return;
        }
        
        var editor = this.getEditor();
        editor._bookmark = editor.selection.getBookmark(1);
    },

    /**
     * @private
     * This method is called for IE only to restore the cursor bookmark as IE may forgot it and restore the cursor to the 1st character.
     * It used in conjonction with #_savePosition 
     * @param {Obejct} o A tinymce object for command 
     */
    _restorePosition: function(o)
    {
        if (o.command == "AutoUrlDetect" /* The autolink plugin send this command on focus to test if IE has this feature */
            || o.command == "mceCleanup" /* Do not change bookmark after a cleanup */
            || o.command == "mceAddUndoLevel" /* Do not change bookmark after a addundolevel */)
        {
            return;
        }
        
        var editor = this.getEditor();
        if (editor._bookmark)
        {
            editor.selection.moveToBookmark(editor._bookmark);
            editor._bookmark = null;
        }
    },

    /**
     * @private
     * Add methods, and position some path on tinymce
     */
    _enhanceTinyMCE: function()
    {
        if (!tinyMCE.insertHTMLAtRoot)
        {
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
     * Check if the title hierarchy in the content is correct
     * &lt;h1&gt; before any &lt;h2&gt; and so on
     */
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
     * Get the registered semantic annotations
     * @return {Object[]} The semantic annotations. See #cfg-annotations for format.
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
    
    
    
    afterComponentLayout: function(width, height, oldWidth, oldWeight)
    {
        this.callParent(arguments);
        
        // Creates the tinymce editor
        if (!this._editorInitialized)
        {
            this._editorInitialized = true;
            
            this._createEditor();
        }
    },
    
    /**
     * @private
     * Creates the richtext editor
     */
    _createEditor: function()
    {
        tinyMCE.EditorManager.baseURL = Ametys.CONTEXT_PATH + '/plugins/tiny_mce/resources/js';
        tinymce.init({
            document_base_url: Ametys.CONTEXT_PATH + "/",
            language: Ametys.LANGUAGE_CODE,
            selector: "#" + this.getComponent("card").getComponent("wrapper").getEl().down("div").getId(),

            entity_encoding : 'raw',
            fix_list_elements : true,
            browser_spellcheck : true,              
            paste_data_images: true,
            paste_webkit_styles: "none",
            paste_retain_style_properties: "",
            relative_urls : false,
            remove_script_host: false,
            
            minHeight: 0,
            
            menubar: false,
            toolbar: false,
            table_toolbar: false,
            statusbar: false,
            elementpath : false,
            resize : false,
            
            setup: Ext.bind(this._onEditorSetup, this),
            
            content_css: Ext.isArray(this.getInitialConfig("editorCSSFile")) ? Ext.isArray(this.getInitialConfig("editorCSSFile")).join(',') : this.getInitialConfig("editorCSSFile"),
            body_class: this.getInitialConfig("editorBodyClass") || "mceContentBody",
            valid_elements: this.getInitialConfig("validElements"),
            valid_styles: this.getInitialConfig("validStyles"),
            valid_classes: this.getInitialConfig("validClasses"),
            
            plugins: 'table,paste,noneditable,autolink'
        });
    },
        
    /**
     * @private
     * Listener when the editor is being setup
     * @param {tinymce.Editor} editor The editor object
     */
    _onEditorSetup: function(editor)
    {
        editor.on('init', Ext.bind(this._onEditorInit, this));
        
        editor.on('NodeChange', Ext.bind(this._sendSelection, this));
        editor.on('focus', Ext.bind(function() { this.getEditor().nodeChanged() }, this)); // Giving focus using TAB a previously focused editor, does not fire 'NodeChange' automatically. This is necessary for the messagebus AND for UI issues (such as RUNTIME-1593)
        
        editor.on('focus', Ext.bind(this._onEditorFocus, this));
        editor.on('blur', Ext.bind(this._onEditorBlur, this));
        
        editor.on('GetContent', Ext.bind(this._onEditorGetContent, this));
        editor.on('BeforeSetContent', Ext.bind(this._onEditorSetContent, this));
        editor.on('PreProcess', Ext.bind(this._onEditorPreProcess, this));  
        
        editor.on('KeyPress', Ext.bind(this._onEditorKeyPress, this));
        editor.on('KeyDown', Ext.bind(this._onEditorKeyDown, this));
        editor.on('KeyUp', Ext.bind(this._onEditorKeyUp, this));
        
        editor.on('VisualAid', Ext.bind(this._onEditorVisualAid, this));
        
        if (Ext.isIE)
        {
            editor.on('BeforeExecCommand', Ext.bind(this._restorePosition, this));
            editor.on('focus', Ext.bind(this._restorePosition, this));
            
            editor.on('ExecCommand', Ext.bind(this._savePosition, this));
            editor.on('blur', Ext.bind(this._savePosition, this));
        }
    },
    
    /**
     * @private
     * Listener when the editor is initialized
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     */
    _onEditorInit: function(object)
    {
        var editor = object.target;
        
        if (this.getLogger().isDebugEnabled())
        {
            this.getLogger().debug("Richtext '" + this.getInputId() + "' editor initialized")
        }
        
        // Set the start value
        // We cannot call setValue that would trigger validate... 
        editor.setContent(this.value || "");
        
        var _onUpdate = Ext.bind(this._onUpdate, this);
        editor.on('change', _onUpdate);
        editor.on('SetContent', _onUpdate);
        editor.on('KeyUp', _onUpdate);

        this.suspendCheckChange++;
        this.lastValue = this.getValue(); // Call to #getValue is required to set _charCount ; moreover, the checkChange have to be helped by setting #lastValue manually (because #getValue changes the indentation)
        this.suspendCheckChange--;
        this._updateCharCounter(editor);
        // End set value
        
        this._setMode();
        
        // Setting css classes for browsers
        var body = this.getDocument().body;
        Ext.Object.each(Ext, function(key, value) {
            if (value === true && Ext.String.startsWith(key, "is") && key != "isDomReady" && key != "isReady")
            {
                tinyMCE.DOM.addClass(body, "x-" + key.toLowerCase().substring(2));
            }
        });
        
        // Re-layout purposes
        this._onEditorLoaded();
        this.getFrameEl().on('load', this._onEditorLoaded, this);
    },
    
    /**
     * @private
     * Listener when the iframe is loaded to detect if the richtext is broken
     */
    _onEditorLoaded: function()
    {
        var me = this,
            doc = me.getDocument(),
            fn = me.onRelayedEvent;

        if (doc) 
        {
            
            try {
                var extdoc = Ext.get(doc); 
                
                extdoc._getPublisher('mousemove').directEvents.mousemove = 1;
                extdoc._getPublisher('mousedown').directEvents.mousedown = 1;
                extdoc._getPublisher('mouseup').directEvents.mouseup = 1;
                extdoc._getPublisher('click').directEvents.click = 1;
                extdoc._getPublisher('dblclick').directEvents.dblclick = 1;
            
                extdoc.on(
                    me._docListeners = {
                        mousedown: fn, // menu dismisal (MenuManager) and Window onMouseDown (toFront)
                        mousemove: fn, // window resize drag detection
                        mouseup: fn,   // window resize termination
                        click: fn,     // not sure, but just to be safe
                        dblclick: fn,  // not sure again
                        scope: me
                    }
                );
                
                extdoc._getPublisher('mousemove').directEvents.mousemove = 0;
                extdoc._getPublisher('mousedown').directEvents.mousedown = 0;
                extdoc._getPublisher('mouseup').directEvents.mouseup = 0;
                extdoc._getPublisher('click').directEvents.click = 0;
                extdoc._getPublisher('dblclick').directEvents.dblclick = 0;
            
                
                // We need to be sure we remove all our events from the iframe on unload or we're going to LEAK!
                Ext.get(me.getWindow()).on('beforeunload', me.cleanupListeners, me);
            } catch(e) {
                // cannot do this xss
                this.getLogger().info(e)
            }
        } 

        if (!doc || !doc.body || !doc.body.className) // first time should be useless
        {
            if (this.getLogger().isDebugEnabled())
            {
                this.getLogger().debug("Reseting richtext " + this.getInputId());
            }
            
            this.getValue(); // Save the current editor value

            this._removeEditor();

            this._createEditor();
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
            this.getLogger().debug("remove " + editor.id)
            
            if (tinyMCE.activeEditor == editor)
            {
                tinyMCE.activeEditor = null;
            }

            this.cleanupListeners(true);
                
            // Let's destroy the tinymce component
            editor.remove();
        }
    },

    onRelayedEvent: function (event) 
    {
        // relay event from the iframe's document to the document that owns the iframe...

        var iframeEl = this.getFrameEl(),

            // Get the left-based iframe position
            iframeXY = iframeEl.getTrueXY(),
            originalEventXY = event.getXY(),

            // Get the left-based XY position.
            // This is because the consumer of the injected event will
            // perform its own RTL normalization.
            eventXY = event.getTrueXY();

        // the event from the inner document has XY relative to that document's origin,
        // so adjust it to use the origin of the iframe in the outer document:
        event.xy = [iframeXY[0] + eventXY[0], iframeXY[1] + eventXY[1]];

        event.injectEvent(iframeEl); // blame the iframe for the event...

        event.xy = originalEventXY; // restore the original XY (just for safety)
    },
    
    beforeDestroy: function () 
    {
        if (this.getLogger().isDebugEnabled())
        {
            this.getLogger().debug("Richtext '" + this.getInputId() + "' editor destroyed")
        }
        
        this._removeEditor();
        this.callParent();
    },
    
    cleanupListeners: function(destroying)
    {
        var doc, prop;

        if (this.rendered) 
        {
            try 
            {
                doc = this.getDoc();
                if (doc) 
                {
                    Ext.get(doc).un(this._docListeners);
                    if (destroying) 
                    {
                        for (prop in doc) 
                        {
                            if (doc.hasOwnProperty && doc.hasOwnProperty(prop)) 
                            {
                                delete doc[prop];
                            }
                        }
                    }
                }
            } catch(e) { }
        }
    },

    /**
     * @private
     * Listener called when the value of the source code filed changed.
     * @param {Ext.form.field.Field} field The field
     * @param {Object} newValue The new value
     * @param {Object} oldValue The new value
     * @param {Object} eOpts The options object
     */
    _onSourceChange: function(field, newValue, oldValue, eOpts)
    {
        this._onUpdate();
    },
    
    /**
     * @private
     * This listener is called when the internal field state needs to be updated.
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     */
    _onUpdate: function(object)
    {
        // Invalidate the editor content and current char count.
        this._charCount = -1;
        
        var counter = Ext.get(this.getId() + '-counter-val'); 
        if (counter != null)
        {
            counter.parent().addCls(this.charCounterCountingCls);
            
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
     */
    _update: function()
    {
        // Cancel the running timer if necessary.
        if (this._counting != null)
        {
            window.clearTimeout(this._counting);
            this._counting = null;
        }
        
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
                this.markWarning("{{i18n PLUGINS_CORE_UI_FIELD_RICH_TEXT_HIERARCHY_ERROR}}");
            }
        }
        
        // Update the counter.
        this._updateCharCounter();
        
        var took2 = new Date().getTime();
        this._updateEvery = Math.max(took2 - took, 100);
    },
    
    /**
     * @private
     * Update the char counter under the editor.
     */
    _updateCharCounter: function()
    {
        if (this._charCount > -1)
        {
            var count = this._charCount;
            
            var counter = Ext.get(this.getId() + '-counter-val'); 
            if (counter != null)
            {
                counter.parent().removeCls(this.charCounterCountingCls);
                counter.setHtml("" + count);
                
                // is there a maxlength ?
                if (this._maxLength != Number.MAX_VALUE)
                {
                    if (count > this._maxLength)
                    {
                        counter.parent.addCls(this.charCounterMaxExceededCls);
                    }
                    else
                    {
                        counter.parent.removeCls(this.charCounterMaxExceededCls);
                    }
                }
            }
        }
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
    
    
    
    
    /**
     * @event editorsetcontent
     * Fires when the editor received new content. This allows to convert storing tags to internal tags.
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} object A tinymce object
     * @param {String} object.content The html content
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorSetContent: function (object)
    {
        this.fireEvent ('editorsetcontent', this, this.getEditor(), object);
    },
    
    /**
     * @event editorgetcontent
     * Fires when the editor received content. This allows to convert internal tags to storing tags.
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} object A tinymce object
     * @param {String} object.content The html content
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorGetContent: function (object)
    {
        this.fireEvent ('editorgetcontent', this, this.getEditor(), object);
    },
     
    /**
     * @event editorkeypress
     * Fires when the editor has a key press.
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} e The event 
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorKeyPress: function (object)
    {
        this.fireEvent ('editorkeypress', this, this.getEditor(), object);
    },
    
    /**
     * @event editorkeydown
     * Fires when the editor has a key down.
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} e The event 
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorKeyDown: function (object)
    {
        var editor = this.getEditor();
        
        // BUG FIX CMS 2499
        if (tinymce.isGecko || tinymce.isIE)
        {
            var rng, table, dom = editor.dom;
    
            // On gecko it's not possible to place the caret before a table
            if (object.keyCode == 37 || object.keyCode == 38) 
            {
                rng = editor.selection.getRng();
                table = dom.getParent(rng.startContainer, 'table');
    
                if (table && editor.getBody().firstChild != table) // DOING THE REVERSE CONDITION BECAUSE WE WANT TO DO SO ALL THE TIME 
                {
                    if (this._isAtStart(rng, table)) 
                    {
                        rng = dom.createRng();
    
                        rng.setStartBefore(table);
                        rng.setEndBefore(table);
    
                        editor.selection.setRng(rng);
                        
                        object.preventDefault();
                    }
                }
            }
        }   
        
        this.fireEvent ('editorkeydown', this, editor, object);
    },
    
    /**
     * @private
     * Is the given element a start ?
     * @param {Object} rng the browser's internal range object.
     * @param {HTMLElement} el the element to start from
     * @return true if the given element matches a start, false otherwise
     */
    _isAtStart: function(rng, el) 
    {
        var doc = el.ownerDocument, rng2 = doc.createRange(), elm;
    
        rng2.setStartBefore(el);
        rng2.setEnd(rng.endContainer, rng.endOffset);
    
        elm = doc.createElement('body');
        elm.appendChild(rng2.cloneContents());
    
        // Check for text characters of other elements that should be treated as content
        return elm.innerHTML.replace(/<(br|img|object|embed|input|textarea)[^>]*>/gi, '-').replace(/<[^>]+>/g, '').length == 0;
    },    
    
    /**
     * @event editorkeyup
     * Fires when the editor has a key up.
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} e The event 
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorKeyUp: function (object)
    {
        this.fireEvent ('editorkeyup', this, this.getEditor(), object);
    },
    
    /**
     * @event editorvisualaid
     * Fires when the editor pre process the serialization
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} object The object 
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorVisualAid: function (object)
    {
        this.fireEvent ('editorvisualaid', this, this.getEditor(), object);
    },
    
    /**
     * @event editorpreprocess
     * Fires when the editor pre process the serialization
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {Object} object The object 
     */
    /**
     * Listener on tinymce event to call extjs listeners
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _onEditorPreProcess: function (object)
    {
        this.fireEvent ('editorpreprocess', this, this.getEditor(), object);
    },  
    
    /**
     * @event editorhtmlnodeselected
     * Fires when a HTML node is selected in editor
     * @param {Ext.form.Field} field The editor field
     * @param {tinymce.Editor} editor The tinyMCE editor
     * @param {HTMLElement} node The HTML element selected
     */
    /**
     * This function is called when a node is selected in the rich text
     * @param {Object} object The tinymce content object. See tinymce doc to know more.
     * @private
     */
    _sendSelection: function(object)
    {
        if (this._currentMode == "source")
        {
            Ext.defer(this.fireEvent, 1, this , ['editorhtmlnodeselected', this, null]);
        }
        else
        {
            // Remember this selection for the current succession of events (click, mouseup, focus....)
            this.getEditor()._lastActiveNode = this.getNode();
            this._sendDelayedSelection();
        }
    },
    
    _sendDelayedSelection: function()
    {
        if (this.getEditor())
        {
            this.fireEvent('editorhtmlnodeselected', this, this.getEditor()._lastActiveNode);
        }
    }
});
