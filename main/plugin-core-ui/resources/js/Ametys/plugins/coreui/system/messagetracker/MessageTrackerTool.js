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
 * This tool does display the message running on the message bus.
 * @private
 */
Ext.define("Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool",
    {
        extend: "Ametys.plugins.coreui.log.AbstractLogTool",
        
        statics: {
            /**
             * This action find the unique instance of the message tracker tool, and removes all the entries
             */
            removeAll: function()
            {
                var tool = Ametys.tool.ToolsManager.getTool("uitool-messagestracker");
                if (tool != null)
                {
                    tool.store.removeAll();
                }
                else
                {
                    this.getLogger().error("Cannot remove entries from unexisting tool 'uitool-messsagestracker'");
                }
            }
        },
        
        /**
         * @property {Ext.data.ArrayStore} store The store with the messages received
         * @private
         */
        
        constructor: function(config)
        {
            this.callParent(arguments);
            
            this._messageTpl = new Ext.Template(
                "<b>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_ID}}</b> : {id}, - ",
                "<b>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_CREATIONDATE}}</b> : {creationDate}, - ",
                "<b>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_FIREDATE}}</b> : {fireDate}<br/>",
                "<br/>",
                "<b>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_TYPE}}</b> : {type}<br/>",
                "{parameters}",
                "<br/>",
                "<b>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_TARGET}}</b> :<br/>",
                "{targets}<br/>",
                "<br/>",
                "<b>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_CALLSTACK}}</b> :<br/>",
                "{callstack}"
            );
            
            Ametys.message.MessageBus.on('*', this.onMessage, this);
        },
        
        getStore: function()
        {
            this.store = Ext.create("Ext.data.ArrayStore",{
                autoDestroy: true,
                autoSync: true,
                proxy: { type: 'memory' },              
                sorters: [{property: 'fireDate', direction:'DESC'}],
                model: "Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool.MessageEntry"
            });
            
            return this.store;
        },
        
        getColumns: function()
        {
            return [
                {
                    stateId: 'grid-id',
                    header: "{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_ID}}",
                    width: 40,
                    sortable: true,
                    dataIndex: 'id',
                    hideable: false,
                    filter: {
                        type: 'number'
                    }
                },
                {
                    stateId: 'grid-creationdate',
                    header: "{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_CREATIONDATE}}",
                    width: 130,
                    sortable: true,
                    renderer: Ext.util.Format.dateRenderer(Ext.Date.patterns.ShortDateTime),
                    dataIndex: 'creationDate'
                },
                {
                    stateId: 'grid-firedate',
                    header: "{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_FIREDATE}}",
                    width: 130,
                    sortable: true,
                    renderer: Ext.util.Format.dateRenderer(Ext.Date.patterns.ShortDateTime),
                    dataIndex: 'fireDate'
                },
                {
                    stateId: 'grid-type',
                    header: "{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_TYPE}}",
                    width: 150,
                    sortable: true,
                    dataIndex: 'type',
                    filter: {
                        type: 'list'
                    }
                },
                {
                    stateId: 'grid-target',
                    header: "{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_TARGET}}",
                    flex: 1,
                    sortable: true,
                    dataIndex: 'target',
                    renderer: Ext.bind(this._targetRenderer, this)
                }
            ];
        }, 
        
        getDetailsText: function(records)
        {
            var record = records[0];
            var id = record.getId();
            
            var type = record.get("type");
            var parameters = Ext.JSON.prettyEncode(record.get("parameters"));
            var targets = record.get("target");
            var callstack = record.get("callstack");
            
            return this._messageTpl.applyTemplate({
                id: id, 
                creationDate: Ext.Date.format(record.get("creationDate"), Ext.Date.patterns.ShortDateTime),
                fireDate: Ext.Date.format(record.get("fireDate"), Ext.Date.patterns.ShortDateTime),
                type: type,
                parameters: parameters ? parameters + "<br/>" : "",
                targets: this._targetsToString(targets, false),
                callstack: Ext.String.stacktraceToHTML(callstack, 4)
            });
        },
                
        getType: function()
        {
            return Ametys.tool.Tool.TYPE_DEVELOPER;
        },
        
        /**
         * The listener on all bus messages
         * @param {Ametys.message.Message} message The message received
         * @private
         */
        onMessage: function(message)
        {
            var store = this.store;
            try
            {
                var record = Ext.create("Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool.MessageEntry", {
                    id: message.getNumber(),
                    creationDate: message.getCreationDate(),
                    fireDate: new Date(),
                    type: message.getType(),
                    parameters: message.getParameters(),
                    target: message.getTargets(),
                    callstack: message.getCallStack()
                });

                store.addSorted(record);
            }
            catch (e)
            {
                this.getLogger().error({
                    message: "Cannot create the message entry",
                    details: e
                });
            }
        },
        
        /**
         * @private
         * Render the target in the grid
         * @param {Ametys.message.MessageTarget[]} targets The targets to render
         */
        _targetRenderer: function(targets)
        {
            return  this._targetsToString(targets, true);
        },
        
        /**
         * Converts '@link Ametys.message.MessageTarget} to a readable string
         * @param {Ametys.message.MessageTarget[]} targets The message parameters to convert
         * @param {Boolean} [shortVersion=false] When true return only the names (and not the parameters)
         * @param {Number} [offset = 0] The offset to indent the text
         * @private
         */
        _targetsToString: function (targets, shortVersion, offset)
        {
            offset = offset || 0;
            
            var s = "";
            for (var i = 0; i < targets.length; i++)
            {
                var target = targets[i];
                
                if (offset != 0 || i != 0)
                {
                    s += "<br/>";
                }
                
                for (var j = 0; j < offset; j++)
                {
                    s += "&#160;&#160;&#160;&#160;";
                }
                s += "<span style='font-weight: bold'>" + target.getType() + "</span>";
                if (shortVersion !== true) 
                {
                    s += "<br/>" + Ext.JSON.prettyEncode(target.getParameters(), offset, null, 0);
                }
                
                s += this._targetsToString(target.getSubtargets(), shortVersion, offset + 1);
            }
            
            if (targets.length == 0 && offset == 0)
            {
                s = "<span style='color: #7f7f7f; font-style: italic;'>{{i18n PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_NOTARGET}}</span>";
            }
            
            return s;
        }
    }
);
