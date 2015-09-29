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
         * @cfg {String} ui=ribbon-button @inheritdoc
         */
        ui: 'ribbon-button',
        
        /**
         * @private
         * @readonly
         * @property {String} buttonWithMenuCls The CSS classname for split buttons
         */
        buttonSplitCls: 'a-fluent-control-button-split',
        
        /**
         * @readonly
         * @private
         * @property {String} splitCls The CSS classname to set on split buttons when the mouse is over the trigger part
         */
        splitOverTriggerCls: 'a-fluent-control-button-split-overtrigger',
        
        constructor: function(config)
        {
            config = config || {};
                    
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this.buttonSplitCls);
            
            this.mixins.button.constructor.call(this, config);
            
            this.callParent(arguments);
            
            this.on("menutriggerover", this._onMenuTriggerOver, this);
            this.on("menutriggerout", this._onMenuTriggerOut, this);
        },
        
        afterRender: function()
        {
            this.callParent(arguments);
            
            this.setText(this.text);
        },
        
        /**
         * Listener when mouse is over the trigger 
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @private
         */
        _onMenuTriggerOver: function(e)
        {
            this.el.addCls(this.splitOverTriggerCls);
        },

        /**
         * Listener when mouse is no more over the trigger 
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @private
         */
        _onMenuTriggerOut: function(e)
        {
            this.el.removeCls(this.splitOverTriggerCls);
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
 