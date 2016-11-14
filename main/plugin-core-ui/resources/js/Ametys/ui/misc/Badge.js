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

/*
 * Freely inspired from Brian Wendt https://github.com/whiskeredwonder/Ext.ux.mixin.Badge (under MIT Licence)
 */
 
/**
 * Add a badge on buttons
 */
Ext.define('Ametys.ui.misc.Badge', {
    extend : 'Ext.Mixin',
    
    /**
     * @private
     * @property {Object} mixinConfig The mixin configuration
     */
    mixinConfig : {
        id : 'badge',
        after : {
            onRender : 'renderBadgeText'
        }
    },
    
    config : {
        /**
         * @cfg {String} badgeText A text that appear as a badge on mixed component
         */
        badgeText : null
    },

    /**
     * @private
     * Update the badget text
     */
    renderBadgeText : function() {
        var badgeText = this.getBadgeText();
        
        if (badgeText) 
        {
            this.updateBadgeText(badgeText);
        }
    },
    
    /**
     * @private
     * Set the badge text
     * @param {String} badgeText The new badge text
     * @param {String} oldBadgeText The old badge text. Can be undefined.
     */
    updateBadgeText : function(badgeText, oldBadgeText) {
        var me = this,
            el = me.el;
        
        if (me.rendered) 
        {
            el.set({ 'data-abp-badge' : badgeText });
            
            el.toggleCls('abp-badge', !!badgeText);
            
            /**
             * @event badgetextchange
             * Fired when the badge text is changed
             * @param {String} newBadgeText The new badge text
             * @param {String} oldBadgeText The old badge text. Can be undefined.
             */
            me.fireEvent('badgetextchange', me, badgeText, oldBadgeText);
        }
    }
}, function(BadgeMixin) {
    Ext.override(Ext.button.Button, {
        mixins : [BadgeMixin]
    });
    Ext.override(Ext.panel.Tool, {
        mixins : [BadgeMixin]
    });
});
