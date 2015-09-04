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
 * &lt;p&gt;This tool displays a tree if log categories, and allows the administrator to perform several operations such as :&lt;/p&gt;
 * &lt;ul&gt;
 * 	&lt;li&gt; change the log levels &lt;/li&gt;
 * 	&lt;li&gt; force log level inheritance &lt;/li&gt;
 * 	&lt;li&gt; inherit log level &lt;/li&gt;
 * &lt;/ul&gt;
 * @private
 */
Ext.define('Ametys.plugins.admin.logs.LogsLevelTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.tree.Panel} _logsTree The logs tree panel
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
		this._logsTree = this._drawLogsLevelPanel();
		return this._logsTree;
	},
	
	sendCurrentSelection: function()
	{
		this._onSelectCategory();
	},
	
	/**
	 * Refreshes the tool
	 */
	refresh: function ()
	{
		this.showRefreshing();
		this._logsTree.getStore().load({node: this._logsTree.getRootNode(), callback: this.showRefreshed, scope: this});
	},

    /**
     * Get the tree of the tool
     * @return {Ext.tree.Panel} The main tree component of the tool
     */
	getTree: function()
	{
		return this._logsTree;
	},
	
	/**
	 * @private
	 * Draw the tree panel for the log levels
	 */
	_drawLogsLevelPanel: function()
	{
		var store = Ext.create('Ext.data.TreeStore', {
			model: 'Ametys.plugins.admin.logs.LogsLevelTool.Category',
			
			root: {
				expanded: false
			},			
			
	        proxy: {
	        	type: 'ametys',
				plugin: 'admin',
				url: 'logs-level',
	        	reader: {
	        		type: 'json',
					rootProperty: 'children'
	        	}
	        },
	        
	        listeners: {
	        	'load': Ext.bind(this._onLoad, this)
	        },
	        
	        sorters: [ { property: 'text', direction: "ASC" }]
	        
		});
		
		return new Ext.tree.Panel({
			store: store,
			rootVisible: false,
			
			border: false,
			scrollable: true,
			
			listeners: {
				'selectionchange': Ext.bind(this._onSelectCategory, this)
			}
		});
	},
	
    /**
     * @private
     * Listener when store is loaded
     */
	_onLoad: function()
    {
		var rootNode = this._logsTree.getRootNode();
		
		// Set the parent level field on each node of the tree
		this._setParentLevels(rootNode.firstChild.data.level, rootNode.firstChild.childNodes);
		
        // Expand first nodes
        this._logsTree.getRootNode().expandChildren(false, false, this._logsTree._onRootNodesChangedAndExpanded, this);
    },
    
    /**
     * @private
     * Set the parent levels on the tree nodes
     * @param {String} parentLevel the level of the parent node
     * @param {Ext.data.NodeInterface[]} children the list of child nodes
     */
    _setParentLevels: function(parentLevel, children)
    {
    	var me = this;
    	Ext.Array.forEach(children, function(child) {
    		child.set('parentLevel', parentLevel);
    		
    		if (child.childNodes)
			{
    			parentLevel = child.data.level == "inherit" ? parentLevel : child.data.level;
    			me._setParentLevels(parentLevel, child.childNodes);
			}
    	});
    },
    
    /**
     * @private
     * When the newly loaded root nodes are expanded
     */
    _onRootNodesChangedAndExpanded: function()
    {
        // Select first node
        this._logsTree.getSelectionModel().select(this._logsTree.getRootNode().firstChild);
    },
	
    /**
     * @private
     * Send selection message
     */
	_onSelectCategory: function()
	{
		var targets = [];
		
		var selectedCategories = this._logsTree.getSelectionModel().getSelection();
		Ext.Array.forEach(selectedCategories, function(selectedCategory) {
			
			target = Ext.create('Ametys.message.MessageTarget', {
				type: 'log-category',
				parameters: {id: selectedCategory.getId(), level: selectedCategory.data.level, category: selectedCategory.data.category}
			});
			
			targets.push(target);
		});
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
    
    /**
     * @private
     * Listener on the message bus of type modified
     * @param {Ametys.message.Message} message The message
     */
	_onModified: function(message)
	{
		var targets = message.getTargets("log-category");
		if (targets.length > 0)
		{
			var store = this._logsTree.getStore();
			var me = this;
			
			Ext.Array.forEach(targets, function(target) {
				var category = target.getParameters().category;
				var index = store.find("category", category); 
				if (index != -1)
				{
					var level = target.getParameters().level,
					    modifiedCategory = store.getAt(index); 
					
					if (level == 'FORCE')
					{
						var level = modifiedCategory.getResolvedLevel();
						me._updateNode(modifiedCategory, level, true, true);
					}
					else if (level == 'INHERIT')
					{
						var level = modifiedCategory.parentNode.getResolvedLevel(); // bug ?
						me._updateNode(modifiedCategory, level, true, false);
					}
					else
					{
						me._updateNode(modifiedCategory, level, false, false);
					}
				}
			});
			
		}
	},
	
    /**
     * @private
     * Update node info
     * @param {Ametys.plugins.admin.logs.LogsLevelTool.Category} node The node to update
     * @param {String} level The level to set
     * @param {Boolean} inherited Is the level inherited
     * @param {Boolean} force True to force recursiverly the level inheritance
     */
	_updateNode: function (node, level, inherited, force)
	{
		var selection = node == null;
        inherited = inherited == true;

        if (!selection || !force)
        {
    		node.set('level', inherited ? "inherit" : level);
			
    		// Find the first parent containing the appropriate level information
    		var parentNode = node.parentNode;
			while (parentNode.data.level == "inherit")
			{
				parentNode = parentNode.parentNode;
			}
			node.set('parentLevel', parentNode.data.level);
        }		
		
		for (var i = 0; i < node.childNodes.length; i++)
		{
			var childNode = node.childNodes[i];
			if (childNode.get('level') == "inherit" || force)
			{
				this._updateNode(childNode, level, true, force);
			}
		}
	}
});
