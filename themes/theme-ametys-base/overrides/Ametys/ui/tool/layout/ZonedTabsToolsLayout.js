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
 
(function () {
    function setWidth(button)
    {
        button.setMaxWidth(70 + Ext.create("Ext.util.TextMetrics", button.btnInnerEl).getWidth(button.text));
    }

    function setHeight(button)
    {
        button.setMaxHeight(42 + Ext.create("Ext.util.TextMetrics", button.btnInnerEl).getHeight("a") * 2);
    }
    
    function setText(button)
    {
        button._rawText = button.getText();
        
        button.getText = function()
        {
            return this._rawText;
        }
        
        button.setText = function(text)
        {
            
            var oldText = this._rawText;
            this._rawText = text;
            this.text = text;
            
            // split text
                    var textMesurer = Ext.create("Ext.util.TextMetrics", this.btnInnerEl);
    
                    var minText = this.text;
                    var minWidth = textMesurer.getWidth(minText);
                    var secondLineText = "";
                    
                    var nextIndex = this.text.indexOf(' ', 0);
                    while (nextIndex != -1 && nextIndex < this.text.length)
                    {
                        var tmpSecondLineText = this.text.substring(nextIndex + 1);
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

            this.text = minText;
            this.btnInnerEl.setHtml(this.text);
            this.fireEvent('textchange', this, oldText, this._rawText);
        }
        
        button.setText(button.getText());
    }

    Ext.define('Ametys.theme.ametysbase.ui.tool.layout.ZonedTabsToolsLayout', {
        override: 'Ametys.ui.tool.layout.ZonedTabsToolsLayout',
       
        statics: {
             __ADDITIONNAL_ZONE_CONFIG_OTHER: {
                tabBar: {
                    defaults:{
                        flex: 1,
                        minWidth: 56,
                        textAlign: 'left',
                        
                        listeners: {
                            'afterrender': setWidth,
                            'textchange': setWidth
                        }
                    }
                }         
             },
             
             __ADDITIONNAL_ZONE_CONFIG_LEFT: {
                headerPosition: 'left', 
                tabPosition: 'left', 
                tabRotation: 0 ,
                
                tabBar: {
                    defaults: {
                        flex: 1,
                        minHeight: 56,
                        iconAlign: 'top',
                        textAlign: 'center',
                        
                        listeners: {
                            'afterrender': setText,
                            'textchange': setHeight
                        }
                    }
                }            
            },
             __ADDITIONNAL_ZONE_CONFIG_RIGHT: {
                headerPosition: 'right', 
                tabPosition: 'right', 
                tabRotation: 0 ,
                
                tabBar: {
                    defaults: {
                        flex: 1,
                        minHeight: 56,
                        iconAlign: 'top',
                        textAlign: 'center',
                        
                        listeners: {
                            'afterrender': setText,
                            'textchange': setHeight
                        }
                    }
                }            
            }
        }
    });
})();
