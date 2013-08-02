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
 * A logger.
 * This is a store of Ametys.log.Logger.Entry
 * Also contains methods helper to create entries and add it in a row
 * 
 * 		try
 * 		{
 * 			if (Ametys.log.Logger.isDebugEnabled())
 * 			{
 * 				Ametys.log.Logger.debug({
 * 					category: this.self.getName(), // could be any string like 'Ametys.my.Component'
 * 					message: 'My debug message'
 * 				});
 * 			}
 * 
 * 			// some error code
 * 		}
 * 		catch (e)
 * 		{
 * 			Ametys.log.Logger.error({
 * 				category: this.self.getName(),
 * 				message: 'My error message',
 * 				details: e
 * 			});
 * 		}
 * 
 * Level under the currentLogLevel will be ignored.
 * For example, default log level is LEVEL_ERROR, logging a warn will be ignored
 */
Ext.define(
	"Ametys.log.Logger",
	{
		singleton: true,
		
		/**
		 * @private
		 * @property {Ext.data.ArrayStore} _store The store of log entries 
		 */
		_store: Ext.create("Ext.data.ArrayStore"),
		
		/**
		 * @private
		 * @property {Number} _currentLogLevel The current log level. If a log is created with a lower level it will be ignored 
		 */
		_currentLogLevel: 3,
		
		/**
		 * Get the current log level
		 * @returns {Number} A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 */
		getLogLevel: function()
		{
			return _currentLogLevel;
		},

		/**
		 * Set the current log level.
		 * @param {Number} logLevel A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 */
		setLogLevel: function(logLevel)
		{
			this._currentLogLevel = Math.min(Math.max(logLevel, Ametys.log.Logger.Entry.LEVEL_DEBUG), Ametys.log.Logger.Entry.LEVEL_FATAL);
		},
		
		/**
		 * Will a debug message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isDebugEnabled: function()
		{
			return this._currentLogLevel <= Ametys.log.Logger.Entry.LEVEL_DEBUG;
		},
		/**
		 * Will an info message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isInfoEnabled: function()
		{
			return this._currentLogLevel <= Ametys.log.Logger.Entry.LEVEL_INFO;
		},
		/**
		 * Will a warn message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isWarnEnabled: function()
		{
			return this._currentLogLevel <= Ametys.log.Logger.Entry.LEVEL_WARN;
		},
		/**
		 * Will an error message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isErrorEnabled: function()
		{
			return this._currentLogLevel <= Ametys.log.Logger.Entry.LEVEL_ERROR;
		},
		/**
		 * Will a fatal message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isFatalEnabled: function()
		{
			return this._currentLogLevel <= Ametys.log.Logger.Entry.LEVEL_FATAL;
		},
		
		/**
		 * Get the entries of the logger.
		 * @return {Ext.data.ArrayStore} The ordered entries.
		 */
		getStore: function()
		{
			return this._store;
		},

		/**
		 * Log as debug
		 * @param {Object} config The message configuration
		 * @param {String} config.category The name of the category of the log entry. Use '.' to create a hierarchy. Can be the class name of the current component (this.self.getName())
		 * @param {String} config.message The message
		 * @param {String/Error} config.details The detailled message. Can be null or empty. 
		 */
		debug: function(config)
		{
			this._log(config, Ametys.log.Logger.Entry.LEVEL_DEBUG);
		},
		/**
		 * Log as info
		 * @param {Object} config The message configuration
		 * @param {String} config.category The name of the category of the log entry. Use '.' to create a hierarchy. Can be the class name of the current component (this.self.getName())
		 * @param {String} config.message The message
		 * @param {String/Error} config.details The detailled message. Can be null or empty. 
		 */
		info: function(config)
		{
			this._log(config, Ametys.log.Logger.Entry.LEVEL_INFO);

		},
		/**
		 * Log as warn
		 * @param {Object} config The message configuration
		 * @param {String} config.category The name of the category of the log entry. Use '.' to create a hierarchy. Can be the class name of the current component (this.self.getName())
		 * @param {String} config.message The message
		 * @param {String/Error} config.details The detailled message. Can be null or empty. 
		 */
		warn: function(config)
		{
			this._log(config, Ametys.log.Logger.Entry.LEVEL_WARN);
		},
		/**
		 * Log as error
		 * @param {Object} config The message configuration
		 * @param {String} config.category The name of the category of the log entry. Use '.' to create a hierarchy. Can be the class name of the current component (this.self.getName())
		 * @param {String} config.message The message
		 * @param {String/Error} config.details The detailled message. Can be null or empty. 
		 */
		error: function(config)
		{
			this._log(config, Ametys.log.Logger.Entry.LEVEL_ERROR);
		},
		/**
		 * Log as fatal
		 * @param {Object} config The message configuration
		 * @param {String} config.category The name of the category of the log entry. Use '.' to create a hierarchy. Can be the class name of the current component (this.self.getName())
		 * @param {String} config.message The message
		 * @param {String/Error} config.details The detailled message. Can be null or empty. 
		 */
		fatal: function(config)
		{
			this._log(config, Ametys.log.Logger.Entry.LEVEL_FATAL);
		},
		
		/**
		 * Create a log, trace it and store it
		 * @param {Object} config The message configuration
		 * @param {String} config.category The name of the category of the log entry. Use '.' to create a hierarchy. Can be the class name of the current component (this.self.getName())
		 * @param {String} config.message The message
		 * @param {String/Error} config.details The detailled message. Can be null or empty. 
		 * @param {Number} level A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 * @private
		 */
		_log: function(config, level)
		{
			if (this._currentLogLevel < level)
			{
				return;
			}

			config = Ext.apply(config, {
				level: level, 
				date: new Date() 
			});
			
			var entry = new Ametys.log.Logger.Entry(config);
			entry.traceInConsole();
			
			this.getStore().add(entry);			
		}
	}
);
