/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys.form');

/**
 * org.ametys.form.TextAreaField
 *
 * @class This class provides a multi-line text field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.form.TextArea
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.TextAreaField = function(config) 
{
	config.itemCls = "ametys-input";
	config.labelSeparator = '';
	
	org.ametys.form.TextAreaField.superclass.constructor.call(this, config);
	
	this.addEvents(
			/**
		     * @event beforeclick
		     * Fires before click processing. Return false to cancel the default action.
		     * @param {Node} this This node
		     * @param {Ext.EventObject} e The event object
		    */
		    "keyup"
	);
}; 

Ext.extend(org.ametys.form.TextAreaField, Ext.form.TextArea, {});

org.ametys.form.TextAreaField.prototype.onRender = function(ct, position)
{
	org.ametys.form.TextAreaField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.el.insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 7px;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc,
	        
	        dismissDelay: 0 // disable automatic hiding
	    });
	}
}
