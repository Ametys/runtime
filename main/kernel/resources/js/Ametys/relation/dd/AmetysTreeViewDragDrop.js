/*
 *  Copyright 2014 Anyware Services
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
 * This plugin is a Ext.tree.plugin.TreeViewDragDrop plugin that is compatible with the Ametys relation system.
 * 
 * That means that any dragged node can be drop on any Ametys relation zone and will use the Ametys.relation.RelationManager to connect the results.
 * That means that any Ametys object can be drop here.
 * 
 * If you want to be able to drag objects using the relation system, it is required to implement #setAmetysDragInfos and use the default "ametys" dragGroup.
 * If you want to be able to drop objects using the relation system, it is required to implement #setAmetysDropZoneInfos and use the default "ametys" dropGroup.
 */
Ext.define('Ametys.relation.dd.AmetysTreeViewDragDrop', {
	extend: 'Ext.tree.plugin.TreeViewDragDrop',
	alias: 'plugin.ametystreeviewdragdrop',
	
    /**
     * @ignore
     * @cfg {String} ddGroup
     */
	
	config: {
		/**
		 * @cfg {String} defaultRelation=null A constant in Ametys.relation.Relation to define the default relation during a drag'n'drop operation. Can be null to take the default relation of the Ametys.relation.RelationHandler
		 */
		defaultRelation: null,
		/**
		 * @cfg {String} ctrlRelation=Ametys.relation.Relation.COPY A constant in Ametys.relation.Relation to define the relation during a drag'n'drop operation when CTRL key is used. Can be null to take the default relation of the Ametys.relation.RelationHandler
		 */
		ctrlRelation: Ametys.relation.Relation.COPY,
		/**
		 * @cfg {String} shiftRelation=Ametys.relation.Relation.MOVE A constant in Ametys.relation.Relation to define the relation during a drag'n'drop operation when SHIFT key is used. Can be null to take the default relation of the Ametys.relation.RelationHandler
		 */
		shiftRelation: Ametys.relation.Relation.MOVE
	},
	
	/**
	 * @template
	 * @cfg {Function} setAmetysDragInfos (required) Implements this method called by the getDragData method to create the Ametys.relation.RelationPoint of the drag operation.
	 * @cfg {Object} setAmetysDragInfos.item The default drag data that will be transmitted. You have to add a 'source' item in it: 
	 * @cfg {Object} setAmetysDragInfos.item.source The source (in the relation way) of the drag operation. A Ametys.relation.RelationPoint config.
	 */	
	/**
	 * @template
	 * @cfg {Function} setAmetysDropZoneInfos (required) Implements this method called before the beforeDrop event to create the Ametys.relation.RelationPoint of the drop operation.
	 * @cfg {Ext.data.Model[]} setAmetysDropZoneInfos.targetRecords The target records of the drop operation.
	 * @cfg {Object} setAmetysDropZoneInfos.item The default drag data that will be transmitted. You have to add a 'target' item in it: 
	 * @cfg {Object} setAmetysDropZoneInfos.item.target The target (in the relation way) of the drop operation. A Ametys.relation.RelationPoint config.
	 */		
    onViewRender : function(view) 
    {
        var me = this,
            scrollEl;

        if (me.enableDrag) 
        {
            if (me.containerScroll) 
            {
                scrollEl = view.getEl();
            }
            
            view.copy = true;
            
            me.dragZone = new Ametys.relation.dd.AmetysTreeViewDragZone({
                view: view,
                ddGroup: me.dragGroup || "ametys",
                dragText: me.dragText,
                displayField: me.displayField,
                repairHighlightColor: me.nodeHighlightColor,
                repairHighlight: me.nodeHighlightOnRepair,
                scrollEl: scrollEl,
                setAmetysDragInfos: me.setAmetysDragInfos
            });
        }

        if (me.enableDrop) 
        {
            me.dropZone = new Ametys.relation.dd.AmetysTreeViewDropZone({
                view: view,
                ddGroup: me.dropGroup || "ametys",
                allowContainerDrops: me.allowContainerDrops,
                appendOnly: me.appendOnly,
                allowParentInserts: me.allowParentInserts,
                expandDelay: me.expandDelay,
                dropHighlightColor: me.nodeHighlightColor,
                dropHighlight: me.nodeHighlightOnDrop,
                sortOnDrop: me.sortOnDrop,
                containerScroll: me.containerScroll,
                setAmetysDropZoneInfos: me.setAmetysDropZoneInfos
            });
            
            view.addListener('beforedrop', this._onBeforeDrop, this);
        }
    },
    
	/**
	 * This function is called when a drop gesture has been triggered in a valid drop position in the tree.
	 * @param {HTMLElement} nodeEl The TreeView node
	 * @param {Object} data The data object gathered at mousedown time
	 * @param {Ext.data.Model} overModel The Model over which the drop gesture took place.
	 * @param {String} dropPosition: "before", "after" or "append" depending on whether the mouse is above or below the midline of the node, or the node is a branch node which accepts new child nodes.
	 * @param {Object} dropHandlers This parameter allows to control when the drop action takes place.
	 * @private
	 */
	_onBeforeDrop: function (nodeEl, data, overModel, dropPosition, dropHandlers)
	{
		if (data.source && data.target)
		{
			dropHandlers.wait = true; // Defer the handling
			
			// FIXME handle the CTRL / SHIFT / ALT d'n'd and fill the below vars
			var relationType = this.defaultRelation;
			var forceUserChoice = false;
			
			var bindedDropHandled = Ext.bind(this._onDropHandled, this, [data, dropHandlers], true);
			if (!overModel.isExpanded())
			{
				overModel.expand (false, Ext.bind (Ametys.relation.RelationManager.link, Ametys.relation.RelationManager, [data.source, data.target, bindedDropHandled, relationType, forceUserChoice], false));
			}
			else
			{
				Ametys.relation.RelationManager.link(data.source, data.target, bindedDropHandled, relationType, forceUserChoice);
			}
		}
		else
		{
			return false;
		}
	},
	
	/**
	 * @private
	 * Callback when the Ametys.relation.RelationManager ended.
	 * @param {Boolean/String} success Has the operation been successful? The success. False is a problem occurred, a Ametys.relation.Relation constant else determining which operation was done
	 * @param {Object} data The drag data
	 * @param {Object} dropHandlers This parameter allows to control when the drop action takes place.
	 */
	_onDropHandled: function(success, data, dropHandlers)
	{
		// FIXME Was the node moved ? or copy/referenced ? is it new (from another place) ? how to do that (refresh nodes?)...
		if (success)
		{
			switch (success)
			{
				case Ametys.relation.Relation.MOVE:
					data.copy = false;
					break;
				case Ametys.relation.Relation.COPY:
					data.copy = true;
					break;
				case Ametys.relation.Relation.REFERENCE:
					data.copy = true;
					break;
			}
			
			dropHandlers.processDrop();
		}
		else
		{
			dropHandlers.cancelDrop();
		}
	}
});
	