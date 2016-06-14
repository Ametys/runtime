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
 * @private
 * Class registering the field checkers and allowing to instantiate and manipulate them
 */
Ext.define('Ametys.form.ConfigurableFormPanel.FieldCheckersManager', {
	
	/**
	 * @property {Ametys.form.ConfigurableFormPanel} _form the configurable form panel
	 * @private
	 */
	
	/**
	 * @property {Ametys.form.ConfigurableFormPanel.FieldChecker[]} _fieldCheckers The list of field checkers handled
	 * @private
	 */
	_fieldCheckers: [],
	
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
	 * @cfg {Ametys.form.ConfigurableFormPanel} form The form panel the field checkers are attached to
	 */
    
    /**
     * @cfg {Boolean} hideDisabledButtons=false Set to true to hide the disabled buttons
     */
    hideDisabledButtons: false,
	
	constructor: function(config)
	{
		this._form = config.form;
		this._form.on('formready', Ext.bind(this._resumeEvents, this));
		
		this._fieldCheckers = [];
        
        this.hideDisabledButtons = config.hideDisabledButtons === true;
	},
	
	/**
	 * Reset the warnings and the status of the field checkers
	 */
	reset: function()
	{
		Ext.Array.each(this._fieldCheckers, function(fieldChecker)
		{
			fieldChecker.reset();
		});
		
		// Reset the field checkers first, otherwise the warnings will not be reset 
		this._updateWarnings();
	},
	
	/**
	 * Register one or more field checkers
	 * @param {Ext.container.Container} container the container where to insert the field checker(s)
	 * @param {Object/Object[]} fieldCheckersCfg the configuration of the field checker(s) to register
	 * @param {String} path the path of the field checker(s)
	 * @param {Number} offset The offset of the field checker(s) 
     * @param {Number} roffset The right offset of the field checker(s) 
     * @param {Ext.form.Field} [field] when the field checker is graphically attached to a field, the field it is attached to, null otherwise
	 */
	addFieldCheckers: function(container, fieldCheckersCfg, path, offset, roffset, field)
	{
		if (fieldCheckersCfg.length > 1)
		{
			fieldCheckersCfg.sort(Ext.bind(this._compareByOrder, this));
		}
		
		Ext.Array.each(fieldCheckersCfg, function(fieldCheckerCfg){
			
			if (this._form._isElement(fieldCheckerCfg))
			{
				fieldCheckerCfg = this._getFieldCheckerCfgFromXML(fieldCheckerCfg);
			}
			
			this._addFieldChecker(container, fieldCheckerCfg, path, offset, roffset, field);
		}, this);
	},
	
	/**
	 * @private
	 * Register a field checker
	 * @param {Ext.container.Container} container the container where to insert the field checker
	 * @param {Object} fieldCheckerCfg The configuration of the field checker to add. 
	 * @param {String} fieldCheckerCfg.id the id of the field checker
	 * @param {String} fieldCheckerCfg.class the class implementing the check 
	 * @param {Number} fieldCheckerCfg.order the order of the field checker (if several are attached to the same location)
	 * @param {String} fieldCheckerCfg.small-icon-path the path to the small icon representing the field checker
	 * @param {String} fieldCheckerCfg.medium-icon-path the path to the medium icon representing the field checker
	 * @param {String} fieldCheckerCfg.large-icon-path the path to the large icon representing the field checker
	 * @param {String[]} fieldCheckerCfg['linked-fields'] the ids of the parameters used for the checking
	 * @param {String} fieldCheckerCfg.label the label of the field checker
	 * @param {String} fieldCheckerCfg.description the description of the field checker
	 * @param {String} uiRefLabel the label corresponding to the field checker's location 
	 * @param {String} prefix the path prefix of the field checker
	 * @param {Number} offset The offset of the field checker 
     * @param {Number} roffset The right offset of the field checker 
     * @param {Ext.form.Field} [field] when the field checker is graphically attached to a field, the field it is attached to, null otherwise
	 */
	_addFieldChecker: function(container, fieldCheckerCfg, path, offset, roffset, field)
	{	
	    var fieldChecker =  new Ametys.form.ConfigurableFormPanel.FieldChecker(fieldCheckerCfg, field || container, path, fieldCheckerCfg.label, fieldCheckerCfg.description, this._form.getFieldNamePrefix(), this._form.defaultPathSeparator),
	    	testButton = this._generateTestButton(fieldChecker, offset),
	    	testContainer = this._generateTestContainer(fieldChecker, testButton, offset, roffset);
    	
	    container.add(testContainer);
	    
	    // Handle the case of repeaters : field checkers are created dynamically
	    var parentContainer = container.up();
	    if (parentContainer)
    	{
	    	var parentParentContainer = parentContainer.up();
    	}
	    
	    if (container.isRepeater || (parentContainer && parentContainer.isRepeater) || (parentParentContainer && parentParentContainer.isRepeater))
    	{
	    	this._initializeFieldChecker(fieldChecker);
	    	this.updateTestResults();
    	}
	    
		this._fieldCheckers.push(fieldChecker);
	},
	
	/**
	 * @private
     * Create a field checker object configuration from an XML configuration
     * @param {HTMLElement} fieldChecker the field checker's XML configuration
     */
    _getFieldCheckerCfgFromXML: function(fieldChecker)
    {
    	return {
            'id': Ext.dom.Query.selectValue("> id", fieldChecker),
            'small-icon-path':  Ext.dom.Query.selectValue("> small-icon-path", fieldChecker),
            'medium-icon-path':  Ext.dom.Query.selectValue("> medium-icon-path", fieldChecker),
            'large-icon-path':  Ext.dom.Query.selectValue("> large-icon-path", fieldChecker),
            'icon-glpyh':  Ext.dom.Query.selectValue("> icon-glyph", fieldChecker),
            'linked-fields':  Ext.JSON.decode(Ext.dom.Query.selectValue("> linked-fields", fieldChecker)),
            'label':  Ext.dom.Query.selectValue("> label", fieldChecker),
            'description':  Ext.dom.Query.selectValue("> description", fieldChecker),
            'order':  Ext.dom.Query.selectValue("> order", fieldChecker)
        };
    },
	
	/**
	 * @private
     * Compare two field checkers with their order
     * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} a the first field checker
     * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} b the second field checker
     */
    _compareByOrder: function(a, b) 
    {
        var aOrder = this._form._isElement(a) ? Ext.dom.Query.selectValue('order', a) : a.order;
        var bOrder = this._form._isElement(b) ? Ext.dom.Query.selectValue('order', b) : b.order;
    	
        var comparison = 0;
        if (aOrder && bOrder)
    	{
      	    comparison = aOrder - bOrder;
    	}
        
        return comparison != 0 ? comparison : 1;
    },
	
	/**
	 * Initialize the listeners on linked fields
	 */
	initializeFieldCheckers: function()
	{
		// Field checkers listeners
		Ext.Array.each(this._fieldCheckers, function(fieldChecker){
			if (!fieldChecker.isInitialized())
			{
				this._initializeFieldChecker(fieldChecker);
			}
		}, this);
		
		if (!Ext.isEmpty(this._fieldCheckers))
		{
			// Initialize test results if there is at least one field checker 
			this._form.on({
				// Update test results after the field checkers are created
				formready: {fn: this.updateTestResults, scope: this, single: true, order: 'after'} 
			});
		}
	},
	
	/**
	 * @private
	 * Initialization of a single field checker
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} fieldChecker the field checker to initialize 
	 */
	_initializeFieldChecker: function (fieldChecker)
	{
		var linkedFields = [],
			linkedFieldsLabels = [];
		
		var linkedFieldsPaths = fieldChecker.getLinkedFieldsPaths();

		Ext.Array.each(linkedFieldsPaths, function(linkedFieldPath) {
			
			var	linkedField = this._form.getForm().findField(linkedFieldPath);
			var isHidden = linkedField.isHidden();
			
			linkedFields.push(linkedField);

			if (!isHidden)
			{
				// Remove the starting or trailing '*' character
		        var fieldLabel = linkedField.getFieldLabel();
		        if (Ext.String.startsWith(fieldLabel, '*'))
		        {
		            fieldLabel = fieldLabel.substr(1).trim();
		        }
		        else if (Ext.String.endsWith(fieldLabel, '*'))
		        {
		            fieldLabel = fieldLabel.substr(0, fieldLabel.length - 1).trim();
		        }
				
		        linkedFieldsLabels.push(fieldLabel);
				
		        linkedField.on('change', Ext.bind(this._updateTestButton, this, [fieldChecker], false)); 
		        linkedField.on('errorchange', Ext.bind(this._updateTestButton, this, [fieldChecker], false)); 
		        linkedField.on('disable', Ext.bind(this._updateTestButton, this, [fieldChecker], false));
		        linkedField.on('enable', Ext.bind(this._updateTestButton, this, [fieldChecker], false)); 
				
		        linkedField.on('warningchange', Ext.bind(this._form._updateTabsStatus, this._form, [false], false));
			}
		}, this);
		
		// Store the list of linked fields and their labels 
		fieldChecker.setLinkedFields(linkedFields);
		fieldChecker.setLinkedFieldsLabels(linkedFieldsLabels.length > 1 ? linkedFieldsLabels.join(', ') : "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_NO_LINKED_FIELD}}");
		
		if (!this._form.isFormReady())
		{
			// Update the test buttons once as soon as the form is ready
			this._form.on({
				formready: {fn: Ext.bind(this._updateTestButton, this, [fieldChecker], false), scope: this, single: true} 
			});
		}
		else if (this._form._addingRepeaterEntry)
		{
			this._form.on({
				// Update the test buttons once as soon as the repeater entry is ready
				repeaterentryready: {fn: Ext.bind(this._updateTestButton, this, [fieldChecker], false), scope: this, single: true} 
			});
		}
		else
		{
			this._updateTestButton(fieldChecker);
		}
		
		// Complete initialization: the initialization of field checkers on repeater items is dynamic, and we don't want those
		// to be initialized twice
		fieldChecker.setInitialized(true);
	},
	
	/**
	 * @private
	 * Generates the button launching the tests on the corresponding location
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} fieldChecker the field checker 
	 * @param {Number} offset the field checker offset
	 */
	_generateTestButton: function(fieldChecker, offset)
	{	
		return Ext.create('Ext.button.Button', {
			id: Ext.id(),
			
			text: fieldChecker.label,
			textAlign: 'left',
			
            iconCls: fieldChecker.iconGlyph + (fieldChecker.iconDecorator ? ' ' + fieldChecker.iconDecorator : ''),
            icon: Ext.isEmpty(fieldChecker.iconGlyph) ? Ametys.CONTEXT_PATH + fieldChecker.smallIconPath : null,
                    
			cls: 'param-checker-button',
			
			width: Ametys.form.ConfigurableFormPanel.LABEL_WIDTH - offset + Ametys.form.ConfigurableFormPanel.FIELD_MINWIDTH,

			border: true,
            tooltip: '',
            
            fieldChecker: fieldChecker,
            handler: function()
            {
        		this.check([fieldChecker], false);
            },
            scope: this
		});
	},
	
	/**
	 * @private
	 * Generates the container that wraps the test button and the ametys description
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} fieldChecker the field checker 
	 * @param {Ext.button.Button} testButton the test button
	 * @param {Number} offset the offset of the field checker
	 * @param {Number} roffset the right offset of the field checker
	 * @return {Ext.container.Container} the container with the test button and the help box
	 */
	_generateTestContainer: function(fieldChecker, testButton, offset, roffset)
	{
		var items = [],
		    me = this;
		
        items.push({
            xtype: 'tbspacer',
            flex: 1
        });
		
		// the button itself
		items.push(testButton);
        fieldChecker.setButtonId(testButton.getId());
		
        // component initially hidden that will reflect the status of the check
		var fieldCheckerStatusCmpId = Ext.id();
		fieldChecker.setStatusCmpId(fieldCheckerStatusCmpId);
        items.push({
            xtype: 'component',
            id: fieldCheckerStatusCmpId,
            
            hidden: true,
            cls: 'param-checker-status'
        });
        
		// the description
		var helpBoxId = Ext.id();
		fieldChecker.setHelpBoxId(helpBoxId);
		items.push({
			xtype: 'component', 

			id: helpBoxId,
			cls: "ametys-description",
		    
			listeners: {
				'render': function() {
					Ext.tip.QuickTipManager.register(me._getAmetysDescriptionTooltipConfig(fieldChecker, this.getEl()));
				}
			}
		});
		
		return Ext.create('Ext.Container', {
			layout: {
				type: 'hbox',
				align: 'stretch',
				pack: 'start'
			},
			style: 'margin-right:' + Math.max(this._form.maxNestedLevel * Ametys.form.ConfigurableFormPanel.OFFSET_FIELDSET - roffset, 0) + 'px',
			cls: 'param-checker-container',
			items: items
		});
	},

	/**
	 * Updates the test results panel in the actions' tooltips
	 * Fires a 'testresultschange' event if the test results have changed
	 */
	updateTestResults: function()
	{
		var notTested = 0,
			failures = 0,
			successes = 0;
		
		Ext.Array.each(this._fieldCheckers, function(fieldChecker){
			var status = fieldChecker.getStatus();
			switch (status)
			{
				case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_SUCCESS: 
					successes += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE: 
					failures += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED:
					notTested += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_DEACTIVATED:
					notTested += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_WARNING:
					notTested += 1;
					break;
					
				case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_HIDDEN: // ignore
					break;
					
				default:
					throw 'Unknown status ' + status;
			}
		});
		
		var testResults = {successes: successes, failures: failures, notTested: notTested};
		if (!Ext.Object.equals(this._testResults, testResults))
		{
			this._testResults = testResults;
			this._form.fireEvent('testresultschange');
		}
	},
	
	/**
	 * @private 
     * Listener function invoked to resume the events as soon as the {@link Ametys.form.ConfigurableFormPanel} is ready 
	 */
	_resumeEvents: function()
	{
		this._isSuspended = false;
	},
	
	/**
	 * @private
	 * Run the update routine on the test button if the attached  {@link Ametys.form.ConfigurableFormPanel} is ready.
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} fieldChecker the field checker to update
	 */	
	_updateTestButton: function(fieldChecker)
	{
		if (this._isSuspended)
		{
			return;
		}
		
		var isDeactivated = this._isDeactivated(fieldChecker),
			statusCmp = Ext.getCmp(fieldChecker.statusCmpId),
			btn = Ext.getCmp(fieldChecker.buttonId);
		
		if (fieldChecker.getStatus() != Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED)
		{
			if (isDeactivated)
			{
				statusCmp.setVisible(false);
				fieldChecker.setStatus(Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED);
			}
			else
			{
				btn.enable();
		    	btn.up('container').setVisible(true);
		    	
		    	statusCmp.removeCls(['success', 'failure', 'warning']);
		    	statusCmp.setVisible(false);
		    	if (fieldChecker.getStatus() != Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_DEACTIVATED)
				{
		    		statusCmp.addCls('warning');
		    		statusCmp.setVisible(true);
		    		fieldChecker.setStatus(Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_WARNING);
				}
			    
			    // update the tooltip of the status component
			    this._updateStatusCmpTooltip(fieldChecker);
			    
			    // update the test results 
			    this.updateTestResults();
		    	
			    // update the warnings
			    this._updateWarnings();
			    
			    // update the tabs of the attached form
			    this._form._updateTabsStatus();
			}
		}
		
		// We want to be able to deactivate a field checker even when its status is 'not tested'
	    if (isDeactivated)
		{
	    	btn.disable();
	        btn.up('container').setHidden(this.hideDisabledButtons);
	        
	        fieldChecker.setStatus(Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_DEACTIVATED);
	        if (!this.hideDisabledButtons)
        	{
	        	statusCmp.removeCls(['success', 'failure']);
	        	statusCmp.addCls('warning');
	        	statusCmp.setVisible(true);
        	}
	        
	        // update the tooltip of the status component
	        this._updateStatusCmpTooltip(fieldChecker);
		}
	},
	
	/**
	 * @private
	 * Test whether the given field checker is activated or not 
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} fieldChecker the field checker to test
	 * @return true if the field checker is deactivated, false otherwise
	 */	
	_isDeactivated: function(fieldChecker)
	{
		var linkedFields = fieldChecker.getLinkedFields(),
		    isDeactivated = true;

		Ext.Array.each(linkedFields, function(linkedField) {
	    	
	    	// check if all linked fields are disabled
			if (!linkedField.isDisabled())
			{
				isDeactivated = false;
			}
    	});
	
		return isDeactivated;
	},
	
	/**
	 * @private
	 * Test whether the given field checker has an invalid linked field 
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} fieldChecker the field checker to test
	 * @return true if the field checker has at least one invalid linked field, false otherwise
	 */	
	_hasInvalidField: function(fieldChecker)
	{
		var linkedFields = fieldChecker.getLinkedFields(),
			hasInvalidField = false;
	
		Ext.Array.each(linkedFields, function(linkedField) 
		{
			if (!linkedField.isDisabled() && linkedField.isVisible() && !Ext.isEmpty(linkedField.getActiveErrors())) 
			{
				hasInvalidField = true;
				return false;
			}
		});
		
		return hasInvalidField;
	},
	
    /**
	 * @private
	 * Update the warnings on fields
	 */
	_updateWarnings: function()
	{
        Ext.suspendLayouts();
        
		Ext.Array.each(this._fieldCheckers, function(fieldChecker){
			var button = Ext.getCmp(fieldChecker.buttonId),
				helpBox = Ext.getCmp(fieldChecker.helpBoxId),
				notTested = fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED,
				success = fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_SUCCESS,
				failure = fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE,
				warningMsg = "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_WARNING_TEXT_BEGINNING}}" + fieldChecker.label + "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_WARNING_TEXT_END}}";	
			
			Ext.Array.each(fieldChecker.getLinkedFields(), function(linkedField){
				linkedField._warnings = linkedField._warnings || {};
				
				var activeWarnings = linkedField.getActiveWarnings();
				if (success || notTested)
				{
					Ext.Array.remove(activeWarnings, warningMsg);
					linkedField._warnings[fieldChecker.id] = null;
					
					linkedField.markWarning(activeWarnings);
				}
				else if (failure)
				{
					if (!linkedField._warnings[fieldChecker.id])
					{
						linkedField._warnings[fieldChecker.id] = warningMsg;
						activeWarnings.push(warningMsg);
					}
					
					linkedField.markWarning(activeWarnings);
				}
			});
		});
        
        Ext.resumeLayouts(true);        
	},
	
	/**
	 * @private
	 * Updates the tooltip for the status component of the given field checker
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} the field checker
	 */
	_updateStatusCmpTooltip: function(fieldChecker)
	{
		var target = Ext.getCmp(fieldChecker.statusCmpId).getEl();
			tooltipCfg = 
			{
				target: target,
				inribbon: false
			};

		switch (fieldChecker.getStatus())
		{
			case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE: 
				tooltipCfg.text = '<strong>{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_STATUS_FAILURE}}</strong> ' + fieldChecker.errorMsg
				break;
				
			case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_DEACTIVATED:
				tooltipCfg.text = "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_TOOLTIP_DEACTIVATED_MESSAGE}}";
				break;
				
			case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_WARNING:
				tooltipCfg.text = "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_STATUS_WARNING}}" + 
					    (!fieldChecker.errorMsg ? "" : 
				    	" </br></br>" +
						"{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_TOOLTIP_LAST_MESSAGE}} " + "\"" + fieldChecker.errorMsg + "\"");
				break;

			case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_SUCCESS: // no tooltip
				tooltipCfg = {};
				break;
			
			case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED: // no tooltip
				tooltipCfg = {};
				break;
				
			case Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_HIDDEN: // no tooltip
				tooltipCfg = {};
				break;
				
			default:
				throw 'Unknown status ' + status;
		}
		
		if (fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_WARNING || fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_DEACTIVATED)
		{
			tooltipCfg.cls = 'x-tip-form-warning';
			tooltipCfg.image = Ametys.getPluginResourcesPrefix('core-ui') + '/themes/theme-ametys-admin/images/form/field/warning.png';
			tooltipCfg.imageHeight = 17;
			tooltipCfg.imageWidth = 17;
		}
		else if (fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE)
		{
			tooltipCfg.cls = 'x-tip-form-invalid';
			tooltipCfg.image = Ametys.getPluginResourcesPrefix('core-ui') + '/themes/theme-ametys-admin/images/form/exclamation.png';
			tooltipCfg.imageHeight = 17;
			tooltipCfg.imageWidth = 17;
		}
		
		if (!Ext.Object.isEmpty(tooltipCfg))
		{
			Ext.tip.QuickTipManager.register(tooltipCfg);
		}
		else
		{
			Ext.tip.QuickTipManager.unregister(target);
		}
	},
	
	/**
	 * @private
	 * Get the tooltip configuration for a given field checker's ametys description
	 * @param {Ametys.form.ConfigurableFormPanel.FieldChecker} the field checker
	 * @param {Ext.dom.Element} target the target element of the tooltip
	 * @return {Object} the configuration object for the tooltip
	 */
	_getAmetysDescriptionTooltipConfig: function(fieldChecker, target)
	{
		var tooltipCfg = 
		{
			target: target,
			title: fieldChecker.label,
			inribbon: false,
			text: '<emphasis>' + fieldChecker.description + '</emphasis><br /><br />' 
			+ "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_TOOLTIP_TESTED_PARAMS}}" + fieldChecker.linkedFieldsLabels
		};
		
		if (fieldChecker.iconGlyph)
		{
			tooltipCfg.glyphIcon = fieldChecker.iconGlyph;
			tooltipCfg.iconDecorator = fieldChecker.iconDecorator;
		}
		else if (fieldChecker.largeIconPath)
		{
			tooltipCfg.image = Ametys.CONTEXT_PATH + fieldChecker.largeIconPath;
		}
		
		return tooltipCfg;
	},
	
    /**
     * Calls the server-side with the values of the linked fields.
     * @param {Ametys.form.ConfigurableFormPanel.FieldChecker[]} fieldCheckers the field checkers to use, can be null to run all the tests
     * @param {Boolean} displayErrors true if the errors have to be displayed at the end of the tests
     * @param {Function} [callback] the optional callback for this function.
     * @param {Boolean} [forceTest=true] to replay even successful tests
     */
    check: function(fieldCheckers, displayErrors, callback, forceTest)
    {
        var form = this._form;
		    forceTest = forceTest !== false ? true : false;
		    fieldCheckers = fieldCheckers != null ? fieldCheckers : this._fieldCheckers; 
        
        var formValues = {};
        
        // Add information concerning the involved field checkers 
        var fieldCheckersInfo = {};
        Ext.Array.each(fieldCheckers, function(fieldChecker) {
            if (forceTest || fieldChecker.getStatus() != Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_SUCCESS)
            {
            	if (!this._hasInvalidField(fieldChecker))
        		{
            		// Reset the error message before the test
            		fieldChecker.setErrorMsg(null);
            		
            		var btn = Ext.getCmp(fieldChecker.buttonId);
	            		btn.disable();
	            		btn.getEl().mask("");
            		
            		var fieldCheckerId = fieldChecker.id;
            			fieldCheckersInfo[fieldCheckerId] = {};
            		
            		var	rawTestValues = [],
            			testParamsNames = [];
            		
            		// Add the names and the raw values of the linked parameters
            		Ext.Array.each(fieldChecker.getLinkedFields(), function(linkedField){
            			rawTestValues.push(linkedField.getValue());
            			testParamsNames.push(linkedField.getName());
            		});
            		
            		fieldCheckersInfo[fieldCheckerId].rawTestValues = rawTestValues;
            		fieldCheckersInfo[fieldCheckerId].testParamsNames = testParamsNames;
        		}
            	else
        		{
            		fieldChecker.setErrorMsg("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_INVALID_LINKED_FIELD}}");
        		}
            }
        }, this);
        
        // Don't run tests if there is no test to run... go directly to the check callback routine instead 
        if (Ext.Object.isEmpty(fieldCheckersInfo))
    	{
        	this._checkCb({}, true, {}, fieldCheckers, callback, displayErrors);
    	}
        else
    	{
	        // Server call
	        Ext.Ajax.request({
	        	url: this._form.getTestURL(),  
	            params: "fieldCheckersInfo=" + encodeURIComponent(Ext.JSON.encode(fieldCheckersInfo)) + "&formValues=" + encodeURIComponent(Ext.JSON.encode(formValues)), 
	            callback: Ext.bind(this._checkCb, this, [fieldCheckers, callback, displayErrors], true)
	        });
    	}
    },
    
    /**
     * @private
     * Callback function invoked upon reception of the server's response
     * @param {Object} options the {@link Ext.Ajax.request} call configuration
     * @param {Object} options.params the call parameters
     * @param {boolean} success true if the request succeeded, false otherwise
     * @param {Object} response the server's response
     * @param {Ametys.form.ConfigurableFormPanel.FieldChecker[]} fieldCheckers the field checkers
     * @param {Function} callback the optional callback for this function.
     * @param {Boolean} displayErrors true if the errors have to be displayed at then end of the tests
     */
    _checkCb: function(options, success, response, fieldCheckers, callback, displayErrors)
    {   
        if (!success)
        {
            Ametys.log.ErrorDialog.display({
                title: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_SERVER_CALL_ERROR_TITLE}}", 
                text: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_SERVER_CALL_ERROR_TEXT}}",
                category: this.self.getName()
            });
            
            // Re-enable buttons and reinitialize test buttons
            for (var i = 0; i < fieldCheckers.length; i++)
            {
            	var fieldChecker = fieldCheckers[i];
            	var btn = Ext.getCmp(fieldChecker.buttonId);
            	
            	btn.getEl().unmask();
            	btn.enable();
            }
            
            return;
        }
        
        var form = this._form,
            result = response.responseXML,
            errorMessages = Ext.dom.Query.selectDirectElements("* > *", result); // We cannot use a better selector because of possible "." in the tagName.
            errors = 0;

        // Server's response handler
        Ext.Array.each(fieldCheckers, function(fieldChecker) {
            var btn = Ext.getCmp(fieldChecker.buttonId),
                helpBox = Ext.getCmp(fieldChecker.helpBoxId),
                statusCmp = Ext.getCmp(fieldChecker.statusCmpId);
                
            // Compute the corresponding error message
            Ext.Array.each(errorMessages, function(errorMessage){
            	if (errorMessage.tagName == fieldChecker.id)
        		{
            		fieldChecker.setErrorMsg(errorMessage.childNodes[0].textContent); 
        		}
            });
            
            btn.getEl().unmask();
            btn.enable();
            
            statusCmp.setVisible(true);
            statusCmp.removeCls(['failure', 'warning', 'success']);
            
            // Update the field checker's component
            if (fieldChecker.getErrorMsg() == null)
            {
            	statusCmp.addCls('success');
            	fieldChecker.setStatus(Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_SUCCESS);
            }
            else
            {
                errors++;
                statusCmp.addCls('failure');
                
                fieldChecker.setStatus(Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE);
            }
            
            this._updateStatusCmpTooltip(fieldChecker);
        }, this);
        
        this.updateTestResults();
        this._form._updateTabsStatus(); 
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
	 * Displays the error dialogs at the end of a check
	 */
	_displayErrorDialog: function()
	{
		var nbErrors = 0,
			mainFormContainerId = this._form.getFormContainer().getId();
			details = '';
		
		for (var i = 0; i < this._fieldCheckers.length; i++)
		{
			var fieldChecker = this._fieldCheckers[i];
			if (Ext.getCmp(fieldChecker.buttonId).isVisible() && fieldChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE)
			{
				var fullLabel = "";
				nbErrors += 1;
				if (fieldChecker.uiComponent.getId() == mainFormContainerId)
				{
					fullLabel = "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_GLOBAL_FIELD_CHECKER}}";
				}
				else
				{
					fullLabel = this._form._getFullLabel(fieldChecker.uiComponent);
				}
				
				details += fullLabel + ":\n\t" + fieldChecker.errorMsg + "\n\n";
			}
		}
		
		if (nbErrors > 0)
		{
			Ametys.log.ErrorDialog.display({
				title: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_ERROR}}", 
				text: nbErrors > 1 ? nbErrors + " {{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_ERROR_TEXT}}" : "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_FIELD_CHECKER_SINGLE_ERROR_TEXT}}",
	    		category: this.self.getName(),
	    		details: details
			});
		}
	}
});
	