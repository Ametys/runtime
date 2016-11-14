/*
 *  Copyright 2016 Anyware Services
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
 * This class is an interaction for {@link Ametys.plugins.admin.jvmstatus.MonitoringTool} only.
 * It allows to pan with horizontal dragging and to zoom with vertical dragging.
 * It also allows to make a zoom on double tap.
 */
Ext.define('Ametys.plugins.admin.jvmstatus.MonitoringTool.TimeAxisPanZoom', {
    extend: 'Ext.chart.interactions.PanZoom',
    xtype: 'timepanzoom',
    
    constructor: function(config)
    {
        config.zoomOnPanGesture = true;
        config.axes = {
            bottom: {
                allowZoom: true,
                allowPan: true
            },
            left: {
                allowZoom: false,
                allowPan: false
            }
        };
        
        this.callParent(arguments);
    },
    
    /**
     * @inheritdoc
     * @param {Ext.event.Event} e The event
     */
    onDoubleTap: function(e)
    {
        // Let's do some math :)
        var zoomLevel = 2;
        var chart = this.getChart(),
            range = chart.getAxis(0).getVisibleRange(),
            element = e.getTarget();
        
        // Bug => When the double click does not occured on the graph but on the edges (docked elements, etc.)
        // Here is a dirty workaround
        if (!Ext.String.endsWith(element.parentElement.parentElement.getAttribute('id'), '-overlay'))
        {
            return;
        }
            
        var elRect = element.getBoundingClientRect();
        var relX = e.getX() - elRect.left;
        var elWidth = elRect.right - elRect.left;
        
        var oldStart = range[0],
            oldEnd = range[1],
            oldRangeLength = oldEnd - oldStart,
            newCenter = oldStart + oldRangeLength * (relX / elWidth),
            newStart = newCenter - (oldRangeLength / 2 / zoomLevel),
            newEnd = newCenter + (oldRangeLength / 2 / zoomLevel);
            
        if (newStart < oldStart)
        {
            newStart = oldStart;
            newEnd = newStart + oldRangeLength / zoomLevel;
        }
        else if (newEnd > oldEnd)
        {
            newEnd = oldEnd;
            newStart = newEnd - oldRangeLength / zoomLevel;
        }
        
        chart.getAxis(0).setVisibleRange([newStart, newEnd]);
        chart.redraw();
    },
    
    /**
     * @inheritdoc
     * @param {Ext.event.Event} e The event
     */
    onPanGestureMove: function(e)
    {
        // We do not override the behavior on multitouch devices
        if (this.isMultiTouch()) {
            return this.callParent(arguments);
        }
        
        // Otherwise, here is the original code edited in order to be able to zoom by dragging horizontally
        var me = this;
        if (me.getLocks()[me.getPanGesture()] === me) { // Limit drags to single touch.
            var rect = me.getChart().getInnerRect(),
                xy = me.getChart().element.getXY()
                timeAxis = me.getChart().getAxes()[0];
                
            var panX = e.getX() - xy[0] - rect[0] - me.startX; //pan by horizontal dragging
            var sx = me.startY / (e.getY() - xy[1] - rect[1]); //zoom by vertical dragging
            me.transformAxesBy([timeAxis], panX, 0, sx, 1);
            me.sync();
            return false;
        }
    }
});