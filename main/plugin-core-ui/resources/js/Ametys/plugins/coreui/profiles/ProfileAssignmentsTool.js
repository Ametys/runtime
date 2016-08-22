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
 * Tool which displays a grid with the assignments for users and groups on the profiles of the application, depending on a context object.
 */
Ext.define('Ametys.plugins.coreui.profiles.ProfileAssignmentsTool', {
    extend: "Ametys.tool.Tool",
    
    statics: {
        /**
         * @readonly
         * @property {String} ASSIGNMENT_TYPE_ANONYMOUS The record is an assignment for an anonymous user
         */
        ASSIGNMENT_TYPE_ANONYMOUS: '1-anonymous',
        /**
         * @readonly
         * @property {String} ASSIGNMENT_TYPE_ANYCONNECTEDUSER The record is an assignment for any connected user
         */
        ASSIGNMENT_TYPE_ANYCONNECTEDUSER: '2-anyconnected',
        /**
         * @readonly
         * @property {String} ASSIGNMENT_TYPE_USERS The record is an assignment for a user
         */
        ASSIGNMENT_TYPE_USERS: '3-users',
        /**
         * @readonly
         * @property {String} ASSIGNMENT_TYPE_GROUPS The record is an assignment for a group
         */
        ASSIGNMENT_TYPE_GROUPS: '4-groups',
        
        /**
         * @readonly
         * @property {String} READER_PROFILE_ID The id of the special profile (READER profile)
         */
        READER_PROFILE_ID: 'READER',
        
        /**
         * Function called when an assignment is clicked in order to change its value
         * @param {String} recordId The id of the record
         * @param {String} profileId The profile id (id of the column)
         * @param {String} value The current value
         */
        onCellClick: function(recordId, profileId, value)
        {
            var tool = Ametys.tool.ToolsManager.getTool("uitool-profile-assignment");
            if (tool != null)
            {
                tool.onCellClick(recordId, profileId, value);
            }
        }
    },
    
    /**
     * @private
     * @property {Ext.form.field.ComboBox} _contextCombobox The combobox displaying the right assignment contexts
     */
    
    /**
     * @private
     * @property {Ext.panel.Panel} _contextPanel The panel on the left of the tool, with a card layout, showing the context panel corresponding to the currently selected right assignment context in the combobox
     */
    
    /**
     * @private
     * @property {Object} _contextComponents An object containing the context {@link Ext.Component}s (the key is the right assignment context id)
     */
    
    /**
     * @private
     * @property {Object} _classNames An object containing the ExtJS class names for each right assignment context (the key is the right assignment context id)
     */
    
    /**
     * @private
     * @property {Ext.grid.Panel} _assignmentsGrid The grid panel on the right of the tool, showing the assignment on current object context.
     */
    
    /**
     * @private
     * @property {Ext.data.Store} _gridStore The store of the grid
     */
    
    /**
     * @private
     * @property {Object} _objectContext The current object context. Must be up-to-date before loading the grid store 
     */
    
    /**
     * @private
     * @property {Object[]} _parentObjectContexts The current parent object contexts. First item in the array is the direct parent, etc. Must be up-to-date before loading the grid store 
     */
    
    /**
     * @private
     * @property {Object[]} _profiles The profiles of the application.
     * @property {String} _profiles.id The id of the profile
     * @property {String} _profiles.label The label of the profile
     * @property {String[]} _profiles.rights The ids of the rights this profile contains
     */
    _profiles: [],
    
    /**
     * @private
     * @property {Object} _columns Object containing the columns representing the profiles. 
     * The key is the profile id, the value is an object (where its key is the record id, the value is the cell value from the server data before computing possible induced values, so it can only be 'unknown', 'localAllow', 'inheritAllow', 'localDenied' or 'inheritDenied')
     */
    
    createPanel: function()
    {
        this._contextCombobox = Ext.create('Ext.form.field.ComboBox', this._getContextComboCfg());
        
        this._contextPanel = Ext.create('Ext.panel.Panel', {
            width: 500,
            minWidth: 300,
            scrollable: true,
            layout: 'card',
            
            dockedItems: [{
                xtype: 'toolbar',
                layout: { 
                    type: 'hbox',
                    align: 'stretch'
                },
                dock: 'top',
                
                items: [this._contextCombobox]
            }],
            
            listeners: {
                'objectcontextchange': Ext.bind(this._onObjectContextChange, this)
            }
        });
        
        this._gridStore = Ext.create('Ext.data.Store', {
            model: 'Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry',
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'profileAssignments.json',
                reader: {
                    type: 'json',
                    rootProperty: 'assignments'
                }
            },
            groupField: 'assignmentType',
            sortOnLoad: true,
            sorters: [{property: 'sortableLabel', direction:'ASC'}],
            
            listeners: {
                'beforeload': Ext.bind(this._onBeforeLoadGrid, this),
                'update': Ext.bind(this._onUpdateOrLoadGrid, this),
                'load': Ext.bind(this._onUpdateOrLoadGrid, this)
            }
        });
        
        this._assignmentsGrid = Ext.create('Ext.grid.Panel', {
            dockedItems: this._getGridDockedItemsCfg(),
            
            flex: 3,
            split: true,
            scrollable: true,
            enableColumnMove: true,
            
            store: this._gridStore,
            listeners: {
                'selectionchange': Ext.bind(this.sendCurrentSelection, this)
            },
            
            stateful: true,
            stateId: this.self.getName() + "$grid",
            
            selModel: {
                mode: 'MULTI'
            },
            
            // Grouping by assignment type
            features: [{
                ftype: 'grouping',
                enableGroupingMenu: false,
                expandTip: "",
                collapseTip: "",
                groupHeaderTpl: [
                    '{name:this.formatAssignmentType}', 
                    {
                        formatAssignmentType: Ext.bind(function(type) {
                            switch (type) {
                                case this.self.ASSIGNMENT_TYPE_ANONYMOUS:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_ASSIGNMENT_TYPE_ANONYMOUS}}";
                                case this.self.ASSIGNMENT_TYPE_ANYCONNECTEDUSER:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_ASSIGNMENT_TYPE_ANYCONNECTEDUSERS}}";
                                case this.self.ASSIGNMENT_TYPE_USERS:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_ASSIGNMENT_TYPE_USERS}}";
                                case this.self.ASSIGNMENT_TYPE_GROUPS:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_ASSIGNMENT_TYPE_GROUPS}}";
                                default:
                                    // would never go there
                                    return "";
                            }
                        }, this)
                    }
                ]
            }]
        });
        
        var mainPanel = Ext.create("Ext.container.Container", {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            cls: 'uitool-profile-assignment',
            items: [this._contextPanel, this._assignmentsGrid]
        });
        
        return mainPanel;
    },
    
    /**
     * @private
     * Gets the configuration of the combobox for assignment contexts
     * @return {Object} The config object
     */
    _getContextComboCfg: function()
    {
        return {
            store: {
                fields: ['value', {name: 'displayText', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
                proxy: {
                    type: 'ametys',
                    plugin: 'core-ui',
                    url: 'rightAssignmentContexts.json',
                    reader: {
                        type: 'json',
                        rootProperty: 'contexts'
                    }
                },
                sorters: [{property: 'displayText', direction: 'ASC'}]
            },
            autoSelect: false,
            
            listeners: {
                'change': Ext.bind(this._onComboboxChange, this)
            },
            
            fieldLabel : "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CONTEXT}}",
            queryMode: 'local',
            allowBlank: false,
            forceSelection: true,
            triggerAction: 'all',
            
            valueField: 'value',
            displayField: 'displayText',
            
            labelWidth: 150,
            width: '100%',
            style: {
                borderBottomStyle: 'solid',  
                borderBottomWidth: '1px',
                paddingBottom: '8px'
            }
        };
    },
    
    /**
     * @private
     * Function called when the current object context has changed.
     * @param {Object} object The new object context
     * @param {Object[]} parentObjects The new parent object contexts
     * @param {String} hintTextContext The hint text to update
     */
    _onObjectContextChange: function(object, parentObjects, hintTextContext)
    {
        if (this.isDirty())
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CHANGE_CONTEXT_BOX_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CHANGE_CONTEXT_BOX_MESSAGE}}",
                buttons: Ext.Msg.YESNO,
                icon: Ext.MessageBox.QUESTION,
                fn: Ext.bind(callback, this, [object, parentObjects, hintTextContext], 1)
            });
        }
        else
        {
            this._internalChangeObjectContext(object, parentObjects, hintTextContext);
        }
        
        function callback(btn, object, parentObjects, hintTextContext)
        {
            if (btn == 'yes')
            {
                this._saveChanges(this._contextCombobox.getValue(), Ext.bind(this._internalChangeObjectContext, this, [object, parentObjects, hintTextContext]));
            }
            else
            {
                this._internalChangeObjectContext(object, parentObjects, hintTextContext);
            }
            
        }
    },
    
    /**
     * @private
     * Changes the internal representation of the object context (and its parent object contexts), update the hint text of the grid and updates the grid.
     * @param {Object} object The new object context
     * @param {Object[]} parentObjects The new parent object contexts
     * @param {String} hintTextContext The hint text to update
     */
    _internalChangeObjectContext: function(object, parentObjects, hintTextContext)
    {
        this._clearFilters(); // avoid bugs in the grid store before loading it
        
        this.getLogger().info("Context has changed : " + object);
        this._objectContext = object;
        this._parentObjectContexts = parentObjects;
        
        this._assignmentsGrid.getDockedItems('#context-helper-text')[0].update("{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_HINT1}}" + hintTextContext);
        this._updateGrid();
    },
    
    /**
     * @private
     * Gets the configuration of the docked items of the grid
     * @return {Object[]} the docked items
     */
    _getGridDockedItemsCfg: function()
    {
        return [{
            xtype: 'component',
            itemId: 'context-helper-text',
            ui: 'tool-hintmessage'
        }, {
            dock: 'top',
            xtype: 'toolbar',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            border: false,
            defaultType: 'textfield',
            items: [{
                xtype: 'component',
                html: '{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_FILTERS}}'
            }, {
                xtype: 'edition.right',
                itemId: 'profile-filter',
                name: 'profile-filter',
                cls: 'ametys',
                allowBlank: true,
                multiple: false,
                stacked: "false",
                width: 400,
//                emptyText: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_RIGHT_FILTER}}", // FIXME https://www.sencha.com/forum/showthread.php?308083
                listeners: {change: Ext.bind(this._filterByRight, this)}
            }, {
                itemId: 'user-group-filter',
                name: 'user-group-filter',
                cls: 'ametys',
                width: 400,
                emptyText: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_USERGROUP_FILTER}}",
                listeners: {change: Ext.Function.createBuffered(this._filterByUserOrGroup, 500, this)}
            }]
        }];
    },
    
    /**
     * @private
     * Filters the columns by right
     * @param {Ametys.form.widget.Right} field The right field
     */
    _filterByRight: function(field)
    {
        var rightId = field.getValue();
        if (Ext.isEmpty(rightId))
        {
            Ext.Array.forEach(this._assignmentsGrid.getColumns(), function(column, index) {
                column.setVisible(true);
            }, this);
        }
        else
        {
            // Computes the columns to let visible
            var matchingProfiles = [];
            Ext.Array.forEach(this._profiles, function(profile) {
                if (Ext.Array.contains(profile.rights, rightId))
                {
                    matchingProfiles.push(profile.id);
                }
            }, this);
            
            // Hide the others (except the first column)
            Ext.Array.forEach(this._assignmentsGrid.getColumns(), function(column, index) {
                var visible = index == 0 || Ext.Array.contains(matchingProfiles, column.dataIndex);
                column.setVisible(visible);
            }, this);
        }
    },
    
    /**
     * @private
     * Filters the records by their user login/label or group id/label
     * @param {Ext.form.field.Text} field The text field
     */
    _filterByUserOrGroup: function(field)
    {
        this._gridStore.clearFilter();
        
        var text = Ext.String.escapeRegex(field.getRawValue());
        if (text.length == 0)
        {
            return;
        }
        
        var fn = function(record, text)
        {
            var regExp = new RegExp('.*' + text + '.*', 'i');
            return regExp.test(record.get('login')) 
                    || regExp.test(record.get('groupId'))
                    || regExp.test(record.get('sortableLabel'));
        };
        
        this._gridStore.filterBy(Ext.bind(fn, this, [text], 1), this);
    },
    
    /**
     * @private
     * Clear the filters
     */
    _clearFilters: function()
    {
        this._gridStore.clearFilter(); // We cannot wait for the 'change' event to be fired after the #setValue("") because we created a 500ms buffer and it lead to bugs
        this._assignmentsGrid.down('#user-group-filter').setValue("");
    },

    /**
     * Function called when an assignment is clicked in order to change its value
     * @param {String} recordId The id of the record
     * @param {String} profileId The profile id (id of the column)
     * @param {String} value The current value
     */
    onCellClick: function(recordId, profileId, currentValue)
    {
        // Compute the new value
        var serverColumn = this._columns[profileId],
            newValue;
        
        if (currentValue == 'localAllow')
        {
            newValue = 'localDeny';
        }
        else if (currentValue == 'localDeny')
        {
            var serverValue = serverColumn[recordId]
            if (serverValue != 'localAllow' && serverValue != 'localDeny')
            {
                // it's a 'unknown' or 'inheritAllow' or 'inheritDeny' or 'disabled'
                newValue = serverValue;
            }
            else
            {
                newValue = 'unknown';
            }
        }
        else
        {
            newValue = 'localAllow';
        }
        
        // Change the clicked cell with this new value
        var dirty = newValue != serverColumn[recordId];
        this._gridStore.getById(recordId).set(profileId, newValue, {
            dirty: dirty
        });
        
        // Be sure to not have previously induced values for the entire column...
        this._getRecordsInStore().each(function(record) {
            var localRecordId = record.get('id'),
                serverValue = serverColumn[localRecordId],
                localValue = record.get(profileId);
            
            if (localValue == 'localInducedAllow' || localValue == 'localInducedDeny' || localValue == 'disabled')
            {
                this._gridStore.getById(localRecordId).set(profileId, serverValue, {dirty: false});
            }
        }, this);
        
        // ...but still compute with possible other local dirty assignments
        var columnWithLocalChanges = {}; // this object represents the column with local changes but without induced values
        this._getRecordsInStore().each(function(record) {
            if (record.get('id') != recordId)
            {
                columnWithLocalChanges[record.get('id')] = record.get(profileId);
            }
            else // the clicked cell
            {
                columnWithLocalChanges[recordId] = newValue;
            }
        }, this);
        
        // Computes and possibly change some cells to induced values on the same column
        this._computeInducedAssignments(profileId, columnWithLocalChanges, dirty ? recordId : null);
    },
    
    /**
     * Adds user records in the assignment grid.
     * @param {Object[]} users The users to add
     * @param {String} users.login The login of the user
     * @param {String} users.population The id of the population of the user
     * @param {String} users.populationName The label of the population of the user
     * @param {String} users.fullName The full name of the user
     */
    addUsers: function(users)
    {
        var recordsToAdd = [],
            count = users.length;
        
        function addInStore(groups, user)
        {
            recordsToAdd.push({
                assignmentType: this.self.ASSIGNMENT_TYPE_USERS,
                login: user.login,
                population: user.population,
                populationLabel: user.populationName,
                userSortableName: user.fullName,
                groups: groups
            });
            
            if (recordsToAdd.length == count)
            {
                this._gridStore.add(recordsToAdd);
                this._onStoreUpdated(this._getRecordsInStore().getRange());
            }
        }
        
        Ext.Array.forEach(users, function(user) {
            // Need to know the groups the user belongs to
            this.serverCall('getUserGroups', [user.login, user.population], Ext.bind(addInStore, this, [user], 1));
        }, this);
    },
    
    /**
     * Adds group records in the assignment grid.
     * @param {Object[]} groups The groups to add
     * @param {String groups.id The id of the group
     * @param {String groups.groupDirectory The id of the group directory of the group
     * @param {String groups.groupDirectoryName The label of the group directory of the group
     * @param {String groups.label The label of the group
     */
    addGroups: function(groups)
    {
        Ext.Array.forEach(groups, function(group) {
            this._gridStore.add({
                assignmentType: this.self.ASSIGNMENT_TYPE_GROUPS,
                groupId: group.id,
                groupDirectory: group.groupDirectory,
                groupDirectoryLabel: group.groupDirectoryName,
                groupLabel: group.label
            });
        }, this);
        
        this._onStoreUpdated(this._getRecordsInStore().getRange());
    },
    
    /**
     * Removes the given assignments
     * @param {Object[]} assignments The assignments to remove
     * @param {String} assignments.id The record id
     * @param {Object} assignments.context The context
     */
    removeAssignments: function(assignments)
    {
        var assignmentsInfo = [];
        Ext.Array.forEach(assignments, function(assignment) {
            var record = this._gridStore.getById(assignment.id);
            if (assignment.context == this._objectContext 
                && assignment != null
                && record.get('assignmentType') != this.self.ASSIGNMENT_TYPE_ANONYMOUS
                && record.get('assignmentType') != this.self.ASSIGNMENT_TYPE_ANYCONNECTEDUSER)
            {
                // Iterate through the profiles, only keep the ones with local assignments to avoid useless removal
                Ext.Array.forEach(this._profiles, function(profile) {
                    var profileId = profile.id,
                        currentAssignment = record.get(profileId);
                    if (currentAssignment == 'localAllow' || currentAssignment == 'localDeny')
                    {
                        var assignmentInfo = {
                            profileId: profileId,
                            assignment: ''
                        };
                        if (record.get('assignmentType') == this.self.ASSIGNMENT_TYPE_USERS)
                        {
                            assignmentInfo.assignmentType = "user";
                            assignmentInfo.identity = {
                                login: record.get('login'),
                                population: record.get('population')
                            };
                        }
                        else
                        {
                            assignmentInfo.assignmentType = "group";
                            assignmentInfo.identity = {
                                groupId: record.get('groupId'),
                                groupDirectory: record.get('groupDirectory')
                            };
                        }
                        assignmentsInfo.push(assignmentInfo);
                    }
                }, this);
            }
        }, this);
        
        if (assignmentsInfo.length > 0)
        {
            var parameters = [this._contextCombobox.getValue(), this._objectContext, assignmentsInfo];
            this.serverCall('saveChanges', parameters, this._updateGrid);
        }
    },
    
    /**
     * Saves the changes made in the grid
     */
    saveChanges: function()
    {
        this._saveChanges(this._contextCombobox.getValue(), this._updateGrid);
    },

    /**
     * @private
     * Computes the assignments and make a server call to save changes.
     * @param {String} rightAssignmentId The id of the right assignment context
     * @param {Function} [callback] The callback function to call when the changes are saved.
     */
    _saveChanges: function(rightAssignmentId, callback)
    {
        var assignmentsInfo = [];
        Ext.Array.forEach(this._gridStore.getModifiedRecords(), function(record) {
            Ext.Object.each(record.modified, function(profileId) {
                var assignmentInfo = {
                    profileId: profileId,
                    assignment: record.get(profileId) // 'localAllow', 'localDeny' or others
                };
                switch (record.get('assignmentType')) {
                    case this.self.ASSIGNMENT_TYPE_ANONYMOUS:
                        assignmentInfo.assignmentType = "anonymous";
                        break;
                    case this.self.ASSIGNMENT_TYPE_ANYCONNECTEDUSER:
                        assignmentInfo.assignmentType = "anyConnectedUser";
                        break;
                    case this.self.ASSIGNMENT_TYPE_USERS:
                        assignmentInfo.assignmentType = "user";
                        assignmentInfo.identity = {
                            login: record.get('login'),
                            population: record.get('population')
                        };
                        break;
                    case this.self.ASSIGNMENT_TYPE_GROUPS:
                    default:
                        assignmentInfo.assignmentType = "group";
                        assignmentInfo.identity = {
                            groupId: record.get('groupId'),
                            groupDirectory: record.get('groupDirectory')
                        };
                        break;
                }
                assignmentsInfo.push(assignmentInfo);
            }, this);
        }, this);
        
        if (assignmentsInfo.length > 0)
        {
            var parameters = [rightAssignmentId, this._objectContext, assignmentsInfo];
            this.serverCall('saveChanges', parameters, callback);
        }
    },
    
    /**
     * @private
     * Function called before loading the grid store
     * @param {Ext.data.Store} store The grid store
     * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
     */
    _onBeforeLoadGrid: function(store, operation)
    {
        operation.setParams(Ext.apply(operation.getParams() || {}, {
            rightAssignmentContextId: this._contextCombobox.getValue(),
            context: this._objectContext,
            parentContexts: this._parentObjectContexts
        }));
    },
    
    /**
     * @private
     * Listener when a Model instance of the grid store has been updated, or when the grid store is loaded
     * @param {Ext.data.Store} store The store
     */
    _onUpdateOrLoadGrid: function(store)
    {
        // store.getModifiedRecords().length > 0 is not sufficient as when adding a record, there is no dirty cell but the record is returned in this array anyway
        var dirty = false;
        Ext.Array.each(store.getModifiedRecords(), function(record) {
            if (!Ext.Object.isEmpty(record.modified))
            {
                dirty = true;
                return false;
            }
        }, this);
        this.setDirty(dirty);
    },
    
    /**
     * @private
     * Listener when the value of the context combobox changes.
     * @param {Ext.form.field.ComboBox} combo The combobox
     * @param {Object} newValue The new value (the selected context id)
     * @param {Object} oldValue The old value
     */
    _onComboboxChange: function(combo, newValue, oldValue)
    {
        if (this.isDirty())
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CHANGE_CONTEXT_BOX_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CHANGE_CONTEXT_BOX_MESSAGE}}",
                buttons: Ext.Msg.YESNO,
                icon: Ext.MessageBox.QUESTION,
                fn: Ext.bind(callback, this, [combo, newValue, oldValue], 1)
            });
        }
        else
        {
            this._changeContextPanel(newValue);
        }
        
        function callback(btn, combo, newValue, oldValue)
        {
            // Force dirty state to false to avoid a second dialog box if event 'objectcontextchange' is fired too soon
            this.setDirty(false);
            
            if (btn == 'yes')
            {
                this._saveChanges(oldValue, Ext.bind(this._changeContextPanel, this, [newValue]));
            }
            else
            {
                this._changeContextPanel(newValue);
            }
        }
    },
    
    /**
     * @private
     * Changes the active context panel
     * @param {String} rightAssignmentContextId The id of the right assignment context to display
     */
    _changeContextPanel: function(rightAssignmentContextId)
    {
        // Clear filters
        this._clearFilters();
        
        // Change the current panel displayed in the context panel
        this._contextPanel.getLayout().setActiveItem(this._contextComponents[rightAssignmentContextId]);
        
        // Call its initialize() method
        var className = this._classNames[rightAssignmentContextId];
        eval(className + '.initialize()');
    },
    
    /**
     * @private
     * Updates the grid cells from the context.
     */
    _updateGrid: function()
    {
        this._gridStore.load({
            callback: this._onStoreUpdated,
            scope: this
        });
    },
    
    /**
     * @private
     * This function has to be called after the store is modified 
     * For instance, callback of the store loading, or after adding a new user record.
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry[]} records The records of the grid
     */
    _onStoreUpdated: function(records)
    {
        this._updateColumnsObject(records);
        // Now update the records
        Ext.Object.each(this._columns, function(profileId, column) {
            this._computeInducedAssignments(profileId, column, null);
        }, this);
        
        // Clear the selection
        this._assignmentsGrid.getSelectionModel().deselectAll();
    },
    
    /**
     * @private
     * Create a columns object representing the columns
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry[]} records The records read from the server
     */
    _updateColumnsObject: function(records)
    {
        this._columns = {};
        Ext.Array.forEach(this._profiles, function(profile) {
            this._columns[profile.id] = {};
        }, this);
        
        Ext.Array.forEach(records, function(record) {
            Ext.Object.each(this._columns, function(profileId, column) {
                if (record.get(profileId) != null)
                {
                    column[record.get('id')] = record.get(profileId);
                }
                else
                {
                    column[record.get('id')] = 'unknown';
                }
            }, this)
        }, this);
    },
    
    /**
     * @private
     * Computes the induces assignments on a column.
     * @param {String} profileId The id of the column (the id of the profile)
     * @param {Object} column The column
     * @param {String} [dirtyRecordId] The id of the record which has to be set with the 'dirty' option to true. Other records will be set with 'dirty' at false'. 
     * Typically when one cell is currently modified and need to be visually marked. Can be null
     */
    _computeInducedAssignments: function(profileId, column, dirtyRecordId)
    {
        // When iterating, we need to be sure anonymous and anyconnected are the two first records (for then computing the others)
        // and we need to be sure group records come before all user records, as user records can be computed from their groups values
        
        var groups = {},
            users = {};
        
        Ext.Object.each(column, function(recordId, assignment) {
            var dirty = recordId === dirtyRecordId,
                type = this._gridStore.getById(recordId).get('assignmentType');
            switch (type) {
                case this.self.ASSIGNMENT_TYPE_ANONYMOUS:
                    this._checkDenyInducedByAnyConnected(this._getRecordsInStore(), profileId, column, recordId, assignment, dirty);
                    break;
                case this.self.ASSIGNMENT_TYPE_ANYCONNECTEDUSER:
                    this._checkAllowInducedByAnonymous(this._getRecordsInStore(), profileId, column, recordId, assignment, dirty);
                    break;
                case this.self.ASSIGNMENT_TYPE_GROUPS:
                    groups[recordId] = assignment;
                    break;
                case this.self.ASSIGNMENT_TYPE_USERS:
                    users[recordId] = assignment;
                    break;
                default:
                    break;
            }
        }, this);
        
        Ext.Object.each(groups, function(recordId, assignment) {
            var dirty = recordId === dirtyRecordId;
            this._checkInducedByAnyConnectedAndAnonymous(this._getRecordsInStore(), profileId, column, recordId, assignment, dirty);
        }, this);
        
        Ext.Object.each(users, function(recordId, assignment) {
            var dirty = recordId === dirtyRecordId;
            var wasChanged = this._checkInducedByGroups(this._getRecordsInStore(), profileId, column, recordId, assignment, dirty);
            if (!wasChanged)
            {
                this._checkInducedByAnyConnectedAndAnonymous(this._getRecordsInStore(), profileId, column, recordId, assignment, dirty);
            }
        }, this);
    },
    
    /**
     * @private
     * Checks if there are 'disabled' assignments from anonymous and updates the record if so
     * @param {Ext.util.Collection} allRecords The records
     * @param {String} profileId The id of the profile
     * @param {Object} column The object representing the column
     * @param {String} recordId The id of the record
     * @param {String} assignment The assignment of the cell
     * @param {Boolean} dirty true if the update is dirty
     */
    _checkAllowInducedByAnonymous: function(allRecords, profileId, column, recordId, assignment, dirty)
    {
        if (assignment != 'localAllow' && assignment != 'localDeny')
        {
            // Check anonymous value
            var anonymousRecordId = this._getAnonymousRecord(allRecords).get('id');
            if (column[anonymousRecordId] == 'localAllow' || column[anonymousRecordId] == 'inheritAllow')
            {
                allRecords.get(recordId).set(profileId, 'disabled', {dirty: dirty})
            }
        }
    },
    
    /**
     * @private
     * Gets the anonymous record
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry[]} records The records
     * @return {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} the anonymous record
     */
    _getAnonymousRecord: function(records)
    {
        var result;
        records.each(function(record) {
            if (record.get('assignmentType') == this.self.ASSIGNMENT_TYPE_ANONYMOUS)
            {
                result = record;
                return false;
            }
        }, this);
        
        return result;
    },
    
    /**
     * @private
     * Checks if there are 'localInducedDeny' assignments from anyconnected and updates the record if so
     * @param {Ext.util.Collection} allRecords The records
     * @param {String} profileId The id of the profile
     * @param {Object} column The object representing the column
     * @param {String} recordId The id of the record
     * @param {String} assignment The assignment of the cell
     * @param {Boolean} dirty true if the update is dirty
     */
    _checkDenyInducedByAnyConnected: function(allRecords, profileId, column, recordId, assignment, dirty)
    {
        if (assignment != 'localAllow' && assignment != 'localDeny')
        {
            // Check anyconnected value
            var anyconnectedRecordId = this._getAnyConnectedRecord(allRecords).get('id');
            if (column[anyconnectedRecordId] == 'localDeny')
            {
                allRecords.get(recordId).set(profileId, 'localInducedDeny', {dirty: dirty})
            }
        }
    },
    
    /**
     * @private
     * Gets the anyconnected record
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry[]} records The records
     * @return {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} the anyconnected record
     */
    _getAnyConnectedRecord: function(records)
    {
        var result;
        records.each(function(record) {
            if (record.get('assignmentType') == this.self.ASSIGNMENT_TYPE_ANYCONNECTEDUSER)
            {
                result = record;
                return false;
            }
        }, this);
        
        return result;
    },
    
    /**
     * @private
     * Checks if there are induced assignments from anyconnected and anonymous and updates the record if so
     * @param {Ext.util.Collection} allRecords The records
     * @param {String} profileId The id of the profile
     * @param {Object} column The object representing the column
     * @param {String} recordId The id of the record
     * @param {String} assignment The assignment of the cell
     * @param {Boolean} dirty true if the update is dirty
     */
    _checkInducedByAnyConnectedAndAnonymous: function(allRecords, profileId, column, recordId, assignment, dirty)
    {
        if (assignment != 'localAllow' && assignment != 'localDeny')
        {
            // Check anyconnected value, then anonymous value
            var anyconnectedRecordId = this._getAnyConnectedRecord(allRecords).get('id');
            var anonymousRecordId = this._getAnonymousRecord(allRecords).get('id');
            
            if (column[anonymousRecordId] == 'localAllow' || column[anonymousRecordId] == 'inheritAllow')
            {
                allRecords.get(recordId).set(profileId, 'disabled', {dirty: dirty})
            }
            else if (column[anyconnectedRecordId] == 'localDeny')
            {
                allRecords.get(recordId).set(profileId, 'localInducedDeny', {dirty: dirty})
            }
//            else if (column[anyconnectedRecordId] == 'inheritDeny')
//            {
//                allRecords.get(recordId).set(profileId, 'inheritDeny', {dirty: dirty})
//            }
            else if (column[anyconnectedRecordId] == 'localAllow')
            {
                allRecords.get(recordId).set(profileId, 'localInducedAllow', {dirty: dirty})
            }
//            else if (column[anyconnectedRecordId] == 'inheritAllow')
//            {
//                allRecords.get(recordId).set(profileId, 'inheritAllow', {dirty: dirty})
//            }
        }
    },
    
    /**
     * @private
     * Checks if there are induced assignments for a user from its groups 
     * @param {Ext.util.Collection} allRecords The records
     * @param {String} profileId The id of the profile
     * @param {Object} column The object representing the column
     * @param {String} recordId The id of the record
     * @param {String} assignment The assignment of the cell
     * @param {Boolean} dirty true if the update is dirty
     */
    _checkInducedByGroups: function(allRecords, profileId, column, recordId, assignment, dirty)
    {
        var isAllowed = false,
            isDenied = false;;
        
        if (assignment != 'localAllow' && assignment != 'localDeny')
        {
            Ext.Array.forEach(allRecords.get(recordId).get('groups'), function(group) {
                var groupRecord = this._getGroupRecord(group.groupId, group.groupDirectory, allRecords);
                if (groupRecord != null)
                {
                    var groupRecordId = groupRecord.get('id'); 
                    if (column[groupRecordId] == 'localDeny')
                    {
                        // at least one denied group
                        allRecords.get(recordId).set(profileId, 'localInducedDeny', {dirty: dirty});
                        isDenied = true;
                    }
                    else if (column[groupRecordId] == 'localAllow')
                    {
                        isAllowed = true;
                    }
                }
            }, this);
            
        }
        
        if (isAllowed)
        {
            allRecords.get(recordId).set(profileId, 'localInducedAllow', {dirty: dirty});
        }
        return isDenied || isAllowed;
    },
    
    /**
     * @private
     * Gets the record of the corresponding group 
     * @param {String} groupId the id of the group
     * @param {String} groupDirectory the id of the group directory
     * @param {Ext.util.Collection} allRecords  the records
     * @return {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} the record of the corresponding group. Can be null
     */
    _getGroupRecord: function(groupId, groupDirectory, allRecords)
    {
        var result = null;
        allRecords.each(function(record) {
            if (record.get('groupId') == groupId && record.get('groupDirectory') == groupDirectory)
            {
                // found, stop iteration
                result = record;
                return false;
            }
        }, this);
        
        return result;
    },
        
    
    /**
     * @private
     * Gets the initial columns
     * @return {Object[]} The configuration of the columns
     */
    _getInitialColumns: function()
    {
        return [{stateId: 'grid-first-column', text: "", dataIndex: "sortableLabel", minWidth: 300, hideable: false, sortable: true, renderer: Ext.bind(this._renderWho, this)}];
    },
    
    setParams: function(params)
    {
        this.callParent(arguments);
        this._objectContext = null;
        this._parentObjectContexts = [];
        this.serverCall('getJSClassNames', [], this._createContextPanels);
        
        this.showOutOfDate();
    },
    
    close: function(manual)
    {
        if (this.isDirty())
        {
            Ametys.form.SaveHelper.promptBeforeQuit("{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CLOSE_BOX_TITLE}}", 
                                                    "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_CLOSE_BOX_MESSAGE}}",
                                                    null,
                                                    Ext.bind(this._closeCb, this));
        }
        else
        {
            this.callParent(arguments);
        }
    },
    
    /**
     * @private
     * Callback function after the user clicked on one of the three choices in the "Prompt before quit" dialog box
     * @param {Boolean} doSave true means the user want to save. false means the user does not want to save. null means the user does not want to save nor quit.
     */
    _closeCb: function(doSave)
    {
        if (doSave === true)
        {
            this._saveChanges(this._contextCombobox.getValue(), Ext.bind(this._closeWithoutPrompt, this));
        }
        else if (doSave === false)
        {
            this._closeWithoutPrompt();
        }
    },
    
    /**
     * @private
     * Calls the close method on superclass
     */
    _closeWithoutPrompt: function()
    {
        Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.superclass.close.call(this);
    },
    
    /**
     * @private
     * Creates the context panels
     * @param {Object} classNames The names of right assignment context classes
     */
    _createContextPanels: function(classNames)
    {
        this._classNames = classNames;
        this._contextComponents = {};
        Ext.Object.each(classNames, function(contextId, className) {
            var cmp = eval(className + '.getComponent()');
            this._contextComponents[contextId] = cmp;
            // Add the component in the context panel (which has a card layout)
            this._contextPanel.add(cmp);
            // Give the reference to the context panel
            eval(className + '.setContextPanel(this._contextPanel)');
        }, this);
    },
    
    refresh: function()
    {
        this.showRefreshing();
        
        // First, retrieve the profiles to reconfigure the grid panel (every profile is a column)
        Ametys.data.ServerComm.send({
            plugin: 'core',
            url: 'rights/profiles.json',
            parameters: {
                limit: null
            },
            priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
            callback: {
                handler: this._getProfilesCb,
                scope: this,
                arguments: []
            },
            errorMessage: true,
            waitMessage: false,
            responseType: 'text'
        });
    },
    
    /**
     * @private
     * Callback function after retrieving the profiles
     * @param {Object} response The server response
     */
    _getProfilesCb: function(response)
    {
        // The server returned a string json, decode it into an array of object
        var stringData = response.firstChild.textContent;
        var data = Ext.decode(stringData, true);
        
        // Add the columns in the grid
        this._addColumns(data.profiles);
        
        // Load the contexts store
        this._contextCombobox.getStore().load({callback: function(records) {
            var rightAssignmentContextIds = Ext.Array.map(records, function(record) {return record.get('value');}),
                contributorContext = "org.ametys.plugins.core.impl.right.ContributorRightAssignmentContext";
            
            if (records.length > 0 && Ext.Array.contains(rightAssignmentContextIds, contributorContext))
            {
                // By default select the contributor context
                this._contextCombobox.select(contributorContext);
            }
            else if (records.length > 0)
            {
                // In case contributor context was not found, select first option
                this._contextCombobox.select(records[0].get('value'));
            }
            this._refreshCb();
        }, scope: this});
    },
    
    /**
     * @private
     * Updates the model and the columns with the retrieved profiles
     * @param {Object[]} profiles The profiles
     */
    _addColumns: function(profiles)
    {
        this._profiles = profiles;
        var newFields = Ext.Array.map(profiles, function(profile) {
            return {name: profile.id};
        }, this);
        
        var newColumns = [];
        Ext.Array.forEach(profiles, function(profile) {
            var tdCls = profile.id == this.self.READER_PROFILE_ID ? 'a-grid-cell-reader' : ''; 
            newColumns.push({
                stateId: 'grid-profile-' + profile.id,
                text: profile.label,
                dataIndex: profile.id,
                hideable: profile.id != this.self.READER_PROFILE_ID,
                sortable: false,
                align: 'center',
                tdCls: tdCls,
                renderer: this._renderAssignment
            });
        }, this);
        
        // All columns are the initial ones and then all the profiles alphabetically sorted
        var me = this;
        var allColumns = this._getInitialColumns().concat(newColumns.sort(function(a, b) {
            if (a.dataIndex == me.self.READER_PROFILE_ID)
            {
                return -1;
            }
            else if (b.dataIndex == me.self.READER_PROFILE_ID)
            {
                return 1;
            }
            else
            {
                return Ext.data.SortTypes.asNonAccentedUCString(a.text) < Ext.data.SortTypes.asNonAccentedUCString(b.text) ? -1 : 1;
            }
        }));
        
        this._gridStore.getModel().addFields(newFields);
        this._assignmentsGrid.reconfigure(allColumns);
    },
    
    /**
     * @private
     * Called when the refresh process is over.
     */
    _refreshCb: function()
    {
        this.showRefreshed();
        this.showUpToDate();
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_ACTIVE;
    },
    
    sendCurrentSelection: function()
    {
        var me = this;
        
        function hasLocalAssignments(record)
        {
            var result = false;
            Ext.Array.each(me._profiles, function(profile) {
                var profileId = profile.id,
                    assignment = record.get(profileId);
                if (assignment == 'localAllow' || assignment == 'localDeny')
                {
                    result = true;
                    return false;
                }
            }, me);
            
            return result;
        }
        
        var selection = this._assignmentsGrid.getSelection();
        
        var targets = Ext.Array.map(selection, function(record) {
            var type = record.get('assignmentType'),
                removable = (type == this.self.ASSIGNMENT_TYPE_USERS || type == this.self.ASSIGNMENT_TYPE_GROUPS)
                            && hasLocalAssignments(record);
            return {
                id: Ametys.message.MessageTarget.PROFILE_ASSIGNMENT,
                parameters: {
                    id: record.get('id'),
                    type: type,
                    context: this._objectContext,
                    removable: removable
                }
            };
        }, this);
        
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.SELECTION_CHANGED,
            targets: targets
        });
    },
    
    /**
     * @private
     * Renderer for the first column, depending on the type of assignement
     * @param {Object} value The data value (the user sortable name or the group label)
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} record The record
     * @return {String} The html representation
     */
    _renderWho: function(value, metaData, record)
    {
        var type = record.get('assignmentType');
        switch (type) {
            case this.self.ASSIGNMENT_TYPE_ANONYMOUS:
                var text = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_ASSIGNMENT_TYPE_ANONYMOUS}}";
                return '<span class="ametysicon-carnival23"></span> ' + text;
            case this.self.ASSIGNMENT_TYPE_ANYCONNECTEDUSER:
                var text = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_ASSIGNMENT_TYPE_ANYCONNECTEDUSERS}}";
                return '<span class="ametysicon-key162"></span> ' + text;
            case this.self.ASSIGNMENT_TYPE_USERS:
                var text = Ametys.plugins.core.users.UsersDAO.renderUser(record.get('login'), record.get('populationLabel'), value);
                return '<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/' + record.get('population') + '/' + record.get('login') + '/image_16" class="a-grid-icon a-grid-icon-user"/>' + text;
            case this.self.ASSIGNMENT_TYPE_GROUPS:
                var text = value + ' (' + record.get('groupId') + ', ' + record.get('groupDirectoryLabel') + ')';
                return '<span class="ametysicon-multiple25"></span> ' + text;
            default:
                return value;
        }
    },
    
    /**
     * @private
     * Renderer for the assignment cells, which draws a clickable icon representing the assignement
     * @param {Object} value The data value for the current cell
     * @param {Object} metaData A collection of metadata about the current cell
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} record The record for the current row
     * @return {String} The HTML string to be rendered
     */
    _renderAssignment: function(value, metaData, record)
    {
        var me = Ametys.plugins.coreui.profiles.ProfileAssignmentsTool;
        
        var img, color, tooltip;
        
        switch (value) 
        {
            case "disabled":
                img = "check";
                color = "lightgreen";
                tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_DISABLED}}";
                break;
            case "localAllow":
                img = "check-1";
                color = "green";
                tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_LOCAL_ALLOW}}"
                break;
            case "localInducedAllow":
                img = "check";
                color = "green";
                if (record.get('assignmentType') == me.ASSIGNMENT_TYPE_USERS)
                {
                    tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_LOCAL_INDUCED_ALLOW_USER}}"
                }
                else
                {
                    tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_LOCAL_INDUCED_ALLOW_OTHER}}"
                }
                break;
            case "inheritAllow":
                img = "check";
                color = "black";
                tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_INHERIT_ALLOW}}"
                break;
                
            case "localDeny":
                img = "cross-1";
                color = "red";
                tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_LOCAL_DENY}}"
                break;
            case "localInducedDeny":
                img = "cross"; 
                color = "red";
                if (record.get('assignmentType') == me.ASSIGNMENT_TYPE_USERS)
                {
                    tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_LOCAL_INDUCED_DENY_USER}}"
                }
                else
                {
                    tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_LOCAL_INDUCED_DENY_OTHER}}"
                }
                break;
            case "inheritDeny":
                img = "cross"; 
                color = "black";
                tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_INHERIT_DENY}}"
                break;
                
            case "unknown":
            default:
                img = "circle";
                color = "black";
                tooltip = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_UNKNOWN}}"
        }
        
        metaData.tdAttr = 'data-qtip="' + tooltip + '"';
        
        var onclickFn = "Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.onCellClick('" + record.get('id') + "', '" + metaData.column.dataIndex + "', '" + value + "')";
        
        if (value == "disabled")
        {
            return '<span class="a-grid-glyph a-grid-assignment-glyph ametysicon-' + img
                    + ' a-grid-glyph-' + color
                    + '" />';
        }
        else
        {
            return '<a class="a-grid-glyph a-grid-assignment-glyph ametysicon-' + img
                    + ' a-grid-glyph-' + color
                    + '" href="javascript:void(0)" onclick="' 
                    + onclickFn + '" />';
        }
    },
    
    /**
     * @private
     * Gets all records in the assignment grid store, even the unfiltered items if there is a filter in this store.
     * @return {Ext.util.Collection}
     */
    _getRecordsInStore: function()
    {
        var collection = this._gridStore.getData(),
            unfilteredItems = collection.getSource();
        if (unfilteredItems != null)
        {
            // there is a filter
            return unfilteredItems;
        }
        else
        {
            return collection;
        }
    }
});

 /**
 * This class is the data model for profile assignment grid entries.
 * @private
 */
