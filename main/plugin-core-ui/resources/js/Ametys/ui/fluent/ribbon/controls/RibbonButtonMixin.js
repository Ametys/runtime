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
    "Ametys.ui.fluent.ribbon.controls.RibbonButtonMixin", 
    {
        /**
         * @cfg {String} scale The size of the button. Can be 'large', 'small' or 'very-small'.
         * 'large' buttons have to be used directly in a Group. Icon size is 32x32.
         * 'small' buttons have to be used in a GroupPart. Icon size is 16x16.
         * 'very-small' buttons have to be used in a GroupPart or in a Toolbar. Icon size is 16x16.
         */
        
        /**
         * @cfg {String} iconAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
        /**
         * @cfg {String} arrowAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
        /**
         * @cfg {Number} height Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
        /**
         * @cfg {Number} width Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
         
        /**
         * @property {String} loadingCls The CSS classname for buttons that are in "loading" state 
         * @private
         * @readonly
         */
        loadingCls: 'a-fluent-loading',
        
        /**
         * @private
         * @readonly
         * @property {String} buttonWithMenuCls The CSS classname for buttons with a menu
         */
        buttonWithMenuCls: 'a-fluent-control-button-menu',
        
        /**
         * @property {Number} The minimum width of the button in pixels
         * @private
         * @readonly
         */
        largeButtonMinWidth: 32,
            
        /**
         * @property {String} _rawText The text set, not modified. While the property text can have breakline or stuffs like this.
         * @private
         */
         
        /**
         * Check the button configuration
         * @param {Object} config The config
         */
        constructor: function(config)
        {
            if (config.scale == 'very-small')
            {
                config.text = null;
                config.scale = 'small';
                config.iconAlign = 'left';
                config.arrowAlign = 'right';
            }
            else if (config.scale == 'small')
            {
                config.iconAlign = 'left';
                config.arrowAlign = 'right';
            }
            else
            {
                config.scale = 'large';
                config.iconAlign = 'top';
                config.arrowAlign = 'bottom';
            }
            
            if (config.menu)
            {
                config.cls = Ext.Array.from(config.cls);
                config.cls.push(this.buttonWithMenuCls);
                
                config.menu.ui = "ribbon-menu";
            }
        },

        /**
         * Get the button text
         * @return {String} The set text
         */
        getText: function()
        {
            return this._rawText;
        },
        
        /**
         * Set the button text
         * @param {String} text The text to set
         */
        setText: function(text)
        {
            this._rawText = text;
            this.text = text || '';
    
            if(this.el)
            {
                if (this.scale == 'large')
                {
                    var textMesurer = Ext.create("Ext.util.TextMetrics", this.btnInnerEl);
    
                    var minWidth = textMesurer.getWidth(this.text);
                    var minText = this.text;
                    var secondLineText = "";
                    
                    var menuOffset1stline = this.menu ? "<br/><div class='arrow'></div>" : "";
                    var menuOffset2ndline = this.menu ? "<div class='arrow arrow-with-text'></div>" : "";
    
                    var nextIndex = this.text.indexOf(' ', 0);
                    while (nextIndex != -1 && nextIndex < this.text.length)
                    {
                        var tmpSecondLineText = this.text.substring(nextIndex + 1) + menuOffset2ndline;
                        var testText = this.text.substring(0, nextIndex) + '<br/>' + tmpSecondLineText;
                        var testWidth = textMesurer.getWidth(testText);
                        
                        if (testWidth < minWidth)
                        {
                            minWidth = testWidth;
                            minText = testText;
                            secondLineText = tmpSecondLineText;
                        }
                        
                        nextIndex = this.text.indexOf(' ', nextIndex + 1);
                    }
                    
                    while (minWidth < this.largeButtonMinWidth)
                    {
                        var brpos = minText.indexOf('<br/>'); 
                        if (brpos == -1)
                        {
                            minText = "&#160;" + minText + "&#160;";
                        }
                        else
                        {
                            minText = "&#160;" + minText.substring(0, brpos) + "&#160;" + "<br/>" + "&#160;" + minText.substring(brpos + 5) + "&#160;";
                        }
                        minWidth = textMesurer.getWidth(minText);
                    }
                    
                    if (minText.indexOf('<br/>') == -1)
                    {
                        // Text on one line, insert the menu code
                        minText += menuOffset1stline;
                    }
                    
                    this.text = minText;
                }
                else if (this.text == null)
                {
                    this.text = '&#160;';

                    this.el.addCls("x-btn-very-small");
                }
                
                this.btnInnerEl.update(this.text || '&#160;');
            }
        },
        
        /**
         * Set the button in a refreshing state. See #stopRefreshing
         */
        refreshing: function ()
        {
            this.addCls(this.loadingCls);
        },
    
        /**
         * Stop the refreshing state. See #stopRefreshing
         */
        stopRefreshing: function ()
        {
            this.removeCls(this.loadingCls);
        }    
    }
);
