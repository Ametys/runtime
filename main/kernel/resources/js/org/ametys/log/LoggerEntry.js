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
 * Creates a log entry 
 * @constructor
 * @class This class creates a log entry
 * @param {integer} level A level between org.ametys.log.LoggerEntry.LEVEL_DEBUG, org.ametys.log.LoggerEntry.LEVEL_INFO, org.ametys.log.LoggerEntry.LEVEL_WARNING, org.ametys.log.LoggerEntry.LEVEL_ERROR or org.ametys.log.LoggerEntry.LEVEL_FATALERROR
 * @param {String} category The name of the category of the log entry
 * @param {Date} date The date of the entry
 * @param {String} message The message
 * @param {String} details The detailled message. Can be null or empty.   
 */
org.ametys.log.LoggerEntry = function (level, category, date, message, details)
{
	this._level = level;
	this._category = category;
	this._date = date;
	this._message = message;
	this._details = details;
}

/**
 * Level of entry for a debug message
 * @constant
 * @type {integer}
 */
org.ametys.log.LoggerEntry.LEVEL_DEBUG = 0;
/**
 * Level of entry for a information message
 * @constant
 * @type {integer}
 */
org.ametys.log.LoggerEntry.LEVEL_INFO = 1;
/**
 * Level of entry for a warning message
 * @constant
 * @type {integer}
 */
org.ametys.log.LoggerEntry.LEVEL_WARNING = 2;
/**
 * Level of entry for an error message
 * @constant
 * @type {integer}
 */
org.ametys.log.LoggerEntry.LEVEL_ERROR = 3;
/**
 * Level of entry for a fatal error message
 * @constant
 * @type {integer}
 */
org.ametys.log.LoggerEntry.LEVEL_FATALERROR = 4;

/**
 * @private
 * @property {integer} _level The level of log entry (debug, info...). Use the constants.
 */
org.ametys.log.LoggerEntry.prototype._level;
/**
 * Get the level of the log entry.
 * @return {integer} A constant between org.ametys.log.LoggerEntry.LEVEL_DEBUG, org.ametys.log.LoggerEntry.LEVEL_INFO, org.ametys.log.LoggerEntry.LEVEL_WARNING, org.ametys.log.LoggerEntry.LEVEL_ERROR or org.ametys.log.LoggerEntry.LEVEL_FATALERROR
 */
org.ametys.log.LoggerEntry.prototype.getLevel = function()
{
	return this._level;
}

/**
 * @private
 * @property {String} _category The category of the message
 */
org.ametys.log.LoggerEntry.prototype._category;
/**
 * Get the category of the message
 * @return {String} The category
 */
org.ametys.log.LoggerEntry.prototype.getCategory = function()
{
	return this._category;
}

/**
 * @private
 * @property {Date} _date The date of the message
 */
org.ametys.log.LoggerEntry.prototype._date;
/**
 * Get the date of the message
 * @return {Date} The date
 */
org.ametys.log.LoggerEntry.prototype.getDate = function()
{
	return this._date;
}

/**
 * @private
 * @property {String} _message The message
 */
org.ametys.log.LoggerEntry.prototype._message;
/**
 * Get the message
 * @return {String} The message
 */
org.ametys.log.LoggerEntry.prototype.getMessage = function()
{
	return this._message;
}

/**
 * @private
 * @property {String} _details The detailed message. Can be null
 */
org.ametys.log.LoggerEntry.prototype._details;
/**
 * Get the detailed message
 * @return {String} The detailed message. Can be null.
 */
org.ametys.log.LoggerEntry.prototype.getDetails = function()
{
	return this._details;
}