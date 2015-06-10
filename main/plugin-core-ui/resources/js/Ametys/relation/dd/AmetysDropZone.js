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
 * This zone is a Ext.dd.DropZone that is compatible with the Ametys relation system.
 * 
 * That means that any dropped record can have been dragged from any Ametys relation zone and will use the Ametys.relation.RelationManager to connect the results.
 * Note that a drop zone can also be a normal zone to just graphically connect the objects (drag a resource explorer file on a file widget)
 * 
 * It is required to implement #setAmetysDropZoneInfos.
 * Use:
 * 
 * 			initComponent: function() {
 * 				this.callParent();
 * 				this.on('render', function() {
 * 						new Ametys.relation.dd.AmetysDropZone(this.getEl(), {setAmetysDropZoneInfos: Ext.bind(this.getDropInfo, this)});
 * 				});
 * 			},
 * 
 *			// @private
 *			// This event is thrown before the beforeDrop event and create the target of the drop operation relation.
 *			// @param {Ext.data.Model[]} targetRecords The target records of the drop operation.
 *			// @param {Object} item The default drag data that will be transmitted. You have to add a 'target' item in it: 
 *			// @param {Object} item.target The target (in the relation way) of the drop operation. A Ametys.relation.RelationPoint config. 
 *			//	
 *			getDropInfo: function(targetRecords, item)
 *			{
 *				item.target = {
 *					relationTypes: [Ametys.relation.Relation.MOVE, Ametys.relation.Relation.REFERENCE, Ametys.relation.Relation.COPY], 
 *					targets: {
 *						type: this._contentMessageType,
 *						parameters: { ids: [this._contentId] }
 *					}
 *				};
 *			}
 *
 */
Ext.define('Ametys.relation.dd.AmetysDropZone', {
	extend: 'Ext.dd.DropZone',
	
	/**
	 * @private
	 * @cfg {String} ddGroup='ametys' The dd group should always be 'ametys'
	 */
	ddGroup: "ametys",
	
	overClass: "drag-n-drop-over",

	/**
	 * @template
	 * @cfg {Function} setAmetysDropZoneInfos (required) Implements this method called before the beforeDrop event to create the Ametys.relation.RelationPoint of the drop operation.
	 * @cfg {Object} setAmetysDropZoneInfos.target The target of the drop operation.
	 * @cfg {Object} setAmetysDropZoneInfos.item The default drag data that will be transmitted. You have to add a 'target' item in it: 
	 * @cfg {Object} setAmetysDropZoneInfos.item.target The target (in the relation way) of the drop operation. A Ametys.relation.RelationPoint config.
	 */	
	
    onNodeOver: function(node, dragZone, e, data)
    {
        var me = this;
        
        var parent = this.callParent(arguments);
        if (parent != this.dropNotAllowed)
        {
    		if (Ext.isFunction(this.setAmetysDropZoneInfos))
    		{
	        	this.setAmetysDropZoneInfos(null, data);
	        	if (!data.source || !data.target || !Ametys.relation.RelationManager.testLink(data.source, data.target))
	        	{
	        		parent = this.dropNotAllowed;
	        	}
	    		data.target = null;
    		}
        }

        if (this.overClass)
        {
        	if (parent != this.dropNotAllowed)
        	{
        		this.el.addCls(this.overClass);
        	}
        	else
        	{
        		this.el.removeCls(this.overClass);
        	}
        }
        
        return parent;
    },
    
	onContainerOver : function(dd, e, data) 
	{
		this.overRecord = null;
		
		if (Ext.isFunction(this.setAmetysDropZoneInfos))
		{
        	this.setAmetysDropZoneInfos(null, data);
        	if (!data.source || !data.target || !Ametys.relation.RelationManager.testLink(data.source, data.target))
        	{
        		data.target = null;
        		return this.dropNotAllowed;
        	}
        	else
        	{
        		data.target = null;
        		return this.dropAllowed;	
        	}
		}
		return this.dropNotAllowed;
	},
	
    notifyOut : function(dd, e, data)
    {
        if(this.overClass)
        {
            this.el.removeCls(this.overClass);
        }
        
        return this.callParent(arguments);
    },
    
    /**
     * Called when the drop operation will not be valid
     */
    invalidateDrop: function() 
    {
        if(this.overClass)
        {
            this.el.removeCls(this.overClass);
        }
    },
    
	onNodeDrop: function(n, dragZone, e, data) 
	{
		var me = this;
		var dropHandled = false;

        // Create a closure to perform the operation which the event handler may use.
        // Users may now set the wait parameter in the beforedrop handler, and perform any kind
        // of asynchronous processing such as an Ametys.Msg.confirm, or an Ajax request,
        // and complete the drop gesture at some point in the future by calling either the
        // processDrop or cancelDrop methods.
        var dropHandlers = {
            processDrop: function () {
                me.invalidateDrop();
                dropHandled = true;
            },

            cancelDrop: function() {
                me.invalidateDrop();
                dropHandled = true;
            }
        };
        
        var performOperation = false;
		
		// If overRecord is part of the selection, the target is the whole selection
		if (Ext.isFunction(this.setAmetysDropZoneInfos))
		{
			this.setAmetysDropZoneInfos(n, data);
		}
        
		this.el.removeCls(this.overClass);

		if (data.source && data.target)
		{
			// FIXME handle the CTRL / SHIFT / ALT d'n'd and fill the below vars
			var relationType = this.defaultRelation;
			var forceUserChoice = false;
			
			Ametys.relation.RelationManager.link(data.source, data.target, Ext.bind(this._onDropHandled, this, [data, dropHandlers], true), relationType, forceUserChoice);
			
			return;
		}
        
        return performOperation;
	},
	
    onContainerDrop : function(dd, e, data) {
        return this.onNodeDrop(null, dd, e, data);
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
					dropHandlers.processDrop();
					break;
				case Ametys.relation.Relation.COPY:
					data.copy = true;
					dropHandlers.processDrop();
					break;
				case Ametys.relation.Relation.REFERENCE:
					data.copy = true;
					dropHandlers.cancelDrop();
					break;
			}
			
		}
		else
		{
			dropHandlers.cancelDrop();
		}
	}
});
