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
Ext.define('Ametys.plugins.admin.tool.WorkspacesTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.tree.Panel} _workspacesTree The plugins by file tree panel
	 */
	
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
		this._onSelectWorkspace();
	},
	
	/**
	 * Refreshes the tool
	 */
	refresh: function ()
	{
		this.showRefreshing();
		this._workspacesTree.getStore().load({node: this._workspacesTree.getRootNode(), callback: this.showRefreshed, scope: this});
	},

    /**
     * Get the tree panel of the tool
     * @return {Ext.tree.Panel} The tree panel instance. Can be null if called before the #createPanel
     */
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
			model: 'Ametys.plugins.admin.Workspaces.Item',
			
			proxy: {
	        	type: 'ametys',
				plugin: 'admin',
				url: 'workspaces',
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
			
			scrollable: true,
			border: false,
			
			listeners: {
				'selectionchange': Ext.bind(this._onSelectWorkspace, this)
			}
		});
	},

	/**
     * @private
     * Listener when store is loaded
     */
	_onLoad: function()
    {
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
    _onSelectWorkspace: function(panel, selection)
	{
    	var targets = [];
		
		var selections = this._workspacesTree.getSelectionModel().getSelection();
		Ext.Array.forEach(selections, function(node) {
			targets.push({
				id: node.get('isRootWorkspaces') ? 'ametys-workspace-root' : 'ametys-workspace',
				parameters: {
					name: node.get('text')
				}
			});
		});
		
    	Ext.create('Ametys.message.Message', {
    		type: Ametys.message.Message.SELECTION_CHANGED,
    		targets: targets
    	});
	}
});


