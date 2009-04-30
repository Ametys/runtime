/*
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */

// Ametys Namespace
Ext.namespace('Ext.ametys');

/**
 * Ext.ametys.NavigationItem
 *
 * @class Ext.ametys.NavigationItem
 * @extends Ext.Button
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.NavigationItem = function(config) 
{
	Ext.ametys.NavigationItem.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.NavigationItem, Ext.Button, 
{
	cls : "navigation-item",
	overCls: "over",
	border: false,
	width: '100%',
	enableToggle : true,
	allowDepress : true,
	template : new Ext.Template(
            '<div>',
            '<button class="navigation-item-button" type="{1}">{0}</button>',
            '</div>'),
	handler: function ()
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
			else if (this.ctToScroll.animConfig.anim.isAnimated())
			{
				this.ctToScroll.animConfig.anim.stop();
			}
			this.ctToScroll.getEl().child("div:first").child("*:first").scrollTo('top', div.offsetTop, this.ctToScroll.animConfig);
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
	onRender : function(ct, position)
	{
		Ext.ametys.NavigationItem.superclass.onRender.call(this, ct, position);
		this.ctToScroll = Ext.getCmp(this.ctToScroll);
	}
});