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
 * @private
 * Singleton class defining the actions related to the super user
 */
Ext.define('Ametys.plugins.admin.superuser.SuperUserActions', {
	singleton: true,
	
	/**
	 * Grant all privileges to a user on an empty context or on the context defined by controller
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	act: function (controller)
	{
		this.affectSuperUser (controller.getInitialConfig('context') || "", "/application", "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_NO_POPULATION_DESCRIPTION}}");
	},
	
	/**
	 * Grant all privileges to a user on a given context
	 * @param rightContext The context on which give rights
	 * @param populationContext The context for the populations to select users
	 * @param noPopulationMessage The message to display when no user population is linked to the population's context
	 * @param [callback] The callback function to invoked after granting privileges
	 */
	affectSuperUser: function (rightContext, populationContext, noPopulationMessage, callback)
	{
		Ametys.helper.SelectUser.act({
            context: populationContext,
            noPopulationMessage: noPopulationMessage || "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_NO_POPULATION_DESCRIPTION}}",
			callback: Ext.bind(this._selectUserCb, this, [rightContext, callback], 1), 
			cancelCallback: null, 
			allowMultiselection: true, 
			plugin: null
		});	
	},
	
	/**
	 * @private
	 * A callback function invoked when a user has been selected
	 * @param {Object[]} users The array of users
     * @param {String} users.login The login of the user
     * @param {String} users.population The population id of the user
	 * @param {String} rightContext The context to affect rights
	 * @param [callback] The callback function to invoked after granting privileges
	 */
	_selectUserCb: function (users, rightContext, callback) 
	{
		Ametys.data.ServerComm.callMethod({
			role: 'org.ametys.core.ui.RibbonControlsManager',
			id: 'org.ametys.runtime.plugins.admin.superuser.Affect',
			methodName: 'affectSuperUser',
			parameters: [users, rightContext],
			callback: {
				handler: this._affectSuperUserCb,
				scope: this,
				arguments: {
					callback: callback
				}
			},
			errorMessage: { 
            	msg: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_ERROR}}", 
            	category: Ext.getClassName(this)
        	},
			waitMessage: true
		 });
	},
	
	/**
	 * @private
	 * Callback function invoked when the super user allocation process is complete
	 * @param {Object} response the server's response
	 * @param {Object} args the callback arguments
	 */
	_affectSuperUserCb: function(response, args)
	{
        var targets = [];
        Ext.Array.forEach(response.profileIds || [], function(id) {
            targets.push({
                id: Ametys.message.MessageTarget.PROFILE,
                parameters: {id: id}
            });
            
        }, this);
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.MODIFIED,
            targets: targets
        });
        
		Ametys.Msg.show ({
			title: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_TITLE}}",
			msg: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_SUCCESS}}",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO
		});
		
		if (Ext.isFunction(args.callback))
		{
			args.callback();
		}
	}
});
