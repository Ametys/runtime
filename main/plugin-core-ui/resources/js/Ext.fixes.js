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
        // Fix for CMS-6366 https://www.sencha.com/forum/showthread.php?304867-D-n-D-over-an-IFrame-issue
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
    
    
    Ext.override(Ext.layout.container.Box, {
        // Fix for https://www.sencha.com/forum/showthread.php?307214-BoxLayout-and-split-configuration&p=1122342#post1122342
        onAdd: function (item, index) {
            var me = this,
                // Buttons will gain a split param
                split = me.enableSplitters && item.split && !item.isButton;
    
            me.callSuper(arguments);
    
            if (split) {
                if (item.split === true) {
                    split = {
                        collapseTarget: 'next'
                    };
                } else if (Ext.isString(item.split)) {
                    split = {
                        collapseTarget: item.split === 'before' ? 'next' : 'prev'
                    };
                } else {
                    split = Ext.apply({
                        collapseTarget: item.split.side === 'before' ? 'next' : 'prev'
                    }, item.split);
                }
    
                me.insertSplitter(item, index, !!item.hidden, split);
            }
        }    
    });
    
    Ext.override(Ext.form.field.Base, {
        // Fix for https://issues.ametys.org/browse/RUNTIME-1858
        // See https://www.sencha.com/forum/showthread.php?311209-Autocomplete-with-Chrome&p=1136279#post1136279
        getSubTplMarkup: function(fieldDate)
        {
            var value = this.callParent(arguments);
            value = value.replace('autocomplete="off"', 'autocomplete="new-password"');
            return value;
        }
    });
    
    Ext.override(Ext.form.field.Date, {
        // Fix for https://issues.ametys.org/browse/RUNTIME-1872
        // See https://www.sencha.com/forum/showthread.php?310219
        // Will be fixed in 6.0.3
        onBlur: function(e) {
            var me = this,
                v = me.rawToValue(me.getRawValue());
            
            if (Ext.isDate(v) || v === null) {
                me.setValue(v);
            }
            me.callParent([e]);
        }
    });

    // RUNTIME-1864 When the browser (mainly FF) is slow down with bgi errorful pagetools, the drag is stick to the mouse
    Ext.getDoc().on({
        // This is already registered but with a -1000 priority that seems to be the issue.
        mouseup: Ext.dd.DragDropManager.handleMouseUp,
        capture: true,
        scope: Ext.dd.DragDropManager
    });
    
    /**
     * @member Ext.app.ViewController
     * @method afterRender
     * After render
     */
    
    /**
     * @member Ext.util.Floating
     * @event tofront
     * When bring to front
     */
    
    /**
     * @member Ext.data.proxy.Server
     * @event beginprocessresponse
     * When starting to process answer
     * @param {Object} response The response
     * @param {Object} operation The running operation
     */
    /**
     * @member Ext.data.proxy.Server
     * @event endprocessresponse
     * When starting to process answer
     * @param {Object} response The response
     * @param {Object} operation The running operation
     */
})();
