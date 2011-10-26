/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys.administration.System');

org.ametys.administration.System = function ()
{
}


org.ametys.administration.System.initialize = function (pluginName)
{
	org.ametys.administration.System.pluginName = pluginName;
}

org.ametys.administration.System.createPanel = function ()
{
	org.ametys.administration.System._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
		
		border: false,
		width: 277,
		
		cls: 'admin-right-panel',
	    
		items: [org.ametys.administration.System._drawGlobalActionsPanel (), 
		        org.ametys.administration.System._drawActionsPanel (),
		        org.ametys.administration.System._drawHelpPanel ()
		]
	});
	
	org.ametys.administration.System._listView = new org.ametys.ListView({
		region: 'center',
		
		id: 'list-view-announce',
		
		baseCls : 'list-view',
	    /*width : 500,
	    height: 400,*/
		autoScroll: true,	
		
		viewConfig: {
	        forceFit: true
	    },
	    
	    store : new Ext.data.SimpleStore({
				id:0,
		        fields: [
		           {name: 'lang'},
		           {name: 'message'}
		        ]
		}),
		
	    columns: [
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>", width : 80, menuDisabled : true, sortable: true, dataIndex: 'lang', defaultSortable : true},
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>", width : 400, menuDisabled : true, sortable: true, dataIndex: 'message'}
	    ],
		
	    listeners: {'rowclick': org.ametys.administration.System._selectAnnouncement}
	});		
	
	org.ametys.administration.System._listView.getStore().sort('lang', 'ASC');
	
	org.ametys.administration.System._fieldSet = new Ext.form.FieldSet({
		region:'center',
		layout: 'border',
		cls: 'system',
		
		title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_CHECK"/>",
		checkboxToggle: true,
		
		items : [ org.ametys.administration.System._listView ],
		
		listeners: { 'collapse': org.ametys.administration.System._onCollapse,
					 'expand': org.ametys.administration.System._onExpand }

	});
	
	return new Ext.Panel({
		region: 'center',
		layout: 'border',
		
		baseCls: 'transparent-panel',
		border: false,
		
		items: [org.ametys.administration.System._fieldSet, org.ametys.administration.System._contextualPanel]
	});
	
}

org.ametys.administration.System._onExpand = function(panel)
{
	org.ametys.administration.System._actions.show();	
	panel.doLayout();
}

org.ametys.administration.System._onCollapse = function()
{
	org.ametys.administration.System._actions.hide();
}


/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.System._drawGlobalActionsPanel = function ()
{
	org.ametys.administration.System._globalActions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE"/>"});
	
	// Save
	org.ametys.administration.System._globalActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE_SAVE"/>", 
				     getPluginResourcesUrl(org.ametys.administration.System.pluginName) + '/img/administrator/system/save.png',
				     org.ametys.administration.System.save);
	
	// Quit
	org.ametys.administration.System._globalActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE_QUIT"/>", 
				     getPluginResourcesUrl(org.ametys.administration.System.pluginName) + '/img/administrator/system/quit.png',
				     org.ametys.administration.System.goBack);
	
	return org.ametys.administration.System._globalActions;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.System._drawActionsPanel = function ()
{
	org.ametys.administration.System._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT"/>"});
	
	// Add
	org.ametys.administration.System._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_ADD"/>", 
								getPluginResourcesUrl(org.ametys.administration.System.pluginName) + '/img/administrator/system/add.png', 
								org.ametys.administration.System.add);
	
	// Edit
	org.ametys.administration.System._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_EDIT"/>", 
								getPluginResourcesUrl(org.ametys.administration.System.pluginName) + '/img/administrator/system/edit.png', 
								org.ametys.administration.System.edit);
	
	// Delete
	org.ametys.administration.System._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", 
								getPluginResourcesUrl(org.ametys.administration.System.pluginName) + '/img/administrator/system/delete.png', 
								org.ametys.administration.System.remove);
		
	org.ametys.administration.System._actions.hideElt(3);
	org.ametys.administration.System._actions.hideElt(4);
	
	return org.ametys.administration.System._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.System._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP_TEXT"/>");
	
	return helpPanel;
}





org.ametys.administration.System._selectAnnouncement = function ()
{
	var element = org.ametys.administration.System._listView.getSelection()[0];
	if (element == null)
	{
		org.ametys.administration.System._actions.hideElt(3);
		org.ametys.administration.System._actions.hideElt(4);		  
	}
	else
	{
		org.ametys.administration.System._actions.showElt(3);
		if (element.get('lang') == '*')
		{
			org.ametys.administration.System._actions.hideElt(4);		  
		}
		else
		{
			org.ametys.administration.System._actions.showElt(4);		      
		}     
	}
}

/**
 * Quit
 */
org.ametys.administration.System.goBack = function ()
{
    document.location.href = context.workspaceContext;
}

/**
 * Save
 */
