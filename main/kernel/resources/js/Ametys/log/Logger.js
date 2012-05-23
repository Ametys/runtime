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
 * A logger
 */
Ext.define(
	"Ametys.log.Logger",
	{
		singleton: true,
		
		/**
		 * @private
		 * @property {Ametys.log.Logger.Entry[]} _entries The ordered log entries 
		 */
		_entries: [],

		/**
		 * Get the entries of the logger.
		 * @return {Ametys.log.Logger.Entry[]} The ordered entries.
		 */
		getEntries: function()
		{
			return this._entries;
		},

		/**
		 * Log as debug
		 * @param {String} category {@see Ametys.log.Logger.Entry}
		 * @param {String} message {@see Ametys.log.Logger.Entry}
		 * @param {String} details {@see Ametys.log.Logger.Entry}
		 */
		debug: function(category, message, details)
		{
			var entry = new Ametys.log.Logger.Entry(Ametys.log.Logger.Entry.LEVEL_DEBUG, category, new Date(), message, details);
			getInstance().getEntries().push(entry);
		},
		/**
		 * Log as info
		 * @param {String} category {@see Ametys.log.Logger.Entry}
		 * @param {String} message {@see Ametys.log.Logger.Entry}
		 * @param {String} details {@see Ametys.log.Logger.Entry}
		 */
		info: function(category, message, details)
		{
			var entry = new Ametys.log.Logger.Entry(Ametys.log.Logger.Entry.LEVEL_INFO, category, new Date(), message, details);
			getInstance().getEntries().push(entry);
		},
		/**
		 * Log as warning
		 * @param {String} category {@see Ametys.log.Logger.Entry}
		 * @param {String} message {@see Ametys.log.Logger.Entry}
		 * @param {String} details {@see Ametys.log.Logger.Entry}
		 */
		warning: function(category, message, details)
		{
			var entry = new Ametys.log.Logger.Entry(Ametys.log.Logger.Entry.LEVEL_WARNING, category, new Date(), message, details);
			getInstance().getEntries().push(entry);
		},
		/**
		 * Log as error
		 * @param {String} category {@see Ametys.log.Logger.Entry}
		 * @param {String} message {@see Ametys.log.Logger.Entry}
		 * @param {String} details {@see Ametys.log.Logger.Entry}
		 */
		error: function(category, message, details)
		{
			var entry = new Ametys.log.Logger.Entry(Ametys.log.Logger.Entry.LEVEL_ERROR, category, new Date(), message, details);
			getInstance().getEntries().push(entry);
		},
		/**
		 * Log as fatalerror
		 * @param {String} category {@see Ametys.log.Logger.Entry}
		 * @param {String} message {@see Ametys.log.Logger.Entry}
		 * @param {String} details {@see Ametys.log.Logger.Entry}
		 */
		fatalerror: function(category, message, details)
		{
			var entry = new Ametys.log.Logger.Entry(Ametys.log.Logger.Entry.LEVEL_FATALERROR, category, new Date(), message, details);
			getInstance().getEntries().push(entry);
		}
	}
);
