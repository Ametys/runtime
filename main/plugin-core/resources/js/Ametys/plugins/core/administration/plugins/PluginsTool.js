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
 * &lt;p&gt;This tool displays the tree of plugins by file, and allows the administrator to perform several operations such as :&lt;/p&gt;
 * &lt;ul&gt;
 * 	&lt;li&gt; see the documentation &lt;/li&gt;
 * 	&lt;li&gt; activate/deactivate plugins &lt;/li&gt;
 * 	&lt;li&gt; reboot the application with the changes made &lt;/li&gt;
 * &lt;/ul&gt;
 */
Ext.define('Ametys.plugins.core.administration.plugins.PluginsTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.tree.Panel} _tree The plugins by file tree panel
	 */
	
	/**
	 * @private
	 * @property {Boolean} _hasChanges true if changes were made, false otherwise
	 */
	
	/**
	 * @private
	 * @property {String} _proxyUrl the url of the proxy of the associated tree store
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		this._proxyUrl = config.proxyUrl;
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFYING, this._onModifying, this);
	},
	
	onOpen: function()
	{
		this.callParent(arguments);
		
		var hasPendingChanges = Ametys.plugins.core.administration.plugins.PluginsDAO.hasPendingChanges();
		this.setDirty(hasPendingChanges);
	},
	
	setDirty: function (dirty)
	{
		this.callParent(arguments);
		this._tree.getDockedItems()[0].setVisible(dirty);
	},
		
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function ()
	{
		this._tree = this._drawPanel();
		return this._tree;
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
		this._tree.getStore().load({node: this._tree.getRootNode(), callback: this.showRefreshed, scope: this});
	},

	/**
	 * @private
	 * Draw the tree panel for the plugins by file tool
	 */
	_drawPanel: function()
	{
		var store = Ext.create('Ext.data.TreeStore', {
			model: 'Ametys.plugins.core.administration.Plugins.Item',
			
			proxy: {
	        	type: 'ametys',
				plugin: 'core',
				url: this._proxyUrl,
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
			cls: 'plugins-tool',
			store: store,
			rootVisible: false,
			
			autoScroll: true,
			border: false,
			
			dockedItems: [
	              {
	            	  xtype: 'component',
	            	  cls: 'hint',
	            	  hidden: true,
	            	  dock: 'top',
	            	  html: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_PENDING'/>"
	              }
			],
					
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
		var rootNode = this._tree.getRootNode();
		
        // Expand first nodes
        this._tree.getRootNode().expandChildren(false, false, this._tree._onRootNodesChangedAndExpanded, this);
    },
    
    /**
     * @private
     * When the newly loaded root nodes are expanded
     */
    _onRootNodesChangedAndExpanded: function()
    {
        // Select first node
        this._tree.getSelectionModel().select(this._tree.getRootNode().firstChild);
    },
	
    /**
     * @private
     * Send selection message
     */
	_onSelect: function()
	{
		var targets = [];
		
		var selectedNodes = this._tree.getSelectionModel().getSelection();
		Ext.Array.forEach(selectedNodes, function(selectedNode) {
			
			var nodeData = selectedNode.data;
			
			var activeFeature = false,
				inactiveFeature = false;
			
			if (nodeData.type == 'feature')
			{
				if (nodeData.cause == "")
				{
					activeFeature = nodeData.active;
				}
				else
				{
					inactiveFeature = nodeData.cause == "EXCLUDED";
				}
			}
			
			target = Ext.create('Ametys.message.MessageTarget', {
				type: 'plugin-by-file-node',
				parameters: {
								id: selectedNode.getId(), 
								type: nodeData.type,
								active: nodeData.active,
								activeFeature: activeFeature, // if true the "deactivate" action is enabled
								inactiveFeature: inactiveFeature, // if true the "activate" action is enabled
								selectable: nodeData.type == 'extension' && selectedNode.parentNode.data.isMultiple == "false", 
								pluginName: nodeData.pluginName, 
								featureName: nodeData.featureName,
								componentName: nodeData.componentName,
								extensionPointName: nodeData.extensionPointName,
								parentExtensionPointName: selectedNode.parentNode != null ? selectedNode.parentNode.data.extensionPointName : "", // for the "select" action
								extensionId: nodeData.extensionId
							}
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
	 * Listener when the tree is being modified
	 * @param {Ametys.message.Message} message The modified message.
	 */
	_onModifying: function (message)
	{
		this.setDirty(Ametys.plugins.core.administration.plugins.PluginsDAO.hasPendingChanges());
	}
});

Ext.define('Ametys.plugins.core.administration.Plugins.Item', { 
    extend: 'Ext.data.TreeModel', 
    fields: [ 
       { name: 'icon', type: 'string' }, 
       { name: 'text', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}, 
       { name: 'type', type: 'string' }, // can be 'plugin', 'extension', 'feature', 'component', 'extension-point'
       { name: 'active', type: 'boolean' }, // true if its an active feature
       { name: 'cause', type: 'string' }, // if inactive, the cause of inactivation
       { name: 'isMultiple', type: 'string' }, // is it a multiple extension point 
       { name: 'pluginName', type: 'string' }, // the name of the plugin bringing the element
       { name: 'featureName', type: 'string' }, // the name of the feature bringing the lement
       { name: 'componentName', type: 'string' }, // the name of the component
       { name: 'extensionPointName', type: 'string'}, // the extension point if
       { name: 'extensionId', type: 'string'}, // the extension if
       { name: 'leaf', type: 'boolean'} 
    ] 
}); 