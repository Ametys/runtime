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
 * Field for selecting one or several group directories.
 */
Ext.define('Ametys.form.field.SelectGroupDirectories', {
    extend: 'Ametys.form.AbstractQueryableComboBox',
    
    /**
     * @cfg {Boolean} [allowCreation=false] Set to `true` to allow directory creation.
     */
    allowCreation: false,
    
    /**
     * @cfg {String} createButtonText='' The text of the create content button.
     */
    createButtonText: '',
    /**
     * @cfg {String} createButtonIcon The button icon path for the create content button.
     */
    createButtonIcon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/groupdirectories/add_16.png',
    /**
     * @cfg {String} createButtonTooltip The button icon tooltip for the create button.
     */
    createButtonTooltip: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_SELECT_GROUP_DIRECTORIES_WIDGET_CREATEBUTTON_TOOLTIP}}",
    
    /**
     * @private
     * @property {Ext.data.Store} _store The store of the combobox
     */
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
    },
    
    onDestroy: function()
    {
        Ametys.message.MessageBus.unAll(this);
        this.callParent(arguments);
    },
    
    getStore: function()
    {
        this._store = Ext.create('Ext.data.Store', {
            autoDestroy: true,
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'group-directories.json',
                reader: {
                    type: 'json',
                    rootProperty: 'groupDirectories'
                }
             },
             sorters: [{property: 'label', direction: 'ASC'}],
             fields: [
	             {name: 'id'},
	             {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}
             ]
        });
        
        return this._store;
    },
    
    getItems: function()
    {
        var items = this.callParent(arguments);
        
        if (!this.readOnly)
        {
            // Button that opens the create dialog box. 
            if (this.allowCreation == true || this.allowCreation == 'true')
            {
                var createButton = Ext.create('Ext.button.Button', {
                    text: this.createButtonText,
                    icon: this.createButtonIcon,
                    tooltip: this.createButtonTooltip,
                    handler: this._createGroupDirectory,
                    scope: this
                });
                items.push(createButton);
            }
        }
        
        return items;
    },
    
    /**
     * @private
     * Opens the {@link EditGroupDirectoryHelper} for creating a group directory.
     */
    _createGroupDirectory: function()
    {
        Ametys.plugins.coreui.groupdirectories.EditGroupDirectoryHelper.open(null, 'add');
    },
    
    /**
     * Listener on creation message.
     * @param {Ametys.message.Message} message The creation message.
     * @private
     */
    _onMessageCreated: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.GROUP_DIRECTORY);
        if (targets.length > 0)
        {
            var createdDirectories = Ext.Array.map(targets, function(target) {return target.getParameters().id;}, this);
            this._store.load({
                scope: this,
                callback: function(records)
                {
                    this.combobox.addValue(createdDirectories);
                }
            });
        }
    }
    
});