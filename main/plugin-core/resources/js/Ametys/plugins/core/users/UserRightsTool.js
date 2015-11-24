/*
 *  Copyright 2014 Anyware Services
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
 * This tool displays the rights of a user
 * @private
 */
Ext.define('Ametys.plugins.core.users.UserRightsTool', {
	extend: 'Ametys.tool.SelectionTool',
	
	/**
	 * @private
	 * @property {Ext.Template} _toolTitleTpl The template used for tool's title
	 */
	_toolTitleTpl: new Ext.Template(
			"<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_TITLE'/>",
			" {fullname}"
	),
	
	/**
	 * @private
	 * @property {Ext.Template} _toolDescriptionTpl The template used for tool's description
	 */
	_toolDescriptionTpl: new Ext.Template(
			"<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_TOOLTIPTEXT'/>",
			" {fullname} ({login})"
	),
	
	/**
	 * @private
	 * @property {Ext.Template} _contextByProfileHintTpl The template used for hint description
	 */
	_contextByProfileHintTpl: new Ext.Template("<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_CONTEXT_BY_PROFILE_HINT_1'/>",
			" {fullname} ({login})",
			"<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_CONTEXT_BY_PROFILE_HINT_2'/>"
	),
	
	/**
	 * @private
	 * @property {Ext.Template} _rightsByContextHintTpl The template used for hint description
	 */
	_rightsByContextHintTpl: new Ext.Template("<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_RIGHTS_BY_CONTEXT_HINT_1'/>",
			" {fullname} ({login})",
			"<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_RIGHTS_BY_CONTEXT_HINT_2'/>"
	),
	
	/**
	 * @private
	 * @property {Ext.Template} _profileTooltipTpl The template for a profile with its rights
	 */
	_profileTooltipTpl: new Ext.Template ("<b>{label}</b><br/>{rights}<br/>"),

	constructor: function(config)
	{
		this.callParent(arguments);
		
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onUserDeleted, this);
	},
	
	setParams: function(params)
	{
    	// Register the tool on the history tool
		var role = this.getFactory().getRole();
	    var toolParams = this.getParams();

        Ametys.navhistory.HistoryDAO.addEntry({
			id: this.getId(),
			label: this.getTitle(),
			description: this.getDescription(),
			iconSmall: this.getSmallIcon(),
			iconMedium: this.getMediumIcon(),
			iconLarge: this.getLargeIcon(),
			type: Ametys.navhistory.HistoryDAO.TOOL_TYPE,
			action: Ext.bind(Ametys.tool.ToolsManager.openTool, Ametys.tool.ToolsManager, [role, toolParams], false)
        });
        
        this.callParent(arguments); 
	},
	
	createPanel: function ()
	{
		return Ext.create('Ext.panel.Panel', {
			scrollable: false, 
			border: false,
			layout: 'card',
			activeItem: 0,
			
			items: [{
				xtype: 'component',
				cls: 'a-panel-text-empty',
				border: false,
				html: ''
			}, {
				xtype: 'panel',
				scrollable: true,
				border: false,
				dockedItems: {
					dock: 'top',
					ui: 'tool-hintmessage',
					xtype: 'component',
					html: ''
				}
			}]
		});
	},
	
	refresh: function (manual)
	{
		this.showRefreshing();
		
		var userTarget = this.getCurrentSelectionTargets()[0];
		this._login = userTarget.getParameters().id;
		
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.right.UsersClientInteraction",
			methodName: "getUserRights",
			parameters: [this._login],
			callback: {
 				scope: this,
				handler: this._updateUserRights,
				arguments: {login: this._login}
			},
			waitMessage: true,
			errorMessage: {
				msg: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USER_RIGHTS_ERROR'/>",
				category: Ext.getClassName(this)
			}
		});
	},
	
	/**
	 * @private
	 * Callback function called after #refresh to update the user'rights panel
	 * @param {Object[]} response The server's response structured as following:
	 * @param {Object[]} response.user the user object
	 * @param {String} response.user.login the user's login
	 * @param {String} response.user.fullname the user's fullname
	 * @param {String} response.user.sortablename the user's sortablename, to be displayed in lists
	 * @param {Object[]} response.profiles the user's profiles. Can be null.
	 * @param {Object[]} response.profiles.profile a profile of the user
	 * @param {String} response.profiles.profile.id the id of the user's profile
	 * @param {String} response.profiles.profile.label the label of the user's profile
	 * @param {Object[]} response.profiles.profile.context a context of one of the user's profile
	 * @param {String} response.profiles.profile.context.path the path of the corresponding context
	 * @param {Object[]} response.rights the user's rights. Can be null.
	 * @param {Object[]} response.rights.right a right of the user
	 * @param {String} response.rights.right.id the id of the right
	 * @param {String} response.rights.right.label the label of the right
	 * @param {String} response.rights.right.description the description of the right
	 * @param {String} response.rights.right.category the category of the right
	 * @param {String} response.rights.right.category.id the id of the category
	 * @param {Object[]} params The callback arguments.
	 * @param {String} params.login the login of the user
	 */
	_updateUserRights: function (response, params)
	{
		var login = params.login;
		
		if (this.getCurrentSelectionTargets() == null || this.getCurrentSelectionTargets()[0].getParameters().id != login)
		{
			// too late => discard (another user has been selected)
			return;
		}
		
		var user = response.user;
	    if (user.profiles)
	    {
	    	this._drawContextByProfiles (user, user.profiles);
	    }
	    else if (user.rights)
	    {
	    	this._drawRightsByContext (user, user.rights);
	    }
	    
	    this.showRefreshed();
	},
	
	/**
	 * @private
	 * Draw the user's profiles and their contexts
	 * @param {Object} user The user
	 * @param {Object[]} profiles The profiles
	 */
	_drawContextByProfiles: function (user, profiles)
	{
		this.getContentPanel().items.get(1).removeAll();
		this.getContentPanel().items.get(0).hide();
		
		var me = this;
		Ext.Array.each (profiles, function (profile) {
		
			var items = [];
			var contexts = profile.contexts;
			Ext.Array.each (contexts, function (context) {
				items.push({
					xtype: 'component',
					html: context
				})
			});
			
			var profilePanel = Ext.create ('Ext.Panel', {
				id: me._id + '-profile-' + profile.id,
				title: '<img src="' + Ametys.getPluginResourcesPrefix('core') + '/img/profiles/profile_16.png' + '" style="float:left; margin-right:5px;"' + '"/>' + profile.label,
				border: false,
				ui: 'light',
				cls: 'a-panel-text',
				header: {
					titlePosition: 1
				},
				collapsible: true,
				titleCollapse: true,
				
				items: items,
				
				bodyStyle: {
					paddingLeft: '20px'
				},
				
				listeners: {
					'afterrender': Ext.bind (me._setProfileTooltip, me, [profile.id])
				}
			});
			
			me.getContentPanel().items.get(1).add(profilePanel);
		});
		
		var login = user.login;
		var fullname = user.fullname;
		
		this.getContentPanel().items.get(1).down("*[dock='top']").update(this._contextByProfileHintTpl.applyTemplate({'fullname': fullname, 'login': login}));
		this.getContentPanel().getLayout().setActiveItem(1);
		
		this._updateInfos (this._toolTitleTpl.applyTemplate({'fullname': fullname}), this._toolDescriptionTpl.applyTemplate({'fullname': fullname, 'login': login}));
	},

	/**
	 * @private
	 * Draw the user's rights classified by context
	 * @param {Object} user The user
	 * @param {Object[]} rightsbycontext The rights by context
	 */
	_drawRightsByContext:  function (user, rightsbycontext)
	{
		this.getContentPanel().items.get(1).removeAll();
		this.getContentPanel().items.get(0).hide();
		
		var me = this;
		Ext.Array.each (rightsbycontext, function (rights) {
			
			var context = rights.context;
			
			var items = [];
			Ext.Array.each (rights.rights, function (right) {
				items.push({
					xtype: 'component',
					html: right.label
				});
			});
			
			var contextPanel = Ext.create ('Ext.Panel', {
				title: context,
				ui: 'light',
				cls: 'a-panel-text',
				header: {
					titlePosition: 1
				},
				border: false,
				collapsible: true,
				titleCollapse: true,
				
				items: items
			});
			
			me.getContentPanel().items.get(1).add(contextPanel);
		});
		
		var login = user.login;
		var fullname = user.fullname;
		
		this.getContentPanel().items.get(1).down("*[dock='top']").update(this._rightsByContextHintTpl.applyTemplate({'fullname': fullname, 'login': login}));
		this.getContentPanel().getLayout().setActiveItem(1);
		
		this._updateInfos (this._toolTitleTpl.applyTemplate({'fullname': fullname}), this._toolDescriptionTpl.applyTemplate({'fullname': fullname, 'login': login}));
	},
	
	/**
	 * Update tool information
	 * @private
	 */
	_updateInfos: function (title, description)
	{
		this.setTitle(title);
		this.setDescription(description);
		
		this.showRefreshed();
	},
	
	/**
	 * @private
	 * Set the tooltip for profile
	 * @param {Number} profileId The profile id
	 */
	_setProfileTooltip: function (profileId)
	{
		Ametys.data.ServerComm.callMethod({
			role: 'org.ametys.plugins.core.right.ProfilesClientInteraction',
			methodName: 'getProfileRights',
			parameters: [profileId],
			callback: {
 				scope: this,
				handler: this._setProfileTooltipCb,
				arguments: {profileId: profileId}
 			}
 		});
	},
	
	/**
	 * @private
	 * Callback function called after #_setProfileTooltip to set the tooltip on profile
	 * @param {Object[]} result The JSON result
	 * @param {Object} params The callback arguments.
	 */
	_setProfileTooltipCb: function (result, params)
	{
		var html = '';
		
		var categories = result.categories;
		
		var me = this;
		Ext.Array.each (categories, function (category) {
			
			var label = category.label;
			
			var rights = [];
			Ext.Array.each (category.rights, function (right) {
				rights.push (right.label);
			});
			
			html += me._profileTooltipTpl.apply ({label: label, rights: rights.join(', ')});
		});
		
		var profileId = params.profileId;
		
		var tooltip = Ext.getCmp(this._id + '-profile-' + profileId + '-tooltip');
		if (tooltip)
			tooltip.destroy();
		
		Ext.create('Ext.ToolTip', {
			id: this._id + '-profile-' + profileId + '-tooltip',
			target: this._id + '-profile-' + profileId,
			html: html
		}); 
	},
	
	setNoSelectionMatchState: function (message)
	{
		this.callParent(arguments);
		
		var panel = this.getContentPanel().items.get(0);
		panel.update(message);
		this.getContentPanel().getLayout().setActiveItem(0);
		
		this._updateInfos(this.getInitialConfig('title'), this.getInitialConfig('description'));
		
		this._login = null;
	},
	
	/**
	 * Listener on {@link Ametys.message.Message#DELETED} message. If the current user is concerned, the tool will be set in no selection mode.
	 * @param {Ametys.message.Message}  message The deleted message.
	 * @private
	 */
	_onUserDeleted: function (message)
	{
		if (this.getTargetsInCurrentSelectionTargets(message).length > 0)
		{
			this.setNoSelectionMatchState();
		}
	}
});
