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
 * This class controls a ribbon button allowing to delete a data source if it not currently in use or set as default
 * @private
 */
Ext.define('Ametys.plugins.admin.datasource.DeleteDataSourceController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    constructor: function ()
    {
        this.callParent(arguments);
        this._allRightDataSourceIds = [];
    	
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
        this._getStatus(this.getMatchingTargets());
    },
    
    /**
     * Get the logging level of the selected target
     * @param targets The content targets
     * @private
     */
    _getStatus: function (targets)
    {
        var datasources = {};
        
        for (var i=0; i < targets.length; i++)
        {
            datasources[targets[i].getParameters().id] = targets[i].getParameters().type;
        }
        
        this._allRightDataSourceIds = [];
        this.disable();
        this.serverCall ('getStatus', [datasources], this._getStatusCb, { errorMessage: true, refreshing: true });
    },
    
    /**
     * @private
     * Callback function called after retrieving the 'in-use' state of data source targets
     * @param params The JSON result 
     */
    _getStatusCb: function (params)
    {
        var description = "";
        
        var allRightDataSources = params["allright-datasources"];
        if (allRightDataSources.length > 0)
        {
            if (description != "")
            {
                description += "<br/><br/>";
            }
            
            description += this.getInitialConfig("allright-start-description");
            for (var i=0; i < allRightDataSources.length; i++)
            {
                if (i != 0) 
                {
                    description += ", ";
                }
                description += allRightDataSources[i].name;
                this._allRightDataSourceIds.push(allRightDataSources[i].id);
            }
            description += this.getInitialConfig("allright-end-description");
        }
        
        var inUseDataSources = params["inuse-datasources"];
        if (inUseDataSources.length > 0)
        {
            if (description != "")
            {
                description += "<br/><br/>";
            }
            
            description += this.getInitialConfig("inuse-start-description");
            for (var i=0; i < inUseDataSources.length; i++)
            {
                if (i != 0) 
                {
                    description += ", ";
                }
                description += inUseDataSources[i].name;
            }
            description += this.getInitialConfig("inuse-end-description");
        }
        
        var defaultDataSources = params["internal-datasources"];
        if (defaultDataSources.length > 0)
        {
            if (description != "")
            {
                description += "<br/><br/>";
            }
            
            description += this.getInitialConfig("internal-start-description");
            for (var i=0; i < defaultDataSources.length; i++)
            {
                if (i != 0) 
                {
                    description += ", ";
                }
                description += defaultDataSources[i].name;
            }
            description += this.getInitialConfig("internal-end-description");
        }
        
        this.setDescription (description);
        
        this._allRightDataSourceIds.length == 0 ? this.disable() : this.enable();
    }
});
