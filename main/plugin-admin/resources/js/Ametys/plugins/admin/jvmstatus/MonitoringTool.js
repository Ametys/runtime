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
 * This tool displays monitoring data
 */
Ext.define('Ametys.plugins.admin.jvmstatus.MonitoringTool', {
	extend: 'Ametys.tool.Tool',
    
    statics: {
        /**
         * Downloads the given chart.
         * @param {String} chartId The chart id
         */
        download: function(chartId)
        {
            var tool = this._getTool();
            if (tool != null)
            {
                tool.download.call(tool, [chartId]);
            }
        },
        
        /**
         * Sets the zoom of all the charts at year level.
         */
        setZoomYear: function()
        {
            this._setZoom(5);
        },
        
        /**
         * Sets the zoom of all the charts at month level.
         */
        setZoomMonth: function()
        {
            this._setZoom(12 * 5);
        },
        
        /**
         * Sets the zoom of all the charts at week level.
         */
        setZoomWeek: function()
        {
            this._setZoom(52 * 5);
        },
        
        /**
         * Sets the zoom of all the charts at day level.
         */
        setZoomDay: function()
        {
            this._setZoom(365 * 5);
        },
        
        /**
         * Sets the zoom of all the charts at hour level.
         */
        setZoomHour: function()
        {
            this._setZoom(365 * 24 * 5);
        },
        
        /**
         * Moves the time axis to now.
         */
        moveToNow: function()
        {
            var tool = this._getTool();
            if (tool != null && !tool.getDrawMode())
            {
                tool.moveToNow.call(tool);
            }
        },
        
        /**
         * Enables / disables the draw mode, which enables to annotate the graphs.
         */
        switchDrawMode: function()
        {
            var tool = this._getTool();
            if (tool != null)
            {
                tool.setDrawMode.call(tool, !tool.getDrawMode());
            }
        },
        
        /**
         * Reloads the graphs data
         */
        reloadGraphs: function()
        {
            var tool = this._getTool();
            if (tool != null)
            {
                tool.reloadActiveGraph.call(tool);
            }
        },
        
        /**
         * @private
         * Sets the given zoom on the charts.
         * @param {Number} zoomValue The zoom value. Must be between 0 (excluded) and 1 (included)
         */
        _setZoom: function(zoomValue)
        {
            var tool = this._getTool();
            if (tool != null && !tool.getDrawMode())
            {
                tool.zoomOnTimeAxis.call(tool, [zoomValue]);
            }
        },
        
        /**
         * @private
         * Gets the monitoring tool.
         * @return {Ametys.plugins.admin.jvmstatus.MonitoringTool} The tool if opened, null otherwise.
         */
        _getTool: function()
        {
            return Ametys.tool.ToolsManager.getTool("uitool-admin-monitoring");
        }
    },
    
	/**
	 * @private
	 * @property {Ext.tab.Panel} _monitoringPanel The JVM status main panel
	 */
    
    /**
     * @private
     * @property {Boolean} _drawMode true if the tool is in draw mode, false otherwise.
     */
    _drawMode: false,
	
    /**
     * Updates the time axis of the charts
     * @param {Number} zoomValue The zoom value. Must greater or equals to 1.
     */
    zoomOnTimeAxis: function(zoomValue)
    {
        var chart = this._monitoringPanel.getActiveTab();
        if (chart != null)
        {
            var axis = chart.getAxis(0), // time axis
                range = axis.getVisibleRange(),
                oldEnd = range[1];
            axis.setVisibleRange([oldEnd - 1 / zoomValue, oldEnd]);
            chart.redraw();
        }
    },
    
    /**
     * Moves the end of the time axis to maximum
     */
    moveToNow: function()
    {
        var chart = this._monitoringPanel.getActiveTab();
        if (chart != null)
        {
            var axis = chart.getAxis(0), // time axis
                range = axis.getVisibleRange(),
                oldStart = range[0],
                oldEnd = range[1],
                length = oldEnd - oldStart;
            axis.setVisibleRange([1-length, 1]);
            chart.redraw();
        }
    },
    
    /**
     * Reloads the active graph data
     */
    reloadActiveGraph: function()
    {
        var chart = this._monitoringPanel.getActiveTab(),
            id = chart.getItemId(),
            serverId = chart.serverId,
            toDate = new Date(),
            fromDate = new Date(toDate.getTime() - (1000*60*60*24*365*5)), // 5 year range
            axis = chart.getAxis(0);
        axis.setFromDate(fromDate);
        axis.setToDate(toDate);
        
        Ametys.data.ServerComm.send({
            plugin: 'admin', 
            url: 'jvmstatus/monitoring/' + serverId + '.json',
            responseType: "text",
            priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
            callback: {
                handler: this._populateGraphData,
                arguments: [id],
                scope: this
            },
            errorMessage: true
        });
    },
    
    /**
     * Sets the value of the draw mode, which enables to annotate the graphs.
     */
    setDrawMode: function(drawMode)
    {
        var chart = this._monitoringPanel.getActiveTab();
        if (chart != null)
        {
            chart.setDraw(drawMode);
            this.enableInteractions(chart, !drawMode);
            chart.getSurface('chart').removeAll(true);
            chart.renderFrame();
        }
        
        this._drawMode = drawMode;
        
        Ext.create("Ametys.message.Message", {
            type: Ametys.message.Message.MODIFIED,
            
            targets: {
                id: Ametys.message.MessageTarget.TOOL,
                parameters: { tools: [this] }
            }
        });
    },
    
    /**
     * Gets the draw mode state of the tool.
     * @return {Boolean} true if the tool is in draw mode, false otherwise.
     */
    getDrawMode: function()
    {
        return this._drawMode;
    },
    
    /**
     * Function to render values on a pretier way.
     * @param {String} value The value to render.
     * @return {String} The label to display.
     */
    renderValue: function(value) {
        return Ext.util.Format.number(value, '0,000.###'); // 3 digits max after decimal point, and will use the locale default separators for thousand and decimal
    },
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function ()
	{
		this._monitoringPanel = Ext.create('Ext.tab.Panel', {
            tabPosition: 'left',
            tabRotation: 0,
            listeners: {
                'tabchange': Ext.bind(function(tabPanel, newChart, oldChart, eOpts) {
                    if (oldChart != null)
                    {
                        newChart.getAxis(0).setVisibleRange(oldChart.getAxis(0).getVisibleRange());
                    }
                    this.setDrawMode(this.getDrawMode());
                    this.reloadActiveGraph();
                }, this)
            }
        });
		
		return this._monitoringPanel;
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		this.showOutOfDate();
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
				msg: "{{i18n PLUGINS_ADMIN_TOOL_MONITORING_SERVER_ERROR}}"
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
        if (!this.isNotDestroyed())
        {
            return;
        }
		
        // Construct the graphs
        var sampleList = response['samples']['sampleList'];
        Ext.Array.forEach(sampleList, function(sample) {
            var serverId = sample.id,
                id = serverId.replace(/\./g, '-'), // no '.' in ExtJS ids
                label = sample.label,
                description = sample.description,
                thresholds = sample.thresholds,
            
                toDate = new Date(),
                fromDate = new Date(toDate.getTime() - (1000*60*60*24*365*5)), // 5 year range
                chartCfg = this._getChartCfg(id, serverId, label, description, fromDate, toDate, thresholds);
                
	        this._monitoringPanel.add([chartCfg]);
		}, this);
		
        var first = this._monitoringPanel.items.first();
        if (first != null)
        {
            this._monitoringPanel.setActiveTab(first);
            this.statics().setZoomDay();
        }
            
		this.showRefreshed();
	},
    
    /**
     * @private
     * Callback after receiving data from the server to populate the graph.
     * @param {Object} response the server response
     * @param {Array} args The callback arguments
     */
    _populateGraphData: function(response, args)
    {
        if (this._monitoringPanel == null)
        {
            return;
        }
        
        var id = args[0],
            chart = this._monitoringPanel.getComponent(id),
            store = chart.getStore();
        
        if (chart.rendered)
        {
            // The server returned a string json, decode it into an array of object
            var stringData = response.firstChild.textContent;
            var data = Ext.decode(stringData, true);
            
            // Compute the fields from data object
            function getFields(data)
            {
                var fields = [];
                Ext.Array.forEach(data, function(item) {
                    Ext.Object.each(item, function(key, value) {
                        if (!Ext.Array.contains(fields, key))
                        {
                            fields.push(key);
                        }
                    });
                });
                return fields;
            };
            var fields = getFields(data);
            fields.sort().reverse(); //all MAX must be before AVERAGE for graphical purposes
            
            // Create the series
            var series = [];
            Ext.Array.forEach(fields, function(field) {
                if (field != 'time')
                {
    	            series.push(this._getLineSerieCfg(field));
                }
            }, this);
            
            // Draw the series in the chart
            chart.setSeries(series);
            store.setFields(fields);
            store.setData(data);
            
            // depending on the series length, set the colors (AVERAGE will be same colors as MAX but a bit lighter)
            var colorSet = ['#115fa6', '#ff8809', '#a61187', '#94ae0a', '#a61120', '#ffd13e', '#7c7474', '#a66111', '#24ad9a'],
                offset = series.length / 2;
            function shadeColor2(color, percent) { // see http://stackoverflow.com/questions/5560248/programmatically-lighten-or-darken-a-hex-color-or-rgb-and-blend-colors
                var f=parseInt(color.slice(1),16),t=percent<0?0:255,p=percent<0?percent*-1:percent,R=f>>16,G=f>>8&0x00FF,B=f&0x0000FF;
                return "#"+(0x1000000+(Math.round((t-R)*p)+R)*0x10000+(Math.round((t-G)*p)+G)*0x100+(Math.round((t-B)*p)+B)).toString(16).slice(1);
            }
            
            Ext.Array.erase(colorSet, offset, -1);
            var newColors = colorSet;
            Ext.Array.forEach(colorSet, function(color, index) {
                newColors[index + offset] = shadeColor2(color, 0.5);
            }, this);
            chart.setColors(newColors);
        }
    },
    
    /**
     * @private
     * Gets the object config for creating a graph
     * @param {String} itemId The item id
     * @param {String} serverId The id of server's sample manager
     * @param {String} label The title of the graph panel
     * @param {Date} fromDate The start of the time axis
     * @param {Date} toDate the end of the time axis
     * @param {Object} thresholds the thresholds for the limits to display. Can be null.
     * @return {Object} The configuration object for creating a graph
     */
    _getChartCfg: function(itemId, serverId, label, description, fromDate, toDate, thresholds)
    {
        return {
            xtype: 'drawable-cartesian',
            title: label,
            border: false,
            collapsible: true,
            titleCollapse: true,
            header: {
                titlePosition: 1
            },
            itemId: itemId,
            serverId: serverId,
            style: {
                marginBottom: '50px'
            },
                
            store: {
                fields: ['time'],
                data: []
            },
            
            interactions: [this._getPanzoomInteractionCfg()],
            
            legend: {
                docked: 'right'
            },
            shadow: false,
            animation: true,
            
            axes: [{
                type: 'time',
                position: 'bottom',
                grid: true,
                dateFormat: Ext.Date.patterns.FriendlyDateTime,
                fromDate: fromDate,
                toDate: toDate,
                maxZoom: 500000,
                label: {
                    rotate: {
                        degrees: -45
                    }
                }
            }, {
                type: 'numeric',
                position: 'left',
                grid: true,
                minimum: 0,
                limits: this._getLimitLineCfg(thresholds),
                renderer: Ext.bind(function(axis, label, layoutContext) {
                    var value = layoutContext.renderer(label);
                    return this.renderValue(value);
                }, this)
            }],
            
            series: [],
            
            listeners: {
                afterrender: Ext.bind(function(chart) {
                    // Draw our components with the legend (in the rbar)
                    var rbar = chart.getDockedItems('[dock="right"]')[0];
                    rbar.insert(0, {
                        xtype: 'component',
                        html: description,
                        maxWidth: 250,
                        padding: '5px'
                    });
                    
                    var onDownloadClickFn = Ext.ClassManager.getName(this) + ".download('" + itemId + "')";
                    rbar.add({
                        xtype: 'component',
                        html: '<a href="javascript:void(0)" onclick="' + onDownloadClickFn + '">' + '{{i18n PLUGINS_ADMIN_TOOL_MONITORING_CHART_PREVIEW}}' + '</a>',
                        platformConfig: {
                            desktop: {
                                html: '<a href="javascript:void(0)" onclick="' + onDownloadClickFn + '">' + '{{i18n PLUGINS_ADMIN_TOOL_MONITORING_CHART_DOWNLOAD}}' + '</a>'
                            }
                        }
                    });
                }, this)
            }
        };
    },
    
    /**
     * @private
     * Gets the object config for the panzoom interaction
     * @return {Object} The configuration object for creating our custom panzoom interaction
     */
    _getPanzoomInteractionCfg: function()
    {
        return {
            xtype: 'timepanzoom'
        };
    },
    
    /**
     * @private
     * Gets the object config for the limit line
     * @param {Object} thresholds the thresholds for the limits to display. Can be null.
     * @return {Object[]} The configuration object for creating limit lines
     */
    _getLimitLineCfg: function(thresholds)
    {
        var limits = [];
        thresholds = thresholds || {};
        Ext.Object.each(thresholds, function(dsName, value) {
            limits.push({
                value: value,
                line: {
                    title: {
                        text: Ext.util.Format.format('{{i18n PLUGINS_ADMIN_TOOL_MONITORING_CHART_THRESHOLD}}', dsName, value)
                    },
                    lineDash: [2,2]
                }
            });
        }, this);
        
        return limits;
    },
    
    /**
     * @private
     * Gets the object config for creating a serie.
     * @param {String} field The name of the field for this serie
     * @return {Object} The configuration object for the series
     */
    _getLineSerieCfg: function(field)
    {
        return {
            type: 'line',
            xField: 'time',
            yField: field,
            axis: 'bottom',
            fill: true,
            nullStyle: 'connect',
            style: {
                fillOpacity: .6
            },
            tooltip: {
                trackMouse: true,
                dismissDelay: 0,
                renderer: Ext.bind(function (tooltip, record, item) {
                    var date = Ext.Date.format(new Date(record.get('time')), Ext.Date.patterns.FriendlyDateTime);
                    var value = this.renderValue(record.get(field));
                    tooltip.setHtml(field + ' (' + date + '): ' + value);
                }, this)
            }
        }
    },
    
    /**
     * Downloads the given chart.
     * @param {String} chartId The chart id
     */
    download: function(chartId)
    {
        var chart = this._monitoringPanel.items.get(chartId);
        if (Ext.os.is.Desktop) {
            chart.download({
                filename: chart.getTitle()
            });
        } else {
            chart.preview();
        }
    },
    
    /**
     * Enables or disables the interactions of the given chart
     * @param {Ext.chart.CartesianChart} chart The chart
     * @param {Boolean} enable true to enable the interactions, false otherwise.
     */
    enableInteractions: function(chart, enable)
    {
        Ext.Array.forEach(chart.getInteractions(), function (interaction) {
            interaction.setEnabled(enable);
        }, this);
    }
});