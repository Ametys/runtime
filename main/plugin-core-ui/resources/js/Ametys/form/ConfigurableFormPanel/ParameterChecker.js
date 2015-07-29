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
 * Class representing a parameter checker
 */
Ext.define('Ametys.form.ConfigurableFormPanel.ParameterChecker', {
	
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
		/** @cfg {String} smallIconPath The path to the 16x16 icon representing the test */
		smallIconPath: null,
		/** @cfg {String} mediumIconPath The path to the 32x32 icon representing the test */
		mediumIconPath: null,
		/** @cfg {String} largeIconPath The path to the 48x48 icon representing the test */
		largeIconPath: null,
		/** @cfg {String[]} linkedParams The ids of the parameters linked to this test */
		linkedParams: [],
		/** @cfg {String[]} linkedParamsLabels The readable labels of the parameters linked to this test (in the same order thant #cfg-linkedParams) */
		linkedParamsLabels: [],
		/** @cfg {String} buttonId The id of the button that launch the test */
		buttonId: null,
		/** @cfg {String} helpBoxId The id of the help icon associated to the test */
		helpBoxId: null,
		/** @cfg {String} statusCmpId The id of the status component associated to the test */
		statusCmpÎd: null
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
	 * Set the tooltip associated to error message
	 * @param {String} errorTooltip See #cfg-errorMsg
	 */
	setErrorTooltip: function(errorTooltip)
	{
		this.errorTooltip = errorTooltip;
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
	setLinkedParamsLabels: function(labelList)
	{
		this.linkedParamsLabels = labelList;
	},
    
    /**
     * Is the param checker active? (not part of a switched-off group or deactivated)
     * @return {Boolean} true if active
     */
    isActive: function()
    {
        var status = this.getStatus();
        return status != Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_HIDDEN
            && status != Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_DEACTIVATED;
    }    
});
