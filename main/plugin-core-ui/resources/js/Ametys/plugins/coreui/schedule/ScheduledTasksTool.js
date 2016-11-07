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
 * Tool for displaying the scheduled tasks (the 'Runnables') of the application.
 */
Ext.define('Ametys.plugins.coreui.schedule.ScheduledTasksTool', {
    extend: "Ametys.tool.Tool",
    
    /**
     * @property {Ext.data.ArrayStore} _store The store with the tasks
     * @private
     */
    
    /**
     * @property {Ext.grid.Panel} _grid The grid panel displaying the tasks
     * @private
     */
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageModified, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
    },
    
    /**
     * Gets the grid of the tool
     * @return {Ext.grid.Panel} the grid of the tool
     */
    getGrid: function()
    {
        return this._grid;
    },
    
    createPanel: function()
    {
        this._store = this._createStore();
        
        this._grid = Ext.create("Ext.grid.Panel", { 
            store: this._store,
            stateful: true,
            stateId: this.self.getName() + "$grid",
            
            // Grouping by state
            features: [
                {
                    ftype: 'grouping',
                    enableGroupingMenu: false,
                    groupHeaderTpl: [
                        '{name:this.formatState}', 
                        {
                            formatState: function(group) {
                                switch (group) {
                                    case "enabled":
                                        return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_GROUP_ENABLED_LABEL}}";
                                    case "disabled":
                                        return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_GROUP_DISABLED_LABEL}}";
                                    case "completed":
                                    default:
                                        return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_GROUP_COMPLETED_LABEL}}";
                                }
                            }
                        }
                    ]
                } 
            ],
            plugins: 'gridfilters',
            
            columns: [
                 {stateId: 'grid-state', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_STATE}}", width: 50, dataIndex: 'state', renderer: this._renderState},
                 {stateId: 'grid-title', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_TITLE}}", flex: 1, minWidth: 100, sortable: true, dataIndex: 'label'},
                 {stateId: 'grid-description', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_DESCRIPTION}}", flex: 2, dataIndex: 'description', hidden: true},
                 {stateId: 'grid-id', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_ID}}", width: 120, dataIndex: 'id', hidden: true},
                 {stateId: 'grid-cron', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_CRON}}", width: 170, dataIndex: 'cronExpression'},
                 {
                    itemId: 'grid-schedulable-id', 
                    stateId: 'grid-schedulable-id', 
                    header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_SCHEDULABLE_LABEL}}", 
                    flex: 1, 
                    dataIndex: 'schedulableId',
                    renderer: this._renderSchedulableId,
                    filter: {
                        type: 'list',
                        store: Ext.create('Ext.data.Store', {
                            fields: ['id','text'],
                            data: []
                        })
                    }
                 },
                 {stateId: 'grid-schedulable-description', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_SCHEDULABLE_DESCRIPTION}}", flex: 2, dataIndex: 'schedulableDescription', hidden: true},
                 {stateId: 'grid-schedulable-parameters', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_SCHEDULABLE_PARAMETERS}}", flex: 2, dataIndex: 'schedulableParameters', renderer: this._renderSchedulableParameters, hidden: true},
                 {stateId: 'grid-system-task', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_TYPE}}", width: 100, dataIndex: 'private', renderer: this._renderTaskType},
                 {stateId: 'grid-next-fire', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_NEXT_FIRE}}", width: 150, dataIndex: 'nextFireTime', renderer: this._renderDate},
                 {stateId: 'grid-previous-fire', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_PREVIOUS_FIRE}}", width: 150, dataIndex: 'previousFireTime', renderer: this._renderDate},
                 {stateId: 'grid-last-duration', header: "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_LAST_DURATION}}", width: 140, dataIndex: 'lastDuration', renderer: this._renderDuration}
                 
            ],
            
            listeners: {'selectionchange': Ext.bind(this.sendCurrentSelection, this)}
        });
        
        return this._grid;
    },
    
    /**
     * @private
     * Create the store for tasks.
     * @return {Ext.data.Store} The store
     */
    _createStore: function()
    {
        return Ext.create('Ext.data.Store', {
            autoDestroy: true,
            model: 'Ametys.plugins.coreui.schedule.ScheduledTasksTool.TaskEntry',
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'scheduledTasks.json',
                reader: {
                    type: 'json',
                    rootProperty: 'tasks'
                }
             },
             
             groupField: 'group',
             sortOnLoad: true,
             sorters: [{property: 'label', direction:'ASC'}]
        });
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_ACTIVE;
    },
    
    setParams: function(params)
    {
        this.callParent(arguments);
        this.showOutOfDate();
    },
    
    refresh: function()
    {
        this.showRefreshing();

        // Add the schedulables in the store of the filter
        Ametys.plugins.core.schedule.Scheduler.getSchedulables([], function(schedulables) {
            this._grid.down('#grid-schedulable-id').filter.store.loadData(schedulables);
        }, {scope: this});
        
        // Load the store of the grid
        this._store.load({callback: this.showRefreshed, scope: this});
    },
    
    sendCurrentSelection: function()
    {
        var selection = this._grid.getSelection();
        
        var tasks = [];
        Ext.Array.forEach(selection, function(task) {
            tasks.push(
                Ext.create('Ametys.plugins.coreui.schedule.Task', {
                    id: task.get('id'),
                    modifiable: task.get('modifiable'),
                    removable: task.get('removable'),
                    deactivatable: task.get('deactivatable')
                })
            );
        }, this);
        
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.SELECTION_CHANGED,
            targets: {
                id: Ametys.message.MessageTarget.TASK,
                parameters: {
                    tasks: tasks
                }
            }
        });
    },
    
    /**
     * @private
     * Schedulable renderer
     * @param {Object} value The data value
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderSchedulableId: function(value, metaData, record)
    {
        var label = record.get('schedulableLabel'),
            iconGlyph = record.get('iconGlyph'),
            iconSmall = record.get('iconSmall');
        if (!Ext.isEmpty(iconGlyph))
        {
            return '<span class="' + iconGlyph + '"></span> ' + label;
        }
        else
        {
            return '<img src="' + Ametys.getPluginResourcesPrefix('core-ui') + '/' + iconSmall + '"></img> ' + label;
        }
    },
    
    /**
     * @private
     * Renders the parameters of the parameters
     * @param {Object} value The data value
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderSchedulableParameters: function(value, metaData, record)
    {
        var result = "";
        Ext.Object.each(value, function(paramId, details) {
            result += details.label + ", ";
        }, this);
        return result.replace(/, $/, "");
    },
    
    /**
     * @private
     * Renders the value of the "system" column.
     * @param {Boolean} value The data value for the current cell.
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderTaskType: function(value, metaData, record)
    {
        var isTrue = Ext.isBoolean(value) ? value : value == 'true';
        if (isTrue)
        {
            return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_TYPE_SYSTEM}}"; 
        }
        else
        {
            return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COLUMN_TYPE_USER}}"; 
        }
    },
    
    /**
     * @private
     * Renders the value of the "running" column.
     * @param {Boolean} value The data value for the current cell.
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderState: function(value, metaData, record)
    {
        if (value == 'running')
        {
            return '<span class="a-grid-glyph ametysicon-play124 task-running" title="{{i18n PLUGINS_CORE_UI_TASKS_TOOL_STATE_RUNNING}}"/>';
        }
        else if (value == 'success')
        {
            return '<span class="a-grid-glyph ametysicon-checked34 task-success" title="{{i18n PLUGINS_CORE_UI_TASKS_TOOL_STATE_SUCCESS}}"/>';
        }
        else if (value == 'failure')
        {
        	return '<span class="a-grid-glyph ametysicon-cross-1 task-failure" title="{{i18n PLUGINS_CORE_UI_TASKS_TOOL_STATE_FAILURE}}"/>';
        }
        else
        {
        	return '<span class="a-grid-glyph ametysicon-clock169" title="{{i18n PLUGINS_CORE_UI_TASKS_TOOL_STATE_AWAITING}}"/>';
        }
    },
    
    /**
     * @private
     * Renders the value of the "running" column.
     * @param {Boolean} value The data value for the current cell.
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderRunning: function(value, metaData, record)
    {
        var isTrue = Ext.isBoolean(value) ? value : value == 'true';
        if (isTrue)
        {
            return '<span class="a-grid-glyph ametysicon-play124"/>';
        }
        else
        {
            return '';
        }
    },
    
    /**
     * @private
     * Renders the value of the "success" column.
     * @param {Boolean} value The data value for the current cell.
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderSuccess: function(value, metaData, record)
    {
        if (value == null)
        {
            // The task did not run yet or is currently running, display nothing
            return '';
        }
        else if (value)
        {
            return '<span class="a-grid-glyph ametysicon-check34"/>';
        }
        else
        {
            return '<span class="a-grid-glyph ametysicon-delete30"/>';
        }
    },
    
    /**
     * @private
     * Date renderer
     * @param {Object} value The data value
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html representation
     */
    _renderDate: function(value, metaData, record)
    {
        if (Ext.isString(value))
        {
            return value;
        }
        var formattedValue = value == null ? '-' : Ext.util.Format.date(new Date(value), Ext.Date.patterns.FriendlyDateTime);
        return formattedValue;
    },
    
    /**
     * @private
     * Duration renderer
     * @param {Object} value The data value
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record
     */
    _renderDuration: function(value, metaData, record)
    {
        return value == null ? '-' : Ext.util.Format.duration(value);
    },
    
    /**
     * Listener on creation message.
     * @param {Ametys.message.Message} message The edition message.
     * @private
     */
    _onMessageCreated: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.TASK);
        if (targets.length > 0)
        {
            this.showOutOfDate();
        }
    },
    
    /**
     * Listener on edition message.
     * @param {Ametys.message.Message} message The edition message.
     * @private
     */
    _onMessageModified: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.TASK);
        if (targets.length > 0)
        {
            this.showOutOfDate();
        }
    },
    
    /**
     * Listener on deletion message.
     * @param {Ametys.message.Message} message The deletion message.
     * @private
     */
    _onMessageDeleted: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.TASK);
        Ext.Array.forEach(targets, function(target) {
            var record = this._store.getById(target.getParameters().id);
            this._grid.getSelectionModel().deselect([record]);
            this._store.remove(record);
        }, this);
    }
    
});

