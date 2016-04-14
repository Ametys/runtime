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
 * Singleton class defining the actions related to the plugins and workpaces tools
 * @private
 */
Ext.define('Ametys.plugins.admin.plugins.PluginsActions', {
	singleton: true,
	
	/**
	 * View the documentation of the selected node
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	showDocumentation: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var zoom = "";
			var targetParameters = target.getParameters();
			var type = targetParameters.type;
			
			switch (type)
			{
				case 'plugin': 
					zoom = targetParameters.pluginName + "_main.html";
					break;
			
				case 'feature': 
					zoom = targetParameters.pluginName + "_features.html%23feature_" + targetParameters.featureName;
					break;
					
				case 'component': 
					zoom = targetParameters.pluginName + "_features.html%23feature_" + targetParameters.featureName + "_component_" + targetParameters.componentName;
					break;
						
				case 'component-role': 
					zoom = targetParameters.pluginName + "_features.html%23feature_" + targetParameters.featureName + "_component_" + targetParameters.componentName;
					break;
							
				case 'extension-point': 
					zoom = targetParameters.pluginName + "_extensions.html%23extension_point_" + targetParameters.extensionPointName;
					break;
				
				case 'extension': 
					zoom = targetParameters.pluginName + "_features.html%23feature_" + targetParameters.featureName + "_extension_" + targetParameters.extensionId;
					break;
				
				default: 
					break;
			}
			
			window.open(Ametys.getPluginDirectPrefix(controller._pluginName) + "/plugins/doc/index.html?zoom=" + zoom, "plugindoc", "");
		}
	},
	
	/**
	 * Activate the feature corresponding to the selected node
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	activate: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var targetParameters = target.getParameters();
			
			Ametys.plugins.admin.plugins.PluginsDAO.activateExtensionPoint (targetParameters.pluginName + "/" + targetParameters.featureName);
			
			Ext.MessageBox.alert("{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_ALERT_TITLE}}", "{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_ALERT_TEXT}}");
			this._sendModifyingMessage(targetParameters.pluginName);
		}
	},
	
	/**
	 * Deactivate the feature corresponding to the selected node
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	deactivate: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var targetParameters = target.getParameters();
			Ametys.plugins.admin.plugins.PluginsDAO.deactivateExtensionPoint (targetParameters.pluginName + "/" + targetParameters.featureName);
			
			Ext.MessageBox.alert("{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_ALERT_TITLE}}", "{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_ALERT_TEXT}}");
			this._sendModifyingMessage(targetParameters.pluginName);
		}
	},
	
	/**
	 * Select the extension point
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	select: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var targetParameters = target.getParameters();
			
			Ametys.plugins.admin.plugins.PluginsDAO.selectComponent(targetParameters.parentName, targetParameters.componentId);
			
			Ext.MessageBox.alert("{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_ALERT_TITLE}}", "{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_ALERT_TEXT}}");
			this._sendModifyingMessage(targetParameters.pluginName);
		}
	},
	
	/**
	 * Send a {Ametys.message.Message.MODIFIED} message
	 * @param {String} pluginName the name of the targeted plugin
	 */
	_sendModifyingMessage: function(pluginName)
	{
		var targets = [];
		var target = Ext.create('Ametys.message.MessageTarget', {
				id: Ametys.message.MessageTarget.AMETYS_PLUGIN,
				parameters: {name: pluginName}
			});
		
		targets.push(target);
		
		if (targets.length > 0)
		{			
			Ext.create('Ametys.message.Message', {
				type: Ametys.message.Message.MODIFYING,
				targets: targets
			});
		}
	},
	
	/**
	 * Cancel all changes
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	cancelChanges: function (controller)
	{
		Ametys.plugins.admin.plugins.PluginsDAO.reset();
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.MODIFYING,
			targets: [{
				id: Ametys.message.MessageTarget.AMETYS_PLUGIN,
				parameters: {}
			}]
		});
	},
	
	/**
	 * Apply the changes made on the tree
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	saveChanges: function(controller)
	{
		Ext.MessageBox.alert("{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_BEWARE_TITLE}}", 
				"{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_BEWARE_TEXT}}",
				Ext.bind(function ()
				{
					var cmpchanges = "";
					
					var cmp = Ametys.plugins.admin.plugins.PluginsDAO.getModifiedComponents();
					for (var i in cmp)
					{
						cmpchanges += i + " : " + cmp[i] + "<br/>";
					}
					var epchanges = "";
					var ep = Ametys.plugins.admin.plugins.PluginsDAO.getModifiedExtensionPoints();
					for (var i in ep)
					{
						epchanges += i + " : " + (ep[i] ? "{{i18n PLUGINS_ADMIN_PLUGINS_ACTIVATE_LABEL}}" : "{{i18n PLUGINS_ADMIN_PLUGINS_DEACTIVATE_LABEL}}") + "<br/>";
					}
					
					var changes = "";
					if (cmpchanges != "")
					{
						changes += "<b>{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_CMP}}</b><br/>"
								   + cmpchanges;
					}
					if (epchanges != "")
					{
						changes += "<b>{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_EP}}</b><br/>"
								   + epchanges;
					}
					
					Ext.MessageBox.confirm("{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_CONFIRM_TITLE}}", 
							"{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_CONFIRM_TEXT_START}}<ul>"
							+ changes
							+ "</ul>{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_CONFIRM_TEXT_END}}", 
							function(val) { if (val == "yes") { this._saveChangesNow(controller._pluginName); } }, this);
				}, this));
	},
	
	/**
	 * Operates the pending changes
	 * @private
	 */
	_saveChangesNow: function(pluginName)
	{
		var params = {};
		params.CMP = Ametys.plugins.admin.plugins.PluginsDAO.getModifiedComponents();
		params.EP = Ametys.plugins.admin.plugins.PluginsDAO.getModifiedExtensionPoints();
		
		Ametys.data.ServerComm.send({
			plugin: pluginName, 
			url: "plugins/change", 
			parameters: params, 
			priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
			callback: {
				handler: this._saveChangesNowCB,
				scope: this,
				arguments: {pluginName: pluginName},
			},
			errorMessage: {
				msg: "{{i18n PLUGINS_ADMIN_PLUGINS_SAVE_CHANGES_ERROR}}",
				category: this.self.getName()
			},
			responseType: null
		});
		
		Ext.getBody().mask("{{i18n plugin.core-ui:PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}");
	},

	/**
	 * @private
	 * Call back for the server message of the {@link #_saveChangesNow} method
	 * @param {Object} response The xmlhttpresponse
	 * @param {Object} args the callback arguments
	 * @param {String} args.pluginName the name of the plugin
	 */
	_saveChangesNowCB: function(response, args)
	{
		Ext.getBody().unmask();

	    Ametys.Msg.alert("{{i18n PLUGINS_ADMIN_PLUGINS_SAVE_CHANGES_LABEL}}", "{{i18n PLUGINS_ADMIN_PLUGINS_CHANGES_DONE}}");

	    Ext.getBody().mask("{{i18n plugin.core-ui:PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}");
	    
	    // Restart
	    Ext.Ajax.request({url: Ametys.getPluginDirectPrefix(args.pluginName) + "/restart", params: "", async: false});
	    
	    Ametys.reload();
	}
});


Ext.define("Ametys.message.PluginsMessageTarget",
	{
		override: "Ametys.message.MessageTarget",
		statics: 
		{
			/**
			 * @member Ametys.message.MessageTarget
			 * @readonly
			 * @property {String} AMETYS_PLUGIN The target of the message is a server-side plugin (for instance "admin"). The only parameter is: 'name' the name of the plugin 
			 */
			AMETYS_PLUGIN: "ametys-plugin"
		}
	}
);
