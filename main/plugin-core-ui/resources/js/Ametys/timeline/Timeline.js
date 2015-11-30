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

Ext.define('Ametys.timeline.Timeline', {
	extend: 'Ext.grid.Panel',
	
	cls: 'a-timeline',
	hideHeaders: true,
	
	/**
	 * @cfg {Boolean} groupByDay=true Set to false to disable day grouping
	 */
	groupByDay: true,
	
	/**
	 * @cfg {String[]/String} timelineItemHTML The HTML fragment as a single String or an Array of String to use for item of the timeline 
	 */
	timelineItemHTML: ['<div class="timeline-item {type}">',
	                  		'<div class="profile-img-wrap">',
	                  			'<img src="{profileImg}" alt="">',
	                  			'<div>{hour}</div>',
	                  		'</div>',
	                  		'<div class="contents-wrap">',
	                  			'<span class="vertical-line"></span>',
	                  			'<tpl if="topText && topText != \'\'">',
	                  				'<div class="top">{topText}</div>',
	                  			'</tpl>',
	                  			'<div class="text">{text}</div>',
	                  			'<tpl if="comment && comment != \'\'">',
	                  				'<div class="comment"><span class="x-fa fa-quote-left"></span>{comment}</div>',
	                  			'</tpl>',
	                  		'</div>',
	                  	'</div>'],	
	 
	/**
	 * @private
	 * @property {Ext.XTemplate} _timelineItemTpl HTML fragment template for items of time line
	 */                  	
	_timelineItemTpl: null,
	
	constructor: function (config)
	{
		config.store = Ext.create('Ametys.timeline.Timeline.TimelineStore', {});

		config.columns = [{
	        xtype: "gridcolumn",
	        flex: 1,
	        renderer: Ext.bind(this._columnRenderer, this)
	    }]
		
		config.features = [{
	        groupHeaderTpl: ['<div class="timeline-day">{name}</div>'],
	        ftype: 'grouping',
	        collapsible: false
	    }];
		
		this.callParent(arguments);
		
		this._timelineItemTpl = Ext.create ('Ext.XTemplate', this.timelineItemHTML);
		this._timelineItemTpl.compile();
	},
	
	/**
	 * @private
	 * Transforms a timeline record into a beautiful HTML fragment
	 * @param {Object} value the data value
	 * @param {Object} metaData A collection of metadata about the current cell
	 * @param {Ext.data.Model} record The record 
	 * @param {Number} index The index of the current row
	 * @return
	 */
	_columnRenderer: function (value, metaData, record, index)
	{
		return this._timelineItemTpl.apply(record.data);
	}
});

Ext.define('Ametys.timeline.Timeline.TimelineItem', {
    extend : 'Ext.data.Model',

    fields : [ 'id',
               'username',
               'profileImg',
               {name: 'type',  type: 'string', defaultValue: 'info'},
               { name: 'read', type: 'boolean', defaultValue: false},
               { name: 'date', type: 'date'},
		       {
		    		name: 'day',
		    		depends: ['date'],
		    		calculate: function (data)
		    		{
		    			var formattedDate = Ext.Date.format(data.date, 'd/m/y');
		    			var today = Ext.Date.format(new Date(), 'd/m/y');
		    			return formattedDate == today ? "<i18n:text i18n:key='PLUGINS_CORE_UI_TIMELINE_TODAY'/>" : Ext.Date.format(data.date, 'd/m/y');
		    		}
		       },
		       {
			   		name: 'hour',
			   		depends: ['date'],
			   		calculate: function (data)
			   		{
			   			return Ext.Date.format(data.date, 'H\\hi');
			   		}
		      },
		      'text',
		      'comment',
		      'topText',
		      'icon'
     ]
});

Ext.define('Ametys.timeline.Timeline.TimelineStore', {
    extend : 'Ext.data.Store',

    model: 'Ametys.timeline.Timeline.TimelineItem',
    
    data: [],
    
    sorters: [{
        property: 'date',
        direction: 'DESC'
    }],
    
    groupField : 'day',
    groupDir: 'DESC'
});



