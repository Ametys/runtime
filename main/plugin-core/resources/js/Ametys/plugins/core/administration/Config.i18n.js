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
 * The configuration screen
 * @private
 */
Ext.define('Ametys.plugins.core.administration.Config', {
	singleton: true,
	
	/**
	 * @property {Number} LABEL_WIDTH The width for labels
	 * @private
	 * @readonly 
	 */
	LABEL_WIDTH: 230,
	/**
	 * @property {Number} FIELD_WIDTH The width for fields
	 * @private
	 * @readonly 
	 */
	FIELD_WIDTH: 250 + 20,
	
	/**
	* @property {Ext.form.Field[]} _fields The configuration fields
	* @private
	 */
	_fields: [],
	
	/**
	 * @property {Ext.form.FormPanel} _form The configuration form
	 * @private
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.NavigationPanel} _nav The navigation panel
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.ActionPanel} _actions The action panel
	 */
	/**
	 * @property {Object[]} _navItems The navigation items
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
			
			cls: 'transparent-panel',
			bodyStyle: 'position:relative;',
				
			border: false,
			autoScroll : true,
			
			id : 'config-inner',
			formId : 'save-config',
			
			fieldDefaults: {
				cls: 'ametys',
				labelAlign: 'right',
		        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        labelSeparator: ''
			},

			listeners: {
				'boxready': Ext.Function.bind(this._onFormReady, this)
			},
			
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
			
			cls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			items: [this._form , 
			        this._contextualPanel],
			        
	        listeners: {
				'boxready': Ext.Function.bind(this._onBoxReady, this)
			}        
		});
	},
	
	/**
	 * @private
	 * Listener when the form's box is ready
	 */
	_onFormReady: function() {
		
		var fieldsLength = this._fields.length;
		for (var i = 0; i < fieldsLength ; i++)
		{
			if (this._fields[i].disableCondition != null)
			{
				this._disableField(this._fields[i]);
				this._addListeners(this._fields[i].disableCondition, this._fields[i]);
			}
		}
	},
	
	/**
	 * @private
	 * Listener when the panel's box is ready
	 */
	_onBoxReady: function() {
		this._ct = Ext.getCmp("config-inner").getEl().child("div:first");
		this._ct.on('scroll', Ext.Function.bind(this._calcScrollPosition, this));
		
		this._calcScrollPosition();
	},
	
	/**
	 * Creates a fieldset with this id and label
	 * @param {String} id The id of the new field set
	 * @param {String} label The label of the field set
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
	 * @private
	 * Show or hide the elements of a group
	 * @param {HTMLElement} input The changed input 
	 * @param {String} newValue The new value of the input 
	 * @param {String} oldValue The preceding value of the input
	 * @param {String[]} elements The html names of the elements to show or hide
	 */
	_showHideGroup: function(input, newValue, oldValue, elements)
	{
		Ext.suspendLayouts();
		for (var i = 0; i < elements.length; i++)
		{
			this._form.getForm().findField(elements[i]).setVisible(newValue);
		}
		Ext.resumeLayouts(true);
	},

	/**
	 * Add a group category
	 * @param {Ext.form.FieldSet} fd The fieldset where to add the category
	 * @param {String} name The name of the category
	 * @param {Object} switcher If the group can be switched on/off, this object has the following keys : type, name, label, description, widget, mandatory, regexp and optionally invalidText and disableCondition.
	 * @param {String[]} subitems An array containing the names of the fields the switcher will show/hide 
	 */
	addGroupCategory: function (fd, name, switcher, subitems)
	{
		if (switcher != null)
		{
			var modifiedSwitcher = Ext.applyIf ({label: '', description: '', width: 'auto'}, switcher);
			
			var input = this.createInputField(modifiedSwitcher);
			input.on('change', Ext.bind(this._showHideGroup, this, [subitems], true));

			var items = [
			      input,
			      { flex: 1, baseCls: '', html: "<label for='" + input.getInputId() + "'>" + name + "</label>", padding: "3 0 0 5" }
            ];
			
			if (switcher.description != '')
			{
				items.push({
					cls: "ametys-description",
				    baseCls: '', 
				    padding: "3 0 0 5",
					html: '  ',
					width: 20,
					listeners: {
						'render': function() {
						    new Ext.ToolTip({
						        target: this.getEl(),
						        html: switcher.label + "<br/><br/>" + switcher.description
						    });
						}
					}
				});
			}
			
			fd.add (new Ext.Container({
				layout: 'hbox',
				cls: 'ametys-subcategory',
				minWidth: 250,
				items: items
			}));
		}
		else
		{
			fd.add (new Ext.Container ({
				cls: 'ametys-subcategory',
				html: name
			}));
		}
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
	_addListeners: function(disableCondition, disablingField)
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
				var field = this._form.getForm().findField(conditionList[i]['id']);
				field.on('change', Ext.bind(this._disableField, this, [disablingField], false));
			}
		}
	},
	
	/**
	 * @private
	 * Enables/disables the field.
	 * @param {Object} disablingField the field to(/not to) disable.
	 */
	_disableField: function(disablingField)
	{	
		var disable = this._evaluateDisableCondition(disablingField.disableCondition);
		disablingField.setDisabled(disable);
	},
	
	/**
	 * @private
	 * Evaluates the disable condition when a matching field is changing and enables/disables the field accordingly.
	 * @param {Object} disableCondition the disable condition.
	 */
	_evaluateDisableCondition: function(disableCondition)
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
		        var id = conditionList[i]['id'],
					op = conditionList[i]['operator'],
					val = conditionList[i]['value'];
					
		        var result = this._evaluateCondition(id, op, val);
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
	 * @return {Boolean} result true if the condition is verified, false otherwise.
	 */
	_evaluateCondition: function(id, operator, value)
	{
		var fieldValue = this._form.getForm().findField(id).getValue();
		switch (operator)
		{
			case "gt" : 
				return (fieldValue > value);
			case "geq" : 
				return (fieldValue >= value);
			case "eq" : 
				return (fieldValue == value);
			case "leq" : 
				return (fieldValue <= value);
			case "lt" : 
				return (fieldValue < value);
			case "neq" : 
				return (fieldValue != value);
			default:
				throw "Unknown operator " + operator;
				break;
		}
	},
	
	/**
	 * Add an input field to the form
	 * @param {Ext.Element} ct The container where to add the input
	 * @param {Object} config this object have the following keys : type, name, value, label, description, enumeration, widget, mandatory, regexp and optionally invalidText and width.<br/>
	 * @param {String} config.type The type of the field to create
	 * @param {String} config.name The name of the field (the one used to submit the request)
	 * @param {Object} config.value The value of the field at the creating time
	 * @param {String} config.label The label of the field
	 * @param {String} config.description The associated description
	 * @param {String[]} config.enumeration The list of values if applicable (only for type text)
	 * @param {String} config.widget The widget to use for edition. Can be null
	 * @param {Boolean} config.mandatory True if the field can not be empty
	 * @param {String} config.regexp The regexp to use to validate the field value
	 * @param {String} config.invalidText The text to display when the field value is not valid
	 * @param {Number/String} config.width Replace the default width with this one
	 * @param {Object} config.disableCondition the optional disable condition on this field
	 * @param {String} config.disableCondition.type the type of the disable condition, union (or) or intersection (and)
	 * @param {Object[]} config.disableCondition.conditions an array of conditions that can contain several condition objects or other conditions
	 * @param {Object[]} config.disableCondition.condition an array of condition objects
	 * @param {String} config.disableCondition.condition.id the id of the field that will be evaluated
	 * @param {String} config.disableCondition.condition.operator the operator used to evaluate the field 
	 * @param {String} config.disableCondition.condition.value the value on which the field will be compared to
	 * @param {String} startVisible Optional, if 'false' this field will be hidden 
	 * @return {Ext.form.field.Field} The created field
	 */
	addInputField: function (ct, config, startVisible)
	{
		var field = this.createInputField(config);
		if (startVisible == 'false')
		{
			field.hide();
		}
		
		if (field != null)
	    {
		    ct.add(field);
		    this._fields.push(field);
	    }

		return field;
	},
	
	/**
	 * Creates and returns an input field depending on the given configuration
	 * @param {Object} config this object have the following keys : type, name, value, label, description, enumeration widget, mandatory, regexp and optionally invalidText, width, disableCondition and switchable. See config in {#addInputField}
	 * @return {Ext.form.field.Field} The created field
	 * @private
	 */
	createInputField: function(config)
	{
	    var field = null;
	    
		if (config.enumeration != null)
		{
			field = this._createTextField (config.name, config.value, config.label, config.description, config.enumeration, config.mandatory == 'true', null, config.width, config.disableCondition);
		}
		else
		{
			if (config.mandatory)
			{
				config.label = '* ' + config.label;
			}
			
			switch (config.type) 
			{
				case 'double':
					field = this._createDoubleField (config.name, config.value, config.label, config.description, config.mandatory == 'true', config.regexp != '' ? new RegExp (config.regexp) : null, config.invalidText, config.width, config.disableCondition);
					break;
				case 'long':
					field = this._createLongField (config.name, config.value, config.label, config.description, config.mandatory == 'true', config.regexp != '' ? new RegExp (config.regexp) : null, config.invalidText, config.width, config.disableCondition);
					break;
				case 'password':
					field = this._createPasswordField (config.name, config.value, config.label, config.description, config.mandatory == 'true', config.width, config.disableCondition);
					break;
				case 'date':
					field = this._createDateField (config.name, config.value, config.label, config.description, config.mandatory == 'true', config.regexp != '' ? new RegExp (config.regexp) : null, config.invalidText, config.width, config.disableCondition);
					break;
				case 'boolean':
					field = this._createBooleanField (config.name, config.value, config.label, config.description, config.width, config.disableCondition);
					break;
				default:
					if (config.widget != '')
					{
						 field = this._createWidgetField(config.widget, config.name, config.value, config.label, config.description, config.mandatory == 'true', config.regexp != '' ? new RegExp (config.regexp) : null, config.invalidText, config.width, config.disableCondition);
					}
					else
					{
						field = this._createTextField (config.name, config.value, config.label, config.description, null, config.mandatory == 'true', config.regexp != '' ? new RegExp (config.regexp) : null, config.invalidText, config.width, config.disableCondition);
					}
					break;
			}
		}
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
	 * @param {String} regexp The regular expression to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @param {String/Number} width The optional width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createDoubleField: function (name, value, label, description, mandatory, regexp, invalidText, width, disableCondition)
	{
		var me = this;
		
		return new Ext.form.field.Number({
			name: name,
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        value: value,
	        validator: function(value)
	        {	
	        	var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
	    		if(!enabled)
	    		{
	    			return true;
	    		}
	    		
	      		// Otherwise the field has to be checked
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	                return this._blankText;
	    	            }
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
			invalidText: invalidText != null ? invalidText : null,
	        msgTarget: 'side',
	        
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createLongField: function (name, value, label, description, mandatory, regexp, invalidText, width, disableCondition)
	{
		var me = this;

		return new Ext.form.field.Number ({
			name: name,
			fieldLabel: label,
	        ametysDescription: description,
	        
	        allowDecimals: false,
	        
	        value: value,
	        validator: function(value)
	        {	
	        	var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
	        	if(!enabled)
	    		{
	    			return true;
	    		}
	    		
	      		// Otherwise the field has to be checked
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    			if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	                return this._blankText;
	    	            }
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
			invalidText: invalidText != null ? invalidText : null,
			msgTarget: 'side',
			
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
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
	 * @param {String/Number} width The optional width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createPasswordField: function (name, value, label, description, mandatory, width, disableCondition)
	{
		var me = this;
		
		return new Ametys.form.field.ChangePassword({
			name: name,
			fieldLabel: label,
		    ametysDescription: description,
		    
		    value: value,
		    validator: function(value)
	        {	
		    	var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
		    	if(!enabled)
	    		{
	    			return true;
	    		}
	    		
	      		// Otherwise the field has to be checked
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	                return this._blankText;
	    	            }
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createDateField: function (name, value, label, description, mandatory, regexp, invalidText, width, disableCondition)
	{
		var me = this;
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
	        format: "<i18n:text i18n:key='DATE_FIELD_FORMAT'/>",
	        value: dateValue,
	        
	        validator: function(value)
	        {	
	    		var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
	    		if(!enabled)
	    		{
	    			return true;
	    		}
	    		
	      		// Otherwise the field has to be checked
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	                return this._blankText;
	    	            }
	    	        }
	    	        if (regexp && !regexp.test(value)) 
	    	        {
	    	            return this.regexText;
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
	        regexText: invalidText != null ? invalidText :  "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP'/>"  + regexp,
			invalidText: invalidText != null ? invalidText : null,
			msgTarget: 'side',
			
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a checkbox
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */	
	_createBooleanField: function (name, value, label, description, width, disableCondition)
	{
		return new Ext.form.field.Checkbox ({
			name: name,
			 
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        checked: (value == "true"),
	        
	        inputValue: 'true', 
	        uncheckedValue: 'false',
	        
	        disableCondition: disableCondition != null ? disableCondition : null,
	        
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createTextField: function (name, value, label, description, enumeration, mandatory, regexp, invalidText, width, disableCondition)
	{
		var me = this;
		
		if (enumeration != null)
		{
			return new Ext.form.field.ComboBox ({
				name: name,
				
		        fieldLabel: label,
		        ametysDescription: description,
		        value: value,
		        
		        validator: function(value)
		        {	
		        	var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
		        	if(!enabled)
		    		{
		    			return true;
		    		}
		    		
		      		// Otherwise the field has to be checked
		    		{
		    			var trimmed =  Ext.String.trim(value);		    			
		    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
		    	        {
		    	        	if (mandatory) 
		    	        	{
		    	                return this._blankText;
		    	            }
		    	        }
		    	        return true;
		    		}
		        },
		        disableCondition: disableCondition != null ? disableCondition : null,
		        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        
		        msgTarget: 'side',
		        
		        mode: 'local',
		        editable: false,
		        forceSelection: true,
				triggerAction: 'all',
		        store: new Ext.data.SimpleStore({
		            id: 0,
		            fields: [ 'value', {name: 'text', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
		            data: enumeration,
		            sortInfo: {field: 'text'} // default order
		        }),
		        valueField: 'value',
		        displayField: 'text',
		        
		        listConfig: {
		        	cls: 'ametys-boundlist'
		        }
			});
		}
		else
		{
			return new Ext.form.field.Text ({
				name: name,
				
		        fieldLabel: label,
		        ametysDescription: description,
		        value: value,
		        validator: function(value)
		        {	
		        	var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
		        	if(!enabled)
		    		{
		    			return true;
		    		}
		    		
		      		// Otherwise the field has to be checked
		    		{
		    			var trimmed =  Ext.String.trim(value);		    			
		    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
		    	        {
		    	        	if (mandatory) 
		    	        	{
		    	        		return this._blankText;
		    	            }
		    	        }
		    	        
		    	        if (regexp && !regexp.test(value)) 
		    	        {
		    	            return this.regexText;
		    	        }
		    	        return true;
		    		}
		        },
		        disableCondition: disableCondition != null ? disableCondition : null,
		        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        
		        regexText: invalidText != null ? invalidText :  "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP'/>"  + regexp,
				invalidText: invalidText != null ? invalidText : null,
				msgTarget: 'side'
			});
		}
	},

	/**
	 * @private
	 * Creates a field based upon its widget id
	 * @param {String} widgetId The id of the field used in #_widgets
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {Boolean} mandatory True if the field can not be empty
	 * @param {String} regexp The regexp to use to validate the field value
	 * @param {String} invalidText The text to display when the field value is not valid
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createWidgetField: function (widgetId, name, value, label, description, mandatory, regexp, invalidText, width, disableCondition)
	{
		var me = this;
		
		var widgetCfg = {
				name: name,
				
		        fieldLabel: label,
		        ametysDescription: description,
		        value: value,
		        validator: function(value)
		        {	
		        	var enabled = me._form.getForm().findField(name).isVisible() && !me._form.getForm().findField(name).isDisabled();
		        	if(!enabled)
		    		{
		    			return true;
		    		}
		    		
		      		// Otherwise the field has to be checked
		    		{
		    			var trimmed =  Ext.String.trim(value);		    			
		    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
		    	        {
		    	        	if (mandatory) 
		    	        	{
		    	        		return this._blankText;
		    	            }
		    	        }
		    	        
		    	        if (regexp && !regexp.test(value)) 
		    	        {
		    	            return this.regexText;
		    	        }
		    	        return true;
		    		}
		        },
		        disableCondition: disableCondition != null ? disableCondition : null,
		        
		        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,

		        regexText: invalidText != null ? invalidText :  "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP'/>"  + regexp,
				invalidText: invalidText != null ? invalidText : null,
		        msgTarget: 'side'
		};
		
		var widgetClass = this._widgets[widgetId];
		return eval('new ' + widgetClass + '(widgetCfg)');
	},

	/**
	 * Draw the navigation panel. This function needs the this._navItems was filled first.
	 * @return {Ametys.admin.rightpanel.NavigationPanel} The navigation panel
	 * @private
	 */
	_drawNavigationPanel: function ()
	{
		this._nav = new Ametys.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU'/>"});
		
		for (var i=0; i < this._navItems.length; i++)
		{
			var item = new Ametys.admin.rightpanel.NavigationPanel.NavigationItem ({
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
	 * @return {Ametys.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawHandlePanel: function ()
	{
		this._actions = new Ametys.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE'/>"});
		
		// Save action
		this._actions.addAction("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_SAVE'/>",
						 null,
						 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/config/save.png',
						 Ext.Function.bind(this.save, this));
		
		// Quit action
		this._actions.addAction("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_QUIT'/>",
						 null,
						 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/config/quit.png',
						 Ext.Function.bind(this.goBack));

		return this._actions;
	},

	/**
	 * Draw the help panel.
	 * @return {Ametys.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP'/>"});
		helpPanel.addText("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT'/>");
		
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
			 new Ext.LoadMask({target: Ext.getBody()}).show();
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
			Ext.MessageBox.alert("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID_TITLE'/>", "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID'/>");
			this._form.getForm().markInvalid();
			return;
	    }
	    
		this.save._mask = new Ext.LoadMask({target: Ext.getBody()});
	    this.save._mask.show();
	    Ext.defer(this._save2, 1, this);
	},
	
	/**
	 * @private
	 * Second part of the #save process (due to asynchronous process)
	 */
	_save2: function ()
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
	    	ex = e;
	    }
	    
	    this.save._mask.hide();

		if (result == null)
	    {
			Ametys.log.ErrorDialog.display({
				title: "<i18n:text i18n:key='PLUGINS_CORE_SAVE_DIALOG_TITLE'/>", 
				text: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_FATALERROR'/>",
	    		details: ex,
	    		category: "Ametys.plugins.core.administration.Config.save"
			});
	        return;
	    }
	    result = result.responseXML;
	    
	    var error = Ext.dom.Query.selectValue("*/error", result);
	    if (error != null && error != "")
	    {
	    	Ext.Msg.show ({
	    		title: "<i18n:text i18n:key='PLUGINS_CORE_SAVE_DIALOG_TITLE'/>",
	    		msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_ERROR'/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
	    	});
	        return;
	    }
	    
	    Ext.Msg.show ({
	    		title: "<i18n:text i18n:key='PLUGINS_CORE_SAVE_DIALOG_TITLE'/>",
	    		msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_OK'/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.INFO,
				fn: Ext.Function.bind(this.goBack, this, [true])
	    });
	},

	/**
	 * Bind the scroll to the toc
	 * @private
	 */
	_bindScroll: function ()
	{
		this._bound = true;
	},
	/**
	 * Stop binding the scroll to the toc
	 * @private
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

		for (var i=0;  i < this._ct.dom.children[0].children[0].children.length; i++)
		{
			var anchor = this._ct.dom.children[0].children[0].children[i];
			if (i > 0) 
			{
				last = this._ct.dom.children[0].children[0].children[i-1];
			}
			else 
			{
				last = anchor;
			}
			var posY = Ext.get(anchor).getTop() - a0;
			if(posY >= scrollPosition + p)
			{
				this._activateItemMenu(last.id);
				return;
			}
		
		}
		
		this._activateItemMenu(this._ct.dom.children[0].children[0].children[this._ct.dom.children[0].children[0].children.length - 1].id);
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
	width: 100,
	listConfig: {
		cls: 'ametys-boundlist'
	}
});
/**
 * @private
 * A class to define a local field that handle hours by displaying only hours and that forbidden modifications
 */
Ext.define('Ametys.plugins.core.administrator.Config.HourField',	{
	extend: 'Ametys.plugins.core.administrator.Config.TimeField',
	
	increment: 60,
	editable: false,
    width: 100,
    listConfig: {
		cls: 'ametys-boundlist'
	}
});
/**
 * @private
 * A class to define a local field that handle texts
 */
Ext.define('Ametys.plugins.core.administrator.Config.TextareaField',	{
	extend: 'Ext.form.field.TextArea',
	
	width: Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH,
    height: 80
});
