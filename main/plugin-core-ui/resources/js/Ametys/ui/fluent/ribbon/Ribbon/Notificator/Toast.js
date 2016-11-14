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
Ext.define("Ametys.ui.fluent.ribbon.Ribbon.Notificator.Toast", {
    extend: "Ext.window.Toast",
    
    /**
     * @cfg {Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification} notification (required) The notification to display
     */
    /**
     * @private
     * @property {Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification} _notification See #cfg-notification
     */
    /**
     * @private
     * @property {Boolean} _autoClosed True in #close if auto closed
     */
    autoClosed: false,
    
    /**
     * @cfg {String} ui=ribbon-toast @inheritdoc
     */
    ui: 'ribbon-toast',
    
    /**
     * @private
     * @readonly
     * @property {Number} toastWidth The width of the toast
     */
    toastWidth: 300,
    
    config: {
        /**
         * @cfg {String} endTarget The id of the target where the toast will goes back at the end
         */
        endTarget: null
    },
    
    constructor: function(config)
    {
        this.notification = config.notification;
        
        this.notification.set('displayed', true);
        this.notification.commit();

        config.title = config.title || this.notification.get('title');
        config.html = config.html || this.notification.get('description');
        config.width = config.width || this.toastWidth;
        
        if (this.notification.get('type') == 'error' || this.notification.get('type') == 'warn')
        {
            config.ui = this.ui + "-" + this.notification.get('type');
        }
        
        
        this.callParent(arguments);
        
        this.on('close', this._onClose, this);
    },
    
    /**
     * @property {String} slideInAnimtation Override the slide in animation
     * @private
     */
    slideInAnimtation: 'backIn',
    
    /**
     * Listener on close
     */
    _onClose: function()
    {
        this.notification.set('displayed', false);
        this.notification.set('read', !this._autoClose);
        this.notification.commit();
        this.notification = null;
    },
    
    /**
     * @inheritdoc
     */
    doAutoClose: function()
    {
        this._autoClose = true;
        this.callParent(arguments);
    },
    
    beforeShow: function()
    {
        this.callParent(arguments);
        this.el.setOpacity(0);
    },
    
    // Override to display a specific animation
    afterShow: function () 
    {
        var me = this,
            el = me.el,
            activeToasts, sibling, length, xy;

        Ext.window.Toast.superclass.afterShow.apply(this, arguments);

        activeToasts = me.getToasts();
        length = activeToasts.length;
        sibling = length && activeToasts[length - 1];

        if (sibling) {
            el.alignTo(sibling.el, me.siblingAlignment, [0, 0]);

            me.xPos = me.getXposAlignedToSibling(sibling);
            me.yPos = me.getYposAlignedToSibling(sibling);
        }
        else {
            el.alignTo(me.anchor.el, me.anchorAlign,
                            [ (me.paddingX * me.paddingFactorX),
                              (me.paddingY * me.paddingFactorY) ], false);

            me.xPos = me.getXposAlignedToAnchor();
            me.yPos = me.getYposAlignedToAnchor();
        }

        Ext.Array.include(activeToasts, me);

        if (me.animate) {
            // Repeating from coordinates makes sure the windows does not flicker
            // into the center of the viewport during animation
            xy = el.getXY();
            el.animate({
                from: {
                    x: xy[0],
                    y: xy[1],
                    opacity: 0
                },
                to: {
                    x: me.xPos,
                    y: me.yPos,
                    opacity: 1
                },
                easing: me.slideInAnimation,
                duration: me.slideInDuration,
                dynamic: true,
                callback: me.afterPositioned,
                scope: me
            });
        }
        else {
            me.setLocalXY(me.xPos, me.yPos);
            me.afterPositioned();
        }
    },
    
    hide: function () {
        var me = this,
            el = me.el;

        me.cancelAutoClose();

        if (me.isHiding) {
            if (!me.isFading) {
                Ext.window.Toast.superclass.hide.apply(this, arguments);
                // Must come after callParent() since it will pass through hide() again triggered by destroy()
                me.removeFromAnchor();
                me.isHiding = false;
            }
        }
        else {
            // Must be set right away in case of double clicks on the close button
            me.isHiding = true;
            me.isFading = true;

            me.cancelAutoClose();

            if (el) {
                if (me.animate) {
                    var endTarget = Ext.get(this.getEndTarget());
                    el.animate({
                        easing: 'easeIn',
                        duration: me.hideDuration,
                        listeners: {
                            afteranimate: function () {
                                me.isFading = false;
                                me.hide(me.animateTarget, me.doClose, me);
                            }
                        },
                        to: {
                            x: endTarget.getLeft(),
                            y: endTarget.getTop(),
                            width: endTarget.getWidth(),
                            height: endTarget.getHeight(),
                            opacity: 0.1
                        }
                    });
                }
                else {
                    me.isFading = false;
                    me.hide(me.animateTarget, me.doClose, me);
                }
            }
        }

        return me;
    }
});
