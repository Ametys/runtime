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
Ext.define('Ametys.form.widget.UserPopulation', {
    extend: 'Ametys.form.AbstractQueryableComboBox',
    
    /**
     * @cfg {Boolean} [allowCreation=false] Set to `true` to allow population creation.
     */
    allowCreation: false,
    
    /**
     * @cfg {String} createButtonText='' The text of the create content button.
     */
    createButtonText: '',
    /**
     * @cfg {String} createButtonIcon The button icon path for the create content button.
     */
    createButtonIcon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/populations/add_16.png',
    /**
     * @cfg {String} createButtonTooltip The button icon tooltip for the create button.
     */
    createButtonTooltip: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_SELECT_POPULATIONS_WIDGET_CREATEBUTTON_TOOLTIP}}",
    
    /**
     * @private
     * @property {Ext.data.Store} _store The store of the combobox
     */
    
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
                url: 'populations.json',
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
        
        return this._store;
    },
    
    getItems: function()
    {
        var items = this.callParent(arguments);
        
        if (!this.readOnly && (this.allowCreation == true || this.allowCreation == 'true'))
        {
            // Button that opens the create dialog box. 
            var createButton = Ext.create('Ext.button.Button', {
                text: this.createButtonText,
                icon: this.createButtonIcon,
                tooltip: this.createButtonTooltip,
                handler: this._createUserPopulation,
                scope: this
            });
            items.push(createButton);
        }
        
        return items;
    },
    
    /**
     * @private
     * Opens the {@link Ametys.plugins.coreui.populations.EditPopulationHelper} for creating a user population.
     */
    _createUserPopulation: function()
    {
        Ametys.plugins.coreui.populations.EditPopulationHelper.open(null, 'add', null, Ext.bind(this._onPopulationCreated, this));
    },
    
    /**
     * @private
     * Selects in the combobox the created population
     * @param {String} populationId The id of the created population
     */
    _onPopulationCreated: function(populationId)
    {
        this._store.load({
            scope: this,
            callback: function(records)
            {
                this.combobox.addValue(this.multiple ? [populationId] : populationId);
            }
        });
    }
});
