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

Ext.namespace('org.ametys.form');

/**
 * @class This class provides a file upload field width a help icon. Use the &lt;code&gt;desc&lt;/option&gt; to add the help icon.
 * @extends Ext.ux.form.FileUploadField
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.FileUploadField = function(config) 
{
	config.itemCls = "ametys-file";
	config.labelSeparator = '';
	
	if (!config.buttonOffset)
		config.buttonOffset = 6;
	
	org.ametys.form.FileUploadField.superclass.constructor.call(this, config);
}

Ext.extend(org.ametys.form.FileUploadField, Ext.ux.form.FileUploadField, {});

org.ametys.form.FileUploadField.prototype.onRender = function(ct, position)
{
	org.ametys.form.FileUploadField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.el.insertSibling({
			id: this.id + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 6px; float: left;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		Ext.QuickTips.register({
		    target: this.id + '-img',
		    text: this.desc,
		    dismissDelay: 0 // disable automatic hiding
		});
	}
	
	if (context != null &amp;&amp; context.maxUploadSize != null &amp;&amp; context.maxUploadSize != '')
	{
		Ext.get(this.el.dom.parentNode).insertSibling({cls: 'ametys-file-hint', html: "(<i18n:text i18n:key="KERNEL_UPLOAD_HINT"/>" + Ext.util.Format.fileSize(context.maxUploadSize) + ")"}, 'after');
	}
}

/**
 * Override the bindLisners method to prevent file path such as 'C:\fakepath\6_b.jpg' (IE)
 */
org.ametys.form.FileUploadField.prototype.bindListeners = function()
{
    this.fileInput.on({
        scope: this,
        mouseenter: function() {
            this.button.addClass(['x-btn-over','x-btn-focus'])
        },
        mouseleave: function(){
            this.button.removeClass(['x-btn-over','x-btn-focus','x-btn-click'])
        },
        mousedown: function(){
            this.button.addClass('x-btn-click')
        },
        mouseup: function(){
            this.button.removeClass(['x-btn-over','x-btn-focus','x-btn-click'])
        },
        change: function(){
            var v = this.fileInput.dom.value;
            
            if (v.lastIndexOf('/') > 0)
            {
            	v = v.substring(v.lastIndexOf('/') + 1);
            }
            else if (v.lastIndexOf('\\') > 0)
            {
            	v = v.substring(v.lastIndexOf('\\') + 1);
            }
            this.setValue(v);
            this.fireEvent('fileselected', this, v);    
        }
    }); 
}
