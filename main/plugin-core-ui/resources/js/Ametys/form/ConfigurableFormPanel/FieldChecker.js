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
 * Class representing a field checker
 */
Ext.define('Ametys.form.ConfigurableFormPanel.FieldChecker', {
	
	statics: {
		/**
	     * @protected
	     * @readonly
	     * @property {Number} 'Not tested' status 
	     */
	    STATUS_NOT_TESTED: 0,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 'Success' status 
	     */
	    STATUS_SUCCESS: 1,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 'Failure' status 
	     */
	    STATUS_FAILURE: 2,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 'Deactivated' status 
	     */
	    STATUS_DEACTIVATED: 3,	    
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 'Warning' status 
	     */
	    STATUS_WARNING: 4,
	    
	    /**
	     * @protected
	     * @readonly
	     * @property {Number} 'Hidden' status 
	     */
	    STATUS_HIDDEN: 5
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
		/** @cfg {Ext.Component} uiComponent the component the parameter checker is graphically attached to */
		uiComponent: null,
		/** @cfg {String} errorMsg The error message associated to a failure status */
		errorMsg: null,
		/** @cfg {String} fieldCheckerPrefix the path prefix of this field checker */
		fieldCheckerPrefix: null,
		/** @cfg {Number} order The order of the field checker to display tests when many should be rendered at the same location */
		order: null,
		/** @cfg {String} plugin The name of the plugin that declared the test */
	    plugin: null,
        /** @cfg {String} iconGlyph The CSS class to use as glyph icon */
        iconGlyph: null,
        /** @cfg {String} iconDecorator The CSS class to use as decorator icon */
        iconDecorator: null,
		/** @cfg {String} smallIconPath The path to the 16x16 icon representing the test */
		smallIconPath: null,
		/** @cfg {String} mediumIconPath The path to the 32x32 icon representing the test */
		mediumIconPath: null,
		/** @cfg {String} largeIconPath The path to the 48x48 icon representing the test */
		largeIconPath: null,
		/** @cfg {String[]} linkedParams The ids of the parameters linked to this test */
		linkedParams: [],
		/** @cfg {String[]} linkedParamsLabels The readable labels of the fields linked to this test (in the same order thant #cfg-linkedParams) */
		linkedParamsLabels: [],
		/** @cfg {Ext.form.Field[]} linkedParamsFields the list of fields associated to this field checker */
		linkedParamsFields: [],
		/** @cfg {String} buttonId The id of the button that launch the test */
		buttonId: null,
		/** @cfg {String} helpBoxId The id of the help icon associated to the test */
		helpBoxId: null,
		/** @cfg {String} statusCmpId The id of the status component associated to the test */
		statusCmpId: null,
		/** @cfg {String} fieldNamePrefix The optional prefix for field names */
		fieldNamePrefix: null,
		/** @cfg {String} pathSeparator The path separator */
		pathSeparator: null
	},
	
	constructor: function(fieldChecker, uiComponent, path, label, description, fieldNamePrefix, pathSeparator)
	{
		this.id = fieldChecker.id;
		
		this.label = label;
		this.description = description;
		
		this.status = this.self.STATUS_NOT_TESTED;

		this.uiComponent = uiComponent;
		
        this.iconGlyph = fieldChecker['icon-glyph'];
        this.iconDecorator = fieldChecker['icon-decorator'];
		this.smallIconPath = fieldChecker['small-icon-path'];
		this.mediumIconPath = fieldChecker['medium-icon-path'];
		this.largeIconPath = fieldChecker['large-icon-path'];
		
		this.linkedFieldsPaths = fieldChecker['linked-fields'] || "";
		this.fieldCheckerPrefix = path;
		this.fieldNamePrefix = fieldNamePrefix;
		this.pathSeparator = pathSeparator;
		
		this.plugin = fieldChecker.plugin;
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
	 * Get the error message.
	 * @return the error message 
	 */
	getErrorMsg: function()
	{
		return this.errorMsg;
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
	},
	
	/**
	 * Set the id of the status component
	 * @param {String} id See the #cfg-status-cmp-id
	 */
	setStatusCmpId: function(id)
	{
		this.statusCmpId = id;
	},
	
	/**
	 * Set the labels of the linked parameter
	 * @param {Number} labelList See the #cfg-linkedParamsLabels
	 */
	setLinkedFieldsLabels: function(labelList)
	{
		this.linkedFieldsLabels = labelList;
	},
    
	/**
	 * Set the list of linked parameters fields of this field checker
	 * @param {Ext.form.Field[]} linkedParamsFields the list of linked parameters fields
	 */
	setLinkedFields: function(linkedFields)
	{
		this.linkedFields = linkedFields;
	},
	
	/**
	 * Get the list of linked parameters fields of this field checker
	 * @return the list of fields that are linked to this parameter checker
	 */
	getLinkedFields: function(linkedFields)
	{
		return this.linkedFields;
	},
	
	/**
	 * Reset this field checker
	 */
	reset: function()
	{
		this.setStatus(this.self.STATUS_NOT_TESTED);
		
		Ext.getCmp(this.buttonId).enable();
		var statusCmp = Ext.getCmp(this.statusCmpId);
		if (statusCmp)
		{
			statusCmp.hide();
		}
	},
	
    /**
     * Is the field checker active?
     * @return {Boolean} true if the parameter checker is active, false otherwise
     */
    isActive: function()
    {
        var status = this.getStatus();
        
        // not part of a switched-off group or deactivated
        return status != Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_HIDDEN
            && status != Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_DEACTIVATED;
    },    
	
	/**
	 * Get the paths of the fields corresponding to the linked parameters of this field checker
	 * The fields can either by relative to the field checker prefix (see #cfg-fieldCheckerPrefix), or absolute,
	 * in which case we have to prepend the given fieldPrefix. 
	 * @return {String[]} the list of linked parameters paths
	 */
	getLinkedFieldsPaths: function()
	{
		var linkedFieldsPaths = [];
		Ext.Array.each(this.linkedFieldsPaths, function (linkedFieldPath) {
			
			if (Ext.String.startsWith(linkedFieldPath, this.pathSeparator))
			{
				// Absolute path
				linkedFieldsPaths.push(this.fieldNamePrefix + linkedFieldPath.substring(this.pathSeparator.length));
			}
			else
			{
				// Relative path : the field prefix is contained in the field checker prefix
				linkedFieldsPaths.push(this.fieldCheckerPrefix + linkedFieldPath);
			}
			
		}, this);
		
		return linkedFieldsPaths;
	}
});
