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
 * This tool displays monitoring data
 */
Ext.define('Ametys.plugins.admin.jvmstatus.MonitoringTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.Container} _monitoringPanel The JVM status main panel
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function ()
	{
		this._monitoringPanel = Ext.create('Ext.Container', {
									border: false,
									scrollable: true,
									cls: ['uitool-admin-monitoring', 'a-panel-spacing'],
									ui: 'panel-text'
								});
		
		return this._monitoringPanel;
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		this.refresh();
	},
	
	/**
	 * Refreshes the tool
	 */
	refresh: function ()
	{
		this.showRefreshing();

		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.runtime.plugins.admin.jvmstatus.JVMStatusHelper",
			methodName: "getMonitoringData",
			parameters: [],
			callback: {
				scope: this,
				handler: this._refreshCb
			},
			errorMessage: {
				category: this.self.getName(),
				msg: "<i18n:text i18n:key='PLUGINS_ADMIN_TOOL_MONITORING_SERVER_ERROR'/>"
			}
		});
	},
	
	/**
	 * @private
	 * Callback for the refreshing process
	 * @param {Object} response the server's xml response
	 * @param {Object[]} args the callback arguments
	 * @param {Function} args.callback the callback 
	 */
	_refreshCb: function (response, args)
	{
		var items = [];
		for (var i = 0; i < response.samples.sampleList.length; i++)
		{
			var id = response.samples.sampleList[i].id;
			var label = response.samples.sampleList[i].label;
			var description = response.samples.sampleList[i].description;
			
		    items.push({
		    	xtype: 'panel',
		    	cls: 'a-panel-text',

		    	title : label,
		    	border: false,
		    	
				collapsible: true,
				titleCollapse: true,
                
                header: {
                    titlePosition: 1
                },
				
		    	html: '<div class="monitoring">'
		    		+ '    <button style="border-left-style: none;" id="btn-' + id + '-left" onclick="Ametys.plugins.admin.jvmstatus.MonitoringTool.prototype._nextImg(\'' + id + '\', -1,\'' + response.samples.periods + '\'); return false;">&lt;&lt;</button>'
		    		+ '    <img id="img-' + id + '" src="' + Ametys.getPluginDirectPrefix("admin") + '/jvmstatus/monitoring/' + id + '/' + response.samples.periods[1] + '.png" title="' + description + '"/>'
		    		+ '    <button style="border-right-style: none;" id="btn-' + id + '-right"  onclick="Ametys.plugins.admin.jvmstatus.MonitoringTool.prototype._nextImg(\'' + id + '\', +1,\'' + response.samples.periods + '\'); return false;">&gt;&gt;</button>'
		    		+ '<br/><a target="_blank" href="' + Ametys.getPluginDirectPrefix("admin") + '/jvmstatus/monitoring/' + id + '.xml"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_MONITORING_EXPORT"/></a>'
		    	    + '</div>'
		    });
		}
		
		this._monitoringPanel.add(items);
		
		this.showRefreshed();
	},
	
	/**
	 * @private
	 * This method is called to go to the next image in the monitoring panel 
	 * @param {String} id the id of the image
	 * @param {Number} dir the direction, -1 for left, 1 for right
	 * @param {String} periods the coma separated string of available periods of time for the graph
	 */
	_nextImg: function(id, dir, periods)
	{
		periods = periods.split(",");
		
		var img = Ext.get('img-' + id);
		var src = img.dom.src;
		
		var currentPeriod = src.substring(src.lastIndexOf("/") + 1, src.length - 4);
		
		src = src.substring(0, src.lastIndexOf("/") + 1);
		for (var i = 0; i < periods.length; i++)
		{
			if (periods[i] == currentPeriod)
			{
				src += periods[i + dir]
	            if (i + dir == 0)
	            {
	            	Ext.get("btn-" + id + "-left").hide();
	            	Ext.get("btn-" + id + "-right").show();
	            }
	            else if (i + dir == periods.length - 1)
	            {
	            	Ext.get("btn-" + id + "-left").show();
	            	Ext.get("btn-" + id + "-right").hide();
	            }
	            else
	            {
	            	Ext.get("btn-" + id + "-left").show();
	            	Ext.get("btn-" + id + "-right").show();
	            }
				break;
			}
		}
		src += '.png';
			
		img.dom.src = src;
	},
});
