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
 * This class is split button to set in the ribbon
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.controls.SplitButton",
    {
        extend: 'Ext.button.Split',
        alias: 'widget.ametys.ribbon-splitbutton',
        mixins: { button: 'Ametys.ui.fluent.ribbon.controls.RibbonButtonMixin' },
    
        /**
         * @readonly
         * @private
         * @property {String} splitCls The CSS classname to set on split buttons
         */
        splitCls: 'a-fluent-control-splitbutton',
        
        constructor: function(config)
        {
            config = config || {};
            
            this.mixins.button.constructor.call(this, config);
            
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this.splitCls);
            
            this.callParent(arguments);
            
            this.on("mousemove", this._onMouseMove, this, { element: 'el' });
        },
        
        initComponent: function()
        {
            // Avoid the error message about WAI that seems to be wrong
            // will be fixed in 6.0.1 https://www.sencha.com/forum/showthread.php?303438-Error-menu-button-behavior-will-conflict-with-toggling
            // so when in 6.0.1 this method can be removed: test it by creating a split button and check that there is no WAI error message in logs 
            var currentVal = Ext.enableAriaButtons; 
            Ext.enableAriaButtons = false;  
            this.callParent(arguments);
            Ext.enableAriaButtons = currentVal;  
        },
        
        afterRender: function()
        {
            this.callParent(arguments);
            
            this.setText(this.text);
        },
        
        /**
         * Listener on mouse move over the button, to determine if the mouse if over the button or the arrow 
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @private
         */
        _onMouseMove: function(e)
        {
            if (this.isWithinTrigger(e))
            {
                this.el.addCls(this.splitCls + "-menu");
            }
            else
            {
                this.el.removeCls(this.splitCls + "-menu");
            }
        },
        
        getTriggerRegion: function() 
        {
            if (this.scale == 'large')
            {
                var me = this,
                    region = me._triggerRegion,
                    btnRegion = me.getRegion();
    
                region.begin = me.btnInnerEl.getY() - btnRegion.top;
                region.end = btnRegion.bottom - btnRegion.top;
                return region;
            }
            else
            {
                return this.callParent(arguments);
            }
        }        
    }
 );
 