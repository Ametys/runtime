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
 * 			title: 'An error occurred',
 * 			text: 'An unknown error as occurred while processing the request',
 * 			details: 'The component failed to connect to database...',
 * 			category: 'Ametys.my.Component' 
 * 		});
 * 
 * You can use this to use the class name automatically
 * 		category: this.self.getName()
 */
Ext.define(
	"Ametys.log.ErrorDialog",
	{
		singleton: true,
		
		/**
		 * @private
		 * @property {Ametys.log.ErrorDialog[]} _stack The stack of running errors
		 */
		_stack: [],

		/**
		 * @private
		 * Change the title depending on the number of errors
		 * @param {Ametys.window.DialogBox} dialogBox The dialog box to adapt
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
				dialogBox.setTitle(dialogBox.originalTitle + " - " + nb + " "+ "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_OTHERSERRORS}}");
				
				var buttons = dialogBox.getDockedItems('toolbar[dock="bottom"]')[0];
				buttons.items.get(0).setVisible(false);
				buttons.items.get(1).setVisible(true);
			}
		},
		
		/**
		 * Prepare the details string
		 * @param {String/Error} details The detailed text of the box. Hidden by default. Can be technical.
		 */
		_prepareDetails: function(details)
		{
			details = details || '';

			var text = details.toString();
			
			if (details.stack)
			{
				text += "\n" + details.stack.toString();
			}
			
            return text.toString()
                        // Convert text to html
                        .replace(/&/g, '&amp;').replace(/</g, '&lt;') 
                        .replace(/\n?\n/g, '<br/>').replace(/\t/g, '&#160;&#160;&#160;&#160;')
                        // Convert text to link (assuming ':' or ')' or whitespace are ending the link even if they are valid link characters)
                        .replace(/(https?:\/\/[^\s/]+(\/[^\s:)]*)?)/g, '<a href="$1" target="_blank">$1</a>');		},

		/**
		 * Creates and display directly an error message dialog box 
		 * @param {Object} config The box to display
		 * @param {String} config.title The title of the box
		 * @param {String} config.text The main text of the box. Should be localized
		 * @param {String/Error} [config.details] The detailed text of the box. Hidden by default. Can be technical.
		 * @param {String} [config.category] The log category. Not logged if null.
		 */
		display: function(config) {
			if (Ext.AbstractComponent.runningLayoutContext && Ext.AbstractComponent.runningLayoutContext.state == 0)
			{
				// The layout seems to be broken
				Ext.AbstractComponent.runningLayoutContext.run();
				Ext.AbstractComponent.runningLayoutContext = null;
				window.setTimeout(function() { while (Ext.AbstractComponent.layoutSuspendCount > 0){Ext.resumeLayouts(true);} }, 1);
			}
			
			var title = config.title;
			var text = config.text;
			var details = config.details;
			var category = config.category;
			
			var centralMsg = Ext.create('Ext.container.Container', {
				cls: 'error-dialog-text',
				scrollable: true,
				border: false,
		    	html: text,
		    	height: 70
		    });
			
			var detailedText = this._prepareDetails(details);
			var detailedMsg = Ext.create('Ext.container.Container', {
		    	cls: 'error-dialog-details',
		    	scrollable: true, 
		    	border: true,
		    	hidden: true,
		    	height: 160
		    });
			detailedMsg.update(detailedText);
			
			if (category)
			{
				Ametys.log.LoggerFactory.getLoggerFor(category).error({
					message: text, 
					details: details,
					
					defaultFocus: 0
				});
			}
			
			var errorDialog = Ext.create ('Ametys.window.DialogBox', {
				title: "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_ERROR_MSG}}" + title,
				originalTitle: "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_ERROR_MSG}}" + title,
				bodyPadding: '0',
				layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
				width: 500,
				scrollable: false,
				iconCls: 'error-dialog-icon',
				items: [ centralMsg, detailedMsg ],
				closeAction: 'close',
				closable: false,
				
				defaultFocus: 0,
				
				buttons: [
				    {
				    	text: "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_OK}}",
				    	handler: Ametys.log.ErrorDialog._okMessage
				    },
				    {
				    	// A bug of extjs causes to have to set space characters. If not the menu zone is too big.
				    	text: "&#160;&#160;&#160;&#160;&#160;" + "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_SPLITBUTTON_OK}}" + "&#160;&#160;&#160;&#160;&#160;",
				    	xtype: 'splitbutton',
				    	menu: 
				    	{
				    		items: 
				    		[{
			    		 		text: "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_IGNOREERRORS}}",
			    		 		handler: function() { window.setTimeout(Ametys.log.ErrorDialog._okMessages, 10); }
				    		 }]
				    	},
				    	handler: Ametys.log.ErrorDialog._okMessage
				    },
				    {
						text: "{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_DETAILS}} >>",
						handler: function() 
						{
			    			var currentErrorDialog = Ametys.log.ErrorDialog._stack[0];
			    			if (detailedMsg.hidden)
				    		{
			    				detailedMsg.show();
				    			this.setText("<< {{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_DETAILS}}");
				    		}
				    		else
				    		{
				    			this.setText("{{i18n PLUGINS_CORE_UI_MSG_ERRORDIALOG_DETAILS}} >>");
				    			detailedMsg.hide();
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
