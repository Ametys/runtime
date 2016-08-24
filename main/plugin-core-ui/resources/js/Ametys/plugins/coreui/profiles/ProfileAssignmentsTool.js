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
         * @property {String} TARGET_TYPE_ANONYMOUS The record is an assignment for an anonymous user
         */
        TARGET_TYPE_ANONYMOUS: 'anonymous',
        /**
         * @readonly
         * @property {String} TARGET_TYPE_ANYCONNECTEDUSER The record is an assignment for any connected user
         */
        TARGET_TYPE_ANYCONNECTEDUSER: 'anyconnected_user',
        /**
         * @readonly
         * @property {String} TARGET_TYPE_USER The record is an assignment for a user
         */
        TARGET_TYPE_USER: 'user',
        /**
         * @readonly
         * @property {String} TARGET_TYPE_GROUP The record is an assignment for a group
         */
        TARGET_TYPE_GROUP: 'group',
        
        /**
         * @readonly
         * @property {String} READER_PROFILE_ID The id of the special profile (READER profile)
         */
        READER_PROFILE_ID: 'READER',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_ALLOW Type of access for an allowed access
         */
        ACCESS_TYPE_ALLOW: 'allow',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_DENY Type of access for an denied access
         */
        ACCESS_TYPE_DENY: 'deny',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_INHERITED_ALLOW Type of access for an allowed access by inheritance
         */
        ACCESS_TYPE_INHERITED_ALLOW: 'inherited_allow',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_DENIED Type of access for an denied access by inheritance
         */
        ACCESS_TYPE_INHERITED_DENY: 'inherited_deny',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_ALLOW_BY_GROUP Type of access for an allowed access through groups (client-side only)
         */
        ACCESS_TYPE_ALLOW_BY_GROUP: 'allow_by_group',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_DENY_BY_GROUP Type of access for a denied access through groups (client-side only)
         */
        ACCESS_TYPE_DENY_BY_GROUP: 'deny_by_group',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_ALLOW_BY_ANONYMOUS Type of access for an allowed access through anonymous (client-side only)
         */
        ACCESS_TYPE_ALLOW_BY_ANONYMOUS: 'allow_by_anonymous',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_ALLOW_BY_ANYCONNECTED Type of access for an allowed access through any connected user (client-side only)
         */
        ACCESS_TYPE_ALLOW_BY_ANYCONNECTED: 'allow_by_anyconnected',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_DENY_BY_ANYCONNECTED Type of access for a denied access through any connected user (client-side only)
         */
        ACCESS_TYPE_DENY_BY_ANYCONNECTED: 'deny_by_anyconnected',
        
        /**
         * @readonly
         * @property {String} ACCESS_TYPE_UNKNOWN Type of access for a undetermined access
         */
        ACCESS_TYPE_UNKNOWN: 'unknown',
        
        /**
         * Function called when an assignment is clicked in order to change its value
         * @param {String} recordId The id of the record
         * @param {String} profileId The profile id (id of the column)
         */
        onCellClick: function(recordId, profileId)
        {
            var tool = Ametys.tool.ToolsManager.getTool("uitool-profile-assignment");
            if (tool != null)
            {
                tool.onCellClick(recordId, profileId);
            }
        },
        
        /**
         * Compute the tooltip for a given record and access type
         * @param {Ext.data.Model} record The record
         * @param {String} The access type
         * @return The tooltip text
         */
        computeTooltip: function (record, accessType)
        {
        	var type = record.get('targetType');
        	
        	if (type == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANONYMOUS)
        	{
        		return this._computeTooltipForAnonymous(record, accessType);
        	}
        	else if (type == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANYCONNECTEDUSER)
        	{
        		return this._computeTooltipForAnyconnectedUser(record, accessType);
        	}
        	else if (type == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_USER)
        	{
        		return this._computeTooltipForUser(record, accessType);
        	}
        	else if (type == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_GROUP)
        	{
        		return this._computeTooltipForGroup(record, accessType);
        	}
        },
        
        /**
         * @private
         * Compute the tooltip for the Anonymous record and access type
         * @param {Ext.data.Model} record The Anonymous record
         * @param {String} The access type
         * @return The tooltip text
         */
        _computeTooltipForAnonymous: function (record, accessType)
        {
        	switch (accessType) 
            {
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW:
                	// Anonymous is locally allowed 
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANONYMOUS_LOCAL_ALLOWED}}";
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW:
                	// Anonymous is allowed by inheritance
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANONYMOUS_INHERIT_ALLOWED}}";
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY:
                	// Anonymous is locally denied 
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANONYMOUS_LOCAL_DENIED}}";
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY:
                	// Anonymous is denied by inheritance
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANONYMOUS_INHERIT_DENIED}}";
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN:
                default:
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_UNKNOWN}}";
            }
        },
        
        /**
         * @private
         * Compute the tooltip for the anyconnected user record and access type
         * @param {Ext.data.Model} record The anyconnected user record
         * @param {String} The access type
         * @return The tooltip text
         */
        _computeTooltipForAnyconnectedUser: function (record, accessType)
        {
        	switch (accessType) 
            {
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS:
            		// Any connected users are allowed because Anonymous is allowed
            		return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANYCONNECTED_DISABLED}}";
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW:
                	// Any connected users are locally allowed 
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANYCONNECTED_LOCAL_ALLOWED}}";
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW:
                	// Any connected users are allowed by inheritance
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANYCONNECTED_INHERIT_ALLOWED}}";
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY:
                	// Any connected users are locally denied 
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANYCONNECTED_LOCAL_DENIED}}";
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY:
                	// Any connected users are denied by inheritance
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_ANYCONNECTED_INHERIT_DENIED}}";
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN:
                default:
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_UNKNOWN}}";
            }
        },
        
        /**
         * @private
         * Compute the tooltip for a user record and access type
         * @param {Ext.data.Model} record The user record
         * @param {String} The access type
         * @return The tooltip text
         */
        _computeTooltipForUser: function (record, accessType)
        {
        	switch (accessType) 
            {
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS:
        			// The user is allowed because Anonymous is allowed
                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_ALLOWED_BY_ANONYMOUS}}";
                    
            	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW:
                	// The user is locally allowed 
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_LOCAL_ALLOWED}}";
                	
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_GROUP:
                	// The user is allowed because he belongs to a allowed group locally
                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_ALLOWED_BY_GROUP}}";
                    
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANYCONNECTED:
                	// The user is allowed because any connected users are allowed
                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_ALLOWED_BY_ANYCONNECTED}}";
                    
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW:
                	// The user is allowed by inheritance
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_INHERIT_ALLOWED}}";
                	
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY:
                	// The user is denied 
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_LOCAL_DENIED}}";
                	
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_GROUP:
                	// The user is denied because he belongs to a denied group locally
                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_DENIED_BY_GROUP}}";
                    
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_ANYCONNECTED:
                	// The user is denied because any connected users are denied
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_DENIED_BY_ANYCONNECTED}}";
                	
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY:
                	// The user is denied by inheritance
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_USER_INHERIT_DENIED}}";
                	
                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN:
                default:
                	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_UNKNOWN}}";
            }
        },
        
        /**
         * @private
         * Compute the tooltip for a group record and access type
         * @param {Ext.data.Model} record The group record
         * @param {String} The access type
         * @return The tooltip text
         */
        _computeTooltipForGroup: function (record, accessType)
        {
        	switch (accessType) 
            {
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS:
	    			// The group is allowed because Anonymous is allowed
	                return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_ALLOWED_BY_ANONYMOUS}}";
	                
	        	case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW:
	            	// The group is locally allowed 
	            	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_LOCAL_ALLOWED}}";
	            	
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANYCONNECTED:
	            	// The group is allowed because any connected users are allowed
	                return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_ALLOWED_BY_ANYCONNECTED}}";
	                
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW:
	            	// The group is allowed by inheritance
	            	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_INHERIT_ALLOWED}}";
	            	
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY:
	            	// The group is denied 
	            	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_LOCAL_DENIED}}";
	            	
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_ANYCONNECTED:
	            	// The group is denied because any connected users are denied
	            	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_DENIED_BY_ANYCONNECTED}}";
	            	
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY:
	            	// The group is denied by inheritance
	            	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_GROUP_INHERIT_DENIED}}";
	            	
	            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN:
	            default:
	            	return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TOOLTIP_UNKNOWN}}";
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
     * @property {Object[]} _profiles The profiles of the application.
     * @property {String} _profiles.id The id of the profile
     * @property {String} _profiles.label The label of the profile
     * @property {String[]} _profiles.rights The ids of the rights this profile contains
     */
    _profiles: [],
    
    /**
     * @private
     * @property {Object} _storedValues The stored assignments by profile (server-side) 
     * The key is the profile id, the value is an object (where its key is the record id, the value is the server-side value (with no induced local changes)
     */
    
    createPanel: function()
    {
        this._contextCombobox = Ext.create('Ext.form.field.ComboBox', this._getContextComboCfg());
        
        this._contextPanel = Ext.create('Ext.panel.Panel', {
            width: 500,
            minWidth: 340,
            scrollable: true,
            layout: 'card',
            cls: 'context-panel',
            
            dockedItems: [{
                xtype: 'toolbar',
                cls: 'context-toolbar',
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
            grouper: {
            	property: 'targetType',
            	direction: 'ASC',
            	transform: function (value)
            	{
            		// This is done to order the target types in the grid
            		switch (type) {
	                    case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANONYMOUS:
	                        return 0;
	                    case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANYCONNECTEDUSER:
	                        return 1;
	                    case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_USER:
	                        return 2;
	                    case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_GROUP:
	                        return 3;
	                    default:
	                        return 4;
            		}
            	}
            },
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
                    '{name:this.formatTargetType}', 
                    {
                    	formatTargetType: Ext.bind(function(type) {
                            switch (type) {
                                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANONYMOUS:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TARGET_TYPE_ANONYMOUS}}";
                                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANYCONNECTEDUSER:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TARGET_TYPE_ANYCONNECTEDUSERS}}";
                                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_USER:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TARGET_TYPE_USERS}}";
                                case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_GROUP:
                                    return "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TARGET_TYPE_GROUPS}}";
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
            items: [
                this._contextPanel, 
                {
                	xtype: 'container',
                	itemId: 'right-card-container',
	            	layout: 'card',
	    			activeItem: 0,
	    			split: true,
	    			flex: 3,
	            	items: [{
							xtype: 'component',
							cls: 'a-panel-text-empty',
							border: false,
							html: "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_NO_OBJECT_CONTEXT}}"
						}, 
						this._assignmentsGrid
					]
                }
            ]
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
            
            labelAlign: 'top',
            labelSeparator: '',
            flex: 1
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
    _internalChangeObjectContext: function(object, hintTextContext)
    {
        this._clearFilters(); // avoid bugs in the grid store before loading it
        
        this.getLogger().info("Right assignment context has changed to : " + object);
        this._objectContext = object;
        
        if (!object)
        {
        	this.getContentPanel().down('#right-card-container').getLayout().setActiveItem(0);
        }
        else
        {
        	this.getContentPanel().down('#right-card-container').getLayout().setActiveItem(1);
            
            this._assignmentsGrid.getDockedItems('#context-helper-text')[0].update("{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_HINT1}}" + hintTextContext);
            this._updateGrid();
        }
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
     */
    onCellClick: function(recordId, profileId)
    {
    	function callback (computedValue)
    	{
    		// Re-init other local values to be sure to not have previously induced values
        	this._reinitComputedLocalValues (profileId);
        	
            // Compute and update the induced values on other records of same column
        	var records = this._getUnfilteredRecords();
        	Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.AssignmentHelper.computeAndUpdateLocalInducedAssignments (records, profileId);
    	}
    	
    	// Compute and set the new value
    	var newValue = this._computeNewValue (recordId, profileId, callback, this);
    },
    
    /**
     * @private
     * Compute the new value for assignment and save the new value on record.
     * The value is computed on following rotation : Allow <-> Deny <-> Unknown <-> Allow
     * @param {String} recordId The id of record
     * @param {String} profileId The id of profile
     * @param {Function} callback The callback function invoked after computing the new value. The arguments are:
	 * @param {Function} callback.value The computed value
	 * @param {Object} [scope] The scope of callback function
     */
    _computeNewValue: function (recordId, profileId, callback, scope)
    {
    	var record = this._gridStore.getById(recordId);

    	var newValue;
    	// Local value (client-side)
    	var currentValue = record.get(profileId);
    	// Stored value (server-side)
    	var storedValue = this._storedValues[profileId][recordId] || Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN;
    	
    	if (currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW)
        {
    		this._setComputedValue(Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY, record, profileId, callback, scope);
        }
    	else if (currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY)
        {
    		// When go to 'unknown' value, the value is computed from parent contexts
    		var parameters = [
    		       this._contextCombobox.getValue(), 
    		       this._objectContext, 
    		       profileId, 
    		       record.get('targetType'), 
    		       this._getIdentity(record)
    		];
    		this.serverCall('getInheritedAssignment', parameters, Ext.bind(this._setComputedValue, this, [record, profileId, callback, scope], 1), {waitMessage: false});
        }
    	else
        {
    		this._setComputedValue(Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW, record, profileId, callback, scope);
        }
    },
    
    /**
     * @private
     * Function invoked after computing the new assignment value. Set the new value.
     * @param {String} newValue The computed value
     * @param {String} record The record to update
     * @param {String} profileId The id of profile
     * @param {Function} callback The callback function invoked after computing the new value. The arguments are:
	 * @param {Function} callback.value The computed value
	 * @param {Object} [scope] The scope of callback function
     */
    _setComputedValue: function (newValue, record, profileId, callback, scope)
    {
    	var storedValue = this._storedValues[profileId][record.getId()] || Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN;
    	record.set(profileId, newValue, {dirty: storedValue != newValue});
    	
    	if (Ext.isFunction(callback))
    	{
    		callback.call (scope, newValue);
    	}
    },
    
    /**
     * @private
     * Reinitialize the local values (client-side) of records with the last stored values (server-side) for the given profile (column) except for the given record
     * @param {String} profileId The id of profile
     * @param {String} recordId The id of record to ignore
     */
    _reinitComputedLocalValues: function (profileId)
    {
    	var storedValues = this._storedValues[profileId]
    	
    	this._getUnfilteredRecords().each(function(record) {
    		if (!record.dirty)
    		{
    			var storedValue = storedValues[record.getId()] || Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN;
        		record.set(profileId, storedValue, {dirty: false})
    		}
    	});
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
    	var usersToAdd = [],
    		records = this._getUnfilteredRecords();
    	
    	Ext.Array.forEach(users, function(user) {
        	if (!Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.AssignmentHelper.findUserRecord(records, user.login, user.population))
        	{
        		usersToAdd.push(user);
        	}
        }, this);
    	
    	var total = usersToAdd.length,
    		count = 0;
    	
        function addInStore(groups, user)
        {
        	this._gridStore.add({
                targetType: this.self.TARGET_TYPE_USER,
                login: user.login,
                population: user.population,
                populationLabel: user.populationName,
                userSortableName: user.fullName,
                groups: groups
            });
        	count++;
        	
        	if (count == total)
            {
            	this._onStoreUpdated(records.getRange());
            }
        }
        
        Ext.Array.forEach(usersToAdd, function(user) {
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
    	var needUpdate = false,
			records = this._getUnfilteredRecords();
    	
        Ext.Array.forEach(groups, function(group) {
        	if (!Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.AssignmentHelper.findGroupRecord(records, group.id, group.groupDirectory))
        	{
	        	this._gridStore.add({
	                targetType: this.self.TARGET_TYPE_GROUP,
	                groupId: group.id,
	                groupDirectory: group.groupDirectory,
	                groupDirectoryLabel: group.groupDirectoryName,
	                groupLabel: group.label
	            });
	        	
	        	needUpdate = true;
        	}
        }, this);
        
        if (needUpdate)
        {
        	this._onStoreUpdated(records.getRange());
        }
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
                && record.get('targetType') != this.self.TARGET_TYPE_ANONYMOUS
                && record.get('targetType') != this.self.TARGET_TYPE_ANYCONNECTEDUSER)
            {
                // Iterate through the profiles, only keep the ones with local assignments to avoid useless removal
                Ext.Array.forEach(this._profiles, function(profile) {
                    var profileId = profile.id,
                        currentAssignment = record.get(profileId);
                    if (currentAssignment == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || currentAssignment == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY)
                    {
                        var assignmentInfo = {
                            profileId: profileId,
                            targetType: record.get('targetType'),
                            assignment: null
                        };
                        
                        if (record.get('targetType') == this.self.TARGET_TYPE_USER)
                        {
                            assignmentInfo.identity = {
                                login: record.get('login'),
                                population: record.get('population')
                            };
                        }
                        else
                        {
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
                assignmentsInfo.push({
                    profileId: profileId,
                    targetType: record.get('targetType'),
                    assignment: record.get(profileId), // 'allow', 'deny' or others
                    identity: this._getIdentity (record)
                });
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
            context: this._objectContext
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
     * This function has to be invoked after the store is modified 
     * For instance, callback of the store loading, or after adding a new user/group record.
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry[]} records The records of the grid
     */
    _onStoreUpdated: function(records)
    {
    	var me = this;
    	
    	// Update the server-side values
    	this._reinitStoredValues();
    	
    	// Update local assignments for each profiles
    	var records = this._getUnfilteredRecords();
    	Ext.Array.forEach(this._profiles, function(profile) {
    		Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.AssignmentHelper.computeAndUpdateLocalInducedAssignments (records, profile.id);
    	});
    	
    	// Clear the selection
        this._assignmentsGrid.getSelectionModel().deselectAll();
    },
    
    /**
     * @private
     * Create a columns object representing the columns
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry[]} records The records read from the server
     */
    _reinitStoredValues: function(records)
    {
        this._storedValues = {};
        Ext.Array.forEach(this._profiles, function(profile) {
            this._storedValues[profile.id] = {};
        }, this);
        
        var records = this._getUnfilteredRecords();
        records.each(function(record) {
            Ext.Object.each(this._storedValues, function(profileId, assignments) {
            	assignments[record.getId()] = record.get(profileId) || Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN;
            }, this)
        }, this);
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
            newColumns.push({
                stateId: 'grid-profile-' + profile.id,
                text: profile.label,
                dataIndex: profile.id,
                hideable: profile.id != this.self.READER_PROFILE_ID,
                sortable: false,
                align: 'center',
                cls: profile.id == this.self.READER_PROFILE_ID ? 'a-column-header-reader' : '',
                tdCls: profile.id == this.self.READER_PROFILE_ID ? 'a-grid-cell-reader' : '',
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
                if (assignment == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || assignment == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY)
                {
                    result = true;
                    return false;
                }
            }, me);
            
            return result;
        }
        
        var selection = this._assignmentsGrid.getSelection();
        
        var targets = Ext.Array.map(selection, function(record) {
            var type = record.get('targetType'),
                removable = (type == this.self.TARGET_TYPE_USER || type == this.self.TARGET_TYPE_GROUP)
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
        var type = record.get('targetType');
        switch (type) {
            case this.self.TARGET_TYPE_ANONYMOUS:
                var text = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TARGET_TYPE_ANONYMOUS}}";
                return '<span class="ametysicon-carnival23"></span> ' + text;
            case this.self.TARGET_TYPE_ANYCONNECTEDUSER:
                var text = "{{i18n PLUGINS_CORE_UI_TOOL_PROFILE_ASSIGNMENTS_TARGET_TYPE_ANYCONNECTEDUSERS}}";
                return '<span class="ametysicon-key162"></span> ' + text;
            case this.self.TARGET_TYPE_USER:
                var text = Ametys.plugins.core.users.UsersDAO.renderUser(record.get('login'), record.get('populationLabel'), value);
                return '<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/' + record.get('population') + '/' + record.get('login') + '/image_16" class="a-grid-icon a-grid-icon-user"/>' + text;
            case this.self.TARGET_TYPE_GROUP:
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
        
        var glyph, suffix;
        
        switch (value) 
        {
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS:
                glyph = "ametysicon-check";
                suffix = "allowed disabled";
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW:
                glyph = "ametysicon-check-1";
                suffix = "allowed";
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_GROUP:
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANYCONNECTED:
                glyph = "ametysicon-check";
                suffix = "allowed";
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW:
                glyph = "ametysicon-check decorator-ametysicon-up-arrow";
                suffix = "allowed";
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY:
                glyph = "ametysicon-cross-1";
                suffix = "denied"; 
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_GROUP:
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_ANYCONNECTED:
                glyph = "ametysicon-cross"; 
                suffix = "denied"; 
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY:
            	// Denied by inheritance
                glyph = "ametysicon-cross decorator-ametysicon-up-arrow"; 
                suffix = "denied"; 
                break;
            case Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_UNKNOWN:
            default:
            	// Undetermined
                glyph = "ametysicon-minus-symbol";
                suffix = "unknown";
        }
        
        var tooltip = Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.computeTooltip(record, value);
        
        metaData.tdAttr = 'data-qtip="' + tooltip + '"';
        
        var onclickFn = "Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.onCellClick('" + record.get('id') + "', '" + metaData.column.dataIndex + "')";
        
        if (value == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS)
        {
        	// Disable cell (no action available)
        	// We need to reinitialize onclick to empty otherwise the last setted onclick function remains attached to the span (strange ...)
            return '<span class="a-grid-assignment-glyph ' + glyph + ' ' + suffix + '" onclick=""/>';
        }
        else
        {
            return '<span class="a-grid-assignment-glyph ' + glyph + ' ' + suffix + '" onclick="' + onclickFn + '"/>';
        }
    },
    
    /**
     * @private
     * Gets all records in the assignment grid store, even the unfiltered items if there is a filter in this store.
     * @return {Ext.util.Collection}
     */
    _getUnfilteredRecords: function()
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
    },
    
    /**
     * @private
     * Returns a Object representing the identity of the record
     * @param {Ext.data.Model} The target record
     * @return {Object} The identity
     */
    _getIdentity: function (record)
    {
    	if (record.get('targetType') == this.self.TARGET_TYPE_USER)
        {
    		return {
    			login: record.get('login'),
    			population: record.get('population')
    		}
        }
        else if (record.get('targetType') == this.self.TARGET_TYPE_GROUP)
        {
        	return {
        		groupId: record.get('groupId'),
        		groupDirectory: record.get('groupDirectory')
        	}
        }
    	return null;
    },
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
        {name: 'targetType'},
        
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

