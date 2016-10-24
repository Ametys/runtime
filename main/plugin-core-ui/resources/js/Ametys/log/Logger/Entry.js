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
 * A model for a logger entry.
 * Use helper methods on Ametys.log.Logger to create theses entries
 */
Ext.define(
	"Ametys.log.Logger.Entry",
	{
		extend: 'Ext.data.Model',
		
		/**
		 * @cfg {Number} level A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL 
		 */
		/**
		 * @cfg {String} category The name of the category of the log entry 
		 */
		/**
		 * @cfg {Date} date The date of the entry
		 */
		/**
		 * @cfg {String}  message The message
		 */
		/**
		 * @cfg {String/Error} details The detailled message. Can be null or empty.   
		 */
		fields: [
		   { name: 'level', type: 'int'},    
		   { name: 'category', type: 'string'},    
		   { name: 'date', type: 'date'},    
		   { name: 'message', type: 'string'},   
		   { name: 'details', type: 'auto'}, // String or Error
           { name: 'stacktrace', type: 'string' }
		],
		
		statics: 
		{
			/**
			 * Level of entry for a debug message
			 * @readonly
			 * @static
			 * @type {Number}
			 */
			LEVEL_DEBUG: 0,
			/**
			 * Level of entry for a information message
			 * @readonly
			 * @static
			 * @type {Number}
			 */
			LEVEL_INFO: 1,
			/**
			 * Level of entry for a warn message
			 * @readonly
			 * @static
			 * @type {Number}
			 */
			LEVEL_WARN: 2,
			/**
			 * Level of entry for an error message
			 * @readonly
			 * @static
			 * @type {Number}
			 */
			LEVEL_ERROR: 3,
			/**
			 * Level of entry for a fatal error message
			 * @readonly
			 * @static
			 * @type {Number}
			 */
			LEVEL_FATAL: 4

		},
		
		/**
		 * Get the level of the log entry but as a string
		 * @return {String} The level converted as a readeable string. eg 'DEBUG' for Ametys.log.Logger.Entry.LEVEL_DEBUG
		 */
		getLevelAsString: function() 
		{
			switch (this.get('level'))
			{
				case this.self.LEVEL_DEBUG: return 'DEBUG';
				case this.self.LEVEL_INFO: return 'INFO';
				case this.self.LEVEL_WARN: return 'WARN';
				case this.self.LEVEL_ERROR: return 'ERROR';
				case this.self.LEVEL_FATAL: return 'FATAL';
			}
		},

		/**
		 * Convert to a readable string
		 * @return {String} the entry as a string
		 */
		toString: function() 
		{
			return Ext.Date.format(this.get('date'), Ext.Date.patterns.ISO8601DateTime) + "\t" + this.getLevelAsString() + "\t[" + this.get('category') + "]\t" + this.get('message') + (this.get('details') ? " (" + this.get('details').toString() + ")" : '');
		},
		
		/**
		 * Trace the entry into the console (if available)
		 * If the level is not LEVEL_ERROR or LEVEL_FATAL and the message is of type ERROR, the stack is also traced 
		 */
		traceInConsole: function()
		{
			if (console)
			{
				var message = this.toString() 
							+ (this.get('details') && this.get('details').stack ? 
									'\n\t' + this.get('details').stack.replace('\n', '\n\t') 
									: '');
				
				switch (this.get('level'))
				{
					case this.self.LEVEL_DEBUG: 
						console.log(message);
						break;
					case this.self.LEVEL_INFO: 
						console.info(message);
						break;
					case this.self.LEVEL_WARN: 
						console.warn(message);
						break;
					case this.self.LEVEL_ERROR: 
						console.error(message);
						break;
					case this.self.LEVEL_FATAL: 
						console.error(message);
						break;
				}
			}
		}
	}
);
