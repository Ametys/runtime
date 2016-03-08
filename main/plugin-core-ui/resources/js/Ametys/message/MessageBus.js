/*
 *  Copyright 2013 Anyware Services
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
 * This class handle the message bus. A message is a way for ui elements to communicate without knwoing each others.
 * One tool will say "I just edit the content X", a concerned button will then refresh. But both elements does not references themselves. 
 * 
 * Use this class to register/unregister your component or to send a message.
 */
Ext.define("Ametys.message.MessageBus",
	{
		singleton: true,
		
		/**
		 * @property {Object} _listeners The registered listeners. An association of event type and functions.
		 * @property {Object[]} _listeners.MESSAGETYPE The event type list of listeners
		 * @property {Function} _listeners.MESSAGETYPE.fn The function to call
		 * @property {Object} _listeners.MESSAGETYPE.scope The scope to use
		 * @private
		 */
		_listeners: {},
		
		/**
		 * @property {Ametys.message.Message} _currentSelectionMessage The current selection
		 * @private
		 */
		/**
		 * @property {Ametys.message.Message} _currentSelectionChangingMessage The current changing selection
		 * @private
		 */
		
		/**
		 * This method adds an function to the registered list of message receivers for a given event type.
		 * You must unregister the object before you destroy it. See {@link #un}
		 * @param {String} messageType See Ametys.message.Message#cfg-type. Use '*' to register to all messages. Cannot be null.
		 * @param {Function} listener The function to call. The function has the following signature:
		 * @param {Ametys.message.Message} listener.message The message received
		 * @param {Object} scope The scope used to call this function
		 */
		on: function(messageType, listener, scope)
		{
			if (messageType == null)
			{
				var msg = "Ametys.message.MessageBus#on cannot be called with a null messageType";
				this.getLogger().error(msg);
				throw new Error(msg);
			}
			
			if (this._listeners[messageType] == null)
			{
				this._listeners[messageType] = [];
			}
			
			this._listeners[messageType].push({
				fn: listener,
				scope: scope
			});
		},

		/**
		 * This method removes an object to the registered list of message receivers.
		 * @param {String} messageType See Ametys.message.Message#cfg-type
		 * @param {Function} listener The function to call
		 * @param {Object} scope The scope used to call this function
		 */
		un: function(messageType, listener, scope)
		{
			for (var i = this._listeners[messageType].length - 1; i >= 0; i--)
			{
				if (this._listeners[messageType][i].fn == listener && this._listeners[messageType][i].scope == scope)
				{
					Ext.Array.remove(this._listeners[messageType], this._listeners[messageType][i]);
					return;
				}
			}
		},
		
		/**
		 * This method removes an object to the all registered list of message receivers.
		 * @param {Object} scope The scope used to call this function
		 */
		unAll: function (scope)
		{
			for (var messageType in this._listeners)
			{
				for (var i = this._listeners[messageType].length - 1; i >= 0; i--)
				{
					if (this._listeners[messageType][i].scope == scope)
					{
						Ext.Array.remove(this._listeners[messageType], this._listeners[messageType][i]);
					}
				}
			}
		},
		
		/**
		 * Get the current selection.
		 * @returns {Ametys.message.Message} The last message of type Ametys.message.Message#SELECTION_CHANGED. Cannot be null, be can contains empty targets.
		 */
		getCurrentSelectionMessage: function()
		{
			return this._currentSelectionMessage;
		},
		
		/**
		 * @private
		 * Main method of the bus : send a serie of message to the registered objects.
		 * @param {Ametys.message.Message} message An array of message to send.
 		 */
		fire: function(message)
		{

		    // If this is a selection message, discard obsolete message and record the last one
		    if (message.getType() == Ametys.message.Message.SELECTION_CHANGING)
		    {
		    	this._currentSelectionChangingMessage = message;
		    }
		    else if (message.getType() == Ametys.message.Message.SELECTION_CHANGED)
		    {
		    	if ((this._currentSelectionMessage && this._currentSelectionMessage.getCreationDate() > message.getCreationDate())
		    		|| (this._currentSelectionChangingMessage && this._currentSelectionChangingMessage.getCreationDate() > message.getCreationDate()))
		    	{
		    		// discarding this new selection message that is obsolete
					
					if (this.getLogger().isDebugEnabled())
					{
						this.getLogger().debug("Discarding obsolete selection message " + message.getNumber() + " with creation date " + message.getCreationDate() + " and associated targets " + message.getTargets() + "\n" + message._stack);
					}

					return;
		    	}
		    	
		    	this._currentSelectionMessage = message;
		    }
		    
		    // Duplicate listener array to avoid synchronization problems
		    var listeners = Ext.Array.merge(Ext.Array.clone(this._listeners['*'] || []),
		    								Ext.Array.clone(this._listeners[message.getType()] || []) 
		    							);
		    
		    // Stops server comm to group all incoming request
		    Ametys.data.ServerComm.suspend();
            Ext.suspendLayouts();
		    
		    // Loop on listeners
		    for (var i = 0; i < listeners.length; i++)
		    {
				var obj = listeners[i];
				if (typeof obj.fn == "function")
				{
					try
					{
						obj.fn.apply(obj.scope, [message]);
					}
					catch (e)
					{
						Ametys.log.ErrorDialog.display({
								title: "{{i18n PLUGINS_CORE_UI_MSG_MESSAGEBUS_BUSERROR_TITLE}}",
								text: "{{i18n PLUGINS_CORE_UI_MSG_MESSAGEBUS_BUSERROR_DESC}}",
				                details: e,
				                category: "Ametys.message.MessageBus"
						});
						function throwException(e) 
						{ 
							throw e; 
						}
						
						Ext.defer(throwException, 1, this, [e]);
					}
					
				}		    	
		    }
		    
		    // Refresh tools
			try
			{
				Ametys.tool.ToolsManager.refreshTools();
			}
			catch (e)
			{
				Ametys.log.ErrorDialog.display({
						title: "{{i18n PLUGINS_CORE_UI_MSG_MESSAGEBUS_REFRESHERROR_TITLE}}",
						text: "{{i18n PLUGINS_CORE_UI_MSG_MESSAGEBUS_REFRESHERROR_DESC}}",
		                details: e,
		                category: "Ametys.message.MessageBus"
				});

				function throwException(e) 
				{ 
					throw e;
				}
				Ext.defer(throwException, 1, this, [e]);
			}

			// Finally send all requests
		    Ametys.data.ServerComm.restart();
            Ext.resumeLayouts(true);
		},
		
		/**
		 * @private
		 * This will log the different listeners size. For debug purposes.
		 */
		debugListenersSize: function()
		{
			var s = "";
			var size = 0;
			Ext.Object.each(this._listeners, function(messageType, listeners) {
				size += listeners.length;
				s += " - " + messageType + ": " + listeners.length;
			});
			console.log(size + " Listeners. " + s);
			s = null;
		}
	}
);
