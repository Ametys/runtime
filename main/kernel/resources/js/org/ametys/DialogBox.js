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
Ext.namespace('org.ametys');

/**
 * org.ametys.DialogBox
 * 
 * @class This class provides a window panel. See {@link Ext.Window} for other
 *        configuration options.<br/> You can define a absolute path by 'icon'
 *        config option to be used as the header icon
 * @extends Ext.Window
 * @constructor
 * @param {Object}
 *            config Configuration options. icon : path to icon.
 * @example RUNTIME_Announcement.box = new org.ametys.DialogBox({ title
 *          :'&lt;i18n:text
 *          i18n:key="PLUGINS_SYSTEM_ANNOUNCEMENT_DIALOG_CAPTION"/&gt;', width
 *          :380, height :200, icon : getPluginResourcesUrl(pluginName) +
 *          '/img/announce.png', items : [ RUNTIME_Announcement.form ],
 *          closeAction: 'close', buttons : [ { text :"&lt;i18n:text
 *          i18n:key="PLUGINS_SYSTEM_ANNOUNCEMENT_DIALOG_OK"/&gt;", handler :
 *          RUNTIME_Announcement.ok }, { text :"&lt;i18n:text
 *          i18n:key="PLUGINS_ANNOUNCEMENT_DIALOG_CANCEL"/&gt;', handler :
 *          RUNTIME_Announcement.cancel }] });
 */
org.ametys.DialogBox = function(config) {
	if (config.icon) 
	{
		config.iconCls = "instyleicon";
	}

	org.ametys.DialogBox.superclass.constructor.call(this, config);
	
	this.on('show', this._onShow, this);
};

Ext.extend(
	org.ametys.DialogBox, 
	Ext.Window, 
	{
		resizable :false,
		shadow :true,
		modal :true,
		ametysCls :'ametys-box'
	}
);

org.ametys.DialogBox.prototype.onRender = function(ct, position) 
{
	org.ametys.DialogBox.superclass.onRender.call(this, ct, position);
	
	this.body.addClass(this.ametysCls + '-body');
	this.header.addClass(this.ametysCls + '-header');
}

/**
 * Set the icon image for this dialog box
 * 
 * @param {String}
 *            icon The icon path
 */
org.ametys.DialogBox.prototype.setIconPath = function(icon) 
{
	this.icon = icon;
	this.setIconClass(this.iconCls);
}

org.ametys.DialogBox.prototype.setIconClass = function(cls)
{
	org.ametys.DialogBox.superclass.setIconClass.call(this, cls);
	
    if(this.rendered && this.header)
    {
        if(this.frame)
        {
        	this.header.dom.style.backgroundImage = "url('" + this.icon + "')";
        }
        else
        {
            var img = hd.firstChild && String(hd.firstChild.tagName).toLowerCase() == 'img' ? hd.firstChild : null;
            img.style.backgroundImage = this.icon;
        }
    }
}

/**
 * @private
 * Listener when the window shows to place it correctly in the screen
 */
org.ametys.DialogBox.prototype._onShow = function()
{
	var pos = this.getPosition();
	var size = this.getSize();
	
	var viewportSize = this.getEl().getViewSize();
	
	pos[0] = Math.max(0, Math.min(pos[0], Ext.getBody().getWidth() - size.width));
	pos[1] = Math.max(0, Math.min(pos[1], Ext.getBody().getHeight() - size.height));
	
	this.setPosition(pos[0], pos[1]);
}
