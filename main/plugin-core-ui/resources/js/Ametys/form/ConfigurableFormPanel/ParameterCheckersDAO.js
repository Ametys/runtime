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
 * @private
 * Class representing a parameter checker attached to a configurable form panel
 * The parameter checker can either be graphically attached to:
 *  - a parameter
 *  - a group
 *  - a category
 */
Ext.define('Ametys.form.ConfigurableFormPanel.ParameterCheckersDAO', {

	/**
	 * @property {Number} AMETYS_DESCRIPTION_WIDTH The width for ametys descriptions
	 * @private
	 * @readonly 
	 */
	AMETYS_DESCRIPTION_WIDTH: 20,
	
	/**
	 * @property {Ametys.form.ConfigurableFormPanel.ParameterChecker[]} _paramCheckers The list of parameter checkers
	 * @private
	 */
	_paramCheckers: [],
	
	/**
	 * @property {Boolean} _isSuspended are the events currently suspended?
	 * @private
	 */
	_isSuspended: true,
	
	/**
	 * @property {Object} _testResults the results of the tests
	 * 					  _testResults.successes the amount of successes
	 * 					  _testResults.failures the amount of failures
	 * 					  _testResults.notTested the amount of untested checkers
	 */
	_testResults : {},
	
	/**
	 * @cfg {Ametys.form.ConfigurableFormPanel} form The form panel the parameter checkers are attached to
	 */
	
	/**
	 * Initialize the list of parameter checkers
	 */
	constructor: function(config)
	{
		this.form = config.form;
		this.form.on('formready', Ext.bind(this._resumeEvents, this));
		
		this._paramCheckers = [];
	},
	
	/**
	 * Add a parameter checker to a category.
	 * @param {Ext.panel.Panel} tab the panel where to add the category checker
	 * @param {Object} paramChecker 
	 * @param {String} paramChecker.id the id of the parameter checker
	 * @param {String} paramChecker.class the class implementing the check 
	 * @param {Object} paramChecker.category the tested category
	 * @param {Number} paramChecker.order the order of the parameter checker (if several are attached to the same parameter/group/category)
	 * @param {String} paramChecker.small-icon-path the path to the small icon representing the parameter checker
	 * @param {String} paramChecker.medium-icon-path the path to the medium icon representing the parameter checker
	 * @param {String} paramChecker.large-icon-path the path to the large icon representing the parameter checker
	 * @param {String[]} paramChecker.linked-params the ids of the parameters used for the checking
	 * @param {String} paramChecker.label the label of the parameter checker
	 * @param {String} paramChecker.description the description of the parameter checker
	 */
	addCategoryChecker: function(tab, paramChecker)
	{
		var parameterChecker = new Ametys.form.ConfigurableFormPanel.ParameterChecker(paramChecker, tab.title, "category", paramChecker.label, paramChecker.description, Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_NOT_TESTED),
			testButton = this._generateTestButton(parameterChecker),
			testContainer = this._generateTestContainer(parameterChecker, testButton);
		
		tab.add(testContainer);
		
		this._paramCheckers.push(parameterChecker); 
	},
	
	/**
	 * Add a parameter checker to a group.
	 * @param {Ext.form.FieldSet} fieldset The fieldset where to add the group checker
	 * @param {Object} paramChecker 
	 * @param {String} paramChecker.id the id of the parameter checker
	 * @param {String} paramChecker.class the class implementing the check 
	 * @param {Object} paramChecker.group the tested group 
	 * @param {Number} paramChecker.order the order of the parameter checker 
	 * @param {String} paramChecker.small-icon-path the path to the small icon representing the parameter checker
	 * @param {String} paramChecker.medium-icon-path the path to the medium icon representing the parameter checker
	 * @param {String} paramChecker.large-icon-path the path to the large icon representing the parameter checker
	 * @param {String} paramChecker.label the label of the group the parameter checker is attached to
	 * @param {String} paramChecker.description the description of the parameter checker
	 */
	addGroupChecker: function(fieldset, paramChecker, label, description)
	{
		var fieldsetCt = fieldset.up();
		
		var uiRefLabel = fieldset.title;
		if (fieldsetCt.hasCls('ametys-form-tab-item'))
    	{
	    	uiRefLabel = fieldsetCt.title + "/" + uiRefLabel;
    	}
		
		var parameterChecker = new Ametys.form.ConfigurableFormPanel.ParameterChecker(paramChecker, uiRefLabel, "group", paramChecker.label, paramChecker.description),
		    testButton = this._generateTestButton(parameterChecker),
		    testContainer = this._generateTestContainer(parameterChecker, testButton);
		
		fieldset.add(testContainer);
		
		this._paramCheckers.push(parameterChecker);
	},
	
	/**
	 * Add a parameter checker to a parameter.
	 * @param {Ext.form.Field} field the field the parameter checker is attached to
	 * @param {Object} paramChecker 
	 * @param {String} paramChecker.id the id of the parameter checker
	 * @param {String} paramChecker.class the class implementing the check 
	 * @param {String} paramChecker.param-ref the id of the tested parameter
	 * @param {Number} paramChecker.order the order of the parameter checker (if several are attached to the same parameter/group/category)
	 * @param {String} paramChecker.small-icon-path the path to the small icon representing the parameter checker
	 * @param {String} paramChecker.medium-icon-path the path to the medium icon representing the parameter checker
	 * @param {String} paramChecker.large-icon-path the path to the large icon representing the parameter checker
	 * @param {String[]} paramChecker.linked-params the ids of the parameters used for the checking
	 * @param {String} paramChecker.label the label of the parameter checker
	 * @param {String} paramChecker.description the description of the parameter checker
	 */
	addParameterChecker: function(paramChecker)
	{
		var field = this.form.getField(paramChecker['param-ref'].replace(/\./g, '_')), // FIXME
		    uiRefLabel = field.getFieldLabel(),
	    	fieldCt = field.up(),
	    	fieldCtParent = fieldCt.up();
		
	    if (Ext.String.startsWith(uiRefLabel, "*"))
    	{
	    	uiRefLabel = uiRefLabel.substring(2);
    	}
	    
	    if (fieldCt.hasCls('ametys-fieldset'))
    	{
	    	uiRefLabel = fieldCt.title + "/" + uiRefLabel;
    	}
	    
	    if (fieldCtParent.hasCls('ametys-form-tab-item'))
    	{
	    	uiRefLabel = fieldCtParent.title + "/" + uiRefLabel;
    	}
	    
	    var parameterChecker =  new Ametys.form.ConfigurableFormPanel.ParameterChecker(paramChecker, uiRefLabel, "parameter", paramChecker.label, paramChecker.description),
	    	testButton = this._generateTestButton(parameterChecker),
	    	testContainer = this._generateTestContainer(parameterChecker, testButton);
	    
	    fieldCt.add(testContainer);
	    
		this._paramCheckers.push(parameterChecker);
	},
	
	/**
	 * @private 
     * Listener to resume the events once the form is ready 
	 */
	_resumeEvents: function()
	{
		this._isSuspended = false;
	},
	
	
	/**
	 * Initialize the listeners on linked parameters
     * To be called once fields are created
	 */
	initializeParamCheckers: function()
	{
		var me = this;
		
		// Parameter checkers listeners
		Ext.Array.each(this._paramCheckers, function(paramChecker){
			
			var linkedParamsLabels = [];
			Ext.Array.each(paramChecker.linkedParams, function(linkedParam) {

				var	linkedParamField = me.form.getField(linkedParam.replace(/\./g, '_')); // FIXME
				var fieldLabel = linkedParamField.getFieldLabel();
				
				linkedParamField.on('change', Ext.bind(me._updateTestButton, me, [paramChecker], false));
				linkedParamField.on('disable', Ext.bind(me._updateTestButton, me, [paramChecker], false));
				linkedParamField.on('enable', Ext.bind(me._updateTestButton, me, [paramChecker], false));
				linkedParamField.on('warningchange', Ext.bind(me.form._updateTabsStatus, me.form));
			})
		});
		
		// Initialize test results 
		this.form.on({
			// Update test results after the parameter checkers are created
			formready: {fn: this._updateTestResults, scope: this, single: true, order: 'after'} 
		});
	},
	
	/**
	 * @private
	 * Generates the button launching the tests on the corresponding parameter/group/category
	 * @param {Ametys.form.ConfigurableFormPanel.ParameterChecker} paramChecker the parameter checker 
	 */
	_generateTestButton: function(paramChecker)
	{	
		var me = this;
		
		return Ext.create('Ext.button.Button', {
			id: Ext.id(),
			paramChecker: paramChecker,
			tooltip: '',
			icon: Ametys.CONTEXT_PATH + paramChecker.smallIconPath,
			text: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_TEXT_PREFIX'/>" + " \"" + paramChecker.label + "\"",
			cls: 'param-checker-component',
            border: false,
            handler: function(btn, event)
            {
        		me.check([paramChecker], false);
            }
		});
	},
	
	/**
	 * @private
	 * Generates the container that holds the test button and the ametys description
	 * @param {Ametys.form.ConfigurableFormPanel.ParameterChecker} paramChecker the parameter checker 
	 * @param {Ext.button.Button} testButton the test button
	 * @return {Ext.container.Container} the container with the test button and the help box
	 */
	_generateTestContainer: function(paramChecker, testButton)
	{
		var me = this,
			helpBoxId = Ext.id(),
			items = [testButton];
			
		paramChecker.setButtonId(testButton.getId());
		paramChecker.setHelpBoxId(helpBoxId);
		
		items.push({
			id: helpBoxId,
			xtype: 'component', 
			cls: "ametys-description",
		    baseCls: '', 
		    padding: "3 0 0 5",
			width: Ametys.form.ConfigurableFormPanel.ParameterCheckersDAO.AMETYS_DESCRIPTION_WIDTH,
			listeners: {
				'render': function() {
					this.getEl().set({"data-qtip": me._generateHelpBoxTip(paramChecker)});
				}
			}
		});
		
		return Ext.create('Ext.Container', {
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
	 * @param {Ametys.plugins.admin.Config.ParameterChecker} paramChecker the parameter checker 
	 */
	_generateHelpBoxTip: function(paramChecker)
    {
		var me = this,
			linkedParamsLabels= [];
		
		// Set the labels of the linked parameters in the param checker
		Ext.Array.each(paramChecker.linkedParams, function(linkedParam) {
			var	linkedParamField = me.form.getField(linkedParam.replace(/\./g, '_')); // FIXME
			var fieldLabel = linkedParamField.getFieldLabel();
			
			if (Ext.String.startsWith(fieldLabel,"*"))
			{
				fieldLabel = fieldLabel.substring(2);
			}
			
			linkedParamsLabels.push(fieldLabel);
			paramChecker.setLinkedParamsLabels(linkedParamsLabels.join(', '));
		});

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
			   				"- <i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_TOOLTIP_TESTED_PARAMS'/>" + paramChecker.linkedParamsLabels +
			   			'</td>' +
				   '</tr>' +
			'</table>'
			);
		
		tpl.applyOut(paramChecker, tipHtml);
		return tipHtml.join("");
	},
	
	/**
	 * @private
	 * Updates the test results panel in the actions' tooltips
	 */
	_updateTestResults: function()
	{
		var nbTests = this._paramCheckers.length,
			notTested = 0,
			failures = 0,
			successes = 0;
		
		for (var i = 0; i < nbTests; i++)
		{
			var paramChecker = this._paramCheckers[i],
			    status = paramChecker.getStatus();
			
			switch (status)
			{
				case Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_SUCCESS: 
					successes += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE: 
					failures += 1;
					break;
				
				case Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_NOT_TESTED:
					notTested += 1;
					break;
				
				case Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_DEACTIVATED:
					notTested += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_WARNING:
					notTested += 1;
					break;
				
				case Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_HIDDEN: // ignore
					break;
					
				default:
					throw 'Unknown status ' + status;
			}
		}
		
		this._testResults =  {successes: successes, failures: failures, notTested: notTested};
		
		// Modified message to update the buttons state and description
		Ext.create("Ametys.message.Message", {
			type: Ametys.message.Message.MODIFIED,
			parameters: {},
			targets: {
				type: Ametys.message.MessageTarget.CONFIGURATION,
				parameters: {},
				subtargets: [ this.form.getMessageTargetConf() ]
			}
		});
	},
	
	/**
	 * @private
	 * Sets the test button to the "warning" status, updates the button's tooltip and the test results panel
	 * @param {Ametys.plugins.admin.Config.ParameterChecker} paramChecker the parameter checker to be updated
	 */	
	_updateTestButton: function(paramChecker)
	{
		if (this._isSuspended)
		{
			return;
		}
		
		var btn = Ext.getCmp(paramChecker.buttonId),
			helpBox = Ext.getCmp(paramChecker.helpBoxId),
			linkedParams = paramChecker.linkedParams,
		    allLinkedParamsDisabled = true,
		    invalidLinkedParameter = false;
	
	    for (var i = 0; i < linkedParams.length; i++)
		{
			var linkedParam = linkedParams[i],
				linkedParamField = this.form.getField(linkedParam.replace(/\./g, '_')); //FIXME

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
	    	paramChecker.setStatus(Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_WARNING);
    	}
	    else
    	{
	    	btn.disable();
			paramChecker.setStatus(Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_DEACTIVATED);
    	}
	    
	    // update the tooltip
	    this._generateStatusTip(btn.up('container').getEl(), paramChecker);
	    helpBox.getEl().set({"data-qtip": this._generateHelpBoxTip(paramChecker)});
	    
	    // update the test results 
	    this._updateTestResults();
	    this.form._updateTabsStatus();
	},
	
	/**
	 * @private
	 * Generates the HTML code for the parameter checker's status tooltip.
	 * @param {Ext.Element} el The element to add tooltips on
	 * @param {Ametys.plugins.admin.Config.ParameterChecker} paramChecker the parameter checker 
	 */
	_generateStatusTip: function(el, paramChecker)
    {
		var tipHtml = [],
			tpl = new Ext.XTemplate(
			   					"<tpl if='status == " + Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_NOT_TESTED + "'><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_STATUS_NOT_TESTED'/></tpl>" +
					   			"<tpl if='status == " + Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_SUCCESS + "'><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_STATUS_SUCCESS'/></tpl>" +
					   			"<tpl if='status == " + Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE + "'><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_STATUS_FAILURE'/></br>" +
					   				'<tr>' + 
					   					'<td colspan="2">' +
					   						"- <i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_TOOLTIP_MESSAGE'/> <strong>" + paramChecker.errorMsg + "</strong>" +
				   						'</td>' +
			   						'</tr>' +
				   				"</tpl>" +
				   				"<tpl if='status == " + Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_DEACTIVATED + "'>" +
				   					"<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_STATUS_DEACTIVATED'/> </br>" +
				   					'<tr>' + 
				   						'<td colspan="2">' +
				   							"<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_TOOLTIP_MESSAGE'/>" +
				   							"<strong><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_TOOLTIP_DEACTIVATED_MESSAGE'/></strong>" +
											'</td>' +
			   						'</tr>' +
								"</tpl>" +
			   					"<tpl if='status == " + Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_WARNING + "'>" +
					   				"<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_STATUS_WARNING'/> </br>" +
					   				"<tpl if='errorMsg != null'>" +
				  		   				'<tr>' + 
				  		   					'<td colspan="2">' +
				  		   						"<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_TOOLTIP_MESSAGE2'/> <strong>" + paramChecker.errorMsg + "</strong>" +
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

		if (paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_WARNING)
		{
			el.set({"data-warnqtip": tip}); 
		}
		else if (paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE)
		{
			el.set({"data-errorqtip": tip});
		}
		else if (paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_DEACTIVATED)
		{
			el.set({"data-warnqtip": tip}); 
		}
    },
    
    /**
	 * @private
	 * Update the warnings on fields
	 */
	_updateWarnings: function()
	{
		var me = this;
		
		Ext.Array.each(this._paramCheckers, function(paramChecker){
			var button = Ext.getCmp(paramChecker.buttonId),
				helpBox = Ext.getCmp(paramChecker.helpBoxId),
				success = paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_SUCCESS,
				failure = paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE,
				warning = paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_WARNING,
				warningMsg = "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_WARNING_TEXT_BEGINNING'/>" + paramChecker.label + "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_WARNING_TEXT_END'/>";	
			
			Ext.Array.each(paramChecker.linkedParams, function(linkedParam){
				var linkedParamField = me.form.getField(linkedParam.replace(/\./g, '_')); // FIXME
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
			});
		});
	},
	
	/**
	 * @private
	 * Displays the error dialogs at the end of a check
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
						firstSentence = "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_UI_REF_CATEGORY'/>";
						break;
				
					case 'group':
						firstSentence = "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_UI_REF_GROUP'/>";
						break;
						
					case 'parameter':
						firstSentence = "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_UI_REF_PARAMETER'/>";
						break;
					
					default:
						throw 'Unknown ui-ref type: ' + uiRefType;
				}
	
				if (paramChecker.getStatus() == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE)
				{
					nbErrors += 1;
					details += firstSentence + " '" + paramChecker.uiRefLabel + "'" + ":\n\t" + paramChecker.errorMsg + "\n\n";
				}
			}
		}
		
		if (nbErrors > 0)
		{
			Ametys.log.ErrorDialog.display({
				title: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_ERROR'/>", 
				text: nbErrors + " <i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_ERROR_TEXT'/>",
	    		category: this.self.getName(),
	    		details: details
			});
		}
	},
    
    /**
     * Calls the server-side with the configuration parameters.
     * @param {Ametys.form.ConfigurableFormPanel.ParameterChecker[]} paramCheckers the parameter checkers to use
     * @param {Boolean} displayErrors true if the errors have to be displayed at the end of the tests
     * @param {Function} [callback] the optional callback for this function.
     * @param {Boolean} [forceTest=true] to replay even successful tests
     */
    check: function(paramCheckers, displayErrors, callback, forceTest)
    {
        var form = this.form;
        
        forceTest = forceTest !== false ? true : false;
        
        var formValues = form.getValues(),
            params = {};
        
        // FIXME
        Ext.Object.each(formValues, function(fieldName){
            params[fieldName.replace(/\_/g, '.')] = this[fieldName];
        });
        
        // Add the ids of the involved parameter checkers 
        var paramCheckersIds = [];
        Ext.Array.each(paramCheckers, function(paramChecker) {
            var btn = Ext.getCmp(paramChecker.buttonId);
            btn.disable();
            if (forceTest || paramChecker.getStatus() != Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_SUCCESS)
            {
                btn.getEl().mask("");
                paramCheckersIds.push(paramChecker.id);
            }
        });

        params.paramCheckersIds = paramCheckersIds.join(",");
        
        // Server call
        Ext.Ajax.request({
            url: Ametys.getPluginDirectPrefix("admin") + "/config/test", 
            params: params, 
            callback: Ext.bind(this._checkCb, this, [paramCheckers, callback, displayErrors], true)
        }); 
    },
    
    /**
     * @private
     * Callback function invoked upon reception of the server's response
     * @param {Object} options the Ext.Ajax.request call configuration
     * @param {Object} options.params the call parameters
     * @param {boolean} success true if the request succeeded, false otherwise
     * @param {Object} response the server's response
     * @param {Ametys.plugins.admin.Config.ParameterChecker[]} paramCheckers the parameter checker
     * @param {Function} callback the optional callback for this function.
     * @param {Boolean} displayErrors true if the errors have to be displayed at then end of the tests
     */
    _checkCb: function(options, success, response, paramCheckers, callback, displayErrors)
    {   
        var form = this.form;
        
        var paramCheckersDAO = form._paramCheckersDAO;
        if (!success)
        {
            Ametys.log.ErrorDialog.display({
                title: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_SERVER_CALL_ERROR_TITLE'/>", 
                text: "<i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_PARAM_CHECKER_SERVER_CALL_ERROR_TEXT'/>",
                category: this.self.getName()
            });
            
            // Re-enable buttons and reinitialize test buttons
            for (var i = 0; i < paramCheckers.length; i++)
            {
                paramCheckersDAO._updateTestButton(paramCheckers[i]);
            }
            
            return;
        }
        
        var params = options.params,
            result = response.responseXML,
            errors = 0;

        // Server's response handler
        Ext.Array.each(paramCheckers, function(paramChecker) {
            var btn = Ext.getCmp(paramChecker.buttonId),
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
                paramChecker.setStatus(Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_SUCCESS);
                paramChecker.setErrorMsg(null);
                
                paramCheckersDAO._generateStatusTip(btn.up('container').getEl(), paramChecker);
                helpBox.getEl().set({"data-qtip": paramCheckersDAO._generateHelpBoxTip(paramChecker)});
            }
            else
            {
                errors++;
                btn.removeCls('success');
                btn.addCls('failure');
                helpBox.addCls('offset');
                paramChecker.setStatus(Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE);
                paramChecker.setErrorMsg(errorMsg);
                
                paramCheckersDAO._generateStatusTip(btn.up('container').getEl(), paramChecker);
                helpBox.getEl().set({"data-qtip": paramCheckersDAO._generateHelpBoxTip(paramChecker)});
            }
        });
        
        paramCheckersDAO._updateTestResults();
        paramCheckersDAO.form._updateTabsStatus(); 
        paramCheckersDAO._updateWarnings();
        
        if (callback && typeof callback === 'function') 
        {
            callback(errors == 0);
        }
                    
        if (displayErrors)
        {
            paramCheckersDAO._displayErrorDialog();
        }
    }
});
	