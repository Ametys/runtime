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
 * This class that provides default configuration for tree node inline editing.
 */
Ext.define('Ametys.tree.TreeEditor', {
	extend: 'Ext.Editor',
	
	alias: 'widget.treeeditor',
	
	maxWidth: 200,
	alignment: 'tl-tl',
	updateEl: true,
	ignoreNoChange: false,
	
	/**
	 * @property {Ext.tree.Panel} _tree See #cfg-tree
	 * @private
	 */
	/**
	 * @cfg {Ext.tree.Panel} tree (required) The editable tree panel
	 */
	
	constructor: function(config)
	{
		Ext.applyIf (config, {
			field: {
				cls: 'treenode-edit',
				xtype: 'textfield',
				allowBlank: false,
				selectOnFocus: true
			}
		});
		
		this._tree = config.tree;
		
		this.callParent(arguments);
	},
	
	/**
	 * Get the associated tree
	 * @return {Ext.tree.Panel} The tree
	 */
	getTree: function ()
	{
		return this._tree;
	},
	
	getValue : function()
	{
		return Ext.String.trim(this.field.getValue())
    }
});