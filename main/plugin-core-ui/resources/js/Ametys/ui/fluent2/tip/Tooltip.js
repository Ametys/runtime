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
 *          footertext: 'Press F1 to have help', 
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
            helpText: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLTIP_FOOTER_TEXT'/>",
            
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
                '<tpl if="image">',
                '<div class="{tipCls} {tipCls}-withimage">',
                '<tpl else>',
                '<div class="{tipCls}">',
                '</tpl>',
                    '<div class="{tipCls}-text">',
                        '<tpl if="image">',
                            '<span class="{tipCls}-img" style="height: {imageHeight + 2}px; width: {imageWidth + 2}px; background-image: url(\'{image}\'); display: inline-block;"></span>',
                        '</tpl>',
                        '{text}',
                    '</div>',
                    '<tpl if="helpId">',
                        '<div class="{tipCls}-footer">{helpText}</div>',
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
             * @param {String} config.image An optional image path to display a main image in the tooltip.
             * @param {String} [config.imageHeight=48] Height, in pixels, of the image above.
             * @param {String} [config.imageWidth=48] Width, in pixels, of the image above.
             * @param {String} config.text The main text of the tooltip. Can contains html tags
             * @param {String} [config.helpId] The optionnal help identifier that is linked to an url to be displayed in the help too.
             * @param {Boolean} config.inribbon Is the tooltip applying for a component of the ribbon? Default to true. It does matter to vertically align the tooltip to the ribbon.
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
                            
                        showDelay: 900,
                        dismissDelay: config.dismissDelay ? config.dismissDelay : 20000,
                        helpId: config.helpId, // can be undefined
                        
                        width: config.width ? config.width : (config.image ? this.imagedTooltipWidth : this.tooltipWidth),
                                
                        ribbon: config.inribbon == false ? false : true
                    };
                }
                else
                {
                    return null;
                }
            }
        },
        
        register: function(config)
        {
            if (config.helpId || config.image || config.inribbon)
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
                
                // required position
                newX = target.getLeft() - 5;
                
                var rootOfTarget = target;
                newY = target.parent("body > *").getBottom();
                
                newX = Math.max(newX, 3);
                newX = Math.min(newX, Ext.getBody().getRight() - this.getWidth());
                
                newY = Math.min(newY, Ext.getBody().getBottom() - this.getHeight());
                
                var currentXY = tip.getPosition();
                if (currentXY[0] != newX || currentXY[1] != newY)
                {
                    tip.setPagePosition(newX, newY);
                }
            }
        }
    }
);

Ext.tip.QuickTipManager.init(null, {xtype: 'ametys.quicktip'});
