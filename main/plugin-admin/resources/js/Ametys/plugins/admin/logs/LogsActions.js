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
 * Singleton class defining the actions related to the visualization of logs.
 * @private
 */
Ext.define('Ametys.plugins.admin.logs.LogsActions', {
	singleton: true,
	
	/**
	 * View the selected log file
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	view: function(controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var targetParameters = target.getParameters();
			if (targetParameters.size > 1024 * 1024)
		    {
		    	Ametys.Msg.confirm ("{{i18n PLUGINS_ADMIN_LOGS_VIEW_DIALOG_TITLE}}", 
		    					    "{{i18n PLUGINS_ADMIN_LOGS_VIEW_DIALOG_CONFIRM}}",
		    					    function (answer)
		    					    {
		    							if (answer == 'yes')
		    							{
		    								Ametys.plugins.admin.logs.LogsActions.downloadFiles ([targetParameters.location]);
		    							}
		    					    });
		    }
		    else
		    {
				window.location.href = Ametys.getPluginDirectPrefix('admin') + "/logs/view/" + encodeURIComponent(targetParameters.location);
		    }
		}
	},
	
	/**
	 * Download the selected log file(s)
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	download: function (controller)
	{
		var files = [];
		
		var targets = controller.getMatchingTargets();
		
		for (var i = 0; i < targets.length; i++)
	    {
			files.push(targets[i].getParameters().location)
	    }
		
		this.downloadFiles(files);
	},
	
	/**
	 * Download the selected log files
	 * @param {String} files Files to download
	 */
	downloadFiles: function (files)
	{
	    var url = Ametys.getPluginDirectPrefix('admin') + "/logs/download.zip";
	    var args = "";

	    for (var i = 0; i < files.length; i++)
	    {
	        args += "file=" + encodeURIComponent(files[i]) + "&";
	    }
	    
	    window.location.href = url + "?" + args;
	},
	
	/**
	 * Delete the selected log file(s) with a confirmation dialog
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	deleteFile: function (controller)
	{
		Ametys.Msg.confirm ("{{i18n PLUGINS_ADMIN_LOGS_DELETE_DIALOG_TITLE}}", 
							"{{i18n PLUGINS_ADMIN_LOGS_DELETE_DIALOG_CONFIRM}}", 
							Ext.bind(this._delete, this, [controller], 1));
	},
	
	/**
	 * @private
	 * Callback to actually delete the log files
	 * @param {String} answer Will do the deletion if 'yes'
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller of the clicked button
	 */
	_delete: function(answer, controller)
	{
		if (answer == 'yes')
	    {
			var filesLocations = [];
	        
	        var targets = controller.getMatchingTargets();
	        for (var i = 0; i < targets.length; i++)
	        {
	            var target = targets[i];
	            filesLocations.push(target.getParameters().location);
	        }
	        
	        controller.serverCall('deleteLogs', [filesLocations], Ext.bind(this._deleteCb, this),
				{ 
                    errorMessage: { msg: "{{i18n PLUGINS_ADMIN_LOGS_DELETE_ERROR}}", category: 'Ametys.plugins.admin.actions.LogsActions'},
                    refreshing: true
                } 
			);
	    }
	},
	
	/**
	 * @private 
	 * Callback for the purge/deletion process
	 * @param {Object} response the server's response
	 * @param {String[]} response.failures the list of file locations for failed deletions
	 * @param {String[]} response.successes the list of file locations for successful deletions
	 */
	_deleteCb: function(response)
	{
		this._sendDeletionMessage(response.successes);
		
        if (response.failures.length > 0)
        {
        	Ametys.Msg.show({
				title: "{{i18n PLUGINS_ADMIN_LOGS_DELETE_DIALOG_TITLE}}",
				msg: "{{i18n PLUGINS_ADMIN_LOGS_DELETE_ERROR}}",
				buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
			});
        }
	},
	
	/**
	 * Remove the at least 12 days old log files from the tool with a confirmation dialog
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	purge: function (controller)
	{
		Ametys.Msg.confirm ("{{i18n PLUGINS_ADMIN_LOGS_PURGE_DIALOG_TITLE}}", 
					         "{{i18n PLUGINS_ADMIN_LOGS_PURGE_DIALOG_CONFIRM}}",
					         Ext.bind(this._purge, this, [controller], 1));
	},
	
	/**
	 * @private
	 * Purge the selected files with a confirmation dialog
	 */
	_purge: function (answer, controller)
	{
		if (answer == 'yes')
	    {
	        controller.serverCall('purgeLogs', null, Ext.bind(this._purgeCb, this),
				{ 
                    errorMessage: { msg: "{{i18n PLUGINS_ADMIN_LOGS_PURGE_SERVER_ERROR}}", category: 'Ametys.plugins.admin.actions.LogsActions'},
                    refreshing: true
                } 
			);
	    }	
	},
	
	/**
	 * @private
	 * Callback for the purge process
	 * @param {Object} response the server's response
	 * @param {String[]} response.filesToPurge the files to remove from the logs tool
	 */
	_purgeCb: function(response)
	{
		var deletedFiles = response.successes;
		this._sendDeletionMessage(deletedFiles);
		
        Ametys.Msg.show({
			title: "{{i18n PLUGINS_ADMIN_LOGS_PURGE}}",
			msg: deletedFiles.length == 0 ? "{{i18n PLUGINS_ADMIN_LOGS_PURGE_NONE}}" : deletedFiles.length + " " + "{{i18n PLUGINS_ADMIN_LOGS_PURGE_AMOUNT}}",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO
    	});
	},
	
	/**
	 * @private
	 * Send a deletion message for the logs tool
	 * @param {String[]} deletedFiles the deleted files or the files to remove from the store in the case of a purge
	 */
	_sendDeletionMessage: function(deletedFiles)
	{
		var targets = [];

		Ext.Array.forEach(deletedFiles, function(deletedFile) {
			
			var target = Ext.create('Ametys.message.MessageTarget', {
					id: Ametys.message.MessageTarget.LOG_FILE,
					parameters: {location: deletedFile}
				});
			
			targets.push(target);
		});
		
		if (targets.length > 0)
		{			
			Ext.create('Ametys.message.Message', {
				type: Ametys.message.Message.DELETED,
				targets: targets
			});
		}
	},
	
	/**
	 * Change the logging level of the selected category to the configured #Ametys.plugins.admin.controller.LogLevelController._level
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	changeLogLevel: function(controller)
	{
		var targets = controller.getMatchingTargets();
		if (targets.length > 0)
		{
			var newLevel = controller._level,
			id = targets[0].getParameters().id;
			name = targets[0].getParameters().name;
			
			controller.serverCall('changeLogLevel',
					[newLevel, name], 
					Ext.bind(this._changeLogLevelCB, this),
					{ 
						errorMessage: { 
							msg: "{{i18n PLUGINS_ADMIN_TOOL_LOGS_LEVEL_SERVER_ERROR}}", 
							category: 'Ametys.plugins.admin.actions.LogsActions' 
						},
						arguments: {
							id: id,
							level: newLevel,
							name: name
						},
                        refreshing: true
					}
			);
		}
	},
	
	/**
	 * @private
	 * Callback for the log level changing process
	 * @param {Object} response the server's response
	 * @param {Object} args the callback arguments
	 */
	_changeLogLevelCB: function(response, args)
	{
		var targets = [];
		var target = Ext.create('Ametys.message.MessageTarget', {
		        id: Ametys.message.MessageTarget.LOG_CATEGORY,
				parameters: {id: args.id, level: args.level, name: args.name}
			});
		
		targets.push(target);
		
		if (targets.length > 0)
		{			
			Ext.create('Ametys.message.Message', {
				type: Ametys.message.Message.MODIFIED,
				targets: targets
			});
		}
	}
});