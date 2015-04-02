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
 * The profiles tree panel.
 * Display the users and groups for each profiles in a tree panel.
 */
Ext.define('Ametys.runtime.profiles.ProfilesTreePanel', {
	extend: 'Ext.tree.Panel',
	
	 /**
	 * @cfg {String} context=/resources The initial context. Defaults to '/resources'
	 */
	/**
	 * @property {String} _context The rights context. See #cfg-context
	 * @private
	 */
	
	/**
	 * @cfg {String} rootID (required) The id of the root resource node.
	 */
	
	 /**
	 * @cfg {Function} upToContextFn (required) The function to call for up to context.
	 */
	/**
	 * @property {Function} _upToContextFn See #cfg-upToContextFn.
	 * @private
	 */
	
	/**
	 * @private
	 * @property {Object} _profileQTips The registered qtips for profiles
	 */
	/**
	 * @private
	 * @property {Ext.Template} _profileTooltipTpl The template used for profiles' tooltip
	 */
	/**
	 * @private
	 * @property {Ext.Template} _userTooltipTpl The template used for users' tooltip
	 */
	/**
	 * @private
	 * @property {Ext.Template} _groupTooltipTpl The template used for users' tooltip
	 */
	
	/**
	 * @property {Object} _loadParams={} Additional parameters to pass along with the request when loading the store
	 * @private
	 */
	
	/**
	 * @property {Array} _waitingLoadRequest An array representing the current possible waiting load request for the tree.
	 * The first element in the array is the user callback (Function) to execute for this load request, and the second element is the right context for the load (String).
	 * @private
	 */
	
	animate:true,
	
	border: true,
	autoScroll:false,

	rootVisible: false,
	
	constructor: function(config)
	{
		this._context = config.context || '';
		this._upToContextFn = config.upToContextFn || null;
		
		// The root node
		config.root = config.root || {
			id: config.rootID,
			text: ' <b>' + "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_ROOT_NODE'/>" + '</b>',
			loaded: true,
			expanded: true
		};
		
		// The store
		config.store = config.store || this._createStore();
		
		// Toolbar
		config.dockedItems = this._getDockedItems();
		
		this.callParent(arguments);
		
		// Tooltips 
		this._profileQTips = {};
		
		this._rightsTooltipTpl = Ext.create ('Ext.Template', [
  			'<b>{label}</b><br/>',
  			'{rights}<br/>'
  		]);
  		
  		this._userTooltipTpl = Ext.create ('Ext.Template', [
  				"<b>{text}</b><br/>",
  				"<u><i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_CONTEXT_TOOLTIP'/></u> : ",
  				"{context}"
  		]);
  		
  		this._groupTooltipTpl = Ext.create ('Ext.Template', [
  				"<b>{text}</b><br/>",
  				"<u><i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_CONTEXT_TOOLTIP'/></u> : ",
  				"{context}"
  		]);
		                                          		
		this.on ('beforeitemappend', this._setQtips, this);
		this.on ('selectionchange', this._onSelectionChange, this);
		
		// Additional parameters
		this._loadParams = {};
		
		this.addEvents (
	            /**
	             * @event profileupdated
	             * Fires when a profile has been updated
	             * @param {Ametys.runtime.profiles.ProfilesTreePanel} tree The tree
	             * @param {Ametys.runtime.profiles.ProfilesTreePanel.NodeEntry} node The the profile node
	             */
	            'profileupdated'
		);
		
	},
	
	/**
	 * Get the current rights' context
	 * @return {String} The context
	 */
	getContext: function ()
	{
		return this._context;
	},
	
	/**
	 * Creates the store the tree should use as its data source.
	 * @return {Ext.data.TreeStore} The tree store
	 * @protected
	 */
	_createStore: function ()
	{
		return Ext.create('Ext.data.TreeStore', {
			model: 'Ametys.runtime.profiles.ProfilesTreePanel.NodeEntry',
			
			proxy: {
				type: 'ametys',
				plugin: 'core',
				url: 'rights/profiles-context.xml',
				reader: {
					type: 'xml',
					root: 'nodes',
					record: '> node'
				}
			},
			
			sorters: {property: 'text', direction: 'ASC'},

			listeners: {
				beforeload: Ext.bind (this._onBeforeLoad, this),
				load: Ext.bind (this._onLoad, this)
			}
		});
	},
	
	/**
	 * Get the items to be added as docked items to this tree panel.
	 * This function returns one docked item that is docked to the top and that contains a toolbar with search input and tree tools.
	 * @return {Object/Object[]} the docked items
	 * @private
	 */
	_getDockedItems: function ()
	{
		return [{
			xtype: 'toolbar',
			cls: 'profile-toolbar',
			dock: 'top',
			items: [
				{
					// Search input field
					xtype: 'textfield',
					name: 'textfield',
					width: 250,
					emptyText: "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_FILTER_BY_NAME'/>",
					hideLabel: true,
					enableKeyEvents: true,
					listeners: {
						keyup: Ext.bind (this._onKeyUp, this)
					}
				},
				' ',
				'-', 
				{
					// See inheritance
					tooltip: "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_BTN_INHERITANCE_TOOLTIP'/>",
					enableToggle: true,
					toggleHandler: Ext.bind (this._seeInheritance, this),
					pressed: true,
					scope: this,
					icon: Ametys.getPluginResourcesPrefix('core') + '/img/profiles/actions/inheritance_16.gif',
					cls: 'x-btn-text-icon'
				}, 
				{
					// Up to parent context
					itemId: 'up-to-context',
					tooltip: "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_BTN_UPTOCONTEXT_TOOLTIP'/>",
					handler: Ext.bind (this._upToContext, this),
					disabled : true,
					scope: this,
					icon: Ametys.getPluginResourcesPrefix('core') + '/img/profiles/actions/up_16.png',
					cls: 'x-btn-text-icon'
				},
				'-', 
				{
					// Expand tool
					tooltip: "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_BTN_EXPAND_TOOTIP'/>",
					handler: Ext.bind (this._expandAll, this),
					scope: this,
					icon: Ametys.getPluginResourcesPrefix('core') + '/img/profiles/actions/expand-all.gif',
					cls: 'x-btn-text-icon'
				}, 
				{
					// Collapse tool
					tooltip: "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_BTN_COLLAPSE_TOOTIP'/>",
					handler: Ext.bind (this.collapseAll, this),
					scope: this,
					icon: Ametys.getPluginResourcesPrefix('core') + '/img/profiles/actions/collapse-all.gif',
					cls: 'x-btn-text-icon'
				},
				'-',
				{
					// Hint for context
					xtype: 'component',
					cls: 'hint',
					id: 'context-helper-text'
				}
			]
		}]
	},
	
	/**
	 * Expand the all tree
	 * @private
	 */
	_expandAll: function ()
	{
		this.getRootNode().expandChildren(true);
	},
	
	/**
	 * Refresh the profile node by its id or by passing the node directly.
	 * If the node is passed, then the profileId is ignored.
	 * @param {String} [profileId] The id of profile. Cannot be null if node is null.
	 * @param {Ext.data.Model} [node] The profile node. Cannot be null if profileId is null.
	 */
	refreshProfileNode: function (profileId, node)
	{
		node = node || this._findProfileNode(profileId);
		
		if (node)
		{
			this.getStore().load({
				node: node,
				callback: Ext.bind (this._refreshProfileNodeCb, this, [node], false)
			});
		}
	},
	
	/**
	 * Callback for #refreshProfileNode
	 * Expand the profile node and fire the #event-profileupdated
	 * @param {Ext.data.Model} node The profile node.
	 */
	_refreshProfileNodeCb: function(node)
	{
		this.expandNode(node);
		this.fireEvent ('profileupdated', this, node);
	},
	
	/**
	 * @private
	 * Retrieves a profile node by its profile id.
	 * {String} profileId the profile id of the node.
	 */
	_findProfileNode: function(profileId)
	{
		var root = this.getRootNode();
		var profileNode = null;
		
		// profile are child of the root node.
		root.eachChild(function(node) {
			if (node.get('profileId') === profileId)
			{
				profileNode = node;
			}
			
			// Stop iteration when the profile node has been found.
			return !profileNode;
		});
		
		return profileNode;
	},
	
	/**
	 * @private
	 * Listen to the *beforeload* of the tree store
	 * Inject extra parameters to the load request
	 * See {@link Ext.data.TreeStore#event-beforeload}
	 */
	_onBeforeLoad: function (store, operation, eOpts)
	{
		if (operation.node == null || operation.node.isRoot())
		{
			this._getProfileTooltipText ();
		}
		
		operation.params = operation.params || {};
		operation.params.profileContext = this._context || '';
		
		Ext.applyIf(operation.params, Ametys.getAppParameters());
		
		var node = operation.node;
		if (node != null && !node.isRoot())
		{
			operation.params.profile = node.get('profileId').substring('profile-'.length);
		}
		
		Ext.apply(operation.params, this._loadParams);
	},
	
	/**
	 * Listener called after the store is loaded. 
	 * Appends manually the user and group nodes to each profile.
	 * @param {Ext.data.TreeStore} store The tree store
	 * @param {Ext.data.NodeInterface} node The node that was loaded.
	 * @param {Ext.data.Model[]} records The loaded records
	 * @private
	 */
	_onLoad: function(store, node, records)
	{
		var me = this;
		var view = this.getView();
		
		if (node.isRoot())
		{
			this.getSelectionModel().select(0);
			
			// Expand each profile
			Ext.Array.forEach(node.childNodes, function(profileNode) {
				this.expandNode(profileNode);
			}, this);
		}
	},
	
	/**
	 * This function is called before appending a node
	 * Sets the tooltip text to show on this node.
	 * @param {Ext.data.NodeInterface} parentNode The parent node
	 * @param {Ext.data.NodeInterface} node The child node to be append
	 * @private
	 */
	_setQtips: function (parentNode, node)
	{
		var type = node.get('type');
		if (type == 'user')
		{
			node.data.qtip = this._userTooltipTpl.applyTemplate (node.data);
		}
		else if (type == 'group')
		{
			node.data.qtip = this._groupTooltipTpl.applyTemplate (node.data);
		}
		else if (type == 'profile')
		{
			node.data.qtip = this._profileQTips[node.get('profileId').substring('profile-'.length)];
		}
	},
	
	/**
	 * Update the right's context then reload the tree store.
	 * @param {String} context The context
	 * @param {Object} params the additional parameters to pass along with the request when loading the store
	 * @param {Function} callback The callback for the deferred reload tree method.
	 */
	updateContext: function(context, params, callback)
	{	
		this._context = context || '';
		this._loadParams = params || {};
		
		this.reloadTree(callback, context);
	},
	
	/**
	 * Reload the whole tree
	 * @param {Function} callback the callback function to run after the load.
	 * @param {String} context the requested context of rights.
	 */
	reloadTree: function (callback, context)
	{
		if (this._treeReloading)
		{	
			if (this._waitingLoadRequest)
			{
				// cancel the waiting request, calls the user callback.
				this._waitingLoadRequest[0](this._waitingLoadRequest[1], false, true);
			}
			
			this._waitingLoadRequest = [callback, context];
		}
		else
		{
			this._treeReloading = true;
			
			this.getStore().load({
				callback: Ext.bind(this._reloadTreeCb, this, [callback, context])
			});
		}
	},
	
	/**
	 * Callback for the #reloadTree function
	 * @param {Function} callback the user callback function to run after the load.
	 * @param {String} context the requested context of rights.
	 */
	_reloadTreeCb: function(callback, context)
	{
		// user callback
		this._treeReloading = false;
		
		callback(context, true, this._waitingLoadRequest != null);
		
		if (this._waitingLoadRequest)
		{
			var cb = this._waitingLoadRequest[0];
			var ctx = this._waitingLoadRequest[1];
			
			this._waitingLoadRequest = null;
			
			this.reloadTree(cb, ctx);
		}
	},
	
	/**
	 * Update the text of contained in the hint panel for context
	 * @param {String} text the new text
	 */
	updateContextHelperText: function(text)
	{
		var hintPanel = this.getDockedItems('toolbar[dock="top"]')[0].child('#context-helper-text');
		if (hintPanel)
		{
			hintPanel.update(text);
		}
	},
	
	/**
	 * Get the profiles' tooltip text
	 * @private
	 */
	_getProfileTooltipText : function ()
	{
		var response = Ametys.data.ServerComm.send({
			plugin: 'core', 
			url: 'rights/profile.xml',
			parameters: Ext.apply(Ametys.getAppParameters(), this._loadParams),
			priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
			
			callback: {
				handler: this._getProfileTooltipTextCb,
				scope: this
			}
		});
	},
	
	/**
	 * @private
	 * Callback function called after #_getProfileTooltipText is processed.
	 * @param {Object} response The XML response provided by the {@link Ametys.data.ServerComm}
	 * @param {Object} params The callback parameters passed to the {@link Ametys.data.ServerComm#send} method
	 */
	_getProfileTooltipTextCb : function (response, params)
	{
		if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_PROFILES_TREE_GET_PROFILES_RIGHTS_ERROR'/>", response, Ext.getClassName(this)))
		{
			return;
		}
		
		var profiles = Ext.dom.Query.select('profiles/profile', response);
		for (var i=0; i < profiles.length; i++)
		{
			var qtip = '';
			var id = profiles[i].getAttribute('id');
			var rightsCategory = Ext.dom.Query.select('> category', profiles[i]);
			
			for (var j=0; j < rightsCategory.length; j++)
			{
				var label = Ext.dom.Query.selectValue('> label', rightsCategory[j]);
				
				var rights = [];
				var rightNodes = Ext.dom.Query.select('> right', rightsCategory[j]);
				for (var k=0; k < rightNodes.length; k++)
				{
					rights.push(Ext.dom.Query.selectValue('label', rightNodes[k]));
				}
				
				qtip += this._rightsTooltipTpl.applyTemplate ({label: label, rights: rights.join(', ')});
			}
			
			this._profileQTips[id] = qtip;
		}
	},
	
	/**
	 * Remove users and groups node from the tree.
	 */
	removeUsersGroups: function()
	{
		var rootNode = this.getRootNode();
		if (rootNode)
		{
			rootNode.eachChild(function(profile) {
				profile.eachChild(function (node) {
					node.remove();
				})
			});
		}
	},
	
	/**
	 * @private
	 * Update the view by (un)filtering the inherited node depending on the button state.
	 * @param {Ext.button.Button} button The 'see inheritance' button.
	 * @param {Boolean} state The new state of the Button, true means pressed.
	 */
	_seeInheritance: function(button, state)
	{
		var fn = []; // filters function
		
		var txt = this.getDockedItems('toolbar[dock="top"]')[0].items.get(0).getValue();
		if (txt != '')
		{
			fn.push (Ext.bind(this._filterByText, this, [txt], true))
		}
		
		this._inheritanceEnabled = state;
		if (this._inheritanceEnabled)
		{
			fn.length == 0 ? this.clearFilter() : this.filterBy (fn);;
		}
		else
		{
			fn.push(this._filterByInherit);
			this.filterBy (fn);
		}
	},
	
	/**
	 * This function calls the configured #cfg-upToContextFn for the context of the selected node.
	 * @param {Ext.button.Button} button The button calling this function
	 * @private
	 */
	_upToContext: function(button)
	{
		var node = this.getSelectionModel().getSelection()[0];
		if (node != null)
		{
			if (typeof (this._upToContextFn) == 'function')
			{
				this._upToContextFn(node.get('context'));
			}
		}
	},
	
	/**
	 * @private
	 * Listen on 'keyup' event
	 * Filters the tree nodes by entered text.
	 * @param {Ext.form.field.Text} field This text field
	 */
	_onKeyUp: function(field)
	{
		var val = Ext.String.escapeRegex(field.getRawValue());
		
		var fn = [Ext.bind(this._filterByText, this, [val], true)];
		if (!this._inheritanceEnabled)
		{
			fn.push(this._filterByInherit);
		}
		this.filterBy (fn);
	},
	
	/**
	 * Returns true if the node text contains the given text
	 * @param {Ext.data.Model} node The node
	 * @param {String} text The text to test
	 * @private
	 */
	_filterByText: function (node, text)
	{
		var re = new RegExp('.*' + text + '.*', 'i');
		return node.get('type') == 'profile' || re.test(node.get('text'))
	},
	
	/**
	 * Returns true if the node text does not contains the inherit property
	 * @param {Ext.data.Model} node The node
	 * @private
	 */
	_filterByInherit: function (node)
	{
		return !node.get('inherit');
	},
	
	/**
	 * Clear all filters
	 */
	clearFilter: function ()
	{
		var view = this.getView(); 
		
		this.getRootNode().cascadeBy(function(tree, view) {
			 var uiNode = view.getNodeByRecord(this);
		  
			 if(uiNode) {
				 Ext.get(uiNode).setDisplayed('table-row');
			 }
		 }, null, [this, view]);
		 
	},
	
	/**
	 * Filters by a set of function. The specified function will be called for each Record in this Store. 
	 * If the function returns true the Record is included, otherwise it is filtered out.
	 * @param {Function/Function[]} filterFn A function or a set of functions to be called. It will be passed the following parameters:
	 * @param {Ext.data.Model} filterFn.node The node to test for filtering
	 */
	filterBy: function (filterFn)
	{
		if (!Ext.isArray(filterFn)) 
		{
			filterFn = [filterFn];
		}
		
		this.clearFilter();
		
		var view = this.getView(); 

		this.getRootNode().cascadeBy( function (node) {
			
			 var uiNode = this.getView().getNodeByRecord(node);
			 
			 var display = true;
			 for (var i=0; i < filterFn.length; i++)
			 {
				 display = display && filterFn[i](node);
			 }
			 
			 if (uiNode && !display)
			 {
				 Ext.get(uiNode).setDisplayed('none');
			 }
		}, this);
	},
	
	// --------------- Listeners ----------------------//
	
	/**
	 * Listener when selection has changed
	 * @param {Ext.selection.Model} sm The selection model
	 * @param {Ext.data.Model[]} selected The selected records
	 */
	_onSelectionChange: function (sm, selected)
	{
		var node = selected.length > 0 ? selected[0] : null;
		var upToContextBtn = this.getDockedItems('toolbar[dock="top"]')[0].down('#up-to-context');
		upToContextBtn.setDisabled(node == null || !node.get('inherit'));
	}
});