/**
 * This class is the model for entries in the grid of the scheduled tasks
 * @private
 */
Ext.define("Ametys.plugins.coreui.schedule.ScheduledTasksTool.TaskEntry", {
    extend: 'Ext.data.Model',
    
    fields: [
        {name: 'id'},
        {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString},
        {name: 'description'},
        
        {name: 'cronExpression', convert: function(v, rec) {
            switch (rec.get('fireProcess')) {
                case "STARTUP":
                    return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_STARTUP_TASK}}";
                case "NOW":
                    return "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_NOW_TASK}}";
                case "CRON":
                default:
                    return v;
            }
        }},
        {name: 'enabled', defaultValue: false},
        {name: 'completed', defaultValue: false},
        {
        	depends: ['enabled', 'completed'],
        	name: 'group', 
        	calculate: function(data) {
        		return data.completed ? "completed" : (data.enabled ? "enabled" : "disabled");
        	}
        },
        
        {name: 'modifiable'},
        {name: 'removable'},
        {name: 'deactivatable'},
        
        {name: 'schedulableId', mapping: function(data) {return data.schedulable.id;}},
        {name: 'schedulableLabel', mapping: function(data) {return data.schedulable.label;}},
        {name: 'schedulableDescription', mapping: function(data) {return data.schedulable.description;}},
        {name: 'schedulableParameters', mapping: function(data) {return data.schedulable.parameters;}},
        {name: 'iconGlyph', mapping: function(data) {return data.schedulable.iconGlyph;}},
        {name: 'iconSmall', mapping: function(data) {return data.schedulable.iconSmall;}},
        {name: 'private', mapping: function(data) {return data.schedulable['private'];}},
        
        {name: 'nextFireTime', convert: function(v, rec) {
            switch (rec.get('fireProcess')) {
                case "STARTUP":
                    return rec.get('completed') ? "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COMPLETED_TASK}}" : "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_NEXT_STARTUP_TASK}}";
                case "NOW":
                    return rec.get('completed') ? "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_COMPLETED_TASK}}" : "{{i18n PLUGINS_CORE_UI_TASKS_TOOL_RUNNING_TASK}}";
                case "CRON":
                default:
                    return v;
            }
        }},
        {name: 'previousFireTime'},
        {name: 'lastDuration'},
        {name: 'success'},
        {name: 'running'},
        {
        	name: 'state', 
        	depends: ['success', 'running', 'completed'],
        	calculate: function(data) {
        		if (data.completed)
        		{
        			return data.success ? 'success' : 'failure';
        		}
        		else if (data.running)
        		{
        			return 'running';
        		}
        		else
        		{
        			return 'waiting';
        		}
        	}
        }
    ]
});
