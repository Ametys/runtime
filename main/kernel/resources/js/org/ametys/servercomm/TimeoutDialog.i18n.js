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

Ext.namespace('org.ametys.servercomm');

/**
 * Creates and display directly a timeout message dialog box 
 * @constructor
 * @class This class creates a dialog box of timeout message
 * @param {String} details The technical details to display  
 * @param {Integer} index The index in ServerComm._runningRequests
 */
org.ametys.servercomm.TimeoutDialog = function (details, index)
{
	var title = "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_TITLE'/>"
	var text = "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_TEXT1'/>" + "&lt;br/&gt;" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_TEXT2'/>"
		
	this._runningRequestIndex = index;
	this.getRequestOptions()._timeout = null;
	
	var centralMsg = new Ext.Panel({
    	html: text,
    	cls: 'timeout-dialog-text',
    	autoScroll: true,
    	border: false,
    	height: 50
    });
	var detailledMsg = new Ext.Panel({
    	html: "&lt;div style='white-space: nowrap'&gt;" + details.replace(/\n?\n/g, '&lt;br/&gt;').replace(/\t/g, '&amp;#160;&amp;#160;&amp;#160;&amp;#160;') + "&lt;/div&gt;",
    	cls: 'timeout-dialog-details',
    	autoScroll: true, 
    	border: false,
    	hidden: true
    });
	
	var waitId = Ext.id();
	
	var timeoutDialog = new org.ametys.DialogBox( {
		title: title,
		businessWrapper: this,
		cls: 'timeout-dialog',
		width: 460,
		height: 120,
		autoScroll: true,
		icon: context.workspaceContext + "/resources/img/uitool/timeout_16.png",
		items: [ centralMsg, detailledMsg ],
		closeAction: 'close',
		closable: false,
		defaultButton: waitId,
		buttons : [
		    {
		    	text :"<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_WAIT'/>",
		    	id: waitId,
		    	handler : function() 
		    	{
					var currentTimeoutDialog = org.ametys.servercomm.TimeoutDialog._stack[0];

					currentTimeoutDialog.businessWrapper.kill();
					currentTimeoutDialog.businessWrapper.getRequestOptions()._timeoutDialog = null;
					currentTimeoutDialog.businessWrapper.getRequestOptions()._timeout = window.setTimeout("org.ametys.servercomm.ServerComm._onRequestTimeout ('" + currentTimeoutDialog.businessWrapper._runningRequestIndex + "');", org.ametys.servercomm.TimeoutDialog.TIMEOUT);
		    	}
		    },
		    {
		    	text :"<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_CANCEL'/>",
		    	handler : function() 
		    	{
    				var currentTimeoutDialog = org.ametys.servercomm.TimeoutDialog._stack[0];

    				org.ametys.servercomm.ServerComm.getInstance()._abort(currentTimeoutDialog.businessWrapper.getRequestOptions());
		    	}
		    },
			{
				text : "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_DETAILS'/>" + " &gt;&gt;", // FIXME i18n
				handler : function() 
				{
	    			var currentTimeoutDialog = org.ametys.servercomm.TimeoutDialog._stack[0];

	    			if (detailledMsg.hidden)
		    		{
		    			detailledMsg.setHeight(50);
		    			detailledMsg.show();
		    			this.setText("&lt;&lt; " + "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_DETAILS'/>");
		    			currentTimeoutDialog.setHeight(170)
		    		}
		    		else
		    		{
		    			this.setText("<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_DETAILS'/>" + " &gt;&gt;");
		    			detailledMsg.hide();
		    			currentTimeoutDialog.setHeight(120)
		    		}
				}
			}
		]
	});
	
	this._td = timeoutDialog;
	org.ametys.servercomm.TimeoutDialog._stack.push(timeoutDialog);
	if (org.ametys.servercomm.TimeoutDialog._stack.length == 1)
	{
		timeoutDialog.show();
	}
}

/**
 * @type {Integer}
 * The time out between dialog calls (in milliseconds) 
 */
org.ametys.servercomm.TimeoutDialog.TIMEOUT = 30000;

/**
 * @private
 * An array of currently opened dialogs
 * @type {org.ametys.servercomm.TimeoutDialog[]}
 */
org.ametys.servercomm.TimeoutDialog._stack = [];

/**
 * Display the next dialog box
 */
org.ametys.servercomm.TimeoutDialog._displayNext = function()
{
	if (org.ametys.servercomm.TimeoutDialog._stack.length != 0)
	{
		var nextTimeoutDialog = org.ametys.servercomm.TimeoutDialog._stack[0];
		nextTimeoutDialog.show();
	}
}

/**
 * @property {Integer} _runningRequestIndex The index in ServerComm._runningRequests
 * @private
 */
org.ametys.servercomm.TimeoutDialog.prototype._runningRequestIndex;

org.ametys.servercomm.TimeoutDialog.prototype.getRequestOptions = function()
{
	return org.ametys.servercomm.ServerComm._runningRequests[this._runningRequestIndex];
}

/**
 * Kill the dialog box (close if was opened and display the next one)
 */
org.ametys.servercomm.TimeoutDialog.prototype.kill = function()
{
	if (this._td == org.ametys.servercomm.TimeoutDialog._stack[0])
	{
		org.ametys.servercomm.TimeoutDialog._stack[0].close();
		org.ametys.servercomm.TimeoutDialog._stack.remove(this._td);
		org.ametys.servercomm.TimeoutDialog._displayNext();
	}
	else
	{
		org.ametys.servercomm.TimeoutDialog._stack.remove(this._td);
	}
	this._td = null;
}
