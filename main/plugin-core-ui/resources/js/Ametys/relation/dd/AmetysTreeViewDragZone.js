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
 * This zone is a Ext.tree.ViewDragZone that is compatible with the Ametys relation system.
 * 
 * That means that any dragged record can be drop on any Ametys relation zone and will use the Ametys.relation.RelationManager to connect the results.
 * Note that a drop zone can also be a normal zone to just graphically connect the objects (drag a resource explorer file on a file widget)
 */
Ext.define('Ametys.relation.dd.AmetysTreeViewDragZone', {
	extend: 'Ext.tree.ViewDragZone',

	onInitDrag: function(x, y)
    {
    	var item = this.callParent(arguments);
        if (item) 
        {
        	if (Ext.isFunction(this.setAmetysDragInfos))
        	{
        		this.setAmetysDragInfos(this.dragData)
        		
        		if (!this.dragData.source)
        		{
        			return false;
        		}
        	}
        }
        return item;
    }
});
