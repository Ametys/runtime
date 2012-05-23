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
 * A timeout message dialog box 
 * @private
 * @param {String} details The technical details to display  
 * @param {Number} index The index in ServerComm._runningRequests
 * @param {Number} timeout Optional new timeout value
 */
Ext.define(
	"Ametys.data.ServerComm.TimeoutDialog",
	{
		statics: {
			/**
			 * @property {Number}
			 * @readonly
			 * The time out between dialog calls (in milliseconds) 
			 */
			TIMEOUT: 60000,

			/**
			 * @private
			 * An array of currently opened dialogs
			 * @property {Ametys.data.ServerComm.TimeoutDialog[]}
			 */
			_stack: [],
		},

		/**
		 * @property {Number} _runningRequestIndex The index in {@link Ametys.data.ServerComm#_runningRequests}
		 * @private
		 */
		_runningRequestIndex: 0,

		/**
		 * Creates a timeout message dialog box and show it directly
		 * @param {String} details The technical details to display  
		 * @param {Number} index The index in ServerComm._runningRequests
		 * @param {Number} timeout Optional new timeout value
		 */
	    constructor: function(details, index, timeout) {
	    	timeout = timeout || this.TIMEOUT;
	    	var title = "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_TITLE'/>"
	    	var text = "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_TEXT1'/>" + "&lt;br/&gt;" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_TEXT2'/>"
	    		
	    	this._runningRequestIndex = index;
	    	this.getRequestOptions()._timeout = null;
	    	
	    	var centralMsg = new Ext.Panel({
	        	html: text,
	        	cls: 'timeout-dialog-text',
	        	autoScroll: true,
	        	border: false,
	        	height: 55
	        });
	    	var detailledMsg = new Ext.Panel({
	        	html: "&lt;div style='white-space: nowrap'&gt;" + details.replace(/\n?\n/g, '&lt;br/&gt;').replace(/\t/g, '&amp;#160;&amp;#160;&amp;#160;&amp;#160;') + "&lt;/div&gt;",
	        	cls: 'timeout-dialog-details',
	        	autoScroll: true, 
	        	border: false,
	        	hidden: true
	        });
	    	
	    	var waitId = Ext.id();
	    	
	    	var timeoutDialog = new Ametys.window.DialogBox( {
	    		title: title,
	    		businessWrapper: this,
	    		bodyPadding: '0',
	    		width: 530,
	    		height: 120,
	    		autoScroll: true,
	    		icon: Ametys.CONTEXT_PATH + "/kernel/resources/img/Ametys/theme/gray/timeout_16.png",
	    		items: [ centralMsg, detailledMsg ],
	    		closeAction: 'close',
	    		closable: false,
	    		defaultButton: waitId,
	    		buttons : [
	    		    {
	    		    	text :"<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_WAIT'/>",
	    		    	id: waitId,
	    		    	timeout: timeout,
	    		    	handler : function() 
	    		    	{
	    					var currentTimeoutDialog = Ametys.data.ServerComm.TimeoutDialog._stack[0];

	    					currentTimeoutDialog.businessWrapper.kill();
	    					currentTimeoutDialog.businessWrapper.getRequestOptions()._timeoutDialog = null;
	    					currentTimeoutDialog.businessWrapper.getRequestOptions()._timeout = window.setTimeout("Ametys.data.ServerComm._onRequestTimeout ('" + currentTimeoutDialog.businessWrapper._runningRequestIndex + "');", this.timeout);
	    		    	}
	    		    },
	    		    {
	    		    	text :"<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_CANCEL'/>",
	    		    	handler : function() 
	    		    	{
	        				var currentTimeoutDialog = Ametys.data.ServerComm.TimeoutDialog._stack[0];

	        				Ametys.data.ServerComm.getInstance()._abort(currentTimeoutDialog.businessWrapper.getRequestOptions());
	    		    	}
	    		    },
	    			{
	    				text : "<i18n:text i18n:key='KERNEL_SERVERCOMM_TIMEOUTDIALOG_DETAILS'/>" + " &gt;&gt;", 
	    				handler : function() 
	    				{
	    	    			var currentTimeoutDialog = Ametys.data.ServerComm.TimeoutDialog._stack[0];

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
	    	Ametys.data.ServerComm.TimeoutDialog._stack.push(timeoutDialog);
	    	if (Ametys.data.ServerComm.TimeoutDialog._stack.length == 1)
	    	{
	    		timeoutDialog.show();
	    	}
	    },

	    /**
	     * Display the next dialog box
	     * @private
	     */
	    _displayNext: function()
	    {
	    	if (Ametys.data.ServerComm.TimeoutDialog._stack.length != 0)
	    	{
	    		var nextTimeoutDialog = Ametys.data.ServerComm.TimeoutDialog._stack[0];
	    		nextTimeoutDialog.show();
	    	}
	    },
	    
	    /**
	     * @private
	     */
	    getRequestOptions: function()
	    {
	    	return Ametys.data.ServerComm._runningRequests[this._runningRequestIndex];
	    },
	    
	    /**
	     * Kill the dialog box (close if was opened and display the next one)
	     */
	    kill: function()
	    {
	    	if (this._td == Ametys.data.ServerComm.TimeoutDialog._stack[0])
	    	{
	    		Ametys.data.ServerComm.TimeoutDialog._stack[0].close();
	    		Ext.Array.remove(Ametys.data.ServerComm.TimeoutDialog._stack, this._td);
	    		Ametys.data.ServerComm.TimeoutDialog._displayNext();
	    	}
	    	else
	    	{
	    		Ext.Array.remove(Ametys.data.ServerComm.TimeoutDialog._stack, this._td);
	    	}
	    	this._td = null;
	    }
	}
);
