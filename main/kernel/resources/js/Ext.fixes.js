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
})();