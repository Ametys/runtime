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
 * A error message dialog box.
 * 
 * 		Ametys.log.ErrorDialog.display({
 * 			title: 'An error occured',
 * 			text: 'An unknown error as occured while processing the request',
 * 			details: 'The component faild to connect to database...',
 * 			category: 'Ametys.my.Component'
 * 		});
 */
Ext.define(
	"Ametys.log.ErrorDialog",
	{
		singleton: true,
		
		/**
		 * @private
		 * @property {Ametys.log.ErrorDialog[]} The stack of running errors
		 */
		_stack: [],

		/**
		 * @private
		 * Change the title depending on the number of errors
		 * @param {Ametys.window.DialogBox} dialogBox The dialogbox to adapt
		 */
		_adaptTitle: function(dialogBox)
		{
			var nb = Ametys.log.ErrorDialog._stack.length - 1;
			
			if (nb == 0)
			{
				dialogBox.setTitle(dialogBox.originalTitle);
				
				var buttons = dialogBox.getDockedItems('toolbar[dock="bottom"]')[0];
				buttons.items.get(0).setVisible(true);
				buttons.items.get(1).setVisible(false);
			}
			else
			{
				dialogBox.setTitle(dialogBox.originalTitle + " - " + nb + " "+ "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_OTHERSERRORS'/>");
				
				var buttons = dialogBox.getDockedItems('toolbar[dock="bottom"]')[0];
				buttons.items.get(0).setVisible(false);
				buttons.items.get(1).setVisible(true);
			}
		},
		
		/**
		 * Prepare the details string
		 * @param {String/Error} details The detailled text of the box. Hidden by default. Can be technical.
		 */
		_prepareDetails: function(details)
		{
			details = details || '';

			var text = details.toString();
			
			if (details.stack)
			{
				text += "\n" + details.stack.toString()
			}
			
			return text.toString().replace(/\n?\n/g, '<br/>').replace(/\t/g, '&#160;&#160;&#160;&#160;');
		},

		/**
		 * Creates and display directly an error message dialog box 
		 * @param {Object} config The box to display
		 * @param {String} config.title The title of the box
		 * @param {String} config.text The main text of the box. Should be localized
		 * @param {String/Error} config.details The detailled text of the box. Hidden by default. Can be technical.
		 * @param {String} config.category The log category. Not logged if null.
		 */
		display: function(config) {
			var title = config.title;
			var text = config.text;
			var details = config.details;
			var category = config.category;
			
			var centralMsg = new Ext.Panel({
		    	html: text,
		    	cls: 'error-dialog-text',
		    	autoScroll: true,
		    	border: false,
		    	height: 47
		    });
			var detailledMsg = new Ext.Panel({
		    	html: "<div style='white-space: nowrap'>" + this._prepareDetails(details) + "</div>",
		    	cls: 'error-dialog-details',
		    	autoScroll: true, 
		    	border: false,
		    	hidden: true
		    });
			
			if (category)
			{
				Ametys.log.Logger.error(category, centralMsg, detailledMsg);
			}
			
			var okId = Ext.id();
			
			var errorDialog = new Ametys.window.DialogBox( {
				title: "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_ERROR_MSG'/>" + title,
				originalTitle: "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_ERROR_MSG'/>" + title,
				bodyPadding: '0',
				width: 450,
				height: 110,
				autoScroll: true,
				icon: Ametys.CONTEXT_PATH + "/kernel/resources/img/error_16.gif",
				items: [ centralMsg, detailledMsg ],
				closeAction: 'close',
				closable: false,
				defaultButton: okId,
				buttons : [
				    {
				    	text :"<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_OK'/>",
				    	id: okId,
				    	handler : Ametys.log.ErrorDialog._okMessage
				    },
				    new Ext.SplitButton({
				    	// A bug of extjs imply to set space character. If not the menu zone is too big.
				    	text :"&#160;&#160;&#160;&#160;&#160;" + "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_SPLITBUTTON_OK'/>" + "&#160;&#160;&#160;&#160;&#160;",
				    	menu: 
				    	{
				    		items: 
				    		[{
			    		 		text: "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_IGNOREERRORS'/>",
			    		 		handler: function() { window.setTimeout(Ametys.log.ErrorDialog._okMessages, 10); } 
				    		 }]
				    	},
				    	handler : Ametys.log.ErrorDialog._okMessage
				    }),
				    {
						text : "<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_DETAILS'/> >>",
						handler : function() 
						{
			    			var currentErrorDialog = Ametys.log.ErrorDialog._stack[0];

			    			if (detailledMsg.hidden)
				    		{
				    			detailledMsg.setHeight(100);
				    			detailledMsg.show();
				    			this.setText("<< <i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_DETAILS'/>");
				    			currentErrorDialog.setHeight(221)
				    		}
				    		else
				    		{
				    			this.setText("<i18n:text i18n:key='KERNEL_MSG_ERRORDIALOG_DETAILS'/> >>");
				    			detailledMsg.hide();
				    			currentErrorDialog.setHeight(110)
				    		}
						}
					}
				]
			});
			
			Ametys.log.ErrorDialog._stack.push(errorDialog);
			if (Ametys.log.ErrorDialog._stack.length == 1)
			{
				errorDialog.show();
			}
			
			var currentlyDisplayedDialog = Ametys.log.ErrorDialog._stack[0];
			Ametys.log.ErrorDialog._adaptTitle(currentlyDisplayedDialog);
		},

		/**
		 * @private
		 * Validate current message
		 */
		_okMessage: function()
		{
			var currentErrorDialog = Ametys.log.ErrorDialog._stack[0];
			currentErrorDialog.close();
			Ext.Array.remove(Ametys.log.ErrorDialog._stack, currentErrorDialog);
			
			if (Ametys.log.ErrorDialog._stack.length != 0)
			{
				var nextErrorDialog = Ametys.log.ErrorDialog._stack[0];
				nextErrorDialog.show();
				Ametys.log.ErrorDialog._adaptTitle(nextErrorDialog);
			}
		},
		
		/**
		 * @private
		 * Validate all messages
		 */
		_okMessages: function()
		{
			if (Ametys.log.ErrorDialog._stack.length > 0)
			{
				Ametys.log.ErrorDialog._stack[0].close();
			}
			Ametys.log.ErrorDialog._stack = [];
		}
	}
);
