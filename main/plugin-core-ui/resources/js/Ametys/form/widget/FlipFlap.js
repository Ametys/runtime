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
 * This class provides a control that allows selection of between two list controls.<br>
 * 
 * This widget is registered for enumerated and multiple fields of type Ametys.form.WidgetManager#TYPE_STRING.<br>
 */
Ext.define('Ametys.form.widget.FlipFlap', {
	extend: 'Ametys.form.AbstractFieldsWrapper',
    
	mixins: {
        bindable: 'Ext.mixin.Bindable'
    },
    
    /**
     * @cfg {Boolean} [hideNavIcons=false] True to hide the navigation icons
     */
    hideNavIcons:false,
    
    /**
     * @cfg {Array} buttons Defines the set of buttons that should be displayed in between the ItemSelector
     * fields. Defaults to <tt>['top', 'up', 'add', 'remove', 'down', 'bottom']</tt>. These names are used
     * to build the button CSS class names, and to look up the button text labels in {@link #buttonsText}.
     * This can be overridden with a custom Array to change which buttons are displayed or their order.
     */
    buttons: ['top', 'up', 'add', 'remove', 'down', 'bottom'],

    /**
     * @cfg {Object} buttonsText The tooltips for the {@link #buttons}.
     * Labels for buttons.
     */
    buttonsText: {
        top: "{{i18n PLUGINS_CORE_UI_WIDGET_FLIPFLAP_MOVE_TO_TOP}}",
        up: "{{i18n PLUGINS_CORE_UI_WIDGET_FLIPFLAP_MOVE_UP}}",
        add: "{{i18n PLUGINS_CORE_UI_WIDGET_FLIPFLAP_ADD_TO_SELECTED}}",
        remove: "{{i18n PLUGINS_CORE_UI_WIDGET_FLIPFLAP_REMOVE_FROM_SELECTED}}",
        down: "{{i18n PLUGINS_CORE_UI_WIDGET_FLIPFLAP_MOVE_DOWN}}",
        bottom: "{{i18n PLUGINS_CORE_UI_WIDGET_FLIPFLAP_MOVE_TO_BOTTOM}}"
    },

    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    
    constructor: function (config)
	{
    	var storeCfg = {
            id: 0,
            fields: [ 'value', {name: 'text', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
            data: config.enumeration
        };
		
		config.naturalOrder = Ext.isBoolean(config.naturalOrder) ? config.naturalOrder : config.naturalOrder == 'true';
		if (!config.naturalOrder)
		{
			storeCfg.sorters = [{property: 'text', direction:'ASC'}]; // default order
		}
		
		config = Ext.apply(config, {
			queryMode: 'local',
			store: new Ext.data.SimpleStore(storeCfg),
	        valueField: 'value',
	        displayField: 'text'
		});
		
		this.callParent(arguments);
	},
    
    initComponent: function() {
        var me = this;

        me.ddGroup = me.id + '-dd';
        
        me.fromField = me.createList(me.fromTitle);
        me.toField = me.createList(me.toTitle);
        
        me.items = [
            me.fromField,
            {
                xtype: 'container',
                margins: '0 4',
                layout: {
                    type: 'vbox',
                    pack: 'center'
                },
                items: me.createButtons()
            },
            me.toField
        ];
        
        // bindStore must be called after the fromField has been created because
        // it copies records from our configured Store into the fromField's Store
        me.bindStore(me.store);
        
        me.callParent();
    },
    
    /**
     * Creates a list
     * @param {String} title The title of the list
     * @private
     */
    createList: function(title)
    {
        var me = this;

        return Ext.create('Ext.ux.form.MultiSelect', {
            // We don't want the multiselects themselves to act like fields,
            // so override these methods to prevent them from including
            // any of their values
            submitValue: false,
            getSubmitData: function(){
                return null;
            },
            getModelData: function(){
                return null;    
            },
            flex: 1,
            dragGroup: me.ddGroup,
            dropGroup: me.ddGroup,
            title: title,
            store: {
                model: me.store.model,
                data: []
            },
            displayField: me.displayField,
            valueField: me.valueField,
            disabled: me.disabled,
            listeners: {
                boundList: {
                    scope: me,
                    itemdblclick: me.onItemDblClick,
                    drop: me.syncValue
                }
            }
        });
    },
    
    /**
     * Creates the buttons
     * @private
     */
    createButtons: function() {
        var me = this,
            buttons = [];

        if (!me.hideNavIcons) {
            Ext.Array.forEach(me.buttons, function(name) {
                buttons.push({
                    xtype: 'button',
                    tooltip: me.buttonsText[name],
                    handler: me['on' + Ext.String.capitalize(name) + 'BtnClick'],
                    cls: Ext.baseCSSPrefix + 'form-widget-flipflap-btn',
                    iconCls: Ext.baseCSSPrefix + 'form-widget-flipflap-' + name,
                    navBtn: true,
                    scope: me,
                    margin: '4 0 0 0'
                });
            });
        }
        return buttons;
    },
    
    /**
     * Populate from store
     * @param {Object} store The store
     * @private
     */
    populateFromStore: function(store) {
        var fromStore = this.fromField.store;

        // Flag set when the fromStore has been loaded
        this.fromStorePopulated = true;

        fromStore.add(store.getRange());

        // setValue waits for the from Store to be loaded
        fromStore.fireEvent('load', fromStore);
    },
    
    /**
     * Function called when clicking on 'Move to Top' button
     * @private
     */
    onTopBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list);

        store.suspendEvents();
        store.remove(selected, true);
        store.insert(0, selected);
        store.resumeEvents();
        list.refresh();
        this.syncValue(); 
        list.getSelectionModel().select(selected);
    },

    /**
     * Function called when clicking on 'Move to Bottom' button
     * @private
     */
    onBottomBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list);

        store.suspendEvents();
        store.remove(selected, true);
        store.add(selected);
        store.resumeEvents();
        list.refresh();
        this.syncValue();
        list.getSelectionModel().select(selected);
    },

    /**
     * Function called when clicking on 'Move Up' button
     * @private
     */
    onUpBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list),
            rec,
            i = 0,
            len = selected.length,
            index = 0;

        // Move each selection up by one place if possible
        store.suspendEvents();
        for (; i < len; ++i, index++) {
            rec = selected[i];
            index = Math.max(index, store.indexOf(rec) - 1);
            store.remove(rec, true);
            store.insert(index, rec);
        }
        store.resumeEvents();
        list.refresh();
        this.syncValue();
        list.getSelectionModel().select(selected);
    },

    /**
     * Function called when clicking on 'Move Down' button
     * @private
     */
    onDownBtnClick : function() {
        var list = this.toField.boundList,
            store = list.getStore(),
            selected = this.getSelections(list),
            rec,
            i = selected.length - 1,
            index = store.getCount() - 1;

        // Move each selection down by one place if possible
        store.suspendEvents();
        for (; i > -1; --i, index--) {
            rec = selected[i];
            index = Math.min(index, store.indexOf(rec) + 1);
            store.remove(rec, true);
            store.insert(index, rec);
        }
        store.resumeEvents();
        list.refresh();
        this.syncValue();
        list.getSelectionModel().select(selected);
    },

    /**
     * Function called when clicking on 'Add to selected' button
     * @private
     */
    onAddBtnClick : function() {
        var me = this,
            selected = me.getSelections(me.fromField.boundList);

        me.moveRec(true, selected);
        me.toField.boundList.getSelectionModel().select(selected);
    },

    /**
     * Function called when clicking on 'Remove from selected' button
     * @private
     */
    onRemoveBtnClick : function() {
        var me = this,
            selected = me.getSelections(me.toField.boundList);

        me.moveRec(false, selected);
        me.fromField.boundList.getSelectionModel().select(selected);
    },
    
    /**
     * Get the selected records from the specified list.
     * 
     * Records will be returned *in store order*, not in order of selection.
     * @param {Ext.view.BoundList} list The list to read selections from.
     * @return {Ext.data.Model[]} The selected records in store order.
     * 
     */
    getSelections: function(list) {
        var store = list.getStore();

        return Ext.Array.sort(list.getSelectionModel().getSelection(), function(a, b) {
            a = store.indexOf(a);
            b = store.indexOf(b);

            if (a < b) {
                return -1;
            } else if (a > b) {
                return 1;
            }
            return 0;
        });
    },

    /**
     * Move records from list to another
     * @param {Boolean} add true to move to right list, false to move to left list
     * @param {Ext.data.Model[]} recs The records to move
     * @private
     */
    moveRec: function(add, recs) {
        var me = this,
            fromField = me.fromField,
            toField   = me.toField,
            fromStore = add ? fromField.store : toField.store,
            toStore   = add ? toField.store   : fromField.store;

        fromStore.suspendEvents();
        toStore.suspendEvents();
        fromStore.remove(recs);
        toStore.add(recs);
        fromStore.resumeEvents();
        toStore.resumeEvents();

        fromField.boundList.refresh();
        toField.boundList.refresh();

        me.syncValue();
    },

    // Synchronizes the submit value with the current state of the toStore
    syncValue: function() {
        var me = this; 
        this.self.superclass.setValue.call(me, me.setupValue(me.toField.store.getRange()));
    },

    /**
     * Listener on double click on a item of the list
     * @param {Ext.view.View} view The view where the double-click occurred
     * @param {Ext.data.Model} rec The double-clicked records
     * @private
     */
    onItemDblClick: function(view, rec) {
        this.moveRec(view === this.fromField.boundList, rec);
    },
    
    /**
     * Setup the value
     * @param {Object} value the value
     * @private
     */
    setupValue: function(value){
        var delimiter = this.delimiter,
            valueField = this.valueField,
            i = 0,
            out,
            len,
            item;
            
        if (Ext.isDefined(value)) {
            if (delimiter && Ext.isString(value)) {
                value = value.split(delimiter);
            } else if (!Ext.isArray(value)) {
                value = [value];
            }
        
            for (len = value.length; i < len; ++i) {
                item = value[i];
                if (item && item.isModel) {
                    value[i] = item.get(valueField);
                }
            }
            out = Ext.Array.unique(value);
        } else {
            out = [];
        }
        return out;
    },
    
    /**
     * Get the records from given values
     * @param {Object} value the value
     * @private
     */
    getRecordsForValue: function(value){
        var me = this,
            records = [],
            all = me.store.getRange(),
            valueField = me.valueField,
            i = 0,
            allLen = all.length,
            rec,
            j,
            valueLen;
            
        for (valueLen = value.length; i < valueLen; ++i) {
            for (j = 0; j < allLen; ++j) {
                rec = all[j];   
                if (rec.get(valueField) == value[i]) {
                    records.push(rec);
                }
            }    
        }
            
        return records;
    },
    
    /**
     * Listener on bind store
     * @param {Object} store the store
     * @private
     */
    onBindStore: function(store) 
    {
        var me = this;

        if (me.fromField) {
            me.fromField.store.removeAll()
            me.toField.store.removeAll();

            // Add everything to the from field as soon as the Store is loaded
            if (store.getCount()) {
                me.populateFromStore(store);
            } else {
                me.store.on('load', me.populateFromStore, me);
            }
        }
    },
    
    /**
     * @inheritdoc
     */
    setValue: function(value){
        var me = this,
            store = me.store;

        // Store not loaded yet - we cannot set the value
        if (!store.getCount()) {
            store.on({
                load: Ext.Function.bind(me.setValue, me, [value]),
                single: true
            });
            return;
        }

        value = me.setupValue(value);
        
        var records = me.getRecordsForValue(value);
        me.moveRec(true, records);
        
        this.callParent(arguments);
    },
    
    /**
     * @inheritdoc
     */
    getSubmitValue: function() {
    	return this.getValue();
    }
	
});