org.ametys.administration.System.save = function ()
{
	var args = {};
	args.lang = [];
	
	// args.announcement = (document.getElementById('maintenance').checked ? "true" : "false");
	args.announcement = org.ametys.administration.System._fieldSet.checkbox.dom.checked ? "true" : "false";

	var elmts = org.ametys.administration.System._listView.getElements();
    for (var i = 0; i &lt; elmts.length; i++)
    {
        var element = elmts[i];
        var lang = element.get('lang');
        args.lang.push(element.get('lang'));
        args['message_' + element.get('lang')] = Utils.textareaToHTML(element.get('message'));
    }
    
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.System.pluginName, "/administrator/system/update", args, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ERROR_SAVE"/>", result, "org.ametys.administration.System.save"))
    {
       return;
    }

    org.ametys.administration.System.goBack ();
}

/**
 * Add a new announcement
 */
org.ametys.administration.System.add = function ()
{
	org.ametys.administration.System._mode = 'new';
	org.ametys.administration.System.act();
}

/**
 * Edit an announcement
 */
org.ametys.administration.System.edit = function ()
{
	org.ametys.administration.System._mode = 'edit';
	org.ametys.administration.System.act();
}

/**
 * Delete an announcement
 */
org.ametys.administration.System.remove = function ()
{
	var element = org.ametys.administration.System._listView.getSelection()[0];
	if (element.get('lang') == '*')
		return;
	
	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", 
			         "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE_CONFIRM"/>", 
			         org.ametys.administration.System.doRemove);
}
org.ametys.administration.System.doRemove = function (answer)
{
	if (answer == 'yes')
	{
		var element = org.ametys.administration.System._listView.getSelection()[0];
		org.ametys.administration.System._listView.removeElement(element);
	}
}

org.ametys.administration.System._mode;
org.ametys.administration.System._initialized;

org.ametys.administration.System.act = function ()
{
	if (!org.ametys.administration.System.delayedInitialize())
	{
		return;
	}
	
	org.ametys.administration.System.box.show();
	org.ametys.administration.System._initForm();
}

org.ametys.administration.System.delayedInitialize = function ()
{
	if (org.ametys.administration.System._initialized)
		return true;
	
	
	org.ametys.administration.System._form = new Ext.FormPanel({
		id : 'form-announcement',
		
		labelWidth: 70,
		width: 'auto',
		border: false,
		bodyStyle :'padding:10px 10px 0',
		
		items : [ new org.ametys.form.TextField ({
					fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>",
					desc: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_LANG_HELP"/>",
					name: 'lang',
					msgTarget: 'side',
					anchor:'90%'
				}),
				new org.ametys.form.TextAreaField ({
					fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>",
					desc: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_MESSAGE_HELP"/>",
					name: 'message',
					anchor:'90%',
					msgTarget: 'side',
			        height: 80
				})
		]
	});
	
	org.ametys.administration.System.box = new org.ametys.DialogBox({
		title :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CAPTION"/>",
		
		layout :'fit',
		width: 450,
		height: 205,
		
		icon: getPluginResourcesUrl(org.ametys.administration.System.pluginName) + '/img/administrator/system/announce_16.png',
		
		items : [ org.ametys.administration.System._form ],
		
		defaultButton: org.ametys.administration.System._form.getForm().findField('message'),
		closeAction: 'hide',
		
		buttons : [{
			text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_OK"/>",
			handler : org.ametys.administration.System.ok
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CANCEL"/>",
			handler : org.ametys.administration.System.cancel
		}]
	});
	
	org.ametys.administration.System._initialized = true;
	
	 return true;
}

org.ametys.administration.System._initForm = function ()
{
	if (org.ametys.administration.System._mode == 'new')
	{
	    var lang = org.ametys.administration.System._form.getForm().findField("lang");
	    var message = org.ametys.administration.System._form.getForm().findField("message");
	    
	    lang.setDisabled(false);
	    lang.setValue("");
	    message.setValue("");
	    try {
	    	lang.focus();
	    } catch(e) {}
	}
	else
	{
		var element = org.ametys.administration.System._listView.getSelection()[0];
		
		var lang = org.ametys.administration.System._form.getForm().findField("lang");
		var message = org.ametys.administration.System._form.getForm().findField("message");

		lang.setDisabled(element.get('lang') == "*");
		lang.setValue(element.get('lang'));
		message.setValue(Utils.htmlToTextarea(element.get('message')));
		try {
			message.focus();
			message.select();
		} catch(e) {}
	}
}

org.ametys.administration.System.ok = function ()
{
	var lang = org.ametys.administration.System._form.getForm().findField("lang");
    var message = org.ametys.administration.System._form.getForm().findField("message");

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

	if (org.ametys.administration.System._mode == 'new')
	{
		org.ametys.administration.System._listView.addElement(null, {
			lang : lang.getValue(),
            message : message.getValue().replace(/\r/g, "")
		});
	}
	else
	{
		var element = org.ametys.administration.System._listView.getSelection()[0];  
		element.set('lang', lang.getValue());        
		element.set('message', message.getValue().replace(/\r/g, ""));      
	}
    
	org.ametys.administration.System.box.hide();
}

org.ametys.administration.System.cancel = function ()
{
	org.ametys.administration.System.box.hide();
}
