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
 * org.ametys.form.MultiSelectField
 *
 * @class This class provides a multi-select combo field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.ux.form.MultiSelect
 * @constructor
 * @param {Object} config The configuration options
 */
org.ametys.form.MultiSelectField = function(config) 
{
	config.itemCls = "ametys-multi-select";
	config.labelSeparator = '';
	
	org.ametys.form.MultiSelectField.superclass.constructor.call(this, config);
};

Ext.extend(org.ametys.form.MultiSelectField, Ext.ux.form.MultiSelect, {});

org.ametys.form.MultiSelectField.prototype.onRender = function(ct, position)
{
    // BEGIN OF OVERRIDE OF ONRENDER TO CHANGE TEMPLATE
	Ext.ux.form.MultiSelect.superclass.onRender.call(this, ct, position);

    var fs = this.fs = new Ext.form.FieldSet({
        renderTo: this.el,
        title: this.legend,
        height: this.height,
        width: this.width,
        style: "padding:0;",
        tbar: this.tbar
    });
    fs.body.addClass('ux-mselect');

    this.view = new Ext.ListView({
        selectedClass: 'ux-mselect-selected',
        multiSelect: true,
        store: this.store,
        columns: [{ header: 'Value', width: 1, dataIndex: this.displayField, tpl: this.tpl || "{" + this.displayField + "}" }],
        hideHeaders: true
    });

    fs.add(this.view);

    this.view.on('click', this.onViewClick, this);
    this.view.on('beforeclick', this.onViewBeforeClick, this);
    this.view.on('dblclick', this.onViewDblClick, this);

    this.hiddenName = this.name || Ext.id();
    var hiddenTag = { tag: "input", type: "hidden", value: "", name: this.hiddenName };
    this.hiddenField = this.el.createChild(hiddenTag);
    this.hiddenField.dom.disabled = this.hiddenName != this.name;
    fs.doLayout();
    // END OF OVERRIDE
    
	if (this.desc)
	{
		this.itemCt.child('div.x-form-element div.x-form-field').insertSibling({
			id: this.id + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 6px; float: left;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		Ext.QuickTips.register({
		    target: this.id + '-img',
		    text: this.desc,
		    dismissDelay: 0 // disable automatic hiding
		});
	}
}

org.ametys.form.MultiSelectField.prototype.setWidth = function (width)
{
	org.ametys.form.MultiSelectField.superclass.setWidth.call(this, width);
	if (this.fs != null)
	{
		this.fs.setWidth(width);
	}
}