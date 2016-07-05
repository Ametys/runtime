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
 * This class is a configurable form panel that can contains tabs, fieldsets, repeaters and widgets. Configuration is made through XML or JSON requests.
 * The configuration format can be in JSON or XML.
 * The 2 steps to use this components are to call once and only once: 
 *  
 * 1) create the form (#configure) 
 * 2) fill the values (#setValues)
 */
Ext.define('Ametys.form.ConfigurableFormPanel', {
    extend: "Ext.form.Panel",
    
    statics: {
        /**
         * @property {Number} HORIZONTAL_PADDING_FIELDSET The left and right padding for fieldset
         * @private
         * @readonly 
         */
        HORIZONTAL_PADDING_FIELDSET: 5,
        /**
         * @property {Number} VERTICAL_PADDING_FIELDSET The top and bottom padding for fieldset
         * @private
         * @readonly 
         */
        VERTICAL_PADDING_FIELDSET: 5,
        
        /**
         * @property {Number} OFFSET_FIELDSET The offset for fieldset
         * @private
         * @readonly 
         */
        OFFSET_FIELDSET: 20,
        /**
         * @property {Number} PADDING_TAB The padding for tabs
         * @private
         * @readonly 
         */
        PADDING_TAB: 5,
        
        /**
         * @property {Number} LABEL_WIDTH The width for labels (at root nesting level)
         * @private
         * @readonly 
         */
        LABEL_WIDTH: 200,
        
        /**
         * @property {Number} FIELD_MINWIDTH The minimum width for fields
         * @private
         * @readonly 
         */
        FIELD_MINWIDTH: 150,
        
        /**
         * @private
         * @property {String} OUTOFTAB_FIELDSET_ID The id referencing the fields that do not belong to any fieldset 
         * @readonly
         */
        OUTOFTAB_FIELDSET_ID: 'out-of-tab-fieldset'
    },
    
    /**
     * @cfg {Number} maxNestedLevel=1 For layout purposes, this value is the number of nested repeaters/fieldsets.
     */
    maxNestedLevel: 1,
    
    /**
     * @cfg {String/String[]/Ext.XTemplate} tabErrorFieldsTpl
     * The template used to format the Array of warnings and errors fields passed to tab ToolTip into a single HTML
     * string. It renders each message as an item in an unordered list.
     */
    tabErrorFieldsTpl: [
        '<div class="a-configurable-form-panel-tooltip-status">',
        '<tpl if="errors && errors.length">',
            '<tpl if="errors.length == 1">',
                "<span class=\"a-configurable-form-panel-tooltip-status-error-label\">{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_ERROR_FIELD}}</span>",
            '</tpl>',
            '<tpl if="errors.length != 1">',
                "<span class=\"a-configurable-form-panel-tooltip-status-error-label\">{errors.length}{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_ERROR_FIELDS}}</span>",
            '</tpl>',
            '<ul class="error"><tpl for="errors"><li>{.}</li></tpl></ul>',
        '</tpl>',
        '<tpl if="warns && warns.length">',
            '<tpl if="warns.length == 1">',
                "<span class=\"a-configurable-form-panel-tooltip-status-warn-label\">{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_WARN_FIELD}}</span>",
            '</tpl>',
            '<tpl if="warns.length != 1">',
                "<span class=\"a-configurable-form-panel-tooltip-status-warn-label\">{warns.length}{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_WARN_FIELDS}}</span>",
            '</tpl>',
            '<ul class="warn"><tpl for="warns"><li>{.}</li></tpl></ul>',
        '</tpl>',
        '<tpl if="comments && comments.length">',
            '<tpl if="comments.length == 1">',
                "<span class=\"a-configurable-form-panel-tooltip-status-comment-label\">{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_COMMENT_FIELD}}</span>",
            '</tpl>',
            '<tpl if="comments.length != 1">',
                "<span class=\"a-configurable-form-panel-tooltip-status-comment-label\">{comments.length}{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_COMMENT_FIELDS}}</span>",
            '</tpl>',
            '<ul class="comment"><tpl for="comments"><li>{.}</li></tpl></ul>',
        '</tpl>',
        '</div>'
    ],
    
    /**
     * @cfg {boolean} showAmetysComments=false True to displays the comments of each fields and allow the user to add/edit the comments by clicking on the comments icon.
     */
    
    /**
     * @cfg {boolean} withTitleOnLabels=false True to wrap field's labels within a span with title as such <span title="My label">My label</span>. Useful if labels could be cut by CSS style.
     */
    
    /**
     * @cfg {Boolean/Function} autoFocus=true When form is ready and rendered, the first field will be focused. If it is a function, it should return a boolean.
     */
    autoFocus: true,
    
    /**
     * @cfg {Boolean} tableOfContents=false 
     * True to display a table of contents panel at the left of the form summarizing the first level fieldsets. 
     * This can only be applied with a form in linear mode. See #cfg-tab-policy-mode.
     */
    tableOfContents: false,
    
    /**
     * @property {Boolean} _showTableOfContents True if a table of contents is displayed. See #cfg-tableOfContents.
     */
    
    /**
     * @cfg {Object} defaultFieldConfig Default config to apply to all form fields
     */
    
    /**
     * @cfg {String} [tab-policy-mode] The display tab policy name (which has a higher priority than the userprefs value). Currently accepted values are 'default' or 'inline'.
     */
    
    /**
     * @cfg {String} testURL The url to use to run the verifications. see {@link Ametys.form.ConfigurableFormPanel.FieldCheckersManager#check}
     */
    
    /**
     * @private
     * @property {String} _testURL See #cfg-testURL.
     */
    
    /**
     * @property {String} _tabPolicy The current display tab policy name.
     * @private
     */
    
    /**
     * @property {Ametys.form.ConfigurableFormPanel.TableOfContents} _tableOfContents The table of contents instance attached to this form
     * @private
     */
    
    /**
    * @property {Ext.form.Field[]} _fields The configuration fields
    * @private
     */
    
    /**
     * @property {String[]} _initiallyNotNullFieldNames the names of the fields that are initially not null 
     * @private
     */

    /**
     * @property {Ext.panel.Panel/Ext.tab.Panel} _tabPanel The main panel or tabpanel depending on policy.
     * @private
     */

    /**
     * @private
     * @property {String[]} _notInFirstEditionPanels the ids list of the root panels (tabs or panel) that have been edited at least once. 
     * We consider a tab edited once when the focus has switched from one field of one of the tabs fieldsets to a different fieldset, of this tab or of another tab.
     */
    
    /**
     * @private
     * @property {Ametys.form.ConfigurableFormPanel.FieldCheckersManager} _fieldCheckersManager The field checkers manager instance of this form
     */
    
    /**
     * @private
     * @property {Ext.container.Container} _formContainer The container of this form
     */
    
    /** @cfg {Object} itemsLayout The layout to use in the container. Default to { type: 'anchor' }. */
    
    /** @cfg {Object} fieldsetLayout The layout to use in the nested fieldsets. Default to { type: 'anchor' }. */

    /** @cfg {Object} tabsLayout The layout to use in the tabs. Default to { type: 'anchor' }. */
    
    /** @cfg {Object} additionalWidgetsConfFromParams Additional configuration for every widget that will be created by this form. Each key of this object is the widget configuration name to add, and each corresponding value is the configuration name that will be read from the input configuration stream. */ 
    /**
     * @property {Object} _additionalWidgetsConfFromParams See #cfg-additionalWidgetsConfFromParams.
     */
    
    /** @cfg {Object} additionalWidgetsConf Additional configuration for every widget that will be created by this form. Each key of this object is the widget configuration name to add, and each corresponding value is the value of widget configuration. */ 
    /**
     * @property {Object} _additionalWidgetsConf See #cfg-additionalWidgetsConf.
     */
    
    /**
     * @cfg {String} labelAlign=right The label position. See Ext.form.Labelable#cfg-labelAlign
     */
    labelAlign: 'right',
    
    scrollable: true,
    layout: {
        type: 'anchor',
        reserveScrollbar: true
    },
    
    border: false,
    
    focusable: true,
    
    /**
     * @cfg {Number} tabIndex=0 DOM tabIndex attribute for the focused element
     */
    tabIndex: 0,
    
    /**
     * @cfg {String} defaultPathSeparator='.' The default separator for fields
     */
    defaultPathSeparator: '.',
    
    /**
     * @cfg {String} fieldNamePrefix='' The prefix to all submitted fields (should end with '.' if non empty)
     */
    fieldNamePrefix: '',
    
    /**
     * @cfg {Boolean} hideDisabledFields=false Set to true to hide the disabled fields
     */
    hideDisabledFields: false,
    
    /**
     * @private
     * @property {Boolean} _formReady indicates if the form is ready. The form is ready when all fields are rendered and have a value set.
     */
    _formReady: false,
    
    /**
     * @private
     * @property {Boolean} _addingRepeaterEntry indicates if a new repeater entry is currently being added.
     */
    _addingRepeaterEntry: false,
    
    /**
     * @private
     * @property {String} _focusFieldId The identifier of the lastly selected field that is still focused 
     */
    _focusFieldId: null,
    
    /**
     * @private
     * @property {String} _lastSelectedFieldId The identifier of the lastly selected field, regardless if it is focused or not
     */
    _lastSelectedFieldId: null,

    /**
     * @event inputblur
     * Fires when a field loses the focus
     * @param {Ext.form.Field} field The field
     */
    /**
     * @event inputfocus
     * Fires when a field received the focus
     * @param {Ext.form.Field} field The field
     */
    /**
     * @event htmlnodeselected
     * Fires when a HTML element is selected
     * @param {Ext.form.Field} field The field
     * @param {HTMLElement} node The selected HTML element
     */
    /**
     * @event formready
     * Fired after all fields have been drawn and values have been set.
     * This event should be also fired each time new fields are inserted (from a new repeater instance for example).
     * @param {Ext.form.Panel} form The form containing the fields
     */
    /**
     * @event repeaterEntryReady
     * Fired after all fields a new repeater entry has been inserted, which means that all its fields have been drawn and the repeater entry is initialized.
     * @param {Ametys.form.ConfigurableFormPanel.Repeater} repeater The repeater containing the entry.
     */
    /**
     * @event fieldchange
     * Fired when one of the fields changes after the form is ready
     * @param {Ext.form.Field} field The field that changed
     */
    /**
     * @event testresultschange
     * Fired when the results of the tests change
     * @param {Object} testResults the tests results
     */

    constructor: function (config)
    {
        var me = this;
        
        config = config || {};
        
        config.cls = Ext.Array.from(config.cls);
        config.cls.push("a-configurable-form-panel");
        config.cls.push("a-panel-spacing");
        
        // Display tab policy. The 'tab-policy-mode' configuration parameter passed
        // during the form edition panel instantiation has priority over the
        // userprefs value.
        this._tabPolicy = config['tab-policy-mode'] || Ametys.userprefs.UserPrefsDAO.getValue('edition-tab-policy') || 'default';
        this._showTableOfContents = config.tableOfContents === true && this._tabPolicy == 'inline';

    	config.dockedItems = Ext.Array.from(config.dockedItems);
    	config.dockedItems.push({
    		itemId: 'inline-toolbar',
    		dock: 'top',
    		
    		xtype: 'toolbar',
    		style: {
    			borderWidth: '0 0 1px 0 !important'
    		},
    		hidden: true,
    		
    		items:[
    		       {
    		    	   text: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_COLLAPSE_ALL}}",
    		    	   handler: function (btn) { 
    		    		   me._expandOrCollapseAllInlineTab(me._tabPanel, btn, true)
    		    	   }
    		       },
    		       {
    		    	   text: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_EXPAND_ALL}}",
    		    	   handler: function (btn) { 
    		    		   me._expandOrCollapseAllInlineTab(me._tabPanel, btn, false)
    		    	   }
    		       }
		       ]
    	});
        
    	this.defaultFieldConfig = config.defaultFieldConfig || {};
    	
    	if (this._showTableOfContents)
		{
            config = Ext.apply (config, {
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                scrollable: true // with a table of contents, the child items are scrollable vertically. at minwidth this scroll is horizontal
            })
		}
        
        this._additionalWidgetsConf = config.additionalWidgetsConf || {};
        this._additionalWidgetsConfFromParams = config.additionalWidgetsConfFromParams || {};
        this._testURL = config.testURL;
        
        config.items = this._getFormItems(config);
        config.autoFocus = false; 

        this.callParent(arguments);
        
        this._fields = [];  
        this._repeaters = [];
        this._notInFirstEditionPanels = [];
        this._initiallyNotNullFieldNames = [];
        	
        this.hideDisabledFields = config.hideDisabledFields === true;
        this._fieldCheckersManager = Ext.create('Ametys.form.ConfigurableFormPanel.FieldCheckersManager', {
            form: this,
            hideDisabledButtons: this.hideDisabledFields
        });

        // Message bus listeners.
        // 'tab-policy-mode' has priority over 'edition-tab-policy' userprefs.
        if (!config['tab-policy-mode']) 
        {
            Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onUserPrefsChanged, this);
        }
        
        this._fieldNamePrefix = config["fieldNamePrefix"] || '';
        
        this.showAmetysComments = config.showAmetysComments === true;
        
        this.on('afterrender', this._setFocusIfReady, this);
        this.on('resize', this._onResize, this);
        
        if (config.defaultPathSeparator)
        {
        	this.defaultPathSeparator = config.defaultPathSeparator;
        }
    },
    
    /**
     * Test if the form is ready, i.e. when all fields are rendered and have a value set.
     * @return {Boolean} `true` when the form is ready. 
     */
    isFormReady: function()
    {
        return this._formReady;
    },
    
    /**
     * Set an additional configuration for every widget that will be created by the form.
     * @param {String} name The name of additional configuration parameter
     * @param {Object} value The value of additional configuration parameter
     */
    setAdditionalWidgetsConf: function (name, value)
    {
    	this._additionalWidgetsConf[name] = value;
    },
    
    /**
     * @private
     * When a field is selected
     * @param {Ext.form.Field} field The field that has been selected (focused) or null if the last selected field blurred
     */
    _onFieldSelectedOrBlurred: function(field)
    {
        this._focusFieldId = field != null ? field.getId() : null;
    	if (field)
		{
    		this._handlePanelsEdition(field);
    		this._lastSelectedFieldId = field.getId(); 
		}        
    },
    
    /**
     * @private
     * When a richtext field is selected (a different html node is selected in it), focused or blurred
     * @param {Ext.form.Field} field The field that contains the HTML node
     * @param {HTMLElement} node The selected HTML node or null on focus/blur
     */
    _onRichTextFieldHTMLNodeSelected: function(field, node)
    {
    	this._onFieldSelectedOrBlurred(field);
    },
    
    /**
     * @private
     * Compare the selected field to the previously selected field in order to determine whether the previous tab (thumbnails mode) or previous
     * panel (linearized mode) is in first edition or not, and validate the fields/update the tabs status accordingly
     * @param {Ext.form.Field} field the newly focused field
     */
    _handlePanelsEdition: function(field)
    {
    	if (this._lastSelectedFieldId && field.getId() != this._lastSelectedFieldId)
		{
    		// The focus has switched
    		var previouslyFocusedField = this.getField(this._lastSelectedFieldId);
    		
    		// It might have been destroyed already (if contained in a deleted repeater item)
    		if (previouslyFocusedField)
			{
	    		var previousPanel = previouslyFocusedField.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
				var currentPanel = field.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
	            
				// The previous panel and/or the current panel can be null if it is outside of the thumbnails
				if (!previousPanel && currentPanel)
				{
					this._notInFirstEditionPanels.push(this.self.OUTOFTAB_FIELDSET_ID);
					
					// The previous panel is outside of the thumbnails 
					this._validateTabOrPanelFields(null);
				}
				else if (previousPanel && (!currentPanel || (previousPanel.id != currentPanel.id)))
				{
					// The focus has switched from one panel or from outside of the thumbnails to another panel 
					// => the previously selected panel is not in first edition mode anymore
					if (!Ext.Array.contains(this._notInFirstEditionPanels, previousPanel.id))
					{
						this._notInFirstEditionPanels.push(previousPanel.id);
					}
					
					this._validateTabOrPanelFields(previousPanel);
				}
			}
		}
    },
    
    // Inherited to unregister from the message bus
    destroy: function()
    {
        this.destroyComponents();
        Ametys.message.MessageBus.unAll(this);
        this.callParent(arguments);
    },
    
    // Inherited to return the disabled/hidden values as well
    getValues:  function(asString, dirtyOnly, includeEmptyText, useDataValues)
    {
    	var formValues = this.callParent(arguments);

		Ext.Array.each(this._fields, function(fieldName){
			var field = this.getField(fieldName);
			if (field.isDisabled() || field.isHidden())
			{
				var fieldName = field.name;
				formValues[fieldName] = field.getValue();
			}
		}, this);
		
		return formValues;
    },
    
    reset: function()
    {
    	// reset the fields
    	this.callParent(arguments);
    	
    	// Reset the warnings and the status of the field checkers
		this._fieldCheckersManager.reset();

		// Reset the repeaters
		Ext.Array.each(this._repeaters, function(repeaterId)
		{
			var repeaterPanel = Ext.getCmp(repeaterId);
			// repeater panel can already have been deleted if they were within a parent repeater item
			if (repeaterPanel)
			{
				repeaterPanel.reset();
			}
		});
    },
    
    isValid: function()
    {
    	var isFormValid = this.callParent();
    	var areAllRepeatersValid = true;
    	
    	// Are the repeaters of the form valid ?
		Ext.Array.each(this._repeaters, function(repeaterId)
		{
			var repeaterCt = Ext.getCmp(repeaterId);
			if (!repeaterCt.isValid())
			{
				areAllRepeatersValid = false;
			}
		});
    	
		// Force the update of the tabs status
		this._updateTabsStatus(true);
		
		return isFormValid && areAllRepeatersValid;
    },
    
    /**
     * Get the form container config to be used during its creation.
     * @protected
     */
    _getFormContainerCfg: function(config)
    {
        return {
            xtype: 'container',
            scrollable: false,
            border: false,
            
            layout: config.itemsLayout || { type: 'anchor' },
            
            items: config.items,
            
            // minWidth is a minWidth of a field + a number of repeaters margins
            minWidth: (this.defaultFieldConfig.labelWidth || Ametys.form.ConfigurableFormPanel.LABEL_WIDTH)
                + Ametys.form.ConfigurableFormPanel.FIELD_MINWIDTH
                + 20 // ametysDescription
                + (config.showAmetysComments ? 20 : 0)
                + (Ametys.form.ConfigurableFormPanel.Repeater.NESTED_OFFSET+1) * 3 // 3 level of repeaters
        };
    },
    
    /**
     * Get the table of contents config to be used during its creation.
     * @protected
     */
    _getTableOfContentsCfg: function ()
    {
        return {
            xtype: 'configurable-form-panel.toc',
            form: this,
            scrollable: 'vertical',
            flex: 0.2
        }
    },
    
    /**
     * @private
     * Listener on general resize
     * @param {Ametys.form.ConfigurableFormPanel} panel
     * @param {Number} width The panel width
     * @param {Number} height The panel height
     */
    _onResize: function(panel, width, height)
    {
        var toc = this._getTableOfContents();
        if (toc)
        {
            toc.setVisible(width >= this.getFormContainer().getInitialConfig().minWidth * 1.2);
        }
    },
    
    /**
     * @protected
     * Get the items of the form to be used during its creation.
     * @return {Object[]} the array of objects items of the form container 
     */
    _getFormItems: function(config)
    {
        if (this._showTableOfContents)
        {
            var hItems = [];
            
            hItems.push(this._getTableOfContentsCfg());
            hItems.push(Ext.apply(this._getFormContainerCfg(config), {
                flex: 0.8,
                scrollable: true
            }));
            
            return hItems;
        }
        else
        {
            return this._getFormContainerCfg(config);
        }
    },
       
    /**
     * Get the names of fields handle by the form panel
     * @return {String[]} The fields' names
     */
    getFieldNames: function ()
    {
        return this._fields;
    },
    
    /**
     * Get the prefix for field name
     * @return {String} The prefix
     */
    getFieldNamePrefix: function ()
    {
        return this._fieldNamePrefix;
    },
    
     /**
     * Get the form container in which the edition form must be drawn.
     * By default it is the first child item of this panel.
     * @protected
     */
    getFormContainer: function ()
    {
    	return this._showTableOfContents ? this.items.get(1) : this.items.get(0);
    },
    
    /**
     * Get the table of contents
     * @return {Ext.panel.Panel} The table of contents or null
     */
    _getTableOfContents: function ()
    {
        return this._showTableOfContents ? this.items.get(0) : null;
    },
    
    /**
     * Get the url used to run the checks on parameters
     * @return {String} The url
     */
    getTestURL: function ()
    {
        return this._testURL;
    },
    
    /**
     * Get a field in this form by id or name
     * @param {String} name The name (or id) of the searched field
     * @return {Ext.form.field.Field} The first matching field, or null if none was found.
     */
    getField: function (name)
    {
        return this.getForm().findField(name);
    },
    
    /**
     * Call Ext.form.field.Field.markInvalid on all fields of the form that are in error
     * @param {Object} fieldsInError The fields in error: the key is the name and the value is the error message.
     */
    markFieldsInvalid: function (fieldsInError)
    {
        for (var name in fieldsInError)
        {
            var fd = this.getForm().findField(name);
            if (fd)
            {
                fd.markInvalid (fieldsInError[name])
            }
        }
    },
    
    /**
     * Function to call when renaming a field of the form. Called by Ametys.form.ConfigurableFormPanel.Repeater
     * @param {Number/String} field The position or the name of the renamed field
     * @param {String} newName The new name
     * @private
     */
    _onRenameField : function (field, newName)
    {
        var pos = -1;
        
        if (Ext.isNumber(field))
        {
            pos = field;
        }
        else if (typeof field == 'string')
        {
            var pos = this._fields.indexOf(field);
        }
        
        if (pos >= 0 && pos < this._fields.length)
        {
            this._fields[pos] = newName;
        }
    },
    
    /**
     * Function to call when removing a field of the form. Called by Ametys.form.ConfigurableFormPanel.Repeater
     * @param {String} name The name of the removed field
     * @private
     */
    _onRemoveField: function (name)
    {
        var pos = this._fields.indexOf(name);
        if (pos >= 0)
        {
            this._fields.splice(pos, 1);
        }
    },
    
    /**
     * Destroy the all form items.
     * Call this if the form is not destroyed but you want to free its underlying children.
     */
    destroyComponents: function()
    {
        if (this.items && this.items.length > 0)
        {
            this.getFormContainer().removeAll();
            if (this._showTablesOfContents)
        	{
            	this.items.get(0).removeAll();
        	}
        }
    },
    
    /**
     * @private
     * Expand or collapse child items. Show a load mask.
     * @param {Ext.panel.Panel} tabpanel The panel containing items to collapse or expand
     * @param {Ext.Button} btn the clicked button to collapse or expand all
     * @param {Boolean} collapse true to collapse all tab' items or false to expand
     */
    _expandOrCollapseAllInlineTab: function (tabpanel, btn, collapse)
    {
        this.mask("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_WAIT_MSG}}");
        
        Ext.Function.defer(this._doExpandOrCollapseAllInlineTab, 100, this, [tabpanel, btn, collapse]);
    },
    
    /**
     * @private
     * Expand or collapse child items
     * @param {Ext.panel.Panel} tabpanel The panel containing items to collapse or expand
     * @param {Ext.Button} btn the clicked button to collapse or expand all
     * @param {Boolean} collapse true to collapse all tab' items or false to expand
     */
    _doExpandOrCollapseAllInlineTab : function (tabpanel, btn, collapse)
    {
        this.suspendLayouts();
        
        try
        {
            tabpanel.items.each (function (panel) {
                if (collapse)
                {
                    panel.collapse();
                }
                else
                {
                    panel.expand();
                }
            });
        }
        finally 
        {
            this.resumeLayouts(true);
            this.unmask();
        }
    },
    
    /**
     * @private
     * Creates a tab container
     * @return {Ext.tab.Panel} the tab container
     */
    _addTab: function ()
    {
        var tabPanel;
        
        var me = this;
        
        if (this._tabPolicy === 'inline')
        {
            tabPanel = Ext.create('Ext.panel.Panel', {
                cls: 'ametys-form-tab-inline',
                layout: this.initialConfig.tabsLayout || { type: 'anchor' },
            	
                border: false
            });
            
            this.getDockedComponent('inline-toolbar').show();
        }
        else
        {
            tabPanel = Ext.create('Ext.tab.Panel', {
                cls: 'ametys-form-tab',
                plain: true,
                bodyStyle: "border-width: 1px !important",
                margin: '15 0 0 0',
                layout: this.initialConfig.tabsLayout || { type: 'anchor' },

                deferredRender: false,
                listeners: {
                    'add': function(tabpanel, panel, index, eOpts) {
                        if (tabpanel.getActiveTab() == null)
                        {
                            tabpanel.setActiveTab(panel);
                        }
                    },
                    'tabchange': {fn: this._onTabChange, scope: this}
                }
            });
            
            this.getDockedComponent('inline-toolbar').hide();
        }
        
        this.getFormContainer().add(tabPanel);
        
        this._tabPanel = tabPanel; 
        return tabPanel;
    },
    
    getFocusEl: function()
    {
    	var focusEl = this.element || this.el;
    	Ext.Array.each(this._getFields(), function(field) {
    		if (field != null && field.focusable && Ext.isFunction(field.focus)
    				&& (!Ext.isFunction(field.isVisible) || field.isVisible()) 
        			&& (!Ext.isFunction(field.isDisabled) || !field.isDisabled()))
			{
    			focusEl = field;
    			return false;
			}
    	});
    	
    	return focusEl;
    },
    
    /**
     * @private
     * Focus the first field when the form is ready and rendered
     */
    _setFocusIfReady: function()
    {
    	if (this.rendered && this._formReady)
		{	
    		// Focus first field of the form
    		if (this._fields[0] != null)
    		{
                var me = this;
                
                var field = this.getField(this._fields[0]);
                
                function focusIfAutoFocus()
                {
                    if (Ext.isFunction(me.autoFocus) ? me.autoFocus() : me.autoFocus)
                    {
                         field.focus();
                    }
                }
                
                if (field.rendered)
                {
                    focusIfAutoFocus();
                }
                else
                {
                    field.on('render', focusIfAutoFocus, null, { single: true });
                }
    		}
    		
	    	this.on({
	    		'inputfocus':  Ext.bind(this._onFieldSelectedOrBlurred, this),
	    		'inputblur': Ext.bind(this._onFieldSelectedOrBlurred, this, []),
	    		'htmlnodeselected': Ext.bind(this._onRichTextFieldHTMLNodeSelected, this)
	    	});
		}
    },
    
    /**
     * @private
     * Function invoked when a new tab is selected
     * @param {Ext.tab.Panel} tabPanel the tab panel
     * @param {Ext.Component} newCard the newly activated item
     * @param {Ext.Component} oldCard the previously active item
     */
    _onTabChange: function(tabPanel, newCard, oldCard)
    {
    	if (oldCard != null)
		{
	    	// Focus the first field of the newly selected tab
	    	var fields = this._getFields(newCard != null ? newCard.getId() : this.self.OUTOFTAB_FIELDSET_ID);
	    	fields[0].focus();
		}
    },
    
    /**
     * Creates a tab item  with its label
     * @param {Ext.container.Container} ct The container where to add the tab item
     * @param {String} label The label of the tab item
     * @param {String} headerCls Custom CSS class name to apply to header
     * @return {Ext.panel.Panel} the tab item
     * @private
     */
    _addTabItem: function (ct, label, headerCls)
    {
        if (this._tabPolicy === 'inline')
        {
            var fieldset = Ext.create('Ext.panel.Panel', {
                title: label,
                
                bodyPadding: Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' 0 ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB,
                margin: '0 0 5 0',
                
                layout: this.initialConfig.tabsLayout || { type: 'anchor' },
                
                collapsible: true,
                titleCollapse: true,
                
                header: {
                    titlePosition: 1,
                    cls: headerCls
                },
                
                border: true,
                shadow: false,
                cls: 'ametys-form-tab-inline-item'
            });
            
            ct.add(fieldset);
            
            if (this._showTableOfContents)
        	{
            	// Add the tab item in the table of contents
            	this._getTableOfContents().addNavigationItem(label, fieldset.getId());
        	}
            
            return fieldset;
        }
        else
        {
            var tabitem = Ext.create('Ext.panel.Panel', {
                title: label,
                
                cls: 'ametys-form-tab-item',
                layout: this.initialConfig.itemsLayout || { type: 'anchor' },
                
                padding: Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' 0 ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB,
                
                header: {
                    cls: headerCls
                },

                border: false,
                scrollable: false
            });
            ct.add(tabitem);
            
            return tabitem;
        }
    },
    
    /**
     * @private
     * Validate tab when changing the focus to a different fieldset
     * @param {Ext.panel.Panel} previousPanel The panel just left, can be null if the panel if outside of the thumbnails
     */
    _validateTabOrPanelFields: function(previousPanel)
    {
    	// Validate the fields
    	var panelId = previousPanel != null ? previousPanel.getId() : this.self.OUTOFTAB_FIELDSET_ID;
        var fields = this._getFields(panelId);

        Ext.suspendLayouts();
        Ext.Array.each(fields, function(field)
        {
        	if (previousPanel != null || field.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]') == null)
    		{
        		// Trigger internal validation without firing validity change.
        		field.isValid();
    		}
        });

    	// Validate the repeaters
        var repeaters = this.getRepeaters(panelId);
        Ext.Array.each(repeaters, function(repeater)
        {
        	if (previousPanel != null || repeater.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]') == null)
    		{
        		// Trigger internal validation without firing validity change.
        		repeater.isValid();
    		}
        });
        
        // No tab status when outside of the thumbnails 
    	this._updateTabStatus(previousPanel);

    	Ext.resumeLayouts(true);
    },
    
    /**
     * @private
     * Get the list of fields in a container (any level) or all the fields
     * @param {String} componentId The id of the component to get the field from, can be null to get all the fields
     * @return {Ext.Component[]} An array of components which have the field mixin.
     */
    _getFields: function(componentId)
    {
    	var fields = [];
        
        // Function walking the component tree and adding fields to the array.
        var fieldWalker = function(component)
        {
            if (component.isFormField)
            {
                fields.push(component);
            }
            else if (component.isXType('container') && component.isVisible())
            {
                component.items.each(fieldWalker);
            }
        }
        
        if (componentId != this.self.OUTOFTAB_FIELDSET_ID)
    	{
        	var component = componentId != null ? Ext.getCmp(componentId) : this;
        	if (component.isFormField)
        	{
        		fields.push(component);
        	}
        	else if (component.items)
    		{
        		component.items.each(fieldWalker);
    		}
    	}
        else
    	{
        	this.getFormContainer().items.each(function(formItem) {
        		// Do not consider the fields within tabs
        		if (!formItem.hasCls('ametys-form-tab-inline') && !formItem.hasCls('ametys-form-tab'))
    			{
        			fields = Ext.Array.union(fields, this._getFields(formItem.getId()));
    			}
        	}, this);
    	}
        
        return fields;
    },
    
    /**
     * @private
     * Get the list of field checkers contained in the given tab or outside of the tabs (will return the global field checkers)
     * @param {String} componentId the id of the component to get the field checkers from, can be null to get all field checkers
     * @return {Ametys.form.ConfigurableFormPanel.FieldChecker[]} The array of field checkers contained in the panel, in the whole form, or outside the tabs
     */
    _getFieldCheckers: function(componentId)
    {
    	var fieldCheckers = [];
    	if (componentId != this.self.OUTOFTAB_FIELDSET_ID)
		{
    		var parentCmp = componentId != null ? Ext.getCmp(componentId) : this.getFormContainer();
    		if (parentCmp.items)
			{
    			var nestedContainers = parentCmp.query('container');
    			Ext.Array.each(nestedContainers, function(container){
    				if (container.hasCls('param-checker-container'))
    				{
    					fieldCheckers.push(container.down('button').fieldChecker);
    				}
    			});
			}
		}
    	else
		{
    		this.getFormContainer().items.each(function(formItem) {
        		// Do not consider the field checkers within tabs
        		if (!formItem.hasCls('ametys-form-tab-inline') && !formItem.hasCls('ametys-form-tab'))
    			{
        			fieldCheckers = Ext.Array.union(fieldCheckers, this._getFieldCheckers(formItem.getId()));
    			}
    		}, this);
		}
        
        return fieldCheckers;
    },
    
    /**
     * Get the list of repeaters in a container (any level) or the first level repeaters exclusively
     * @param {String} componentId The id of the component to get the repeaters from. Can be null to get all the repeaters of the form
     * @return {Ext.Component[]} An array of components which have the field mixin.
     */
    getRepeaters: function (componentId)
    {
    	var repeaters = [];
    	if (componentId != this.self.OUTOFTAB_FIELDSET_ID)
		{
    		var component = componentId != null ? Ext.getCmp(componentId) : this.getFormContainer();
    		if (component != null && !component .isFormField)
			{
    			repeaters = component.query('panel[isRepeater]');
			}
		}
    	else
		{
    		var repeaters = [];
    		this.getFormContainer().items.each(function(formItem) {
    			// Do not consider the repeaters within tabs
    			if (!formItem.hasCls('ametys-form-tab-inline') && !formItem.hasCls('ametys-form-tab'))
    			{
    				repeaters = Ext.Array.union(repeaters, this.getRepeaters(formItem.getId()));
    			}
    		}, this);
		}
    	
    	return repeaters;
    },
    
    /**
     * @private
     * Initialize the status of all tabs
     */
    _initializeTabsStatus: function()
    {
        if (this._tabPanel)
        {
            this.suspendLayouts();

        	this._tabPanel.items.each (function (item) {
    			
    			var header = item.tab ? item.tab : item.getHeader();
    			if (header != null && header.isHeader)
    			{
    				header.addCls(['empty']);
    			}
    		});
            
            this.resumeLayouts(true);
        }
    },
    
    
    /**
     * @private
     * Update the status of all tabs
     * @param {Boolean} force True to force the rendering of warning and errors 
     */
    _updateTabsStatus: function(force)
    {
        if (this._tabPanel)
        {
            this._tabPanel.items.each (function (item) {
                this._updateTabStatus (item, force);
            }, this);
        }
    },
    
    /**
     * @private
     * Update the tab status. Possibly the table of contents status as well
     * @param {Ext.panel.Panel} panel The panel (tab card or fieldset panel).
     * @param {Boolean} force True to force the rendering of warning and errors
     * @return true if the tab status has changed, false otherwise 
     */
    _updateTabStatus: function(panel, force)
    {
        // The header is the tab when in tab mode or the header in linear mode. 
        var header = null;
        if (panel)
    	{
        	header = panel.tab ? panel.tab : (panel.getHeader().isHeader ? panel.getHeader() : null);
    	}
        
        var hasHeaderChanged = false,
        	hasNavigationItemChanged = false;
        
        if (header != null || this._showTableOfContents)
        {
        	this.suspendLayouts();
        	
        	var panelId = panel != null ? panel.getId() : this.self.OUTOFTAB_FIELDSET_ID;
        	
            // Let's get all errors and warnings from the field checkers
            var fieldCheckers = this._getFieldCheckers(panelId),
                testsErrorMessages = [],
                testsWarnMessages = [];

            if (!Ext.isEmpty(fieldCheckers))
            {
                Ext.Array.each(fieldCheckers, function(fieldChecker){
                    var status = fieldChecker.getStatus();
                    if (status != Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_HIDDEN)
                    {
                        var label = fieldChecker.label;
                        if (status == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE) 
                        {
                            testsErrorMessages.push("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER}}" + " '" + label + "': " +  fieldChecker.getErrorMsg());
                        }
                        else if (status == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_WARNING)
                        {
                            testsWarnMessages.push("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER}}" + " '" + label + "': " +  "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_STATUS_WARNING}}");
                        }
                    }
                });
            }
            
            var fields = this._getFields(panelId);

            var errorFields = [];
            var warnFields = [];
            var commentFields = [];
            
            for (var i = 0; i < fields.length; i++)
            {
            	var field = fields[i];
                if (field.getActiveErrors().length > 0)
                {
                    errorFields.push("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD}}" +  " " + this._getFieldLabel(field, panel));
                }
                
                if (Ext.isFunction(field.hasActiveWarning) && field.hasActiveWarning())
                {
                    warnFields.push("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD}}" + " " + this._getFieldLabel(field, panel));
                }
                
                if (Ext.isFunction(field.getComments) && field.getComments().length > 0)
                {
                    var comment = field.getComments()[0];
                    var commentValue = Ext.String.format('<em>{0} ({1}, le {2})</em>', comment.text, comment.author, Ext.Date.format(comment.date, Ext.Date.patterns.FriendlyDateTime));
                             
                    commentFields.push(this._getFieldLabel(field, panel) + " : " + commentValue);
                }
            }
            
            // Invalidate repeaters
            var repeaters = this.getRepeaters(panel);
        	Ext.Array.each(repeaters, function(repeater){
        		if (repeater.getErrors().length > 0)
                {
                    errorFields.push(this._getRepeaterLabel(repeater, panel));
                }
        	});
            
            // var isActive = panel.tab ? panel.ownerCt.getActiveItem() == panel : true;
            // header[isActive ? 'addCls' : 'removeCls']('active');
            
            var firstEdition = !Ext.Array.contains(this._notInFirstEditionPanels, panelId);
            var navigationItem = null;
            if (this._showTableOfContents)
        	{
            	navigationItem = this._getTableOfContents().getNavigationItem(panelId);
        	}
            
            if (header && !firstEdition)
        	{
            	// When not in first edition mode, remove the startup class.
            	header.removeCls('startup');
            	header.addCls('not-startup');
        	}
            
            if (navigationItem && !firstEdition)
        	{
                // When not in first edition mode, remove the startup class.
            	navigationItem.removeCls('startup');
            	navigationItem.addCls('not-startup');
        	}
            
            var errors = Ext.Array.union(errorFields, testsErrorMessages);
            var warnings = Ext.Array.union(warnFields, testsWarnMessages);
            
            var hasError = errors.length > 0;
            var hasWarn = warnings.length > 0;
            var hasComment = commentFields.length > 0;
            
            if (force && (hasError || hasWarn || hasComment))
        	{
            	this._notInFirstEditionPanels.push(panelId);
            	
            	// Remove the startup class as well if there is something to report at startup
            	if (header)
        		{
            		header.addCls('not-startup');
            		header.removeCls('startup');
        		}
                
                if (navigationItem)
            	{
                	navigationItem.addCls('not-startup');
                	navigationItem.removeCls('startup');
            	}
        	}
            
            if (header)
        	{
            	var oldHeaderClassName = header.el.dom.className;
            	header.removeCls(['error', 'warning', 'comment']);
        	}
            
            if (navigationItem)
        	{
            	var oldNavigationItemClassName = navigationItem.el.dom.className;
            	navigationItem.removeCls(['error', 'warning', 'comment']);
        	}
            
            if (hasError)
            {
            	if (header)
        		{
            		header.addCls('error');
        		}

            	if (navigationItem)
            	{
                	navigationItem.addCls('error');
            	}
            }
            else if (hasWarn)
            {
            	if (header)
        		{
            		header.addCls('warning');
        		}

            	if (navigationItem)
            	{
                	navigationItem.addCls('warning');
            	}
            }
            else if (hasComment)
            {
            	if (header)
    			{
            		header.addCls('comment');
    			}

            	if (navigationItem)
            	{
                    navigationItem.addCls('comment');
            	}
            }
            
            if (panel)
        	{
            	hasHeaderChanged = header.el.dom.className != oldHeaderClassName;
            	if (hasHeaderChanged)
            	{
	            	if (header.rendered)
	            	{
	            		// As we change width with CSS we have to prevent tabs from overlapping one another
            			header.updateLayout();
            			this._createStatusTooltip (header.getEl(), panel, errors, warnings, commentFields);
        			}
	            	else
	            	{
	            		header.on ('afterrender', Ext.bind (this._createStatusTooltip, this, [header.getEl(), panel, errorFields, warnFields, commentFields], false), this, {single: true});
	            	}
            	}
        	}
            
            if (navigationItem)
        	{
            	hasNavigationItemChanged = navigationItem.el.dom.className != oldNavigationItemClassName;
            	if (hasNavigationItemChanged)
        		{
            		this._createStatusTooltip (navigationItem.getEl(), navigationItem, errors, warnings, commentFields);
        		}
        	}
            
	        this.resumeLayouts(hasHeaderChanged || hasNavigationItemChanged);
        }
    },
    
    /**
     * @private
     * Create tab tooltip
     * @param {Ext.dom.Element} el the element to whom bound the tooltip
     * @param {Ext.Component} cmp The component
     * @param {String[]} errors the list of errors
     * @param {String[]} warnings the list of warnings
     * @param {String[]} comments the list of comments
     */
    _createStatusTooltip : function (el, cmp, errors, warnings, comments)
    {
        Ext.tip.QuickTipManager.unregister(el);
        
        if (errors.length > 0 || warnings.length > 0 || comments.length > 0)
        {
            // Set the tooltip.
            var title = cmp.title;
            
            var text = this._getStatusTooltipText(errors, warnings, comments);
            
            Ext.tip.QuickTipManager.register({
                target: el.id,
                title: title,
                text: text,
                cls: ['x-fluent-tooltip', 'a-configurable-form-panel-tooltip'],
                width: 350,
                dismissDelay: 0
            });
        }
    },
    
    /**
     * @private
     * Get the tooltip message.
     * @param {String[]} errorFields The error fields' labels.
     * @param {String[]} warnFields The warning fields' labels.
     * @param {String[]} commentFields The commented fields' labels and their comment.
     * @return {String} the tooltip message markup.
     */
    _getStatusTooltipText: function(errorFields, warnFields, commentFields)
    {
        var html = '';
        if (errorFields.length > 0 || warnFields.length > 0 || commentFields.length > 0)
        {
            html += Ext.XTemplate.getTpl(this, 'tabErrorFieldsTpl').apply({
                errors: errorFields,
                warns: warnFields,
                comments: commentFields
            });
        }
        return html;
    },
    
    /**
     * @private
     * Get a field's label.
     * @param {Ext.form.Labelable} field A component with the Labelable mixin.
     * @param {Ext.Panel} tabpanel The panel containing the field.
     * @return {String} the tooltip message markup.
     */
    _getFieldLabel: function(field, tabpanel)
    {
        var label = '';
        if (tabpanel)
    	{
        	var ownerCt = field.ownerCt;
        	while (ownerCt != null && ownerCt.title && ownerCt.id != tabpanel.id)
        	{
        		label = ownerCt.title + " > " + label;
        		ownerCt = ownerCt.ownerCt;
        	}
    	}
        
        // Remove the starting or trailing '*' character
        var fieldLabel = field.getFieldLabel();
        if (Ext.String.startsWith(fieldLabel, '*'))
        {
            fieldLabel = fieldLabel.substr(1).trim();
        }
        else if (Ext.String.endsWith(fieldLabel, '*'))
        {
            fieldLabel = fieldLabel.substr(0, fieldLabel.length - 1).trim();
        }
        
        return label + fieldLabel;
    },
    
    /**
     * @private
     * Get the full label of a component of the form
     * @param {Ext.Component} cmp the component
     * @return the full label of the given component
     */
    _getFullLabel: function(cmp)
    {
    	var label = '';
    	if (cmp.isFieldLabelable)
    	{
        	// Remove the starting or trailing '*' character
    		label = cmp.getFieldLabel();
            if (Ext.String.startsWith(label, '*'))
            {
            	label = label.substr(1).trim();
            }
            else if (Ext.String.endsWith(label, '*'))
            {
            	label = label.substr(0, label.length - 1).trim();
            }
    	}
    	else if (cmp.isRepeater)
		{
    		label = cmp.getLabel();
		}
    	else
		{
    		label = cmp.title;
		}
    	
    	var ownerCt = cmp.ownerCt;
        while (ownerCt != null && (ownerCt.title || ownerCt.isRepeater))
        {
        	var ownerLabel = ownerCt.isRepeater ? ownerCt.getLabel() : ownerCt.title;
            label = ownerLabel + " > " + label;
            ownerCt = ownerCt.ownerCt;
        }
    	
        return label;
    },

    /**
     * @private
     * Get a repeater's label.
     * @param {Ametys.form.ConfigurableFormPanel.Repeater} repeater A repeater.
     * @param {Ext.Panel} tabpanel The panel containing the repeater.
     * @return {String} the label
     */
    _getRepeaterLabel: function (repeater, tabpanel)
    {
        var label = '';
        
        var ownerCt = repeater.ownerCt;
        while (ownerCt != null && ownerCt.title && ownerCt.id != tabpanel.id)
        {
            label = ownerCt.title + " > " + label;
            ownerCt = ownerCt.ownerCt;
        }
        
        var repeaterLabel = repeater.getLabel();
        
        return label + repeaterLabel;
    },
    
    /**
     * @private
     * Listens when a repeater validity changes.
     * @param {Ametys.form.ConfigurableFormPanel.Repeater} repeater The repeater.
     * @param {Boolean} isValid Whether or not the repeater is now valid.
     */
    _onRepeaterValidityChange: function (repeater, isValid)
    {
        if (this._formReady)
        {
             // Find the tab card (panel) to which belongs the field.
            var card = repeater.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
            if (card == null)
            {
                return;
            }
            
            // Update the tab status and tooltip.
            this._updateTabStatus(card);
        }
    },
    
    /**
     * @private
     * Listens when a field validity changes.
     * @param {Ext.form.field.Field} field The field.
     * @param {Boolean} isValid Whether or not the field is now valid.
     */
    _onFieldValidityChange: function(field, isValid)
    {
        if (this._formReady)
        {
             // Find the tab card (panel) to which belongs the field.
            var panel = field.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
            
            // Do not update the card status if the tab is still in first edition mode
            if (panel && !Ext.Array.contains(this._notInFirstEditionPanels, panel.id))
            {
                return;
            }
            
            // Update the tab status and tooltip.
            this._updateTabStatus(panel);
        }
    },
    
    /**
     * @private
     * Listens when the value of a field is changed
     * @param {Ext.form.field.Field} field The field.
     * @param {Boolean} newValue The new value
     */
    _onFieldChange: function(field, newValue)
    {
        if (!Ext.isEmpty(newValue))
        {
        	if (this._formReady)
        	{
        		this.fireEvent('fieldchange', field);
        	}

        	// Find the tab card (panel) to which belongs the field.
            var card = field.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
            if (card == null)
            {
                return;
            }

            var header = card.tab ? card.tab : card.getHeader();
            if (header != null && header.isHeader)
            {
                header.removeCls(['empty']);
            }
        }
    },
    
    /**
     * @private
     * Show or hide the elements of a fieldset, including the field checkers
     * @param {Ext.form.field.Checkbox} checkbox the checkbox 
     * @param {Ext.panel.Panel} fieldset the fieldset the group switch belongs to
     * @param {Boolean} startup true if this is the first call, false otherwise
     */
    _showHideFieldset: function(checkbox, fieldset, startup)
    {
        Ext.suspendLayouts();
        
        var checked = checkbox.getValue();

        fieldset.items.eachKey(function(key){
        	var fieldsetElement = Ext.getCmp(key);
        	
        	// do not show/hide the checkbox itself
        	if (fieldsetElement.getId() == checkbox.getId())
    		{
        		return;
    		}

        	fieldsetElement.setVisible(checked);
        	
            if (fieldsetElement.hasCls('param-checker-container'))
            {
                // field checker
                fieldsetElement.down('button').fieldChecker.setStatus(checked ? Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED
                                                                      : Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_HIDDEN);
            }
        	else if (!startup)
    		{
        		// validate fields on expanding/collapsing
        		// validation on hidden fields always returns true
                fieldsetElement.validate();
    		}
            
        });

        if (checked)
        {
            // always expand when the box is checked
            fieldset.expand();
        }
        
        if (!startup)
        {
            this._fieldCheckersManager.updateTestResults();
        }
        
        Ext.resumeLayouts(true);
    },
    
    /**
     * @private
     * Prevent click propagation to avoid collapsing/expanding the fieldset
     * @param {Ext.form.field.Checkbox} checkbox the checkbox 
     */
    _preventClickPropagation: function(checkbox)
    {
        checkbox.getEl().on('click', function(event) {event.stopPropagation();})
    },
    
    /**
     * Creates a fieldset with this label
     * @param {Ext.Element} ct The container where to add the fieldset
     * @param {String} label The label of the fieldset
     * @param {Number} nestingLevel The nesting level of the fieldset.
     * @param {Object} switcher If the group can be switched on/off, the configuration object corresponding to the group-switch parameter. A config for #_createInputField.
     * @return {Ext.form.FieldSet} The created fieldset
     * @private
     */
    _addFieldSet: function (ct, label, nestingLevel, switcher)
    {
        var me = this;
        var fdCfg = {
                style: '',
                nestingLevel: nestingLevel,
                ametysFieldSet: true,
                layout: this.initialConfig.fieldsetLayout || { type: 'anchor' },
                
                bodyPadding: Ametys.form.ConfigurableFormPanel.VERTICAL_PADDING_FIELDSET + ' ' + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET + ' ' + Ametys.form.ConfigurableFormPanel.VERTICAL_PADDING_FIELDSET + ' ' + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET,
                margin: '0 0 5 ' + (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : '0'),
                
                border: false,
                shadow: false
        };
        
        if (switcher != null)
        {
            var switcherCfg  = Ext.apply ({minWidth: 20, style: ' '}, switcher);
            
            var switcherField = this._createInputField(switcherCfg);
            switcherField.on('render', Ext.bind(this._preventClickPropagation, this));
            
            Ext.apply(fdCfg, {
                title: switcherCfg.label,
                ui: 'light',
                
                collapsible: true, 
                titleCollapse: true,
                hideCollapseTool: false,
                border: true,
                
                tools: [switcherField],
                
                header: {
                    titlePosition: 2,
                    
                    listeners: {
                        click: {
                            fn: function(header, event) {
                                if (!switcherField.checked) 
                                {
                                    // When the group is switched off, we cannot collapse/expand it
                                	event.stopEvent();
                                    return false;
                                }
                                else
                                {
                                    header.up('panel').toggleCollapse();
                                }
                            }
                        } 
                    }
                },
                
                listeners: {
                    add: {
                        fn: function(){
                            // every time something is added to this panel we want to hide it depending on the switch value
                            // hiding such element will make isValid work as we want
                            me._showHideFieldset(switcherField, this, true);
                        }
                    }
                }
            });
        }
        else if (label)
        {
            Ext.apply(fdCfg, {
                title : label,
                ui: 'light',
                
                collapsible: true,
                titleCollapse: true,
                hideCollapseTool: false,
                header: {
                    titlePosition: 1
                },
                
                border: true
            });
        }
        
        var fieldset = new Ext.panel.Panel(fdCfg);
        
        // we need to add the switchfield here as a regular field in order for the form to consider it as a field immediately
        // otherwise tools of the header are only added during rendering
        // The field will be moved from the fieldset to the head automatically since a component cannot be used twice
        fieldset.add(switcherField);
        
        if (switcherField != null)
        {
            switcherField.on('change', Ext.bind(this._showHideFieldset, this, [fieldset, false], 1));
        }
        ct.add(fieldset);
        
        return fieldset;
    },
    
    /**
     * Add an input field to the form
     * @param {Ext.Element} ct The container where to add the input
     * @param {Object} config The input configuration object:
     * @param {String} config.type The type of the field to create
     * @param {String} config.name The name of the field (the one used to submit the request)
     * @param {Object} config.value The value of the field at the creating time
     * @param {String} config.fieldLabel The label of the field
     * @param {String} config.ametysDescription The associated description
     * @param {String[]} config.enumeration The list of values if applyable (only for type text)
     * @param {String} config.widget The widget to use for edition. Can be null
     * @param {Boolean} config.mandatory True if the field can not be empty
     * @param {String} config.regexp The regexp to use to validate the field value
     * @param {String} config.invalidText The text to display when the field value is not valid
     * @param {String} config.disabled If true the field will be disabled
     * @param {Number/String} config.width Replace the default width with this one
     * @param {String} startVisible Optionally, if 'false' this field will be hidden 
     * @return {Ext.form.field.Field} The created field
     * @private
     */
    _addInputField: function (ct, config, startVisible)
    {
        var field = this._createInputField(config);

        if (startVisible == 'false')
        {
           field.hide();
        }
        
        if (field != null)
        {
            ct.add(field);
            this._fields.push(field.getName());
            
            field.on('validitychange', this._onFieldValidityChange, this);
            field.on('warningchange', this._onFieldValidityChange, this);
            field.on('commentsupdated', this._onFieldValidityChange, this);
            field.on('change', this._onFieldChange, this);
        }
        
        return field;
    },
    
    /**
     * Creates and returns an input field depending on the given configuration
     * @param {Object} config this object have the following keys:
     * @param {String} config.type The type of the field to create
     * @param {String} config.name The name of the field (the one used to submit the request)
     * @param {Object} config.value The value of the field at the creating time
     * @param {String} config.fieldLabel The label of the field
     * @param {String} config.ametysDescription The associated description
     * @param {String[]} config.enumeration The list of values if applyable (only for type text)
     * @param {String} config.widget The widget to use for edition. Can be null
     * @param {Boolean} config.mandatory True if the field can not be empty
     * @param {String} config.regexp The regexp to use to validate the field value
     * @param {String} [config.invalidText] The text to display when the field value is not valid
     * @param {String} config.disabled If true the field will be disabled
     * @param {Number/String} config.width Replace the default width with this one
     * @param {Number/String} config.minWidth Replace the default min width with this one
     * @param {String/Object} config.style Replace the default style with this one
     * @return {Ext.form.field.Field} The created field
     * @private
     */
    _createInputField: function (config)
    {
        var me = this;
        
        var offset = config.offset || 0;
        var roffset = config.roffset || 0;

        var fieldCfg = Ext.clone(this.defaultFieldConfig);
        Ext.applyIf (fieldCfg, {
            cls: 'ametys',
            style: config.style || 'margin-right:' + Math.max(this.maxNestedLevel * Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET - roffset, 0) + 'px',
            
            labelAlign: this.labelAlign,
            labelWidth: Ametys.form.ConfigurableFormPanel.LABEL_WIDTH - offset,
            labelSeparator: '',
            
            minWidth: config.minWidth || Ametys.form.ConfigurableFormPanel.LABEL_WIDTH - offset + Ametys.form.ConfigurableFormPanel.FIELD_MINWIDTH,
            anchor: '100%',
            
            allowBlank: !config.mandatory,
            regex: config.regexp ? new RegExp (config.regexp) : null,
            regexText: config.regexText || config.invalidText || "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_INVALID_REGEXP}}" + config.regexp,
            disabled: config.disabled,
            
            msgTarget: 'side',
            
            listeners: 
            {
                'focus': { fn: function (fd, e) { this.fireEvent ('inputfocus', fd, e)}, scope: this},
                'blur': { fn: function (fd, e) { this.fireEvent ('inputblur', fd, e)}, scope: this},
                'afterrender': { fn: this._onFieldAfterRender, scope: this}
            }
        });
        
        var widgetCfg = Ext.apply(config, fieldCfg);
        
        if (config.type.toLowerCase() == Ametys.form.WidgetManager.TYPE_RICH_TEXT)
        {
            widgetCfg.listeners['editorhtmlnodeselected'] = { fn: function (field, node) { this.fireEvent ('htmlnodeselected', field, node)}, scope: this};
        }
        
        var field = Ametys.form.WidgetManager.getWidget (config.widget, config.type.toLowerCase(), widgetCfg);
        
        // if field is disabled or not visible (group switch off) we return no errors
        field.getErrors = Ext.Function.createInterceptor(field.getErrors, function() { return this.isVisible() && !this.isDisabled(); }, null, []);
        return field;
    },
    
    /**
     * @private
     * Listener on fields afterrender event.
     * @param {Ext.form.field.Field} field The field that has been rendered.
     */
    _onFieldAfterRender: function(field)
    {
        // Add the click event listener on the comment exclamation mark.
        this._bindCommentClickListener(field);
    },
    
    /**
     * Add a repeater to the form
     * @param {Ext.Element} ct The container where to add the repeater
     * @param {Object} config The repeater configuration object. See Ametys.form.ConfigurableFormPanel.Repeater configuration.
     * @param {Number} initialSize The initial size
     * @return {Ametys.form.ConfigurableFormPanel.Repeater} The created repeater panel
     * @private
     */
    _addRepeater: function (ct, config, initialSize)
    {
        var repeater = this._createRepeater(config);
        
        this._repeaters.push(repeater.getId()); 
        
        // First instances
        var initialSize = initialSize || 0;
        if (repeater.getMinSize() > initialSize)
        {
            initialSize = repeater.getMinSize();
        }
        
        ct.add(repeater);
        
        // Add the initial items, expand the last one.
        for (var i = 0; i < initialSize; i++)
        {
            var collapsed = i < (initialSize-1);
            repeater.addRepeaterItem({animate: false, collapsed: collapsed});
        }
        
        repeater.on('validitychange', this._onRepeaterValidityChange, this);
        
        return repeater;
    },
    
    /**
     * Creates and returns a repeater panel from the configuration object
     * @param {Object} config The repeater configuration object. See {@link Ametys.form.ConfigurableFormPanel.Repeater} configuration.
     * @return {Ametys.form.ConfigurableFormPanel.Repeater} The created repeater panel
     * @private
     */
    _createRepeater: function (config)
    {
        var repeaterCfg = Ext.applyIf(config, {
            minSize: 0,
            maxSize: Number.MAX_VALUE,
            defaultPathSeparator: this.defaultPathSeparator,
            form: this
        });
        
        return Ext.create('Ametys.form.ConfigurableFormPanel.Repeater', repeaterCfg);
    },
    
    /**
     * Notify the form the a new repeater entry is being added
     * @param {Boolean} start True indicates that the process started , false indicates the it ended.  
     */
    notifyAddRepeaterEntry: function(start)
    {
        this._addingRepeaterEntry = start;
    },
    
    /**
     * @private
     * Test if an element is a HTMLElement or not 
     * @param {Object} o The object to test
     * @return {Boolean} true is o is an instance of HTMLElement
     */
    _isElement: function isElement(o) {
          return o instanceof HTMLElement ||
                (o && typeof o === "object" && o !== null && o.nodeType === 1 && typeof o.nodeName==="string");
    },
    
    /**
     * This function creates and add form elements from a definition
     * 
     * The JSON configuration format is
     * 
     *      {
     *          "<fieldName>": {
     *              "label":        "My field"
     *              "description":  "This describes what my field is made for"
     *              "type":         "STRING",
     *              "validation":   {
     *                  "mandatory":    true
     *              },
     *              multiple:       false
     *          },
     *          // ...
     *          "fieldsets": [
     *              {
     *                 role: "tabs"
     *                 label: "My first tab"
     *              }
     *          ],
     *          "field-checker": [{
     *          	"id": "my-checker-id",
     *              "label": "Label",
     *              "description": "Description",
     *          	"icon-glyph": "flaticon-data110",
     *              "linked-field": ['linked.field.1.path', 'linked.field.2.path'], // you can begin a linked field with the default separator (#cfg-defaultPathSeparator),
     *              													            // in order to use an absolute path instead of a relative path, see the
     *              											                    // second field checker below where we have defaultPathSeparator = "/"
     *              "order": "1" // used to sort the field checkers amongst themselves, the smaller the vertically higher
     *			}, 
     *			{ 
     *				"id": "my-other-checker-id",
     *              "label": "Other label",
     *              "description": "Other description",
     *          	"small-icon-path": "path for the small icon representing the test",
     *          	"medium-icon-path": "path for the medium icon representing the test",
     *          	"large-icon-path": "path for the large icon representing the test",
     *              "linked-field": ['/linked.field.1.path', '/linked.field.2.path'],
     *              "order": "10", // will be displayed below the first checker 
     *			}]
     *   	
     *      }
     * 
     * The **&lt;fieldName&gt;** is the form name of the field. (Note that you can prefix all field names using #cfg-fieldNamePrefix). See under for the reserved fieldName "fieldsets"
     * 
     * The string **label** is the readable name of your field that is visible to the user.
     * 
     * The string **description** is a sentence to help the user understand the field. It will appear in a tooltip on the right help mark.
     * 
     * The string **type** is the kind of value handled by the field. The supported types for metadata depend on the configuration of your Ametys.runtime.form.WidgetManager. Kernel provides widgets for the following types (case is not important):
     * BINARY, BOOLEAN, DATE, DATETIME, DOUBLE, FILE, GEOCODE, LONG, REFERENCE, RICH_TEXT, STRING, USER.
     * 
     * The **type** can also be **COMPOSITE** to create a fieldset around a few fields.      
     * 
     * If so, a **composition** field must recursively describe child elements.
     * There can also be a **field-checker** defined on the composite field (See below).
     * A composite field can also be a repeater of fields if it does have a **repeater** field.
     * A repeatable composite also needs the following fields:
     * 
     * - String **add-label**: The label to display on the add button
     * - String **del-label**: The label to display on the delete button
     * - String **headerLabel**: The label to display on the repeater itselft
     * - Number **minSize**: The optional minimum size of the repeater. For example 2 means it will at least be repeated twice. 0 if not specified.
     * - Number **maxSize**: The optional maximum size of the repeater. Default value is infinite.
     * - Number **initial-size**: The optional size when loading the form (must be between minSize and maxSize). minSize is the default value.
     * 
     * The object **validation** field is a field validator.
     * Can be an object with the optional properties 
     * 
     * - boolean **mandatory** to true, to check the field is not empty AND add a '*' at its label.
     * - string **invalidText** : a general text error if the field is not valid
     * - string **regexp** : a regular expression that will be checked
     * - string **regexText** : the text error if the regexp is not checked
     * 
     * The object **enumeration** is an array to list available values. Note that types and widgets that can be used with enumeration is quite limited.
     * Each item of the array is an object with **value** and **label**.
     * Exemple: enumeration: [{value: 1, label: "One"}, {value: 2, label: "Two"}]
     * 
     * The object **default-value** field is the default value for the field if not set with #setValues.
     * 
     * The boolean **multiple** specify if the user can enter several values in the field. Types and widgets that support multiple data is quite limited.
     * 
     * The string **widget** specify the widget to use. This is optional to use the default widget for the given type, multiple and enumeration values.
     * The widgets are selected using the js class {@link Ametys.form.WidgetManager} and the extension point org.ametys.runtime.ui.widgets.WidgetsManager.
     * Note that you can transmit additional configuration to all widgets using #cfg-additionalWidgetsConfFromParams 
     * 
     * The optional object **widget-params** will be transmitted to the widget configuration : values depends on the widget you did select.
     * 
     * The boolean **hidden** will hide this field.
     * 
     * The boolean **can-not-write** makes the field in read only mode.
     * 
     * The object **annotations** is an array of object to describe available XML annotations on a richtext.
     * Each item is an object with properties : **name** (the XML tagname), **label** (the label of the button to set this annotation, defaults to name) and **description** (the help text associated to the button).
     * Exemple: annotations: [ { name: "JUSTICE", label: "Justice term", description: "Use this button to annotate the selected text as a justice term" } ] 
     * 
     * The object **disableCondition** can be defined in order to disable/enable the current parameter. It has the following configuration that must be written in JSON:
     *
     * - Object **conditions** conditions that can contain several condition objects or other conditions
     *   - Object **conditions** recursively describe sub conditions groups.
     *   - Object[] **condition** Object describing a unit condition (see under).
     *   - String **type** the type of the underlying conditions. Can be set to "and" (default value) or "or".
     * 
     * The Object **condition** has the following attributes:
     * 
     * - String **id** the id of the field that will be evaluated
     * - String **operator** the operator used to evaluated the field. Can be **eq**, **neq**, **gt**, **geq**, **leq** or **lt**         
     * - String **value** the value with which the field will be compared to
     *
     *
     * The object **field-checker** can be used in order to check the value of certain parameters. It must contain the following attributes: 
     *
     *  - String **id** The id of the parameter checker. 
     *  - String **large-icon-path** The relative path to the 48x48 icon representing the test
     *  - String **medium-icon-path** The relative path to the 32x32 icon representing the test
     *  - String **small-icon-path** The relative path to the 16x16 icon representing the test
     *  - String **icon-glyph** the glyph used for the icon representing the test
     *  - String **icon-decorator** the decorator to use on the icon
     *  - String[] **linked-fields** the absolute or relative paths of the linked field (fields used to run the check). Always JSON-encoded even for XML configurations.
     *  - String **label** The label of the parameter checker
     *  - String **description** The description of the parameter checker
     *  - String **order** The displaying order (from top to bottom). Indeed, several parameter checkers can be defined on a given tab, fieldset or parameter.
     *  
     *  Here is a full example of a field checker definition (example for data in XML, see above for a JSON example):
     *
     *      <field-checker>
     *          <id>checker-id</id>
     *          <icon-glyph>flaticon-data110</icon-glyph>
     *          <icon-decorator></icon-decorator>
     *          <small-icon-path>small/icon/path/img_16.png</small-icon-path>
     *          <medium-icon-path>medium/icon/path/img_32.png</small-icon-path>
     *          <large-icon-path>large/icon/path/img_48.png</large-icon-path>
     *          <linked-fields>["linked.field.path"]</linked-fields>
     *          <label>Checker label</label>
     *          <description>Checker description</description>
     *          <order>1</order>
     *      </field-checker>
     *
     * The **fieldname "fieldsets"** is a reserved keyword (if type is not specified) to create a graphical grouping of fields. This only works at root of data. 
     * Its value is an array of configuration with attributes:
     * 
     * - String **role** Can be "tabs" or "fieldsets" to create a tab grouping or a fieldset grouping. Note that tab grouping can be replaced by simple panels according to a user preference.
     * - String **label** The label of the grouping.
     * - Object **switcher** The configuration of the switcher field. The switcher is a boolean field that is used to enable/disable the other fields of its fieldset (available for fieldsets that have a "fieldset" role exclusively).
     *   It must have a **label** and an **id** (the id of the boolean field) and optionally a **default-value**.
     * - Object **field-checker** (see above) Fieldsets can also have a parameter checker for both "tab" and "fieldset" role
     * - Object **elements** The child elements of the grouping : this is a recursive data object, except that **"fieldsets"** can not be used again with the role "tab".
     * 
     * You can define a **field-checker** directly under the data node/object, in which case the checker will be global and displayed at the bottom of 
     * the main form container. See the example at the top of this documentation.
     *
     * @param {Object/HTMLElement} data The data to create the form structure. Can be a JSON object or an XML HTMLElement.
     */
    configure: function(data)
    {
        if (this._isElement(data))
        {
            this._configureXML(data);
        }
        else
        {
            this._configureJSON(data);
        }
        
        this._initializeDisableConditions();
        this._fieldCheckersManager.initializeFieldCheckers();
    },
    
    /**
     * @private
     * This function creates and add form elements from a JSON definition
     * @param {Object} data The JSON definition of the form fields.
     * @param {String} prefix The input prefix to concatenate to input name
     * @param {Ext.Element} [ct=this] The container where to add the repeater
     * @param {Number} [offset=0] The field offset.
     * @param {Number} [roffset=0] The field right offset.
     */
    _configureJSON: function (data, prefix, ct, offset, roffset)
    {
        prefix = prefix || this.getFieldNamePrefix();
        ct = ct || this.getFormContainer();
        offset = offset || 0;
        roffset = roffset || 0;
        
        var tabs = [];
        
        for (var name in data)
        {
            var type = data[name].type ? data[name].type.toLowerCase() : null;
            
            // Compute the nesting level
            var parentLevelPanel = ct.up('panel[nestingLevel]');
            var nestingLevel = 1;
            if (ct.nestingLevel)
            {
                nestingLevel = ct.nestingLevel + 1;
            }
            else if (parentLevelPanel)
            {
                nestingLevel = parentLevelPanel.nestingLevel + 1;
            }
            
            if (!type && name == 'fieldsets')
            {
                var fieldsets = data[name];
                for (var i=0; i < fieldsets.length; i++)
                {
                    if (fieldsets[i].role == 'fieldset')
                    {
                        var elements = fieldsets[i].elements;
                        
                        var switcherCfg = fieldsets[i].switcher;
                        if (switcherCfg != null)
                        {
                            switcherCfg.name = switcherCfg.id;
                            delete switcherCfg.id;
                            switcherCfg.type = 'boolean';
                        }
                        
                        var fieldset = this._addFieldSet(ct, fieldsets[i].label, nestingLevel, switcherCfg);
                        
                        // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
                        var finalOffset = offset 
                                        + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET
                                        + (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
                                        + 1;
                        var finalROffset = roffset 
                                        + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET
                                        + 1;
                        
                        this._configureJSON(elements, prefix, fieldset, finalOffset, finalROffset);
                        
                        var fieldCheckers = fieldsets[i]['field-checker'];
                        if (!Ext.isEmpty(fieldCheckers))
                        {
                        	this._fieldCheckersManager.addFieldCheckers(fieldset, fieldCheckers, prefix, finalOffset, finalROffset);
                        }
                    }
                    else // role = tab
                    {
                        tabs.push(fieldsets[i]);
                    }
                }
            }
            else if (!type && name == 'field-checker' && nestingLevel == 1)
        	{
            	// Global field checker
            	 var fieldCheckers = data[name];
                 if (!Ext.isEmpty(fieldCheckers))
                 {
                	 this._fieldCheckersManager.addFieldCheckers(ct, fieldCheckers, prefix, offset, roffset);
                 }
        	}
            else if (type == 'composite')
            {
                if (!data[name].repeater)
                {
                    var fieldset = this._addFieldSet(ct, data[name].label, nestingLevel);
                    // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
                    var finalOffset = offset 
                                    + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET 
                                    + (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
                                    + 1; 
                    var finalROffset = roffset 
                                    + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET
                                    + 1;
                    this._configureJSON(data[name].composition, prefix + name + this.defaultPathSeparator, fieldset, finalOffset, finalROffset);
                    
                    var fieldCheckers = data[name]['field-checker'];
                    if (!Ext.isEmpty(fieldCheckers))
                    {
                    	this._fieldCheckersManager.addFieldCheckers(fieldset, fieldCheckers, prefix + name + this.defaultPathSeparator, finalOffset, finalROffset);
                    }
                }
                else
                {
                    var repeater = data[name].repeater;
                    
                    var repeaterCfg = {
                        prefix: prefix,
                        name: name,
                        
                        label: data[name].label,
                        description: data[name].description,
                        
                        addLabel: repeater['add-label'],
                        delLabel: repeater['del-label'],
                        headerLabel: repeater['header-label'],
                        
                        minSize: repeater['min-size'] || 0,
                        maxSize: repeater['max-size'] || Number.MAX_VALUE,
                        
                        composition: repeater.composition,
                        
                        fieldCheckers: repeater['field-checker'],
                        
                        nestingLevel: nestingLevel,
                        offset: offset,
                        roffset: roffset
                    }
                        
                    this._addRepeater (ct, repeaterCfg, repeater['initial-size'] || 0);
                }
            }
            else
            {
                var fieldData = data[name];
                
                var label = fieldData.label;
                var description = fieldData.description;
                var isMandatory = fieldData.validation ? (fieldData.validation.mandatory) || false : false;
                
                var labelWithMandatory = this.labelAlign == 'top' ? label + (isMandatory ? ' *' : '') : (isMandatory ? '* ' : '') + label;
                
                var widgetCfg = {
                    name: prefix + name,
                    shortName: name,
                    type: type,
                    
                    fieldLabel: this.withTitleOnLabels ? '<span title="' + label + '">' + labelWithMandatory + '</span>' : labelWithMandatory, // FIXME Runtime-1465
                    ametysDescription: description || '',
                    showAmetysComments: this.showAmetysComments,
                    
                    mandatory: isMandatory,
                    value: fieldData['default-value'],
                    
                    multiple: fieldData.multiple,
                    widget: fieldData.widget,
                    
                    hidden: fieldData.hidden, 
                    disabled: fieldData['can-not-write'] === true,
                    
                    disableCondition: fieldData.disableCondition,
                    
                    form: this,
                    offset: offset,
                    roffset: roffset
                };

                // Add configured configuration
                Ext.Object.each(this._additionalWidgetsConfFromParams, function(key, value, object) {
                    widgetCfg[key] = fieldData[value];
                });
                Ext.Object.each(this._additionalWidgetsConf, function(key, value, object) {
                    widgetCfg[key] = value;
                });
                
                if (fieldData.validation)
                {
                    var validation = fieldData.validation;
                    widgetCfg.regexp = validation.regexp || null;
                    
                    if (validation.invalidText)
                    {
                        widgetCfg.invalidText = validation.invalidText;
                    }
                    if (validation.regexText)
                    {
                        widgetCfg.regexText = validation.regexText;
                    }
                }
                
                if (fieldData.enumeration)
                {
                    var enumeration = [];
                    
                    var entries = fieldData.enumeration;
                    for (var j=0; j < entries.length; j++)
                    {
                        enumeration.push([entries[j].value, entries[j].label]);
                    }
                    
                    widgetCfg.enumeration = enumeration;
                }
                
                if (fieldData['widget-params'])
                {
                    widgetCfg = Ext.merge (widgetCfg, fieldData['widget-params']);
                }
                
                if (fieldData.annotations)
                {
                    var annotations = [];
                    
                    var entries = fieldData.annotations;
                    for (var j=0; j < entries.length; j++)
                    {
                    	annotations.push({
                            name: entries[j].name,
                            label: entries[j].label || entries[j].name,
                            description: entries[j].description || entries[j].label || entries[j].name
                        });
                    }
                    
                    widgetCfg.annotations = annotations;
                }
                
                var field = this._addInputField(ct, widgetCfg);
                
                var fieldCheckers = fieldData['field-checker'];
                if (!Ext.isEmpty(fieldCheckers))
                {
                	this._fieldCheckersManager.addFieldCheckers(ct, fieldCheckers, prefix, offset, roffset, field);
                }
            }
        }
        
        if (tabs.length > 0)
        {
            var tabPanel = this._addTab();

            for (var i=0; i < tabs.length; i++)
            {
                var tab = this._addTabItem (tabPanel, tabs[i].label || "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_UNTITLED}}");
                
                // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
                var finalOffset = offset 
                                + Ametys.form.ConfigurableFormPanel.PADDING_TAB 
                                + 1; 
                var finalROffset = roffset 
                                + Ametys.form.ConfigurableFormPanel.PADDING_TAB 
                                + 1; 
                this._configureJSON (tabs[i].elements, prefix, tab, finalOffset, finalROffset);
            
                // Add the field checker at the end of the current tab
                var fieldCheckers = tabs[i]['field-checker'];
                if (!Ext.isEmpty(fieldCheckers))
                {
                	this._fieldCheckersManager.addFieldCheckers(tab, fieldCheckers, prefix, finalOffset, finalROffset);
                }
            }
        }
    },
    
    /**
     * @private
     * This function creates and add form elements from a XML definition
     * @param {HTMLElement} data The XML definition of the form fields.
     * @param {String} prefix The input prefix to concatenate to input name
     * @param {Ext.Element} [ct=this] The container where to add the repeater
     * @param {Number} [offset=0] The field offset.
     * @param {Number} [roffset=0] The field right offset.
     */
    _configureXML: function (data, prefix, ct, offset, roffset)
    {
        prefix = prefix || this.getFieldNamePrefix();
        ct = ct || this.getFormContainer();
        offset = offset || 0;
        roffset = roffset || 0;
        
        var nodes = Ext.dom.Query.selectDirectElements(null, data);
        
        var tabs = [];
        
        for (var i=0; i < nodes.length; i++)
        {
            var name = nodes[i].tagName;
            
            if (nodes[i].childNodes.length == 1 && nodes[i].childNodes[0].nodeType != 1) // fieldset label
            {
                continue;
            }
            
            var type = Ext.dom.Query.selectValue("> type", nodes[i], '').toLowerCase();
            var label = Ext.dom.Query.selectValue("> label", nodes[i], name);
            var description = Ext.dom.Query.selectValue('> description', nodes[i], '');
            
            // Compute the nesting level
            var parentLevelPanel = ct.up('panel[nestingLevel]');
            var nestingLevel = 1;
            if (ct.nestingLevel)
            {
                nestingLevel = ct.nestingLevel + 1;
            }
            else if (parentLevelPanel)
            {
                nestingLevel = parentLevelPanel.nestingLevel + 1;
            }
            
            if (type == '' && name == 'fieldsets')
            {
                var role = nodes[i].getAttribute('role');
                if (role == 'fieldset')
                {
                    var elements = Ext.dom.Query.selectNode('> elements', nodes[i]);
                    
                    var switcher = Ext.dom.Query.selectNode('> switcher', nodes[i]);
                    
                    if (switcher != null)
                    {   
                        var switcherCfg = {};
                        
                        // Create an object representing the switcher
                        switcherCfg.name = Ext.dom.Query.selectValue('> id', switcher);
                        switcherCfg.label = Ext.dom.Query.selectValue('> label', switcher);
                        switcherCfg.defaultValue = Ext.dom.Query.selectValue('> default-value', switcher);
                        switcherCfg.type = 'boolean';
                    }
                    
                    var fieldset = this._addFieldSet(ct, label, nestingLevel, switcher != null ? switcherCfg : null);

                    // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
                    var finalOffset = offset 
                                    + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET 
                                    + (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
                                    + 1; 
                    var finalROffset = roffset 
                                    + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET 
                                    + 1; 
                    
                    if (elements)
                    {
                        this._configureXML (elements, prefix, fieldset, finalOffset, finalROffset);
                    }
                    
                    var fieldCheckers = Ext.dom.Query.select('> field-checker', nodes[i]);
                    if (!Ext.isEmpty(fieldCheckers))
                    {
                    	this._fieldCheckersManager.addFieldCheckers(fieldset, fieldCheckers, prefix, finalOffset, finalROffset);
                    }
                }
                else // role = tab
                {
                    tabs.push(nodes[i]);
                }
            }
            else if (!type && name == 'field-checker' && nestingLevel == 1)
        	{
            	 // Global field checker
                 this._fieldCheckersManager.addFieldCheckers(ct, data[name], prefix, offset, roffset);
        	}
            else if (type == 'composite')
            {
                var repeaterNode = Ext.dom.Query.selectNode('> repeater', nodes[i]);
                if (repeaterNode != null)
                {
                    var repeaterCfg = {
                        prefix: prefix,
                        name: name,
                        
                        label: label,
                        description: description,
                        
                        addLabel: Ext.dom.Query.selectValue('> add-label', repeaterNode),
                        delLabel: Ext.dom.Query.selectValue('> del-label', repeaterNode),
                        headerLabel: Ext.dom.Query.selectValue('> header-label', repeaterNode, ''),
                        
                        minSize: Ext.dom.Query.selectNumber('@min-size', repeaterNode, 0),
                        maxSize: Ext.dom.Query.selectNumber('@max-size', repeaterNode, Number.MAX_VALUE),
                        
                        compositionNode: Ext.dom.Query.selectNode('> composition', repeaterNode),
                        fieldCheckers: Ext.dom.Query.selectNode('> field-checker', repeaterNode),
                        	
                        nestingLevel: nestingLevel,
                        offset: offset,
                        roffset: roffset
                    }
                    
                    this._addRepeater (ct, repeaterCfg, Ext.dom.Query.selectNumber('@initial-size', repeaterNode));
                }
                else
                {
                    var fieldset = this._addFieldSet(ct, label, nestingLevel);
                    // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
                    var finalOffset = offset 
                                    + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET 
                                    + (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
                                    + 1; 
                    var finalROffset = roffset 
                                    + Ametys.form.ConfigurableFormPanel.HORIZONTAL_PADDING_FIELDSET 
                                    + 1; 
                    this._configureXML(Ext.dom.Query.select("> composition", nodes[i]), prefix + name + this.defaultPathSeparator, fieldset, finalOffset, finalROffset);
                    
                    var fieldCheckers = Ext.dom.Query.select('> field-checker', nodes[i]);
                    if (!Ext.isEmpty(fieldCheckers))
                    {
                        this._fieldCheckersManager.addFieldCheckers(fieldset, fieldCheckers, prefix + name + this.defaultPathSeparator, finalOffset, finalROffset);
                    }
                }
            }
            else if (type != '')
            {
                var isMandatory = Ext.dom.Query.selectValue("> validation > mandatory", nodes[i]) == 'true';
                
                var widgetCfg = {
                    name: prefix + name,
                    type: Ext.dom.Query.selectValue("> type", nodes[i]),
                    
                    fieldLabel: (isMandatory ? '* ' : '') + label,
                    ametysDescription: Ext.dom.Query.selectValue("> description", nodes[i], ''),
                    showAmetysComments: this.showAmetysComments,
                    
                    value: Ext.dom.Query.selectValue("> default-value", nodes[i], ''),
                    
                    mandatory: isMandatory,
                    regexp: Ext.dom.Query.selectValue("> validation > regexp", nodes[i], null),
                    
                    multiple: Ext.dom.Query.selectValue("> multiple", nodes[i]) == 'true',
                    widget: Ext.dom.Query.selectValue("> widget", nodes[i], null),
                    disabled: Ext.dom.Query.selectValue("> can-not-write", nodes[i]) == 'true',
                    
                    disableCondition: Ext.dom.Query.selectValue("> disable-conditions", nodes[i], null),
                    
                    form: this,
                    offset: offset,
                    roffset: roffset
                };
                
                // Add configured configuration
                Ext.Object.each(this._additionalWidgetsConfFromParams, function(key, value, object) {
                    widgetCfg[key] = Ext.dom.Query.selectValue('> ' + value, nodes[i]);
                });

                Ext.Object.each(this._additionalWidgetsConf, function(key, value, object) {
                    widgetCfg[key] = value;
                });

                
                var invalidText = Ext.dom.Query.selectValue('> validation > invalidText', nodes[i], null);
                if (invalidText != null)
                {
                    widgetCfg.invalidText = invalidText;
                }
                
                var regexText = Ext.dom.Query.selectValue('> validation > regexText', nodes[i], null);
                if (regexText != null)
                {
                    widgetCfg.regexText = regexText;
                }
                
                var enumeration = [];
                var enumerationValues = Ext.dom.Query.selectNode("enumeration", nodes[i]);
                if (enumerationValues != undefined)
                {
                    var entries = Ext.dom.Query.selectDirectElements("*", enumerationValues);
                    for (var j=0; j < entries.length; j++)
                    {
                        enumeration.push([entries[j].getAttribute("value"), Ext.dom.Query.selectValue("", entries[j])]);
                    }
                }
                
                if (enumeration.length > 0)
                {
                    widgetCfg.enumeration = enumeration;
                }
                
                var widgetParamNodes = Ext.dom.Query.select('> widget-params > *', nodes[i]);
                for (var j=0; j < widgetParamNodes.length; j++)
                {
                    widgetCfg[widgetParamNodes[j].tagName] = Ext.dom.Query.selectValue('', widgetParamNodes[j]);
                }
                
                var annotationNodes = Ext.dom.Query.select('> annotations > *', nodes[i]);
                var annotations = [];
                for (var j=0; j < annotationNodes.length; j++)
                {
                    var aName = annotationNodes[j].getAttribute("name");
                    annotations.push({
                        name: aName,
                        label: Ext.dom.Query.selectValue('> label', annotationNodes[j], aName),
                        description: Ext.dom.Query.selectValue('> description', annotationNodes[j], aName)
                    });
                }
                
                if (annotations.length > 0)
                {
                    widgetCfg['annotations'] = annotations;
                }
                
                var field = this._addInputField(ct, widgetCfg);
                
                var fieldCheckers = Ext.dom.Query.select('> field-checker', nodes[i]);
                if (!Ext.isEmpty(fieldCheckers))
                {
                    this._fieldCheckersManager.addFieldCheckers(ct, fieldCheckers, prefix, offset, roffset, field);
                }
            }
        }
        
        if (tabs.length > 0)
        {
            var tabPanel = this._addTab();

            for (var i=0; i < tabs.length; i++)
            {
                var tab = this._addTabItem (tabPanel, Ext.dom.Query.selectValue('> label', tabs[i], "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_UNTITLED}}"));
                
                // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
                var finalOffset = offset 
                                + Ametys.form.ConfigurableFormPanel.PADDING_TAB 
                                + 1; 
                var finalROffset = roffset 
                                + Ametys.form.ConfigurableFormPanel.PADDING_TAB 
                                + 1; 
                
                var elements = Ext.dom.Query.selectNode('> elements', tabs[i]);
                if (elements)
                {
                    this._configureXML (elements, prefix, tab, finalOffset, finalROffset);
                }
                
                // Add the field checker at the end of the current tab
                var fieldCheckers = Ext.dom.Query.select('> field-checker', tabs[i]);
                if (!Ext.isEmpty(fieldCheckers))
                {
                    this._fieldCheckersManager.addFieldCheckers(tab, fieldCheckers, prefix, finalOffset, finalROffset);
                }
            }
        }
    },
    
    /**
     * Fill the configured form with values. #configure must have been called previously with data matching the configured data.
     * 
     * The data can be an XML or a JSON object.
     * 
     * The XML format
     * ==============
     * See the following structure:
     * 
     * 
     *      <myrootnode>
     *          <values>
     *              <fieldname json="false" value="3"/>
     *              <!-- ... -->
     *          </values>
     *          
     *          <comments>
     *              <field path="fieldname">
     *                  <comment id="1" date="2020-12-31T23:59:59.999+02:00">
     *                      My comment for the field <fieldname>
     *                  </comment>
     *                  <!-- ... -->
     *              </field>
     *              <!-- ... -->
     *          </comments>
     *      </myrootnode>
     * 
     * 
     * For the values, the tag **metadata** is the wrapping for tags holding the values and its name is configurable (see **valuesTagName** parameter):
     * 
     * - the tag name is the name of the concerned field (without prefix).
     * - thoses tags are recursive for sub-field (child of composites).
     * - for repeaters, an attribute **entryCount** is set on the tag, its value is the size of the repeater. Each entry is encapsulated in an **entry** tag with an attribute **name** equals to the position (1 based) of the entry.
     * - the attribute **json** set to true means the value will be interpreted as JSON before being set on the field
     * - the value itself can be either the value of the attribute **value**, or the text of the tag
     * - multiple values are set by repeating the tag.
     *   
     * For the comments, the tag **comments** is the wrapping tag and its name is configurable (see **commentsTagName** parameter):
     * 
     * - the **metadata** are not recursive 
     * - the **path** attribute contains the fieldname (without prefix) with '/' separator for sub-fields (child of composites). For repeaters, you also have to add the position of the repeater to modify. Exemple: path="mycompositefield/myrepeater/2/myfield"
     * - the **comment** tag have the following mandatory attributes :
     *   - **id** The number of the comment
     *   - **date** The date of the comment using the ISO 8601 format (will use the Ext.Date.patterns.ISO8601DateTime parser).
     *   - **author** The fullname of the author of the comment.
     *  
     *  Here is a full example:
     *  
     *  
     *      <myrootnode>
     *          <values>
     *              <!-- A simple text value -->
     *              <title>My title</title>
     *              <!-- A composite -->
     *              <illustration>
     *                  <alt-text>My alternative text</alt-text>
     *              </illustration>
     *              <!-- A richtext value -->
     *              <content>&lt;p&gt;my rich text value&lt;/p&gt;</content>
     *              <!-- A repeater -->
     *              <attachments entryCount="1">
     *                  <entry name="1">
     *                      <!-- A file metadata. The widget waits for an object value according to its documentation {@link Ametys.form.widget.File#setValue} -->
     *                      <attachment json="true">
     *                          {
     *                              "type": "metadata",
     *                              "mimeType": "application/unknown",
     *                              "path": "attachments/1/attachment",
     *                              "filename": "ametysv4.ep",
     *                              "size": "188249",
     *                              "lastModified": "2015-06-03T14:15:22.232+02:00",
     *                              "viewUrl": "/cms/plugins/cms/binaryMetadata/attachments/1/attachment?objectId=content://ec7ef7a1-139a-4863-a866-76196ed556cb",
     *                              "downloadUrl": "/cms/plugins/cms/binaryMetadata/attachments/1/attachment?objectId=content://ec7ef7a1-139a-4863-a866-76196ed556cb&amp;&download=true"
     *                          }
     *                      </attachment>
     *                      <attachment-text>fichier</attachment-text>
     *                  </entry>
     *              </attachments>
     *          </values>
     *          
     *          <comments/>
     *      </myrootnode>
     *  
     * 
     * The JSON format
     * ===============
     * See the following structure:
     * 
     * 
     *      {
     *          "values": [
     *              "fieldname": "a string value"
     *              ...
     *          ],
     *          
     *          "invalid": {
     *              "otherfield": "rawvalue"
     *              ...
     *          },

     *          "comments": {
     *              "fieldname": [
     *                  {
     *                      "text": "My comment\non two lines",
     *                      "author": "Author Fullname",
     *                      "date": "2020-12-31T23:59:59.999+02:00"
     *                  }
     *              ]
     *              ...
     *          },
     *          
     *          "repeaters": [
     *              {
     *                  name: "a_repeater",
     *                  prefix: "",
     *                  count: 2
     *              },
     *              {
     *                  name: "a_sub_repeater",
     *                  prefix: "a_repeater",
     *                  count: 5
     *              },
     *              ...
     *          ]
     *      }
     * 
     * 
     * Most information here is common with the XML format, so please start by reading it above.
     * 
     * The **values** array will fill the fields. Unlike in XML the information of the size of the repeaters is not set in this field.
     * The **repeaters** array allow to know the size of every repeater. Each element is an object with:
     * 
     * - a string **name** The name of the repeater
     * - a string **prefix** The path to this repeater ('.' separated)
     * - a number **count** The size of the repeater
     * 
     * The JSON format also accepts an **invalid** field, to pre-fill fields with raw values. For exemple, you can pre-fill a date field with a non-date string.
     * The **invalid** values should not set the same values already brought by **values**, but they will replace them in such a case.
     * 
     * @param {Object/HTMLElement} data The object that will fill the form.
     * @param {String} [valuesTagName=values] the tag name for the values 
     * @param {String} [commentsTagName=comments] the tag name for the comments 
     * @param {String} [invalidFieldsTagName=invalid] the tag name for the invalid fields
     * @param {String} [warnIfNullMessage] the warning message to display if there is no value
     */
    setValues: function(data, valuesTagName, commentsTagName, invalidFieldsTagName, warnIfNullMessage)
    {
    	valuesTagName = valuesTagName || "values";
    	commentsTagName = commentsTagName || "comments";
    	invalidFieldsTagName = invalidFieldsTagName || "invalid";
    	
    	if (this._isElement(data))
        {
            this._setValuesXML(data, valuesTagName, commentsTagName);
        }
        else if (Ext.isObject(data))
        {
            this._setValuesJSON(data, valuesTagName, commentsTagName, invalidFieldsTagName);
        }
        
    	if (warnIfNullMessage)
    	{
    		Ext.Array.each(Ext.Array.difference(this.getFieldNames(), this._initiallyNotNullFieldNames), function(fieldName){
    			var field = this.getField(fieldName);
    			if (field.type != "password")
    			{
    				field.on('render', function() {field.markWarning(warnIfNullMessage)});
    			}
    		}, this);
    	}
    	
    	if (this._showTableOfContents)
    	{
    		// Add the "Out of tabs" navigation item if necessary
    		var outOfTabFields = this._getFields(this.self.OUTOFTAB_FIELDSET_ID),
    			outOfTabRepeaters = this.getRepeaters(this.self.OUTOFTAB_FIELDSET_ID);	
    		if (!Ext.isEmpty(Ext.Array.union(outOfTabFields, outOfTabRepeaters)))
    		{
    			this._getTableOfContents().addNavigationItem("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_OUTOFTAB_FIELDSET}}", this.self.OUTOFTAB_FIELDSET_ID);
    		}
    	}
    	
        this._formReady = true;
        this.fireEvent('formready', this);
        
        this._setFocusIfReady();
        
        // Possible warning/errors at startup
        this.getFormContainer().on('afterlayout', Ext.bind(this._updateTabsStatus, this, [true], false), this, {single: 'true'});
        
    },
    
    /**
     * @private
     * This function set the values of form fields from a XML dom.
     * @param {HTMLElement} xml The XML dom
     * @param {String} valuesTagName the tag name for the values 
     * @param {String} commentsTagName the tag name for the comments 
     */
    _setValuesXML: function (xml, valuesTagName, commentsTagName)
    {
    	this._initializeTabsStatus();

    	var valuesNode = Ext.dom.Query.select(valuesTagName, xml);
        if (valuesNode != undefined)
        {
            var metadataNodes = Ext.dom.Query.selectDirectElements("*", valuesNode);
            for (var i=0; i < metadataNodes.length; i++)
            {
                this._setValuesXMLMetadata(metadataNodes[i], this.getFieldNamePrefix() + metadataNodes[i].tagName);
            }
        }
        
        this._setValuesXMLComments(Ext.dom.Query.selectNode(commentsTagName, xml));
    },
    
    /**
     * @private
     * Set the values of form fields from an automatic backup.
     * @param {Object} data The backup data object.
     * @param {Object[]} data.repeaters The repeater item counts.
     * @param {String} valuesTagName the tag name for the values 
     * @param {String} commentsTagName the tag name for the comments 
     * @param {String} invalidFieldsTagName the tag name for the invalid fields
     */
    _setValuesJSON: function(data, valuesTagName, commentsTagName, invalidFieldsTagName)
    {
    	this._initializeTabsStatus();
    	
        // Sort repeaters to get parent repeaters first.
        var sortedRepeaters = Ext.Array.sort(data.repeaters || [], function(rep1, rep2) {
            var rep1Name = rep1.prefix + rep1.name;
            var rep2Name = rep2.prefix + rep2.name;
            return rep1Name < rep2Name ? -1 : 1;
        });
        
        // Initialize repeater entries before setting the values.
        for (var i = 0; i < sortedRepeaters.length; i++)
        {
            var name = sortedRepeaters[i].name;
            var prefix = this.getFieldNamePrefix() + sortedRepeaters[i].prefix;
            var count = sortedRepeaters[i].count;
            var repeaterPanel = this.down("panel[isRepeater][name=" + name + "][prefix/=^" + prefix + "$]");
            
            if (repeaterPanel != null)
            {
                var itemsDifference = count - repeaterPanel.getItemCount();
                
                if (itemsDifference < 0)
                {
                    // Entries were initialized (initial or min-size) but the repeater contain less:
                    // we need to remove the exceeding ones.
                    repeaterPanel.getItems().each(function(panel, index, length) {
                        if (index >= count)
                        {
                            repeaterPanel.removeItem(panel);
                        }
                    });
                }
                else if (itemsDifference > 0)
                {
                    // Collapse all existing items and expand the last one. 
                    repeaterPanel.collapseAll();
                    for (var j = 0; j < itemsDifference; j++)
                    {
                        var collapsed = j < (itemsDifference-1);
                        repeaterPanel.addRepeaterItem({animate: false, collapsed: collapsed, fireRepeaterEntryReadyEvent: true});
                    }
                }
            }
        }
        
        var values = data[valuesTagName];
        this._initiallyNotNullFieldNames = Ext.Object.getKeys(values);
        
        // Set the field values.
        this._setValuesJSONForField(values);
        
        // Set the invalid field values (raw mode) and validate the fields afterwards.
        this._setValuesJSONForField(data[invalidFieldsTagName], true, true);
        
        // Set the field comments.
        this._setMetadataCommentsJSON(data[commentsTagName])
    },
    
    /**
     * @private
     * Set the values from an object.
     * @param {Object} values The object containing the values, indexed by name.
     * @param {Boolean} [rawMode=false] `true` to set the value in raw mode, `false` otherwise.
     * @param {Boolean} [validate=false] `true` to validate the value after setting it, `false` otherwise.
     */
    _setValuesJSONForField: function(values, rawMode, validate)
    {
        for (var name in values)
        {
            var value = values[name];
            var decodedValue = value;
            
            var field = this.getForm().findField(this.getFieldNamePrefix() + name);
            if (field != null)
            {
                if (rawMode && field.setRawValue)
                {
                    field.setRawValue(decodedValue);
                }
                else
                {
                    field.setValue(decodedValue);
                }
                
                if (validate)
                {
                    field.validate();
                }
            }
        }
    },
    
    /**
     * @private 
     * Sets a data values into the field
     * @param {HTMLElement} metadataNode The DOM node representing the metadata value
     * @param {String} fieldName The name of concerned field
     */
    _setValuesXMLMetadata: function (metadataNode, fieldName)
    {
        var metaName = metadataNode.tagName;
        var prefix = fieldName.substring(0, fieldName.lastIndexOf(this.defaultPathSeparator) + 1);
        var childNodes = Ext.dom.Query.selectDirectElements(null, metadataNode);
        var repeaterItemCount = Ext.dom.Query.selectNumber('@entryCount', metadataNode, -1);
        var repeaterPanel = this.down("panel[isRepeater][name=" + metaName + "][prefix/=^" + prefix + "$]");
        
        // Case of a repeater metadata.
        if (repeaterItemCount >= 0 && repeaterPanel != null)
        {
            if (repeaterItemCount < repeaterPanel.getItemCount())
            {
                // Entries were initialized (initial or min-size) but the repeater contain less:
                // we need to remove the exceeding ones.
                repeaterPanel.getItems().each(function(panel, index, length) {
                    if (index >= repeaterItemCount)
                    {
                        repeaterPanel.removeItem(panel);
                    }
                });
            }
            else if (repeaterItemCount > repeaterPanel.getItemCount())
            {
                // We're going to add some entries: collapse all existing ones.
                repeaterPanel.collapseAll();
            }
            
            for (var i=0; i < childNodes.length; i++)
            {
                if (childNodes[i].tagName == 'entry' && childNodes[i].getAttribute('name') != null)
                {
                    // Repeater value.
                    var entryPos = childNodes[i].getAttribute('name');
                    
                    // Add repeater items if they were not already created
                    // (initial or min-size).
                    if (i >= repeaterPanel.getItemCount())
                    {
                        // Expand the last item.
                        var collapsed = (i < childNodes.length-1);
                        repeaterPanel.addRepeaterItem({previousPosition: i, animate: false, collapsed: collapsed, fireRepeaterEntryReadyEvent: true});
                    }
                    else
                    {
                        // Set the previous position.
                        repeaterPanel.setItemPreviousPosition(i, i);
                    }
                    
                    this._setValuesXMLMetadata(childNodes[i], fieldName + this.defaultPathSeparator + entryPos);
                }
            }
        }
        else if (childNodes.length == 0)
        {
            // Non-composite metadata.
            var previousSibling = metadataNode.previousElementSibling || metadataNode.previousSibling;
            if (!previousSibling || previousSibling.tagName != metadataNode.tagName)
            {
            	this._initiallyNotNullFieldNames.push(fieldName);
            	
                var values = this._getValues (metadataNode);
                
                var field = this.getForm().findField(fieldName);
                if (field != null)
                {
                    if (metadataNode.getAttribute('json') == 'true')
                    {
                        field.setValue(Ext.JSON.decode(values[0], true));
                    }
                    else
                    {
                        field.setValue(field.multiple ? values : values[0]);
                    }
                }
            }
        }
        else
        {
            // Standard composite metadata.
            for (var i=0; i < childNodes.length; i++)
            {
                this._setValuesXMLMetadata(childNodes[i], fieldName + this.defaultPathSeparator + childNodes[i].tagName);
            }
        }
    },
    
    /**
     * @private 
     * Gets data values from DOM
     * @param {HTMLElement} metadataNode The DOM node representing the metadata value
     * @return {String[]} A array of values
     */
    _getValues: function (metadataNode)
    {
        var values = [];
        
        // We get children with the same name of the same parent to have all tags with the same name for multiple values.
        var nodes = Ext.dom.Query.selectDirectElements("*", metadataNode.parentNode); // We cannot make a better selector because of possible "." in the tagName.
        for (var i=0; i < nodes.length; i++)
        {
            if (nodes[i].tagName == metadataNode.tagName)
            {
                var value = nodes[i].getAttribute('value') == null ? Ext.dom.Query.selectValue('', nodes[i], '') : nodes[i].getAttribute('value');
                value = value || '';
                if (value.length > 0)
                {
                    values.push(value);
                }
            }
        }
        return values;
    },
    
    /**
     * Get the values of the invalid fields.
     * @return {Object} The invalid field values, indexed by field name.
     */
    getInvalidFieldValues: function()
    {
        var invalidValues = {};
        
        var fields = this.getForm().getFields().items;
        for (var i = 0; i < fields.length; i++)
        {
            if (!fields[i].isHidden() && fields[i].getErrors().length > 0)
            {
                var name = fields[i].getName();
                
                if (fields[i].getRawValue)
                {
                    invalidValues[name] = fields[i].getRawValue();
                }
                else
                {
                    Ext.applyIf(invalidValues, fields[i].getSubmitData());
                }
            }
        }
        
        return invalidValues;
    },
    
    /**
     * Get the invalid repeaters
     * @return {String[]} The labels of invalid repeaters
     */
    getInvalidRepeaters: function ()
    {
        var invalidRepeaters = [];
        
        var repeaters = this.getRepeaters(null);
        for (var i = 0; i < repeaters.length; i++)
        {
            var repeater = repeaters[i];
            if (!repeater.isValid())
            {
                invalidRepeaters.push(repeater.getLabel());
            }
            else
            {
                repeater.clearInvalid();
            }
        }
        
        return invalidRepeaters;
    },
    
    /**
     * Get the invalid fields
     * @return {String[]} The names of invalid fields
     */
    getInvalidFields: function ()
    {
        var invalidFields = [];
        for (var i = 0; i < this._fields.length; i++)
        {
            var fd = this.getForm().findField(this._fields[i]);
            if (!fd.isValid())
            {
                invalidFields.push(this._getFullLabel(fd));
            }
            else
            {
                fd.clearInvalid();
            }
        }
        
        this._updateTabsStatus();
        return invalidFields;
    },
    
    /**
     * Get the fields with warnings
     * @return {Object} The mapping of warned field names with their warning message
     */
    getWarnedFields: function()
    {
        var warnedFields = {};
        for (var i = 0; i < this._fields.length; i++)
        {
            var fd = this.getForm().findField(this._fields[i]);
            if (!Ext.isEmpty(fd.getActiveWarning()))
            {
            	warnedFields[this._getFullLabel(fd)] = fd.getActiveWarnings();
            }
        }
        
        this._updateTabsStatus();
        return warnedFields;
    },
    
    /**
     * Set the field comments values from a XML node containing the comments information.
     * @param {HTMLElement} rootCommentsNode The DOM node representing the root comments element.
     * @private
     */
    _setValuesXMLComments: function(rootCommentsNode)
    {
        var field, name, path, metadataCommentsNode;
        var fieldNames = this.getFieldNames();
        
        for (var i = 0; i < fieldNames.length; i++)
        {
            name = fieldNames[i];
            field = this.getForm().findField(name);
            
            // remove the field name prefix and replace this.defaultPathSeparator by '/' to compute the path
            var regex = new RegExp("\\" + this.defaultPathSeparator, "g");
            path = name.substring(this.getFieldNamePrefix().length).replace(regex, '/');
            metadataCommentsNode = Ext.dom.Query.selectNode("metadata[@path='" + path + "']", rootCommentsNode);
            
            this._setValuesXMLCommentsForField(field, metadataCommentsNode);
        }
    },
    
    /**
     * Set the field comments for a specific field from a XML node.
     * @param {Ext.form.field.Field} field The field
     * @param {HTMLElement} metadataCommentsNode The DOM node representing the comments of this field.
     * @private
     */
    _setValuesXMLCommentsForField: function(field, metadataCommentsNode)
    {
        if (field && field.showAmetysComments && metadataCommentsNode)
        {
            // Add comments to the field.
            var comments = [];
            var commentNodes = Ext.dom.Query.select('comment', metadataCommentsNode);
            var commentNode;
            
            // Sort comments by id.
            Ext.Array.sort(commentNodes, function(a, b) {
                return Ext.dom.Query.selectNumber('@id', a) - Ext.dom.Query.selectNumber('@id', b);
            });
            
            for (var i = 0; i < commentNodes.length; i++)
            {
                commentNode = commentNodes[i];
                comments.push({
                    text: Ext.dom.Query.selectValue('', commentNode),
                    author: Ext.dom.Query.selectValue('@author', commentNode),
                    date: Ext.Date.parse(Ext.dom.Query.selectValue('@date', commentNode), Ext.Date.patterns.ISO8601DateTime)
                });
            }
            
            field.addComments(comments);
        }
    },
    
    /**
     * @private
     * Set the field comments from an object.
     * @param {Object} commentsMap The object containing the comments, indexed by name.
     */
    _setMetadataCommentsJSON: function(commentsMap)
    {
        var fieldComments, field;
        for (var name in commentsMap)
        {
            fieldComments = commentsMap[name];
            field = this.getForm().findField(this.getFieldNamePrefix() + name);
            this._setMetadataCommentsForFieldJSON(field, fieldComments);
        }
    },
    
    /**
     * Set the field comments for a specific field from an object.
     * @param {Ext.form.field.Field} field The field
     * @param {Object[]} rawComments The raw comments array.
     * @private
     */
    _setMetadataCommentsForFieldJSON: function(field, rawComments)
    {
        if (field && field.showAmetysComments && rawComments)
        {
            // Add comments to the field.
            var comments = [];
            var comment;
            
            // Sort comments by id.
            Ext.Array.sort(rawComments, function(a, b) {
                return a.id - b.id;
            });
            
            for (var i = 0; i < rawComments.length; i++)
            {
                comment = rawComments[i];
                comments.push({
                    text: comment.text,
                    author: comment.author,
                    date: Ext.Date.parse(comment.date, Ext.Date.patterns.ISO8601DateTime)
                });
            }
            
            field.addComments(comments);
        }
    },
    
    /**
     * Get the form's message targets, including the test results.
     * @return {Object} The current message target configuration for the current selection selection
     */
    getMessageTargetConf: function()
    {
    	var form = this.getForm();
        var messageTargets = {
            'id': Ametys.message.MessageTarget.FORM,
            
            'parameters': {
                'object': form,
                'test-results': this._fieldCheckersManager ? this._fieldCheckersManager._testResults : {}
            }
        };
        
        var focusField;
        if (this._focusFieldId != null && (focusField = form.findField(this._focusFieldId)))
        {
            messageTargets['subtargets'] = {
                'id': Ametys.message.MessageTarget.FORM_FIELD,
                
                'parameters': {
                   name: focusField.getName()
                 }
            }
            
            if (focusField.isRichText)
            {
                var node = focusField.getNode(); 
                if (node != null)
                {
                    messageTargets['subtargets']['subtargets'] = {
                        'id': Ametys.message.MessageTarget.FORM_FIELD_RICHTEXTNODE,
                        
                        'parameters': {
                            'object': node
                        }
                    };
                }
            }
        }
        
        return messageTargets;
    },
    
    /**
     * Bind the comment click listener to a field if {@link #cfg-showAmetysComments} is true.
     * @param {Ext.form.field.Field} field The field
     * @private
     */
    _bindCommentClickListener: function(field)
    {
        if (field.showAmetysComments && !field.isDestroyed)
        {
            var commentEl = field.getEl().down('.ametys-comments div');
            if (commentEl && !field.hasAmetysCommentClickListener)
            {
                field.hasAmetysCommentClickListener = true;
                commentEl.on('click', this._onCommentClick, this, {
                    field: field
                });
            }
        }
    },
    
    /**
     * Handler function called when an user click a comment icon.
     * Open a dialogbox to enter or edit a comment for a field.
     * @param {Ext.event.Event} evt The Ext.event.Event encapsulating the DOM event
     * @param {HTMLElement} el The target of the event
     * @param {Object} args The event options
     * @param {Ext.form.field.Field}  args.field The current form field
     * @private
     */
    _onCommentClick: function(evt, el, args)
    {
        var field = args.field;
        var comment = field.getComments()[0];
        var isEdition = comment != null; 
        var text = isEdition ? Ext.String.htmlDecode(comment.text) : '';
        
        if (!this._commentsBox)
        {
            this._commentsBox = Ext.create('Ametys.window.DialogBox', {
                title: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DIALOGBOX_TITLE}}",
                icon: Ametys.getPluginResourcesPrefix("core-ui") + '/img/Ametys/theme/gray/edit_comment_16.png',
                
                scrollable: true,
                cls: 'ametys-dialogbox',
                width: 350,
                
                layout: 'anchor',
                defaults: {
                    cls: 'ametys',
                    anchor: '100%'
                },
                
                items: [{
                	itemId: 'comment',
                    xtype: 'textarea',
                    height: 100
                }],
                
                closeAction: 'hide',
                defaultFocus: 'comment',
                buttons : [{
                    itemId: 'button-ok',
                    text: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_BTN_OK}}",
                    handler: Ext.emptyFn
                }, {
                    text: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_BTN_DELETE}}",
                    handler: Ext.emptyFn,
                    hidden: true
                }, {
                    text: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_BTN_CANCEL}}",
                    handler: Ext.bind(function() {this._commentsBox.close();}, this) 
                }]
            });
        }
        
        var fdLabel = field.getFieldLabel();
        if (fdLabel.indexOf('* ') != -1)
        {
            fdLabel = fdLabel.substring(2);
        }
        this._commentsBox.setTitle ("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DIALOGBOX_TITLE}}" + ' - ' + fdLabel);
        this._commentsBox.items.get(0).setValue (text);
        this._commentsBox.getDockedItems('toolbar > button')[0].setHandler(Ext.bind(this._onCommentClickValidate, this, [field, text]));
        this._commentsBox.getDockedItems('toolbar > button')[1].setHandler(Ext.bind(this._onCommentClickDelete, this, [field]));
        this._commentsBox.getDockedItems('toolbar > button')[1].setVisible(isEdition);
        this._commentsBox.show();
    },
    
    /**
     * Handler function called when an user validate the comment dialog box.
     * Add/Edit the current field comment.
     * @param {Ext.form.field.Field}  field The current form field
     * @param {String} oldValue The old text value of the comment. (The empty string if the comment is new).
     * @private
     */
    _onCommentClickValidate: function(field, oldValue)
    {
        var newValue = Ext.String.htmlEncode(Ext.String.trim(this._commentsBox.down('textarea').getValue()));
        
        if (newValue != oldValue)
        {
            field.removeComments();
            
            if (!Ext.isEmpty(newValue))
            {
                field.addComment({
                    text: newValue,
                    author: Ametys.getAppParameter('user').fullname
                });
            }
        }
        
        this._commentsBox.close();
    },
    
    /**
     * Handler function called when an user clicks on the delete button of the comment dialog box.
     * Delete all existing comments for the field.
     * @param {Ext.form.field.Field}  field The current form field
     * @private
     */
    _onCommentClickDelete: function(field)
    {
        var me = this;
        Ametys.Msg.confirm(
            "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DELETE_CONFIRM_TITLE}}",
            "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DELETE_CONFIRM_MSG}}",
            function (btn) {
                if (btn == 'yes')
                {
                    field.removeComments();
                    me._commentsBox.close();
                }
            }
        );
    },
    
    /**
     * Listener when the edition tab policy has changed.
     * Request a graphical tab reload.
     * @param {Ametys.message.Message} message The edition tab policy changed message.
     * @private
     */
    _onUserPrefsChanged: function(message)
    {
        var target = message.getTarget(Ametys.message.MessageTarget.USER_PREFS);
        if (target != null)
        {
            if (this.getEl() != null)
            {
                this._tabPolicy =  Ametys.userprefs.UserPrefsDAO.getValue('edition-tab-policy');
                
                this._mask = Ext.create('Ext.LoadMask', {
                    target: this,
                    msg: "{{i18n PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}"
                });
                this._mask.show();
                
                Ext.defer(this._reloadTabs, 1, this);
            }
        }
    },
    
    /**
     * @private
     * Change the way tabs are displayed given the new requested policy.
     */
    _reloadTabs: function()
    {
        // Suspend layout update.
        this.suspendLayouts();
        
        if (this._tabPanel)
    	{
        	this._replacePanel(this._tabPanel);
    	}
        
        // Resume layout update and force to recalculate the layout.
        this.resumeLayouts(true);
        
        this._updateTabsStatus();
        
        if (this._mask)
        {
            this._mask.hide();
        }
    },
    
    /**
     * Replace a panel with a new one.
     * This is used when the display tab policy has changed.
     * @param {Ext.panel.Panel/Ext.tab.Panel} oldPanel the tab container to replace
     * @return {Ext.panel.Panel/Ext.tab.Panel} the new tab container
     * @private
     */
    _replacePanel: function(oldPanel)
    {
    	// New panel creation
    	var newPanel = this._addTab();

        // Replace and add items to the new panel
        var items = oldPanel.items.getRange();
        
        var newItem;
        Ext.Array.forEach(items, function(item) {
            var header = item.tab ? item.tab : item.getHeader();
            var headerCls = header.hasCls('empty') ? 'empty' : '';
            
            var newItem = this._replacePanelItem(newPanel, item, headerCls);
        }, this);
        
        // Add new panel / Remove the old one.
        this.getFormContainer().remove(oldPanel);
        
        return newPanel;
    },
    
    /**
     * Replace a panel item with a new one.
     * This is used when the display tab policy has changed.
     * @param {Ext.container.Container} ct The container where to add the tab item
     * @param {Ext.panel.Panel} tabItem the tab item to replace
     * @param {String} headerCls the CSS class name to apply to new tab item header 
     * @private
     */
    _replacePanelItem: function(ct, tabItem, headerCls)
    {
        // New panel creation
        var newItem = this._addTabItem(ct, tabItem.title, headerCls);
        
    	// Stay coherent between the two display modes
    	if (Ext.Array.contains(this._notInFirstEditionPanels, tabItem.id))
    	{
    		Ext.Array.remove(this._notInFirstEditionPanels, tabItem.id);
    		this._notInFirstEditionPanels.push(newItem.id);
		}
        
        // Add each child of the item to the new item.
        var children = tabItem.items.getRange();
        Ext.Array.forEach(children, function(child) {
            newItem.add(child);
            tabItem.remove(child);
        }, this);
        
        return newItem;
    },
    
    /**
     * @private
     * Initialize the disable conditions listeners and enable/disable the fields
     */
    _initializeDisableConditions: function()
    {
        var me = this;
        
        function activateDisableCondition(field) {
            
            var disableCondition = field.disableCondition;
            // fields that are initially disabled can never be enabled See #cfg-helpBoxId
            if (disableCondition != null && !field.disabled)
            {
                me._disableField(field);
                me._addDisableConditionsListeners(typeof disableCondition === 'string' ? JSON.parse(disableCondition) : disableCondition, field);
            }
        }
        
        // Disable conditions listeners
        this.getForm().getFields().each(activateDisableCondition, this);
        
        // Act on fields added in repeaters
        this.on('repeaterEntryReady', function(repeater) {
            this.getChildrenFields(repeater).each(activateDisableCondition, this);
        }, this);
    },
    
    /**
     * @private
     * Add listeners to evaluate the disable condition dynamically
     * @param {Object} disableCondition the disable condition
     * @param {Object[]} disableCondition.conditions an array of conditions that can contain several condition objects or other conditions
     * @param {Object[]} disableCondition.condition an array of condition objects
     * @param {String} disableCondition.condition.id the id of the field that will be evaluated
     * @param {String} disableCondition.condition.operator the operator used to evaluate the field 
     * @param {String} disableCondition.condition.value the value on which the field will be compared to
     * @param {Object} disablingField the field on which the disable condition applies 
     */
    _addDisableConditionsListeners: function(disableCondition, disablingField)
    {
        if (disableCondition.conditions)
        {
            var conditionsList = disableCondition.conditions,
                conditionsListLength = conditionsList.length;
            for (var i = 0; i < conditionsListLength; i++)
            {
                this._addListeners(conditionsList[i], disablingField);
            }
        }
        
        if (disableCondition.condition)
        {
            var conditionList = disableCondition.condition,
                conditionListLength = conditionList.length;
            for (var i = 0; i < conditionListLength; i++)
            {
                var field = this.getRelativeField(this.getFieldNamePrefix() + conditionList[i]['id'], disablingField);
                field.on('change', Ext.bind(this._disableField, this, [disablingField], false));
            }
        }
    },
    
    /**
     * @private
     * Enables/disables the field.
     * @param {Object} field the field to(not to) disable.
     */
    _disableField: function(field)
    {   
        var disable = this._evaluateDisableCondition(typeof field.disableCondition === 'string' ? JSON.parse(field.disableCondition) : field.disableCondition, field);
        field.setDisabled(disable);
        field.setHidden(disable && this.hideDisabledFields);
    },
    
    /**
     * @private
     * Evaluates the disable condition when a matching field is changing and enables/disables the field accordingly.
     * @param {Object} disableCondition the disable condition.
     * @param {Ext.form.Field} field The field of reference
     */
    _evaluateDisableCondition: function(disableCondition, field)
    {
        if (!disableCondition.conditions && !disableCondition.condition)
        {
            return false;
        }
        
        var disable = disableCondition['type'] != "and" ? false : true;
        
        if (disableCondition.conditions)
        {
            var conditionsList = disableCondition.conditions,
                conditionsListLength = conditionsList.length;
            
            for (var i = 0; i < conditionsListLength; i++)
            {
                var result = this._evaluateDisableCondition(conditionsList[i]);
                disable = disableCondition['type'] != "and" ? disable || result : disable && result;
            }
        }
        
        if (disableCondition.condition)
        {
            var conditionList = disableCondition.condition,
                conditionListLength = conditionList.length;
            
            for (var i = 0; i < conditionListLength; i++)
            {
                var id = this.getFieldNamePrefix() + conditionList[i]['id'],
                    op = conditionList[i]['operator'],
                    val = conditionList[i]['value'];
                    
                var result = this._evaluateCondition(id, op, val, field);
                disable = disableCondition['type'] != "and" ? disable || result : disable && result;
            }
        }
        
        return disable;
    },
    
    /**
     * @private
     * Evaluates a single condition.
     * @param {String} id the id of the field.
     * @param {String} operator the operator.
     * @param {String} value the value the field's value will be compared to.
     * @param {Ext.form.Field} field The field of reference
     * @return {Boolean} result true if the condition is verified, false otherwise.
     */
    _evaluateCondition: function(id, operator, value, field)
    {
        var field = this.getRelativeField(id, field);
        var fieldValue = field.getValue();
        
        switch (operator)
        {
            case "gt" : 
                return fieldValue > value;
            case "geq" : 
                return fieldValue >= value;
            case "eq" : 
                return fieldValue == value;
            case "leq" : 
                return fieldValue <= value;
            case "lt" : 
                return fieldValue < value;
            case "neq" : 
                return fieldValue != value;
            default :
                throw "Unknown operator " + operator;
                break;
        }
    },
    
    /* --------------------------------------------------------------------- */
    /*                         Misc helper methods                           */
    /* --------------------------------------------------------------------- */
    
    /**
     * Helper method to be used to execute a function while being sure that the form and all the repeater entry are ready.
     * @param {Function} initiliazeFn The initialize function to execute. Will be executed immediately if the form is ready.
     * @param {Object} scope The scope handler. Default to the form.
     * @param {Object} args Optional function arguments.
     */
    executeFormReady: function(initiliazeFn, scope, args)
    {
        if (this._addingRepeaterEntry)
        {
            this.on('repeaterEntryReady', Ext.bind(this._executeFormReadyCb, this, [initiliazeFn, scope, args]), undefined, {single: true});
        }
        else if(this._formReady)
        {
            this._executeFormReadyCb(initiliazeFn, scope, args);
        }
        else
        {
            this.on('formready', Ext.bind(this._executeFormReadyCb, this, [initiliazeFn, scope, args]), undefined, {single: true});
        }
    },
    
    /**
     * @private
     * Internal callback used for executeFormReady.
     * @param {Function} initiliazeFn The initialize function to execute
     * @param {Object} scope The scope handler. Default to the form.
     * @param {Object} args Optional function arguments.
     */
    _executeFormReadyCb: function(initiliazeFn, scope, args)
    {
        if (Ext.isFunction(initiliazeFn))
        {
            initiliazeFn.apply(scope || this, args);
        }
    },
    
    /* --------------------------------------------------------------------- */
    /*               Helper methods to work on relative fields               */
    /* --------------------------------------------------------------------- */
    
    /**
     * Helper method to get a relative field
     * @param {String} fieldPath The path of the relative field, which is a relative path (e.g. a/b/c or ../../e/f)
     * @param {Ext.form.field.Field} field The field of reference
     * @return {Ext.form.field.Field} The relative field or null if the field has not been found or if the form is not ready yet.
     */
    getRelativeField: function(fieldPath, field)
    {
        var relativeField = null;
        
        if (fieldPath)
        {
            // try to get the relative field from the field cache
            var cache = field['__relativeFields'];
            if (cache && fieldPath in cache)
            {
                var relativeFieldId = cache[fieldPath];
                relativeField = relativeFieldId ? Ext.getCmp(relativeFieldId) : null;
            }
            else
            {
                if (!cache)
                {
                    cache = field['__relativeFields'] = {};
                }
            
                if (!relativeField)
                {
                    var prefix = field.name.substring(0, field.name.lastIndexOf(this.defaultPathSeparator));
                    
                    // Handling '..' in field name.
                    Ext.Array.forEach(fieldPath.split('/'), function(pathPart) {
                        if (pathPart == '..')
                        {
                            prefix = prefix.substring(0, prefix.lastIndexOf(this.defaultPathSeparator));
                            fieldPath = fieldPath.substring(3);
                        }
                    });
                    
                    // Separator in composites path is '/' whereas javascript path separator must be '.'
                    fieldPath = fieldPath.replace('/', this.defaultPathSeparator);
                    var relativeFieldPath = prefix == '' ? fieldPath : (prefix + this.defaultPathSeparator + fieldPath);
                    
                    relativeField = this.getField(relativeFieldPath);
                    if (!relativeField)
                    {
                        var message = "{{i18n PLUGINS_CORE_UI_WIDGET_UNKNOWN_FIELD}}" + relativeFieldPath;
                        this.getLogger().error(message);
                    }
                }
                
                // Populate cache
                cache[fieldPath] = relativeField ? relativeField.getId() : null;
            }
        }
        
        return relativeField;
    },
    
    /**
     * Helper method to get relative fields
     * @param {String[]} fieldPaths An array of path to relative fields. Each path is relative (e.g. a/b/c or ../../e/f).
     * @param {Ext.form.field.Field} field The field of reference
     * @return {Ext.form.field.Field[]} The array of the relative fields in the same order than the fieldPaths array, if a field is not found, its corresponding entry in the array will be null. If the form is not ready, the empty array will be returned.
     */
    getRelativeFields: function(fieldPaths, field)
    {
        var relativeFields = [];
        
        if (fieldPaths)
        {
            Ext.Array.forEach(fieldPaths, function(fieldPath) {
                relativeFields.push(this.getRelativeField(fieldPath, field));
            }, this);
        }
        
        return relativeFields;
    },
    
    /**
     * Helper method to get all children fields of a repeater
     * @param {Ametys.form.ConfigurableFormPanel.Repeater} repeater The repeater
     * @return {Ext.util.MixedCollection} The children fields of this repeater
     */
    getChildrenFields: function(repeater)
    {
        return this.getForm().getFields().filter('name', this.getFieldNamePrefix() + repeater.name + this.defaultPathSeparator + repeater.getItemCount() + this.defaultPathSeparator);
    },
    
    /**
     * Helper method to listen to the change event of a relative field.
     * @param {String/String[]} fieldPaths The path of the relative field, which is a relative path (e.g. a/b/c or ../../e/f). An array of path can also be provided.
     * @param {Ext.form.field.Field} field The field who is searching for a relative field
     * @param {Function} handler The on change handler
     * @param {Ext.form.field.Field} handler.field The relative field that has triggered the on change event.
     * @param {Object} handler.newValue The new value
     * @param {Object} handler.oldValue The old value
     * @param {Object} scope The scope handler. Default to the field.
     */
    onRelativeFieldsChange: function(fieldPaths, field, handler, scope)
    {
        if (this._addingRepeaterEntry)
        {
            this.on('repeaterEntryReady', Ext.bind(this._onRelativeFieldsChangeReady, this, [fieldPaths, field, handler, scope]), undefined, {single: true});
        }
        else if (this._formReady)
        {
            this._onRelativeFieldsChangeFormReady(fieldPaths, field, handler, scope);
        }
        else
        {
            this.on('formready', Ext.bind(this._onRelativeFieldsChangeReady, this, [fieldPaths, field, handler, scope]), undefined, {single: true});
        }
    },
    
    /**
     * @private
     * Internal callback used for onRelativeFieldsChange.
     * @param {String/String[]} fieldPaths The path of the relative field, which is a relative path (e.g. a/b/c or ../../e/f). An array of path can also be provided.
     * @param {Ext.form.field.Field} field The field who is searching for a relative field
     * @param {Function} handler The on change handler
     * @param {Ext.form.field.Field} handler.field The relative field that has triggered the on change event.
     * @param {Object} handler.newValue The new value
     * @param {Object} handler.oldValue The old value
     * @param {Object} scope The scope handler. Default to the field.
     */
    _onRelativeFieldsChangeReady: function(fieldPaths, field, handler, scope)
    {
        fieldPaths = Ext.Array.from(fieldPaths);
        var relativeFields = this.getRelativeFields(fieldPaths, field);
        
        Ext.Array.forEach(relativeFields, function(relativeField) {
            if (relativeField)
            {
                field.mon(relativeField, 'change', handler, scope || field);
            }
        });
    }
});

Ext.define("Ametys.message.ConfigurableFormPanelMessageTarget",
    {
        override: "Ametys.message.MessageTarget",
        statics: 
        {
            /**
             * @member Ametys.message.MessageTarget
             * @readonly
             * @property {String} FORM The target of the message is a form. It has at least the parameters: **form** the form object and **test-results** the results of the test of the parameters checkers. 
             */
            FORM: "form",
            
            /**
             * @member Ametys.message.MessageTarget
             * @readonly
             * @property {String} FORM_FIELD The target of the message is the field of a #FORM. The parent Ametys.message.MessageTarget must be a #FORM. The parameter provided is the **name** of the selected field in the parent target form.  
             */
            FORM_FIELD: "field",
            
            /**
             * @member Ametys.message.MessageTarget
             * @readonly
             * @property {String} FORM_FIELD_RICHTEXTNODE The target of the message is a node of a richtext #FIELD of a #FORM. The parent Ametys.message.MessageTarget must be a #FORM_FIELD providing a field of type 'richtext'. The parameter provided is **object** that is the HTMLElement node in the rich text.  
             */
            FORM_FIELD_RICHTEXTNODE: "node"
        }
    }
);
