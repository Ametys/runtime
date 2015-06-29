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
 * This tool contains a form panel that allows to edit the configuration parameters of the application.
 * The application is rebooted upon saving the configuration values. Those values can be tested before saving
 * thanks to the parameter checker system.
 */
Ext.define('Ametys.plugins.admin.config.ConfigTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ametys.Form.ConfigurableFormPanel} _formPanel The configuration form panel 
	 */
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function ()
	{
		this._formPanel = Ext.create('Ametys.form.ConfigurableFormPanel', {
			cls: 'config',
			
			listeners: {
				'inputfocus': Ext.bind(this.sendCurrentSelection, this),
				'toolopen': Ext.bind(this.sendCurrentSelection, this)
			}
		});
		
		this._getConfigurationParameters();
		
		return this._formPanel;
	},
	
	sendCurrentSelection: function()
	{
        Ext.create("Ametys.message.Message", {
            type: Ametys.message.Message.SELECTION_CHANGED,
            parameters: {},
            targets: {
                type: Ametys.message.MessageTarget.CONFIGURATION,
                parameters: {},
                subtargets: [ this._formPanel.getMessageTargetConf() ]
            }
        });
	},
	
	/**
	 * Get the configuration parameters in order to initialize the form panel with a server call
	 */
	_getConfigurationParameters: function()
	{
        Ametys.data.ServerComm.send({
        	plugin: 'admin', 
        	url: "config/get", 
        	parameters: {}, 
        	priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
        	callback: {
                handler: this._getConfigurationParametersCb,
                scope: this
            },
            errorMessage: {msg: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_LOADING_ERROR'/>", category: this.self.getName()},
            waitMessage: true
        });
	},
	
	/**
	 * Callback for the retrieving of configuration parameters process
	 * @param {Object} response the server's response
	 * @param {Object} response.configuration the configuration for the form panel
	 * @param {Object} response.values the values of the configuration parameters
	 */
	_getConfigurationParametersCb: function(response)
	{
		// Initialize the form panel
		this._formPanel.configure(Ext.dom.Query.select("config/configuration", response)[0]);
		this._formPanel.setValues(Ext.dom.Query.select("config/values", response)[0]);
	}
});

Ext.define("Ametys.message.ConfigMessageTarget",
	{
		override: "Ametys.message.MessageTarget",
		statics: 
		{
			/**
			 * @member Ametys.message.MessageTarget
			 * @readonly
			 * @property {String} CONFIGURATION The target of the message is the configuration 
			 */
			CONFIGURATION: "configuration"
		}
	}
);
