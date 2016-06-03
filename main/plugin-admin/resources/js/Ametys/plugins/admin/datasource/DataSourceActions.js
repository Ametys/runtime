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
 * This singleton gathers the actions invoked when clicking the add/modify/remove data source buttons
 * @private
 */
Ext.define('Ametys.plugins.admin.datasource.DataSourceActions', {
	
	singleton: true,
	
	/**
	 * Add a data source
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller the controller calling this function
	 */
	add: function(controller)
	{
		switch (controller.getInitialConfig('type'))
		{
			case 'SQL':
				Ametys.plugins.admin.datasource.EditSQLDataSourceHelper.add();
				break;
			case 'LDAP': 
				Ametys.plugins.admin.datasource.EditLDAPDataSourceHelper.add();
				break;
			default:
				throw 'Unrecognized data source type ' + controller.getInitialConfig('type');
		}
	},
	
	/**
	 * Edit the selected data source
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller the controller calling this function
	 */
	edit: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var displayPopUp = target.getParameters().isInUse;
			switch (target.getParameters().type)
			{
				case 'SQL':
					Ametys.plugins.admin.datasource.EditSQLDataSourceHelper.edit(target.getParameters().id, Ext.bind(this._editCb, this, [displayPopUp, false], 1));
					break;
				case 'LDAP': 
					Ametys.plugins.admin.datasource.EditLDAPDataSourceHelper.edit(target.getParameters().id, Ext.bind(this._editCb, this, [displayPopUp, false], 1));
					break;
				default:
					throw 'Unrecognized data source type ' + target.getParameters().type;
			}
		}
	},
	
	/**
	 * Remove the selected data source(s)
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller the controller calling this function
	 */
	remove: function(controller)
	{
		var targets = controller.getMatchingTargets();
		if (targets.length > 0)
		{
			Ametys.MessageBox.confirm (
				"{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_REMOVE_LABEL}}",
				"{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_REMOVE_CONFIRM}}",
				Ext.bind(this._removeConfirm, this, [targets, controller._allRightDataSourceIds], 1),
				this
			);
		}
	},
	
	/**
	 * Set the selected data source as the default data source
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller the controller calling this function
	 */
	setDefault: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var type = target.getParameters().type;

			var tool = Ametys.tool.ToolsManager.getTool('uitool-datasource');
			var isDefaultDataSourceInUse = false;
			if (tool != null)
			{
				isDefaultDataSourceInUse = tool.getDefaultDataSource(type).get('isInUse');
			}
			
			if (type != 'SQL' && type != 'LDAP')
			{
				throw 'Unrecognized data source type ' + target.getParameters().type;
			}
			else
			{
				var displayPopUp = isDefaultDataSourceInUse;
				Ametys.plugins.core.datasource.DataSourceDAO.setDefaultDataSource([type, target.getParameters().id], Ext.bind(this._editCb, this, [displayPopUp, true], 1));
			}
		}
	},
	
	/**
     * @private
	 * Callback function invoked when the 'Yes' or 'No' button of the dialog box was clicked
	 * @param {String} answer the user's answer
	 * @param {Ametys.message.MessageTarget[]} targets the data source message targets.
	 * @param {String[]} the ids of the data sources that can be removed
	 */
	_removeConfirm: function(answer, targets, allRightSourceIds)
	{
		if (answer == 'yes')
		{
            var datasourceByType = {};
            
			var ids = [];
			Ext.Array.each(targets, function(target) {
                
                if (Ext.Array.contains(allRightSourceIds, target.getParameters().id))
                {
                    var type = target.getParameters().type;
	                if (!datasourceByType[type])
	                {
	                    datasourceByType[type] = [];
	                }
	                
	                datasourceByType[type].push(target.getParameters().id);
                }
			});
			
            for (var type in datasourceByType)
            {
                Ametys.plugins.core.datasource.DataSourceDAO.removeDataSource([type, datasourceByType[type]], null, {});
            }
		}
	},
	
	/**
	 * @private
	 * Callback function invoked after a data source has been edited. 
	 * It offers to restart the application if the changes made might have led to an unstable state
	 * @param {Object} datasource the data source as an object
	 * @param {Boolean} displayPopUp true to display the warning pop-up, false otherwise
	 * @param {Boolean} defaultChanged true if the default data source was changed
	 */
	_editCb: function(datasource, displayPopUp, defaultChanged)
	{
		if (!displayPopUp)
		{
			return;
		}

		if (defaultChanged)
		{
			Ametys.Msg.show({
				title: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_DEFAULT_MODIFICATION_WARNING_DIALOG_TITLE}}",
				message: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_MODIFICATION_WARNING_DIALOG_MSG_TRANSFER_DATA}}",
				icon: Ext.Msg.WARNING,
				buttons: Ext.Msg.OK,
				scope: this,
				fn: this._showModifiedDataSourceDialog
			}, this);
		}
		else
		{
			this._showModifiedDataSourceDialog();
		}
	},
	
	/**
	 * Show the dialog box when a data source has been modified 
	 */
	_showModifiedDataSourceDialog: function()
	{
		Ametys.Msg.show({
			title: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_MODIFICATION_WARNING_DIALOG_TITLE}}",
			message: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_MODIFICATION_WARNING_DIALOG_MSG}}",
			icon: Ext.Msg.WARNING,
			buttons: Ext.Msg.YESNO,
			scope: this,
			fn : function(btn) {
				if (btn == 'yes') {
					Ext.getBody().mask("{{i18n plugin.core-ui:PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}");
					
					// Restart
					Ext.Ajax.request({url: Ametys.getPluginDirectPrefix('admin') + "/restart", params: "", async: false});
					
					Ametys.reload();
				}
			}
		});
	}
});