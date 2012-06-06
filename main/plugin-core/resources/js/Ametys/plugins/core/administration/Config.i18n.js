/*
 *  Copyright 2012 Anyware Services
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

Ext.define('Ametys.plugins.core.administration.Config', {
	singleton: true,
	
	/**
	 * @property {Number} LABEL_WIDTH The width for labels
	 * @readonly 
	 */
	LABEL_WIDTH: 230,
	/**
	 * @property {Number} FIELD_WIDTH The width for fields
	 * @readonly 
	 */
	FIELD_WIDTH: 250 + 20,
	
	/**
	* @property {Ext.form.Field[]} _fields The configuration fields
	* @private
	 */
	_fields: [],

	/**
	 * @property {Ext.form.BasicForm} _form The configuration form
	 */
	/**
	 * @property {Object []} _navItems The navigation items
	 * @private
	 */
	/**
	 * @property {String} pluginName The plugin loading this class
	 * @private
	 */
	/**
	 * @property {Boolean} bound True if the scrolling panel is bound to the toc
	 * @private
	 */
	/**
	 * @property {Ext.Element} _ct The element containing doing the scroll in 
	 * @private
	 */
	
	/**
	 * @property {Object} _widgets The registered widgets for the config screen. The widgets are js class name for widgets.
	 * @private
	 */
	_widgets: {
		'hour': 'Ametys.plugins.core.administrator.Config.HourField',
		'time': 'Ametys.plugins.core.administrator.Config.TimeField',
		'textarea': 'Ametys.plugins.core.administrator.Config.TextareaField'
	},
		
	/**
	 * Initialize the class
	 * @param {String} pluginName The name of the plugin loading this feature
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;

		this._bound = true;
	},

	/**
	 * Creates the main panel
	 * @return {Ext.panel.Panel} The panel created
	 */
	createPanel: function ()
	{
		// The form
		this._form  = new Ext.form.FormPanel({
			region: 'center',
			
			baseCls: 'transparent-panel',
			bodyStyle: 'position:relative;',
				
			border: false,
			autoScroll : true,
			
			id : 'config-inner',
			formId : 'save-config',

			html: ''
		});
		
		this._contextualPanel = new Ext.Container({
			region:'east',
		
			cls : 'admin-right-panel',
			border: false,
			autoScroll: true,
			width: 277,
		    
			items: [this._drawNavigationPanel (),
			        this._drawHandlePanel (),
			        this._drawHelpPanel ()]
		});
		
		return new Ext.Panel({
			autoScroll: false,
			
			baseCls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			items: [this._form , 
			        this._contextualPanel],
			        
			listeners: {
				'afterrender': Ext.Function.bind(this._onAfterRender, this)
			}
		});
	},
	
	_onAfterRender: function() {
		this._ct = Ext.getCmp("config-inner").getEl().child("div:first").child("*:first");
		this._ct.on('scroll', Ext.Function.bind(this._calcScrollPosition, this));
		
		this._calcScrollPosition();
	},

	/**
	 * Creates a fielset with this id and label
	 * @param {String} id The id of the new fieldset
	 * @param {String} label The label of the fieldset
	 * @return {Ext.form.FieldSet}
	 */
	createFieldSet: function (id, label)
	{
		return new Ext.panel.Panel({
			id : id,
			title : label,
			
			collapsible: true,
			titleCollapse: true,
			hideCollapseTool: true,
			
			border: false,
			shadow: false,
			layout: 'vbox',
			
			width: Ametys.plugins.core.administration.Config.LABEL_WIDTH 
					+ Ametys.plugins.core.administration.Config.FIELD_WIDTH 
					+ 100
		});
	},

	/**
	 * Add a groups category
	 * @param {Ext.form.FieldSet} fd The fieldset where to add the category
	 * @param {String} name The name of the category
	 */
	addGroupCategory: function (fd, name)
	{
		fd.add (new Ext.Container ({
			html: name,
			cls: 'ametys-subcategory'
		}));
	},

	/**
	 * Add an input field to the form
	 * @param {Ext.Element} ct The container where to add the input
	 * @param {String} type The type of the field to create
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {String[]} enumeration The list of values if applyable (only for type text)
	 * @param {String} widget The widget to use for edition. Can be null
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	addInputField: function (ct, type, name, value, label, description, enumeration, widget, mandatory, regexp, invalidText)
	{
	    var field = null;
	    
		if (enumeration != null)
		{
		    field = this._createTextField (name, value, label, description, enumeration, mandatory == 'true', null);
		}
		else
		{
			switch (type) 
			{
				case 'double':
					field = this._createDoubleField (name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
					break;
				case 'long':
					field = this._createLongField (name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
					break;
				case 'password':
					field = this._createPasswordField (name, value, label, description);
					break;
				case 'date':
					field = this._createDateField (name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
					break;
				case 'boolean':
					field = this._createBooleanField (name, value, label, description);
					break;
				default:
					if (widget != '')
					{
						 field = this._createWidgetField(widget, name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
					}
					else
					{
						field = this._createTextField (name, value, label, description, null, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
					}
					break;
			}
		}
		
		if (field != null)
	    {
		    ct.add(field);
	    }
		
		this._fields.push(name);
		
		return field;
	},

	/**
	 * @private
	 * Creates a double field
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	_createDoubleField: function (name, value, label, description, mandatory, regexp, invalidText)
	{
		return new Ext.form.field.Number({
			name: name,
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        value: value,
	        allowBlank: !mandatory,
	        validatorRegexp: regexp,
	        validator: function(value)
			{
				if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return true;
				}
				else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return this.blankText;
				}
				else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
				{
					return this.invalidText != null ? this.invalidText : this.regexText;
				}
				else
				{
					return true;
				}
			},
			regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
			invalidText: invalidText != null ? invalidText : null,
	        msgTarget: 'side',
	        
	    	labelAlign: 'right',
	        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
	        labelSeparator: '',
	        width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a long field
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	_createLongField: function (name, value, label, description, mandatory, regexp, invalidText)
	{
		return new Ext.form.field.Number ({
			name: name,
			fieldLabel: label,
	        ametysDescription: description,
	        
	        allowDecimals: false,
	        
	        value: value,
	        allowBlank: !mandatory,
	        validatorRegexp: regexp,
	        validator: function(value)
			{
				if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return true;
				}
				else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return this.blankText;
				}
				else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
				{
					return this.invalidText != null ? this.invalidText : this.regexText;
				}
				else
				{
					return true;
				}
			},
			regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
			invalidText: invalidText != null ? invalidText : null,
			msgTarget: 'side',
			
			labelAlign: 'right',
	        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
	        labelSeparator: '',
	        width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a password field
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	_createPasswordField: function (name, value, label, description)
	{
		return new Ametys.form.field.ChangePassword({
			name: name,
			fieldLabel: label,
		    ametysDescription: description,
		    
		    value: value,
		    
			labelAlign: 'right',
	        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
	        labelSeparator: '',
		    width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a date field
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	_createDateField: function (name, value, label, description, mandatory, regexp, invalidText)
	{
	    var dateValue = value;
//	    if (typeof value == 'string')
//	    {
//	        dateValue = Date.parseDate(value, 'c');
//	    }
	    
		return new Ext.form.field.Date ({
			name: name,
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        altFormats: 'c',
	        value: dateValue,
	        allowBlank: !mandatory,
	        validatorRegexp: regexp,
	        validator: function(value)
			{
				if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return true;
				}
				else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return this.blankText;
				}
				else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
				{
					return this.invalidText != null ? this.invalidText : this.regexText;
				}
				else
				{
					return true;
				}
			},
			regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
			invalidText: invalidText != null ? invalidText : null,
			msgTarget: 'side',
			
			labelAlign: 'right',
	        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
	        labelSeparator: '',
	        width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a checkbox
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @return {Ext.form.field.Field} The created field
	 */	
	_createBooleanField: function (name, value, label, description)
	{
		return new Ext.form.field.Checkbox ({
			name: name,
			 
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        checked: (value == "true"),
	        
	    	labelAlign: 'right',
	        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
	        labelSeparator: '',
	        width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a text field
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {String[]} enumeration The list of values if applyable (only for type text)
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	_createTextField: function (name, value, label, description, enumeration, mandatory, regexp, invalidText)
	{
		if (enumeration != null)
		{
			return new Ext.form.field.Combo ({
				name: name,
				
		        fieldLabel: label,
		        ametysDescription: description,
		        value: value,
		        
		    	labelAlign: 'right',
		        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        labelSeparator: '',
		        width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        
		        allowBlank: !mandatory,
		        msgTarget: 'side',
		        
		        mode: 'local',
		        editable: false,
		        forceSelection: true,
				triggerAction: 'all',
		        store: new Ext.data.SimpleStore({
		            id: 0,
		            fields: [ 'value', 'text'],
		            data: enumeration,
		            sortInfo: {field: 'text'} // default order
		        }),
		        valueField: 'value',
		        displayField: 'text'
			});
		}
		else
		{
			return new Ext.form.field.Text ({
				name: name,
				
		        fieldLabel: label,
		        ametysDescription: description,
		        value: value,
		        
		    	labelAlign: 'right',
		        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        labelSeparator: '',
		        width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        
		        allowBlank: !mandatory,
		        validatorRegexp: regexp,
		        validator: function(value)
				{
					if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
					{
						return true;
					}
					else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
					{
						return this.blankText;
					}
					else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
					{
						return this.invalidText != null ? this.invalidText : this.regexText;
					}
					else
					{
						return true;
					}
				},
				regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
				invalidText: invalidText != null ? invalidText : null,
				msgTarget: 'side'
			});
		}
	},

	/**
	 * @private
	 * Creates a field based upon its widget id
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @return {Ext.form.field.Field} The created field
	 */
	_createWidgetField: function (widgetId, name, value, label, description, mandatory, regexp, invalidText)
	{
		var widgetCfg = {
				name: name,
		        fieldLabel: label,
		        ametysDescription: description,
		        
		        value: value,
		        allowBlank: !mandatory,
		        validatorRegexp: regexp,
		        validator: function(value)
				{
					if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
					{
						return true;
					}
					else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
					{
						return this.blankText;
					}
					else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
					{
						return this.invalidText != null ? this.invalidText : this.regexText;
					}
					else
					{
						return true;
					}
				},
				regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
				invalidText: invalidText != null ? invalidText : null,
		        msgTarget: 'side'
		};
		
		var widgetClass = this._widgets[widgetId];
		return eval('new ' + widgetClass + '(widgetCfg)');
	},

	/**
	 * Draw the navigation panel. This function needs the this._navItems was filled first.
	 * @return {Ametys.workspace.admin.rightpanel.NavigationPanel} The navigation panel
	 * @private
	 */
	_drawNavigationPanel: function ()
	{
		this._nav = new Ametys.workspace.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
		
		for (var i=0; i &lt; this._navItems.length; i++)
		{
			var item = new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
				id : "a" + this._navItems[i].id,
				text: this._navItems[i].label,
				
				divToScroll: this._navItems[i].id,
				ctToScroll:  'config-inner',
				
				bindScroll: this._bindScroll,
				unbindScroll:  this._unbindScroll,
				
				toggleGroup : 'config-menu'
			});
			
			this._nav.add(item);
		}
		
		return this._nav;
	},

	/**
	 * Draw the actions panel.
	 * @return {Ametys.workspace.admin.rightpanel.Action} The action panel
	 * @private
	 */
	_drawHandlePanel: function ()
	{
		this._actions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE"/>"});
		
		// Save action
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_SAVE"/>", 
						 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/config/save.png',
						 Ext.Function.bind(this.save, this));
		
		// Quit action
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_QUIT"/>", 
						 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/config/quit.png',
						 Ext.Function.bind(this.goBack));

		return this._actions;
	},

	/**
	 * Draw the help panel.
	 * @return {Ametys.workspace.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP"/>"});
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT"/>");
		
		return helpPanel;
	},

	/**
	 * Quit the screen
	 * @param {Boolean} mask Displaying a mask while quitting. False by default
	 */
	goBack: function (mask)
	{
		if (mask)
		{
			 new Ext.LoadMask(Ext.getBody()).show();
		}
	    document.location.href = Ametys.WORKSPACE_URI;
	},

	/**
	 * Save configuration
	 */
	save: function ()
	{
		if (!this._form.getForm().isValid())
		{
			Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID"/>");
			this._form.getForm().markInvalid();
			return;
	    }
	    
	    this.save._mask = new Ext.LoadMask(Ext.getBody());
	    this.save._mask.show();
	    Ext.defer(this.save2, 1, this);
	},
	save2: function ()
	{
	    var url = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/config/set";

	    var argsObj = this._form.getForm().getValues();

	    var result = null;
	    var ex = "";
	    try
	    {
	    	result = Ext.Ajax.request({url: url, params: argsObj, async: false});
	    }
	    catch (e)
	    {
	    	ex = "" + e;
	    }
	    
	    this.save._mask.hide();

		if (result == null)
	    {
			Ametys.log.ErrorDialog.display("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", 
	    			"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_FATALERROR"/>",
	    			ex,
	    			"Ametys.plugins.core.administration.Config.save");
	        return;
	    }
	    result = result.responseXML;
	    
	    var error = Ext.dom.Query.selectValue("*/error", result);
	    if (error != null &amp;&amp; error != "")
	    {
	    	Ext.Msg.show ({
	    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
	    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_ERROR"/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
	    	});
	        return;
	    }
	    
	    Ext.Msg.show ({
	    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
	    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_OK"/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.INFO,
				fn: Ext.Function.bind(this.goBack, this, [true])
	    });
	},

	/**
	 * Bind the scroll to the toc
	 */
	_bindScroll: function ()
	{
		this._bound = true;
	},
	/**
	 * Stop binding the scroll to the toc
	 */
	_unbindScroll: function ()
	{
		this._bound = false;
	},

	/**
	 * @private
	 * Activates the correct item menu relatively to the current scroll position 
	 */
	_calcScrollPosition: function ()
	{
		if (!this._bound)
			return;
			 
		var last;
		var min = 0;
		
		var e = this._form.getEl().first();
		var max = e.dom.scrollHeight - e.getHeight();
		
		var scrollPosition = e.dom.scrollTop;
		var p = (scrollPosition - min) / (max - min);
		p = p * this._form.body.getHeight();
		
		var a0 = Ext.get(this._ct.dom.children[0]).getTop();
		
		for (var i=0;  i &lt; this._ct.dom.children.length; i++)
		{
			var anchor = this._ct.dom.children[i];
			if (i > 0) 
			{
				last = this._ct.dom.children[i-1];
			}
			else 
			{
				last = anchor;
			}
			var posY = Ext.get(anchor).getTop() - a0;
			if(posY >= scrollPosition + p)
			{
				this._activateItemMenu(last.dom.name);
				return;
			}
		
		}
		this._activateItemMenu(this._ct.dom.children[this._ct.dom.children.length - 1].name);
	},

	/**
	 * Do activates a menu item by its id
	 * @param {String} id The menu id to activate
	 * @private
	 */
	_activateItemMenu: function (id)
	{
		var button = Ext.getCmp("a" + id);
		if	(button != null)
		{	
			button.toggle(true);
		}
	}
});

/**
 * @private
 * A class to define a local field that handle hours by displaying only hours
 */
Ext.define('Ametys.plugins.core.administrator.Config.TimeField',	{
	extend: 'Ext.form.field.Time',
	
	increment: 60,
	labelAlign: 'right',
    labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
    labelSeparator: '',
	width: 100
});
/**
 * @private
 * A class to define a local field that handle hours by displaying only hours and that forbidden modifications
 */
Ext.define('Ametys.plugins.core.administrator.Config.HourField',	{
	extend: 'Ametys.plugins.core.administrator.Config.TimeField',
	
	increment: 60,
	edtiable: false,
	labelAlign: 'right',
    labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
    labelSeparator: '',
    width: 100
});
/**
 * @private
 * A class to define a local field that handle texts
 */
Ext.define('Ametys.plugins.core.administrator.Config.TextareaField',	{
	extend: 'Ext.form.field.TextArea',
	
	labelAlign: 'right',
    labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
    labelSeparator: '',
	width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,
    height: 80
});
