/*
 *  Copyright 2012 Anyware Services
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
 * This class handles an item of navigation
 * @private
 */
Ext.define('Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem', {
	extend: 'Ext.button.Button',
	
	cls : "navigation-item",
	overCls: "over",
	border: false,
	enableToggle : true,
	allowDepress : true,
	textAlign: 'left',
	width: 223,
    
	/**
	 * The handler of the button
	 */
    handler: function ()
    {
    	if (this.divToScroll)
    	{
    		//Désactiver le listener sur le scroll
    		this.unbindScroll();
    		var div = Ext.getDom(this.divToScroll);
    		
    		if (this.ctToScroll.getActiveAnimation())
    		{
    			this.ctToScroll.getActiveAnimation().end();
    		}

    		this.ctToScroll.getEl().child("div:first").scrollTo('top', div.offsetTop, {callback: this.bindScroll});
    	}
    	else if (this.activeItem != null)
    	{
    		Ext.getCmp(this.cardLayout).getLayout().setActiveItem(this.activeItem);
    	}
    	else if (this.idToHide)
    	{
    		Ext.getCmp(this.idToHide).hide();
    		Ext.getCmp(this.idToShow).show();
    	}
    	
    	if (this.handlerFn)
    	{
    		this.handlerFn();
    	}
    },
    
    onRender: function(ct, position)
    {
    	this.callParent(arguments);
    	this.ctToScroll = Ext.getCmp(this.ctToScroll);
    }

});
