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
 * A logger that will trace into its internal store and in the browser console if available.
 * Its store is a store of Ametys.log.Logger.Entry
 * 
 * To have a logger you can use getLogger method on any object. See Ext.Base#getLogger
 * 
 * 		try
 *      {
 *           if (this.getLogger().isDebugEnabled())
 *           {
 *            	this.getLogger().debug('my debug message');
 *           }
 *           
 *           // some error code
 *      }
 *      catch(e)
 *      {
 *      	this.getLogger().error({ message: 'error!', details: e});
 *      }
 * 
 * If you want a logger with a customized category use #getLoggerFor
 * 
 * 		var myLogger = Ametys.log.LoggerFactory.getLoggerFor("my.custom.category");
 * 		try
 * 		{
 * 			if (myLogger.isDebugEnabled())
 * 			{
 * 				myLogger.debug('My debug message');
 * 			}
 * 
 * 			// some error code
 * 		}
 * 		catch (e)
 * 		{
 * 			myLogger.error({
 * 				message: 'My error message',
 * 				details: e
 * 			});
 * 		}
 * 
 * Level under the currentLogLevel will be ignored.
 * For example, default log level is LEVEL_ERROR, logging a warn will be ignored
 */
Ext.define(
	"Ametys.log.LoggerFactory",
	{
		singleton: true,
		mixins: {
			state: 'Ext.state.Stateful'
		},
		
		/**
		 * @private
		 * @property {Boolean} _stateInitialized=false Has the stateful already been initialized
		 */
		
		/**
		 * @private
		 * @property {Ext.data.ArrayStore} _store The store of log entries 
		 */
		_store: Ext.create("Ext.data.ArrayStore", {
			autoSync: true,
			model: "Ametys.log.Logger.Entry",
			proxy: { type: 'memory' },	
			sorters: [{property: 'date', direction:'DESC'}]
		}),
		
		/**
		 * @private
		 * @property {Number} _currentLogLevel The current log level. If a log is created with a lower level it will be ignored 
		 */
		_currentLogLevel: {'' : 3},
		
		/**
		 * Get the current log level
		 * @param {String} [category=''] The current loglevel is computed uppon the category
		 * @returns {Number} A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 * @private
		 */
		_getLogLevel: function(category)
		{
			if (!this._stateInitialized)
			{
				this._stateInitialized = true;
				// restore state to overwrite values
                this.stateful = true;
                this.stateId = this.self.getName();
                this.hasListeners = {}; // used by saveState                
				this.mixins.state.constructor.call(this);
			}
			
			var cat = category || '';
			while (this._currentLogLevel[cat] == null)
			{
				var i = cat.lastIndexOf('.');
				if (i == -1)
				{
					cat = '';
				}
				else
				{
					cat = cat.substr(0, i);
				}
			}
			return this._currentLogLevel[cat];
		},
		
		/**
		 * Get a logger for the given category.
		 * @param {String} [category=''] The category for the logger. Can be a string, or a class name. The character '.' is a separator from parent categories.
		 * @return {Ametys.log.Logger} The logger. Cannot be null.
		 */
		getLoggerFor: function(category)
		{
			return Ext.create("Ametys.log.Logger", {category: category});
		},

		/**
		 * Set the current log level.
		 * @param {Number} logLevel A level between Ametys.log.Logger.Entry.LEVEL_DEBUG, Ametys.log.Logger.Entry.LEVEL_INFO, Ametys.log.Logger.Entry.LEVEL_WARN, Ametys.log.Logger.Entry.LEVEL_ERROR or Ametys.log.Logger.Entry.LEVEL_FATAL
		 * @param {String} [category=''] The current loglevel is computed uppon the category
		 * @private
		 */
		_setLogLevel: function(logLevel, category)
		{
			category = category || '';
			
			if (logLevel == null && category)
			{
				delete this._currentLogLevel[category];
			}
			else
			{
				logLevel = logLevel != null ? logLevel : 3;
				this._currentLogLevel[category] = Math.min(Math.max(logLevel, Ametys.log.Logger.Entry.LEVEL_DEBUG), Ametys.log.Logger.Entry.LEVEL_FATAL);
			}
			this.saveState();
		},
		
        /**
         * @private
         * Used by #saveState to delegate save to plugins.
         * @return {Ext.plugin.Abstract[]} The plugins. Will be null here.
         */
        getPlugins: function() 
        {
            return null;
        },
        
		getState: function()
		{
			return {
				_currentLogLevel: this._currentLogLevel
			}
		},
		
		/**
		 * Get the entries of the logger.
		 * @return {Ext.data.ArrayStore} The ordered entries.
		 */
		getStore: function()
		{
			return this._store;
		}
	}
);
