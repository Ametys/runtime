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
 * This class is a container for very small buttons that goes together visually
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.controls.Button",
    {
        extend: 'Ext.button.Button',
        alias: 'widget.ametys.ribbon-button',
        mixins: { button: 'Ametys.ui.fluent.ribbon.controls.RibbonButtonMixin' },
    
        /**
         * @cfg {String} ui=ribbon-component @inheritdoc
         */
        ui: 'ribbon-component',
        
        constructor: function(config)
        {
            config = config || {};
            
            this.mixins.button.constructor.call(this, config);
            
            this.callParent(arguments);
            
            this.getText = this.mixins.button.getText;
            this.setText = this.mixins.button.setText;
        },
        
        afterRender: function()
        {
            this.callParent(arguments);
            
            this.setText(this.text);
        }
    }
 );
 