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
 * This is a helper to select one or more users from the user manager. See {@link #act} method
 * 
 *      Ametys.helper.SelectUser.act({
 *          callback: Ext.bind(function (users) { console.info(users[0].login + ', ' + users[0].population); }, this), 
 *          allowMultiselection: true,
 *      }); 
 */
Ext.define('Ametys.helper.SelectUser', {
    singleton: true,
    
    /**
     * @property {Number} RESULT_LIMIT
     * @readonly
     * The maximum number of records to search for
     */
    RESULT_LIMIT: 100,
    
    /**
     * @private
     * @property {Boolean} _initialized Determines if the dialog box have been already initialized or not
     */
    _initialized: false,
    
    /**
     * @property {Boolean} allowMultiselection Property to enable or disable multiselection
     */
    
    /**
     * @property {String} pluginName=core The name of the currently selected plugin to use for requests. Selected by the {@link #act} call.
     */
    /**
     * @property {String} url=users/search.json The url of the currently selected plugin to use for requests. Selected by the {@link #act} call.
     */
    /**
     * @property {String[]} [contexts] The contexts for the populations to display in the combobox
     */
    /**
     * @property {Boolean} _enableAllPopulationsOption True to add an option in the populations combobx for searching over all the populations.
     * @private
     */
    /**
     * @property {String} _noPopulationMessage The message to display when there is no user population available for the context.
     * @private
     */
    /**
     * @property {String} _allPopulationsOptionId The id of the 'all populations' options.
     * @private
     * @readonly
     */
    _allPopulationsOptionId: '#all',
    
    /**
     * @private
     * @property {Function} callBack The current callback function registered by the {@link #act} call
     */
    /**
     * @private
     * @property {Function} cancelCallback The current cancel callback function registered by the {@link #act} call
     */
    /**
     * @private
     * @property {Ext.form.field.ComboBox} _userPopulationsField The combobox of the dialog box that displays the user populations where to search
     */
    /**
     * @private
     * @property {Ext.form.field.ComboBox} _userDirectoriesField The combobox of the dialog box that displays the user directories where to search
     */
    /**
     * @private
     * @property {Ext.form.field.Text} _searchField The field of the dialog box that displays the filter field
     */
    /**
     * @private
     * @property {Ametys.window.DialogBox} _box The re-usable dialog box
     */
    /**
     * @private
     * @property {Ext.grid.Panel} _userList The grid of result users
     */
    
    /**
     * Open the dialog box to select a user
     * @param {Object} config The configuration options:
     * @param {Function} config.callback The callback function to call when user(s) has(have) been selected
     * @param {Object[]} config.callback.users An array of users.
     * @param {String} config.callback.users.login The user's login 
     * @param {String} config.callback.users.population The user's population id 
     * @param {String} config.callback.users.populationName The user's population name
     * @param {String} config.callback.users.fullName The user's fullname
     * @param {Function} config.cancelCallback The callback function if the user cancel the dialog box. Can be null.
     * @param {Boolean} [config.allowMultiselection=true] Set to false to disable multiple selection of users.
     * @param {String} [config.plugin=core] The plugin to use for search request.
     * @param {String} [config.url=users/search.json] The url to use for search request.
     * @param {String/String[]} [config.contexts] The contexts for the populations to display in the combobox. Default to the current contexts.
     * @param {Boolean} [config.enableAllPopulationsOption=true] True to add an option in the populations combobx for searching over all the populations.
     * @param {String} [config.noPopulationMessage] The message to display when there is no user population available for the contexts. There is a default message if not provided.
     * @param {Boolean} [config.showDirectoryCombobox] True to show the user directory combobox field (then it is possible to filter with user directories).
     */
    act: function (config)
    {
        config = config || {};
        
        this.callback = config.callback || function () {};
        this.cancelCallback = config.cancelCallback || function () {};
        this.allowMultiselection = config.allowMultiselection || true;
        this.pluginName = config.plugin || 'core';
        this.url = config.url || 'users/search.json';
        this.contexts = Ext.Array.from(config.contexts || Ametys.getAppParameter('populationContexts'));
        this._enableAllPopulationsOption = config.enableAllPopulationsOption !== false;
        this._noPopulationMessage = config.noPopulationMessage || "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_NO_POPULATION_DESCRIPTION}}";
        
        this._delayedInitialize();
        
        this._userPopulationsField.clearValue();
        this._userDirectoriesField.setVisible(config.showDirectoryCombobox == "true");
        this._userDirectoriesField.getStore().loadData([], false);
        this._searchField.setValue("");
        this._userList.getStore().loadData([], false);
        this._userList.getSelectionModel().setSelectionMode(this.allowMultiselection ? 'SIMPLE' : 'SINGLE');
        this._userList.getSelectionModel().deselectAll();
        this._userList.getStore().setProxy ({
            type: 'ametys',
            plugin: this.pluginName,
            url: this.url,
            reader: {
                type: 'json',
                rootProperty: 'users'
            },
            extraParams: {
                limit: this.RESULT_LIMIT
            }
        });
        
        this._box.show();
        this.loadPopulations();
    },
    
    /**
     * @private
     * This method is called to initialize the dialog box. Only the first call will be taken in account.
     */
    _delayedInitialize: function ()
    {
        if (this._initialized)
        {
            return true;
        }
        this._initialized = true;

        this._userPopulationsField = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_POPULATION}}",
            name: "userPopulations",
            cls: 'ametys',
            labelWidth: 150,
            
            store: {
                fields: ['id', {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
                proxy: {
                    type: 'ametys',
                    plugin: 'core-ui',
                    url: 'populations.json',
                    reader: {
                        type: 'json',
                        rootProperty: 'userPopulations'
                    }
                },
                sorters: [{property: 'label', direction: 'ASC'}],
                listeners: {
                    'beforeload': {fn: this._onBeforeLoadPopulations, scope: this},
                    'load': {fn: this._onLoadPopulations, scope: this}
                }
            },
            valueField: 'id',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            
            listeners: {
                'change': {fn: this._onChangePopulation, scope: this}
            }
        });
        
        this._userDirectoriesField = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_USER_DIRECTORY}}",
            name: "userDirectories",
            cls: 'ametys',
            labelWidth: 150,
            hidden: true, // hidden by default, will be shown if told
            
            store: {
                fields: ['id', {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
                data: [],
                sorters: [{property: 'label', direction: 'ASC'}],
                listeners: {
                    'datachanged': Ext.bind(function(store) {
                        this._userDirectoriesField.clearValue();
                        this._userDirectoriesField.setValue("-");
                    }, this)
                }
            },
            valueField: 'id',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            
            listeners: {change: Ext.Function.createBuffered(this.loadUsers, 500, this)}
        });
        
        this._searchField = Ext.create('Ext.form.TextField', {
             fieldLabel: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_FIND}}",
             name: "criteria",
             cls: 'ametys',
             
             labelWidth: 150,
             
             value: "",
             
             listeners: {change: Ext.Function.createBuffered(this.loadUsers, 500, this)}
        });
        
        var model = Ext.define('Ametys.helper.SelectUser.Users', {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'id', mapping: 'login'},
                {name: 'login'},
                {name: 'population'},
                {name: 'populationLabel'},
                {name: 'lastname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                {name: 'firstname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                {name: 'email'},
                {name: 'fullname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                {name: 'sortablename', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                {
                    name: 'displayName',
                    sortType: Ext.data.SortTypes.asNonAccentedUCString,
                    calculate: function (data)
                    {
                        return Ametys.plugins.core.users.UsersDAO.renderUser(data.login, data.populationLabel, data.sortablename);
                    }
                }
            ]
        });

        var store = Ext.create('Ext.data.Store', {
            model: 'Ametys.helper.SelectUser.Users',
            data: { users: []},
            listeners: {
                'beforeload': Ext.bind(this._onBeforeLoad, this),
                'load': Ext.bind(this._onLoad, this)
            },
            remoteSort: false,
            sortOnLoad: true,
            sorters: [{property: 'displayName', direction:'ASC'}]
        });
        
        this._userList = Ext.create('Ext.grid.Panel', {
            flex: 1,
            store : store,
            hideHeaders : true,
            columns: [{header: "Label", flex: 1, menuDisabled : true, sortable: true, dataIndex: 'displayName', renderer: Ext.bind(this._renderDisplayName, this)}]
        }); 
        
        this._box = Ext.create('Ametys.window.DialogBox', {
            title : this.allowMultiselection ? "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSERS_DIALOG_CAPTION}}" : "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION}}",
            //icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/users/user_16.png',
            iconCls: 'ametysicon-black302',
            
            layout: {
                type: 'vbox',
                align : 'stretch',
                pack  : 'start'
            },
            width: 450,
            height: 600,
            
            items : [
                     this._userPopulationsField, 
                     this._userDirectoriesField, 
                     this._searchField, 
                     this._userList, 
                     {
                         xtype: 'container',
                         style: {
                             textAlign: 'center'
                         },
                         cls: 'a-text-warning',
                         html: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_WARN100}}"
                     }
            ],
            
            defaultFocus: this._searchField,
            closeAction: 'hide',
            
            defaultButton: 'validate',
            referenceHolder: true,
            
            buttons : [{
        		reference: 'validate',
                text: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_OK}}",
                handler: Ext.bind(this.ok, this)
            }, {
                text: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CANCEL}}",
                handler: Ext.bind(this.cancel, this)
            } ]
        });
    },
    
    /**
     * Function called before loading the population store
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
     * @private
     */
    _onBeforeLoadPopulations: function(store, operation)
    {
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            contexts: this.contexts
        }));
    },
    
    /**
     * @private
     * Listener invoked after loading populations
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.Model[]} records The records of the store
     */
    _onLoadPopulations: function(store, records)
    {
        if (records.length == 0)
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_NO_POPULATION_TITLE}}",
                msg: this._noPopulationMessage,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.WARNING,
                fn: Ext.bind(function() {
                    this._box.close();
                }, this)
            });
            return;
        }
        
        if (this._enableAllPopulationsOption)
        {
            // Add an option in the populations combobox for searching over all the populations
            store.add({
                id: this._allPopulationsOptionId,
                label: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_POPULATION_OPTION_ALL}}"
            });
        }
    },
    
    /**
     * @private
     * Function called when the value of the population combobox field changed.
     * @param {Ext.form.field.ComboBox} combo The combobox
     * @param {String} newValue The new value
     * @param {String} oldValue The original value
     */
    _onChangePopulation: function(combo, newValue, oldValue)
    {
        if (newValue == this._allPopulationsOptionId)
        {
            // Search over all the populations
            this._userDirectoriesField.setDisabled(true);
            Ext.defer(this.loadUsers, 500, this);
            return;
        }
        else
        {
            this._userDirectoriesField.setDisabled(false);
        }
        
        // Populate the user directories combobox store
        var data = [{
            id: '-',
            label: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_USER_DIRECTORY_OPTION_ALL}}"
        }];
        var record = combo.getStore().getById(newValue);
        if (record)
        {
	        Ext.Array.forEach(record.get('userDirectories'), function(item, index) {
	            data.push({
	                id: item.id,
	                label: item.label
	            });
	        }, this);
	        this._userDirectoriesField.getStore().loadData(data, false);
        }
    },
    
    /**
     * Load the store of the populations combobox.
     */
    loadPopulations: function()
    {
        this._userPopulationsField.getStore().load({
            scope: this,
            callback: function(records) {
                // When store loaded, select the 'all' option if available
                if (this._enableAllPopulationsOption)
                {
                    this._userPopulationsField.select(this._allPopulationsOptionId);
                }
                // Otherwise select the fist data
                else if (records.length > 0)
                {
                    this._userPopulationsField.select(records[0].get('id'));
                }
                // If there is one and only one population, hide the combobox
                this._userPopulationsField.setHidden(records.length == 1);
            }
        });
    },
    
    /**
     * This method is called to apply the current filter immediately
     * @private
     */
    loadUsers: function ()
    {
        this._userList.getStore().load();
    },
    
    /**
     * Function called before loading the store
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
     * @param {Object} eOpts Event options
     * @private
     */
    _onBeforeLoad: function(store, operation, eOpts)
    {
        // If one of the two comboboxes is invalid, cancel the loading
        if (this._userPopulationsField.getValue() == null 
            || this._userPopulationsField.getValue() != this._allPopulationsOptionId && this._userDirectoriesField.getValue() == null)
        {
            return false;
        }
        
        // 'all' option is selected
        if (this._userPopulationsField.getValue() == this._allPopulationsOptionId)
        {
            operation.setParams( Ext.apply(operation.getParams() || {}, {
                contexts: this.contexts,
                criteria: this._searchField.getValue()
            }));
            return true;
        }
        
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            userPopulationId: this._userPopulationsField.getValue(),
            userDirectoryId: this._userDirectoriesField.getValue(),
            criteria: this._searchField.getValue()
        }));
    },
    
    /**
     * Function called after loading the store
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.Model[]} records The loaded records
     */
    _onLoad: function (store, records)
    {
        if (records != null && records.length == 0)
        {
            Ametys.Msg.show({
               title: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION}}",
               msg: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_NORESULT}}",
               buttons: Ext.Msg.OK,
               icon: Ext.MessageBox.INFO
            });
        }
    },
    
    /**
     * @private
     * The method called when the user push the ok button of the dialog box
     */
    ok: function ()
    {
        var addedusers = [];
        
        var selection = this._userList.getSelectionModel().getSelection();
        if (selection.length == 0)
        {
            Ametys.Msg.show({
                   title: this.allowMultiselection ? "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSERS_DIALOG_CAPTION}}" : "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION}}",
                   msg: "{{i18n PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_ERROR_EMPTY}}",
                   buttons: Ext.Msg.OK,
                   icon: Ext.MessageBox.INFO
                });
            return;
        }

        this._box.hide();
        
        for (var i=0; i < selection.length; i++)
        {
            var opt = selection[i];
            addedusers.push({
                login: opt.get('login'),
                population: opt.get('population'),
                fullName: opt.get('sortablename'),
                populationName: opt.get('populationLabel')
            });
        }
    
        this.callback(addedusers);
    },

    /**
     * @private
     * The method called when the user cancel the dialog box
     */
    cancel: function ()
    {
        this._box.hide();
        this.cancelCallback();
    },
    
    /**
     * @private
     * Renderer for user name
     * @param {Object} value The data value
     * @param {Object} metaData A collection of data about the current cell
     * @param {Ext.data.Model} record The record
     * @return {String} The html value to render.
     */
    _renderDisplayName: function(value, metaData, record)
    {
        return '<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/' + record.get('population') + '/' + record.get('login') + '/image_16" class="a-grid-icon a-grid-icon-user"/>' + value;
    }
});
