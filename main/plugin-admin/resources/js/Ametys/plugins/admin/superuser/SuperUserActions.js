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
	 * Give all the rights to a user on the context
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	affectSuperUser: function (controller)
	{
		Ametys.helper.SelectUser.act({
            context: this._getAppContext(controller),
            noPopulationMessage: this._getNoPopulationMessage(),
			callback: Ext.bind(this._affectSuperUser, this, [controller], 1), 
			cancelCallback: null, 
			allowMultiselection: true, 
			plugin: null
		});	
	},
	
	/**
	 * @private
	 * A callback method when a user has been selected
	 * @param {Object[]} users The array of users
     * @param {String} users.login The login of the user
     * @param {String} users.population The population id of the user
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller the button's controller
	 */
	_affectSuperUser: function (users, controller) 
	{
		// Transmit the controller to get the context
		var context = controller.getInitialConfig('context') || this._getRightContext(controller);
		
		controller.serverCall
		(
			'affectSuperUser',
			[users, context],
			Ext.bind(this._affectSuperUserCb, this),
			{ 
                errorMessage: { 
                	msg: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_ERROR}}", 
                	category: Ext.getClassName(this)
            	},
                refreshing: true
            } 
		);
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
	},
    
    /**
     * @protected
     * Get the context for the populations to display in the SelectUser dialog box 
     * @param {Ametys.ribbon.element.ui.ButtonController} controller the button's controller
     * @return {String} the application context
     */
    _getAppContext: function(controller)
    {
        return "/application";
    },
	
	/**
	 * @protected
	 * Get the context the user will be granted all rights to
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller the button's controller
	 * @return {String} the right context
	 */
	_getRightContext: function(controller)
	{
		return "";
	},
    
    /**
     * @protected
     * Get the message to display when no user population is linked to the context
     * @param {Ametys.ribbon.element.ui.ButtonController} controller the button's controller
     * @return {String} the message to display when no user population is linked to the context
     */
    _getNoPopulationMessage: function(controller)
    {
        return "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_NO_POPULATION_DESCRIPTION}}";
    }
});
