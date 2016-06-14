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

/**
 * This class will handle the drag and drop process of tabs between zones
 * @private
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZonedTabsDD", 
    {
        extend: "Ext.dd.DDProxy",
        
        /**
         * @property {String} visibleZoneCls The CSS classname for the rendering of a drop zone
         * @readonly
         * @private
         */
        visibleZoneCls : 'a-tool-layer-zoned-dragndrop-marker',
        
        /**
         * @property {String} effectiveZoneCls The CSS classname for the effective location of a drop zone
         * @readonly
         * @private
         */
        effectiveZoneCls: 'a-tool-layer-zoned-dragndrop-effective',
        
        statics: 
        {
            /**
             * @readonly
             * @private
             * @property {Number} __FLOATINGZONES_RATIO The size to display for slided in lateral zones (ratio of central zone).
             */
            _FLOATINGZONES_RATIO: 0.3,
            /**
             * @readonly
             * @private
             * @property {Number} ___FLOATINGZONES_DROPRATIO The size to detect drop for slided in lateral zones (ratio of central zone).
             */
            _FLOATINGZONES_DROPRATIO: 0.25,
            /**
             * @readonly
             * @private
             * @property {Number} __HEADER_HEIGHT The size in pixels of a horizontal header of centrals zones to avoid overlap
             */
            _HEADER_HEIGHT: 40
        },
    
        /**
         * @property {String} _originalGroup The drag'n'drop group name used at creation time
         * @private
         */
    
        /**
         * @property {HTMLElement[]} _ddzones The array of html element used to display drop targets and other elements for the drop targets themselves.
         * @private
         */
        _ddzones: [],
        /**
         * @property {Ext.dd.DDTarget[]} __ddobjects The drop targets associated to #_ddzones (for destroy purposes).
         * @private
         */
        _ddobjects: [],
        
        /**
         * @property {Ext.dom.Element} el The dom element to drag
         * @private
         */
        /**
         * @property {Array} originalXY The x,y coordinates of the #el before the drag has started. Used to cancel the drag.
         * @private
         */     
         
        /**
         * @cfg {String} toolId The identifier of the Ametys.ui.tool.ToolPanel associated to the drag process 
         */      
        
        /**
         * @cfg {Ametys.ui.tool.ToolsLayout} toolsLayout (required) The current tools layout 
         */
    
        constructor: function(id, sGroup, config)
        {
            this._originalGroup = sGroup;
            this.toolsLayout = config.toolsLayout;
            
            this.callParent(arguments);
        },
        
        b4StartDrag : function(x, y) 
        {
            this.callParent(arguments);
            
            // Cache the drag element
            if (!this.el) 
            {
                this.el = Ext.get(this.getEl());
            }
         
            //Cache the original XY Coordinates of the element, we'll use this later.
            this.originalXY = this.el.getXY();
            
            // Which panels are visible? where are they?
            var visible = {}; // {String} location <-> {Boolean} isVisible
            var position = {}; // {String} location <-> {Boolean} getPosition (x,y position)
            var size = {}; // {String} location <-> {Boolean} getSize (width,height size)
            var dropOffset = {}; // {String} location <-> {Number[2]} the drop zone position relatively to the zone
            var dropSize = {}; // {String} location <-> {Object} the width and height of the drop zone inside the zone

            this.toolsLayout._slideInZone();
            
            for (var loc in this.toolsLayout._panelHierarchy)
            {
                var tabPanel = this.toolsLayout._panelHierarchy[loc];
            
                visible[loc] = tabPanel.isVisible();
                
                var p = tabPanel;
                if (!visible[loc])
                {
                    p = tabPanel.ownerCt || tabPanel.floatingOwnerCt;
                    while (!p.isVisible())
                    {
                        p = p.ownerCt;
                    }
                }
                
                position[loc] =  p.getPosition();
                size[loc] = p.getSize();
                
                if (visible[loc])
                {
                    // Add spliter to zone
                    var associatedSplitter = null;
                    if (loc == 'l' || loc == 't' || loc == 'cl')
                    {
                        associatedSplitter = tabPanel.nextSibling();
                    }
                    else if (loc == 'r' || loc == 'b')
                    {
                        associatedSplitter = tabPanel.previousSibling();
                    }
                    
                    if (associatedSplitter && associatedSplitter.isVisible()) // spliter can be invisible for a visible zone with "cl" if "cr" is not visible
                    {
                        spliterPosition = associatedSplitter.getPosition();
                        spliterSize = associatedSplitter.getSize();
                        
                        size[loc] = { 
                            width: Math.max(position[loc][0] + size[loc].width, spliterPosition[0] + spliterSize.width) - Math.min(position[loc][0], spliterPosition[0]),
                            height: Math.max(position[loc][1] + size[loc].height, spliterPosition[1] + spliterSize.height) - Math.min(position[loc][1], spliterPosition[1])
                        };
                        
                        position[loc] = [ 
                            Math.min(position[loc][0], spliterPosition[0]), 
                            Math.min(position[loc][1], spliterPosition[1]) 
                        ];
                    }
                }

                dropOffset[loc] = [0, 0];
                dropSize[loc] = {width: size[loc].width, height: size[loc].height};
            }
            
            // Handle top zone
            // When top zone is visible, nothing to do
            // But when top zone is invisible
            if (!visible['t'])
            {
                // look and drop width are set
                size['t'].height = Math.round(size['cl'].height * this.self._FLOATINGZONES_RATIO);
                dropSize['t'].height = 0; // we be set to max possible value later
                
                // drop position is negativized
                dropOffset['t'][1] -= dropSize['t'].height;
                
                // no impact on middle dropzones height
            }
            
            // Handle bottom zone
            // When bottom zone is visible, nothing to do
            // But when bottom zone is invisible
            if (!visible['b'])
            {
                var futureSize = Math.round(size['cl'].height * this.self._FLOATINGZONES_RATIO);
                var futureDropSize = Math.round(size['cl'].height * this.self._FLOATINGZONES_DROPRATIO);
                
                // drop vposition is set for a bottom align
                position['b'][1] += size['b'].height - futureSize;
                dropOffset['b'][1] += futureSize - futureDropSize;
                
                // look and drop height are set
                size['b'].height = futureSize;
                dropSize['b'].height = futureDropSize;

                // vmiddle dropzones height are reduced
                for (var loc in {"cl": null, "cr": null, "l": null, "r": null})
                {
                    dropSize[loc].height -= dropSize['b'].height;
                }
            }       
            
            // Handle left/right zone
            // When visible nothing to do (invisible bottom part overlap is already handled)
            // But when invisible
            for (var loc in {"l": null, "r": null})
            {
                if (!visible[loc])
                {
                    // on the right, right align the drop zone
                    if (loc == "r") 
                    {
                        position[loc][0] += Math.round(size[loc].width - size['c' + loc].width * this.self._FLOATINGZONES_RATIO);
                        dropOffset[loc][0] = Math.round(size['c' + loc].width * this.self._FLOATINGZONES_RATIO - size['c' + loc].width * this.self._FLOATINGZONES_DROPRATIO);
                    }
                    
                    // look and drop width are set
                    size[loc].width = Math.round(size['c' + loc].width * this.self._FLOATINGZONES_RATIO);
                    dropSize[loc].width = Math.round(size['c' + loc].width * this.self._FLOATINGZONES_DROPRATIO);
                    
                    // RUNTIME-1344 
                    dropSize[loc].height -= this.self._HEADER_HEIGHT;
                    dropOffset[loc][1] += this.self._HEADER_HEIGHT;
                }
            }       
            
            var zones = {"cl": null, "cr": null, "l": null, "r": null, "t": null, "b": null};
            
            // Handle central zones
            var flexCL = this.toolsLayout._panelHierarchy['cl'].flex || this.toolsLayout._panelHierarchy['cr'].flex || 1;
            var flexCR = this.toolsLayout._panelHierarchy['cr'].flex ||this.toolsLayout._panelHierarchy['cl'].flex || 1;
            
            if (visible['cl'] && !visible['cr'])
            {
                if (!visible['l'])
                {
                    // space for left zone
                    // RUNTIME-1344 dropSize['cl'].width -= dropSize['l'].width;
                    // RUNTIME-1344 dropOffset['cl'][0] += dropSize['l'].width;
                }
                
                if (!visible['r'])
                {
                    // space for right zone
                    // RUNTIME-1344 dropSize['cl'].width -= dropSize['r'].width;
                }
                
                // space for cr zone
                var centralWidth = dropSize['cl'].width; 
                dropSize['cl'].width = Math.round(0.66 * centralWidth);
                
                size['cr'].width = Math.round(size['cl'].width * flexCR / (flexCL + flexCR));
                position['cr'][0] += size['cl'].width - size['cr'].width;
                dropSize['cr'].width = centralWidth - dropSize['cl'].width;
                dropOffset['cr'][0] = size['cr'].width - dropSize['cr'].width;
                if (!visible['r'])
                {
                    // RUNTIME-1344 dropOffset['cr'][0] -= dropSize['r'].width;
                }
            }
            else if (!visible['cl'] && visible['cr'])
            {
                if (!visible['r'])
                {
                    // space for right zone
                    // RUNTIME-1344 dropSize['cr'].width -= dropSize['r'].width;
                }
                
                if (!visible['l'])
                {
                    // space for left zone
                    // RUNTIME-1344 dropSize['cr'].width -= dropSize['l'].width;
                    // RUNTIME-1344 dropOffset['cr'][0] += dropSize['l'].width;
                }
                
                // space for cl zone
                var centralWidth = dropSize['cr'].width; 
                dropSize['cr'].width = Math.round(0.66 * centralWidth);
                dropOffset['cr'][0] += centralWidth - dropSize['cr'].width;
                
                size['cl'].width = Math.round(size['cr'].width * flexCL / (flexCL + flexCR));
                dropSize['cl'].width = centralWidth - dropSize['cr'].width;
                if (!visible['l'])
                {
                    // RUNTIME-1344 dropOffset['cl'][0] += dropSize['l'].width;
                }
            }
            else if (visible['cl'] && visible['cr'])
            {
                if (!visible['l'])
                {
                    // RUNTIME-1344 dropOffset['cl'][0] += dropSize['l'].width;
                    // RUNTIME-1344 dropSize['cl'].width -= dropSize['l'].width;
                }
                
                if (!visible['r'])
                {
                    // RUNTIME-1344 dropSize['cr'].width -= dropSize['r'].width;
                }
            }
            else if (!visible['cl'] && !visible['cr'])
            {
                if (!visible['l'])
                {
                    // RUNTIME-1344 dropOffset['cl'][0] += dropSize['l'].width;
                    // RUNTIME-1344 dropSize['cl'].width -= dropSize['l'].width;
                }
                if (!visible['r'])
                {
                    // RUNTIME-1344 dropSize['cl'].width -= dropSize['r'].width;
                }
                delete zones.cr;
            }
            
            // Now, due to borders in the theme and due to splitters, we may have a few pixels between zones
            // Let's remove it
            
            // Vertically middle zones must stick to top and bottom zones
            for (var loc in {"l": null, "r": null, "cl": null, "cr": null})
            {
                if (loc == 'cl' || loc == 'cr' || visible[loc]) // left and right zones, when hidden are starting after the central header
                {
                    var topDiff = (position[loc][1] + dropOffset[loc][1]) - (position['t'][1] + dropOffset['t'][1] + dropSize['t'].height);
                    dropSize[loc].height += topDiff;
                    dropOffset[loc][1] -= topDiff;
                }
                
                var bottomDiff = (position['b'][1] + dropOffset['b'][1]) - (position[loc][1] + dropOffset[loc][1] + dropSize[loc].height);
                dropSize[loc].height += bottomDiff;
                
                console.info("Diff " + loc + " " + topDiff + " - " + bottomDiff)
            }
            
            // Horizontally middle zones must stick to left and right zones : issue with splitters when lateral zones are visible
            if (visible['l'])
            {
                var leftDiff = (position['cl'][0] + dropOffset['cl'][0]) - (position['l'][0] + dropOffset['l'][0] + dropSize['l'].width);
                dropOffset['cl'][0] -= leftDiff;
                dropSize['cl'].width += leftDiff;
            }
            if (visible['r'])
            {
                if (zones.cr !== undefined)
                {
                    var rightDiff = (position['r'][0] + dropOffset['r'][0]) - (position['cr'][0] + dropOffset['cr'][0] + dropSize['cr'].width);
                    dropSize['cr'].width += rightDiff;
                }
                else
                {
                    var rightDiff = (position['r'][0] + dropOffset['r'][0]) - (position['cl'][0] + dropOffset['cl'][0] + dropSize['cl'].width);
                    dropSize['cl'].width += rightDiff;
                }
            }

            var toStickTo = Ext.getBody().getRegion();
            // Top zone must stick to the top
            var topDiff = position['t'][1] + dropOffset['t'][1] - toStickTo.top;
            dropOffset['t'][1] -= topDiff;
            dropSize['t'].height += topDiff;
            // Bottom zone must stick to the bottom
            var bottomDiff = toStickTo.bottom - (position['b'][1] + dropOffset['b'][1] + dropSize['b'].height);
            dropSize['b'].height += bottomDiff;
            // Left zone must stick to the left
            var leftDiff = position['l'][0] + dropOffset['l'][0] - toStickTo.left;
            dropOffset['l'][0] -= leftDiff;
            dropSize['l'].width += leftDiff;
            // If left zone is not visible, cl zone must stick to the left too
            if (!visible['l'])
            {
                var leftDiff = position['cl'][0] + dropOffset['cl'][0] - toStickTo.left;
                dropOffset['cl'][0] -= leftDiff;
                dropSize['cl'].width += leftDiff;
            }
            // Right zone must stick to the right
            var rightDiff = toStickTo.right - (position['r'][0] + dropOffset['r'][0] + dropSize['r'].width);
            dropSize['r'].width += rightDiff;
            // If right zone is not visible, cr zone must stick to the right too
            if (!visible['r'])
            {
                var rightDiff = toStickTo.right - (position['cr'][0] + dropOffset['cr'][0] + dropSize['cr'].width);
                dropSize['cr'].width += rightDiff;
            }
            
            // Draw now
            for (var loc in zones)
            {
                var div = document.createElement("div");
                div.id = Ext.id();
                div.className = this.visibleZoneCls;
                div.style.top = position[loc][1] + "px";
                div.style.left = position[loc][0] + "px";
                div.style.width = size[loc].width + "px";
                div.style.height = size[loc].height + "px";
                document.body.appendChild(div);
                this._ddzones.push(div);

                var idiv = document.createElement("div");
                idiv.location = loc;
                idiv.displaydiv = div.id;
                idiv.className = this.effectiveZoneCls;
                idiv.style.top = (position[loc][1] + dropOffset[loc][1]) + "px";
                idiv.style.left = (position[loc][0] + dropOffset[loc][0]) + "px";
                idiv.style.width = dropSize[loc].width + "px";
                idiv.style.height = dropSize[loc].height + "px";
                document.body.appendChild(idiv);
                this._ddzones.push(idiv);
                
                this._ddobjects.push(Ext.create("Ext.dd.DDTarget", idiv, this._originalGroup));         
            }
        },
        
        onDragDrop: function(evtObj, targetElId) 
        {
            // Wrap the drop target element with Ext.Element
            var dropEl = Ext.get(targetElId);
         
            // Perform the node move only if the drag element's 
            // parent is not the same as the drop target
            if (this.el.dom.parentNode.id != targetElId) 
            {
                // Move the element
                var uiToolId = this.config.toolId;
                
                
                var t = Ext.get(targetElId);
                var newlocation = t.dom.location;
                
                // Defering the moves because the tab button have to be released before it is destroyed
                Ext.defer(this.toolsLayout.moveTool, 1, this.toolsLayout, [Ext.getCmp(uiToolId), newlocation]);

                // Remove the drag invitation
                this.onDragOut(evtObj, targetElId);
            }
            else {
                // This was an invalid drop, initiate a repair
                this.onInvalidDrop();
            }
        },
        
        onDragEnter : function(evtObj, targetElId) 
        {
            // Colorize the drag target
            Ext.get(Ext.get(targetElId).dom.displaydiv).addCls('dragover');
        },
        
        onInvalidDrop : function() 
        {
            // Set a flag to invoke the animated repair
            this.invalidDrop = true;
        },
        
        b4EndDrag: function(e) 
        {
            if (this.invalidDrop !== true) 
            {
                this.callParent(arguments);
            }
            
            for (var i = 0; i < this._ddzones.length; i++)
            {
                Ext.removeNode(this._ddzones[i]);
            }
            this._ddzones = [];
            
            for (var i = 0; i < this._ddobjects.length; i++)
            {
                Ext.destroy(this._ddobjects[i]);
            }
            this._ddobjects = [];
        },
        
        endDrag : function(e) 
        {
            // Invoke the animation if the invalidDrop flag is set to true
            if (this.invalidDrop === true) {
                // Remove the drop invitation
                this.el.removeCls('dropOK');
                
                // Create the animation configuration object
                var animCfgObj = {
                    easing   : 'easeIn',
                    //duration : 1,
                    scope    : this,
                    callback : function() {
                        // Remove the position attribute
                        Ext.fly(this.getDragEl()).hide();
                    }
                };
         
                // Apply the repair animation
                Ext.fly(this.getDragEl()).setXY(this.originalXY, animCfgObj);
                delete this.invalidDrop;
            }
        },
        
        onDragOut: function(evtObj, targetElId) 
        {
            Ext.get(Ext.get(targetElId).dom.displaydiv).removeCls('dragover');
        }
    }
);
