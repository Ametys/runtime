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
 * This class is a configurable form panel that can contains tabs, fieldsets, repeaters and widgets. Configuration is made through XML or JSON requests.
 * The configuration format can be in JSON or XML.
 * The 2 steps to use this components are : 
 *  
 * 1) create the form (#configure) 
 * 2) fill the values (#setValues)
 */
Ext.define('Ametys.form.ConfigurableFormPanel', {
	extend: "Ext.form.Panel",
	
	statics: {
		/**
		 * @property {Number} PADDING_GENERAL The main padding
		 * @private
		 * @readonly 
		 */
		PADDING_GENERAL: 5,
		/**
		 * @property {Number} PADDING_FIELDSET The padding for fieldset
		 * @private
		 * @readonly 
		 */
		PADDING_FIELDSET: 5,
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
		FIELD_MINWIDTH: 150
	},
	
	/**
     * @cfg {String/String[]/Ext.XTemplate} tabErrorFieldsTpl
     * The template used to format the Array of warnings and errors fields passed to tab ToolTip into a single HTML
     * string. It renders each message as an item in an unordered list.
     */
	tabErrorFieldsTpl: [
		'<div class="ametys-tab-tooltip-status">',
		'<tpl if="errors && errors.length">',
			'<tpl if="errors.length == 1">',
				"<span class=\"ametys-tab-tooltip-error-label\"><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_ERROR_FIELD'/></span>",
			'</tpl>',
			'<tpl if="errors.length != 1">',
				"<span class=\"ametys-tab-tooltip-error-label\">{errors.length}<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_ERROR_FIELDS'/></span>",
			'</tpl>',
		    '<ul class="error"><tpl for="errors"><li>{.}</li></tpl></ul>',
		'</tpl>',
		'<tpl if="warns && warns.length">',
			'<tpl if="warns.length == 1">',
				"<span class=\"ametys-tab-tooltip-warn-label\"><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_WARN_FIELD'/></span>",
			'</tpl>',
			'<tpl if="warns.length != 1">',
				"<span class=\"ametys-tab-tooltip-warn-label\">{warns.length}<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_WARN_FIELDS'/></span>",
			'</tpl>',
		    '<ul class="warn"><tpl for="warns"><li>{.}</li></tpl></ul>',
		'</tpl>',
		'<tpl if="comments && comments.length">',
			'<tpl if="comments.length == 1">',
				"<span class=\"ametys-tab-tooltip-comment-label\"><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_COMMENT_FIELD'/></span>",
			'</tpl>',
			'<tpl if="comments.length != 1">',
				"<span class=\"ametys-tab-tooltip-comment-label\">{comments.length}<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_TPL_COMMENT_FIELDS'/></span>",
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
      * @cfg {String} fieldNamePrefix='' The prefix to all submited fields (should end with '.' if non empty
      */
     
	/**
	 * @cfg {Object} defaultFieldConfig Default config to apply to all form fields
	 */
	
	/**
	 * @cfg {String} [tab-policy-mode] The display tab policy name (which has a higher priority than the userprefs value). Currently accepted values are 'default' or 'inline'.
	 */
	
	/**
	 * @property {String} _tabPolicy The current display tab policy name.
	 * @private
	 */
	
	/**
	* @property {Ext.form.Field[]} _fields The configuration fields
	* @private
	 */
	
	/**
	* @property {Ext.form.Panel[]} _tabPanels The tab panels.
	* @private
	 */
	
	/** @cfg {Object} itemsLayout The layout to use in the container. Default to { type: 'anchor' }. */
	
	/** @cfg {Object} fieldsetLayout The layout to use in the nested fieldsets. Default to { type: 'anchor' }. */

	/** @cfg {Object} tabsLayout The layout to use in the tabs. Default to { type: 'anchor' }. */
    
    /** @cfg {Object} additionnalWidgetsConf Additionnal configuration for every widget that will be created by this form. Each key of this object is the widget configuration name to add, and each corresponding value is the configuration name that will be read from the input configuration stream. */ 
    /**
     * @property {Object} _additionnalWidgetsConf See #cfg-additionnalWidgetsConf.
     */
	
	autoScroll: true,
	layout: {
		type: 'anchor',
		reserveScrollbar: true
	},
	
	border: false,
	componentCls: 'ametys-form-panel',
	
	/**
	* @property {Boolean} _formReady indicates if the form is ready.
	* The form is ready when all fields are rendered and set value
	* @private
	 */
	_formReady: false,
	
	constructor: function (config)
	{
		config.bodyPadding = config.bodyPadding != null ?  config.bodyPadding : Ametys.form.ConfigurableFormPanel.PADDING_GENERAL;
		config.items = this._getFormContainerCfg(config);
		
		this.defaultFieldConfig = config.defaultFieldConfig || {};
        
        this._additionnalWidgetsConf = config.additionnalWidgetsConf || {};
		
		this.callParent(arguments);
		
        this._init();

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
		
		// Message bus listeners.
		// 'tab-policy-mode' has priority over 'edition-tab-policy' userprefs.
		if (!config['tab-policy-mode']) 
		{
			Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onUserPrefsChanged, this);
		}
		
        // Display tab policy. The 'tab-policy-mode' configuration parameter passed
        // during the form edition panel instantiation has priority over the
        // userprefs value.
        this._tabPolicy = config['tab-policy-mode'] || Ametys.userprefs.UserPrefsDAO.getValue('edition-tab-policy') || 'default';
		
		this._fieldNamePrefix = config["fieldNamePrefix"] || '';
		
		this.showAmetysComments = config.showAmetysComments === true;
	},
	
	/**
	 * @private
	 * Inherited to unregister from the message bus
	 * @inheritdoc
	 */
	destroy: function()
	{
        this.destroyComponents();
		Ametys.message.MessageBus.unAll(this);
		this.callParent(arguments);
	},
	
	/**
	 * Get the form container config to be used during its creation.
	 * @protected
	 */
	_getFormContainerCfg: function(config)
	{
		return {
			xtype: 'container',
			autoScroll: false,
			border: false,
			
			layout: config.itemsLayout || { type: 'anchor' },
			
			items: config.items,
			
			// minWidth is a minWidth of a field + a number of repeaters margins
			minWidth: Ametys.form.ConfigurableFormPanel.LABEL_WIDTH
				+ Ametys.form.ConfigurableFormPanel.FIELD_MINWIDTH
				+ 20 // ametysDescription
				+ (config.showAmetysComments ? 20 : 0)
				+ (Ametys.form.ConfigurableFormPanel.Repeater.NESTED_OFFSET+1) * 3 // 3 level of repeaters
		};
	},
	
	/**
	 * Get the form container in which the edition form must be drawn.
	 * By default it is the first child item of this panel.
	 * @protected
	 */
	_getFormContainer: function()
	{
		return this.items.get(0);
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
	 * Get a field in this form by id or name
	 * @param {String} name The name (or id) of the searched field
	 * @return {Ext.form.field.Field} The first matching field, or null if none was found.
	 */
	getField : function (name)
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
			var fd = this.getField (name);
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
	 * Initialize the form edition panel.
	 * Call these when reusing the panel.
     * @private
	 */
	_init: function ()
	{
		this._fields = [];	
		this._repeaters = [];
		this._tabPanels = [];
	},
	
	/**
	 * Destroy the all form items.
     * Call this if the form is not destroyed but you want to free its underlying children.
	 */
	destroyComponents: function()
	{
		if (this.items && this.items.length > 0)
		{
			this._getFormContainer().removeAll();
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
		tabpanel.mask("<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_WAIT_MSG'/>");
		
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
					panel.expand()
				}
			});
		}
		finally 
		{
			this.resumeLayouts(true);
			tabpanel.unmask();
			
			btn.setText(collapse ? "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_EXPAND_ALL'/>" : "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_COLLAPSE_ALL'/>");
		}
	},
	
	/**
	 * Creates a tab container
	 * @return {Ext.tab.Panel} the tab container
	 * @private
	 */
	_addTab: function ()
	{
		var tabPanel;
		
		var me = this;
		
		if (this._tabPolicy === 'inline')
		{
			tabPanel = Ext.create('Ext.panel.Panel', {
				cls: 'ametys-form-tab-inline',
				margin: '5 0 0 0',
				layout: this.initialConfig.tabsLayout || { type: 'anchor' },

				border: false,
				
				dockedItems: [{
					dock: 'top',
					xtype: 'toolbar',
					cls: 'ametys-form-tab-inline-toolbar',
					items:[
					    '->',
					    {
						    text: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_INLINETAB_COLLAPSE_ALL'/>",
						    enableToggle: true,
							toggleHandler: function (btn, state) { 
								me._expandOrCollapseAllInlineTab(tabPanel, btn, state)
							}
					    }
					]
				}]
			});
		}
		else
		{
			tabPanel = Ext.create('Ext.tab.Panel', {
				cls: 'ametys-form-tab',
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
					beforetabchange: {fn: this._onTabChange, scope: this}
				}
			});
		}
		
		this._getFormContainer().add(tabPanel);
		this._tabPanels.push(tabPanel);
		
		return tabPanel;
	},
	
	/**
	 * Validate tab on leaving.
	 * @param {Ext.tab.Panel} tabPanel The tab panel.
	 * @param {Ext.panel.Panel} newCard The panel just switched to.
	 * @param {Ext.panel.Panel} oldCard The panel just left.
	 */
	_onTabChange: function(tabPanel, newCard, oldCard)
	{
	    if (oldCard != null)
        {
	        var fields = this._getFields(oldCard);
	        
	        for (var i = 0; i < fields.length; i++)
	        {
	            // Trigger internal validation without firing validity change.
	            fields[i].isValid();
	        }
	        
	        
	        var repeaters = this.getRepeaters(oldCard);
	        for (var i = 0; i < repeaters.length; i++)
	        {
	            // Trigger internal validation without firing validity change.
	        	repeaters[i].isValid();
	        }
	        
	        this._updateTabStatus(oldCard);
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
				headerCls: headerCls,
				
				bodyPadding: Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' 0 ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB,
				margin: '0 0 5 0',
				
				layout: this.initialConfig.tabsLayout || { type: 'anchor' },
				
				collapsible: true,
				titleCollapse: true,
				hideCollapseTool: true,
				
				border: false,
				shadow: false,
				cls: 'ametys-form-tab-inline-item',
				
				listeners: {
					'afterrender': function () {
						if (this.headerCls) {
				            this.header.addCls(this.headerCls);
				        }
					}
				}
			});
			
			ct.add(fieldset);
			
			return fieldset;
		}
		else
		{
			var tabitem = Ext.create('Ext.panel.Panel', {
				title: label,
				headerCls: headerCls,
				
				cls: 'ametys-form-tab-item',
				layout: this.initialConfig.itemsLayout || { type: 'anchor' },
				
				padding: Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB + ' 0 ' + Ametys.form.ConfigurableFormPanel.PADDING_TAB,
				
				border: false,
				autoScroll: false,
				
				listeners: {
					'afterrender': function () {
						if (this.headerCls) {
				            this.tab.addCls(this.headerCls);
				        }
					}
				}
			});
			ct.add(tabitem);
			
			return tabitem;
		}
	},
    
    /**
     * Get the list of fields in a container (any level).
     * @param {Ext.container.Container} container The container.
     * @return {Ext.Component[]} An array of components which have the field mixin.
     */
    _getFields: function(container)
    {
        var fields = [];
        
        // Function walking the component tree and adding fields to the array.
        var fieldWalker = function(component)
        {
            if (component.isFormField)
            {
                fields.push(component);
            }
            else if (component.isXType('container'))
            {
                component.items.each(fieldWalker);
            }
        }
        container.items.each(fieldWalker);
        
        return fields;
    },
    
    /**
     * Get the list of repeaters in a container (any level).
     * @param {Ext.container.Container} [container] The container of repeaters. The form if not specified
     * @return {Ext.Component[]} An array of components which have the field mixin.
     */
    getRepeaters: function (container)
    {
        container = container || this;
    	return container.query('panel[isRepeater]');
    },
    
    /**
     * @private
     * Initialize the status of all tabs
     */
    _initializeTabsStatus: function()
    {
    	if (this._tabPanels.length > 0)
    	{
    		this.suspendLayouts();

    		var me = this;
    		for (var i=0; i < this._tabPanels.length; i++)
    		{
    			this._tabPanels[i].items.each (function (item) {
    				
    				var header = item.tab ? item.tab : item.getHeader();
                    if (header != null)
                    {
                    	header.addCls(['empty']);
                    }
    			})
    		}
    		
    		this.resumeLayouts(true);
    	}
    },
    
    
    /**
     * @private
     * Update the status of all tabs
     * @param {Boolean} [startup=false] `true` when laying out the form for the first time: the panel header class will be `startup` instead of `not-startup`.
     */
    _updateTabsStatus: function(startup)
    {
    	if (this._tabPanels.length > 0)
    	{
    		this.suspendLayouts();

    		var me = this;
    		for (var i=0; i < this._tabPanels.length; i++)
    		{
    			this._tabPanels[i].items.each (function (item) {
    				me._updateTabStatus (item, startup);
    			})
    		}
    		
    		this.resumeLayouts(true);
    	}
    },
    
    /**
     * @private
     * Update the tab status.
     * @param {Ext.panel.Panel} panel The panel (tab card or fieldset panel).
     * @param {Boolean} [startup=false] `true` when laying out the form for the first time: the panel header class will be `startup` instead of `not-startup`.
     */
    _updateTabStatus: function(panel, startup)
    {
        // The header is the tab when in tab mode or the header in linear mode. 
        var header = panel.tab ? panel.tab : panel.getHeader();
        
        if (header != null)
        {
        	var tabFields = this._getFields(panel);
            
            var errorFields = [];
            var warnFields = [];
            var commentFields = [];
            
            for (var i = 0; i < tabFields.length; i++)
            {
                if (!tabFields[i].isHidden())
                {
                    if (tabFields[i].getErrors().length > 0)
                    {
                        errorFields.push(this._getFieldLabel(tabFields[i], panel));
                    }
                    
                    if (Ext.isFunction(tabFields[i].hasActiveWarning) && tabFields[i].hasActiveWarning())
                    {
                    	warnFields.push(this._getFieldLabel(tabFields[i], panel));
                    }
                    
                    if (Ext.isFunction(tabFields[i].getComments) && tabFields[i].getComments().length > 0)
                    {
                    	var comment = tabFields[i].getComments()[0];
                    	var commentValue = Ext.String.format('<em>{0} ({1}, le {2})</em>', comment.text, comment.author, Ext.Date.format(comment.date, Ext.Date.patterns.FriendlyDateTime));
                    			 
                    	commentFields.push(this._getFieldLabel(tabFields[i], panel) + " : " + commentValue);
                    }
                }
            }
            
            // Invalidate repeaters
            var tabRepeaters = this.getRepeaters(panel);
            for (var i = 0; i < tabRepeaters.length; i++)
            {
            	if (tabRepeaters[i].getErrors().length > 0)
                {
                    errorFields.push(this._getRepeaterLabel(tabRepeaters[i], panel));
                }
            }
            
        	// var isActive = panel.tab ? panel.ownerCt.getActiveItem() == panel : true;
        	// header[isActive ? 'addCls' : 'removeCls']('active');
        	
        	// When not in startup mode, remove the startup class.
        	header[startup ? 'addCls' : 'removeCls']('startup');
        	header[startup ? 'removeCls' : 'addCls']('not-startup');
        	
            var hasError = errorFields.length > 0;
            var hasWarn = warnFields.length > 0;
            var hasComment = commentFields.length > 0;
            
            var headerCls = '';
            if (hasError)
            {
            	header.removeCls(['warning']);
            	header.removeCls(['comment']);
            	header.addCls('error')
            }
            else if (hasWarn)
            {
            	header.removeCls(['error']);
            	header.removeCls(['comment']);
            	header.addCls('warning')
            }
            else if (hasComment)
            {
            	header.removeCls(['error']);
            	header.removeCls(['warning']);
            	header.addCls('comment')
            }
            else
            {
            	header.removeCls(['error', 'warning', 'comment']);
            }
            
            // The default error class adds a padding: force the tab bar to layout.
            header.ownerCt.doLayout(); //TODO : what should I do ?
            
            if (header.rendered)
            {
            	this._createTabTooltip (header, panel, errorFields, warnFields, commentFields);
            }
            else
            {
            	header.on ('afterrender', Ext.bind (this._createTabTooltip, this, [header, panel, errorFields, warnFields, commentFields], false), this, {single: true});
            }
        }
    },
    
    /**
     * @private
     * Create tab tooltip
     * @param {Ext.tab.Tab} header the tab panel's header
     * @param {Ext.panel.Panel} panel The panel
     * @param {Object[]} errorFields Fields in error
     * @param {Object[]} warnFields Fields with warnings
     * @param {Object[]} commentFields Fields with comments
     */
    _createTabTooltip : function (header, panel, errorFields, warnFields, commentFields)
    {
    	Ext.tip.QuickTipManager.unregister(header.getEl());
        
        if (errorFields.length > 0 || warnFields.length > 0 || commentFields.length > 0)
        {
            // Set the tooltip.
            var title = panel.title;
            
            var text = this._getTabTooltipText(panel, errorFields, warnFields, commentFields);
            
            Ext.tip.QuickTipManager.register({
                target: header.getEl().id,
                title: title,
                text: text,
                cls: 'x-fluent-tooltip ametys-tab-tooltip',
                width: 350,
                dismissDelay: 0
            });
        }
    },
    
    /**
     * Get a field's label.
     * @param {Ext.form.Labelable} field A component with the Labelable mixin.
     * @param {Ext.Panel} tabpanel Teh panel containing the field.
     * @return {String} the tooltip message markup.
     */
    _getFieldLabel: function(field, tabpanel)
    {
    	var label = '';
    	
    	var ownerCt = field.ownerCt;
    	while (ownerCt != null && ownerCt.title && ownerCt.id != tabpanel.id)
    	{
    		label = ownerCt.title + " > " + label;
    		ownerCt = ownerCt.ownerCt;
    	}
    	
        var fieldLabel = field.getFieldLabel();
        if (Ext.String.startsWith(fieldLabel, '*'))
        {
        	fieldLabel = fieldLabel.substr(1).trim();
        }
        
        // var errors = Ext.merge(field.getErrors(), field.getActiveWarnings()).join(',');
        return label + fieldLabel;
    },
    

    /**
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
     * Get the tooltip message.
     * @param {Ext.panel.Panel} panel The panel to which the component was added.
     * @param {String[]} errorFields The error fields' labels.
     * @param {String[]} warnFields The warning fields' labels.
     * @param {String[]} commentFields The commented fields' labels and their comment.
     * @return {String} the tooltip message markup.
     */
    _getTabTooltipText: function(panel, errorFields, warnFields, commentFields)
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
     * Listens when a field validity changes.
     * @param {Ext.form.field.Field} field The field.
     * @param {Boolean} isValid Whether or not the field is now valid.
     */
    _onFieldValidityChange: function(field, isValid)
    {
    	if (this._formReady)
    	{
    		 // Find the tab card (panel) to which belongs the field.
            var card = field.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
            if (card == null)
            {
                return;
            }
            
            // Update the tab status and tooltip.
            this._updateTabStatus(card);
    	}
    },
    
    /**
     * Listens when the value of a field is changed
     * @param {Ext.form.field.Field} field The field.
     * @param {Boolean} newValue The new value
     */
    _onFieldChange: function(field, newValue)
    {
    	if (!Ext.isEmpty(newValue))
    	{
    		 // Find the tab card (panel) to which belongs the field.
            var card = field.up('panel[cls~=ametys-form-tab-item], panel[cls~=ametys-form-tab-inline-item]');
            if (card == null)
            {
                return;
            }
            
            var header = card.tab ? card.tab : card.getHeader();
            if (header != null)
            {
            	header.removeCls(['empty']);
            }
    	}
    },
    
	/**
	 * Creates a fieldset with this label
	 * @param {Ext.Element} ct The container where to add the fieldset
	 * @param {String} label The label of the fieldset
	 * @param {Number} nestingLevel The nesting level of the fieldset.
	 * @return {Ext.form.FieldSet} The created fieldset
     * @private
	 */
	_addFieldSet: function (ct, label, nestingLevel)
	{
		var fdCfg = {
			nestingLevel: nestingLevel,
			ametysFieldSet: true,
			layout: this.initialConfig.fieldsetLayout || { type: 'anchor' },

			bodyPadding: Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET + ' ' + Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET + ' 0 ' + Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET,
			margin: '0 0 5 ' + (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : '0'),
			
			border: false,
			shadow: false
		};
		
		if (label)
		{
			Ext.apply(fdCfg, {
				title : label,
				
				collapsible: true,
				titleCollapse: true,
				hideCollapseTool: true,
				cls: 'ametys-fieldset'
			});
		}
		
		var fieldset = new Ext.panel.Panel(fdCfg);
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
	 * @return {Ext.form.field.Field} The created field
	 * @private
	 */
	_createInputField: function (config)
	{
	    var offset = config.offset || 0;
	    var roffset = config.roffset || 0;
	    
	    var fieldCfg = Ext.clone(this.defaultFieldConfig);
	    Ext.applyIf (fieldCfg, {
	    	cls: 'ametys',
			style: 'margin-right:' + Math.max(60 - roffset, 0) + 'px',
			labelAlign: 'right',
	        labelWidth: Ametys.form.ConfigurableFormPanel.LABEL_WIDTH - offset,
	        labelSeparator: '',
	        minWidth: Ametys.form.ConfigurableFormPanel.LABEL_WIDTH - offset + Ametys.form.ConfigurableFormPanel.FIELD_MINWIDTH,
	        anchor: '100%',
	        
	        allowBlank: !config.mandatory,
	        regex: config.regexp ? new RegExp (config.regexp) : null,
			regexText: config.regexText || config.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_INVALID_REGEXP'/>" + config.regexp,
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
		
		return Ametys.form.WidgetManager.getWidget (config.widget, config.type.toLowerCase(), widgetCfg);
	},
	
    /**
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
	 * @param {Object} config The repeater configuration object. See Ametys.form.ConfigurableFormPanel.Repeater configuration.
	 * @return {Ametys.form.ConfigurableFormPanel.Repeater} The created repeater panel
     * @private
	 */
	_createRepeater: function (config)
	{
		var repeaterCfg = Ext.applyIf(config, {
			minSize: 0,
			maxSize: Number.MAX_VALUE,
			form: this
		});
		
		return Ext.create('Ametys.form.ConfigurableFormPanel.Repeater', repeaterCfg);
	},
    
    /**
     * @private
     * Test if an element is a HTMLElement or not 
     * @param {Object} o The object to test
     * @return {Boolean} true is o is an instance of HTMLElement
     */
    _isElement: function isElement(o) {
          return typeof HTMLElement === "object" ? 
                o instanceof HTMLElement : //DOM2
                o && typeof o === "object" && o !== null && o.nodeType === 1 && typeof o.nodeName==="string";
    },
    
    /**
     * This function creates and add form elements from a definition
     * 
     * The JSON configuration format is
     * 
     * 
     *      {
     *          "&lt;fieldName&gt;": {
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
     *          ]
     *      }
     * 
     * 
     * 
     * The **&lt;fieldName&gt;** is the form name of the field. (Note that you can prefix all field names using #cfg-fieldNamePrefix). See under for the reserved fieldName "fieldsets"
     * 
     * The string **label** is the readable name of your field that is visible to the user.
     * 
     * The string **description** is a sentence to help the user understand the field. It will appear in a tooltip on the right help mark.
     * 
     * The string **type** is the kind of value handled by the field. The supported types for metadata depends of the configuration of you Ametys.form.WidgetManager. Kernel provides widgets for the following types (case is not important):
     * BINARY, BOOLEAN, DATE, DATETIME, DOUBLE, FILE, GEOCODE, LONG, REFERENCE, RICH_TEXT, STRING, USER.
     * 
     * The **type** can also be **COMPOSITE** to creates a fieldset around a few fields. 
     * If so, a **composition** field must recursively describe child elements.
     * A composite field can also be a repeater of fields if it do have a **repeater** field.
     * A repeatable composite also need the following field
     * 
     * - String **add-label**: The label to display on the add button
     * - String **del-label**: The label to display on the delete button
     * - String **headerLabel**: The label to display on the repeater itselft
     * - Number **minSize**: The optional minimum size of the repeater. For example 2 means it will at least be repeated twice. 0 if not specified.
     * - Number **maxSize**: The optional maximum size of the repeater. Default value is infinite.
     * - Number **initial-size**: The optional size when loading the form (must be between minSize and maxSize). minSize is the default value.
     * 
     * The object **validation** field is a field validator.
     * Can be an object with the optionnal properties 
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
     * The string **widget** specify the widget to use. This is optionnal to use the default widget for the given type, multiple and enumeration values.
     * The widgets are selected using the js class {@link Ametys.form.WidgetManager} and the extension point org.ametys.runtime.ui.widgets.WidgetsManager.
     * Note that you can transmit additionnal configuration to all widgets using #cfg-additionnalWidgetsConf 
     * 
     * The optionnal object **widget-params** will be transmitted to the widget configuration : values depends on the widget you did select.
     * 
     * The boolean **hidden** will hide this field.
     * 
     * The boolean **can-not-write** makes the field in read only mode.
     * 
     * The object **annotations** is an array of object to describe available XML annotations on a richtext.
     * Each item is an object with properties : **name** (the XML tagname), **label** (the label of the button to set this annotation, defaults to name) and **description** (the help text associated to the button).
     * Exemple: annotations: [ { name: "JUSTICE", label: "Justice term", description: "Use this button to annotate the selected text as a justice term" } ] 
     * 
     * The **fieldname "fieldsets"** is a reseved keyword (if type is not specified) to create a graphical grouping of fields. This only work at root of data. 
     * Its value is an array of configuration with attributes:
     * 
     * - String **role** Can be "tabs" or "fieldsets" to create a tab grouping or a fieldset grouping. Note that tab grouping can be replaced by simple panels according to a user preference.
     * - String  **label** The label of the grouping.
     * - Object **elements** The child elements of the grouping : this is a recursive data object, except that "fieldset" can not be used again.
     * 
     * @param {Object/HTMLElement} data The data to create the form structure. Can be a JSON object  or an XML HTMLElement.
     */
    configure: function(data)
    {
        this.destroyComponents();
        this._init();
        
        if (this._isElement(data))
        {
            this._configureXML(data);
        }
        else
        {
            this._configureJSON(data);
        }
    },
	
	/**
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
			ct = ct || this._getFormContainer();
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
						var role = fieldsets[i].role;
						if (role == 'fieldset')
				        {
					        var fieldset = this._addFieldSet(ct, fieldsets[i].label, nestingLevel);
					        
			                // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
			                var finalOffset = offset 
			                				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET
			                				+ (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
			                				+ 1;
			                var finalROffset = roffset 
					        				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET
					        				+ 1;
					        this._configureJSON(fieldsets[i].elements, prefix, fieldset, finalOffset, finalROffset);
				        }
					    else // role = tab
					    {
							tabs.push(fieldsets[i]);
					    }
					}
				}
				else if (type == 'composite')
				{
					if (!data[name].repeater)
					{
						var fieldset = this._addFieldSet(ct, data[name].label, nestingLevel);
		                // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
		                var finalOffset = offset 
		                				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET 
		                				+ (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
		                				+ 1; 
		                var finalROffset = roffset 
					    				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET
					    				+ 1;
		                this._configureJSON(data[name].composition, prefix + name + '.', fieldset, finalOffset, finalROffset);
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
					    		
					    		nestingLevel: nestingLevel,
			    		        offset: offset,
			    		        roffset: roffset
						}
		                    
					    this._addRepeater (ct, repeaterCfg, repeater['initial-size'] || 0);
					}
				}
				else
				{
					var metadata = data[name];
					
					var label = metadata.label;
					var description = metadata.description;
					var isMandatory = metadata.validation ? (metadata.validation.mandatory) || false : false;
					
					var widgetCfg = {
						name: prefix + name,
						shortName: name,
						type: type,
						
						fieldLabel: this.withTitleOnLabels ? '<span title="' + label + '">' + (isMandatory ? '* ' : '') + label + '</span>' : (isMandatory ? '* ' : '') + label,
						ametysDescription: description || '',
						showAmetysComments: this.showAmetysComments,
						
						mandatory: isMandatory,
						value: metadata['default-value'],
						
						multiple: metadata.multiple,
						widget: metadata.widget,
						
						hidden: metadata.hidden, 
						disabled: metadata['can-not-write'] === true,
						
						form: this,
						offset: offset,
						roffset: roffset
					};

                    // Add configured configuration
                    Ext.Object.each(this._additionnalWidgetsConf, function(key, value, object) {
                        widgetCfg[key] = metadata[value];
                    });
					
					if (metadata.validation)
					{
						var validation = metadata.validation;
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
					
					if (metadata.enumeration)
					{
						var enumeration = [];
						
						var entries = metadata.enumeration;
						for (var j=0; j < entries.length; j++)
						{
							enumeration.push([entries[j].value, entries[j].label]);
						}
						
						widgetCfg.enumeration = enumeration;
					}
					
					if (metadata['widget-params'])
					{
						widgetCfg = Ext.merge (widgetCfg, metadata['widget-params']);
					}
					
	                if (metadata.annotations)
	                {
	                	var annotations = [];
	                	
	                	var entries = metadata.annotations;
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
					
					this._addInputField(ct, widgetCfg);
				}
			}
			
			if (tabs.length > 0)
			{
				var tabPanel = this._addTab();

				for (var i=0; i < tabs.length; i++)
				{
					var tab = this._addTabItem (tabPanel, tabs[i].label || "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_UNTITLED'/>");
					
					// Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
					var finalOffset = offset 
									+ Ametys.form.ConfigurableFormPanel.PADDING_TAB 
									+ 1; 
					var finalROffset = roffset 
									+ Ametys.form.ConfigurableFormPanel.PADDING_TAB 
									+ 1; 
					this._configureJSON (tabs[i].elements, prefix, tab, finalOffset, finalROffset);
				}
			}
	},
	
	/**
	 * This function creates and add form elements from a XML definition
	 * @param {HTMLElement} metadataNode The XML definition of the form fields.
	 * @param {String} prefix The input prefix to concatenate to input name
	 * @param {Ext.Element} [ct=this] The container where to add the repeater
	 * @param {Number} [offset=0] The field offset.
	 * @param {Number} [roffset=0] The field right offset.
	 */
	_configureXML: function (metadataNode, prefix, ct, offset, roffset)
	{
            prefix = prefix || this.getFieldNamePrefix();
			ct = ct || this._getFormContainer();
			offset = offset || 0;
			roffset = roffset || 0;
			
			var nodes = Ext.dom.Query.selectDirectElements(null, metadataNode);
			
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
				        var fieldset = this._addFieldSet(ct, label, nestingLevel);
		                // Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
		                var finalOffset = offset 
					    				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET 
					    				+ (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
					    				+ 1; 
		                var finalROffset = roffset 
					    				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET 
					    				+ 1; 
				        this._configureXML(nodes[i], prefix, fieldset, finalOffset, finalROffset);
			        }
				    else // role = tab
				    {
						tabs.push(nodes[i]);
				    }
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
					    				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET 
					    				+ (nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET : 0)
					    				+ 1; 
		                var finalROffset = roffset 
					    				+ Ametys.form.ConfigurableFormPanel.PADDING_FIELDSET 
					    				+ 1; 
		                this._configureXML(Ext.dom.Query.select("> composition", nodes[i]), prefix + name + '.', fieldset, finalOffset, finalROffset);
			        }
				}
				else if (type != '')
				{
					var isMandatory = Ext.dom.Query.selectValue("> validation > mandatory", nodes[i]) == 'true';
					
					var widgetCfg = {
						name: prefix + name,
						metadataName: name,
						type: Ext.dom.Query.selectValue("> type", nodes[i]),
						
						fieldLabel: (isMandatory ? '* ' : '') + label,
						ametysDescription: Ext.dom.Query.selectValue("> description", nodes[i], ''),
						showAmetysComments: this.showAmetysComments,
						
						mandatory: isMandatory,
						regexp: Ext.dom.Query.selectValue("> validation > regexp", nodes[i], null),
						
						multiple: Ext.dom.Query.selectValue("> multiple", nodes[i]) == 'true',
						widget: Ext.dom.Query.selectValue("> widget", nodes[i], null),
						disabled: Ext.dom.Query.selectValue("> can-not-write", nodes[i]) == 'true',
						
						form: this,
						offset: offset,
						roffset: roffset
					};
                    
                    // Add configured configuration
                    Ext.Object.each(this._additionnalWidgetsConf, function(key, value, object) {
                        widgetCfg[key] = Ext.dom.Query.selectValue('> ' + value, nodes[i]);
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
					var entries = Ext.dom.Query.select("> enumeration > *", nodes[i]);
					for (var j=0; j < entries.length; j++)
					{
						enumeration.push([entries[j].getAttribute("value"), Ext.dom.Query.selectValue("", entries[j])]);
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
					
					this._addInputField(ct, widgetCfg);
				}
			}
			
			if (tabs.length > 0)
			{
				var tabPanel = this._addTab();
	
				for (var i=0; i < tabs.length; i++)
				{
					var tab = this._addTabItem (tabPanel, Ext.dom.Query.selectValue('> label', tabs[i], "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TAB_UNTITLED'/>"));
					// Transmit offset + 5 (padding) + 1 (border) + 11 (margin + border) if we are in a nested composite.
					var finalOffset = offset 
									+ Ametys.form.ConfigurableFormPanel.PADDING_TAB 
									+ 1; 
					var finalROffset = roffset 
									+ Ametys.form.ConfigurableFormPanel.PADDING_TAB 
									+ 1; 
					this._configureXML (tabs[i], prefix, tab, finalOffset, finalROffset);
				}
			}
	},
    
    /**
     * Fill a configured form with values. #configure must have been call previously with data corresponding to these data.
     * 
     * The data can be an XML or a JSON object.
     * 
     * The XML format
     * ==============
     * See the following strucure:
     * 
     * 
     *      <myrootnode>
     *          <metadata>
     *              <fieldname json="false" value="3"/>
     *              <!-- ... -->
     *          </metadata>
     *          
     *          <comments>
     *              <metadata path="fieldname">
     *                  <comment id="1" date="2020-12-31T23:59:59.999+02:00">
     *                      My comment for the field <fieldname>
     *                  </comment>
     *                  <!-- ... -->
     *              </metadata>
     *              <!-- ... -->
     *          </comments>
     *      </myrootnode>
     * 
     * 
     * For the values, the tag **metadata** is the wrapping for tags holdings the values:
     * 
     * - the tag name is the name of the field concerned (without prefix).
     * - thoses tags are recursive for sub-field (child of composites).
     * - for repeaters, an attribute **entryCount** is set on the tag, its value is the size of the repeater. Each entry is encapsulated in an **entry** tag with an attribute **name** that worth the position (1 based) of the entry.
     * - the attribute **json** set to true means the value will be interpreted as JSON before being set on the field
     * - the value itself can be either the value of the attribute **value**, or the text of the tag
     * - multiple values are set by repeating the tag.
     *   
     * For the **comments**:
     * 
     * - the **metadata** are not recursive
     * - the **path** attribute contains the fieldname (without prefix) with '/' separator for sub-fields (child of composites). For repeaters, you also have to add the position of the repeater to modify. Exemple: path="mycompositefield/myrepeater/2/myfield"
     * - the **comment** tag have the following mandatories attributes :
     *   - **id** The number of the comment
     *   - **date** The date of the comment using the ISO 8601 format (will use the Ext.Date.patterns.ISO8601DateTime parser).
     *   - **author** The fullname of the author of the comment.
     *   
     *  Here is a full example:
     *  
     *  
     *      <myrootnode>
     *          <metadata>
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
     *          </metadata>
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
     * Most information here is common with the XML format, so please starts by reading it above.
     * 
     * The **values** array will fill the fields. Unlike in XML the information of the size of the repeaters is not set in this field.
     * The **repeaters** array allow to know the size of every repeaters. Each element is an object with:
     * 
     * - a string **name** The name of the repeater
     * - a string **prefix** The path to this repeater ('.' separated)
     * - a number **count** The size of the repeater
     * 
     * The JSON format also accept a **invalid** field, to pre-fill fields with raw values. For exemple, you can pre-fill a date field with a non date string.
     * The **invalid** values should not set the same values already brought by **values**, but they will replace them in such a case.
     * 
     * @param {Object/HTMLElement} data The object that will fill the form.
     */
    setValues: function(data)
    {
        if (this._isElement(data))
        {
            this._setValuesXML(data);
        }
        else
        {
            this._setValuesJSON(data);
        }
    },
	
	/**
	 * This function set the values of form fields from a XML dom.
	 * @param {HTMLElement} xml The XML dom
     * @private
	 */
	_setValuesXML: function (xml)
	{
		this._initializeTabsStatus();
		
	    var metadataNodes = Ext.dom.Query.select("*/metadata/*", xml);
	    for (var i=0; i < metadataNodes.length; i++)
	    {
	        this._setValuesXMLMetadata(metadataNodes[i], this.getFieldNamePrefix() + metadataNodes[i].tagName);
	    }
	    
	    this._setValuesXMLComments(Ext.dom.Query.selectNode("*/comments", xml));
	    
	    this._formReady = true;
	    this.fireEvent('formready', this);
	    
	    this._updateTabsStatus(true);
	},
	
	/**
	 * Set the values of form fields from an automatic backup.
	 * @param {Object} data The backup data object.
	 * @param {Object[]} data.repeaters The repeater item counts.
	 * @param {Object} data.values The valid field values.
	 * @param {Object} data.invalid The invalid field values.
     * @private
	 */
	_setValuesJSON: function(data)
	{
		this._initializeTabsStatus();
		
	    // Sort repeaters to get parent repeaters first.
	    var sortedRepeaters = Ext.Array.sort(data.repeaters, function(rep1, rep2) {
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
	        var repeaterPanel = this.down("panel[isRepeater][name='" + name + "'][prefix='" + prefix + "']");
	        
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
	                    repeaterPanel.addRepeaterItem({animate: false, collapsed: collapsed});
	                }
                }
            }
        }
	    
	    // Set the field values.
	    this._setValuesJSONForField(data.values);
	    
	    // Set the invalid field values (raw mode) and validate the fields afterwards.
	    this._setValuesJSONForField(data.invalid, true, true);
	    
	    // Set the field comments.
	    this._setMetadataCommentsJSON(data.comments)
        
        this._formReady = true;
        this.fireEvent('formready', this);
        
        this._updateTabsStatus(true);
	},
	
	/**
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
            
            try
            {
                if (!Ext.isNumeric(value))
                {
                    decodedValue = Ext.JSON.decode(value);
                }
            }
            catch (e)
            {
                // Ignore, just take the undecoded value.
                // FIXME this do logs an error now!
            }
            
            var field = this.getField(this.getFieldNamePrefix() + name);
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
	 * @private Sets a data values into the field
	 * @param {HTMLElement} metadataNode The DOM node representing the metadata value
	 * @param {String} fieldName The name of concerned field
	 */
	_setValuesXMLMetadata: function (metadataNode, fieldName)
	{
        var metaName = metadataNode.tagName;
        var prefix = fieldName.substring(0, fieldName.lastIndexOf('.') + 1);
	    var childNodes = Ext.dom.Query.selectDirectElements(null, metadataNode);
        var repeaterItemCount = Ext.dom.Query.selectNumber('@entryCount', metadataNode, -1);
        var repeaterPanel = this.down("panel[isRepeater][name='" + metaName + "'][prefix='" + prefix + "']");
	    
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
                        repeaterPanel.addRepeaterItem({previousPosition: i, animate: false, collapsed: collapsed});
                    }
                    else
                    {
                        // Set the previous position.
                        repeaterPanel.setItemPreviousPosition(i, i);
                    }
                    
                    this._setValuesXMLMetadata(childNodes[i], fieldName + '.' + entryPos);
                }
            }
        }
        else if (childNodes.length == 0)
		{
        	// Non-composite metadata.
        	var previousSibling = metadataNode.previousElementSibling || metadataNode.previousSibling;
        	if (!previousSibling || previousSibling.tagName != metadataNode.tagName)
        	{
    			var values = this._getValues (metadataNode);
    			
    			var field = this.getField(fieldName);
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
				this._setValuesXMLMetadata(childNodes[i], fieldName + '.' + childNodes[i].tagName);
			}
		}
	},
	
	/**
	 * @private Gets data values from DOM
	 * @param {HTMLElement} metadataNode The DOM node representing the metadata value
	 * @return {String[]} A array of values
	 */
	_getValues: function (metadataNode)
	{
		var values = [];
		var nodes = Ext.dom.Query.select(metadataNode.tagName, metadataNode.parentNode);
		for (var i=0; i < nodes.length; i++)
		{
			var value = nodes[i].getAttribute('value') == null ? Ext.dom.Query.selectValue('', nodes[i], '') : nodes[i].getAttribute('value');
			value = value || '';
			if (value.length > 0)
			{
				values.push(value);
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
		
		var repeaters = this.getRepeaters();
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
			var fd = this.getField(this._fields[i]);
			if (!fd.isValid())
			{
				invalidFields.push(fd.getFieldLabel());
			}
			else
			{
				fd.clearInvalid();
			}
		}
		
		this._updateTabsStatus(false);
		return invalidFields;
	},
	
	/**
	 * Get the fields with warnings
	 * @return {String[]} The names of the fields with warnings
	 */
	getWarnedFields: function()
	{
		var invalidFields = [];
		for (var i = 0; i < this._fields.length; i++)
		{
			var fd = this.getField(this._fields[i]);
			if (fd.getActiveWarning() != null)
			{
				invalidFields.push(fd.getFieldLabel());
			}
		}
		
		this._updateTabsStatus(false);
		return invalidFields;
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
			field = this.getField(name);
			
			// remove the field name prefix and replace '.' by '/' to compute the path
			path = name.substring(this.getFieldNamePrefix().length).replace(/\./g, '/');
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
	 * Set the field comments from an object.
	 * @param {Object} commentsMap The object containing the comments, indexed by name.
	 */
	_setMetadataCommentsJSON: function(commentsMap)
	{
		var fieldComments, field;
		for (var name in commentsMap)
		{
			fieldComments = commentsMap[name];
			field = this.getField(name);
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
				title: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DIALOGBOX_TITLE'/>",
				icon: Ametys.CONTEXT_PATH + '/kernel/img/Ametys/common/form/configurable/edit_comment_16.png',
				
				autoScroll: true,
				cls: 'ametys-dialogbox',
				width: 350,
				
				layout: 'anchor',
				defaults: {
					cls: 'ametys',
					anchor: '100%'
				},
				
				items: [{
					xtype: 'textarea',
					height: 100
				}],
				
				closeAction: 'hide',
				defaultFocus: 'button-ok',
				buttons : [{
					itemId: 'button-ok',
					text: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_BTN_OK'/>",
					handler: Ext.emptyFn
				}, {
					text: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_BTN_DELETE'/>",
					handler: Ext.emptyFn,
					hidden: true
				}, {
					text: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_BTN_CANCEL'/>",
					handler: Ext.bind(function() {this._commentsBox.close();}, this) 
				}]
			});
		}
		
		var fdLabel = field.getFieldLabel();
		if (fdLabel.indexOf('* ') != -1)
		{
			fdLabel = fdLabel.substring(2);
		}
		this._commentsBox.setTitle ("<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DIALOGBOX_TITLE'/>" + ' - ' + fdLabel);
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
			"<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DELETE_CONFIRM_TITLE'/>",
			"<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_COMMENTS_DELETE_CONFIRM_MSG'/>",
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
					msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE'/>"
				});
				this._mask.show();
				
				Ext.defer(this._reloadTabs, 1, this);
			}
		}
	},
	
	/**
	 * Change the way tabs are displayed given the new requested policy.
	 */
	_reloadTabs: function()
	{
		// Suspend layout update.
		this.suspendLayouts();
		
		var oldTabPanels = this._tabPanels || [];
		this._tabPanels = [];
		
		var newPanel;
		Ext.Array.forEach(oldTabPanels, function(panel) {
			newPanel = this._replacePanel(panel);
		}, this);
		
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
	 * @param {Ext.panel.Panel/Ext.tab.Panel} panel the tab container to replace
	 * @return {Ext.panel.Panel/Ext.tab.Panel} the new tab container
	 * @private
	 */
	_replacePanel: function(panel)
	{
		// New panel creation
		var newPanel = this._addTab();
		
		// Replace and add items to the new panel
		var items = panel.items.getRange();
		
		var newItem;
		Ext.Array.forEach(items, function(item) {
			var header = item.tab ? item.tab : item.getHeader();
			var headerCls = header.hasCls('empty') ? 'empty' : '';
			
			var newItem = this._replacePanelItem(newPanel, item, headerCls);
		}, this);
		
		// Add new panel / Remove the old one.
		this._getFormContainer().remove(panel);
		
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
		
		// Add each child of the item to the new item.
		var children = tabItem.items.getRange();
		Ext.Array.forEach(children, function(child) {
			newItem.add(child);
			tabItem.remove(child);
		}, this);
		
		return newItem;
	}
});
