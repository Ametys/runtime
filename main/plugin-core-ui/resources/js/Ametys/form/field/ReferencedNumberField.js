/*
 *  Copyright 2013 Anyware Services
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
 * A numeric text field that provides a read-only value of reference next to the number field
 * 
 * 		Ext.create('Ext.form.Panel', {
 * 				title: 'My form',
 * 				renderTo: Ext.getBody(),
 * 				items: [{
 * 					xtype: 'referencednumberfield',
 * 					name: 'size',
 * 					fieldLabel: 'Width (px)',
 * 					minValue: 0,
 * 					value: 40,
 * 					ref-value: '48 px',
 * 					ref-cls: 'rule',
 * 					ref-description: 'The real width in pixels with padding and margin'
 * 				}]
 * 		});
 */
Ext.define('Ametys.form.field.ReferencedNumberField', {
	extend: 'Ext.form.field.Number',
	
	alias: ['widget.referencednumberfield'],
	
	/**
	 * @cfg {String} ref-value The value of the value of reference
	 */
	/**
	 * @property {String} _referenceValue See #cfg-ref-value.
	 * @private 
	 */
	
	/**
	 * @cfg {String} ref-cls CSS class to use for the value of reference
	 */
	/**
	 * @property {String} _refCls See #cfg-ref-cls.
	 * @private
	 */
	
	constructor: function (config)
	{
		this.callParent(arguments);
		
		this._refCls = config['ref-cls'];
		this._referenceValue = config['ref-value'];
	},

	/**
	 * Set the value of reference
	 * @param {String} value The value to set
	 */
	setReferenceValue: function (value)
	{
		this._referenceValue = value;
		
		if (this.rendered)
		{
            this.getEl().set({"data-refvalue": Ext.isString(value) ? value : ""});
		}
	},
	
    /**
     * @private
     * Add graphical stuffs when rendering is ok.
     */
	afterRender: function ()
	{
		this.callParent(arguments);
		
        this.setReferenceValue(this._referenceValue);
	}
});
