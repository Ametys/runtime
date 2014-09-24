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
	FIELD_WIDTH: 250,
	/**
	 * @property {Number} AMETYS_DESCRIPTION_WIDTH The width for ametys descriptions
	 * @private
	 * @readonly 
	 */
	AMETYS_DESCRIPTION_WIDTH: 20,
	/**
	 * @property {Number} CHECKERS_LEFT_MARGIN The left margin for category checkers
	 * @private
	 * @readonly 
	 */
 	CHECKERS_LEFT_MARGIN: 235,
	/**
	 * @property {Number} TEST_CONTAINERS_TOP_MARGIN The top margin for test containers
	 * @private
	 * @readonly 
	 */
 	TEST_CONTAINERS_TOP_MARGIN: -10,
	/**
	 * @property {Number} TEST_CONTAINERS_BOTTOM_MARGIN The bottom margin for test containers
	 * @private
	 * @readonly 
	 */
 	TEST_CONTAINERS_BOTTOM_MARGIN: 20,
	/**
	 * @property {Number} SWITCHER_WIDTH The width for the group switchers (ametys description included)
	 * @private
	 * @readonly 
	 */
	SWITCHER_WIDTH: 155,
	/**
	 * @property {Number} ADMIN_RIGHT_PANEL_WIDTH The width for the admin panel on the right of the screen 
	 * @private
	 * @readonly 
	 */
	ADMIN_RIGHT_PANEL_WIDTH: 277,
	/**
	 * @property {Number} LAUNCH_TESTS_SAVE_MBOX_HEIGHT The height and of the "launch tests and save" message box
	 * @private
	 * @readonly 
	 */
	LAUNCH_TESTS_SAVE_MBOX_HEIGHT: 130,
	/**
	* @property {Ext.form.Field[]} _fields The configuration fields
	* @private
	 */
	_fields: [],
	/**
	 * @property {Object} _categories The mapping of the categories' id with their label
	 */
	_categories: {},
	/**
	 * @property {Ametys.plugins.core.administration.Config.ParameterChecker[]} _paramCheckers all the parameter checkers of this page
	 */
	_paramCheckers: [],
	/**
	 * @property {Object} _navigationMap mapping of the navigation items with their labels in order to ease their updating
	 */
	_navigationMap: [],
	/**
	 * @property {Ext.form.FormPanel} _form The configuration form
	 * @private
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.NavigationPanel} _navigationPanel The navigation panel
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.ActionPanel} _actions The action panel
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.ActionPanel} _testResults the test results panel
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
			
			layout: 'anchor',
			
			fieldDefaults: {
				cls: 'ametys',
				labelAlign: 'right',
		        labelWidth: Ametys.plugins.core.administration.Config.LABEL_WIDTH,
		        labelSeparator: ''
			},

			listeners: {
				'boxready': Ext.Function.bind(this._onFormReady, this),
				'fieldvaliditychange': Ext.Function.bind(this._updateNavigationPanel, this),
			},
			
			html: ''
		});
		
		this._contextualPanel = new Ext.Container({
			region:'east',
			cls : 'admin-right-panel',
			border: false,
			autoScroll: true,
			width: Ametys.plugins.core.administration.Config.ADMIN_RIGHT_PANEL_WIDTH,
			
			items: [this._drawNavigationPanel(),
			        this._drawHandlePanel(),
			        this._drawTestResultsPanel(),
			        this._drawHelpPanel()]
		});
		
		return new Ext.Panel({
			autoScroll: false,
			
			cls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			items: [this._form, 
			        this._contextualPanel],
			        

	        listeners: {
				'boxready': Ext.Function.bind(this._onBoxReady, this)
			}        
		});
	},
	
	/**
	 * @private
	 * Listener when the main panel is ready
	 */
	_onFormReady: function() 
	{
		// Disable conditions listeners
		var fieldsLength = this._fields.length;
		for (var i = 0; i < fieldsLength ; i++)
		{
			if (this._fields[i].disableCondition != null)
			{
				this._disableField(this._fields[i]);
				this._addListeners(this._fields[i].disableCondition, this._fields[i]);
			}
		}
		
		// Parameter checkers listeners
		var nbParamCheckers = this._paramCheckers.length;
		for (var i = 0; i < nbParamCheckers; i++)
		{
			var paramChecker = this._paramCheckers[i], 
				linkedParams = paramChecker.linkedParams,
			    nbLinkedParams = linkedParams.length;
			
			// on change listeners to reset the parameter checkers' status
			for (var j = 0; j < nbLinkedParams; j++)
			{
				var	linkedParamField = this._form.getForm().findField(linkedParams[j]);
				linkedParamField.on('change', Ext.bind(this._updateTestButton, this, [paramChecker], false));
				linkedParamField.on('disable', Ext.bind(this._updateTestButton, this, [paramChecker], false));
				linkedParamField.on('enable', Ext.bind(this._updateTestButton, this, [paramChecker], false));
				linkedParamField.on('warningchange', Ext.Function.bind(this._updateNavigationPanel, this));
			}		
		}
		
		this._updateTestResults();
	},
	
	/**
	 * @private
	 * Listener when the panel's box is ready
	 */
	_onBoxReady: function() 
	{
		this._ct = Ext.getCmp("config-inner").getEl().child("div:first");
		this._ct.on('scroll', Ext.Function.bind(this._calcScrollPosition, this));
		
		this._calcScrollPosition();
	},
	
	/**
	 * Creates a fieldset with this id and label
	 * @param {String} id The id of the new field set
	 * @param {String} label The label of the field set
	 * @return {Ext.form.FieldSet} the fieldset representing a category
	 */
	createFieldSet: function (id, label)
	{
		this._categories[label] = id;
		
		return new Ext.panel.Panel({
				id : id,
				title:label,
				
				border: false,
				shadow: false,
				
				layout: {
					type: 'vbox',
				},
				
				collapsible: true,
				titleCollapse: true,
				hideCollapseTool: true,

				width: Ametys.plugins.core.administration.Config.LABEL_WIDTH
					   + Ametys.plugins.core.administration.Config.FIELD_WIDTH
					   + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH
		});
	},
	
	/**
	 * Add a group to a category
	 * @param {Ext.form.FieldSet} fd The fieldset where to add the group
	 * @param {String} id the id of the new group
	 * @param {String} name The name of the group
	 * @param {Object} switcher If the group can be switched on/off, this object has the same configuration as the config parameter of the method #addInputField 
	 * @param {String[]} subitems An array containing the names of the fields the switcher will show/hide 
	 */
	addGroupCategory: function (fd, id, name, switcher, subitems)
	{
		if (switcher != null)
		{
			var modifiedSwitcher = Ext.applyIf ({label: '', description: '', width: 'auto'}, switcher);
			var inputCt = this.createInputField(modifiedSwitcher);
			var input = inputCt.items.get(0);
			
			// check/uncheck
			input.on('change', Ext.bind(this._showHideGroup, this, [subitems, name], true));
			
			// parameter checkers' visibility at startup
			if (!input.getValue())
			{
				input.on('boxready', Ext.bind (this._showHideGroup, this, [null, false, null, subitems, name], false));
			}
			
			var items = [
			      input,
			      { width: Ametys.plugins.core.administration.Config.SWITCHER_WIDTH, baseCls: '', html: "<label for='" + input.getInputId() + "'>" + name + "</label>", padding: "3 0 0 5" }
            ];
			
			if (switcher.description != '')
			{
				items.push({
					cls: "ametys-description",
				    baseCls: '', 
				    padding: "3 0 0 5",
					html: '  ',
					width: Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH,
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
				id: id,
				layout: {
					type: 'hbox',
				},
				cls: 'ametys-subcategory',
				items: items
			}));
		}
		else
		{
			fd.add (new Ext.Container({
				id: id,
				cls: 'ametys-subcategory',
				html: name
			}));
		}
	},
	
	/**
	 * @private
	 * Show or hide the elements of a group and the associated parameter checkers if there is one on a subitem
	 * @param {HTMLElement} input The changed input 
	 * @param {String} newValue The new value of the input 
	 * @param {String} oldValue The preceding value of the input
	 * @param {String[]} elements The html names of the elements to show or hide
	 * @param {String} name the name of the group
	 */
	_showHideGroup: function(input, newValue, oldValue, elements, name)
	{
		Ext.suspendLayouts();
		for (var i = 0; i < elements.length; i++)
		{
			var field = this._form.getForm().findField(elements[i]),
				categoryLabel = field.up('panel').title;

			this._showHideCheckers(field.getFieldLabel(), categoryLabel + '/' + name, newValue);

			field.setVisible(newValue);
		}
		Ext.resumeLayouts(true);
		this._updateTestResults();
		this._updateNavigationPanel();
	},
	
	/**
	 * @private
	 * Show or hide the parameter checkers linked to the group or to its parameters
	 * @param {String} fieldLabel the label of the field
	 * @param {String} groupLabel the label of the group
	 * @param {String} newValue the new value of the input 
	 */
	_showHideCheckers: function(fieldLabel, groupLabel, newValue)
	{
	    if (Ext.String.startsWith(fieldLabel, "*"))
		{
	    	fieldLabel = fieldLabel.substring(2);
		}
	    
		for (var i = 0; i < this._paramCheckers.length; i++)
		{
			if (this._paramCheckers[i].uiRefLabel == groupLabel + '/' + fieldLabel ||  this._paramCheckers[i].uiRefLabel == groupLabel)
			{
				Ext.getCmp(this._paramCheckers[i].buttonId).up('container').setVisible(newValue);
			}
		}
	},

	/**
	 * Add a parameter checker to a category.
	 * @param {Ext.form.FieldSet} fd The fieldset where to add the group checker
	 * @param {Object} paramChecker 
	 * @param {String} paramChecker.id the id of the parameter checker
	 * @param {String} paramChecker.class the class implementing the check 
	 * @param {Object} paramChecker.category the tested category
	 * @param {Number} paramChecker.order the order of the parameter checker (if several are attached to the same parameter/group/category)
     * @param {String} paramChecker.plugin the plugin declaring this parameter checker
	 * @param {String} paramChecker.small-icon-path the path to the small icon representing the parameter checker
	 * @param {String} paramChecker.medium-icon-path the path to the medium icon representing the parameter checker
	 * @param {String} paramChecker.large-icon-path the path to the large icon representing the parameter checker
	 * @param {String[]} paramChecker.linked-params the ids of the parameters used for the checking
	 * @param {String} categoryLabel the category label the parameter checker is attached to
	 * @param {String} label the label of the parameter checker
	 * @param {String} description the description of the parameter checker
	 */
	addCategoryChecker: function(fd, paramChecker, categoryLabel, label, description)
	{
		var parameterChecker = new Ametys.plugins.core.administration.Config.ParameterChecker(paramChecker, categoryLabel, "category", label, description),
			testButton = this._generateTestButton(parameterChecker),
			testContainer = this._generateTestContainer(parameterChecker, testButton);
		
		fd.add(testContainer);
		testContainer.setMargin(Ametys.plugins.core.administration.Config.TEST_CONTAINERS_TOP_MARGIN + ' 0 ' +  Ametys.plugins.core.administration.Config.TEST_CONTAINERS_BOTTOM_MARGIN + ' ' + Ametys.plugins.core.administration.Config.CHECKERS_LEFT_MARGIN);
		
		this._paramCheckers.push(parameterChecker); 
	},
	
	/**
	 * Add a parameter checker to a group.
	 * @param {Ext.form.FieldSet} fd The fieldset where to add the group checker
	 * @param {Object} paramChecker 
	 * @param {String} paramChecker.id the id of the parameter checker
	 * @param {String} paramChecker.class the class implementing the check 
	 * @param {Object} paramChecker.group the tested group 
	 * @param {Number} paramChecker.order the order of the parameter checker 
	 * @param {String} paramChecker.plugin the plugin declaring this parameter checker 
	 * @param {String} paramChecker.small-icon-path the path to the small icon representing the parameter checker
	 * @param {String} paramChecker.medium-icon-path the path to the medium icon representing the parameter checker
	 * @param {String} paramChecker.large-icon-path the path to the large icon representing the parameter checker
	 * @param {String[]} paramChecker.linked-params the ids of the parameters used for the checking
	 * @param {String} categoryLabel the label of the category of the group the parameter checker is attached to
	 * @param {String} groupLabel the label of the group the parameter checker is attached to
	 * @param {String} label the label of the parameter checker
	 * @param {String} description the description of the parameter checker
	 */
	addGroupChecker: function(fd, paramChecker, categoryLabel, groupLabel, label, description)
	{
		var parameterChecker = new Ametys.plugins.core.administration.Config.ParameterChecker(paramChecker, categoryLabel+"/"+groupLabel, "group", label, description),
		    testButton = this._generateTestButton(parameterChecker),
		    testContainer = this._generateTestContainer(parameterChecker, testButton);
		
		fd.add(testContainer);
		testContainer.setMargin(Ametys.plugins.core.administration.Config.TEST_CONTAINERS_TOP_MARGIN + ' 0 ' +  Ametys.plugins.core.administration.Config.TEST_CONTAINERS_BOTTOM_MARGIN + ' ' + Ametys.plugins.core.administration.Config.CHECKERS_LEFT_MARGIN);
		
		this._paramCheckers.push(parameterChecker);
	},
	
	/**
	 * Add a parameter checker to a parameter.
	 * @param {Object} paramChecker 
	 * @param {String} paramChecker.id the id of the parameter checker
	 * @param {String} paramChecker.class the class implementing the check 
	 * @param {String} paramChecker.param-ref the id of the tested parameter
	 * @param {Number} paramChecker.order the order of the parameter checker (if several are attached to the same parameter/group/category)
	 * @param {String} paramChecker.plugin the plugin declaring this parameter checker
	 * @param {String} paramChecker.small-icon-path the path to the small icon representing the parameter checker
	 * @param {String} paramChecker.medium-icon-path the path to the medium icon representing the parameter checker
	 * @param {String} paramChecker.large-icon-path the path to the large icon representing the parameter checker
	 * @param {String[]} paramChecker.linked-params the ids of the parameters used for the checking
	 * @param {String} categoryLabel the label of the parameter's category
	 * @param {String} groupLabel the label of the parameter's group
	 * @param {String} label the label of the parameter checker
	 * @param {String} description the description of the parameter checker
	 */
	addParameterChecker: function(paramChecker, categoryLabel, groupLabel, label, description)
	{
	    var	field = this._form.getForm().findField(paramChecker['param-ref']),
			uiRefLabel = field.getFieldLabel(),
	    	fieldCt = field.up();
	    	
	    if (Ext.String.startsWith(uiRefLabel, "*"))
    	{
	    	uiRefLabel = uiRefLabel.substring(2);
    	}
	    
	    var parameterChecker = new Ametys.plugins.core.administration.Config.ParameterChecker(paramChecker, categoryLabel+"/"+groupLabel+"/"+uiRefLabel, "parameter", label, description),
	    	testButton = this._generateTestButton(parameterChecker),
	    	testContainer = this._generateTestContainer(parameterChecker, testButton);
	    
		fieldCt.add(testContainer);
		
		this._paramCheckers.push(parameterChecker);
	},
	
	/**
	 * @private
	 * Generates the button launching the tests on the corresponding parameter/group/category
	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker} paramChecker the parameter checker 
	 */
	_generateTestButton: function(paramChecker)
	{	
		var me = this;
		
		return new Ext.button.Button({
			id: Ext.id(),
			paramChecker: paramChecker,
			tooltip: '',
			icon: Ametys.getPluginResourcesPrefix(paramChecker.plugin) + "/" + paramChecker.smallIconPath,
			text: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_LABEL_PREFIX'/>" + " \"" + paramChecker.label + "\"",
//		    html: '<table style="position: absolute;top: -3px; left: -2px">' + 
//				  	  '<tr>' +
//			  	  		  '<td style="width: 20px">' +
//							  '<img width="16" height="16" src=/plugins/' + paramChecker.plugin + '/resources/' + paramChecker.smallIconPath + '/>' +
//					  	  '</td>' +
//			  	  		  '<td>' +
//			  	  		  	'<em style="font-size: x-small; text-align: center">' + "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_LABEL_PREFIX'/>" + " \"" + paramChecker.label + "\"" + '</em>' +
//					  	  '</td>' +
//					  '</tr>' +
//				  '</table>',
			cls: 'param-checker-component',
            border: false,
            handler: function(btn, event)
            {
				event.stopPropagation();
        		me._check([paramChecker], false);
            }
		});
	},
	
	/**
	 * @private
	 * Generates the container that holds the test button and the ametys description
	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker} paramChecker the parameter checker 
	 * @param {Ext.button.Button} testButton the test button
	 * @return {Ext.container.Container} the container with the test button and the help box
	 */
	_generateTestContainer: function(paramChecker, testButton)
	{
		var tooltip = this._generateHelpBoxTip(paramChecker),
			helpBoxId = Ext.id(),
			items = [testButton];
			
		paramChecker.setButtonId(testButton.getId());
		paramChecker.setHelpBoxId(helpBoxId);
		
		items.push({
			id: helpBoxId,
			cls: "ametys-description",
		    baseCls: '', 
		    padding: "3 0 0 5",
			html: '  ',
			width: Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH,
			listeners: {
				'render': function() {
				    new Ext.ToolTip({
				        target: this.getEl(),
				        html: tooltip
				    });
				}
			}
		});
		
		return new Ext.Container({
			layout: {
				type: 'hbox',
				pack:'end'
			},
			cls : 'param-checker-container',
			items: items
		});
	},
	
	/**
	 * @private
	 * Generates the HTML code for the parameter checker's help box tooltip.
	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker} paramChecker the parameter checker 
	 */
	_generateHelpBoxTip: function(paramChecker)
    {
		var tipHtml = [],
		tpl = new Ext.XTemplate(
								'<table>' + 
								  	  '<tr>' +
							  	  		  '<td style="width: 48px">' +
											  '<img width="48" height="48" src=/plugins/' + paramChecker.plugin + '/resources/' + paramChecker.largeIconPath + '/>' +
									  	  '</td>' +
							  	  		  '<td>' +
											  '<strong> &#008; &#008; ' + paramChecker.label + ' </strong>' +
									  	  '</td>' +
									  '</tr>' +
									  '<tr>' +
										  '<td colspan="2">' +
											  '<emphasis>' + paramChecker.description + '</emphasis>' +
										  '</td>' +
									   '</tr>' +
									   '<tr>' +
									     	'<td colspan="2">' +
								   				"- <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_TOOLTIP_TESTED_PARAMS'/>" + paramChecker.linkedParamsLabels +
								   			'</td>' +
									   '</tr>' +
								'</table>'
								);
		
		tpl.applyOut(paramChecker, tipHtml);
		return tipHtml.join("");
	},
	
	/**
	 * @private
	 * Generates the HTML code for the parameter checker's status tooltip.
	 * @param {Ext.Element} el The element to add tooltips on
	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker} paramChecker the parameter checker 
	 */
	_generateStatusTip: function(el, paramChecker)
    {
		var tipHtml = [],
			tpl = new Ext.XTemplate(
						   					"<tpl if='status == " + Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_NOT_TESTED + "'><i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_STATUS_NOT_TESTED'/></tpl>" +
								   			"<tpl if='status == " + Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_SUCCESS + "'><i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_STATUS_SUCCESS'/></tpl>" +
								   			"<tpl if='status == " + Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE + "'>" +
								   				"<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_STATUS_FAILURE'/> </br>" +
								   				'<tr>' + 
								   					'<td colspan="2">' +
								   						"- <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_TOOLTIP_MESSAGE'/> <strong>" + paramChecker.errorMsg + "</strong>" +
							   						'</td>' +
						   						'</tr>' +
							   				"</tpl>" +
							   				"<tpl if='status == " + Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_DEACTIVATED + "'>" +
							   					"<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_STATUS_DEACTIVATED'/> </br>" +
							   					'<tr>' + 
							   						'<td colspan="2">' +
							   							"<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_TOOLTIP_MESSAGE'/>" +
							   							"<strong><i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_TOOLTIP_DEACTIVATED_MESSAGE'/></strong>" +
														'</td>' +
						   						'</tr>' +
											"</tpl>" +
						   					"<tpl if='status == " + Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING + "'>" +
								   				"<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_STATUS_WARNING'/> </br>" +
								   				"<tpl if='errorMsg != null'>" +
							  		   				'<tr>' + 
							  		   					'<td colspan="2">' +
							  		   						"<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECKER_TOOLTIP_MESSAGE2'/> <strong>" + paramChecker.errorMsg + "</strong>" +
								   						'</td>' +
							   						'</tr>' +
						   						'</tpl>' +
						   					'</tpl>'
				   				);
		
		tpl.applyOut(paramChecker, tipHtml);
		var tip = tipHtml.join("");
		
		el.set({"data-qtip": ""}); 
		el.set({"data-warnqtip": ""}); 
		el.set({"data-errorqtip": ""}); 

		if (paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING)
		{
		    Ext.form.Labelable.initWarnTip();
			el.set({"data-warnqtip": tip}); 
		}
		else if (paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE)
		{
			el.set({"data-errorqtip": tip});
		}
		else if (paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_DEACTIVATED)
		{
		    Ext.form.Labelable.initWarnTip();
			el.set({"data-warnqtip": tip}); 
		}
    },
	
	
	/**
	 * @private
	 * Updates the test results panel in the rights panel
	 */
	_updateTestResults: function()
	{
		var nbTests = this._paramCheckers.length,
			testResultsPanel = this._testResults,
			notTested = 0,
			failures = 0,
			successes = 0;
		
		for (var i = 0; i < nbTests; i++)
		{
			var paramChecker = this._paramCheckers[i],
			    status = paramChecker.getStatus();
			
			if (!Ext.getCmp(this._paramCheckers[i].buttonId).isVisible())
			{
				continue;
			}
				
			switch (status)
			{
				case Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_SUCCESS: 
					successes += 1;
					break;
					
				case Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE: 
					failures += 1;
					break;
				
				case Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_NOT_TESTED:
					notTested += 1;
					break;
				
				case Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_DEACTIVATED:
					notTested += 1;
					break;
					
				case Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING:
					notTested += 1;
					break;
					
				default:
					throw 'Unknown status ' + status;
			}
		}
		
		this._updateTestResultsPanel({notTested: notTested, successes: successes, failures: failures});
	},
	
	/**
	 * @private
	 * Sets the test button to the "warning" status, updates the button's tooltip and the test results panel
	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker} paramChecker the parameter checker to be updated
	 */	
	_updateTestButton: function(paramChecker)
	{
		var btn = Ext.getCmp(paramChecker.buttonId),
			helpBox = Ext.getCmp(paramChecker.helpBoxId),
			linkedParams = paramChecker.linkedParams,
		    allLinkedParamsDisabled = true,
		    invalidLinkedParameter = false;
	
	    for (var i = 0; i < linkedParams.length; i++)
		{
			var linkedParam = linkedParams[i],
				linkedParamField = this._form.getForm().findField(linkedParam);

			// check if all linked parameters are disabled
			if (!linkedParamField.isDisabled())
			{
				allLinkedParamsDisabled = false;
			}
			
			// check for each field's validity and visibility
			if (!linkedParamField.isDisabled() && linkedParamField.isVisible() && linkedParamField.getErrors().length != 0) 
			{
				invalidLinkedParameter = true;
			}
		}

	    // update style
	    btn.removeCls(['success', 'failure']);
	    btn.addCls('warning');
	    helpBox.addCls('offset');
	    btn.up('container').updateLayout();
	    
	    if (!allLinkedParamsDisabled && !invalidLinkedParameter)
    	{
	    	btn.enable();
	    	paramChecker.setStatus(Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING);
    	}
	    else
    	{
	    	btn.disable();
			paramChecker.setStatus(Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_DEACTIVATED);
    	}
	    
	    // update the tooltip
	    this._generateStatusTip(btn.up('container').getEl(), paramChecker);
	    helpBox.getEl().set({"data-qtip": this._generateHelpBoxTip(paramChecker)});
	    
	    // update the test results and the navigation panel
	    this._updateTestResults();
	    this._updateNavigationPanel();
	},
	
	/**
	 * @private
	 * Update the warnings on fields
	 */
	_updateWarnings: function()
	{
		for (var i = 0; i < this._paramCheckers.length; i++)
		{
			var paramChecker = this._paramCheckers[i],
				button = Ext.getCmp(paramChecker.buttonId),
				helpBox = Ext.getCmp(paramChecker.helpBoxId),
				success = paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_SUCCESS ? true : false,
				failure = paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE ? true : false,
				warning = paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING ? true : false,
				warningMsg = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_WARNING_TEXT_BEGINNING'/>" + paramChecker.label +
						  "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_WARNING_TEXT_END'/>";	
			
			for (var j = 0; j < paramChecker.linkedParams.length; j++)
			{
				var linkedParam = paramChecker.linkedParams[j],
				linkedParamField = Ametys.plugins.core.administration.Config._form.getForm().findField(linkedParam);
			
				linkedParamField._warnings = linkedParamField._warnings || {};
				
				var activeWarnings = linkedParamField.getActiveWarnings();
				if (success)
				{
					Ext.Array.remove(activeWarnings, warningMsg);
					linkedParamField._warnings[paramChecker.id] = null;
					
					linkedParamField.markWarning(activeWarnings);
				}
				else if (failure)
				{
					if (!linkedParamField._warnings[paramChecker.id])
					{
						linkedParamField._warnings[paramChecker.id] = warningMsg;
						activeWarnings.push(warningMsg);
					}
					
					linkedParamField.markWarning(activeWarnings);
				}
				else if (warning)
				{
					helpBox.addCls('offset');
				}
			}
		}
	},
	
	/**
	 * @private
	 * Calls the server-side with the configuration parameters.
 	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker[]} paramCheckers the parameter checkers
 	 * @param {Boolean} displayErrors true if the errors have to be displayed at then end of the tests
 	 * @param {Function} [callback] the optional callback for this function.
 	 * @param {Boolean} [forceTest=true] to replay even successful tests
     */
	_check: function(paramCheckers, displayErrors, callback, forceTest)
	{
		forceTest = forceTest !== false ? true : false;
		
		var params = this._getAllFormValues(),
			url = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/config/test",
			enabledParamCheckers = [];
		
		// Add the ids of the parameter checkers involved
		var paramCheckersIds = '';
		for (var i = 0; i < paramCheckers.length; i++)
		{	
			var paramChecker = paramCheckers[i],
				btn = Ext.getCmp(paramChecker.buttonId);
			
			if (btn.isVisible() && !btn.isDisabled())
			{
				btn.disable();
				if (forceTest || paramChecker.getStatus() != Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_SUCCESS)
				{
					btn.getEl().mask("");
					paramCheckersIds += paramChecker.id + ',';
				}
				
				enabledParamCheckers.push(paramChecker);
			}
		}
		
		// Removing the last coma
		params.paramCheckersIds = paramCheckersIds.substr(0, paramCheckersIds.length - 1);
		
		// Server call
		Ext.Ajax.request({
			url: url, 
			params: params, 
			callback: Ext.bind(this._checkCb, this, [enabledParamCheckers, callback, displayErrors], true)
		});	
	},
	
	/**
	 * @private
	 * Callback upon reception of the server's response
	 * @param {Object} options the Ext.Ajax.request call configuration
	 * @param {Object} options.params the call parameters
	 * @param {boolean} success true if the request succeeded, false otherwise
	 * @param {Object} response the server's response
 	 * @param {Ametys.plugins.core.administration.Config.ParameterChecker[]} paramCheckers the parameter checker
 	 * @param {Function} callback the optional callback for this function.
 	 * @param {Boolean} displayErrors true if the errors have to be displayed at then end of the tests
 	 */
	_checkCb: function(options, success, response, paramCheckers, callback, displayErrors)
	{	
		if (!success)
		{
			Ametys.log.ErrorDialog.display({
				title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SERVER_CALL_ERROR_TITLE'/>", 
				text: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SERVER_CALL_ERROR_TEXT'/>",
	    		category: "Ametys.plugins.core.administration.Config._checkCb"
			});
			
			// Re-enable buttons and reinitialize test buttons
			for (var i = 0; i < this._paramCheckers.length; i++)
			{
				this._updateTestButton(this._paramCheckers[i]);
			}
			
			return;
		}
		
		var	params = options.params,
			result = response.responseXML,
			errors = 0;

		// Server's response handler
		for (var i = 0; i < paramCheckers.length; i++)
		{
			var paramChecker = paramCheckers[i],
				btn = Ext.getCmp(paramChecker.buttonId),
				helpBox = Ext.getCmp(paramChecker.helpBoxId),
				errorMsg = Ext.dom.Query.selectValue("*/" + paramChecker.id, result);

			btn.getEl().unmask();
			btn.enable();
			btn.removeCls('warning');
			
			// Update the parameter checker's component
		    if (errorMsg == null)
			{
		    	btn.removeCls('failure');
		    	btn.addCls('success');
		    	helpBox.addCls('offset');
		    	paramChecker.setStatus(Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_SUCCESS);
		    	paramChecker.setErrorMsg(null);
		    	
		    	this._generateStatusTip(btn.up('container').getEl(), paramChecker);
			    helpBox.getEl().set({"data-qtip": this._generateHelpBoxTip(paramChecker)});
			}
		    else
			{
		    	errors++;
		    	btn.removeCls('success');
		    	btn.addCls('failure');
		    	helpBox.addCls('offset');
		    	paramChecker.setStatus(Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE);
		    	paramChecker.setErrorMsg(errorMsg);

		    	this._generateStatusTip(btn.up('container').getEl(), paramChecker);
			    helpBox.getEl().set({"data-qtip": this._generateHelpBoxTip(paramChecker)});
			}
		}
		
		this._updateTestResults();
		this._updateNavigationPanel();
		this._updateWarnings();
		
		if (callback && typeof callback === 'function') 
		{
			callback(errors == 0);
		}
					
		if (displayErrors)
		{
			this._displayErrorDialog();
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
		var fieldCt = this.createInputField(config);
		var field = fieldCt.items.get(0);

		if (startVisible == 'false')
		{
			field.hide();
		}
		
		if (field != null)
	    {
		    ct.add(fieldCt);
		    this._fields.push(field); 
	    }

		return field;
	},
	
	/**
	 * Creates and returns a container wrapping an input field depending on the given configuration
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
		
		return new Ext.container.Container({
			items: field
		});
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
	 * @param {String/Number} width The optional width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
	 * @param {Object} disableCondition The optional disable condition.
	 * @return {Ext.form.field.Field} The created field
	 */
	_createDoubleField: function (name, value, label, description, mandatory, regexp, invalidText, width, disableCondition)
	{
		var me = this;
		
		return	new Ext.form.field.Number({
			name: name,
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        value: value,
	        validator: function(value)
	        {	
	        	var field = me._form.getForm().findField(name);
	        	var enabled = field.isVisible() && !field.isDisabled();
	    		if(!enabled)
	    		{
	    			return true;
	    		}
	      		// Otherwise the field has to be checked
	    		else
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	        		return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>"; 
	    	            }
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
			invalidText: invalidText != null ? invalidText : null,
	        msgTarget: 'side',
	        
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
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
	        	var field = me._form.getForm().findField(name);
	        	var enabled = field.isVisible() && !field.isDisabled();
	        	if(!enabled)
	    		{
	    			return true;
	    		}
	      		// Otherwise the field has to be checked
	        	else
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    			if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	                return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>";
	    	            }
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
			invalidText: invalidText != null ? invalidText : null,
			msgTarget: 'side',
			
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH
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
	 * @param {String/Number} width The optional width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
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
	        	var field = me._form.getForm().findField(name);
	        	var enabled = field.isVisible() && !field.isDisabled();
		    	if(!enabled)
	    		{
	    			return true;
	    		}
	      		// Otherwise the field has to be checked
		    	else
		    	{
	    			var trimmed =  Ext.String.trim(value);	    			
	    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	        		return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>";
	    	            }
	    	        }
	    	        return true;
	    		}
	        },
	        disableCondition: disableCondition != null ? disableCondition : null,
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
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
	        	var field = me._form.getForm().findField(name);
	        	var enabled = field.isVisible() && !field.isDisabled();
	    		if(!enabled)
	    		{
	    			return true;
	    		}
	      		// Otherwise the field has to be checked
	    		else
	    		{
	    			var trimmed =  Ext.String.trim(value);	    			
	    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
	    	        {
	    	        	if (mandatory) 
	    	        	{
	    	        		return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>";
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
			
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH
		});
	},

	/**
	 * @private
	 * Creates a checkbox
	 * @param {String} name The name of the field (the one used to submit the request)
	 * @param {Object} value The value of the field at the creating time
	 * @param {String} label The label of the field
	 * @param {String} description The associated description
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
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
	        
	        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
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
		        	var field = me._form.getForm().findField(name);
		        	var enabled = field.isVisible() && !field.isDisabled();

		        	if (!enabled)
		    		{
		    			return true;
		    		}
		      		// Otherwise the field has to be checked
		        	else
		        	{
		    			var trimmed =  Ext.String.trim(value);		    			
		    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
		    	        {
		    	        	if (mandatory) 
		    	        	{
		    	        		return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>";
		    	            }
		    	        }
		    	        return true;
		    		}
		        },
		        disableCondition: disableCondition != null ? disableCondition : null,
		        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH,
		        
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
		        	var field = me._form.getForm().findField(name);
		        	var enabled = field.isVisible() && !field.isDisabled();
		        	
		        	if (!enabled)
		    		{
		    			return true;
		    		}
		      		// Otherwise the field has to be checked
		        	else
		        	{
		    			var trimmed =  Ext.String.trim(value);		    			
		    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
		    	        {
		    	        	if (mandatory) 
		    	        	{
		    	        		return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>";
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
		        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH,
		        
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
	 * @param {String/Number} width The optionnal width. The default one is the constants FIELD_WIDTH + LABEL_WIDTH + AMETYS_DESCRIPTION_WIDTH
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
		        	var field = me._form.getForm().findField(name);
		        	var enabled = field.isVisible() && !field.isDisabled();
		        	
		        	if(!enabled)
		    		{
		    			return true;
		    		}
		      		// Otherwise the field has to be checked
		        	else
		        	{
		    			var trimmed =  Ext.String.trim(value);		    			
		    	        if (trimmed.length < 1 || (value === this.emptyText && this.valueContainsPlaceholder)) 
		    	        {
		    	        	if (mandatory) 
		    	        	{
		    	        		return this.invalidText || "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_FIELD_TEXT'/>";
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
		        
		        width: width || Ametys.plugins.core.administration.Config.FIELD_WIDTH + Ametys.plugins.core.administration.Config.LABEL_WIDTH + Ametys.plugins.core.administration.Config.AMETYS_DESCRIPTION_WIDTH,

		        regexText: invalidText != null ? invalidText :  "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP'/>"  + regexp,
				invalidText: invalidText != null ? invalidText : null,
		        msgTarget: 'side'
		};
		
		var widgetClass = this._widgets[widgetId];
		return eval('new ' + widgetClass + '(widgetCfg)');
	},

	/**
	 * @private
	 * Draws the navigation panel. This function needs the this._navItems to be filled first.
	 * @return {Ametys.admin.rightpanel.NavigationPanel} The navigation panel
	 */
	_drawNavigationPanel: function ()
	{
		this._navigationPanel = new Ametys.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU'/>"});
		
		for (var i = 0; i < this._navItems.length; i++)
		{
			var item = new Ametys.admin.rightpanel.NavigationPanel.NavigationItem ({
				id : "a" + this._navItems[i].id,
				text: this._navItems[i].label,
				
				divToScroll: this._navItems[i].id,
				
				ctToScroll: 'config-inner',
				
				border: false,
				
				bindScroll: this._bindScroll,
				unbindScroll: this._unbindScroll,
			
				toggleGroup : 'config-menu'
			});
			
			this._navigationPanel.add(item);
			
			// Bind the navigation item with its label
			this._navigationMap[this._navItems[i].label] = item;
		}
		
		return this._navigationPanel;
	},

	
	/**
	 * @private
	 * Updates the navigation panel when a field validity's or a parameter checker status changes.
	 */
	_updateNavigationPanel: function()
	{
		for (var categoryLabel in this._navigationMap)
		{
			var	navItem = this._navigationMap[categoryLabel],
				paramCheckers = this._getParamCheckersByCategory(categoryLabel),
				categoryId = this._categories[categoryLabel],
				category = Ext.getCmp(categoryId),
				fields = category.query('*[componentCls=x-field]'),
				invalidField = false,
				warningField = false;

			// field validity
			for (var i= 0; i < fields.length; i++)
			{
				if (!fields[i].isDisabled() && fields[i].isVisible())
				{
					if (fields[i].hasActiveError())
					{
						navItem.removeCls('navigation-item-warning');
						navItem.addCls('navigation-item-error');
						invalidField = true;
						break;
					}
					else if (fields[i].hasActiveWarning())
					{
						navItem.removeCls('navigation-item-error');
						navItem.addCls('navigation-item-warning');
						warningField = true;
					}
				}
				else 
				{
					navItem.removeCls('navigation-item-warning');
					navItem.removeCls('navigation-item-error');
				}
			}
			
			if (!invalidField)
			{
				var warning = false,
					error = false;
				// parameter checkers
				for (var i = 0; i < paramCheckers.length; i++)
				{
					var paramChecker = paramCheckers[i];
					
					if (Ext.getCmp(paramChecker.buttonId).isVisible())
					{
						if (paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE) 
						{
							navItem.removeCls('navigation-item-warning');
							navItem.addCls('navigation-item-error');
							error = true;
							break;
						}
						else if (warningField || paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING)
						{
							navItem.removeCls('navigation-item-error');
							navItem.addCls('navigation-item-warning');
							warning = true;
						}
					}
				}
				
				if (!warning && !error)
				{
					navItem.removeCls('navigation-item-error');
					navItem.removeCls('navigation-item-warning');
				}
			}
		}
	},
	
	/**
	 * Get the paremeter checkers of a given category
	 * @param {String} category the category label
	 * @returns {Ametys.plugins.core.administration.Config.ParameterChecker[]} the parameter checkers belonging to this category
	 */
	_getParamCheckersByCategory: function(category)
	{
		var paramCheckers= [];
		for (var i = 0; i < this._paramCheckers.length; i++)
		{
			var paramChecker = this._paramCheckers[i],
				uiRefLabel = paramChecker.uiRefLabel;
			
			if (paramChecker.uiRefType == "category")
			{
				if (uiRefLabel == category)
				{
					paramCheckers.push(paramChecker);
				}
			}
			else
			{
				if(uiRefLabel.substring(0, uiRefLabel.indexOf("/")) == category)
				{
					paramCheckers.push(paramChecker);
				}
			}
		}
		
		return paramCheckers;
	},
	
	/**
	 * @private
	 * Draw the actions panel.
	 * @return {Ametys.admin.rightpanel.ActionPanel} The action panel
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
	 * @private
	 * Draws the test results panel.
	 * @return  {Ametys.admin.rightpanel.TextPanel} The test results panel
	 */
	_drawTestResultsPanel: function()
	{
		this._testResults = new Ametys.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TEST_RESULTS_TITLE'/>"});
		
		// Initialize the test results text
		this._testResults.addText('');
		
		// Run all tests action
		this._testResults.addAction("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_RUN_ALL_TESTS'/>",
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/config/testall.png',
				 Ext.bind(this._check, this,  [this._paramCheckers, true, null, true]));
		// Relaunched missed tests action
		this._testResults.addAction("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_RUN_MISSED_TESTS'/>",
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/config/testmissing.png',
				 Ext.bind(this._check, this, [this._paramCheckers, true, null, false]));	

		return this._testResults;
	},
	
	/**
	 * @private
	 * Updates the values inside the test results panel
	 * @param {Object} testResults the results of the tests
	 * @param {Number} testResults.notTested the number of non-performed tests
	 * @param {Number} testResults.successes the number of successful tests
	 * @param {Number} testResults.failures the number of failed tests
	 */
	_updateTestResultsPanel: function(testResults)
	{
		var html = [],
        tpl = new Ext.Template(
        			"<div class='test-results'>" + 
	        			"<span><i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TEST_RESULTS_TEXT'/></span>" +
						'<ul>' + 
							"<li> {successes} <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TEST_RESULTS_SUCCESSES'/></li>" +
							"<li> {failures} <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TEST_RESULTS_FAILURES'/></li>" +
							"<li> {notTested} <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TEST_RESULTS_NOT_TESTED'/></li>" +
						'</ul>' +
					"</div>"
	    			);
	
		tpl.applyOut(testResults, html);
		
		// Update panel values
		this._testResults.items.get(0).update(html);
		
		// Hide/show launch missed tests button
		var missedTestsButton = this._testResults.items.get(2),
			visibleParamCheckers = 0,
			warning = false;
		
		for (var i = 0; i < this._paramCheckers.length; i++)
		{
			if (Ext.getCmp(this._paramCheckers[i].buttonId).isVisible())
			{
				visibleParamCheckers++;
			}
			
			if(this._paramCheckers[i].getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING)
			{
				warning = true;
			}
		}
			
		if (warning || testResults.failures > 0 || (testResults.notTested > 0 && testResults.notTested != visibleParamCheckers))
		{
			missedTestsButton.show();
		}
		else
		{
			missedTestsButton.hide();
		}
	},
	
	/**
	 * @private
	 * Draw the help panel.
	 * @return {Ametys.admin.rightpanel.TextPanel} The help panel
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP'/>"});
		helpPanel.addText("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT'/>");
		
		return helpPanel;
	},

	/**
	 * Quit the screen
	 * @param {Boolean} mask Displaying a mask while quitting. Defaults to false.
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
		var me = this,
			testsOk = true;
		
		this._updateNavigationPanel();
		
		for (var i = 0; i < this._paramCheckers.length; i++)
		{
			var paramChecker = this._paramCheckers[i],
				status = paramChecker.getStatus();
			
			if (Ext.getCmp(paramChecker.buttonId).isVisible() && ( status == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE 
					  											|| status == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_NOT_TESTED
		  														|| status == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_WARNING))
			{
				testsOk = false;
			}
		}
		
		if (!this._form.getForm().isValid())
		{
			Ext.MessageBox.alert("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID_TITLE'/>", "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID'/>");
			this._form.getForm().markInvalid();
			return;
	    }

		if (!testsOk)
		{
			Ext.Msg.buttonText.yes = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TESTS_NOK_MBOX_SAVE'/>";
			Ext.Msg.buttonText.no = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TESTS_NOK_MBOX_RETRY'/>";
			Ext.Msg.buttonText.cancel = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TESTS_NOK_MBOX_CANCEL'/>";
			Ext.Msg.show({
						title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TESTS_NOK_MBOX_TITLE'/>", 
						msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_TESTS_NOK_MBOX_MSG'/>",
						buttons: Ext.Msg.YESNOCANCEL,
						icon: Ext.Msg.ERROR,
						height: Ametys.plugins.core.administration.Config.LAUNCH_TESTS_SAVE_MBOX_HEIGHT,
						fn: function(btn)
						{
							if (btn == 'yes')
							{
								me.save._mask = new Ext.LoadMask({target: Ext.getBody()});
							    me.save._mask.show();
							    Ext.defer(me._save2, 1, me);
							}
							else if (btn == 'no')
							{
								me.save._mask = new Ext.LoadMask({target: Ext.getBody()});
							    me.save._mask.show();
								me._check(me._paramCheckers, true, Ext.bind(function(success) { this.save._mask.hide(); if (success) { this.save(); } } , me), false);
							}
						}
					});
			return;
		}
		
		me.save._mask = new Ext.LoadMask({target: Ext.getBody()});
	    me.save._mask.show();
	    Ext.defer(me._save2, 1, me);
	},
	
	/**
	 * @private 
	 * Adds the disabled fields' values for the server calls and the saving process
	 */
	_getAllFormValues: function()
	{
		var fieldsLength = this._fields.length,
		    formValues = this._form.getForm().getValues();
		
		for (var i = 0; i < fieldsLength; i++)
		{
			var field = this._fields[i];
			if (field.isDisabled())
			{
				var fieldName = field.name;
				formValues[fieldName] = field.getValue();
			}
		}
		
		return formValues;
	},
	
	/**
	 * @private 
	 * Second part of the #save process (due to asynchronous process)
	 */
	_save2: function ()
	{
	    var url = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/config/set",
	    	argsObj = this._getAllFormValues(),
	    	result = null,
	    	ex = "";

	    try
	    {
	    	result =  Ext.Ajax.request({url: url, params: argsObj, async: false});	
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
	 * @private
	 * Displays the error dialogs
	 */
	_displayErrorDialog: function()
	{
		var nbErrors = 0,
			details = '';
		
		for (var i = 0; i < this._paramCheckers.length; i++)
		{
			var paramChecker = this._paramCheckers[i],
				uiRefType = paramChecker.uiRefType,
				firstSentence;
			
			if (Ext.getCmp(paramChecker.buttonId).isVisible())
			{
				switch (uiRefType)
				{
					case 'category':
						firstSentence = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECK_UI_REF_CATEGORY'/>";
						break;
				
					case 'group':
						firstSentence = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECK_UI_REF_GROUP'/>";
						break;
						
					case 'parameter':
						firstSentence = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECK_UI_REF_PARAMETER'/>";
						break;
					
					default:
						throw 'Unknown ui-ref type: ' + uiRefType;
				}
	
				if (paramChecker.getStatus() == Ametys.plugins.core.administration.Config.ParameterChecker.STATUS_FAILURE)
				{
					nbErrors += 1;
					details += firstSentence + " '" + paramChecker.uiRefLabel + "'" + ":\n\t" + paramChecker.errorMsg + "\n\n";
				}
			}
		}
		
		if (nbErrors > 0)
		{
			Ametys.log.ErrorDialog.display({
				title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECK_ERROR'/>", 
				text: nbErrors + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_PARAM_CHECK_ERROR_TEXT'/>",
	    		category: "Ametys.plugins.core.administration.Config._runAllTests",
	    		details: details
			});
		}
	},
	
	/**
	 * @private
	 * Bind the scroll to the toc
	 */
	_bindScroll: function ()
	{
		this._bound = true;
	},
	
	/**
	 * @private
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
	 * @private
	 * Do activates a menu item by its id
	 * @param {String} id The menu id to activate
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

/**
 * @private
 * A class to define an object representing a parameter checker
 */
Ext.define('Ametys.plugins.core.administration.Config.ParameterChecker', {
	
	statics: {
		/**
	     * @protected
	     * @readonly
	     * @property {Number} 
	     * The 'Not tested' status for parameter checkers
	     */
	    STATUS_NOT_TESTED: 0,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 
	     * The 'success' status for parameter checkers
	     */
	    STATUS_SUCCESS: 1,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 
	     * The 'failure' status for parameter checkers
	     */
	    STATUS_FAILURE: 2,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 
	     * The 'deactivated' status for parameter checkers
	     */
	    STATUS_DEACTIVATED: 3,	    
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 
	     * The 'warning' status for parameter checkers
	     */
	    STATUS_WARNING: 4
	},
	
	config: {
		/** @cfg {String} id The identifier */
		id: null,
		/** @cfg {String} label The readable label */
		label: null,
		/** @cfg {String} description The readable description */
		description: null,
		/** @cfg {Number} status A constant of this class to define the current test status */
		status: null,
		/** @cfg {String} errorMsg The error message associated to a failure status */
		errorMsg: null,
		/** @cfg {String} uiRefType equals 'category' || 'group' || 'parameter' */
		uiRefType: null,
		/** @cfg {String} uiRefLabel the label of the category/group/parameter the parameter checker is attached to */
		uiRefLabel: null,
		/** @cfg {Number} order The order of the parameter checker to display tests when many should be rendered at the same location */
		order: null,
		/** @cfg {String} plugin The name of the plugin that declared the test */
	    plugin: null,
		/** @cfg {String} smallIconPath The relative path to the 16x16 icon representing the test */
		smallIconPath: null,
		/** @cfg {String} mediumIconPath The relative path to the 32x32 icon representing the test */
		mediumIconPath: null,
		/** @cfg {String} largeIconPath The relative path to the 48x48 icon representing the test */
		largeIconPath: null,
		/** @cfg {String[]} linkedParams The ids of the parameters linked to this test */
		linkedParams: [],
		/** @cfg {String[]} linkedParamsLabels The readable labels of the parameters linked to this test (in the same order thant #cfg-linkedParams) */
		linkedParamsLabels: [],
		/** @cfg {String} buttonId The id of the button that launch the test */
		buttonId: null,
		/** @cfg {String} helpBoxId The id of the help icon associated to the test */
		helpBoxId: null
	},
	
	constructor: function(paramChecker, uiRefLabel, uiRefType, label, description)
	{
		this.id= paramChecker.id;
		this.label = label;
		this.description = description;
		this.status = this.self.STATUS_NOT_TESTED;
		this.uiRefLabel = uiRefLabel;
		this.uiRefType = uiRefType;
		this.plugin = paramChecker.plugin;
		this.smallIconPath = paramChecker['small-icon-path'];
		this.mediumIconPath = paramChecker['medium-icon-path'];
		this.largeIconPath = paramChecker['large-icon-path'];
		this.linkedParams = paramChecker['linked-params'];
		this.setLinkedParamsLabels();
	},
	
	/**
	 * Set the button id
	 * @param {String} btnId See #cfg-buttonId
	 */
	setButtonId: function(btnId)
	{
		this.buttonId = btnId;
	},
	
	/**
	 * Set the help box id
	 * @param {String} helpBoxId See #cfg-helpBoxId
	 */
	setHelpBoxId: function(helpBoxId)
	{
		this.helpBoxId = helpBoxId;
	},
	
	/**
	 * Set the error message
	 * @param {String} errorMsg See #cfg-errorMsg
	 */
	setErrorMsg: function(errorMsg)
	{
		this.errorMsg = errorMsg;
	},
	
	/**
	 * Set the tooltip associated to error message
	 * @param {String} errorTooltip See #cfg-errorMsg
	 */
	setErrorTooltip: function(errorTooltip)
	{
		this.errorTooltip = errorTooltip;
	},
	
	/**
	 * Set the linked params labels
	 * @param {String[]} linkedParams The linked Params
	 */
	setLinkedParamsLabels: function()
	{
		var linkedParamsLabels = [];
		for (var i = 0; i < this.linkedParams.length; i++)
		{
			var linkedParam = this.linkedParams[i];
			    linkedParamField = Ametys.plugins.core.administration.Config._form.getForm().findField(linkedParam),
			    fieldLabel = linkedParamField.getFieldLabel();
			  
		    if (Ext.String.startsWith(fieldLabel,"*"))
	    	{
		    	fieldLabel = fieldLabel.substring(2);
	    	}
		    
	    	linkedParamsLabels.push(fieldLabel);
    	}
		
		this.linkedParamsLabels = linkedParamsLabels.join(', ');
	},
	
	/**
	 * Get the status
	 * @return {Number} See the #cfg-status
	 */
	getStatus: function()
	{
		return this.status;
	},
	
	/**
	 * Set the status
	 * @param {Number} status See the #cfg-status
	 */
	setStatus: function(status)
	{
		this.status = status;
	}
});
