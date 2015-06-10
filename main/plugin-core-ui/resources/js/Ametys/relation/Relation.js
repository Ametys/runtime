/*
 *  Copyright 2014 Anyware Services
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
 * This class represents a relation.
 */
Ext.define("Ametys.relation.Relation", 
	{
		statics:
		{
			/**
			 * @readonly
			 * @property {String} MOVE To establish a relation where object could be moved to a new place at the end of the operation.
			 */
			MOVE: "move",
			/**
			 * @readonly
			 * @property {String} COPY To establish a relation where object could be copied to a new place at the end of the operation.
			 */
			COPY: "copy",
			/**
			 * @readonly
			 * @property {String} REFERENCE To establish a relation where object could be referenced from a new place at the end of the operation.
			 */
			REFERENCE: "reference"
		},
	
		config: 
		{
			/** @cfg {String} type (required) Describe the type of relation: one of the constants #MOVE, #COPY or #REFERENCE. */
			type: null,
			/** @cfg {String} label (required) A readable label for this operation that would appear in the contextual relation menu. */
			label: '',
			/** @cfg {String} description=null A readable description for this operation that would appear in the contextual relation menu hint. */
			description: null,
			/** @cfg {String} smallIcon=null A small sized icon (16x16 pixels) that would appear in the contextual relation menu. */
			smallIcon: null,
			/** @cfg {String} mediumIcon=smallIcon A medium sized icon (32x32 pixels) that would appear in the contextual relation menu hint. */
			mediumIcon: null,
			/** @cfg {String} largeIcon=mediumIcon A large sized icon (48x48 pixels) that would appear in the contextual relation menu hint. */
			largeIcon: null
		},
		
		/**
		 * Creates a Relation
		 * @param {Object} config The configuration.
		 */
		constructor: function(config)
		{
			this.initConfig(config);
		}
	}
);
