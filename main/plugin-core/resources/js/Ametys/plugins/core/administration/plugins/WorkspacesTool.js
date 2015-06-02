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
 * This tool displays a tree representing the workspaces
 */
Ext.define('Ametys.plugins.core.administration.tool.WorkspacesTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.tree.Panel} _workspacesTree The plugins by file tree panel
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
	},
		
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function ()
	{
		this._workspacesTree = this._drawWorkspacesPanel();
		return this._workspacesTree;
	},
	
	sendCurrentSelection: function()
	{
		this._onSelect();
	},
	
	/**
	 * Refreshes the tool
	 */
	refresh: function ()
	{
		this.showRefreshing();
		this._workspacesTree.getStore().load({node: this._workspacesTree.getRootNode(), callback: this.showRefreshed, scope: this});
	},
	
	getTree: function()
	{
		return this._workspacesTree;
	},
	
	/**
	 * @private
	 * Draw the tree panel for the plugins by file tool
	 */
	_drawWorkspacesPanel: function()
	{
		var store = Ext.create('Ext.data.TreeStore', {
			model: 'Ametys.plugins.core.administration.Workspaces.Item',
			
			proxy: {
	        	type: 'ametys',
				plugin: 'core',
				url: 'administrator/workspaces',
	        	reader: {
	        		type: 'json',
	        		rootProperty: 'children'
	        	}
	        },
			
			root: {
				expanded: false
			},			
			
	        listeners: {
	        	'load': Ext.bind(this._onLoad, this)
	        }
		});
		
		return new Ext.tree.Panel({
			store: store,
			rootVisible: false,
			
			autoScroll: true,
			border: false,
			
			listeners: {
				'selectionchange': Ext.bind(this._onSelect, this)
			}
		});
	},

	/**
     * @private
     * Listener when store is loaded
     */
	_onLoad: function()
    {
		console.info("on load");
		
		var rootNode = this._workspacesTree.getRootNode();
		
        // Expand first nodes
        this._workspacesTree.getRootNode().expandChildren(false, false, this._workspacesTree._onRootNodesChangedAndExpanded, this);
    },
    
    /**
     * @private
     * When the newly loaded root nodes are expanded
     */
    _onRootNodesChangedAndExpanded: function()
    {
        // Select first node
        this._workspacesTree.getSelectionModel().select(this._workspacesTree.getRootNode().firstChild);
    },
	
    /**
     * @private
     * Send selection message
     */
	_onSelect: function(panel, selection)
	{
		console.info("On select");
//		var targets = [];
//		
//		var selectedCategories = this._logsTree.getSelectionModel().getSelection();
//		Ext.Array.forEach(selectedCategories, function(selectedCategory) {
//			
//			target = Ext.create('Ametys.message.MessageTarget', {
//				type: 'log-category',
//				parameters: {id: selectedCategory.getId(), level: selectedCategory.data.level, category: selectedCategory.data.category}
//			});
//			
//			targets.push(target);
//		});
//		
//		Ext.create('Ametys.message.Message', {
//			type: Ametys.message.Message.SELECTION_CHANGED,
//			targets: targets
//		});
	},
    
	_onModified: function(message)
	{
		console.info("On modified");
//		var targets = message.getTargets("log-category");
//		if (targets.length > 0)
//		{
//			var store = this._logsTree.getStore();
//			var me = this;
//			
//			Ext.Array.forEach(targets, function(target) {
//				var category = target.getParameters().category;
//				var index = store.find("category", category); 
//				if (index != -1)
//				{
//					var level = target.getParameters().level,
//					    modifiedCategory = store.getAt(index); 
//					
//					if (level == 'FORCE')
//					{
//						var level = me._getLevel(modifiedCategory);
//						me._updateNode(null, level, true, true, modifiedCategory);
//					}
//					else if (level == 'INHERIT')
//					{
//						var level = me._getLevel(modifiedCategory.parentNode);
//						me._updateNode(null, level, true, false, modifiedCategory);
//					}
//					else
//					{
//						me._updateNode(null, level, false, false, modifiedCategory);
//					}
//				}
//			});
//			
//		}
	}
});

Ext.define('Ametys.plugins.core.administration.Workspaces.Item', { 
    extend: 'Ext.data.TreeModel', 
    fields: [ 
       { name: 'icon', type: 'string' }, 
       { name: 'text', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}, 
       { name: 'leaf', type: 'boolean'} 
    ] 
}); 
