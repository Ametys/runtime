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
 * A logger entry
 */
Ext.define(
	"Ametys.log.Logger.Entry",
	{
		statics: {
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
			 * Level of entry for a warning message
			 * @readonly
			 * @static
			 * @type {Number}
			 */
			LEVEL_WARNING: 2,
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
			LEVEL_FATALERROR: 4,

		},
		
		/**
		 * Creates a log entry 
		 * @private
		 * @param {Number} level A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARNING, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATALERROR
		 * @param {String} category The name of the category of the log entry
		 * @param {Date} date The date of the entry
		 * @param {String} message The message
		 * @param {String} details The detailled message. Can be null or empty.   
		 */
		constructor: function (level, category, date, message, details)
		{
			/**
			 * @property {Number} _level A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARNING, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATALERROR
			 * @private
			 */
			this._level = level;
			/**
			 * @property {String} _category The name of the category of the log entry
			 * @private
			 */
			this._category = category;
			/**
			 * @property {Date} _date The date of the entry
			 * @private
			 */
			this._date = date;
			/**
			 * @property {String} _message The message
			 * @private
			 */
			this._message = message;
			/**
			 * @property {String} _details The detailled message. Can be null or empty.
			 * @private
			 */
			this._details = details;
		},

		/**
		 * Get the level of the log entry.
		 * @return {Number} A constant between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARNING, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATALERROR
		 */
		getLevel: function() {
			return this._level;
		},
		
		/**
		 * Get the level of the log entry but as a string
		 * @return {String} The level converted as a readeable string. eg 'DEBUG' for Ametys.log.Logger.Entry.LEVEL_DEBUG
		 */
		getLevelAsString: function() {
			switch (this._level)
			{
				case Ametys.log.Logger.Entry.LEVEL_DEBUG: return 'DEBUG';
				case Ametys.log.Logger.Entry.LEVEL_DEBUG: return 'INFO';
				case Ametys.log.Logger.Entry.LEVEL_DEBUG: return 'WARNING';
				case Ametys.log.Logger.Entry.LEVEL_DEBUG: return 'ERROR';
				case Ametys.log.Logger.Entry.LEVEL_DEBUG: return 'FATAL';
			}
		},

		/**
		 * Get the category of the message
		 * @return {String} The category
		 */
		getCategory: function() {
			return this._category;
		},

		/**
		 * Get the date of the message
		 * @return {Date} The date
		 */
		getDate: function() {
			return this._date;
		},

		/**
		 * Get the message
		 * @return {String} The message
		 */
		getMessage: function() {
			return this._message;
		},

		/**
		 * Get the detailed message
		 * @return {String} The detailed message. Can be null.
		 */
		getDetails: function() {
			return this._details;
		},
		
		/**
		 * Convert to a readable string
		 * @return {String} the entry as a string
		 */
		toString: function() {
			return "[" + this.getLevelAsString() + "] [" + this._category + "] " + Ext.Date.format(this._date, Ext.Date.patterns.ISO8601Long) + " " + this.message + " /// " + (this._details || '');
		}
	}
);
