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
 * @private
 * Ovverride Ext.resizer.SplitterTracker to make it works as a resizer for the #collapseTarget 
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout.FloatingSplitterTracker", 
    {
        extend: "Ext.resizer.SplitterTracker",
        
        // ensure the tracker is enabled, store boxes of previous and next
        // components and calculate the constrain region
        onBeforeStart: function(e) 
        {
            var me = this,
                collapseTarget = me.getSplitter().collapseTarget,
                target = e.getTarget(),
                box;
                
            if (!collapseTarget)
            {
                return false;
            }
    
            if (collapseTarget && target === collapseTarget.dom) 
            {
                return false;
            }
    
            // SplitterTracker is disabled if any of its adjacents are collapsed.
            if (collapseTarget.collapsed) 
            { 
                return false;
            }
    
            // store boxes of previous and next
            me.collapseTargetBox  = collapseTarget.getBox();
            me.constrainTo = box = me.calculateConstrainRegion();
    
            if (!box) 
            {
                return false;
            }
    
            return box;
        },
        
        onStart: function()
        {
            this.callParent(arguments);
            
            var splitter = this.getSplitter();
            this.splitterGhost = Ext.create(splitter.self.$className, Ext.applyIf({ floating: true }, splitter.getInitialConfig()));
            this.splitterGhost.collapseTarget = splitter.collapseTarget;
            this.splitterGhost.setSize(splitter.getSize());
            this.splitterGhost.showAt(splitter.getX(), splitter.getY());
        },
        
        onDrag: function(e) 
        {
            var me        = this,
                offset    = me.getOffset('dragTarget'),
                splitter  = me.getSplitter(),
                splitEl   = splitter.getEl(),
                orient    = splitter.orientation;
    
            if (orient === "vertical") 
            {
                this.splitterGhost.setX(me.startRegion.left + offset[0]);
            } 
            else 
            {
                this.splitterGhost.setY(me.startRegion.top + offset[1]);
            }
        },
        
        onEnd: function(e) 
        {
            this.callParent(arguments);
            
            this.splitterGhost.destroy();
            this.splitterGhost = null;
        },
        
        // calculate the constrain Region in which the splitter el may be moved.
        calculateConstrainRegion: function() 
        {
            var me         = this,
                splitter   = me.getSplitter(),
                splitWidth = splitter.getWidth(),
                zoneTabsToolPanel = splitter.ownerCt.collapseDirection ? splitter.ownerCt : splitter.ownerCt.ownerCt,
                defaultMin = splitter.defaultSplitMin,
                orient     = splitter.orientation,
                collapseTargetBox = me.collapseTargetBox,
                collapseTarget    = me.collapseTarget,
                collapseTargetConstrainRegion, constrainOptions;
    
            // vertical splitters, so resizing left to right
            if (orient === 'horizontal') 
            {
                var zoneHeight = zoneTabsToolPanel.ownerCt.getEl().findParent(".x-container-tool-layout", null, true).getHeight();

                if (splitter.renderData.collapseDir == 'bottom')
                {
                    collapseTargetConstrainRegion = new Ext.util.Region(
                        collapseTargetBox.y + splitter.collapseTarget.ownerCt._originialMinSize,
                        collapseTargetBox.right,
                        collapseTargetBox.y + (splitter.collapseTarget.ownerCt._originialMaxSize ? splitter.collapseTarget.ownerCt._originialMaxSize : zoneHeight * 2/3),
                        collapseTargetBox.left
                    );
                }
                else
                {
                    collapseTargetConstrainRegion = new Ext.util.Region(
                        collapseTargetBox.bottom - (splitter.collapseTarget.ownerCt._originialMaxSize ? splitter.collapseTarget.ownerCt._originialMaxSize : zoneHeight * 2/3),
                        collapseTargetBox.right,
                        collapseTargetBox.bottom - splitter.collapseTarget.ownerCt._originialMinSize,
                        collapseTargetBox.left
                    );
                }
            } 
            else 
            {
                var zoneWidth = zoneTabsToolPanel.ownerCt.getEl().findParent(".x-container-tool-layout", null, true).getWidth();
                
                if (splitter.renderData.collapseDir == 'right')
                {
                    collapseTargetConstrainRegion = new Ext.util.Region(
                        collapseTargetBox.top,
                        collapseTargetBox.x + (splitter.collapseTarget.ownerCt._originialMaxSize ? splitter.collapseTarget.ownerCt._originialMaxSize : zoneWidth * 2/3),
                        collapseTargetBox.bottom,
                        collapseTargetBox.x + splitter.collapseTarget.ownerCt._originialMinSize
                    );
                }
                else
                {
                    collapseTargetConstrainRegion = new Ext.util.Region(
                        collapseTargetBox.top,
                        collapseTargetBox.right - splitter.collapseTarget.ownerCt._originialMinSize,
                        collapseTargetBox.bottom,
                        collapseTargetBox.right - (splitter.collapseTarget.ownerCt._originialMaxSize ? splitter.collapseTarget.ownerCt._originialMaxSize : zoneWidth * 2/3)
                    );
                }
            }
    
            return collapseTargetConstrainRegion;
        },
        
        // Performs the actual resizing of the previous and next components
        performResize: function(e, offset) 
        {
            var me        = this,
                splitter  = me.getSplitter(),
                orient    = splitter.orientation,
                zoneTabsToolPanel = splitter.ownerCt.collapseDirection ? splitter.ownerCt : splitter.ownerCt.ownerCt,
                vertical  = orient === 'vertical',
                dimension = vertical ? 'width' : 'height',
                position  = vertical ? 'x' : 'y';
                
    
            var value = vertical ? offset[0] : offset[1];
            
            var revert = zoneTabsToolPanel.collapseDirection == "bottom" || zoneTabsToolPanel.collapseDirection == "right";
            if (revert)
            {
                value = -value;
            }
            
            ratio = (splitter.collapseTarget[dimension] + value) / splitter.collapseTarget[dimension];
            splitter.collapseTarget[dimension] += value;
            splitter.collapseTarget.ownerCt._originalFlex *= ratio;
            
            zoneTabsToolPanel.updateLayout();
            if (revert)
            {
                zoneTabsToolPanel.alignTo(zoneTabsToolPanel.ownerCt, "br-br");
            }
        }        
    }
);
    