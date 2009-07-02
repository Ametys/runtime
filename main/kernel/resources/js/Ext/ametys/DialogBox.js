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
 * Ext.ametys.DialogBox
 * 
 * @class This class provides a window panel. See {@link Ext.Window} for other
 *        configuration options.<br/> You can define a absolute path by 'icon'
 *        config option to be used as the header icon
 * @extends Ext.Window
 * @constructor
 * @param {Object}
 *            config Configuration options. icon : path to icon.
 * @example RUNTIME_Announcement.box = new Ext.ametys.DialogBox({ title
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
Ext.ametys.DialogBox = function(config) {
	if (config.icon) {
		var id = "icon-dialog-" + Ext.id();

		try {
			var css = "." + id + " {background-image:url('" + config.icon
					+ "');}"
			Ext.util.CSS.createStyleSheet(css, id);
			Ext.util.CSS.refreshCache();
		} catch (e) {
		}

		config.iconCls = id;
	}

	Ext.ametys.DialogBox.superclass.constructor.call(this, config);

	if (config.icon) {
		this.addListener('close', function() {
			Ext.util.CSS.removeStyleSheet(config.iconCls);
		})
	}
};

Ext.extend(Ext.ametys.DialogBox, Ext.Window, {
	resizable :true,
	shadow :true,
	modal :true,
	ametysCls :'ametys-box'
});

/**
 * Set the icon image for this dialog box
 * 
 * @param {String}
 *            icon The icon path
 */
Ext.ametys.DialogBox.prototype.setIconPath = function(icon) {
	var id = "icon-dialog-" + Ext.id();

	try {
		var css = "." + id + " {background-image:url('" + icon + "');}"
		Ext.util.CSS.createStyleSheet(css, id);
		Ext.util.CSS.refreshCache();
	} catch (e) {
	}

	this.setIconClass(id);
}

Ext.ametys.DialogBox.prototype.onRender = function(ct, position) {
	Ext.ametys.DialogBox.superclass.onRender.call(this, ct, position);
	this.body.addClass(this.ametysCls + '-body');
	this.header.addClass(this.ametysCls + '-header');
}
