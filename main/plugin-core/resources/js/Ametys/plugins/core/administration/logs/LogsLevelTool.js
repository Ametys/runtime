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
Ext.define('Ametys.plugins.core.administration.logs.LogsLevelTool', {
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
			model: 'Ametys.plugins.core.administration.Logs.Category',
			
			root: {
				expanded: false
			},			
			
	        proxy: {
	        	type: 'ametys',
				plugin: 'core',
				url: 'administrator/logs-level',
	        	reader: {
	        		type: 'json',
					rootProperty: 'children'
	        	}
	        },
	        
	        listeners: {
	        	'load': Ext.bind(this._onLoad, this)
	        }
		});
		
		return new Ext.tree.Panel({
			store: store,
			rootVisible: false,
			
			border: false,
			autoScroll: true,
			
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
						var level = me._getLevel(modifiedCategory);
						me._updateNode(null, level, true, true, modifiedCategory);
					}
					else if (level == 'INHERIT')
					{
						var level = me._getLevel(modifiedCategory.parentNode);
						me._updateNode(null, level, true, false, modifiedCategory);
					}
					else
					{
						me._updateNode(null, level, false, false, modifiedCategory);
					}
				}
			});
			
		}
	},
	
	_getLevel: function(node)
    {
    	if (node.get('level') != 'inherit')
    	{
    		return node.get('level');
    	}
    	else
    	{
    		return this._getLevel(node.parentNode);
    	}
    },

	_updateNode: function (node, level, inherited, force, modifiedCategory)
	{
		var selection = node == null;
        inherited = inherited == true;
        node = node != null ? node : modifiedCategory;

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

Ext.define('Ametys.plugins.core.administration.Logs.Category', { 
    extend: 'Ext.data.TreeModel', 
    fields: [ 
        { name: 'id', type: 'string' }, 
        { name: 'parentLevel', type: 'string'},
        { name: 'icon', type: 'string', calculate: function(data) {
        		return Ametys.getPluginResourcesPrefix("core") + "/img/administrator/logs/loglevel_" + (data.level.toLowerCase() == "inherit" ? data.parentLevel.toLowerCase() + "-inherit": data.level.toLowerCase()) + ".png"
        	} 
        }, 
        { name: 'text', type: 'string', mapping: 'name', sortType: Ext.data.SortTypes.asNonAccentedUCString},
        { name: 'category', type: 'string', mapping: 'fullname'},
        { name: 'level', type: 'string', mapping: 'level', sortType: Ext.data.SortTypes.asNonAccentedUCString},
    ] 
}); 
