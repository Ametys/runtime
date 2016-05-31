/*
 *  Copyright 2016 Anyware Services
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
 * @private
 * This class provides a table of contents for a {@link Ametys.form.ConfigurableFormPanel}
 */
Ext.define('Ametys.form.ConfigurableFormPanel.TableOfContents', {
    extend: 'Ext.panel.Panel',
	
    statics: {
        /**
         * @property {Number} NAVIGATION_ITEM_HEIGHT The height of a navigation item
         * @private
         * @readonly 
         */
    	NAVIGATION_ITEM_HEIGHT: 40
    },
        
    /**
     * @private
     * @property {Object} _navigationMap the mapping of fieldset ids with their corresponding navigation item ids
     */
    
	/**
	 * @private
	 * @property {Ametys.form.ConfigurableFormPanel} _form the configurable form panel instance attached to this table of contents instance
	 */
    
    /**
     * @private
     * @property {String} _currentFieldsetId the id of the highest visible fieldset of the {@link Ametys.form.ConfigurableFormPanel}
     */
    
    /**
     * @private
     * @property {Boolean} _bound Is the scroll currently bound to the table of contents ?
     */
    
    /**
     * @private
     * @property {Boolean} _outOfTabsFieldset true if there is an out of tabs fieldset
     */
    
    /**
     * @private
     * @property {Ext.fx.Anim/Boolean} _animation the current animation if this object has any effects actively running or queued, false otherwise
     */
    
    /**
     * @private
     * @property {Boolean} _scrollingHandled True if the scrolling of the form is currently handled, false otherwise
     */
    
	initComponent: function()
	{
    	Ext.applyIf(this, {
            layout: {
                type: 'vbox'
            }
        });

        Ext.apply(this, {
            border: true,
            shadow: false,
            
            cls: 'table-of-contents',
            
            header: {
            	title: '{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TABLE_OF_CONTENTS_TITLE}}',
            	height: 50
            }
        });

        this.callParent(arguments);
	},
	
	constructor: function(config)
	{
		this._navigationMap = {};
		this._bound = true;
		this._scrollingHandled = false;
		
		this.callParent(arguments);
	},
	
	/**
	 * Set up the listeners on the scroll and select the current navigation item
	 */
	initializeListeners: function()
	{
		this.form.getFormContainer().on('afterlayout', Ext.bind(this._updateScrollPosition, this), undefined, {single: true});
	},
	
	/**
	 * Add a new navigation item to the table of contents
	 * @param {String} label the label of the thumbnail to create
	 * @param {String} fieldsetId the id of the corresponding fieldset
	 */
	addNavigationItem: function(label, fieldsetId)
	{
		var navigationItemId = Ext.id();
		var navigationItemCfg = 
		{
			id: navigationItemId, 
			title : label, 
			width: 350,
			padding: '0 0 0 10',
			height: this.self.NAVIGATION_ITEM_HEIGHT,
			
			listeners: {
				'render': {fn: Ext.bind(this._setClickListener, this, [fieldsetId], 1), scope: this}
			}
		};
		
		if (fieldsetId != Ametys.form.ConfigurableFormPanel.OUTOFTAB_FIELDSET_ID)
		{
			// Store the mapping
			this._navigationMap[fieldsetId] = navigationItemId;
			this.add(navigationItemCfg);
		}
		else
		{
			// The out of tabs fieldset goes first
			this._outOfTabsFieldset = true;
			this.insert(0, navigationItemCfg);
		}
	},
	
	/**
	 * Get the navigation item corresponding to the given fieldset id
	 * @param {String} fieldsetId the id of the fieldset
	 * @return {Ext.Component} the navigation item component
	 */
	getNavigationItem: function(fieldsetId)
	{
		if (fieldsetId == Ametys.form.ConfigurableFormPanel.OUTOFTAB_FIELDSET_ID)
		{
			return this.items.get(0);
		}
		else
		{
			return Ext.getCmp(this._navigationMap[fieldsetId]);
		}
	},
	
    /**
     * @private
     * Set the click listener on the component to scroll to the corresponding fieldset
     * @param {Ext.Component} component the component of the navigation item
     * @param {String} fieldsetId the id of the fieldset to set a click listener on to 
     */
	_setClickListener: function(component, fieldsetId)
	{
		component.getEl().on('click', Ext.bind(this._scrollToFieldset, this, [fieldsetId], false));
	},
	
	/**
	 * @private
	 * Scroll to a fieldset of the attached {@link Ametys.form.ConfigurableFormPanel}
	 * @param {String} fieldsetId the id of the fieldset
	 */
	_scrollToFieldset: function(fieldsetId)
	{
		this._bound = false;
		
		var formContainer = this.form.getFormContainer();
		if (fieldsetId == Ametys.form.ConfigurableFormPanel.OUTOFTAB_FIELDSET_ID)
		{
			formContainer.scrollTo(0, 0, {callback: this._bindScroll, scope: this});
		}
		else
		{
    		var fieldset = Ext.getCmp(fieldsetId);
			
			var newTop = fieldset.getPosition()[1]; 
			var formTop = formContainer.getPosition()[1];
			
			formContainer.scrollBy(0, newTop - formTop, {callback: this._bindScroll, scope: this});
		}
		
		if  (this._currentFieldsetId != fieldsetId)
		{
			this._activateNavigationItem(fieldsetId);
		}
	},
	
	/**
	 * @private
	 * Bind the scroll to the table of contents
	 */
	_bindScroll: function()
	{
		this._bound = true;
	},
	
	/**
	 * @private
	 * Activates the correct navigation item relatively to the current scroll position 
	 */
	_updateScrollPosition: function ()
	{
		// Make sure the scrolling is listened to after the form is displayed
		if (!this._scrollingHandled)
		{
			this._scrollingHandled = true;
			this.form.getFormContainer().getEl().on('scroll', Ext.bind(this._updateScrollPosition, this));
		}
		
		// Wait for the scroll by click on a navigation item to finish before updating the scroll position
		if (!this._bound || this._animation)
		{
			this._animation = this.form.getFormContainer().getActiveAnimation();
			return;
		}

		var withinTabsHeight = 0; // The summed up height of all tabs
		
		Ext.Object.each(this._navigationMap, function(fieldsetId) {
			var fieldsetHeight = Ext.getCmp(fieldsetId).getEl().getHeight();
			withinTabsHeight += fieldsetHeight;
		}, this);
		
		var formContainer = this.form.getFormContainer();
		var e = formContainer.getEl();
		var max = e.dom.scrollHeight - e.getHeight();
		
		var scrollPosition = e.dom.scrollTop;
		var scrollRatio = scrollPosition / max;
		
		var p = scrollRatio * this.form.body.getHeight();
		
		var outOfTabsHeight = 0;
		// Handle the out of tabs fieldset separately
		if (this._outOfTabsFieldset)
		{
			outOfTabsHeight = formContainer.getEl().dom.scrollHeight - withinTabsHeight;
			if (outOfTabsHeight > scrollPosition + p)
			{
				this._activateNavigationItem(Ametys.form.ConfigurableFormPanel.OUTOFTAB_FIELDSET_ID);
				return;
			}
		}
		
		// Within tabs
		var fieldsetIds = Ext.Object.getKeys(this._navigationMap);
		var a0 = Ext.get(fieldsetIds[0]).getTop() - outOfTabsHeight;
		
		for (var i = 0; i < fieldsetIds.length; i++)
		{
			var fieldsetId = fieldsetIds[i];
			if (i > 0)
			{
				last = fieldsetIds[i - 1];
			}
			else
			{
				last = fieldsetId;
			}
			
			var posY = Ext.get(fieldsetId).getTop() - a0;
			if (posY >= scrollPosition + p)
			{
				this._activateNavigationItem(last);
				return;
			}
		}
		
		this._activateNavigationItem(fieldsetIds[fieldsetIds.length - 1]);
	},
	
	/**
	 * @private
	 * Visually activate the current navigation item
	 * @param {String} fieldsetId the id of the fieldset to activate
	 */
	_activateNavigationItem: function(fieldsetId)
	{
		if (this._currentFieldsetId)
		{
			var oldNavigationItem = Ext.getCmp(this._navigationMap[this._currentFieldsetId]) || this.items.get(0);
			// Remove the 'activated' rendering
			oldNavigationItem.setStyle('border', 'none');
		}

		var currentItem = Ext.getCmp(this._navigationMap[fieldsetId]) || this.items.get(0);
		currentItem.setStyle('border', '1px solid #0a7fb2');
		
		if (this._currentFieldsetId)
		{
			var previousItem = Ext.getCmp(this._navigationMap[this._fieldsetId]) || this.items.get(0);
			
			var previousTop = previousItem.getPosition()[1];
			var currentTop = currentItem.getPosition()[1]; 
			var gap = currentTop - previousTop; 
			
			if (currentItem != this.items.get(0))
			{
				gap = gap > 0 ? gap - this.self.NAVIGATION_ITEM_HEIGHT : gap + this.self.NAVIGATION_ITEM_HEIGHT;
			}
			
			this.scrollTo(0, gap);
		}
		
		this._currentFieldsetId = fieldsetId;
	}
});