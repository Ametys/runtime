/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys');

/**
 * org.ametys.NavigationItem
 *
 * @class This class handles an item of navigation
 * @extends Ext.Button
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var navigation = new org.ametys.NavigationPanel ({title: "Sommaire"});
 * var item = new org.ametys.NavigationItem ({
 * 	text: 'Base de données',
 *	divToScroll: 'ext-125554',
 * 	ctToScroll:  'config-inner',
 * 	bindScroll: bindScrollFn,
 * 	unbindScroll: unbindScrollFn,
 * 	toggleGroup : 'config-menu',
 * 	id : 'a-ext-125554'
 * });
 * navigation.add(item);
 */
org.ametys.NavigationItem = function(config) 
{
	org.ametys.NavigationItem.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.NavigationItem, Ext.Button, 
{
	cls : "navigation-item",
	overCls: "over",
	border: false,
	enableToggle : true,
	allowDepress : true,
	template : new Ext.Template(
            '<div class="{3}">',
            '<button class="navigation-item-button" type="{1}">{0}</button>',
            '</div>')
});


org.ametys.NavigationItem.prototype.handler = function ()
{
	if (this.divToScroll)
	{
		//Désactiver le listener sur le scroll
		this.unbindScroll();
		var div = Ext.getDom(this.divToScroll);
		if (this.ctToScroll.animConfig == null)
		{
			this.ctToScroll.animConfig = {callback: this.bindScroll};
		}
		else if (this.ctToScroll.animConfig.anim.isAnimated)
		{
			this.ctToScroll.animConfig.anim.stop();
		}
		this.ctToScroll.getEl().child("div:first").child("*:first").scrollTo('top', div.offsetTop, this.ctToScroll.animConfig);
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
}

org.ametys.NavigationItem.prototype.onRender = function(ct, position)
{
	org.ametys.NavigationItem.superclass.onRender.call(this, ct, position);
	this.ctToScroll = Ext.getCmp(this.ctToScroll);
}
