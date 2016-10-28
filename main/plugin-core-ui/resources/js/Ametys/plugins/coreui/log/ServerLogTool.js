/*
 *  Copyright 2013 Anyware Services
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
 * A log tool displayed logs from the server. It can be configured to show only a specific category of logs.  
 */
Ext.define('Ametys.plugins.coreui.log.ServerLogTool', {
    extend: "Ametys.plugins.coreui.log.AbstractLogTool",
    
    /**
     * @cfg {String/String[]} category Limit the logs to one or more specified categories. If empty or omitted, all categories are displayed.  
     */
    
    /**
     * @cfg {Number} [refreshTimer=2000] The logs refresh wait time between , in milliseconds. 
     * The tool also waits for the previous request to resolve before launching a new timer, which can result in a few additional seconds between refreshes if there are a lot of data to process or if the server lags.
     */
    
    /**
     * @property {Number} _lastLogTimestamp The most recent timestamp received.
     * @private
     */
    
    /**
     * @property {Number} _timerId The id of the timer for the next refresh.
     * @private
     */    
    /**
     * @property {Boolean} isRunning True if the timer is running.
     */

    constructor: function(config)
    {
        this.callParent(arguments);
        
        this._detailsTpl = new Ext.XTemplate(
            "<tpl if='timestamp'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_TIMESTAMP_TEXT}}</b> {timestamp}<br /></tpl>",
            "<tpl if='user'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_USER_TEXT}}</b> {user}<br /></tpl>",
            "<tpl if='thread'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_THREAD_TEXT}}</b> {thread}<br /></tpl>",
            "<tpl if='level'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_LEVEL_TEXT}}</b> {level}<br /></tpl>",
            "<tpl if='category'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_CATEGORY_TEXT}}</b> {category}<br /></tpl>",
            "<tpl if='requestURI'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_REQUESTURI_TEXT}}</b> {requestURI}<br /></tpl>",
            "<tpl if='message'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_MESSAGE_TEXT}}</b> {message}<br /></tpl>",
            "<tpl if='location'><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_LOCATION_TEXT}}</b> {location}<br /></tpl>",
            "<tpl if='callstack'><br /><b>{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_CALLSTACK_TEXT}}</b><br />{callstack}</tpl>"
        );
    },
    
    setParams: function(params)
    {
        if (this._timer)
        {
            clearTimeout(this._timer);
        }

        this.callParent(arguments);

        this.setTitle(params.title || this.getInitialConfig().title);

        this.logsCategory = Ext.Array.from(params.category);
        this._refreshTimer = params.refreshTimer || 2000;

        var hint = this.grid.getDockedItems("#categories-hint")[0];
        if (this.logsCategory.length > 0)
        {
            hint.setText(Ext.String.format("{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_CATEGORIES_HINT}}", this.logsCategory.join(", ")));
            hint.show();
        }
        else
        {
            hint.hide();
        }
        
        this._lastLogTimestamp = null;
        this.isRunning = true;
        this._logsQueue = [];

        this._store.removeAll();
        this.updateLogs();
    },
    
    getStore: function()
    {
        this._store = Ext.create("Ext.data.ArrayStore",{
            autoDestroy: true,
            trackRemoved: false,
            proxy: { type: 'memory' },
            sorters: [{property: 'timestamp', direction: 'DESC'}],
            model: "Ametys.plugins.coreui.log.ServerLogTool.ServerLogEntry"
        });
        
        return this._store;
    },

    getColumns: function()
    {
        return [{
                stateId: 'grid-timestamp',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_TIMESTAMP_HEADER}}",
                width: 130,
                sortable: true,
                dataIndex: 'timestamp',
                renderer: Ext.bind(this.datetimeRenderer, this)
            },
            {
                stateId: 'grid-user',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_USER_HEADER}}",
                width: 100,
                sortable: true,
                dataIndex: 'user',
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            },
            {
                stateId: 'grid-thread',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_THREAD_HEADER}}",
                width: 150,
                sortable: true,
                dataIndex: 'thread',
                hidden: true,
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            },
            {
                stateId: 'grid-level',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_LEVEL_HEADER}}",
                width: 75,
                sortable: true,
                dataIndex: 'level',
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'list',
                    store: Ext.create('Ext.data.Store', {
                        fields: ['id','text'],
                        data: [
                           {id: "DEBUG", text: 'Debug'},
                           {id: "INFO", text: 'Info'},
                           {id: "WARN", text: 'Warn'},
                           {id: "ERROR", text: 'Error'},
                           {id: "FATAL", text: 'Fatal'}
                        ]
                    })
                }
            },
            {
                stateId: 'grid-category',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_CATEGORY_HEADER}}",
                width: 250,
                flex: 0.25,
                sortable: true,
                dataIndex: 'category',
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            },
            {
                stateId: 'grid-request-uri',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_REQUESTURI_HEADER}}",
                width: 300,
                sortable: true,
                dataIndex: 'requestURI',
                flex: 0.25,
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            },
            {
                stateId: 'grid-message',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_MESSAGE_HEADER}}",
                width: 400,
                sortable: true,
                dataIndex: 'message',
                flex: 0.5,
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            },
            {
                stateId: 'grid-location',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_LOCATION_HEADER}}",
                width: 100,
                sortable: true,
                dataIndex: 'location',
                hidden: true,
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            },
            {
                stateId: 'grid-callstack',
                header: "{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_CALLSTACK_HEADER}}",
                width: 100,
                sortable: true,
                dataIndex: 'callstack',
                hidden: true,
                renderer: Ext.bind(this.defaultRenderer, this),
                filter: {
                    type: 'string'
                }
            }];
    },
    
    getDetailsText: function(records)
    {
        var record = records[0];
        
        var data = {
            timestamp: Ext.Date.format(new Date(record.get("timestamp")), "d/m H:i:s.u"),
            user: record.get('user'),
            level: record.get('level'),
            category: record.get('category'),
            requestURI: Ext.String.htmlEncode(record.get('requestURI')),
            message: Ext.String.htmlEncode(record.get('message')),
            thread: record.get('thread'),
            location: Ext.String.htmlEncode(record.get('location')),
            callstack: record.get('callstack') ? this.stacktraceJavaToHTML(record.get('callstack')) : null
        };
        
        return this._detailsTpl.applyTemplate(data);
    },

    getDockedItems: function()
    {
        return [{
            dock: 'top',
            xtype: 'button',
            hidden: true,
            itemId: 'categories-hint',
            ui: 'tool-hintmessage',
            text: "",
            scope: this,
            handler: this._openLogLevelTool
        }];
    },

    /**
     * Open the tool to set the log level for the current category
     * @private
     */
    _openLogLevelTool: function()
    {
        // TODO
    },
    
    /**
     * Update the grid by calling the server to retrieve the lastest logs.
     */
    updateLogs: function()
    {
        if (!this.isRunning)
        {
            return;
        }
        
        Ametys.data.ServerComm.callMethod({
            role: "org.ametys.plugins.core.ui.log.LogManager", 
            methodName: "getEvents", 
            parameters: [this._lastLogTimestamp, this.logsCategory],
            callback: {
                handler: this._updateLogsCb,
                scope: this
            }
        });
    },
    
    /**
     * Callback after receiving the logs from the server.
     * @param {Object[]} response The logs.
     * @private
     */
    _updateLogsCb: function(response)
    {
        if (!this.isNotDestroyed())
        {
            return;
        }
        
        var logs = [];
        Ext.Array.each(response, function (data) {
            logs.push(Ext.create("Ametys.plugins.coreui.log.ServerLogTool.ServerLogEntry", {
                timestamp: data.timestamp,
                user: data.user,
                level: data.level,
                category: data.category,
                requestURI: data.requestURI,
                message: data.message,
                location: data.location,
                thread: data.thread,
                callstack: data.callstack
            }));
            this._lastLogTimestamp = data.timestamp;
        }, this);
        
        if (logs.length > 0)
        {
            this._logsQueue = this._logsQueue.concat(logs);
            
            var count = this._logsQueue.length;
            if (count > 1000)
            {
                this._logsQueue.splice(0, count - 1000);
            }

            this._store.loadData(this._logsQueue);
        }
        
        if (this.isRunning)
        {
            this._timer = setTimeout(Ext.bind(this.updateLogs, this), this._refreshTimer);
        }
    },
    
    onClose: function()
    {
        if (this.isRunning)
        {
            this.isRunning = false;
            clearTimeout(this._timer);
            this._timer = null;
        }
        
        this.callParent(arguments);
    },
    
    getRowClass: function(record)
    {
        return "msg-level-" + record.get('levelCode');
    },
    
    /**
     * Default renderer for the columns. Apply the coloration.
     * @param {Number} value The level
     * @param {Object} metadata A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style
     * @param {Ext.data.Model} record The record for the current row
     * @private
     */
    defaultRenderer: function(value, metadata, record)
    {
        return Ext.String.htmlEncode(value);
    },
    
    /**
     * Datetime renderer for the columns. Format the timestamp into a human readable time.
     * @param {Number} value The level
     * @param {Object} metadata A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style
     * @param {Ext.data.Model} record The record for the current row
     * @private
     */
    datetimeRenderer: function(value, metadata, record)
    {
        return Ext.Date.format(new Date(value), "d/m H:i:s.u");
    },
    
    /**
     * Action to pause or unpause sending update requests to the server.
     * @param {Boolean} pause True to pause, false to unpause.
     * @private
     */
    pauseUpdates: function(pause)
    {
        if (!pause && !this.isRunning)
        {
            this.isRunning = true;
            this.updateLogs();
        }
        
        if (pause && this.isRunning)
        {
            this.isRunning = false;
            clearTimeout(this._timer);
            this._timer = null;
        }
    },
    
    /**
     * Clear the logs from the grid.
     * @private 
     */
    clearLogs: function()
    {
        this._store.removeAll();
        this._logsQueue = [];
    },
    
    /**
     * Format a JAVA callstack to a HTML string
     * @param {String} stack The callstack
     * @return {String} the formated string
     */
    stacktraceJavaToHTML: function(stack)
    {
        var stack2 = stack.replace(/\r?\n/g, "<br/>");
        var stack3 = "";
        Ext.each(stack2.split('<br/>'), function(node, index) 
            {
                node = Ext.String.htmlEncode(node); 
                
                node = node.replace(/^Caused by:(.*)$/, "<em>Caused by:</em>$1");
                node = node.replace(/^.*at (.*)\((.*):([0-9]*)\)$/, "<span style='margin-left: 15px'></span>at <span class='method'>$1</span>(<span class='filename'>$2</span>:<span class='line'>$3</span>)"); // highlight methods with files and line in "at" lines
                node = node.replace(/^.*at (.*)\(([^:]*)\)$/, "<span style='margin-left: 15px'></span>at <span class='method'>$1</span>($2)"); // hightlight "at" method event if no file or line is specified.

                node = node.replace(/^.*at /, "<span style='margin-left: 15px'></span>at $1"); // indent "at" lines if previous rules did not matched.

                stack3 += node + "<br/>";
            }
        );
        return "<div class='callstack'>" + stack3.substring(0, stack3.length - 5) + "</div>"; // remove last <br/>
    }
});
