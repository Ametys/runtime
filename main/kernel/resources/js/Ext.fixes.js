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

// ------------------------------
// Here are ExtJS bug fixes
// ------------------------------
(function()
{
	Ext.define("Ametys.Editor", {
		override: 'Ext.Editor',
		
		// Fix CMS-5920 http://www.sencha.com/forum/showthread.php?297056-4.2.3-Bug-in-Editor.js-mix-up-between-Field-and-Base in ExtJS 4.2.3
	    startEdit : function(el, value) {
	        var me = this,
	            field = me.field,
	            dom;

	        me.completeEdit();
	        me.boundEl = Ext.get(el);
	        dom = me.boundEl.dom;
	        value = Ext.isDefined(value) ? value : Ext.String.trim(dom.textContent || dom.innerText || dom.innerHTML);

	        // If NOT configured with a renderTo, render to the ownerCt's element
	        // Being floating, we do not need to use the actual layout's target.
	        // Indeed, it's better if we do not so that we do not interfere with layout's child management.
	        if (!me.rendered && !me.renderTo && me.ownerCt) {
	            (me.renderTo = me.ownerCt.el).position();
	        }

	        if (me.fireEvent('beforestartedit', me, me.boundEl, value) !== false) {
	            me.startValue = value;
	            me.show();
	            // temporarily suspend events on field to prevent the "change" event from firing when reset() and setValue() are called
	            field.suspendEvents();
	            field.reset();
	            field.setValue(value);
	            field.resumeEvents();
	            me.realign(true);
	            field.focus(field.getRawValue ? [field.getRawValue().length] : null); // BUG FIX HERE
	            if (field.autoSize) {
	                field.autoSize();
	            }
	            me.editing = true;
	        }
	    }
	});
	
    Ext.define("Ametys.layout.ContextItem", {
    	override: 'Ext.layout.ContextItem',
    	
    	// Fix CMS-5908 http://www.sencha.com/forum/showthread.php?291412-Error-after-upgrade-to-ExtJS-4.2.3
        init: function (full, options) {
            var me = this,
                oldProps = me.props,
                oldDirty = me.dirty,
                ownerCtContext = me.ownerCtContext,
                ownerLayout = me.target.ownerLayout,
                firstTime = !me.state,
                ret = full || firstTime,
                children, i, n, ownerCt, sizeModel, target,
                oldHeightModel = me.heightModel,
                oldWidthModel = me.widthModel,
                newHeightModel, newWidthModel,
                remainingCount = 0;

            me.dirty = me.invalid = false;
            me.props = {};

            // Reset the number of child dimensions since the children will add their part:
            me.remainingChildDimensions = 0;

            if (me.boxChildren) {
                me.boxChildren.length = 0; // keep array (more GC friendly)
            }

            if (!firstTime) {
                me.clearAllBlocks('blocks');
                me.clearAllBlocks('domBlocks');
            }

            // For Element wrappers, we are done...
            if (!me.wrapsComponent) {
                return ret;
            }

            // From here on, we are only concerned with Component wrappers...
            target = me.target;
            me.state = {}; // only Component wrappers need a "state"

            if (firstTime) {
                // This must occur before we proceed since it can do many things (like add
                // child items perhaps):
                if (target.beforeLayout && target.beforeLayout !== Ext.emptyFn) {
                    target.beforeLayout();
                }

                // Determine the ownerCtContext if we aren't given one. Normally the firstTime
                // we meet a component is before the context is run, but it is possible for
                // components to be added to a run that is already in progress. If so, we have
                // to lookup the ownerCtContext since the odds are very high that the new
                // component is a child of something already in the run. It is currently
                // unsupported to drag in the owner of a running component (needs testing).
                if (!ownerCtContext && (ownerCt = target.ownerCt)) {
                    ownerCtContext = me.context.items[ownerCt.el.id];
                }

                if (ownerCtContext) {
                    me.ownerCtContext = ownerCtContext;
                    me.isBoxParent = target.ownerLayout ? target.ownerLayout.isItemBoxParent(me) : false; // BUG FIX HERE
                } else {
                    me.isTopLevel = true; // this is used by initAnimation...
                }

                me.frameBodyContext = me.getEl('frameBody');
            } else {
                ownerCtContext = me.ownerCtContext;

                // In theory (though untested), this flag can change on-the-fly...
                me.isTopLevel = !ownerCtContext;

                // Init the children element items since they may have dirty state (no need to
                // do this the firstTime).
                children = me.children;
                for (i = 0, n = children.length; i < n; ++i) {
                    children[i].init(true);
                }
            }

            // We need to know how we will determine content size: containers can look at the
            // results of their items but non-containers or item-less containers with just raw
            // markup need to be measured in the DOM:
            me.hasRawContent = !(target.isContainer && target.items.items.length > 0);

            if (full) {
                // We must null these out or getSizeModel will assume they are the correct,
                // dynamic size model and return them (the previous dynamic sizeModel).
                me.widthModel = me.heightModel = null;
                sizeModel = target.getSizeModel(ownerCtContext && 
                    ownerCtContext.widthModel.pairsByHeightOrdinal[ownerCtContext.heightModel.ordinal]);

                if (firstTime) {
                    me.sizeModel = sizeModel;
                }

                me.widthModel = sizeModel.width;
                me.heightModel = sizeModel.height;

                // if we are a container child (e.g., not a docked item), and this is a full
                // init, that means our parent was invalidated, and therefore both our width
                // and our height are included in remainingChildDimensions
                if (ownerCtContext && !me.isComponentChild) {
                    ownerCtContext.remainingChildDimensions += 2;
                }
            } else if (oldProps) {
                // these are almost always calculated by the ownerCt (we might need to track
                // this at some point more carefully):
                me.recoverProp('x', oldProps, oldDirty);
                me.recoverProp('y', oldProps, oldDirty);
                
                // if these are calculated by the ownerCt, don't trash them:
                if (me.widthModel.calculated) {
                    me.recoverProp('width', oldProps, oldDirty);
                } else if ('width' in oldProps) {
                    ++remainingCount;
                }
                if (me.heightModel.calculated) {
                    me.recoverProp('height', oldProps, oldDirty);
                } else if ('height' in oldProps) {
                    ++remainingCount;
                }
                
                // if we are a container child and this is not a full init, that means our
                // parent was not invalidated and therefore only the dimensions that were
                // set last time and removed from remainingChildDimensions last time, need to
                // be added back to remainingChildDimensions. This only needs to happen for
                // properties that we don't recover above (model=calculated)
                if (ownerCtContext && !me.isComponentChild) {
                    ownerCtContext.remainingChildDimensions += remainingCount;
                }
            }

            if (oldProps && ownerLayout && ownerLayout.manageMargins) {
                me.recoverProp('margin-top', oldProps, oldDirty);
                me.recoverProp('margin-right', oldProps, oldDirty);
                me.recoverProp('margin-bottom', oldProps, oldDirty);
                me.recoverProp('margin-left', oldProps, oldDirty);
            }

            // Process any invalidate options present. These can only come from explicit calls
            // to the invalidate() method.
            if (options) {
                // Consider a container box with wrapping text. If the box is made wider, the
                // text will take up less height (until there is no more wrapping). Conversely,
                // if the box is made narrower, the height starts to increase due to wrapping.
                //
                // Imposing a minWidth constraint would increase the width. This may decrease
                // the height. If the box is shrinkWrap, however, the width will already be
                // such that there is no wrapping, so the height will not further decrease.
                // Since the height will also not increase if we widen the box, there is no
                // problem simultaneously imposing a minHeight or maxHeight constraint.
                //
                // When we impose as maxWidth constraint, however, we are shrinking the box
                // which may increase the height. If we are imposing a maxHeight constraint,
                // that is fine because a further increased height will still need to be
                // constrained. But if we are imposing a minHeight constraint, we cannot know
                // whether the increase in height due to wrapping will be greater than the
                // minHeight. If we impose a minHeight constraint at the same time, then, we
                // could easily be locking in the wrong height.
                //
                // It is important to note that this logic applies to simultaneously *adding*
                // both a maxWidth and a minHeight constraint. It is perfectly fine to have
                // a state with both constraints, but we cannot add them both at once.
                newHeightModel = options.heightModel;
                newWidthModel = options.widthModel;
                if (newWidthModel && newHeightModel && oldWidthModel && oldHeightModel) {
                    if (oldWidthModel.shrinkWrap && oldHeightModel.shrinkWrap) {
                        if (newWidthModel.constrainedMax && newHeightModel.constrainedMin) {
                            newHeightModel = null;
                        }
                    }
                }

                // Apply size model updates (if any) and state updates (if any).
                if (newWidthModel) {
                    me.widthModel = newWidthModel;
                }
                if (newHeightModel) {
                    me.heightModel = newHeightModel;
                }

                if (options.state) {
                    Ext.apply(me.state, options.state);
                }
            }

            return ret;
        },
    });
    
    Ext.define("Ametys.ux.IFrame", {
    	override: 'Ext.ux.IFrame',
    	
    	// Fix CMS-5772 CMS-5966 http://www.sencha.com/forum/showthread.php?297288-Drag-and-drop-over-an-Ext.ux.IFrame-scrolled-has-an-offset&p=1085641#post1085641
    	onRelayedEvent: function (event) {
			        // relay event from the iframe's document to the document that owns the iframe...

			        var iframeEl = this.iframeEl,

			            // Get the left-based iframe position
			            iframeXY = Ext.Element.getTrueXY(iframeEl),
			            originalEventXY = event.getXY(),

			            // Get the left-based XY position.
			            // This is because the consumer of the injected event (Ext.EventManager) will
			            // perform its own RTL normalization.
			            eventXY = Ext.EventManager.getPageXY(event.browserEvent);

			        // the event from the inner document has XY relative to that document's origin,
			        // so adjust it to use the origin of the iframe in the outer document:
			        
			        // AMETYS FIX 
			        //event.xy = [iframeXY[0] + eventXY[0], iframeXY[1] + eventXY[1]];
			        event.xy = [iframeXY[0] + eventXY[0] - (iframeEl.dom.contentWindow.document.documentElement.scrollLeft || iframeEl.dom.contentWindow.document.body.scrollLeft), 
			                    iframeXY[1] + eventXY[1] - (iframeEl.dom.contentWindow.document.documentElement.scrollTop || iframeEl.dom.contentWindow.document.body.scrollTop)];
			        
			        event.injectEvent(iframeEl); // blame the iframe for the event...

			        event.xy = originalEventXY; // restore the original XY (just for safety)
			    }	
    });
    
    Ext.define("Ametys.selection.Model", {
    	override: 'Ext.selection.Model',
    	
    	// FIXME CMS-6001 http://www.sencha.com/forum/showthread.php?297509-Ext-4.2.3-onStoreRefresh-of-Ext.selection.Model&p=1086464#post1086464
    	onStoreRefresh: function(){
            var me = this,
                selected = me.selected,
                items, length, i, rec, storeRec;
                
            if (me.store.buffered) {
                return;
            }
                
            items = selected.items;
            length = items.length;
             
            me.lastSelected = me.getStoreRecord(me.lastSelected);
            
            for (i = length-1; i >= 0; i--) {
                rec = items[i];
                storeRec = me.getStoreRecord(rec);
                if (storeRec) {
                    if (rec.hasId()) {
                        me.selected.replace(storeRec);
                    }
                } else {
                    me.selected.remove(rec);
                }
            }   
        }
    });
})();