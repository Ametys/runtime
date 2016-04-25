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
 * This class controls a ribbon button allowing to set a data source as the default data source for its type
 * @private
 */
Ext.define('Ametys.plugins.admin.datasource.SetDefaultDataSourceController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
	constructor: function(config)
	{
		this.callParent(arguments);

		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
	},

	/**
	 * @private
	 * Listener on the {@link Ametys.message.Message#MODIFIED} bus message 
	 * @param {Ametys.message.Message} message The modified message.
	 */
	_onModified: function (message)
	{
		if (this.updateTargetsInCurrentSelectionTargets (message))
		{
			this.refresh();
		}
	},
	
    updateState: function()
    {
    	var dataSourceTargets = this.getMatchingTargets();
    	if (dataSourceTargets.length > 0)
		{
    		var dataSourceTarget = dataSourceTargets[0];
    		var isDefault = dataSourceTarget.getParameters().isDefault;
    		
    		var description = "";
    		if (!isDefault)
			{
    			description = "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_SET_DEFAULT_DESCRIPTION}}";
			}
    		else
			{
    			var type = dataSourceTarget.getParameters().type;
    			switch (type)
    			{
    				case 'SQL':
    					description = "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_SET_DEFAULT_IS_DEFAULT_SQL_DESCRIPTION}}";
    					break;
    				case 'LDAP': 
    					description = "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_SET_DEFAULT_IS_DEFAULT_LDAP_DESCRIPTION}}";
    					break;
    				default:
    					throw 'Unrecognized data source type ' + type;
    			
    			}
			}
			this.setDescription(description);
    		
    		this.setIconDecorator(isDefault ? this.getInitialConfig("enabled-icon-decorator") : this.getInitialConfig("disabled-icon-decorator"));
    		this.toggle(isDefault);
    		this.setDisabled(isDefault);
		}
    }
});