Ext.define('Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry', {
    extend: 'Ext.data.Model',
    
    fields: [
        /* For user entries */
        {name: 'login'},
        {name: 'population'},
        {name: 'populationLabel'},
        {name: 'userSortableName'},
        {name: 'groups'},
        
        /* For group entries */
        {name: 'groupId'},
        {name: 'groupDirectory'},
        {name: 'groupDirectoryLabel'},
        {name: 'groupLabel'},
        
        /* For grouping feature */
        {name: 'assignmentType'},
        
        /* For sorting */
        {
            name: 'sortableLabel',
            type: 'string',
            sortType: Ext.data.SortTypes.asNonAccentedUCString,
            
            convert: function(value, record) // using convert and not calculate because for an unknown reason, it doesn't work when closing and reopening the tool
            {
                if (record.get('userSortableName') != null)
                {
                    return record.get('userSortableName'); 
                }
                else if (record.get('groupLabel') != null)
                {
                    return record.get('groupLabel');
                }
                return "";
            }
        }
    ]
});

Ext.define("Ametys.message.ProfileAssignmentMessageTarget",{
    override: "Ametys.message.MessageTarget",
    
    statics: 
    {
        /**
         * @member Ametys.message.MessageTarget
         * @readonly
         * @property {String} PROFILE_ASSIGNMENT The target of the message is a profile assignment
         * @property {String} PROFILE_ASSIGNMENT.id The id of the record
         * @property {String} PROFILE_ASSIGNMENT.type The type of assignment
         * @property {Object} PROFILE_ASSIGNMENT.context The object context of the assignment
         * @property {Boolean} PROFILE_ASSIGNMENT.removable true if the record is removable
         */
        PROFILE_ASSIGNMENT: "profileAssignment"
    }
});

