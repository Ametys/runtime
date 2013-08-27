/*
 *  Copyright 2012 Anyware Services
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
 * Class to rule the system screen (the one with user announce).
 * See {@link #initialize} and {@link #createPanel}.
 */
Ext.define('Ametys.plugins.core.administration.System', {
	singleton: true,
	
	/**
	 * @property {String} pluginName The plugin declaring this class
	 * @private
	 */
	/**
	 * @private
	 * @property {Ext.Container} _contextualPanel The right panel
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _listView The list of announces
	 */
	/**
	 * @private
	 * @property {Ext.form.FieldSet} _fieldSet The main field set around the _listView
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _globalActions The global action panel
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _actions The announce action panel
	 */
	/**
	 * @private
	 * @property {String} _mode The current mode when opening the edition dialog box {@link #box}. Can be 'new' or 'edit'.
	 */
	/**
	 * @private
	 * @property {Boolean} _initialized Determine if the dialog box {@link #box} for editing announce is initialized
	 */
	/**
	 * @private
	 * @property {Ext.form.Panel} _form The form panel in the dialog box {@link #box} for editing
	 */
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} box The dialog box for editing
	 */
	
	/**
	 * Initialize
	 * @param {String} pluginName The plugin declaring this class
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
	},
	
	/**
	 * Creates the panel that rules the screen
	 * @returns {Ext.Panel} The screen
	 */
	createPanel: function ()
	{
		this._contextualPanel = new Ext.Container({
			region:'east',
			
			border: false,
			width: 277,
			
			cls: 'admin-right-panel',
		    
			items: [this._drawGlobalActionsPanel (), 
			        this._drawActionsPanel (),
			        this._drawHelpPanel ()
			]
		});
		
		Ext.define('Ametys.plugins.core.administrator.System.Announce', {
		    extend: 'Ext.data.Model',
		    fields: [
		       {name: 'lang'},
		       {name: 'message'}
		    ]
		});
		
		this._listView = new Ext.grid.Panel({
			region: 'center',
			
			id: 'list-view-announce',
			
			height: '100%',
			
		    store : Ext.create('Ext.data.Store', {
				model: 'Ametys.plugins.core.administrator.System.Announce',
		        data: { announces: []},
		        
		        sortOnLoad: true,
		        sorters: [ { property: 'lang', direction: "ASC" } ],
		        
		        proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'announces'
		        	}
		        }
			}), 
		    	
		    columns: [
		        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>", width : 80, flex: 0, menuDisabled : true, sortable: true, dataIndex: 'lang', defaultSortable : true},
		        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>", width : 400, flex: 1, menuDisabled : true, sortable: true, dataIndex: 'message'}
		    ],
			
		    listeners: {'select': Ext.bind(this._selectAnnouncement, this)}
		});		
		
		this._fieldSet = new Ext.form.FieldSet({
			region:'center',
			layout: 'border',
			cls: 'system',
			
			collapsed: true,
			height: 'auto',
			
			title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_CHECK"/>",
			checkboxToggle: true,
			
			items : [ this._listView ],
			
			listeners: { 'collapse': Ext.bind(this._onCollapse, this),
						 'expand': Ext.bind(this._onExpand, this),
						 'boxready': Ext.bind(this._onShow, this)}

		});
		
		return new Ext.Panel({
			region: 'center',
			layout: 'border',
			
			baseCls: 'transparent-panel',
			border: false,
			
			items: [this._fieldSet, this._contextualPanel]
		});
	},

	/**
	 * @private
	 * Listener on expanding the announce panel
	 */
	_onExpand: function()
	{
		this._actions.show();
		this._fieldSet.setHeight('auto');
	},
	
	/**
	 * @private
	 * Listener on showing the announce panel
	 */
	_onShow: function()
	{
		this._fieldSet.setHeight('auto');
		
		// OnExpand and OnCollapse is not fired anymore if the component is not rendered
		if (this._fieldSet.collapsed)
		{
			this._actions.hide();
		}
	},

	/**
	 * @private
	 * Listener on collapsing the announce panel
	 */
	_onCollapse: function()
	{
		this._actions.hide();
	},


	/**
	 * Draw the glabal action panel.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawGlobalActionsPanel: function ()
	{
		this._globalActions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE"/>"});
		
		// Save
		this._globalActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE_SAVE"/>",
						 null,
					     Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/system/save.png',
					     Ext.bind(this.save, this));
		
		// Quit
		this._globalActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE_QUIT"/>", 
						 null,
					     Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/system/quit.png',
					     Ext.bind(this.goBack, this));
		
		return this._globalActions;
	},

	/**
	 * Draw the actions panel.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawActionsPanel: function ()
	{
		this._actions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT"/>"});
		
		// Add
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_ADD"/>", 
			    null,
				Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/system/add.png', 
				Ext.bind(this.add, this));
		
		// Edit
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_EDIT"/>", 
			    null,
				Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/system/edit.png', 
				Ext.bind(this.edit, this));
		
		// Delete
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", 
			    null,
				Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/system/delete.png', 
				Ext.bind(this.remove, this));
			
		this._actions.hideElt(1);
		this._actions.hideElt(2);
		
		return this._actions;
	},

	/**
	 * Draw the help panel.
	 * @return {Ametys.workspace.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP"/>"});
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP_TEXT"/>");
		
		return helpPanel;
	},

	/**
	 * @private
	 * Listener when an announce is selected
	 */
	_selectAnnouncement: function ()
	{
		var element = this._listView.getSelectionModel().getSelection()[0];
		if (element == null)
		{
			this._actions.hideElt(1);
			this._actions.hideElt(2);		  
		}
		else
		{
			this._actions.showElt(1);
			if (element.get('lang') == '*')
			{
				this._actions.hideElt(2);		  
			}
			else
			{
				this._actions.showElt(2);		      
			}     
		}
	},

	/**
	 * Quit
	 */
	goBack: function ()
	{
	    document.location.href = Ametys.WORKSPACE_URI;
	},

	/**
	 * Save
	 */
	save: function ()
	{
		var args = {};
		args.lang = [];
		
		args.announcement = this._fieldSet.checkboxCmp.getValue() ? "true" : "false";

		var elmts = this._listView.getStore().data.items;
	    for (var i = 0; i < elmts.length; i++)
	    {
	        var element = elmts[i];
	        var lang = element.get('lang');
	        args.lang.push(element.get('lang'));
	        args['message_' + element.get('lang')] = Ametys.convertTextareaToHtml(element.get('message'));
	    }
	    
		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "/administrator/system/update", 
			parameters: args, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ERROR_SAVE"/>", result, "Ametys.plugin.core.administration.System.save"))
	    {
	       return;
	    }

	    this.goBack ();
	},

	/**
	 * Add a new announcement
	 */
	add: function ()
	{
		this._mode = 'new';
		this.act();
	},

	/**
	 * Edit an announcement
	 */
	edit: function ()
	{
		this._mode = 'edit';
		this.act();
	},

	/**
	 * Delete the selected announcement
	 */
	remove: function ()
	{
		var element = this._listView.getSelectionModel().getSelection()[0];
		if (element.get('lang') == '*')
		{
			return;
		}
		
		Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", 
				         "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE_CONFIRM"/>", 
				         Ext.bind(this.doRemove, this));
	},
	
	/**
	 * @private
	 * Do actually the removal
	 * @param {String} answer If answer is 'yes' the removal is done
	 */
	doRemove: function (answer)
	{
		if (answer == 'yes')
		{
			var element = this._listView.getSelectionModel().getSelection()[0];
			this._listView.getStore().remove(element);
		}
	},

	/**
	 * Open the dialog box to edit a selected announce or a new announce
	 */
	act: function ()
	{
		if (!this.delayedInitialize())
		{
			return;
		}
		
		this.box.show();
		this._initForm();
	},

	/**
	 * @private
	 * Initialize the dialog box
	 */
	delayedInitialize: function ()
	{
		if (this._initialized)
		{
			return true;
		}
		
		this._form = new Ext.form.Panel({
			id : 'form-announcement',
			
			width: 'auto',

			border: false,
			bodyStyle :'padding:10px 10px 0',
			
			items : [ new Ext.form.field.Text ({
						labelWidth: 70,
						fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>",
						
						ametysDescription: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_LANG_HELP"/>",
						name: 'lang',
						
						msgTarget: 'side',
						anchor:'90%'
					}),
					new Ext.form.field.TextArea ({
						labelWidth: 70,
						fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>",
						
						ametysDescription: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_MESSAGE_HELP"/>",
						name: 'message',
						anchor:'90%',
						
						msgTarget: 'side',
				        height: 80
					})
			]
		});
		
		this.box = new Ametys.window.DialogBox({
			title :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CAPTION"/>",
			
			layout :'fit',
			width: 450,
			height: 205,
			
			icon: Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/system/announce_16.png',
			
			items : [ this._form ],
			
			defaultButton: this._form.getForm().findField('message'),
			closeAction: 'hide',
			
			buttons : [{
				text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_OK"/>",
				handler : Ext.bind(this.ok, this)
			}, {
				text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CANCEL"/>",
				handler : Ext.bind(this.cancel, this)
			}]
		});
		
		this._initialized = true;
		
		return true;
	},

	/**
	 * Initialize the form value (depending on new or edit mode)
	 * @private
	 */
	_initForm: function ()
	{
		if (this._mode == 'new')
		{
		    var lang = this._form.getForm().findField("lang");
		    var message = this._form.getForm().findField("message");
		    
		    lang.setDisabled(false);
		    lang.setValue("");
		    message.setValue("");
		    try {
		    	lang.focus();
		    } catch(e) {}
		}
		else
		{
			var element = this._listView.getSelectionModel().getSelection()[0];
			
			var lang = this._form.getForm().findField("lang");
			var message = this._form.getForm().findField("message");

			lang.setDisabled(element.get('lang') == "*");
			lang.setValue(element.get('lang'));
			message.setValue(Ametys.convertHtmlToTextarea(element.get('message')));
			try {
				message.focus();
				message.select();
			} catch(e) {}
		}
	},

	/**
	 * Listener when clicking on the ok button
	 * @private 
	 */
	ok: function ()
	{
		var lang = this._form.getForm().findField("lang");
	    var message = this._form.getForm().findField("message");

	    if (lang.disabled != true)
	    {
	        if (!/[a-z][a-z]/i.test(lang.getValue()))
	        {
	        	lang.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_LANG"/>");
	            return;
	        }
	    }
	    
	    if (message.getValue() == '')
	    {
	    	message.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_MESSAGE"/>");
	        return;
	    }

		if (this._mode == 'new')
		{
			this._listView.getStore().addSorted(Ext.create('Ametys.plugins.core.administrator.System.Announce', {
            	lang : lang.getValue(),
                message : message.getValue().replace(/\r/g, "")
			}));
		}
		else
		{
			var element = this._listView.getSelectionModel().getSelection()[0];  
			element.set('lang', lang.getValue());        
			element.set('message', message.getValue().replace(/\r/g, ""));      
		}
	    
		this.box.hide();
	},

	/**
	 * @private
	 * Listener when closing the dialog box
	 */
	cancel: function ()
	{
		this.box.hide();
	}
});
