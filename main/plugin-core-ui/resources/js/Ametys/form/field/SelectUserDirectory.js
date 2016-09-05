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
 * This class provides a widget to query and select one or more user populations.
 * 
 * This widget is registered for fields of type Ametys.form.WidgetManager#TYPE_STRING.<br>
 */
Ext.define('Ametys.form.field.SelectUserDirectory', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
    
    /** @cfg {Boolean/String} [onlyModifiable=false] When true, limit the directories to modifiable ones */
    /**
     * @private
     * @property {Boolean} _onlyModifiable See #cfg-onlyModifiable.
     */
    /** @cfg {String/String[]} [populationContexts=null] The contexts of the populations. null or empty will use the application parameter 'populationContexts'. */
    /**
     * @private
     * @property {String[]} __populationContexts See #cfg-populationContexts.
     */
    /** @cfg {Boolean/String} [showLabels=false] Show a label for each sub field */
    /**
    
    /**
     * @private
     * @property {Ext.data.Store} _store The store of the combobox
     */
     
    constructor: function(config)
    {
        this._onlyModifiable = config.onlyModifiable === true || config.onlyModifiable === 'true';
        this._populationContexts = Ext.Array.from(config.populationContexts || Ametys.getAppParameter("populationContexts"));
        config.defaults = config.defaults || {};
        config.defaults.labelWidth = config.defaults.labelWidth || 150; 
        
        this.callParent(arguments);
    },
    
    /**
     * @private
     * Get the store
     * @return {Ext.data.Store} The store
     */
    getStore: function()
    {
        if (!this._store)
        {
            this._store = Ext.create('Ext.data.Store', {
                autoDestroy: true,
                proxy: {
                    type: 'ametys',
                    plugin: 'core-ui',
                    url: 'populations.json',
                    extraParams: {
                        contexts: this._populationContexts,
                        modifiable: this._onlyModifiable
                    },
                    reader: {
                        type: 'json',
                        rootProperty: 'userPopulations'
                    }
                },
                sorters: [{property: 'label', direction: 'ASC'}],
                fields: [
    	             {name: 'id'},
    	             {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}
                ]
            });
        }
        
        return this._store;
    },
    
    initComponent: function() 
    {
        var me = this;
        
        /**
         * @private
         * @property {Ext.form.field.ComboBox} _userDirectories The users directory combobox
         */
        this._userDirectories = Ext.create("Ext.form.field.ComboBox", {
            cls: 'ametys',
                        
            fieldLabel: this.getInitialConfig().showLabels ? "{{i18n PLUGINS_CORE_UI_TOOL_USERS_USER_DIRECTORY_FIELD}}" : undefined,
            flex: 1,
            valueField: 'index',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            allowBlank: this.getInitialConfig().allowBlank,
                            
            store: {
                fields: ['index', 'label'],
                data: [],
                listeners: {
                    'datachanged': function(store) {
                        // Auto select first value
                        this._userDirectories.clearValue();
                        this._userDirectories.setValue(store.getRange()[0]);
                    },
                    scope: this
                }
            }
        });

        /**
         * @private
         * @property {Ext.form.field.ComboBox} __userPopulations The users population combobox
         */
        this._userPopulations = Ext.create("Ext.form.field.ComboBox", {
            cls: 'ametys',
                        
            fieldLabel: this.getInitialConfig().showLabels ? "{{i18n PLUGINS_CORE_UI_TOOL_USERS_POPULATION_FIELD}}" : undefined,
            flex: 1,
            store: this.getStore(),
            valueField: 'id',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            allowBlank: this.getInitialConfig().allowBlank,
            
            listeners: {
                'change': Ext.bind(function(combo, newValue, oldValue) {
                    var data = [];
                    var record = combo.getStore().getById(newValue);
                    if (record)
                    {
                        Ext.Array.forEach(record.get('userDirectories'), function(userDirectory, index) {
                            if (userDirectory.modifiable)
                            {
                                data.push({
                                    index: userDirectory.index,
                                    label: userDirectory.label
                                });
                            }
                        }, this);
                    }
                    this._userDirectories.getStore().loadData(data, false);
                }, this)
            }
        });
        
        this.items = [ this._userPopulations, this._userDirectories ];
        
        this.callParent();
    },
    
    getValue: function()
    {
        return this._userPopulations.getValue() + '#' + this._userDirectories.getValue();
    },
    
    setValue: function(value)
    {
        if (!value)
        {
            this._userPopulations.setValue(null);
            this._userDirectories.setValue(null);
        }
        
        var index = value.indexOf('#');
        if (index == -1)
        {
            this._userPopulations.setValue(value);
        }
        else
        {
            this._userPopulations.setValue(value.substring(0, index));
            this._userDirectories.setValue(value.substring(index+1));
        }
    }
});