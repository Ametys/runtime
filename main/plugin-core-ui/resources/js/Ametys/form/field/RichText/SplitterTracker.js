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
 * This special impl will have a good looking effect and will resize the good components
 * @private
 */
Ext.define('Ametys.form.field.RichText.SplitterTracker', {
    extend: 'Ext.resizer.SplitterTracker',
    
    statics: {
        /**
         * @property {Ext.Component} _fakeComponent A fake component to make to parent class ok
         * @private
         */
        _fakeComponent: Ext.create("Ext.Component", {
            floating: true,
            autoShow: true,
            width: 1,
            height: Number.MAX_VALUE,
            cls: "x-field-richtext-resizer-fake"
        })
    },

    /**
     * @cfg {Ext.Component} componentToResize (required) The component to resize.
     */
    /**
     * @property {Ext.Component} _componentToResize See #cfg-componentToResize
     * @private
     */
    /**
     * @property {Ext.Component} __resizeComponent The visually modified component during a drag
     * @private
     */
     
    constructor: function(config)
    {
        this._componentToResize = config.componentToResize;
        
        this._resizeComponent = Ext.create("Ext.Component", {
            floating: true,
            cls: "x-field-richtext-resizer"
        });
        
        this.callParent(arguments);
    },
     
    getPrevCmp: function() {
        return this._componentToResize;
    },

    getNextCmp: function() {
        return Ametys.form.field.RichText.SplitterTracker._fakeComponent;
    },
    
    // calculate the constrain Region in which the splitter el may be moved.
    calculateConstrainRegion: function() {
        var sBox = this.getSplitter().getBox();
        var cBox = this._componentToResize.getBox();
        
        return new Ext.util.Region(
            cBox.top + this._componentToResize.minHeight,
            sBox.right,
            cBox.top + this._componentToResize.minHeight + 20000,
            sBox.left);
    },
    
    
    
    // At the end, the brother components are broken
        performResize: function(e, offset) {
        var me        = this,
            splitter  = me.getSplitter(),
            orient    = splitter.orientation,
            prevCmp   = me.getPrevCmp(),
            nextCmp   = me.getNextCmp(),
            owner     = splitter.ownerCt,
            //flexedSiblings = owner.query('>[flex]'),
            //len       = flexedSiblings.length,
            vertical  = orient === 'vertical',
            i         = 0,
            dimension = vertical ? 'width' : 'height',
            totalFlex = 0,
            item, size;

        // Convert flexes to pixel values proportional to the total pixel width of all flexes.
        /*for (; i < len; i++) {
            item = flexedSiblings[i];
            size = vertical ? item.getWidth() : item.getHeight();
            totalFlex += size;
            item.flex = size;
        }*/

        offset = vertical ? offset[0] : offset[1];

        if (prevCmp) {
            size = me.prevBox[dimension] + offset;
            if (prevCmp.flex) {
                prevCmp.flex = size;
            } else {
                prevCmp[dimension] = size;
            }
        }
        if (nextCmp) {
            size = me.nextBox[dimension] - offset;
            if (nextCmp.flex) {
                nextCmp.flex = size;
            } else {
                nextCmp[dimension] = size;
            }
        }

        owner.updateLayout();
    },
    
    
    
    
    
    
    
    
    
    
    
    // Theses overrides are for having a good looking effect
    
    onDrag: function(e) {
        var offset = this.getOffset('dragTarget');

        this._resizeComponent.show();
        this._resizeComponent.setPosition(this._componentToResize.getFrameEl().getX(), this._componentToResize.getFrameEl().getY());
        this._resizeComponent.setSize(this._componentToResize.getFrameEl().getWidth(), this._componentToResize.getHeight() + offset[1]);
    },    
    
    onEnd: function(e) {
        this.callParent(arguments);
        this._resizeComponent.hide();
    }
});
