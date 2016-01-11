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
 * This class handle the ribbon tooltip in two ways.
 * First you can create such a tooltip by getting the required configuration using the static method #create this way:
 * 
 *      tooltip: {
 *          title: 'My Ametys Tooltip', 
 *          image: 'resources/img/ametys.gif', 
 *          text: 'Click on this button to get access to some features', 
 *          helpId: '12', 
 *          inribbon: true
 *      }
 * 
 * Secondly, this class automatically initialize the tooltip manager by using this class to handle correctly the "inribbon" tooltip position 
 */
Ext.define(
    "Ametys.ui.fluent.tip.Tooltip",
    {
        extend: "Ext.tip.QuickTip",
        alias: 'widget.ametys.quicktip',
        
        /**
         * @cfg {String} [glyphIcon] The CSS class for glyph to use as the icon. This is an alternative to the set of icons.
         */
        
        /**
         * @cfg {String} [iconDecorator] The CSS class to use as decorator above the main icon.
         */
        
        statics: {
            
            /**
             * @property {String} tipCls The CSS classname for tooltips.
             * @readonly
             * @private
             */
            tipCls: 'x-fluent-tooltip',
            
            /**
             * @property {String} helpText The generic help text at the bottom of tips with "helpId"
             * @readonly
             * @private
             */
            helpText: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLTIP_FOOTER_TEXT'>See help</i18n:text>",
            
            /**
             * @property {Number} tooltipWidth The width of the tooltip if the tooltip doest not have an image
             * @readonly
             * @private
             */
            tooltipWidth: 210,
            /**
             * @property {Number} imagedTooltipWidth The width of the tooltip if the tooltip has an image
             * @readonly
             * @private
             */
            imagedTooltipWidth: 318,
            
            /**
             * @property {Ext.Template} tipTemplate The template for tooltips
             * @readonly
             * @private
             */
            tipTemplate: Ext.create('Ext.XTemplate',
            	'<tpl if="glyphIcon">',
            	'<div class="{tipCls}-wrapper {tipCls}-withglyph">',
                '<tpl elseif="image">',
                '<div class="{tipCls}-wrapper {tipCls}-withimage">',
                '<tpl else>',
                '<div class="{tipCls}-wrapper">',
                '</tpl>',
                    '<div class="{tipCls}-text">',
                        '<tpl if="image">',
                            '<span class="{tipCls}-img" style="height: {imageHeight + 2}px; width: {imageWidth + 2}px; background-image: url(\'{image}\'); display: inline-block;"></span>',
                        '</tpl>',
                        '<tpl if="glyphIcon">',
                        	'<span class="{tipCls}-glyph {glyphIcon} size-{imageHeight} {iconDecorator} " style="font-size: {imageHeight}px; line-height: {imageHeight}px; height: {imageHeight + 2}px; width: {imageWidth + 2}px; display: inline-block;"></span>',
                        '</tpl>',
                        '{text}',
                    '</div>',
                    '<tpl if="helpId">',
                        '<div class="{tipCls}-footer"><a href="#{helpId}" onclick="Ametys.tool.ToolsManager.openTool(\'uitool-help\', {helpId: \'{helpId}\'}); return false;">{helpText}</a></div>',
                    '</tpl>',
                '</div>',
                { compiled: true }
            ),
            
            /**
             * Create a config object for tooltip looking ribbon style.
             * This method is automatically called if the config of a registered tip has an image, an help id or as inribbon specified
             * @param {Object} config A config object
             * @param {String} config.title The title in the tooltip
             * @param {Number} [config.width] An optional width in pixel to override the defaults.
             * @param {String} [config.image] An optional image path to display a main image in the tooltip.
             * @param {String} [config.imageHeight=48] Height, in pixels, of the image above.
             * @param {String} [config.imageWidth=48] Width, in pixels, of the image above.
             * @param {String} config.text The main text of the tooltip. Can contains html tags
             * @param {String} [config.helpId] The optionnal help identifier that is linked to an url to be displayed in the help too.
             * @param {Number} [config.dismissDelay=20000] The time before the tooltip automatically disapear is the mouse stay over the element
             * @param {HTMLElement/Ext.dom.Element/String} [config.target] The target element or string id to monitor for mouseover events to trigger showing this ToolTip.
             * @param {Boolean} [config.inribbon=true] Is the tooltip applying for a component of the ribbon? Default to true. It does matter to vertically align the tooltip to the ribbon.
             * @return {Object} A configuration for tooltip
             */
            create: function(config)
            {
                if (config)
                {
                    config.cls = Ext.Array.from(config.cls);
                    config.cls.push(this.tipCls);
                    
                    Ext.applyIf(config, {
                        imageHeight: 48,
                        imageWidth: 48,
                        text: '',
                        tipCls: this.tipCls,
                        helpText: this.helpText
                    })
                    
                    return {
                        cls: config.cls,
                        title: config.title,
                        text: this.tipTemplate.apply(config),
                            
                        target: config.target,
                        showDelay: 900,
                        dismissDelay: config.dismissDelay ? config.dismissDelay : 20000,
                        helpId: config.helpId, // can be undefined
                        
                        width: config.width ? config.width : (config.image || config.glyphIcon ? this.imagedTooltipWidth : this.tooltipWidth),
                                
                        ribbon: config.inribbon == false ? false : true
                    };
                }
                else
                {
                    return null;
                }
            }
        },
        
        constructor: function()
        {
            this.callParent(arguments);
            
            this.on('mouseover', this._onMouseOver, this, { element: 'el'});
            this.on('mouseout', this._onMouseOut, this, { element: 'el'});
        },
        
        /**
         * @private
         * The mouseover listener to stop autohide process for inribbon tips
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @param {HTMLElement} t The target of the event.
         */
        _onMouseOver: function(e, t)
        {
            if (this.lastActiveTarget && this.lastActiveTarget.helpId)
            {
                this.clearTimer('hide');
                this.clearTimer('dismiss');    
            }
        },
        
        /**
         * @private
         * The mouseout listener to restart autohide process for inribbon tips
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @param {HTMLElement} t The target of the event.
         */
        _onMouseOut: function(e, t)
        {
            if (this.lastActiveTarget && this.lastActiveTarget.helpId)
            {
                this.clearTimer('show');
                if (this.autoHide !== false) 
                {
                    this.delayHide();
                }
            }
        },       
        
        onTargetOut: function(e)
        {
            if (this.activeTarget)
            {
                this.lastActiveTarget = this.activeTarget;
            }
            this.callParent(arguments);
        },
        
        hide: function()
        {
            delete this.lastActiveTarget;
            this.callParent(arguments);
        },
        
        register: function(config)
        {
            if (config.helpId || config.image || config.inribbon || config.glyphIcon)
            {
                Ext.apply(config, this.self.create(config));
            }
            
            this.callParent([config]);
        },
    
        onPosition: function(x, y) 
        {
            this.callParent(arguments);

            var tip = this;
            
            
            if (tip.activeTarget.ribbon)
            {
                var target = Ext.get(tip.activeTarget.target);
                var newX, newY;
                
                var parent = target.parent("body > *[id^=viewport] > *[id^=viewport] > *");
                if (!parent)
                {
                    parent = target.parent("body > *");
                }
                
                newX = target.getLeft() - 5;
                newY = parent.getBottom();
                
                newX = Math.max(newX, 3);
                newX = Math.min(newX, Ext.getBody().getRight() - this.getWidth());
                
                newY = Math.min(newY, Ext.getBody().getBottom() - this.getHeight());
                
                var currentXY = tip.getPosition();
                if (currentXY[0] != newX || currentXY[1] != newY)
                {
                    tip.setPagePosition(newX, newY);
                }
            }
            else
            {
                var target = Ext.get(tip.activeTarget.target);
                var newX, newY;
                
                // required position
                var parent;
                if (target && (parent = target.parent(".x-menu")))
                {
                    var parentMenu = parent;
                    newX = parentMenu.getRight() + 2;
                    newY = target.getTop() - 2;
                    
                    if (newX + this.getWidth() > Ext.getBody().getRight())
                    {
                        newX = parentMenu.getLeft() - 2 - this.getWidth();
                    }
                
                    var currentXY = tip.getPosition();
                    if (currentXY[0] != newX || currentXY[1] != newY)
                    {
                        tip.setPagePosition(newX, newY);
                    }
                }
                else if (target && target.is(".x-tab"))
                {
                    if (target.is(".x-tab-top"))
                    {
                        newX = target.getLeft() - 1;
                        newY = target.getBottom() ;
                    }
                    else if (target.is(".x-tab-left"))
                    {
                        newX = target.getRight() + 1;
                        newY = target.getTop() - 1 ;
                    }
                    else if (target.is(".x-tab-right"))
                    {
                        newX = target.getLeft() - 1 - this.getWidth();
                        newY = target.getTop() - 1 ;
                    }
                    else if (target.is(".x-tab-bottom"))
                    {
                        newX = target.getLeft() - 1;
                        newY = target.getTop() - 1  - this.getHeight();
                    }
                
                    var currentXY = tip.getPosition();
                    if (currentXY[0] != newX || currentXY[1] != newY)
                    {
                        tip.setPagePosition(newX, newY);
                    }
                }
            }
        }
    }
);

Ext.tip.QuickTipManager.init(null, {xtype: 'ametys.quicktip'});
