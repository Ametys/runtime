/*
 *  Copyright 2016 Anyware Services
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
	 * @property {Ametys.form.ConfigurableFormPanel} _formPanel The configuration form panel 
	 */
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function ()
	{
		this._formPanel = Ext.create('Ametys.form.ConfigurableFormPanel', {
            defaultPathSeparator: '/',
			cls: 'uitool-admin-config',
			'tab-policy-mode': 'inline',
			testURL: Ametys.getPluginDirectPrefix('admin') + '/config/test',
			tableOfContents: true,
			
			listeners: {
				'fieldchange': Ext.bind(this.setDirty, this, [true], false),
				'inputfocus': Ext.bind(this.sendCurrentSelection, this),
				'testresultschange': Ext.bind(this.sendCurrentSelection, this)
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
                id: Ametys.message.MessageTarget.CONFIGURATION,
                parameters: {},
                subtargets: [ this._formPanel.getMessageTargetConf() ]
            }
        });
	},
	
	onOpen: function()
	{
		this.sendCurrentSelection();
	},
	
	close: function(manual)
	{
		var me = this;
        if (this.isDirty())
		{
			Ametys.form.SaveHelper.promptBeforeQuit("{{i18n PLUGINS_ADMIN_CONFIG_DIRTY_CLOSE_MBOX_LABEL}}", 
												    "{{i18n PLUGINS_ADMIN_CONFIG_DIRTY_CLOSE_MBOX_TEXT}}",
												    null,
												    Ext.bind(this._closeCb, this));
			return;
		}

        this.callParent(arguments);
	},
	
    /**
     * Callback for the tool's closing process
     * @param {Boolean} doSave true to save the form before closing the tool, false not to save the form before closing, null to do nothing
     */
	_closeCb: function(doSave)
	{
		if (doSave != null)
		{
			if (doSave)
			{
				var form = this._formPanel;
				Ametys.form.SaveHelper.canSave(form, Ext.bind(Ametys.plugins.admin.config.SaveConfigAction._doSave, Ametys.plugins.admin.config.SaveConfigAction, [form], 1));
			}
			else
			{
				Ametys.plugins.admin.config.ConfigTool.superclass.close.call(this);
			}
		}
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
            errorMessage: {msg: "{{i18n PLUGINS_ADMIN_CONFIG_LOADING_ERROR}}", category: this.self.getName()},
            waitMessage: true
        });
	},
	
	/**
	 * Callback for the retrieval of configuration parameters process
	 * @param {Object} response the server's response
	 * @param {Object} response.configuration the configuration for the form panel
	 * @param {Object} response.configuration-values the values of the configuration parameters
	 */
	_getConfigurationParametersCb: function(response)
	{
		// Initialize the form panel
		this._formPanel.configure(Ext.dom.Query.select("config/configuration", response)[0]);
		
		var configValues = Ext.dom.Query.select("config/configuration-values", response)[0];
		var isConfigEmpty = !configValues.getElementsByTagName("values")[0].hasChildNodes(); 
		
		this._formPanel.setValues(configValues, "values", "comments", "invalid", isConfigEmpty ? null : "{{i18n PLUGINS_ADMIN_CONFIG_EMPTY_WARNING_MESSAGE}}");
		
		this._formPanel.on({
			afterlayout: {fn: this._focusForm, scope: this, single: true} 
		});
	},
	
	/**
	 * @private
	 * Focuses the form panel
	 */
	_focusForm: function()
	{
		this._formPanel.focus();
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
