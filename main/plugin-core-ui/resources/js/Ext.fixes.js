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

// ------------------------------
// Here are ExtJS bug fixes
// ------------------------------
(function()
{
    Ext.override(Ext.layout.ContextItem, {
        // Fix CMS-5908 http://www.sencha.com/forum/showthread.php?291412-Error-after-upgrade-to-ExtJS-4.2.3
        init: function (full, options) {
            var me = this;
            
            var protection = false;
            if (me.ownerLayout && !target.ownerLayout.isItemBoxParent)
            {
                target.ownerLayout.isItemBoxParent = function() { return false; };
                protection = true;
            }
            
            var returnValue = this.callParent(arguments);
            
            if (protection)
            {
                delete target.ownerLayout.isItemBoxParent;
            }
            
            return returnValue;
        }
    });
    
    Ext.override(Ext.menu.Menu, {
        initComponent: function()
        {
            this.callParent(arguments);
            
            this.on('resize', this._onResize, this);
        },
        
        // FIX CMS-5997 http://www.sencha.com/forum/showthread.php?297558-ExtJs-4.2.3-Adding-items-to-an-opened-menu-on-a-floating-parent&p=1086597#post1086597
        _onResize: function(menu, width, height, oldWidth, oldHeight, eOpts)
        {
            if (this.isVisible() && this.floatParent)
            {
                this.showBy(this.ownerCmp, this.menuAlign);
            }
        }
    });
    
    Ext.override(Ext.view.DropZone, {
        // FIXME CMS-6262 https://www.sencha.com/forum/showthread.php?301552-ExtJS-4.2.3-Drag-n-drop-in-a-grid-and-invalid-zone.&p=1101961#post1101961
        containsRecordAtOffset: function(records, record, offset) 
        {
            if (!record) {
                return false;
            }
            
            var view = this.view,
                recordIndex = view.indexOf(record),
                nodeBefore = view.getNode(recordIndex + offset, true),
                recordBefore = nodeBefore ? view.getRecord(nodeBefore) : null;
    
            var containsRecordAtOffset = recordBefore && Ext.Array.contains(records, recordBefore);
            if (!containsRecordAtOffset)
            {
                return false;
            }
            else if (record.store.getGroupField() != null && Ext.Array.findBy(this.view.features, function(item) { return item.ftype == "grouping" }) != null)
            {
                // using groups, we need to ignore items from different groups 
                var groups = [];
                for (var i = 0; i < records.length; i++)
                {
                    groups.push(records[i].get(record.store.getGroupField()));
                }
                
                var targetGroup = record.get(record.store.getGroupField());
                
                return Ext.Array.contains(groups, targetGroup);
            }
            else
            {
                return true;            
            }
        }
    });
    
    Ext.override(Ext.ux.IFrame, {
        // Fix for CLS-6366 https://www.sencha.com/forum/showthread.php?304867-D-n-D-over-an-IFrame-issue
        onLoad: function()
        {
            var doc = this.getDoc();

            if (doc) 
            {
                var extdoc = Ext.get(doc); 
                
                extdoc._getPublisher('mousemove').directEvents.mousemove = 1;
                extdoc._getPublisher('mousedown').directEvents.mousedown = 1;
                extdoc._getPublisher('mouseup').directEvents.mouseup = 1;
                extdoc._getPublisher('click').directEvents.click = 1;
                extdoc._getPublisher('dblclick').directEvents.dblclick = 1;
            }   
            
            this.callParent(arguments);
            
            if (doc)
            {
                extdoc._getPublisher('mousemove').directEvents.mousemove = 0;
                extdoc._getPublisher('mousedown').directEvents.mousedown = 0;
                extdoc._getPublisher('mouseup').directEvents.mouseup = 0;
                extdoc._getPublisher('click').directEvents.click = 0;
                extdoc._getPublisher('dblclick').directEvents.dblclick = 0;
            }
        }
    });
    
    Ext.override(Ext.form.field.Tag, {
        // https://www.sencha.com/forum/showthread.php?305020-6.0.1-Tag-field-with-mulstiSelect-false&p=1114702#post1114702
        setValue: function(value, /* private */ add, skipLoad) 
        {
            var me = this,
                valueStore = me.valueStore,
                valueField = me.valueField,
                unknownValues = [],
                store = me.store,
                autoLoadOnValue = me.autoLoadOnValue,
                isLoaded = store.getCount() > 0 || store.isLoaded(),
                pendingLoad = store.hasPendingLoad(),
                unloaded = autoLoadOnValue && !isLoaded && !pendingLoad,
                record, len, i, valueRecord, cls, params;
    
            if (Ext.isEmpty(value)) {
                value = null;
            } else if (Ext.isString(value) && me.multiSelect) {
                value = value.split(me.delimiter);
            } else {
                value = Ext.Array.from(value, true);
            }
    
            if (value && me.queryMode === 'remote' && !store.isEmptyStore && skipLoad !== true && unloaded) {
                for (i = 0, len = value.length; i < len; i++) {
                    record = value[i];
                    if (!record || !record.isModel) {
                        valueRecord = valueStore.findExact(valueField, record);
                        if (valueRecord > -1) {
                            value[i] = valueStore.getAt(valueRecord);
                        } else {
                            valueRecord = me.findRecord(valueField, record);
                            if (!valueRecord) {
                                if (me.forceSelection) {
                                    unknownValues.push(record);
                                } else {
                                    valueRecord = {};
                                    valueRecord[me.valueField] = record;
                                    valueRecord[me.displayField] = record;
    
                                    cls = me.valueStore.getModel();
                                    valueRecord = new cls(valueRecord);
                                }
                            }
                            if (valueRecord) {
                                value[i] = valueRecord;
                            }
                        }
                    }
                }
    
                if (unknownValues.length) {
                    params = {};
                    params[me.valueParam || me.valueField] = unknownValues.join(me.delimiter);
                    store.load({
                        params: params,
                        callback: function() {
                            me.setValue(value, add, true);
                            me.autoSize();
                            me.lastQuery = false;
                        }
                    });
                    return false;
                }
            }
    
            // For single-select boxes, use the last good (formal record) value if possible
            if (!me.multiSelect && Ext.isArray(value) && value.length > 0) { // Ametys fix is here
                for (i = value.length - 1; i >= 0; i--) {
                    if (value[i].isModel) {
                        value = value[i];
                        break;
                    }
                }
                if (Ext.isArray(value)) {
                    value = value[value.length - 1];
                }
            }
    
            return me.callSuper([value, add]);
        }
    });
    
	Ext.override(Ext.data.Model, {
		// Fix for https://issues.ametys.org/browse/CMS-6363
		// Actually, this enables to specify a convert or calculate function for an id field in a Ext.data.Model (which does not work, is it a bug ?)
		// See https://www.sencha.com/forum/showthread.php?292044-Ext.data.Field.convert%28%29-not-called-for-idField-if-only-calculated
		
	    privates: {
	        statics: {
	            initFields: function (data, cls, proto) {
	                var me = this,
	                    idField;
	
	                me.callParent(arguments);
	
	                idField = proto.idField;
	                idField.defaultValue = (idField.convert) ? undefined : null; // defaultValue must be undefined instead of null if a convert function is specified
	            }
	        }
	    }
	});
})();