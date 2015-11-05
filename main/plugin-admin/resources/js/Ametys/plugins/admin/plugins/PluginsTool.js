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
Ext.define('Ametys.plugins.admin.plugins.PluginsTool', {
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
		
		var hasPendingChanges = Ametys.plugins.admin.plugins.PluginsDAO.hasPendingChanges();
		this.setDirty(hasPendingChanges);
	},
	
	setDirty: function (dirty)
	{
		this.callParent(arguments);
		this._tree.getDockedItems()[2].setVisible(dirty);
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
			model: 'Ametys.plugins.admin.plugins.PluginsTool.Plugin',
			
			proxy: {
	        	type: 'ametys',
				plugin: 'admin',
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
			
			scrollable: true,
			border: false,
			
			dockedItems: [
				  this._getFilterToolbarConfig(),
				  {
					  dock: 'top',
					  xtype: 'button',
					  hidden: true,
					  itemId: 'no-result',
					  cls: 'hint',
					  text: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_FILTER_NO_MATCH'/>" + "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_FILTER_NO_MATCH_ACTION'/>",
					  scope: this,
					  handler: this._clearSearchFilter
				  },
	              {
	            	  xtype: 'component',
	            	  cls: 'hint',
	            	  hidden: true,
	            	  dock: 'top',
	            	  html: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_CHANGES_PENDING'/>"
	              }
			],
					
			listeners: {
				'selectionchange': Ext.bind(this._onSelect, this)
			}
		});
	},
	
	/**
	 * @private
	 * Get the filter toolbar config
	 * @return {Object} The filter toolbar config
	 */
	_getFilterToolbarConfig: function()
	{
		return {
			dock: 'top',
			xtype: 'toolbar',
			layout: 'column',
			border: false,
			defaultType: 'button',
			items: [{
						// Filter input
						xtype: 'textfield',
						cls: 'ametys',
						columnWidth: 1,
						itemId: 'plugins-filter-input',
						emptyText: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_FILTER'/>",
						enableKeyEvents: true,
						minLength: 3,
						minLengthText: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_FILTER_INVALID'/>",
						msgTarget: 'qtip',
						listeners: {keyup: { fn: this._onKeyUp, scope: this, delay: 500}}
					}, 
					{
						// Clear filter
						width: 20,
						tooltip: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_CLEAR_FILTER'/>",
						handler: Ext.bind (this._clearSearchFilter, this),
						icon: Ametys.getPluginResourcesPrefix('admin') + '/img/plugins/clear.gif',
						cls: 'x-btn-text-icon',
						style: {
							marginRight: '20px'
						}
					}, 
					{
						// Expand all
						width: 20,
						tooltip: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_EXPAND_ALL'/>",
						handler: Ext.bind (this._expandAll, this, [], false),
						icon: Ametys.getPluginResourcesPrefix('admin') + '/img/plugins/expand-all.gif',
						cls: 'x-btn-text-icon'
					},
					{
						// Collapse all
						width: 20,
						tooltip: "<i18n:text i18n:key='PLUGINS_ADMIN_PLUGINS_COLLAPSE_ALL'/>",
						handler: Ext.bind (this._collapseAll, this, [], false),
						icon: Ametys.getPluginResourcesPrefix('admin') + '/img/plugins/collapse-all.gif',
						cls: 'x-btn-text-icon'
					}
			]
		};
	},
	
    /**
     * @private
     * Listen on 'keyup' event
     * Filters the tree nodes by entered text.
     * @param {Ext.form.field.Text} field This text field
     */
	_onKeyUp: function(field)
    {
	    this._filterField = field;
	    this._tree.getStore().clearFilter();
        var val = Ext.String.escapeRegex(field.getRawValue());
        
        if (val.length > 2)
        {
            this._regexFilter = new RegExp(val, 'i');
            
            this._tree.getStore().filter({
                filterFn: Ext.bind(this._filterByTextAndChildren, this)
            });
        }
        else
        {
            this._regexFilter = null;
        }
        
        this._tree.getDockedItems()[1].setVisible(!this._tree.getStore().getCount());
    },
    
    /**
     * @private
     * Filter function that check if a node in the tree should be visible or not.
     * @param {Ext.data.Model} record The record to check.
     * @return {boolean} True if the record should be visible.
     */
    _filterByTextAndChildren: function (record)
    {
        var isVisible = this._regexFilter == null || this._regexFilter.test(record.data.text);
        if (!isVisible)
        {
            // if the record does not match, we check if any child is visible. If at least one is, this record should not be hidden.
            // This is efficient because the data is in the store, and is not loaded in the view.
            for (var i = 0; !isVisible && i < record.childNodes.length; i++) {
                isVisible = this._filterByTextAndChildren(record.childNodes[i]);
            }
        }
        
        if (isVisible)
        {
            this._tree.expandNode(record);
        }
        
        return isVisible; 
    },
    
	/**
     * @private
     * Handler for the clear search
     */
	_clearSearchFilter: function ()
	{
		if (this._filterField)
		{
			this._filterField.reset();
		}
		
		this._regexFilter = null;
		this._tree.getStore().clearFilter();
		this._tree.getDockedItems()[1].setVisible(false);
	},
	
	/**
     * @private
     * Handler for the expand amm button
     */
	_expandAll: function ()
	{
		this._tree.expandAll();	
	},
	
	/**
	 * @private
	 * Handler for the collapse all button
	 */
	_collapseAll: function ()
	{
		this._tree.collapseAll();	
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
								selectable: nodeData.type == 'component', // if true the "select" action is enabled
								pluginName: nodeData.pluginName, 
								featureName: nodeData.featureName,
								componentName: nodeData.componentName,
								extensionPointName: nodeData.name,
								parentName: selectedNode.parentNode != null ? selectedNode.parentNode.data.name : "", // for the "select" action
								extensionId: nodeData.extensionId,
								componentId: nodeData.componentId
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
		this.setDirty(Ametys.plugins.admin.plugins.PluginsDAO.hasPendingChanges());
	}
});

