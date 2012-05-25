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

Ext.namespace('org.ametys');

/**
 * org.ametys.Fieldset
 *
 * @class This class provides a collapsible panel 
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.Fieldset = function(config) 
{
	org.ametys.Fieldset.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.Fieldset, Ext.Panel, 
{
	baseCls : "ametys-fieldset",
	collapsible: true,
	titleCollapse: true,
	animCollapse : true,
	border: false
});

org.ametys.Fieldset.prototype._onExpand = function (panel)
{
	if (panel.ownerCt.ownerCt)
		panel.ownerCt.ownerCt.doLayout();
	else
		panel.ownerCt.doLayout();
}

org.ametys.Fieldset.prototype.onRender = function(ct, position)
{
	org.ametys.Fieldset.superclass.onRender.call(this, ct, position);
	
	this.el.insertFirst({tag: "a", name :this.id});

	if (Ext.isIE6 || Ext.isIE7)
	{
		this.addListener('expand', this._onExpand, this);
	}
}

//----------------------------------------------------------------------------------
org.ametys.TBarFieldset = function(config) 
{
	org.ametys.TBarFieldset.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.TBarFieldset, org.ametys.Fieldset, 
{
	cls : "ametys-tbar-fieldset"
});

org.ametys.TBarFieldset.prototype.onRender = function(ct, position)
{
	org.ametys.TBarFieldset.superclass.onRender.call(this, ct, position);
	
	// move bbar before body
	this.bwrap.dom.appendChild(this.bwrap.dom.firstChild);
}
