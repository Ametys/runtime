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

Ext.namespace('org.ametys.msg');

/**
 * Creates and display directly an error message dialog box 
 * @constructor
 * @class This class creates a dialog box of error message  
 * @param {String} title The title of the box
 * @param {String} text The main text of the box. Should be localized
 * @param {String} details The detailled text of the box. Hidden by default. Can be technical.
 * @param {String} category The log category. Not logged if null.
 */
org.ametys.msg.ErrorDialog = function (title, text, details, category)
{
	var centralMsg = new Ext.Panel({
    	html: text,
    	cls: 'error-dialog-text',
    	autoScroll: true,
    	border: false,
    	height: 47
    });
	var detailledMsg = new Ext.Panel({
    	html: "&lt;div style='white-space: nowrap'&gt;" + details.replace(/\n?\n/g, '&lt;br/&gt;').replace(/\t/g, '&amp;#160;&amp;#160;&amp;#160;&amp;#160;') + "&lt;/div&gt;",
    	cls: 'error-dialog-details',
    	autoScroll: true, 
    	border: false,
    	hidden: true
    });
	
	if (category)
	{
		org.ametys.log.LoggerManager.error(category, centralMsg, detailledMsg);
	}
	
	var okId = Ext.id();
	
	var errorDialog = new org.ametys.DialogBox( {
		title: "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_ERROR_MSG'/>" + title,
		originalTitle: "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_ERROR_MSG'/>" + title,
		cls: 'error-dialog',
		width: 450,
		height: 110,
		autoScroll: true,
		icon: context.contextPath + "/kernel/resources/img/error_16.gif",
		items: [ centralMsg, detailledMsg ],
		closeAction: 'close',
		closable: false,
		defaultButton: okId,
		buttons : [
		    {
		    	text :"<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_OK'/>",
		    	id: okId,
		    	handler : org.ametys.msg.ErrorDialog._okMessage
		    },
		    new Ext.SplitButton({
		    	// A bug of extjs imply to set space character. If not the menu zone is too big.
		    	text :"&amp;#160;&amp;#160;&amp;#160;&amp;#160;&amp;#160;" + "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_SPLITBUTTON_OK'/>" + "&amp;#160;&amp;#160;&amp;#160;&amp;#160;&amp;#160;",
		    	menu: 
		    	{
		    		items: 
		    		[{
	    		 		text: "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_IGNOREERRORS'/>",
	    		 		handler: function() { window.setTimeout(org.ametys.msg.ErrorDialog._okMessages, 10); } 
		    		 }]
		    	},
		    	handler : org.ametys.msg.ErrorDialog._okMessage
		    }),
		    {
				text : "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_DETAILS'/> &gt;&gt;",
				handler : function() 
				{
	    			var currentErrorDialog = org.ametys.msg.ErrorDialog._stack[0];

	    			if (detailledMsg.hidden)
		    		{
		    			detailledMsg.setHeight(100);
		    			detailledMsg.show();
		    			this.setText("&lt;&lt; <i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_DETAILS'/>");
		    			currentErrorDialog.setHeight(221)
		    		}
		    		else
		    		{
		    			this.setText("<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_DETAILS'/> &gt;&gt;");
		    			detailledMsg.hide();
		    			currentErrorDialog.setHeight(110)
		    		}
				}
			}
		]
	});
	
	org.ametys.msg.ErrorDialog._stack.push(errorDialog);
	if (org.ametys.msg.ErrorDialog._stack.length == 1)
	{
		errorDialog.show();
	}
	
	var currentlyDisplayedDialog = org.ametys.msg.ErrorDialog._stack[0];
	org.ametys.msg.ErrorDialog._adaptTitle(currentlyDisplayedDialog);
}

org.ametys.msg.ErrorDialog._okMessage = function()
{
	var currentErrorDialog = org.ametys.msg.ErrorDialog._stack[0];
	currentErrorDialog.close();
	org.ametys.msg.ErrorDialog._stack.remove(currentErrorDialog);
	
	if (org.ametys.msg.ErrorDialog._stack.length != 0)
	{
		var nextErrorDialog = org.ametys.msg.ErrorDialog._stack[0];
		nextErrorDialog.show();
		org.ametys.msg.ErrorDialog._adaptTitle(nextErrorDialog);
	}
}

org.ametys.msg.ErrorDialog._okMessages = function()
{
	if (org.ametys.msg.ErrorDialog._stack.length &gt; 0)
	{
		org.ametys.msg.ErrorDialog._stack[0].close();
	}
	org.ametys.msg.ErrorDialog._stack = [];
}

org.ametys.msg.ErrorDialog._stack = [];

org.ametys.msg.ErrorDialog._adaptTitle = function(dialogBox)
{
	var nb = org.ametys.msg.ErrorDialog._stack.length - 1;
	
	if (nb == 0)
	{
		dialogBox.setTitle(dialogBox.originalTitle);
		dialogBox.buttons[0].setVisible(true);
		dialogBox.buttons[1].setVisible(false);
	}
	else
	{
		dialogBox.setTitle(dialogBox.originalTitle + " - " + nb + " "+ "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_OTHERSERRORS'/>");
		dialogBox.buttons[0].setVisible(false);
		dialogBox.buttons[1].setVisible(true);
	}
}