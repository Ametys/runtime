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

Ext.namespace('org.ametys.log');

/**
 * Creates and display directly an error message dialog box 
 * @constructor
 * @class This class creates a dialog box of error message  
 */
org.ametys.log.LoggerManager = function ()
{
	this._entries = [];
}

/**
 * @property {org.ametys.log.LoggerManager} _instance Unique instance.
 * @private
 */
org.ametys.log.LoggerManager._instance;
/**
 * This method returns the unique instance of the org.ametys.log.LoggerManager
 * @static
 * @returns {org.ametys.log.LoggerManager} The unique instance of the org.ametys.log.LoggerManager class.
 */
org.ametys.log.LoggerManager.getInstance = function()
{
	if (org.ametys.log.LoggerManager._instance == null)
	{
		org.ametys.log.LoggerManager._instance = new org.ametys.log.LoggerManager();
	}
	return org.ametys.log.LoggerManager._instance;
}

/**
 * @private
 * @property {org.ametys.log.LoggerEntry[]} _entries The ordered log entries
 */
org.ametys.log.LoggerManager.prototype._entries;
/**
 * Get the entries of the logger.
 * @return {org.ametys.log.LoggerEntry[]} The ordered entries.
 */
org.ametys.log.LoggerManager.prototype.getEntries = function()
{
	return this._entries;
}

/**
 * Log as debug
 * @param {String} category {@see org.ametys.log.LoggerEntry}
 * @param {String} message {@see org.ametys.log.LoggerEntry}
 * @param {String} details {@see org.ametys.log.LoggerEntry}
 */
org.ametys.log.LoggerManager.debug = function(category, message, details)
{
	var entry = new org.ametys.log.LoggerEntry(org.ametys.log.LoggerEntry.LEVEL_DEBUG, category, new Date(), message, details);
	org.ametys.log.LoggerManager.getInstance().getEntries().push(entry);
}
/**
 * Log as info
 * @param {String} category {@see org.ametys.log.LoggerEntry}
 * @param {String} message {@see org.ametys.log.LoggerEntry}
 * @param {String} details {@see org.ametys.log.LoggerEntry}
 */
org.ametys.log.LoggerManager.info = function(category, message, details)
{
	var entry = new org.ametys.log.LoggerEntry(org.ametys.log.LoggerEntry.LEVEL_INFO, category, new Date(), message, details);
	org.ametys.log.LoggerManager.getInstance().getEntries().push(entry);
}
/**
 * Log as warning
 * @param {String} category {@see org.ametys.log.LoggerEntry}
 * @param {String} message {@see org.ametys.log.LoggerEntry}
 * @param {String} details {@see org.ametys.log.LoggerEntry}
 */
org.ametys.log.LoggerManager.warning = function(category, message, details)
{
	var entry = new org.ametys.log.LoggerEntry(org.ametys.log.LoggerEntry.LEVEL_WARNING, category, new Date(), message, details);
	org.ametys.log.LoggerManager.getInstance().getEntries().push(entry);
}
/**
 * Log as error
 * @param {String} category {@see org.ametys.log.LoggerEntry}
 * @param {String} message {@see org.ametys.log.LoggerEntry}
 * @param {String} details {@see org.ametys.log.LoggerEntry}
 */
org.ametys.log.LoggerManager.error = function(category, message, details)
{
	var entry = new org.ametys.log.LoggerEntry(org.ametys.log.LoggerEntry.LEVEL_ERROR, category, new Date(), message, details);
	org.ametys.log.LoggerManager.getInstance().getEntries().push(entry);
}
/**
 * Log as fatalerror
 * @param {String} category {@see org.ametys.log.LoggerEntry}
 * @param {String} message {@see org.ametys.log.LoggerEntry}
 * @param {String} details {@see org.ametys.log.LoggerEntry}
 */
org.ametys.log.LoggerManager.fatalerror = function(category, message, details)
{
	var entry = new org.ametys.log.LoggerEntry(org.ametys.log.LoggerEntry.LEVEL_FATALERROR, category, new Date(), message, details);
	org.ametys.log.LoggerManager.getInstance().getEntries().push(entry);
}
