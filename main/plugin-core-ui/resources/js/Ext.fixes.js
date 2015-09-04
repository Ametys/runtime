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
    
    Ext.define('Ametys.view.DropZone', {
        override: 'Ext.view.DropZone',
        
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
    
        
    Ext.define('Ametys.dom.Query', {
        override: 'Ext.dom.Query',
        
        // RUNTIME-1187 https://www.sencha.com/forum/showthread.php?301858-6.0.0.415-beta-Ext.dom.Query.selectValue-defaultValue-is-not-used&p=1103310#post1103310
        selectValue: function(path, root, defaultValue) 
        {
            var value = this.callParent(arguments);
            
            return value === undefined ? defaultValue : value;
        }
    });
    
    Ext.define("Ametys.ux.IFrame", {
        override: 'Ext.ux.IFrame',
        
        // Fix for CLS-6366 https://www.sencha.com/forum/showthread.php?304867-D-n-D-over-an-IFrame-issue
        onLoad: function()
        {
            var me = this,
                doc = me.getDoc(),
                fn = me.onRelayedEvent;

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
        }
    });
})();