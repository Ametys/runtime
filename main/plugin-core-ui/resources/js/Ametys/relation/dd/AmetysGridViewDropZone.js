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
 * @private
 * This zone is a Ext.tree.ViewDropZone that is compatible with the Ametys relation system.
 * 
 * That means that any dropped record can have been dragged from any Ametys relation zone and will use the Ametys.relation.RelationManager to connect the results.
 * Note that a drop zone can also be a normal zone to just graphically connect the objects (drag a resource explorer file on a file widget)
 */
Ext.define('Ametys.relation.dd.AmetysGridViewDropZone', {
	extend: 'Ext.grid.ViewDropZone',
    
	/**
	 * @cfg {String} indicatorHtml The html code for the drop indicator
	 */
	indicatorHtml: "",
	/**
	 * @cfg {String} indicatorCls The css classname of the drop indicator
	 */
	indicatorCls: "",
	
	/**
	 * @cfg {String} gridClass=drag-n-drop-plugin A CSS class added to the grid during an drag over operation
	 */
	gridClass: "drag-n-drop-plugin",
	
	overClass: "drag-n-drop-over",
	/**
	 * @cfg {String} overSelectionClass The css class set on the grid root when the current drop target is part of the selection
	 */
	overSelectionClass: "drag-n-drop-over-selection",
	
	constructor: function()
	{
		var plugin = this;
		
		this.callParent(arguments);
		this.view.on('afterrender', function() {
			this.addCls(plugin.gridClass);
		})
	},
	
    onNodeOver: function(node, dragZone, e, data)
    {
        var me = this;
        
		if (node != null && !Ext.get(node).hasCls(this.view.overItemCls))
		{
			// Over on the right space
			return this.onContainerOver(dragZone, e, data);
		}
        
        var parent = this.callParent(arguments);
        if (parent != this.dropNotAllowed)
        {
    		if (Ext.isFunction(this.setAmetysDropZoneInfos))
    		{
    			var targetRecords;
    			if (dragZone != null && this.view.getSelectionModel().isSelected(this.overRecord))
    			{
    				targetRecords = this.view.getSelectionModel().getSelection();
    			}
    			else
    			{
    				targetRecords = dragZone != null && this.overRecord != null ? [this.overRecord] : [];
    			}
    		
    			this.setAmetysDropZoneInfos(targetRecords, data);
    			
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
        
        if (this.overSelectionClass)
        {
	        if (me.view.getSelectionModel().isSelected(me.view.getRecord(node)) && (parent != this.dropNotAllowed))
	        {
	            this.el.addCls(this.overSelectionClass);
	        }
	        else
	       	{
	            this.el.removeCls(this.overSelectionClass);
	       	}
        }
        
        return parent;
    },
    
	onContainerOver : function(dd, e, data) 
	{
		this.overRecord = null;
		
		if (Ext.isFunction(this.setAmetysDropZoneInfos))
		{
        	this.setAmetysDropZoneInfos([], data);
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
        
        if (this.overSelectionClass)
        {
            this.el.removeCls(this.overSelectionClass);
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
            this.el.removeCls(this.overSelectionClass);
        }
        
        return this.callParent(arguments);
    },
	
	onNodeDrop: function(targetNode, dragZone, e, data) 
	{
		if (targetNode != null && !Ext.get(targetNode).hasCls(this.view.overItemCls))
		{
			// Droping on the right space
			return this.onContainerDrop(dragZone, e, data);
		}

		// If overRecord is part of the selection, the target is the whole selection
		if (Ext.isFunction(this.setAmetysDropZoneInfos))
		{
			var targetRecords;
			if (dragZone != null && this.view.getSelectionModel().isSelected(this.overRecord))
			{
				targetRecords = this.view.getSelectionModel().getSelection();
			}
			else
			{
				targetRecords = dragZone != null && this.overRecord != null ? [this.overRecord] : [];
			}
		
			this.setAmetysDropZoneInfos(targetRecords, data);
		}
        
		this.el.removeCls(this.overClass);
        this.el.removeCls(this.overSelectionClass);

        this.callParent(arguments);
	},
	
    onContainerDrop : function(dd, e, data) 
    {
		if (Ext.isFunction(this.setAmetysDropZoneInfos))
		{
			this.valid = true;
		}
        return this.onNodeDrop(null, null, e, data);
    }
});
