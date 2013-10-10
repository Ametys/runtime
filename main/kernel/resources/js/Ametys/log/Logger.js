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
 * A logger for a category.
 * To create a logger see Ext.Base#getLogger or Ametys.log.LoggerFactory.getLoggerFor
 * 
 * You can adjust your log level using #setLogLevel
 * Beware the level inherit between categories
 */
Ext.define(
	"Ametys.log.Logger",
	{
		/**
		 * @cfg {String} category='' A category. Can be a class name.
		 */
		/**
		 * @property {String} _category See #cfg-category
		 * @private
		 */
		
		/**
		 * Creates a logger
		 * @param {Object} config The configuration
		 * @private
		 */
		constructor: function(config)
		{
			this._category = config.category || '';
		},
		
		/**
		 * Get the current log level
		 * @returns {Number} A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 */
		getLogLevel: function()
		{
			return Ametys.log.LoggerFactory._getLogLevel(this._category);
		},
		
		/**
		 * Set the current log level.
		 * Log level is a property inherit from parent category if null.
		 * @param {Number} logLevel A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL. Or null to inherit your parent level.
		 */
		setLogLevel: function(logLevel)
		{
			Ametys.log.LoggerFactory._setLogLevel(logLevel, this._category);
		},
		
		/**
		 * Will a debug message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isDebugEnabled: function()
		{
			return this.getLogLevel() <= Ametys.log.Logger.Entry.LEVEL_DEBUG;
		},
		/**
		 * Will an info message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isInfoEnabled: function()
		{
			return this.getLogLevel() <= Ametys.log.Logger.Entry.LEVEL_INFO;
		},
		/**
		 * Will a warn message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isWarnEnabled: function()
		{
			return this.getLogLevel() <= Ametys.log.Logger.Entry.LEVEL_WARN;
		},
		/**
		 * Will an error message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isErrorEnabled: function()
		{
			return this.getLogLevel() <= Ametys.log.Logger.Entry.LEVEL_ERROR;
		},
		/**
		 * Will a fatal message be logged ?
		 * @return {Boolean} true if the message will be logged
		 */
		isFatalEnabled: function()
		{
			return this.getLogLevel() <= Ametys.log.Logger.Entry.LEVEL_FATAL;
		},
		
		/**
		 * Log as debug
		 * @param {String/Object} message The message if a string or a message configuration:
		 * @param {String} message.message The message
		 * @param {String/Error} message.details The detailled message. Can be null or empty. 
		 */
		debug: function(message)
		{
			this._log(message, Ametys.log.Logger.Entry.LEVEL_DEBUG);
		},
		/**
		 * Log as info
		 * @param {String/Object} message The message if a string or a message configuration:
		 * @param {String} message.message The message
		 * @param {String/Error} message.details The detailled message. Can be null or empty. 
		 */
		info: function(message)
		{
			this._log(message, Ametys.log.Logger.Entry.LEVEL_INFO);

		},
		/**
		 * Log as warn
		 * @param {String/Object} message The message if a string or a message configuration:
		 * @param {String} message.message The message
		 * @param {String/Error} message.details The detailled message. Can be null or empty. 
		 */
		warn: function(message)
		{
			this._log(message, Ametys.log.Logger.Entry.LEVEL_WARN);
		},
		/**
		 * Log as error
		 * @param {String/Object} message The message if a string or a message configuration:
		 * @param {String} message.message The message
		 * @param {String/Error} message.details The detailled message. Can be null or empty. 
		 */
		error: function(message)
		{
			this._log(message, Ametys.log.Logger.Entry.LEVEL_ERROR);
		},
		/**
		 * Log as fatal
		 * @param {String/Object} message The message if a string or a message configuration:
		 * @param {String} message.message The message
		 * @param {String/Error} message.details The detailled message. Can be null or empty. 
		 */
		fatal: function(message)
		{
			this._log(message, Ametys.log.Logger.Entry.LEVEL_FATAL);
		},
		
		/**
		 * Create a log, trace it and store it
		 * @param {String/Object} message The message if a string or a message configuration:
		 * @param {String} message.message The message
		 * @param {String/Error} message.details The detailled message. Can be null or empty. 
		 * @param {Number} level A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 * @private
		 */
		_log: function(message, level)
		{
			if (this.getLogLevel() > level)
			{
				return;
			}

			// convert string message to an object
			if (Ext.isString(message))
			{
				message = {message: message};
			}
			
			// add config for entries
			Ext.apply(message, {
				level: level, 
				date: new Date(),
				category: this._category
			});
			
			// creates the log entry
			var entry = new Ametys.log.Logger.Entry(message);
			entry.traceInConsole();
			
			Ametys.log.LoggerFactory.getStore().add(entry);
		}
	}
);